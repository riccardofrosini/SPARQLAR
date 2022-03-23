/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.rewriting.exceptions;

public class OntologyException extends Exception {
    public OntologyException() {
        super("Ontology not loaded. The relax operator need the ontology.");
    }
}
