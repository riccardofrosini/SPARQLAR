/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.rewriting;

import io.sparqlar.rewriting.exceptions.CostException;
import io.sparqlar.rewriting.exceptions.OntologyException;
import io.sparqlar.sparqlardc.CQSPARQL;
import io.sparqlar.sparqlardc.propertypath.*;
import io.sparqlar.sparqlardc.terms.Constant;
import io.sparqlar.sparqlardc.terms.URI;
import io.sparqlar.sparqlardc.triplepatterns.Flex;
import io.sparqlar.sparqlardc.triplepatterns.Objec;
import io.sparqlar.sparqlardc.triplepatterns.Relax;
import io.sparqlar.sparqlardc.triplepatterns.Subject;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 * @author riccardo
 */
public class Relaxing {
    private final float sp;
    private final float sc;
    private final float dom;
    private final float range;
    private final Ontology o;
    private final Avoid star = new Avoid();

    public Relaxing(float sp, float sc, float dom, float range, Ontology o) throws CostException, OntologyException {
        if (sp <= 0 || sc <= 0 || dom <= 0 || range <= 0) throw CostException.buildCostException(false);
        if (o == null) throw new OntologyException();
        this.sp = sp;
        this.sc = sc;
        this.dom = dom;
        this.range = range;
        this.o = o;
    }

    public HashMap<CQSPARQL, Float> applyRelax(CQSPARQL q, Flex tp) {
        HashMap<CQSPARQL, Float> ret = new HashMap<>();
        for (Entry<Flex, Float> rs : relaxTriplePattern(tp).entrySet()) {
            CQSPARQL queryWithReplacedTriplePattern = q.replaceTriplePattern(tp, rs.getKey());
            if (queryWithReplacedTriplePattern != null) {
                ret.put(queryWithReplacedTriplePattern, rs.getValue());
            }
        }
        return ret;
    }

    private HashMap<Flex, Float> relaxTriplePattern(Flex triplePattern) {
        HashMap<Flex, Float> ret = new HashMap<>();
        Subject subject = triplePattern.getS();
        PropertyPath propertyPath = triplePattern.getP();
        Objec object = triplePattern.getO();
        if (propertyPath instanceof URI) {
            for (URI u : this.o.getSuperProperties((URI) propertyPath)) {
                if (triplePattern instanceof Relax)
                    ret.put(new Relax(subject, u, object), sp);
                else ret.put(new Flex(subject, u, object), sp);
            }
            if (propertyPath.equals(URI.TYPE) && object instanceof URI) {
                for (URI u : this.o.getSuperClasses((URI) object)) {
                    if (triplePattern instanceof Relax) ret.put(new Relax(subject, URI.TYPE, u), sc);
                    else ret.put(new Flex(subject, URI.TYPE, u), sc);
                }
            }
            if (object instanceof Constant) {
                for (URI u : this.o.getDomain((URI) propertyPath)) {
                    if (triplePattern instanceof Relax) ret.put(new Relax(subject, URI.TYPE, u), dom);
                    else ret.put(new Flex(subject, URI.TYPE, u), dom);
                }
            }
            if (subject instanceof Constant) {
                for (URI u : this.o.getRange((URI) propertyPath)) {
                    if (triplePattern instanceof Relax)
                        ret.put(new Relax(u, InverseTypeURI.INV_TYPE, object), range);
                    else ret.put(new Flex(u, InverseTypeURI.INV_TYPE, object), range);
                }
            }
        }
        //INV_TYPE is not a URI anymore so is a special case
        if (propertyPath.equals(InverseTypeURI.INV_TYPE) && subject instanceof URI) {
            for (URI u : this.o.getSuperClasses((URI) subject)) {
                if (triplePattern instanceof Relax)
                    ret.put(new Relax(u, InverseTypeURI.INV_TYPE, object), sc);
                else ret.put(new Flex(u, InverseTypeURI.INV_TYPE, object), sc);
            }
        }
        if (propertyPath instanceof Concatenation) {
            Concatenation con = (Concatenation) propertyPath;
            for (int i = 0; i < con.size(); i++) {
                if (i == 0) {
                    for (Entry<Flex, Float> entry : relaxTriplePattern(new Relax(subject, con.get(i), star)).entrySet()) {
                        Flex key = entry.getKey();
                        if (triplePattern instanceof Relax)
                            ret.put(new Relax(key.getS(), con.replace(0, key.getP()), object), entry.getValue());
                        else
                            ret.put(new Flex(key.getS(), con.replace(i, key.getP()), object), entry.getValue());
                    }
                } else if (i == con.size() - 1) {
                    for (Entry<Flex, Float> entry : relaxTriplePattern(new Relax(star, con.get(i), object)).entrySet()) {
                        Flex key = entry.getKey();
                        if (triplePattern instanceof Relax)
                            ret.put(new Relax(key.getS(), con.replace(con.size() - 1, key.getP()), object), entry.getValue());
                        else
                            ret.put(new Flex(key.getS(), con.replace(i, key.getP()), object), entry.getValue());
                    }
                } else {
                    for (Entry<Flex, Float> entry : relaxTriplePattern(new Relax(star, con.get(i), star)).entrySet()) {
                        Flex key = entry.getKey();
                        if (triplePattern instanceof Relax)
                            ret.put(new Relax(key.getS(), con.replace(con.size() - 1, key.getP()), object), entry.getValue());
                        else
                            ret.put(new Flex(key.getS(), con.replace(i, key.getP()), object), entry.getValue());
                    }
                }
            }
        }
        if (propertyPath instanceof Alternation) {
            Alternation al = (Alternation) propertyPath;
            for (int i = 0; i < al.size(); i++) {
                for (Entry<Flex, Float> entry : relaxTriplePattern(new Relax(subject, al.get(i), object)).entrySet()) {
                    Flex key = entry.getKey();
                    if (triplePattern instanceof Relax)
                        ret.put(new Relax(key.getS(), key.getP(), key.getO()), entry.getValue());
                    else ret.put(new Flex(key.getS(), key.getP(), key.getO()), entry.getValue());
                }
            }
        }

        if (propertyPath instanceof Closure) {
            Closure cl = (Closure) propertyPath;
            for (Entry<Flex, Float> entry : relaxTriplePattern(new Relax(star, cl.getP(), star)).entrySet()) {
                Flex key = entry.getKey();
                if (triplePattern instanceof Relax)
                    ret.put(new Relax(subject, new Concatenation(cl, key.getP(), cl), object), entry.getValue());
                else
                    ret.put(new Flex(subject, new Concatenation(cl, key.getP(), cl), object), entry.getValue());
            }
            for (Entry<Flex, Float> entry : relaxTriplePattern(new Relax(subject, cl.getP(), star)).entrySet()) {
                Flex key = entry.getKey();
                if (triplePattern instanceof Relax)
                    ret.put(new Relax(key.getS(), new Concatenation(key.getP(), cl), object), entry.getValue());
                else
                    ret.put(new Flex(key.getS(), new Concatenation(key.getP(), cl), object), entry.getValue());
            }
            for (Entry<Flex, Float> entry : relaxTriplePattern(new Relax(star, cl.getP(), object)).entrySet()) {
                Flex key = entry.getKey();
                if (triplePattern instanceof Relax)
                    ret.put(new Relax(subject, new Concatenation(cl, key.getP()), key.getO()), entry.getValue());
                else
                    ret.put(new Flex(subject, new Concatenation(cl, key.getP()), key.getO()), entry.getValue());
            }
        }
        return ret;
    }

    private static class Avoid implements Subject, Objec {
        public Avoid() {
        }
    }
}
