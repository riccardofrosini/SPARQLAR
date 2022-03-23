/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.executors.answers;


import io.sparqlar.sparqlardc.SPARQL;
import io.sparqlar.sparqlardc.terms.Constant;
import io.sparqlar.sparqlardc.terms.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author riccardo
 */
public class AnswersContainer {
    private final HashMap<AnswerCost, Integer> answers;
    private final List<AnswerCost> listAnswers;
    private final List<Variable> variables;

    public AnswersContainer(List<Variable> variables) {
        this.listAnswers = new ArrayList<>();
        this.answers = new HashMap<>();
        this.variables = variables;
    }

    //this is only for optimisation
    public static AnswersContainer join(AnswersContainer ac1, AnswersContainer ac2) {
        List<Variable> v = new ArrayList<>(ac1.variables);
        v.removeAll(ac2.variables);
        v.addAll(ac2.variables);
        AnswersContainer ret = new AnswersContainer(v);
        for (AnswerCost ans1 : ac1.listAnswers) {
            for (AnswerCost ans2 : ac2.listAnswers) {
                if (AnswerCost.compatible(ans1, ans2)) ret.add(AnswerCost.join(ans1, ans2));
            }
        }
        return ret;
    }

    public static AnswersContainer union(AnswersContainer ac1, AnswersContainer ac2) {
        AnswersContainer ret = new AnswersContainer(ac1.variables);
        ret.variables.removeAll(ac2.variables);
        ret.variables.addAll(ac2.variables);
        ret.listAnswers.addAll(ac1.listAnswers);
        ret.answers.putAll(ac1.answers);
        for (AnswerCost ans : ac2.listAnswers) {
            ret.add(ans);
        }
        return ret;
    }

    public void updateCostQuery(SPARQL<?> query, float cost) {
        for (AnswerCost listAnswer : listAnswers) {
            listAnswer.setCost(cost);
            listAnswer.setQuery(query);
        }
    }

    public static AnswersContainer project(AnswersContainer ac, List<Variable> v) {
        AnswersContainer ret = new AnswersContainer(v);
        for (AnswerCost ans : ac.listAnswers) {
            ret.add(AnswerCost.project(ans, v));
        }
        return ret;
    }

    public int size() {
        return listAnswers.size();
    }

    public boolean add(AnswerCost ac) {
        if (answers.containsKey(ac)) {
            int indexOf = answers.get(ac);
            AnswerCost get = listAnswers.get(indexOf);
            if (get.getCost() > ac.getCost()) {
                listAnswers.set(indexOf, ac);
                answers.put(ac, indexOf);
            }
            return false;
        }
        listAnswers.add(ac);
        answers.put(ac, listAnswers.size() - 1);
        return true;
    }

    public Constant getIthAnswerVariable(int index, String var) {
        return listAnswers.get(index).get(new Variable(var));
    }

    public float getIthAnswerCost(int index) {
        return listAnswers.get(index).getCost();
    }

    public String[] getVariablesAsArray() {
        return variables.stream().map(Variable::getVar).toArray(String[]::new);
    }

    @Override
    public String toString() {
        return listAnswers.stream().map(answerCost -> answerCost.toString())
                .collect(Collectors.joining("\n"));
    }

    public String getIthAnswerQuery(int index) {
        return listAnswers.get(index).getQuery();
    }

}
