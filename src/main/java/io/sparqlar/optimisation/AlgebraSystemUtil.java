package io.sparqlar.optimisation;

import io.sparqlar.sparqlardc.propertypath.Alternation;
import io.sparqlar.sparqlardc.propertypath.EmptyPath;
import io.sparqlar.sparqlardc.propertypath.PropertyPath;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AlgebraSystemUtil {

    protected static PropertyPath getPropertyPathFromSystem(HashMap<Vertex, Algebra> system) {
        while (system.size() > 1) {
            Vertex toReplace = system.keySet().stream().filter(vertex -> !vertex.isInitial()).findFirst().orElse(null);
            Algebra algebraToReplace = system.get(toReplace);
            system.remove(toReplace);
            algebraToReplace.removeSelfRecursion();
            for (Algebra a : system.values()) {
                a.replace(algebraToReplace);
            }
        }
        Map.Entry<Vertex, Algebra> initial = system.entrySet().iterator().next();
        Vertex initialVertex = initial.getKey();
        if (!initialVertex.isInitial()) return null;
        Algebra initialAlgebra = initial.getValue();
        initialAlgebra.removeSelfRecursion();

        Set<PropertyPath> propertyPaths = initialAlgebra.getStream()
                .filter(vertexPropertyPathEntry -> vertexPropertyPathEntry.getKey().isFina())
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
        if (initialVertex.isFina()) {
            propertyPaths.add(EmptyPath.EMPTY_PATH);
        }
        if (propertyPaths.size() == 1) {
            return propertyPaths.iterator().next();
        }
        return new Alternation(propertyPaths);
    }
}
