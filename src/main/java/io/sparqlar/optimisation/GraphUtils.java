package io.sparqlar.optimisation;

import io.sparqlar.optimisation.exceptions.GraphException;
import io.sparqlar.rewriting.exceptions.OptimisationSchemaException;
import io.sparqlar.sparqlardc.propertypath.*;
import io.sparqlar.sparqlardc.terms.SimpleURI;
import io.sparqlar.sparqlardc.triplepatterns.Predicate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class GraphUtils {

    public static Graph loadGraphFromFile(File f) throws OptimisationSchemaException {
        try {
            Graph summary = new Graph();
            BufferedReader bf = new BufferedReader(new FileReader(f));
            String str = bf.readLine();
            Vertex initialVertex = new Vertex("0", true, true);
            HashMap<String, Vertex> allVertices = new HashMap<>();
            HashMap<String, SimpleURI> allPredicates = new HashMap<>();
            while (str != null) {
                String[] split = str.split(" ");
                Vertex vertexSubject = "0".equals(split[0]) ? initialVertex : allVertices.computeIfAbsent(split[0], s -> new Vertex(s, false, true));
                SimpleURI predicateURI = allPredicates.computeIfAbsent(split[1], s -> new SimpleURI(s));
                Vertex vertexObject = allVertices.computeIfAbsent(split[2], s -> new Vertex(s, false, true));
                summary.put(vertexSubject, predicateURI, vertexObject);
                str = bf.readLine();
            }
            return summary;
        } catch (IOException | GraphException e) {
            throw new OptimisationSchemaException(e);
        }
    }

    public static Graph createGraphFromPredicate(Predicate p) throws GraphException {
        Graph createGraph = createGraphFromPredicate(p,
                new Vertex("i", true, false),
                new Vertex("f", false, true), new Counter());
        return createGraph.toDFA();
    }

    private static Graph createGraphFromPredicate(Predicate p, Vertex i, Vertex f, Counter counter) throws GraphException {
        Graph graphFromPropertyPath = new Graph();
        if (p instanceof Alternation) {
            for (int j = 0; j < ((Alternation) p).size(); j++) {
                graphFromPropertyPath.put(createGraphFromPredicate(((Alternation) p).get(j), i, f, counter));
            }
        }
        if (p instanceof Concatenation) {
            Vertex prev = i;
            Vertex vertex = new Vertex("v" + counter.getAndIncrement());
            for (int j = 0; j < ((Concatenation) p).size(); j++) {
                if (j == ((Concatenation) p).size() - 1)
                    graphFromPropertyPath.put(createGraphFromPredicate(((Concatenation) p).get(j), vertex, f, counter));
                else {
                    graphFromPropertyPath.put(createGraphFromPredicate(((Concatenation) p).get(j), prev, vertex, counter));
                    if (j < ((Concatenation) p).size() - 2) {
                        prev = vertex;
                        vertex = new Vertex("v" + counter.getAndIncrement());
                    }
                }
            }
        }
        if (p instanceof Closure) {
            Vertex vertex = new Vertex("v" + counter.getAndIncrement());
            graphFromPropertyPath.put(i, EmptyPath.EMPTY_PATH, vertex);
            graphFromPropertyPath.put(createGraphFromPredicate(((Closure) p).getP(), vertex, vertex, counter));
            graphFromPropertyPath.put(vertex, EmptyPath.EMPTY_PATH, f);

        }
        if (p instanceof TerminalPropertyPath) {
            graphFromPropertyPath.put(i, (TerminalPropertyPath) p, f);
        }
        return graphFromPropertyPath;
    }

    private static class Counter {
        private int c;

        Counter() {
            c = 0;
        }

        protected int getAndIncrement() {
            int v = c;
            c++;
            return v;
        }
    }
}
