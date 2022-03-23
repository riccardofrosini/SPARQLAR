/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.rewriting;

import io.sparqlar.rewriting.exceptions.CostException;
import io.sparqlar.sparqlardc.CQSPARQL;
import io.sparqlar.sparqlardc.propertypath.*;
import io.sparqlar.sparqlardc.terms.URI;
import io.sparqlar.sparqlardc.triplepatterns.Approx;
import io.sparqlar.sparqlardc.triplepatterns.Flex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

/**
 * @author riccardo
 */
public class Approximating {


    private final HashSet<URI> noDeletion;
    private final HashSet<URI> noSubstitution;

    private final float insert;
    private final float delete;
    private final float substitution;

    public Approximating(float delete, float insert, float substitution, HashSet<URI> noDeletion, HashSet<URI> noSubstitution) throws CostException {
        if (delete <= 0 || insert <= 0 || substitution <= 0) throw CostException.buildCostException(false);
        this.delete = delete;
        this.insert = insert;
        this.substitution = substitution;
        this.noDeletion = noDeletion;
        this.noSubstitution = noSubstitution;
    }

    public HashMap<CQSPARQL, Float> applyApprox(CQSPARQL q, Flex tp) {
        HashMap<CQSPARQL, Float> ret = new HashMap<>();
        for (Entry<PropertyPath, Float> pps : approxRegex(tp.getP(), false).entrySet()) {
            CQSPARQL clone;
            if (tp instanceof Approx)
                clone = q.replaceTriplePattern(tp, new Approx(tp.getS(), pps.getKey(), tp.getO()));
            else clone = q.replaceTriplePattern(tp, new Flex(tp.getS(), pps.getKey(), tp.getO()));
            if (clone != null) {
                ret.put(clone, pps.getValue());
            }
        }
        return ret;
    }

    private HashMap<PropertyPath, Float> approxRegex(PropertyPath propertyPath, boolean singletonKleeneClosure) {
        HashMap<PropertyPath, Float> ret = new HashMap<>();
        if (propertyPath instanceof URI || propertyPath.equals(InverseTypeURI.INV_TYPE)) {
            if (propertyPath instanceof URI) { //this is because of the flex operator if URI then normal run
                if (!noDeletion.contains(propertyPath)) ret.put(EmptyPath.EMPTY_PATH, delete);
                if (!noSubstitution.contains(propertyPath)) ret.put(new NotURI((URI) propertyPath), substitution);
            } else { // if is INV_TYPE then always delete and substitute with ALL URI
                ret.put(EmptyPath.EMPTY_PATH, delete);
                ret.put(NotURI.ALL_URI, substitution);
            }
            if (singletonKleeneClosure) { //impossible if is INV_TYPE
                ret.put(NotURI.ALL_URI, insert);
            } else {
                ret.put(new Concatenation(propertyPath, NotURI.ALL_URI), insert);
                ret.put(new Concatenation(NotURI.ALL_URI, propertyPath), insert);
            }
        }
        if (propertyPath instanceof Concatenation) {
            Concatenation con = (Concatenation) propertyPath;
            for (int i = 0; i < con.size(); i++) {
                PropertyPath p = con.get(i);
                for (Entry<PropertyPath, Float> entry : approxRegex(p, false).entrySet()) {
                    ret.put(con.replace(i, entry.getKey()), entry.getValue());
                }
            }
        }
        if (propertyPath instanceof Alternation) {
            Alternation alt = (Alternation) propertyPath;
            for (int i = 0; i < alt.size(); i++) {
                for (Entry<PropertyPath, Float> entry : approxRegex(alt.get(i), false).entrySet()) {
                    ret.put(entry.getKey(), entry.getValue());
                }
            }
        }

        if (propertyPath instanceof Closure) {
            Closure cl = (Closure) propertyPath;
            for (Entry<PropertyPath, Float> entry : approxRegex(cl.getP(), true).entrySet()) {
                if (entry.getKey() instanceof EmptyPath) ret.put(cl, entry.getValue());
                else
                    ret.put(new Concatenation(cl, entry.getKey(), cl), entry.getValue());
            }
        }
        return ret;
    }

}
