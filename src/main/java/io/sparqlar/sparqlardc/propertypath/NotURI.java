/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.sparqlardc.propertypath;

import io.sparqlar.sparqlardc.terms.SimpleURI;
import io.sparqlar.sparqlardc.terms.URI;

import java.util.HashSet;
import java.util.Objects;

/**
 * @author riccardo
 */
public class NotURI implements TerminalPropertyPath {
    public static final NotURI ALL_URI = new NotURI(new SimpleURI("http://www.rf.com"));

    private final URI uri;

    public NotURI(URI uri) {
        this.uri = uri;
    }

    public URI getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "!" + uri;
    }

    @Override
    public int hashCode() {
        return 89 * Objects.hashCode(this.uri) + 3;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof NotURI)) {
            return false;
        }
        final NotURI other = (NotURI) obj;
        return Objects.equals(this.uri, other.uri);
    }

    @Override
    public HashSet<URI> getURIs() {
        return new HashSet<>(0);
    }


}
