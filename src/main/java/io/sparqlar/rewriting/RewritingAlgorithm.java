/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.rewriting;

import io.sparqlar.optimisation.ContainmentOptimisation;
import io.sparqlar.optimisation.Graph;
import io.sparqlar.optimisation.GraphUtils;
import io.sparqlar.optimisation.exceptions.GraphException;
import io.sparqlar.rewriting.exceptions.OptimisationContainmentException;
import io.sparqlar.rewriting.exceptions.OptimisationSchemaException;
import io.sparqlar.sparqlardc.CQSPARQL;
import io.sparqlar.sparqlardc.SPARQL;
import io.sparqlar.sparqlardc.triplepatterns.Flex;
import io.sparqlar.sparqlardc.triplepatterns.Predicate;
import io.sparqlar.sparqlardc.triplepatterns.TriplePattern;

import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

/**
 * @author riccardo
 */
public class RewritingAlgorithm {

    private static final Logger logger = Logger.getLogger(RewritingAlgorithm.class.getName());

    private static List<QueryCost<CQSPARQL>> rewrite(SPARQL<?> q, float k, Approximating ac, Relaxing rc) {
        HashMap<CQSPARQL, Float> oldGeneration = new HashMap<>();
        HashMap<CQSPARQL, Float> retSet = new HashMap<>();
        HashSet<CQSPARQL> toUCQ = q.toUCQ();
        for (CQSPARQL cq : toUCQ) {
            oldGeneration.put(cq, 0f);
            retSet.put(cq, 0f);
        }
        while (!oldGeneration.isEmpty()) {
            HashMap<CQSPARQL, Float> newGeneration = new HashMap<>();
            for (Entry<CQSPARQL, Float> qEntry : oldGeneration.entrySet()) {
                for (TriplePattern tp : qEntry.getKey()) {
                    HashMap<CQSPARQL, Float> rewritten = new HashMap<>();
                    if (tp.containsApprox()) {
                        rewritten.putAll(ac.applyApprox(qEntry.getKey(), (Flex) tp));
                    }
                    if (tp.containsRelax()) {
                        rewritten.putAll(rc.applyRelax(qEntry.getKey(), (Flex) tp));
                    }
                    for (Entry<CQSPARQL, Float> rEntry : rewritten.entrySet()) {
                        float newCost = rEntry.getValue() + qEntry.getValue();
                        if (newGeneration.containsKey(rEntry.getKey()) && newCost != newGeneration.get(rEntry.getKey()))
                            logger.severe("Something went wrong, same query generated at the same iteration with different costs; Query: " + rEntry.getKey() + ", new cost: " + newCost + ", old cost: " + newGeneration.get(rEntry.getKey()) + " \n");
                        if (newCost <= k &&
                                !newGeneration.containsKey(rEntry.getKey()) &&
                                (!retSet.containsKey(rEntry.getKey()) || newCost < retSet.get(rEntry.getKey()))) {
                            newGeneration.put(rEntry.getKey(), newCost);
                            retSet.put(rEntry.getKey(), newCost);
                        }
                    }
                }
            }
            oldGeneration = newGeneration;
        }
        ArrayList<QueryCost<CQSPARQL>> ret = new ArrayList<>(retSet.size());
        for (Entry<CQSPARQL, Float> map : retSet.entrySet()) {
            ret.add(new QueryCost<>(map.getKey(), map.getValue()));
        }
        Collections.sort(ret);
        return ret;
    }

    public static List<QueryCost<CQSPARQL>> rewrite(SPARQL<?> parse, float maxCost, Approximating approximating, Relaxing relaxing, Graph schema, boolean containment) throws OptimisationSchemaException, OptimisationContainmentException {
        try {
            List<QueryCost<CQSPARQL>> queries = rewrite(parse, maxCost, approximating, relaxing, containment);
            List<QueryCost<CQSPARQL>> ret = new ArrayList<>(queries.size());
            for (QueryCost<CQSPARQL> qc : queries) {
                logger.info("Simplifying query:");
                logger.info(qc.getQuery().toString());
                CQSPARQL q = returnSimplifiedQuery(qc.getQuery(), schema);
                if (q == null) {
                    logger.info("Removed!");
                } else {
                    ret.add(new QueryCost<>(q, qc.getCost()));
                }
            }
            logger.info("Number of queries generated after schema opt " + ret.size());
            return ret;
        } catch (GraphException e) {
            throw new OptimisationSchemaException(e);
        }
    }

    public static List<QueryCost<CQSPARQL>> rewrite(SPARQL<?> parse, float maxCost, Approximating approximating, Relaxing relaxing, boolean containment) throws OptimisationContainmentException {
        try {
            List<QueryCost<CQSPARQL>> queries = rewrite(parse, maxCost, approximating, relaxing);
            logger.info("Number of queries generated " + queries.size());
            if (containment) {
                queries = removeRedundantQueriesAndSimplify(queries);
                logger.info("Number of queries generated containment " + queries.size());
            }
            return queries;
        } catch (GraphException e) {
            throw new OptimisationContainmentException(e);
        }
    }

    private static List<QueryCost<CQSPARQL>> removeRedundantQueriesAndSimplify(List<QueryCost<CQSPARQL>> queries) throws GraphException {
        List<QueryCost<CQSPARQL>> queryCostsSimplified = new ArrayList<>(queries.size());
        for (QueryCost<CQSPARQL> query : queries) {
            CQSPARQL cq = query.getQuery();
            CQSPARQL toAdd = cq;
            for (TriplePattern triplePattern1 : cq) {
                for (TriplePattern triplePattern2 : cq) {
                    if (triplePattern1 != triplePattern2 &&
                            triplePattern1.getS().equals(triplePattern2.getS()) &&
                            triplePattern1.getO().equals(triplePattern2.getO()) &&
                            GraphUtils.createGraphFromPredicate(triplePattern1.getP())
                                    .canSimulate(GraphUtils.createGraphFromPredicate(triplePattern2.getP()))) {
                        toAdd = toAdd.removeTriplePatternIfDoesNotBecomeEmpty(triplePattern2);
                    }
                }
            }
            queryCostsSimplified.add(new QueryCost<>(toAdd, query.getCost()));
        }
        for (int i = 0; i < queryCostsSimplified.size(); i++) {
            for (int j = 0; j < queryCostsSimplified.size(); j++) {
                if (i != j) {
                    QueryCost<CQSPARQL> queryCostContainer = queryCostsSimplified.get(i);
                    QueryCost<CQSPARQL> queryCostContained = queryCostsSimplified.get(j);
                    if (ContainmentOptimisation.contains(queryCostContainer, queryCostContained)) {
                        logger.info("Query:");
                        logger.info(queryCostContainer.getQuery().toString());
                        logger.info("Removed query:");
                        logger.info(queryCostContained.getQuery().toString());
                        queryCostsSimplified.remove(j);
                        i--;
                        j--;
                    }
                }
            }
        }
        return queryCostsSimplified;
    }


    private static CQSPARQL returnSimplifiedQuery(CQSPARQL query, Graph schema) throws GraphException {
        CQSPARQL queryRet = query;
        for (TriplePattern t : query) {
            Predicate p = t.getP();
            Graph createGraph = GraphUtils.createGraphFromPredicate(p);
            Graph intersect = createGraph.intersect(schema);
            if (intersect.isEmpty()) {
                return null;
            } else if(intersect.size()<=50000) {
                queryRet = queryRet.replaceTriplePattern(t, new TriplePattern(t.getS(), intersect.getPropertyPath(), t.getO()));
            } else{
                System.out.println(t);
            }
        }
        return queryRet;
    }
}
