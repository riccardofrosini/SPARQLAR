package io.sparqlar.optimisation;

import io.sparqlar.sparqlardc.propertypath.Alternation;
import io.sparqlar.sparqlardc.propertypath.Closure;
import io.sparqlar.sparqlardc.propertypath.Concatenation;
import io.sparqlar.sparqlardc.propertypath.PropertyPath;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

class Algebra {

    private final Vertex vertex;
    private final Map<Vertex, PropertyPath> recursion;

    Algebra(Vertex vertex, Map<Vertex, PropertyPath> r) {
        this.vertex = vertex;
        this.recursion = r;
    }

    void removeSelfRecursion() {
        if (!recursion.containsKey(vertex)) {
            return;
        }
        PropertyPath pp = new Closure(recursion.get(vertex));
        if (recursion.size() == 1) {
            recursion.put(vertex, pp);
            return;
        }
        if (!vertex.isFina()) {
            recursion.remove(vertex);
        }
        recursion.replaceAll((k, value) -> {
            if (!k.equals(vertex)) {
                return new Concatenation(pp, value);
            } else return value;
        });

    }

    void replace(Algebra replacingAlgebra) {
        if (replacingAlgebra.recursion.isEmpty() || !recursion.containsKey(replacingAlgebra.vertex)) {
            return;
        }
        PropertyPath propertyPathToMatchingVertex = recursion.get(replacingAlgebra.vertex);
        if (!replacingAlgebra.vertex.isFina() || (replacingAlgebra.recursion.size() == 1 && replacingAlgebra.recursion.containsKey(replacingAlgebra.vertex))) {
            recursion.remove(replacingAlgebra.vertex);
        }
        for (Map.Entry<Vertex, PropertyPath> replacingAlgebraPropertyPaths : replacingAlgebra.recursion.entrySet()) {
            PropertyPath toAdd = new Concatenation(propertyPathToMatchingVertex, replacingAlgebraPropertyPaths.getValue());
            Vertex replacingVertex = replacingAlgebraPropertyPaths.getKey();
            PropertyPath thisPropertyPathFromReplacingVertex = recursion.get(replacingVertex);
            if (thisPropertyPathFromReplacingVertex == null) {
                recursion.put(replacingVertex, toAdd);
            } else {
                recursion.put(replacingVertex, new Alternation(Arrays.asList(toAdd, thisPropertyPathFromReplacingVertex)));
            }
        }
    }

    protected Stream<Map.Entry<Vertex, PropertyPath>> getStream() {
        return recursion.entrySet().stream();
    }

    @Override
    public String toString() {
        return vertex + " = " + recursion;
    }
}


