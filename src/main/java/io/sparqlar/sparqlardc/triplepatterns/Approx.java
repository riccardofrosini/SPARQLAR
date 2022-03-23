/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.sparqlardc.triplepatterns;

import io.sparqlar.sparqlardc.propertypath.PropertyPath;

/**
 * @author riccardo
 */
public class Approx extends Flex {

    public Approx(Subject s, PropertyPath p, Objec o) {
        super(s, p, o);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("APPROX(").append(getS()).append(" ").append(getP()).append(" ").append(getO())
                .append(")").toString();
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 11 + 17;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Approx)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public boolean containsApprox() {
        return true;
    }

    @Override
    public boolean containsRelax() {
        return false;
    }

}
