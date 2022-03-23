/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.sparqlardc;

import io.sparqlar.sparqlardc.querypatterns.JoinQueryPattern;
import io.sparqlar.sparqlardc.terms.SimpleURI;
import io.sparqlar.sparqlardc.terms.Variable;
import io.sparqlar.sparqlardc.triplepatterns.Approx;
import io.sparqlar.sparqlardc.triplepatterns.Relax;
import io.sparqlar.sparqlardc.triplepatterns.TriplePattern;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CQSPARQL extends SPARQL<JoinQueryPattern<TriplePattern>> implements Iterable<TriplePattern> {

    protected CQSPARQL(List<Variable> selectVariables, JoinQueryPattern<TriplePattern> queryPattern, HashMap<String, SimpleURI> prefixes, boolean distinct, int limit, String filter) {
        super(selectVariables, queryPattern, prefixes, distinct, limit, filter);
    }

    @Override
    public HashSet<CQSPARQL> toUCQ() {
        HashSet<CQSPARQL> hashSet = new HashSet<>();
        hashSet.add(this);
        return hashSet;
    }

    public CQSPARQL getExact() {
        List<Variable> vars = new ArrayList<>();
        HashSet<TriplePattern> tps = new HashSet<>();
        for (TriplePattern tp : queryPattern) {
            if (!(tp instanceof Approx) && !(tp instanceof Relax)) {
                if (tp.getO() instanceof Variable) {
                    if (!vars.contains(tp.getO())) {
                        vars.add((Variable) tp.getO());
                    }
                }
                if (tp.getS() instanceof Variable) {
                    if (!vars.contains(tp.getS())) {
                        vars.add((Variable) tp.getS());
                    }
                }
                tps.add(tp);
            }
        }
        return new CQSPARQL(vars, new JoinQueryPattern<>(tps), prefixes, true, 0, null);
    }

    public CQSPARQL getFlexed() {
        List<Variable> vars = new ArrayList<>();

        HashSet<TriplePattern> tps = new HashSet<>();
        for (TriplePattern tp : queryPattern) {
            if ((tp instanceof Approx) || (tp instanceof Relax)) {
                if (tp.getO() instanceof Variable) {
                    if (!vars.contains(tp.getO())) {
                        vars.add((Variable) tp.getO());
                    }
                }
                if (tp.getS() instanceof Variable) {
                    if (!vars.contains(tp.getS())) {
                        vars.add((Variable) tp.getS());
                    }
                }
                tps.add(tp);
            }
        }
        return new CQSPARQL(vars, new JoinQueryPattern<>(tps), prefixes, true, 0, null);
    }

    public CQSPARQL replaceTriplePattern(TriplePattern tp1, TriplePattern tp2) {
        HashSet<TriplePattern> cloneTriplePatterns = queryPattern.cloneQueryPattern();
        cloneTriplePatterns.remove(tp1);
        cloneTriplePatterns.add(tp2);
        return new CQSPARQL(selectVariables, new JoinQueryPattern<>(cloneTriplePatterns), prefixes, distinct, limit, filter);
    }

    public CQSPARQL removeTriplePatternIfDoesNotBecomeEmpty(TriplePattern tp) {
        HashSet<TriplePattern> cloneTriplePatterns = queryPattern.cloneQueryPattern();
        if (cloneTriplePatterns.size() == 1) {
            return this;
        }
        cloneTriplePatterns.remove(tp);
        return new CQSPARQL(selectVariables, new JoinQueryPattern<>(cloneTriplePatterns), prefixes, distinct, limit, filter);
    }

    @Override
    public int hashCode() {
        return 7 * super.hashCode() + 3;
    }

    public HashSet<CQSPARQL> getConnectedTriplePatterns() {
        HashSet<CQSPARQL> ret = new HashSet<>();
        HashSet<JoinQueryPattern<TriplePattern>> connected = new HashSet<>();
        boolean newAdded = true;
        while (newAdded) {
            newAdded = false;
            if (connected.isEmpty()) {
                for (TriplePattern triplePattern : queryPattern) {
                    HashSet<TriplePattern> newTriplePattern = new HashSet<>();
                    newTriplePattern.add(triplePattern);
                    JoinQueryPattern<TriplePattern> newJoinQueryPatter = new JoinQueryPattern<>(newTriplePattern);
                    CQSPARQL newCQ = new CQSPARQL(new ArrayList<>(newJoinQueryPatter.getVariableSet()), newJoinQueryPatter, prefixes, true, 0, null);
                    newAdded = true;
                    ret.add(newCQ);
                    connected.add(new JoinQueryPattern<>(newTriplePattern));
                }
            } else {
                for (JoinQueryPattern<TriplePattern> triplePatterns : connected) {
                    for (TriplePattern triplePattern : queryPattern) {
                        HashSet<TriplePattern> newTriplePattern = triplePatterns.cloneQueryPattern();
                        if (newTriplePattern.add(triplePattern)) {
                            JoinQueryPattern<TriplePattern> newJoinQueryPatter = new JoinQueryPattern<>(newTriplePattern);
                            CQSPARQL newCQ = new CQSPARQL(new ArrayList<>(newJoinQueryPatter.getVariableSet()), newJoinQueryPatter, prefixes, true, 0, null);
                            if (newCQ.isConnected()) {
                                newAdded = true;
                                ret.add(newCQ);
                                connected.add(newJoinQueryPatter);
                            }
                        }
                    }
                }
            }
        }
        return ret.stream().filter(query ->
                StreamSupport.stream(query.getExact().queryPattern.spliterator(), false).collect(Collectors.toSet()).equals(
                        StreamSupport.stream(getExact().queryPattern.spliterator(), false).collect(Collectors.toSet()))
        ).collect(Collectors.toCollection(HashSet::new));
    }

    public CQSPARQL getComplementary(CQSPARQL cqsparql) {
        HashSet<TriplePattern> collect = StreamSupport.stream(queryPattern.spliterator(), false).collect(Collectors.toCollection(HashSet::new));
        collect.removeAll(StreamSupport.stream(cqsparql.queryPattern.spliterator(), false).collect(Collectors.toCollection(HashSet::new)));
        JoinQueryPattern<TriplePattern> triplePatterns = new JoinQueryPattern<>(collect);
        return new CQSPARQL(new ArrayList<>(triplePatterns.getVariableSet()), triplePatterns, prefixes, true, 0, null);
    }

    private boolean isConnected() {
        HashSet<TriplePattern> connected = new HashSet<>();
        HashSet<Variable> variables = new HashSet<>();
        boolean newAdded = true;
        while (newAdded) {
            newAdded = false;
            for (TriplePattern triplePattern : queryPattern) {
                if (variables.isEmpty()) {
                    variables.addAll(triplePattern.getVariableSet());
                    connected.add(triplePattern);
                    newAdded = true;
                } else {
                    if ((variables.contains(triplePattern.getS()) || variables.contains(triplePattern.getO())) &&
                            !connected.contains(triplePattern)) {
                        variables.addAll(triplePattern.getVariableSet());
                        connected.add(triplePattern);
                        newAdded = true;
                    }
                }
            }
        }

        return connected.size() == StreamSupport.stream(queryPattern.spliterator(), false).count();

    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CQSPARQL)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public Iterator<TriplePattern> iterator() {
        return queryPattern.iterator();
    }
}
