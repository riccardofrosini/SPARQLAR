package io.sparqlar.rewriting.exceptions;

public class OptimisationSchemaException extends Exception {

    public OptimisationSchemaException(Exception e) {
        super(e);
    }

    public OptimisationSchemaException(String s) {
        super(s);
    }
}
