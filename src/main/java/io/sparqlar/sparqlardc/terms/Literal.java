/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.sparqlardc.terms;

import io.sparqlar.sparqlardc.triplepatterns.Objec;
import io.sparqlar.sparqlardc.triplepatterns.Subject;

import java.util.Objects;

/**
 * @author riccardo
 */
public class Literal extends Constant implements Objec, Subject {
    private final String literal;

    public Literal(String literal) {
        this.literal = literal;
    }

    @Override
    public int hashCode() {
        return 79 * Objects.hashCode(this.literal) + 5;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Literal)) {
            return false;
        }
        final Literal other = (Literal) obj;
        return Objects.equals(this.literal, other.literal);
    }

    @Override
    public String toString() {
        return literal;
    }

}
