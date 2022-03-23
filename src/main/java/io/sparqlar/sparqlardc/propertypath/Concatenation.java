/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.sparqlardc.propertypath;

import io.sparqlar.sparqlardc.terms.URI;
import io.sparqlar.sparqlardc.triplepatterns.Predicate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author riccardo
 */
public class Concatenation implements PropertyPath {
    private final List<PropertyPath> ps;

    public Concatenation(PropertyPath... pps) {
        this(Arrays.asList(pps));
    }

    private Concatenation(List<PropertyPath> propertyPaths) {
        ArrayList<PropertyPath> psTemp = new ArrayList<>();
        for (PropertyPath propertyPath : propertyPaths) {
            if (propertyPath instanceof Concatenation) {
                Concatenation alternation = (Concatenation) propertyPath;
                psTemp.addAll(alternation.ps);
            } else {
                psTemp.add(propertyPath);
            }
        }
        this.ps = psTemp;
    }

    @Override
    public int hashCode() {
        return 41 * Objects.hashCode(this.ps) + 5;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Concatenation)) {
            return false;
        }
        final Concatenation other = (Concatenation) obj;
        return Objects.equals(this.ps, other.ps);
    }

    @Override
    public String toString() {
        return ps.stream().map(Object::toString).collect(Collectors.joining("/"));
    }


    @Override
    public HashSet<URI> getURIs() {
        return ps.stream().map(Predicate::getURIs).reduce((uris, uris2) -> {
            uris.addAll(uris2);
            return uris;
        }).get();
    }

    public PropertyPath remove(int i) {
        ArrayList<PropertyPath> propertyPaths = new ArrayList<>(ps);
        propertyPaths.remove(i);
        if (propertyPaths.size() == 1) {
            return propertyPaths.get(0);
        }
        return new Concatenation(propertyPaths);
    }

    public PropertyPath replace(int i, PropertyPath p) {
        if (p instanceof EmptyPath) {
            return remove(i);
        }
        ArrayList<PropertyPath> propertyPaths = new ArrayList<>(ps);
        propertyPaths.set(i, p);
        return new Concatenation(propertyPaths);
    }

    public PropertyPath get(int i) {
        return ps.get(i);
    }

    public int size() {
        return ps.size();
    }
}
