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

public abstract class QueryPattern {

    public abstract HashSet<Variable> getVariableSet();

    public abstract String toStringExecutable();

    public abstract boolean containsApprox();

    public abstract boolean containsRelax();

    public abstract boolean isEmpty();

    public abstract HashSet<HashSet<TriplePattern>> toJoinOfTriplePattern();

    public abstract HashSet<URI> getApproximatedURIs();
}
