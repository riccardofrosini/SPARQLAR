package io.sparqlar.optimisation;

import io.sparqlar.optimisation.exceptions.GraphException;
import io.sparqlar.rewriting.QueryCost;
import io.sparqlar.sparqlardc.CQSPARQL;
import io.sparqlar.sparqlardc.triplepatterns.TriplePattern;

public class ContainmentOptimisation {
    public static boolean contains(QueryCost<CQSPARQL> queryCostContainer, QueryCost<CQSPARQL> queryCostContained) throws GraphException {
        if (queryCostContainer.getCost() > queryCostContained.getCost()) {
            return false;
        }
        CQSPARQL queryContainer = queryCostContainer.getQuery();
        CQSPARQL queryContained = queryCostContained.getQuery();
        for (TriplePattern triplePatternContainer : queryContainer) {
            boolean triplePatternContains = false;
            for (TriplePattern triplePatternContained : queryContained) {
                if (triplePatternContainer.getS().equals(triplePatternContained.getS()) &&
                        triplePatternContainer.getO().equals(triplePatternContained.getO()) &&
                        GraphUtils.createGraphFromPredicate(triplePatternContainer.getP())
                                .canSimulate(GraphUtils.createGraphFromPredicate(triplePatternContained.getP()))) {
                    triplePatternContains = true;
                }
            }
            if (!triplePatternContains) {
                return false;
            }
        }
        return true;
    }
}
