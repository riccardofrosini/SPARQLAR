/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.rewriting;

import io.sparqlar.sparqlardc.terms.SimpleURI;
import io.sparqlar.sparqlardc.terms.URI;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.HashSet;

/**
 * @author riccardo
 */
public class Ontology {

    private static final String RDF_XML = "RDF/XML";
    private static final String N_TRIPLE = "N-TRIPLE";
    private static final String TURTLE = "TURTLE";
    private static final String N3 = "N3";
    private final OntModel ontology;

    public Ontology(String uri) {
        if (uri.endsWith("owl")) {
            ontology = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_TRANS_INF);
            ontology.read(uri, RDF_XML);
        } else {
            ontology = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF);
            ontology.read(uri, TURTLE);
        }
    }

    public HashSet<URI> getSuperProperties(URI u) {
        HashSet<URI> ret = new HashSet<>();
        OntProperty ontProperty = ontology.getOntProperty(u.getUri());
        if (ontProperty != null) {
            ExtendedIterator<? extends OntProperty> listSuperProperties = ontProperty.listSuperProperties(true);
            while (listSuperProperties.hasNext()) {
                String next = listSuperProperties.next().getURI();
                if (next != null) {
                    SimpleURI su = new SimpleURI(next);
                    ret.add(su);
                }
            }
        }
        return ret;
    }

    public HashSet<URI> getRange(URI u) {
        HashSet<URI> ret = new HashSet<>();
        OntProperty ontProperty = ontology.getOntProperty(u.getUri());
        if (ontProperty != null) {
            ExtendedIterator<? extends OntResource> listRange = ontProperty.listRange();
            while (listRange.hasNext()) {
                String next = listRange.next().getURI();
                if (next != null) {
                    SimpleURI su = new SimpleURI(next);
                    ret.add(su);
                }
            }
        }
        return ret;
    }

    public HashSet<URI> getDomain(URI u) {
        HashSet<URI> ret = new HashSet<>();
        OntProperty ontProperty = ontology.getOntProperty(u.getUri());
        if (ontProperty != null) {
            ExtendedIterator<? extends OntResource> listDomain = ontProperty.listDomain();
            while (listDomain.hasNext()) {
                String next = listDomain.next().getURI();
                if (next != null) {
                    SimpleURI su = new SimpleURI(next);
                    ret.add(su);
                }
            }
        }
        return ret;
    }

    public HashSet<URI> getSuperClasses(URI u) {
        HashSet<URI> ret = new HashSet<>();
        OntClass ontClass = ontology.getOntClass(u.getUri());
        if (ontClass != null) {
            ExtendedIterator<OntClass> listSuperClasses = ontClass.listSuperClasses(true);
            while (listSuperClasses.hasNext()) {
                String next = listSuperClasses.next().getURI();
                if (next != null) {
                    SimpleURI su = new SimpleURI(next);
                    ret.add(su);
                }
            }
        }
        return ret;
    }
}
