/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.executors.answers;

import io.sparqlar.sparqlardc.SPARQL;
import io.sparqlar.sparqlardc.terms.Constant;
import io.sparqlar.sparqlardc.terms.Literal;
import io.sparqlar.sparqlardc.terms.SimpleURI;
import io.sparqlar.sparqlardc.terms.Variable;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AnswerCost implements Comparable<AnswerCost> {
    private static final Logger logger = Logger.getLogger(AnswerCost.class.getName());
    private final HashMap<Variable, Constant> querySolution;
    private float cost;
    private SPARQL<?> query;

    public AnswerCost(QuerySolution qs, float cost, SPARQL<?> q) {
        this.querySolution = new HashMap<>();
        Iterator<String> it = qs.varNames();
        while (it.hasNext()) {
            String s = it.next();
            RDFNode get = qs.get(s);
            if (get.isLiteral()) querySolution.put(new Variable(s), new Literal("\"" + get + "\""));
            else if (get.isURIResource())
                querySolution.put(new Variable(s), new SimpleURI(get.toString().replaceAll("[\"`]", "")));
            else logger.severe("Something is wrong with ANSWER COST!");

        }
        this.cost = cost;
        this.query = q;
    }

    private AnswerCost(HashMap<Variable, Constant> querySolution, float cost, SPARQL<?> q) {
        this.querySolution = querySolution;
        this.cost = cost;
        this.query = q;
    }

    public AnswerCost(float cost, SPARQL<?> q) {
        querySolution = new HashMap<>();
        this.cost = cost;
        this.query = q;
    }

    //this is only for caching optimisation
    public static AnswerCost join(AnswerCost ac1, AnswerCost ac2) {
        HashMap<Variable, Constant> querySolution = new HashMap<>(ac1.querySolution.size() + ac2.querySolution.size());
        querySolution.putAll(ac1.querySolution);
        querySolution.putAll(ac2.querySolution);
        //this is not permanent the final query will be given at the end of the process.
        return new AnswerCost(querySolution, ac1.cost + ac2.cost, null);

    }

    public static boolean compatible(AnswerCost ac1, AnswerCost ac2) {
        for (Map.Entry<Variable, Constant> e : ac1.querySolution.entrySet()) {
            if (ac2.querySolution.containsKey(e.getKey()) && !ac2.querySolution.get(e.getKey()).equals(e.getValue()))
                return false;
        }
        return true;
    }

    public static AnswerCost project(AnswerCost ans, List<Variable> vs) {
        HashMap<Variable, Constant> newQuerySolution = new HashMap<>(vs.size());
        for (Variable v : vs) {
            if (ans.querySolution.containsKey(v)) {
                newQuerySolution.put(v, ans.querySolution.get(v));
            }
        }
        return new AnswerCost(newQuerySolution, ans.cost, ans.query);
    }

    public Constant get(Variable varName) {
        return querySolution.get(varName);
    }

    public float getCost() {
        return cost;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AnswerCost)) {
            return false;
        }
        final AnswerCost other = (AnswerCost) obj;
        return Objects.equals(this.querySolution, other.querySolution);
    }

    @Override
    public int hashCode() {
        return 93 * Objects.hashCode(this.querySolution) + 4;
    }

    @Override
    public int compareTo(AnswerCost ac) {
        if (cost > ac.cost) return 1;
        if (cost == ac.cost) {
            if (ac.querySolution.equals(querySolution)) return 0;
            if (ac.querySolution.hashCode() > querySolution.hashCode()) return 1;
        }
        return -1;
    }

    @Override
    public String toString() {
        String ret = querySolution.entrySet().stream().map(variableConstantEntry -> variableConstantEntry.getKey() + "=" + variableConstantEntry.getValue())
                .collect(Collectors.joining(",", "[", "]\n"));
        return ret + " cost: " + cost;
    }

    public String getQuery() {
        return query.toStringExecutable();
    }


    protected void setCost(float cost){
        this.cost=cost;
    }

    protected void setQuery(SPARQL<?> query){
        this.query=query;
    }

}