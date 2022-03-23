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
public class PrefixedURI extends URI {
    private final String prefix;
    private final String uri;
    private final String base;

    public PrefixedURI(String prefix, String uri, String base) {
        this.prefix = prefix;
        this.uri = uri;
        this.base = base;
    }

    @Override
    public String getUri() {
        return base + uri;
    }

    public SimpleURI getSimpleURI() {
        return new SimpleURI(base + uri);
    }

    @Override
    public int hashCode() {
        return this.getSimpleURI().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof SimpleURI) {
            final SimpleURI other = (SimpleURI) obj;
            return Objects.equals(this.getSimpleURI().getUri(), other.getUri());
        } else if (obj instanceof PrefixedURI) {
            final PrefixedURI other1 = (PrefixedURI) obj;
            if (!Objects.equals(this.uri, other1.uri)) {
                return false;
            }
            return Objects.equals(this.prefix, other1.prefix);
        }
        return false;
    }

    @Override
    public String toString() {
        return prefix + ":" + uri;
    }

    @Override
    public HashSet<URI> getURIs() {
        HashSet<URI> ret = new HashSet<>(1);
        ret.add(this);
        return ret;
    }


}
