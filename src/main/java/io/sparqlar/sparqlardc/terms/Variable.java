/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.sparqlardc.terms;

import io.sparqlar.sparqlardc.triplepatterns.Objec;
import io.sparqlar.sparqlardc.triplepatterns.Predicate;
import io.sparqlar.sparqlardc.triplepatterns.Subject;

import java.util.HashSet;
import java.util.Objects;

/**
 * @author riccardo
 */
public class Variable extends Term implements Subject, Predicate, Objec {
    private final String var;

    public Variable(String var) {
        this.var = var;
    }

    public String getVar() {
        return var;
    }

    @Override
    public int hashCode() {
        return 41 * Objects.hashCode(this.var) + 3;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Variable)) {
            return false;
        }
        final Variable other = (Variable) obj;
        return Objects.equals(this.var, other.var);
    }

    @Override
    public String toString() {
        return "?" + var;
    }

    @Override
    public HashSet<URI> getURIs() {
        return new HashSet<>();
    }


}
