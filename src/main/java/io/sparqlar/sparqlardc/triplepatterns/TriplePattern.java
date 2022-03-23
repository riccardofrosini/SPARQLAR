package io.sparqlar.sparqlardc.triplepatterns;

import io.sparqlar.sparqlardc.querypatterns.QueryPattern;
import io.sparqlar.sparqlardc.terms.URI;
import io.sparqlar.sparqlardc.terms.Variable;

import java.util.HashSet;
import java.util.Objects;

/**
 * @author riccardo
 */
public class TriplePattern extends QueryPattern {
    private final Subject s;
    private final Predicate p;
    private final Objec o;

    public TriplePattern(Subject s, Predicate p, Objec o) {
        this.s = s;
        this.p = p;
        this.o = o;
    }

    public Subject getS() {
        return s;
    }

    public Predicate getP() {
        return p;
    }

    public Objec getO() {
        return o;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.s, this.p, this.o);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TriplePattern)) {
            return false;
        }
        final TriplePattern other = (TriplePattern) obj;
        if (!Objects.equals(this.s, other.s)) {
            return false;
        }
        if (!Objects.equals(this.p, other.p)) {
            return false;
        }
        return Objects.equals(this.o, other.o);
    }

    @Override
    public String toString() {
        return new StringBuilder().append(s).append(" ").append(p).append(" ").append(o).toString();
    }

    @Override
    public String toStringExecutable() {
        return new StringBuilder().append(s).append(" ").append(p).append(" ").append(o).toString();
    }

    @Override
    public HashSet<Variable> getVariableSet() {
        HashSet<Variable> ret = new HashSet<>(3);
        if (s instanceof Variable) ret.add((Variable) s);
        if (o instanceof Variable) ret.add((Variable) o);
        if (p instanceof Variable) ret.add((Variable) p);
        return ret;
    }

    @Override
    public boolean containsApprox() {
        return false;
    }

    @Override
    public boolean containsRelax() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public HashSet<HashSet<TriplePattern>> toJoinOfTriplePattern() {
        HashSet<HashSet<TriplePattern>> ret = new HashSet<>(1);
        HashSet<TriplePattern> tp = new HashSet<>(1);
        tp.add(this);
        ret.add(tp);
        return ret;
    }

    @Override
    public HashSet<URI> getApproximatedURIs() {
        HashSet<URI> ret = new HashSet<>();
        if (this.containsApprox()) {
            ret.addAll(p.getURIs());
        }
        return ret;
    }
}
