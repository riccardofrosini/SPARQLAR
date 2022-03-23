/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.sparqlardc.propertypath;

import io.sparqlar.sparqlardc.terms.Term;
import io.sparqlar.sparqlardc.terms.URI;

import java.util.HashSet;
import java.util.Objects;

/**
 * @author riccardo
 */
public class Closure implements PropertyPath {
    private final PropertyPath p;

    public Closure(PropertyPath p) {
        this.p = p;
    }


    public PropertyPath getP() {
        return p;
    }

    @Override
    public int hashCode() {
        return 11 * Objects.hashCode(this.p) + 3;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Closure)) {
            return false;
        }
        final Closure other = (Closure) obj;
        return Objects.equals(this.p, other.p);
    }

    @Override
    public String toString() {
        if (p instanceof Term || p instanceof Alternation) return p + "*";// alternation always has parenthesis ( )
        return new StringBuilder().append("(").append(p).append(")*").toString();
    }

    @Override
    public HashSet<URI> getURIs() {
        return p.getURIs();
    }

}
