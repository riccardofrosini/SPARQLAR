/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.sparqlardc.triplepatterns;

import io.sparqlar.sparqlardc.propertypath.PropertyPath;

public class OptionalPattern extends TriplePattern {

    public OptionalPattern(Subject s, PropertyPath p, Objec o) {
        super(s, p, o);
    }

    @Override
    public PropertyPath getP() {
        return (PropertyPath) super.getP();
    }

    @Override
    public String toString() {
        return new StringBuilder().append("OPTIONAL(").append(super.toString()).append(")").toString();
    }

    @Override
    public String toStringExecutable() {
        return new StringBuilder().append("OPTIONAL(").append(super.toStringExecutable()).append(")").toString();
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 73 + 7;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OptionalPattern)) {
            return false;
        }
        return super.equals(obj);
    }
}
