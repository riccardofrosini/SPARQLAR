/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.sparqlardc;

import io.sparqlar.sparqlardc.querypatterns.JoinQueryPattern;
import io.sparqlar.sparqlardc.querypatterns.QueryPattern;
import io.sparqlar.sparqlardc.terms.SimpleURI;
import io.sparqlar.sparqlardc.terms.URI;
import io.sparqlar.sparqlardc.terms.Variable;
import io.sparqlar.sparqlardc.triplepatterns.TriplePattern;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author riccardo
 */
public class SPARQL<T extends QueryPattern> {
    protected final List<Variable> selectVariables;
    protected final T queryPattern;
    protected final HashMap<String, SimpleURI> prefixes;
    protected final boolean distinct;
    protected final int limit;
    protected final String filter;

    public SPARQL(List<Variable> selectVariables, T queryPattern, HashMap<String, SimpleURI> prefixes, boolean distinct, int limit, String filter) {
        this.selectVariables = selectVariables;
        this.queryPattern = queryPattern;
        this.prefixes = prefixes;
        this.distinct = distinct;
        this.limit = limit;
        this.filter = filter;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.selectVariables, this.queryPattern, this.prefixes, this.filter, this.distinct, this.limit);
    }

    public HashSet<URI> getApproximatedURIs() {
        return queryPattern.getApproximatedURIs();
    }

    public boolean containsApprox() {
        return queryPattern.containsApprox();
    }

    public boolean containsRelax() {
        return queryPattern.containsRelax();
    }

    public boolean isAsk() {
        return selectVariables.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SPARQL<?>)) {
            return false;
        }
        final SPARQL<?> other = (SPARQL<?>) obj;
        if (!Objects.equals(this.selectVariables, other.selectVariables)) {
            return false;
        }
        if (!Objects.equals(this.queryPattern, other.queryPattern)) {
            return false;
        }
        if (!Objects.equals(this.prefixes, other.prefixes)) {
            return false;
        }
        if (this.distinct != other.distinct) {
            return false;
        }
        if (this.limit != other.limit) {
            return false;
        }
        return Objects.equals(this.filter, other.filter);
    }


    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        if (prefixes != null) for (Entry<String, SimpleURI> p : prefixes.entrySet())
            ret.append("PREFIX ").append(p.getKey()).append(":").append(p.getValue()).append("\n");
        if (!selectVariables.isEmpty()) {
            ret.append("SELECT ");
            if (distinct) ret.append("DISTINCT ");
            for (Variable v : selectVariables) {
                ret.append(v.toString()).append(" ");
            }
            ret.append(" WHERE{ ");
        } else ret.append(" ASK{ ");
        ret.append(queryPattern.toString());
        if (filter != null) ret.append(filter);
        ret.append("}");
        if (limit >= 1) ret.append(" LIMIT ").append(limit);
        return ret.toString();
    }

    public String toStringExecutable() {
        StringBuilder ret = new StringBuilder();
        if (prefixes != null)
            for (Entry<String, SimpleURI> p : prefixes.entrySet())
                ret.append("PREFIX ").append(p.getKey()).append(":").append(p.getValue()).append("\n");
        if (!selectVariables.isEmpty()) {
            ret.append("SELECT ");
            if (distinct) ret.append("DISTINCT ");
            ret.append(selectVariables.stream().map(Variable::toString).collect(Collectors.joining(" ")));
            ret.append(" WHERE{ ");
        } else ret.append(" ASK{ ");
        ret.append(queryPattern.toStringExecutable());
        if (filter != null) ret.append(filter);
        ret.append("}");
        if (limit >= 1) ret.append(" LIMIT ").append(limit);
        return ret.toString();
    }

    public boolean isEmptyQuery() {
        return queryPattern.isEmpty();
    }

    public HashSet<CQSPARQL> toUCQ() {
        HashSet<HashSet<TriplePattern>> set = queryPattern.toJoinOfTriplePattern();
        HashSet<CQSPARQL> ret = new HashSet<>(set.size());
        for (HashSet<TriplePattern> tps : set) {
            ret.add(new CQSPARQL(selectVariables, new JoinQueryPattern<>(tps), prefixes, true, 0, null));
        }
        return ret;
    }

    public List<Variable> copySelectVariables() {
        return new ArrayList<>(selectVariables);
    }
}
