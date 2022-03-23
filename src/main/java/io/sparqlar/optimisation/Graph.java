/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.optimisation;

import io.sparqlar.optimisation.exceptions.GraphException;
import io.sparqlar.sparqlardc.propertypath.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Riccardo
 */
public class Graph {
    private final HashMap<Vertex, HashSet<Edge>> graphVerticesToVertices;
    private final HashSet<Vertex> finals;
    private Vertex initial;

    protected Graph() {
        graphVerticesToVertices = new HashMap<>();
        finals = new HashSet<>();
        initial = null;
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hashCode(this.graphVerticesToVertices) + 23;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Graph)) {
            return false;
        }
        final Graph other = (Graph) obj;
        return Objects.equals(this.graphVerticesToVertices, other.graphVerticesToVertices);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        graphVerticesToVertices.forEach((key, value) -> stringBuilder.append(key.toString()).append("=").append(value.toString()).append("\n"));
        return stringBuilder.toString();
    }

    void put(Graph graph) throws GraphException {
        for (Map.Entry<Vertex, HashSet<Edge>> vertexToEdges : graph.graphVerticesToVertices.entrySet()) {
            for (Edge p : vertexToEdges.getValue()) {
                put(vertexToEdges.getKey(), p.getTerminalPropertyPath(), p.getVertex());
            }
        }
    }

    void put(Vertex i, TerminalPropertyPath p, Vertex f) throws GraphException {
        graphVerticesToVertices.computeIfAbsent(i, vertex -> new HashSet<>()).add(new Edge(p, f));
        graphVerticesToVertices.putIfAbsent(f, new HashSet<>());
        if (i.isFina()) {
            finals.add(i);
        }
        if (f.isFina()) {
            finals.add(f);
        }
        if (i.isInitial()) {
            if (initial == null) {
                initial = i;
            } else if (!initial.equals(i)) {
                throw new GraphException();
            }
        }
        if (f.isInitial()) {
            if (initial == null) {
                initial = f;
            } else if (!initial.equals(f)) {
                throw new GraphException();
            }
        }

    }

    private void retainAll(Set<Vertex> vertices) {
        graphVerticesToVertices.keySet().retainAll(vertices);
        graphVerticesToVertices.forEach((vertex, edges) -> edges.removeIf(edge -> !vertices.contains(edge.getVertex())));
        finals.retainAll(vertices);
        if (!vertices.contains(initial)) {
            graphVerticesToVertices.clear();
            finals.clear();
            initial = null;
        }
    }

    public PropertyPath getPropertyPath() throws GraphException {
        return toDFA().makeMinimalAlternative()
                .getPropertyPathFromDFA();
    }

    private PropertyPath getPropertyPathFromDFA() {
        if (graphVerticesToVertices.isEmpty()) return null;
        HashMap<Vertex,Algebra> system = new HashMap<>(graphVerticesToVertices.size());
        for (Vertex vertex : graphVerticesToVertices.keySet()) {
            Map<Vertex, PropertyPath> algebraicTransactions = graphVerticesToVertices.get(vertex).stream()
                    .collect(Collectors.groupingBy(Edge::getVertex)).entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            vertexListEntry -> {
                                Set<PropertyPath> paths = vertexListEntry.getValue().stream()
                                        .map(edge -> (PropertyPath) edge.getTerminalPropertyPath())
                                        .collect(Collectors.toSet());
                                if (paths.size() == 1) return paths.iterator().next();
                                return new Alternation(paths);
                            }));
            if (!algebraicTransactions.isEmpty()) {
                //behaviour of the algorithm will not change but the if condition will reduce the number of operations
                system.put(vertex,new Algebra(vertex, algebraicTransactions));
            }
        }
        return AlgebraSystemUtil.getPropertyPathFromSystem(system);
    }

    public Graph intersect(Graph graph) throws GraphException {
        Graph intersect = new Graph();
        for (Map.Entry<Vertex, HashSet<Edge>> edge1 : graph.graphVerticesToVertices.entrySet()) {
            for (Map.Entry<Vertex, HashSet<Edge>> edge2 : graphVerticesToVertices.entrySet()) {
                for (Edge p1 : edge1.getValue()) {
                    for (Edge p2 : edge2.getValue()) {
                        if (p1.getTerminalPropertyPath().equals(p2.getTerminalPropertyPath())) {
                            intersect.put(Vertex.combine2VerticesNamesForIntersection(edge1.getKey(), edge2.getKey()), p1.getTerminalPropertyPath(),
                                    Vertex.combine2VerticesNamesForIntersection(p1.getVertex(), p2.getVertex()));
                        } else if (p1.getTerminalPropertyPath() instanceof NotURI &&
                                !((NotURI) p1.getTerminalPropertyPath()).getUri().equals(p2.getTerminalPropertyPath())) {
                            intersect.put(Vertex.combine2VerticesNamesForIntersection(edge1.getKey(), edge2.getKey()), p2.getTerminalPropertyPath(),
                                    Vertex.combine2VerticesNamesForIntersection(p1.getVertex(), p2.getVertex()));

                        } else if (p2.getTerminalPropertyPath() instanceof NotURI &&
                                !((NotURI) p2.getTerminalPropertyPath()).getUri().equals(p1.getTerminalPropertyPath())) {
                            intersect.put(Vertex.combine2VerticesNamesForIntersection(edge1.getKey(), edge2.getKey()), p1.getTerminalPropertyPath(),
                                    Vertex.combine2VerticesNamesForIntersection(p1.getVertex(), p2.getVertex()));
                        }
                    }
                }
            }
        }
        return intersect.removeDisconnectedFromInitialAndToFinal();
    }

    public Graph toDFA() throws GraphException {
        Graph dfa = new Graph();
        HashMap<Vertex, HashSet<Vertex>> basicEquivalences = new HashMap<>(graphVerticesToVertices.size());
        HashSet<HashSet<Vertex>> newGeneration = new HashSet<>();
        HashSet<HashSet<Vertex>> allVerticesTracker = new HashSet<>();
        for (Vertex vertex : graphVerticesToVertices.keySet()) {
            HashSet<Vertex> emptyPathSet = findEmptyPathSet(vertex);
            basicEquivalences.put(vertex, emptyPathSet);
            newGeneration.add(emptyPathSet);
            allVerticesTracker.add(emptyPathSet);
        }
        while (!newGeneration.isEmpty()) {
            HashSet<HashSet<Vertex>> toAdd = new HashSet<>();
            for (HashSet<Vertex> newVertex : newGeneration) {
                for (Map.Entry<TerminalPropertyPath, HashSet<Vertex>> terminalPropertyPathToVertices :
                        findNondeterministicSets(newVertex, basicEquivalences).entrySet()) {
                    if (allVerticesTracker.add(terminalPropertyPathToVertices.getValue())) {
                        toAdd.add(terminalPropertyPathToVertices.getValue());
                    }
                    dfa.put(Vertex.combineVerticesNamesDFA(newVertex),
                            terminalPropertyPathToVertices.getKey(),
                            Vertex.combineVerticesNamesDFA(terminalPropertyPathToVertices.getValue()));
                }
            }
            newGeneration = toAdd;
        }
        return dfa.removeDisconnectedFromInitialAndToFinal();
    }

    private HashMap<TerminalPropertyPath, HashSet<Vertex>> findNondeterministicSets(Set<Vertex> vertices, HashMap<Vertex, HashSet<Vertex>> basicEquivalence) {
        HashMap<TerminalPropertyPath, HashSet<Vertex>> nondeterministicSets = new HashMap<>();
        for (Vertex vertex : vertices) {
            for (Edge edge : graphVerticesToVertices.get(vertex)) {
                if (!(edge.getTerminalPropertyPath() instanceof EmptyPath)) {
                    nondeterministicSets.computeIfAbsent(edge.getTerminalPropertyPath(), set -> new HashSet<>()).addAll(basicEquivalence.get(edge.getVertex()));
                }
            }
        }
        return nondeterministicSets;
    }

    private HashSet<Vertex> findEmptyPathSet(Vertex v) {
        HashSet<Vertex> emptyPathSet = new HashSet<>();
        emptyPathSet.add(v);
        Set<Vertex> newGeneration = new HashSet<>();
        newGeneration.add(v);
        while (!newGeneration.isEmpty()) {
            HashSet<Vertex> toAdd = new HashSet<>(graphVerticesToVertices.size());
            for (Vertex vertex : newGeneration) {
                toAdd.addAll(graphVerticesToVertices.get(vertex).stream()
                        .filter(edge -> edge.getTerminalPropertyPath() instanceof EmptyPath)
                        .map(Edge::getVertex).collect(Collectors.toSet()));
            }
            newGeneration = toAdd.stream().filter(emptyPathSet::add).collect(Collectors.toSet());
        }
        return emptyPathSet;
    }

    private Graph removeDisconnectedFromInitialAndToFinal() {
        if (initial == null) {
            graphVerticesToVertices.clear();
            finals.clear();
            return this;
        }
        Set<Vertex> connected = getVerticesFromVertex(initial);
        retainAll(connected);
        connected = new HashSet<>(graphVerticesToVertices.size());
        for (Vertex vertex : graphVerticesToVertices.keySet()) {
            if (isConnectedToFinal(vertex)) {
                connected.add(vertex);
            }
        }
        retainAll(connected);
        return this;
    }

    private boolean isConnectedToFinal(Vertex initial) {
        if (initial.isFina()) {
            return true;
        }
        Set<Vertex> connectedToInitial = new HashSet<>(graphVerticesToVertices.size());
        connectedToInitial.add(initial);
        Set<Vertex> newGeneration = new HashSet<>();
        newGeneration.add(initial);
        while (!newGeneration.isEmpty()) {
            HashSet<Vertex> toAdd = new HashSet<>();
            for (Vertex vertex : newGeneration) {
                for (Edge edge : graphVerticesToVertices.get(vertex)) {
                    Vertex newVertex = edge.getVertex();
                    if (newVertex.isFina()) {
                        return true;
                    }
                    toAdd.add(newVertex);
                }
            }
            newGeneration = toAdd.stream().filter(connectedToInitial::add).collect(Collectors.toSet());
        }
        return false;
    }

    private Set<Vertex> getVerticesFromVertex(Vertex initial) {
        Set<Vertex> connectedToInitial = new HashSet<>();
        connectedToInitial.add(initial);
        Set<Vertex> newGeneration = new HashSet<>();
        newGeneration.add(initial);
        while (!newGeneration.isEmpty()) {
            HashSet<Vertex> toAdd = new HashSet<>();
            for (Vertex vertex : newGeneration) {
                toAdd.addAll(graphVerticesToVertices.get(vertex).stream().map(Edge::getVertex).collect(Collectors.toSet()));
            }
            newGeneration = toAdd.stream().filter(connectedToInitial::add).collect(Collectors.toSet());
        }
        return connectedToInitial;
    }

    public boolean canSimulate(Graph graph) {
        Vertex thisInitial = this.initial;
        Vertex otherInitial = graph.initial;
        if (thisInitial == null || otherInitial == null) {
            return thisInitial == null && otherInitial == null;
        }
        return canSimulate(thisInitial, otherInitial, graph, new HashMap<>());
    }

    private boolean canSimulate(Vertex thisVertex, Vertex otherVertex, Graph graph, HashMap<Vertex, HashSet<Vertex>> verticesMapping) {
        if (verticesMapping.containsKey(thisVertex) && verticesMapping.get(thisVertex).contains(otherVertex)) {
            return true;
        }
        verticesMapping.computeIfAbsent(thisVertex, vertex -> new HashSet<>()).add(otherVertex);
        if (!thisVertex.isFina() && otherVertex.isFina()) {
            verticesMapping.get(thisVertex).remove(otherVertex);
            return false;
        }
        HashSet<Edge> thisEdges = graphVerticesToVertices.get(thisVertex);
        HashSet<Edge> otherEdges = graph.graphVerticesToVertices.get(otherVertex);

        for (Edge otherEdge : otherEdges) {
            boolean canSimulate = false;
            for (Edge thisEdge : thisEdges) {
                if (otherEdge.getTerminalPropertyPath().equals(thisEdge.getTerminalPropertyPath())) {//should happen only once per outer iteration
                    canSimulate = canSimulate || canSimulate(thisEdge.getVertex(), otherEdge.getVertex(), graph, verticesMapping);
                }
            }
            if (!canSimulate) {
                verticesMapping.get(thisVertex).remove(otherVertex);
                return false;
            }
        }
        return true;
    }

    private Graph makeMinimalAlternative() throws GraphException {
        HashMap<Vertex, HashSet<Vertex>> vertexMapping = new HashMap<>();
        HashMap<Vertex, HashSet<Vertex>> vertexNotBisimulating = new HashMap<>();
        for (Vertex vertex : graphVerticesToVertices.keySet()) {
            for (Vertex vertex1 : graphVerticesToVertices.keySet()) {
                canBiSimulate(vertex, vertex1, vertexMapping, vertexNotBisimulating);
            }
        }
        return makeMinimalGivenEquivalences(vertexMapping);
    }

    private Graph makeMinimalGivenEquivalences(HashMap<Vertex, HashSet<Vertex>> vertexMapping) throws GraphException {
        Graph minimal = new Graph();
        Map<Vertex, Vertex> mapToCandidate = vertexMapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, vertexHashSetEntry -> vertexHashSetEntry.getValue().iterator().next()));
        for (Map.Entry<Vertex, HashSet<Edge>> vertexHashSetEntry : graphVerticesToVertices.entrySet())
            if (mapToCandidate.get(vertexHashSetEntry.getKey()).equals(vertexHashSetEntry.getKey())) {
                for (Edge edge : vertexHashSetEntry.getValue()) {
                    minimal.put(vertexHashSetEntry.getKey(), edge.getTerminalPropertyPath(), mapToCandidate.get(edge.getVertex()));
                }
            }
        return minimal;
    }

    private boolean canBiSimulate(Vertex thisVertex, Vertex otherVertex, HashMap<Vertex, HashSet<Vertex>> verticesMapping, HashMap<Vertex, HashSet<Vertex>> vertexNotBisimulating) {
        if (vertexNotBisimulating.containsKey(thisVertex) && vertexNotBisimulating.get(thisVertex).contains(otherVertex)) {
            return false;
        }
        if (verticesMapping.containsKey(thisVertex) && verticesMapping.get(thisVertex).contains(otherVertex)) {
            return true;
        }
        verticesMapping.computeIfAbsent(thisVertex, vertex -> new HashSet<>()).add(otherVertex);
        verticesMapping.computeIfAbsent(otherVertex, vertex -> new HashSet<>()).add(thisVertex);
        if (thisVertex == otherVertex) {
            return true;
        }
        HashSet<Edge> thisEdges = graphVerticesToVertices.get(thisVertex);
        HashSet<Edge> otherEdges = graphVerticesToVertices.get(otherVertex);
        if (thisEdges.size() != otherEdges.size() || thisVertex.isFina() != otherVertex.isFina()) {
            vertexNotBisimulating.computeIfAbsent(thisVertex, vertex -> new HashSet<>()).add(otherVertex);
            verticesMapping.get(thisVertex).remove(otherVertex);
            vertexNotBisimulating.computeIfAbsent(otherVertex, vertex -> new HashSet<>()).add(thisVertex);
            verticesMapping.get(otherVertex).remove(thisVertex);
            return false;
        }
        for (Edge otherEdge : otherEdges) {
            boolean foundEdge = false;
            for (Edge thisEdge : thisEdges) {
                if (otherEdge.getTerminalPropertyPath().equals(thisEdge.getTerminalPropertyPath())) {//should happen only once per outer iteration
                    foundEdge = true;
                    if (!canBiSimulate(thisEdge.getVertex(), otherEdge.getVertex(), verticesMapping, vertexNotBisimulating)) {
                        vertexNotBisimulating.computeIfAbsent(thisVertex, vertex -> new HashSet<>()).add(otherVertex);
                        verticesMapping.get(thisVertex).remove(otherVertex);
                        vertexNotBisimulating.computeIfAbsent(otherVertex, vertex -> new HashSet<>()).add(thisVertex);
                        verticesMapping.get(otherVertex).remove(thisVertex);
                        return false;
                    }
                }
            }
            if (!foundEdge) {
                vertexNotBisimulating.computeIfAbsent(thisVertex, vertex -> new HashSet<>()).add(otherVertex);
                verticesMapping.get(thisVertex).remove(otherVertex);
                vertexNotBisimulating.computeIfAbsent(otherVertex, vertex -> new HashSet<>()).add(thisVertex);
                verticesMapping.get(otherVertex).remove(thisVertex);
                return false;
            }
        }
        return true;
    }

    public boolean isEmpty() {
        return graphVerticesToVertices.isEmpty();
    }


    public int size(){
        return graphVerticesToVertices.values().stream().map(HashSet::size)
                .reduce(0,Integer::sum);
    }
}
