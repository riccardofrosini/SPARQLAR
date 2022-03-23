/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.sparqlardc.triplepatterns;

import io.sparqlar.sparqlardc.propertypath.PropertyPath;

/**
 * @author riccardo
 */
public class Relax extends Flex {

    public Relax(Subject s, PropertyPath p, Objec o) {
        super(s, p, o);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("RELAX(").append(getS()).append(" ").append(getP()).append(" ").append(getO())
                .append(")").toString();
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 13 + 7;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Relax)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public boolean containsApprox() {
        return false;
    }

    @Override
    public boolean containsRelax() {
        return true;
    }
}
