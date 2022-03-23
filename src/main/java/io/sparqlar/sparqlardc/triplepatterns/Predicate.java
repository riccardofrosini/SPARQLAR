/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.sparqlardc.triplepatterns;

import io.sparqlar.sparqlardc.terms.URI;

import java.util.HashSet;

/**
 * @author riccardo
 */
public interface Predicate {
    HashSet<URI> getURIs();
}
