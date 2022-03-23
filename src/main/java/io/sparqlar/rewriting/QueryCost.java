/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.rewriting;


import io.sparqlar.sparqlardc.SPARQL;

import java.util.Objects;

public class QueryCost<T extends SPARQL<?>> implements Comparable<QueryCost<?>> {
    private final T query;
    private final float cost;

    public QueryCost(T query, float cost) {
        this.query = query;
        this.cost = cost;
    }

    public float getCost() {
        return cost;
    }

    @Override
    public int compareTo(QueryCost<?> qc) {
        if (cost > qc.cost) return 1;
        if (cost == qc.cost) {
            if (query.equals(qc.query)) return 0;
            if (hashCode() > qc.hashCode()) return 1;
        }
        return -1;
    }

    public T getQuery() {
        return query;
    }

    @Override
    public String toString() {
        return query.toStringExecutable();
    }

    public boolean isAsk() {
        return query.isAsk();
    }

    public boolean isEmptyQuery() {
        return query.isEmptyQuery();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof QueryCost<?>)) {
            return false;
        }
        final QueryCost<?> other = (QueryCost<?>) obj;
        if (!this.query.equals(other.query)) {
            return false;
        }
        return this.cost == other.cost;
    }

    @Override
    public int hashCode() {
        return Objects.hash(query, cost);
    }
}
