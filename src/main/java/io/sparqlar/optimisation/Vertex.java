package io.sparqlar.optimisation;

import java.util.HashSet;
import java.util.Objects;

class Vertex {
    private final boolean initial;
    private final boolean fina;
    private final String s;

    protected Vertex(String s) {
        this.s = s;
        initial = false;
        fina = false;
    }

    protected Vertex(String s, boolean init, boolean fin) {
        this.s = s;
        this.initial = init;
        this.fina = fin;
    }

    protected static Vertex combineVerticesNamesDFA(HashSet<Vertex> vertices) {
        StringBuilder name = new StringBuilder();
        boolean init = false;
        boolean fina = false;
        for (Vertex vertex : vertices) {
            name.append("_").append(vertex.getV());
            init = init || vertex.initial;
            fina = fina || vertex.fina;
        }
        return new Vertex(name.substring(1), init, fina);
    }

    protected static Vertex combine2VerticesNamesForIntersection(Vertex v1, Vertex v2) {
        return new Vertex(v1.getV() + "_" + v2.getV(), v1.initial && v2.initial, v1.fina && v2.fina);
    }

    protected boolean isInitial() {
        return initial;
    }

    protected boolean isFina() {
        return fina;
    }

    protected String getV() {
        return s;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Vertex)) {
            return false;
        }
        final Vertex other = (Vertex) obj;
        return Objects.equals(this.s, other.s);
    }

    @Override
    public int hashCode() {
        return 97 * Objects.hashCode(this.s) + 72;
    }

    @Override
    public String toString() {
        return s + " " + initial + " " + fina;
    }
}