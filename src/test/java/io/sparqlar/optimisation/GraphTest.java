package io.sparqlar.optimisation;

import io.sparqlar.optimisation.exceptions.GraphException;
import io.sparqlar.parser.ParseException;
import io.sparqlar.parser.SparqlParser;
import io.sparqlar.sparqlardc.CQSPARQL;
import io.sparqlar.sparqlardc.SPARQL;
import junit.framework.TestCase;

import java.util.HashSet;

public class GraphTest extends TestCase {

    public void testIntersect() throws ParseException, GraphException {
        SPARQL<?> parse = SparqlParser.parse("SELECT * WHERE{ ?x <a>/<b>/<d>|<c>*|<e>/(<v>/<c>|<e>) ?y}");
        System.out.println(parse.toString());
        HashSet<CQSPARQL> cqsparqls = parse.toUCQ();
        System.out.println(cqsparqls);
        Graph graphFromPredicate = GraphUtils.createGraphFromPredicate(cqsparqls.iterator().next().iterator().next().getP());
        System.out.println(graphFromPredicate);
        System.out.println(graphFromPredicate.getPropertyPath());


        SPARQL<?> parse1 = SparqlParser.parse("SELECT * WHERE{ ?x <a>/<b>*/<d>|<c>* ?y}");
        System.out.println(parse1.toString());
        HashSet<CQSPARQL> cqsparqls1 = parse1.toUCQ();
        System.out.println(cqsparqls1);
        Graph graphFromPredicate1 = GraphUtils.createGraphFromPredicate(cqsparqls1.iterator().next().iterator().next().getP());
        System.out.println(graphFromPredicate1);
        System.out.println(graphFromPredicate1.getPropertyPath());

        Graph intersect = graphFromPredicate1.intersect(graphFromPredicate);
        System.out.println(intersect);
        System.out.println(intersect.getPropertyPath());
    }

    public void testToPropertyPath() throws ParseException, GraphException {
        SPARQL<?> parse = SparqlParser.parse("SELECT * WHERE{ ?x (<a>|<n>|<b>)/(<b>|<c>|<h>)|(<b>|<c>) ?y}");
        System.out.println(parse);
        Graph graphFromPredicate = GraphUtils.createGraphFromPredicate(parse.toUCQ().iterator().next().iterator().next().getP());
        graphFromPredicate = graphFromPredicate.intersect(GraphUtils.createGraphFromPredicate(SparqlParser.parse("SELECT * WHERE{ ?x (<a>|<n>)/(<b>|<c>) ?y}").toUCQ().iterator().next().iterator().next().getP()));
        System.out.println(graphFromPredicate);
        System.out.println(graphFromPredicate.getPropertyPath());
    }

    public void testToPropertyPath1() throws ParseException, GraphException {
        SPARQL<?> parse = SparqlParser.parse("SELECT * WHERE{ ?x <a>/<n>*/<b>* ?y}");
        System.out.println(parse);
        Graph graphFromPredicate = GraphUtils.createGraphFromPredicate(parse.toUCQ().iterator().next().iterator().next().getP());
        graphFromPredicate = graphFromPredicate.intersect(GraphUtils.createGraphFromPredicate(SparqlParser.parse("SELECT * WHERE{ ?x <a>/<n>*/<b>* ?y}").toUCQ().iterator().next().iterator().next().getP()));
        System.out.println(graphFromPredicate);
        System.out.println(graphFromPredicate.getPropertyPath());
    }
}