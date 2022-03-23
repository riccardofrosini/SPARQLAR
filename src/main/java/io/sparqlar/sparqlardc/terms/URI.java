/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.sparqlardc.terms;

import io.sparqlar.sparqlardc.propertypath.TerminalPropertyPath;
import io.sparqlar.sparqlardc.triplepatterns.Objec;
import io.sparqlar.sparqlardc.triplepatterns.Subject;

import java.util.HashSet;


/**
 * @author riccardo
 */
public abstract class URI extends Constant implements Subject, Objec, TerminalPropertyPath {
    private static final String TYPE_STRING = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    public static final URI TYPE = new SimpleURI(TYPE_STRING);

    public abstract String getUri();

    @Override
    public abstract HashSet<URI> getURIs();
}
