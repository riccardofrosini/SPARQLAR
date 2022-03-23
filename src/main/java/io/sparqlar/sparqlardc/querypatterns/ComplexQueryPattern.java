/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.sparqlardc.querypatterns;

import io.sparqlar.sparqlardc.terms.URI;
import io.sparqlar.sparqlardc.terms.Variable;
import io.sparqlar.sparqlardc.triplepatterns.TriplePattern;

import java.util.HashSet;
import java.util.Iterator;

/**
 * @param <T>
 * @author Riccardo
 */
public abstract class ComplexQueryPattern<T extends QueryPattern> extends QueryPattern implements Iterable<T> {

    protected final HashSet<T> queryPatterns;

    public ComplexQueryPattern(HashSet<T> queryPatterns) {
        this.queryPatterns = queryPatterns;
    }

    @Override
    public abstract HashSet<HashSet<TriplePattern>> toJoinOfTriplePattern();

    public HashSet<T> cloneQueryPattern() {
        return new HashSet<>(queryPatterns);
    }

    @Override
    public boolean isEmpty() {
        return queryPatterns.isEmpty();
    }

    @Override
    public boolean containsApprox() {
        for (T qp : queryPatterns) {
            if (qp.containsApprox()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsRelax() {
        for (T qp : queryPatterns) {
            if (qp.containsRelax()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public HashSet<URI> getApproximatedURIs() {
        HashSet<URI> ret = new HashSet<>();
        for (T qp : queryPatterns) {
            ret.addAll(qp.getApproximatedURIs());
        }
        return ret;
    }

    @Override
    public HashSet<Variable> getVariableSet() {
        HashSet<Variable> ret = new HashSet<>();
        for (T q : queryPatterns) {
            ret.addAll(q.getVariableSet());
        }
        return ret;
    }

    @Override
    public Iterator<T> iterator() {
        return queryPatterns.iterator();
    }
}
