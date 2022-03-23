package io.sparqlar.optimisation;

import io.sparqlar.sparqlardc.propertypath.TerminalPropertyPath;

import java.util.Objects;

public class Edge {

    private final TerminalPropertyPath terminalPropertyPath;
    private final Vertex vertex;

    public Edge(TerminalPropertyPath terminalPropertyPath, Vertex vertex) {
        this.terminalPropertyPath = terminalPropertyPath;
        this.vertex = vertex;
    }

    public TerminalPropertyPath getTerminalPropertyPath() {
        return terminalPropertyPath;
    }

    public Vertex getVertex() {
        return vertex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) {
            return false;
        }
        if (!(o instanceof Edge)) {
            return false;
        }
        Edge edge = (Edge) o;
        return terminalPropertyPath.equals(edge.terminalPropertyPath) &&
                vertex.equals(edge.vertex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(terminalPropertyPath, vertex);
    }


    @Override
    public String toString() {
        return terminalPropertyPath + " " + vertex;
    }
}
