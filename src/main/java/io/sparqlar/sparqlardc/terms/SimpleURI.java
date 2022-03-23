/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.sparqlardc.terms;

import java.util.HashSet;
import java.util.Objects;

/**
 * @author riccardo
 */
public class SimpleURI extends URI {

    private final String uri;

    public SimpleURI(String uri) {
        this.uri = uri;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public int hashCode() {
        return 83 * Objects.hashCode(this.uri) + 5;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof SimpleURI) {
            final SimpleURI other = (SimpleURI) obj;
            return Objects.equals(this.uri, other.uri);
        } else if (obj instanceof PrefixedURI) {
            final PrefixedURI other1 = (PrefixedURI) obj;
            return Objects.equals(this.uri, other1.getSimpleURI().uri);
        } else return false;
    }

    @Override
    public String toString() {
        return "<" + uri + ">";
    }

    @Override
    public HashSet<URI> getURIs() {
        HashSet<URI> ret = new HashSet<>(1);
        ret.add(this);
        return ret;
    }
}
