/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.sparqlardc.triplepatterns;

import io.sparqlar.sparqlardc.propertypath.PropertyPath;

/**
 * @author Riccardo
 */
public class Flex extends TriplePattern {

    public Flex(Subject s, PropertyPath p, Objec o) {
        super(s, p, o);
    }

    @Override
    public PropertyPath getP() {
        return (PropertyPath) super.getP();
    }

    @Override
    public String toString() {
        return new StringBuilder().append("FLEX(").append(super.toString()).append(")").toString();
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 93 + 3;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Flex)) {
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
        return true;
    }
}
