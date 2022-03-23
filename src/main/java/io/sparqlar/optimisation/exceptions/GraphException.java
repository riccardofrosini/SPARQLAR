package io.sparqlar.optimisation.exceptions;

public class GraphException extends Exception {

    public GraphException() {
        super("Graph has two different initial statuses!");
    }
}
