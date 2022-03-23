/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.sparqlardc.querypatterns;

import io.sparqlar.sparqlardc.triplepatterns.Flex;
import io.sparqlar.sparqlardc.triplepatterns.TriplePattern;

import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

public class JoinQueryPattern<T extends QueryPattern> extends ComplexQueryPattern<T> {

    public JoinQueryPattern(HashSet<T> qps) {
        super(qps);
    }

    @Override
    public int hashCode() {
        return 47 * Objects.hashCode(this.queryPatterns) + 7;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof JoinQueryPattern<?>)) {
            return false;
        }
        final JoinQueryPattern<?> other = (JoinQueryPattern<?>) obj;
        return Objects.equals(this.queryPatterns, other.queryPatterns);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(queryPatterns.stream().map(Object::toString).collect(Collectors.joining(" . ")))
                .toString();
    }

    @Override
    public String toStringExecutable() {
        return new StringBuilder()
                .append(queryPatterns.stream().map(QueryPattern::toStringExecutable).collect(Collectors.joining(" . ")))
                .toString();
    }

    @Override
    public HashSet<HashSet<TriplePattern>> toJoinOfTriplePattern() {
        HashSet<HashSet<TriplePattern>> set = new HashSet<>();
        for (T qp : queryPatterns) {
            HashSet<HashSet<TriplePattern>> temp = new HashSet<>();
            HashSet<HashSet<TriplePattern>> toJoinOfTriplePattern = qp.toJoinOfTriplePattern();
            if (set.isEmpty()) {
                set.addAll(toJoinOfTriplePattern);
            } else {
                for (HashSet<TriplePattern> tp1 : toJoinOfTriplePattern) {
                    for (HashSet<TriplePattern> tp2 : set) {
                        HashSet<TriplePattern> subTemp = new HashSet<>(tp1);
                        subTemp.addAll(tp2);
                        temp.add(subTemp);
                    }
                }
                set = temp;
            }
        }
        return set;
    }

}
