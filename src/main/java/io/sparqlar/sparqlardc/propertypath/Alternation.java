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
public class Alternation implements PropertyPath {
    private final List<PropertyPath> ps;
    boolean isGrouping;

    public Alternation(Collection<PropertyPath> propertyPaths) {
        ArrayList<PropertyPath> psTemp = new ArrayList<>();
        for (PropertyPath propertyPath : propertyPaths) {
            if (propertyPath instanceof Alternation) {
                Alternation alternation = (Alternation) propertyPath;
                psTemp.addAll(alternation.ps);
            } else {
                psTemp.add(propertyPath);
            }
        }
        this.ps = psTemp;
        isGrouping = false;
    }

    private Alternation() {
        ps = new ArrayList<>();
        isGrouping = false;
    }

    public static Alternation buildAlternationForParser(PropertyPath p1, PropertyPath p2) {
        Alternation alternation = new Alternation();
        if (p1 instanceof Alternation && !((Alternation) p1).isGrouping) {
            alternation.ps.addAll(((Alternation) p1).ps);
        } else {
            alternation.ps.add(p1);
        }
        if (p2 instanceof Alternation) {
            Alternation alternationP2 = (Alternation) p2;
            PropertyPath first = alternationP2.getFirst();
            if (first instanceof Alternation) {
                alternation.ps.addAll(((Alternation) first).ps);
                alternation.ps.addAll(((Alternation) p2).getRest().ps);
            } else {
                alternation.ps.addAll(alternationP2.ps);
            }
        } else {
            alternation.ps.add(p2);
        }
        return alternation;
    }

    @Override
    public int hashCode() {
        return 83 * Objects.hashCode(this.ps) + 3;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Alternation)) {
            return false;
        }
        final Alternation other = (Alternation) obj;
        return Objects.equals(this.ps, other.ps);
    }

    @Override
    public String toString() {
        return ps.stream().map(Object::toString)
                .collect(Collectors.joining("|", "(", ")"));
    }

    @Override
    public HashSet<URI> getURIs() {
        return ps.stream().map(Predicate::getURIs).reduce((uris, uris2) -> {
            uris.addAll(uris2);
            return uris;
        }).get();
    }

    public PropertyPath getFirst() {
        return ps.get(0);
    }

    public Alternation getRest() {
        return new Alternation(ps.subList(1, ps.size()));
    }

    public PropertyPath get(int i) {
        return ps.get(i);
    }

    public int size() {
        return ps.size();
    }


    public boolean isGrouping() {
        return isGrouping;
    }

    public void setToIsGrouping() {
        isGrouping = true;
    }
}
