/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.sparqlardc.propertypath;

import io.sparqlar.sparqlardc.terms.URI;

import java.util.HashSet;

/**
 * @author riccardo
 */
public final class EmptyPath implements TerminalPropertyPath {

    public static final EmptyPath EMPTY_PATH = new EmptyPath();
    private static final String emptyURI = "<http://www.rf.com>{0}";

    private EmptyPath() {
    }

    @Override
    public String toString() {
        return emptyURI;
    }

    @Override
    public int hashCode() {
        return emptyURI.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return obj instanceof EmptyPath;
    }

    @Override
    public HashSet<URI> getURIs() {
        return new HashSet<>();
    }
}
