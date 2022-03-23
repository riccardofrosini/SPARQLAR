/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.sparqlardc.querypatterns;

import io.sparqlar.sparqlardc.triplepatterns.TriplePattern;

import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

public class UnionQueryPattern<T extends QueryPattern> extends ComplexQueryPattern<T> {

    public UnionQueryPattern(HashSet<T> qps) {
        super(qps);
    }

    @Override
    public int hashCode() {
        return 47 * Objects.hashCode(this.queryPatterns) + 3;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof UnionQueryPattern<?>)) {
            return false;
        }
        final UnionQueryPattern<?> other = (UnionQueryPattern<?>) obj;
        return Objects.equals(this.queryPatterns, other.queryPatterns);
    }

    @Override
    public String toString() {
        return queryPatterns.stream().map(Object::toString).collect(Collectors.joining("} UNION {", "{", "}"));
    }

    @Override
    public String toStringExecutable() {
        return queryPatterns.stream().map(QueryPattern::toStringExecutable).collect(Collectors.joining("} UNION {", "{", "}"));
    }

    @Override
    public HashSet<HashSet<TriplePattern>> toJoinOfTriplePattern() {
        HashSet<HashSet<TriplePattern>> set = new HashSet<>();
        for (QueryPattern qp : queryPatterns) {
            set.addAll(qp.toJoinOfTriplePattern());
        }
        return set;
    }
}
