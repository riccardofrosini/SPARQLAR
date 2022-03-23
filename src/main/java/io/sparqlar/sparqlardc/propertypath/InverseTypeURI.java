package io.sparqlar.sparqlardc.propertypath;

import io.sparqlar.sparqlardc.terms.URI;

import java.util.HashSet;

public class InverseTypeURI implements TerminalPropertyPath {
    public static final InverseTypeURI INV_TYPE = new InverseTypeURI();

    private InverseTypeURI() {
    }

    @Override
    public int hashCode() {
        return 9 * URI.TYPE.hashCode() + 3;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return obj instanceof InverseTypeURI;
    }

    public String toString() {
        return "^" + URI.TYPE.toString();
    }

    @Override
    public HashSet<URI> getURIs() {
        HashSet<URI> uris = new HashSet<>();
        uris.add(URI.TYPE);
        return uris;
    }
}