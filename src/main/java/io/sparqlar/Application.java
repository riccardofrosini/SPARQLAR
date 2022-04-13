package io.sparqlar;

import io.sparqlar.optimisation.Graph;
import io.sparqlar.optimisation.GraphUtils;
import io.sparqlar.optimisation.exceptions.GraphException;
import io.sparqlar.parser.ParseException;
import io.sparqlar.parser.SparqlParser;
import io.sparqlar.rewriting.*;
import io.sparqlar.rewriting.exceptions.CostException;
import io.sparqlar.rewriting.exceptions.OntologyException;
import io.sparqlar.rewriting.exceptions.OptimisationContainmentException;
import io.sparqlar.rewriting.exceptions.OptimisationSchemaException;
import io.sparqlar.sparqlardc.CQSPARQL;
import io.sparqlar.sparqlardc.SPARQL;
import org.apache.jena.query.Dataset;
import org.apache.jena.tdb2.TDB2Factory;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.LogManager;

public class Application {

    private static String lubmOntology = "C:\\Users\\frosi\\OneDrive\\Desktop\\lubm\\univ-bench.owl";
    private static String[] LUBMQueries = new String[]{
            "prefix  rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                    "prefix  rdfs:     <http://www.w3.org/2000/01/rdf-schema#>" +
                    "prefix  ub:   <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>" +
                    "SELECT ?x ?z WHERE{ " +
                    "?x ub:publicationAuthor/ub:teacherOf ?c . " +
                    "?x ub:publicationAuthor/ub:teachingAssistantOf  ?c ." +
                    "relax(?x rdf:type ub:Article)  . approx(?x ub:title ?t)}",

            "prefix  rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                    "prefix  rdfs:     <http://www.w3.org/2000/01/rdf-schema#>" +
                    "prefix  ub:   <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>" +
                    "SELECT ?c WHERE{" +
                    "relax(<http://www.Department1.University0.edu/GraduateStudent1> ub:mastersDegreeFrom/ub:hasAlumnus <http://www.Department1.University0.edu/UndergraduateStudent25>) ." +
                    "<http://www.Department1.University0.edu/GraduateStudent1> ub:takesCourse ?c ." +
                    "<http://www.Department1.University0.edu/UndergraduateStudent25> ub:takesCourse ?c}",

/*            "prefix  rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                    "prefix  rdfs:     <http://www.w3.org/2000/01/rdf-schema#> prefix  ub:   <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>" +
                    "SELECT ?x ?z WHERE{" +
                    "relax(?x ub:doctoralDegreeFrom <http://www.University0.edu>) . relax(?x ub:worksFor <http://www.University0.edu>) . " +
                    "?x ub:teacherOf ?c . approx(?z ub:teachingAssistantOf ?c)}",*/

            "prefix  rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                    "prefix  rdfs:     <http://www.w3.org/2000/01/rdf-schema#>" +
                    "prefix  ub:   <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>" +
                    "SELECT * WHERE{ ?z ub:publicationAuthor <http://www.Department1.University0.edu/AssociateProfessor0>." +
                    "approx(?z ub:publicationAuthor/ub:advisor <http://www.Department1.University0.edu/AssociateProfessor0>)}",


            "prefix  rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                    "prefix  rdfs:     <http://www.w3.org/2000/01/rdf-schema#>" +
                    "prefix  ub:   <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>" +
                    "SELECT ?s ?c WHERE{ " +
                    "?x rdf:type ub:AssistantProfessor . ?x ub:teacherOf ?c . " +
                    "?s ub:takesCourse ?c . relax(?s rdf:type ub:UndergraduateStudent) ." +
                    "approx(?s ub:address \"UndergraduateStudent5@Department1.University0.edu\")}",


            /*"prefix  rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                    "prefix  rdfs:     <http://www.w3.org/2000/01/rdf-schema#>" +
                    "prefix  ub:   <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>" +
                    "SELECT * WHERE{" +
                    "<http://www.Department1.University0.edu/GraduateStudent37> ub:advisor/ub:teacherOf ?c . <http://www.Department1.University0.edu/GraduateStudent37> ub:takesCourse ?c ." +
                    "relax(?c rdf:type ub:UndergraduateCourse)}",*/


            "prefix  rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                    "prefix  rdfs:     <http://www.w3.org/2000/01/rdf-schema#>" +
                    "prefix  ub:   <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>" +
                    "SELECT ?p WHERE{" +
                    "relax(<http://www.Department1.University0.edu/ResearchGroup0> ub:subOrganizationOf* ?x) ." +
                    "relax(?p rdf:type ub:AssistantProfessor) . ?p ub:worksFor ?x  ." +
                    "<http://www.Department1.University0.edu/FullProfessor0/Publication0> ub:publicationAuthor ?p}"};


    private static String DBPediaData = "C:\\Users\\frosi\\OneDrive\\Desktop\\DBPedia\\tdb";
    private static String DBPediaOntology = "C:\\Users\\frosi\\OneDrive\\Desktop\\DBPedia\\DBPedia\\dbpedia_2015-10-adj.nt";
    private static String DBPediaSummary2 = "C:\\Users\\frosi\\OneDrive\\Desktop\\DBPedia\\summary_2.ttl";
    private static String DBPediaSummary3 = "C:\\Users\\frosi\\OneDrive\\Desktop\\DBPedia\\summary_3.ttl";
    private static String[] DBPediaQueries = new String[]{
            "prefix  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
                    "prefix  rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                    "prefix ont: <http://dbpedia.org/ontology/> " +
                    "prefix res: <http://dbpedia.org/resource/> SELECT ?y WHERE{approx(<http://dbpedia.org/resource/The_Hobbit> ont:subsequentWork* ?y) . ?y rdf:type  ont:Book}",
            "prefix  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
                    "prefix  rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                    "prefix ont: <http://dbpedia.org/ontology/> " +
                    "prefix res: <http://dbpedia.org/resource/> SELECT ?x ?y WHERE{approx(?x ont:albumBy <http://dbpedia.org/resource/The_Rolling_Stones>) . ?x rdf:type ont:Album . ?y ont:album ?x .relax(?x ont:recordLabel <http://dbpedia.org/resource/London_Records>)} group by ?x",
            "prefix  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
                    "prefix  rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                    "prefix ont: <http://dbpedia.org/ontology/> " +
                    "prefix res: <http://dbpedia.org/resource/> SELECT ?k ?d ?kd WHERE{approx(?k  ont:diedIn <http://dbpedia.org/resource/Battle_of_Poitiers>) .<http://dbpedia.org/resource/Battle_of_Poitiers> ont:date ?d . ?k ont:deathDate  ?kd}",
            "prefix  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
                    "prefix  rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                    "prefix ont: <http://dbpedia.org/ontology/> " +
                    "prefix res: <http://dbpedia.org/resource/> SELECT ?x ?kd WHERE{?x ont:subject <http://dbpedia.org/resource/Duelling_Fatalities> . relax(?x ont:deathDate \"18xx-xx-xx\") .relax(?x rdf:type ont:Scientist)}",
            "prefix  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
                    "prefix  rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                    "prefix ont: <http://dbpedia.org/ontology/> " +
                    "prefix res: <http://dbpedia.org/resource/> SELECT ?x ?f  WHERE{approx(?sdf ont:starring <http://dbpedia.org/resource/12_Angry_Men_(1957_film)>) . ?x ont:parent ?sdf. approx(?f ont:actor ?x).relax(?x ont:birthPlace <http://dbpedia.org/resource/New_York>)}"};

    private static String YagoData = "C:\\Users\\frosi\\OneDrive\\Desktop\\yago\\tdb";
    private static String YagoOntology = "C:\\Users\\frosi\\OneDrive\\Desktop\\yago\\yagoSchema.ttl";
    private static String YagoSummary2 = "C:\\Users\\frosi\\OneDrive\\Desktop\\yago\\summary_2.ttl";
    private static String YagoSummary3 = "C:\\Users\\frosi\\OneDrive\\Desktop\\yago\\summary_3.ttl";
    private static String[] YagoQueries = new String[]{
            "prefix  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
                    "prefix  rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                    "prefix yago: <http://yago-knowledge.org/resource/> SELECT * WHERE{approx(<http://yago-knowledge.org/resource/Battle_of_Waterloo> yago:happenedIn/(yago:hasLongitude|yago:hasLatitude) ?x)}",

            "prefix  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
                    "prefix  rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                    "prefix yago: <http://yago-knowledge.org/resource/> SELECT * WHERE{?x yago:actedIn <http://yago-knowledge.org/resource/Tea_with_Mussolini> . relax(?x yago:hasFamilyName ?z) }",

            "prefix  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
                    "prefix  rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                    "prefix yago: <http://yago-knowledge.org/resource/> SELECT * WHERE{?x rdf:type <http://yago-knowledge.org/resource/wordnet_event_100029378> . ?x yago:happenedOnDate \"1642-11-04^^http://www.w3.org/2001/XMLSchema#date\" .approx(?x yago:happenedIn \"Berkshire\")}",

            "prefix  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
                    "prefix  rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                    "SELECT ?n WHERE " +
                    "{ APPROX(?a <http://yago-knowledge.org/resource/actedIn>/<http://yago-knowledge.org/resource/isLocatedIn> " +
                    "<http://yago-knowledge.org/resource/Australia>) . " +
                    "?a rdfs:label ?n . " +
                    "RELAX(?a rdf:type <http://yago-knowledge.org/resource/wordnet_actor_109765278>) . " +
                    "?city <http://yago-knowledge.org/resource/isLocatedIn> <http://yago-knowledge.org/resource/China> . " +
                    "?a <http://yago-knowledge.org/resource/wasBornIn> ?city . " +
                    "APPROX(?a <http://yago-knowledge.org/resource/directed>/<http://yago-knowledge.org/resource/isLocatedIn> " +
                    "<http://yago-knowledge.org/resource/United_States>)}",

            "prefix  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
                    "prefix  rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                    "SELECT ?n1 ?n2 WHERE " +
                    "{ APPROX(?a rdf:type <http://yago-knowledge.org/resource/wordnet_event_100029378>) . " +
                    "RELAX(?a <http://yago-knowledge.org/resource/happenedIn> ?b ). " +
                    "?p <http://yago-knowledge.org/resource/wasBornIn> ?b . " +
                    "?p <http://yago-knowledge.org/resource/wasBornOnDate> ?d . " +
                    "RELAX(?a <http://yago-knowledge.org/resource/happenedOnDate> ?d) . " +
                    "?a rdfs:label ?n1 . " +
                    "?p rdfs:label ?n2}"};

    private static String[] oldYagoQueries = {
           /*"prefix  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
                    "prefix  rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                    "SELECT ?a WHERE\n" +
                    "{ RELAX(?a rdf:type <http://yago-knowledge.org/resource/wordnet_location_100027167>)}\n"
            ,
            "prefix  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
                    "prefix  rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                    "SELECT ?n WHERE\n" +
                    "{ ?a rdfs:label ?n .\n" +
                    "RELAX(?a <http://yago-knowledge.org/resource/happenedIn> <http://yago-knowledge.org/resource/Berlin>)}\n"
            ,
            "prefix  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
                    "prefix  rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                    "SELECT ?n ?d WHERE\n" +
                    "{ ?a rdfs:label ?n .\n" +
                    "RELAX(?a <http://yago-knowledge.org/resource/happenedIn> <http://yago-knowledge.org/resource/Berlin>) .\n" +
                    "?a <http://yago-knowledge.org/resource/happenedOnDate> ?d}\n"
            ,
            "prefix  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
                    "prefix  rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                    "SELECT ?n ?m WHERE\n" +
                    "{ ?a rdfs:label ?n .\n" +
                    "?a <http://yago-knowledge.org/resource/livesIn> ?b .\n" +
                    "?a <http://yago-knowledge.org/resource/actedIn> ?m .\n" +
                    "RELAX(?m <http://yago-knowledge.org/resource/isLocatedIn> ?b)}\n"
            ,*/
            "prefix  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
                    "prefix  rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                    "SELECT ?n1 ?n2 WHERE\n" +
            "{ ?a rdfs:label ?n1 .\n" +
            "?b rdfs:label ?n2 .\n" +
            "RELAX(?a <http://yago-knowledge.org/resource/isMarriedTo> ?b).\n" +
            "APPROX(?a <http://yago-knowledge.org/resource/livesIn>/<http://yago-knowledge.org/resource/isLocatedIn>* ?p).\n" +
            "APPROX(?b <http://yago-knowledge.org/resource/livesIn>/<http://yago-knowledge.org/resource/isLocatedIn>* ?p)}\n"
            /*,
            "prefix  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
                    "prefix  rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                    "SELECT ?n WHERE\n" +
            "{ APPROX(?a <http://yago-knowledge.org/resource/actedIn>/<http://yago-knowledge.org/resource/isLocatedIn>\n" +
            "<http://yago-knowledge.org/resource/Australia>) .\n" +
            "?a rdfs:label ?n .\n" +
            "RELAX(?a rdf:type <http://yago-knowledge.org/resource/wordnet_actor_109765278>) .\n" +
            "?city <http://yago-knowledge.org/resource/isLocatedIn> <http://yago-knowledge.org/resource/China> .\n" +
            "?a <http://yago-knowledge.org/resource/wasBornIn> ?city .\n" +
            "APPROX(?a <http://yago-knowledge.org/resource/directed>/<http://yago-knowledge.org/resource/isLocatedIn>\n" +
            "<http://yago-knowledge.org/resource/United_States>)}\n"
            ,
            "prefix  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
                    "prefix  rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                    "SELECT ?n1 ?n2 WHERE\n" +
            "{ APPROX(?a rdf:type <event>) .\n" +
            "RELAX(?a <http://yago-knowledge.org/resource/happenedIn> ?b ).\n" +
            "?p <http://yago-knowledge.org/resource/wasBornIn> ?b .\n" +
            "?p <http://yago-knowledge.org/resource/wasBornOnDate> ?d .\n" +
            "RELAX(?a <http://yago-knowledge.org/resource/happenedOnDate> ?d) .\n" +
            "?a rdfs:label ?n1 .\n" +
            "?p rdfs:label ?n2}\n"
            ,

            "prefix  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
                    "prefix  rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                    "SELECT ?c ?n ?p ?l ?d WHERE\n" +
                    "{ ?a <http://yago-knowledge.org/resource/hasFamilyName> ?n .\n" +
                    "?a rdfs:label ?c .\n" +
                    "?a <http://yago-knowledge.org/resource/hasWonPrize> ?p .\n" +
                    "?a <http://yago-knowledge.org/resource/wasBornIn> ?l .\n" +
                    "RELAX(?a <http://yago-knowledge.org/resource/wasBornOnDate> ?d) .\n" +
                    "APPROX(?a rdf:type <http://yago-knowledge.org/resource/wordnet_scientist_110560637>) .\n" +
                    "?a <http://yago-knowledge.org/resource/isMarriedTo> ?b1 .\n" +
                    "?a <http://yago-knowledge.org/resource/isMarriedTo> ?b2}\n" +
                    "Filter (?b1!=?b2)\n"
            ,
            "prefix  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
                    "prefix  rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                    "SELECT ?c ?n ?p ?l ?d WHERE\n" +
                    "{ ?a <http://yago-knowledge.org/resource/hasFamilyName> ?n .\n" +
                    "?a rdfs:label ?c .\n" +
                    "?a <http://yago-knowledge.org/resource/hasWonPrize> ?p .\n" +
                    "?a <http://yago-knowledge.org/resource/wasBornIn> ?l .\n" +
                    "?a <http://yago-knowledge.org/resource/wasBornOnDate> ?d .\n" +
                    "RELAX(?a rdf:type <http://yago-knowledge.org/resource/wordnet_scientist_110560637>) .\n" +
                    "?a <http://yago-knowledge.org/resource/isMarriedTo> ?b .\n" +
                    "?b <http://yago-knowledge.org/resource/wasBornOnDate> ?d .\n" +
                    "RELAX(?l <http://yago-knowledge.org/resource/isLocatedIn>* <http://yago-knowledge.org/resource/Germany>)}\n"
            ,
            "prefix  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
                    "prefix  rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                    "SELECT ?n ?n1 ?n2 WHERE\n" +
                    "{ ?a rdfs:label ?n .\n" +
                    "RELAX(?a rdf:type <http://yago-knowledge.org/resource/wordnet_actor_109765278> ).\n" +
                    "APPROX(?a <http://yago-knowledge.org/resource/wasBornIn> ?city) .\n" +
                    "?a <http://yago-knowledge.org/resource/actedIn> ?m1 .\n" +
                    "?m1 <http://yago-knowledge.org/resource/isLocatedIn> <http://yago-knowledge.org/resource/Australia> .\n" +
                    "?a <http://yago-knowledge.org/resource/directed> ?m2 .\n" +
                    "?m2 <http://yago-knowledge.org/resource/isLocatedIn> <http://yago-knowledge.org/resource/Australia>.\n" +
                    "APPROX(?city <http://yago-knowledge.org/resource/isLocatedIn>\n" +
                    "<http://yago-knowledge.org/resource/United_States>) .\n" +
                    "?m1 rdfs:label ?n1 .\n" +
                    "?m2 rdfs:label ?n2}"*/};


    public static void main(String[] args) throws OptimisationSchemaException, ParseException, CostException, OntologyException, OptimisationContainmentException, GraphException {
        //new MainWindow();

        LogManager.getLogManager().reset();
        Approximating approximating = new Approximating(1, 1, 1, new HashSet<>(), new HashSet<>());
        lubmRun(approximating);
        dbPediaRun(approximating);
        yagoRun(approximating);
        //for (String s : LUBMQueries) {
        //    System.out.println(s);
        //}
        //for (String s : DBPediaQueries) {
        //    System.out.println(s);
        //}
        //for (String s : YagoQueries) {
        //    System.out.println(s);
        //}
        /*CQSPARQL parse1 = (CQSPARQL) SparqlParser.parse("SELECT * WHERE {?x <asd> ?z}").toUCQ().iterator().next();
        CQSPARQL parse2 = (CQSPARQL) SparqlParser.parse("SELECT * WHERE {?x <asd> ?z}").toUCQ().iterator().next();
        QueryCost<CQSPARQL> sparqlQueryCost1 = new QueryCost<>(parse1, 1);
        QueryCost<CQSPARQL> sparqlQueryCost2 = new QueryCost<>(parse2, 1);
        System.out.println(RewritingAlgorithm.removeRedundantQueriesAndSimplify(Arrays.asList(sparqlQueryCost1,sparqlQueryCost2)));*/
    }

    private static void lubmRun(Approximating approximating) throws CostException, OntologyException, ParseException, OptimisationContainmentException {
        Ontology ontologyLUBM = new Ontology(lubmOntology);
        Relaxing relaxing = new Relaxing(1, 1, 1, 1, ontologyLUBM);
        for (String lubmQuery : LUBMQueries) {
            SPARQL<?> parse = SparqlParser.parse(lubmQuery);
            System.out.println("query : " + parse);
            for (int i = 1; i <= 3; i++) {
                List<QueryCost<CQSPARQL>> rewrite = RewritingAlgorithm.rewrite(parse, i, approximating, relaxing, false);
                //System.out.println("Rewriting: " + rewrite.size());
                rewrite = RewritingAlgorithm.rewrite(parse, i, approximating, relaxing, true);
                System.out.println("Rewriting containment: " + rewrite.size());
            }
        }
    }

    private static void yagoRun(Approximating approximating) throws CostException, OntologyException, OptimisationSchemaException, ParseException, OptimisationContainmentException {
        Dataset datasetYago = TDB2Factory.connectDataset(YagoData);

        Ontology ontologyYago = new Ontology(YagoOntology);
        Relaxing relaxing = new Relaxing(1, 1, 1, 1, ontologyYago);
        Graph graphYGraph2 = GraphUtils.loadGraphFromFile(new File(YagoSummary2));
        Graph graphYGraph3 = GraphUtils.loadGraphFromFile(new File(YagoSummary3));
        for (String yagoQuery : YagoQueries) {
            SPARQL<?> parse = SparqlParser.parse(yagoQuery);
            System.out.println("query : " + parse);
            for (int i = 0; i <= 3; i++) {
                List<QueryCost<CQSPARQL>> rewrite = RewritingAlgorithm.rewrite(parse, i, approximating, relaxing, false);
                //System.out.println("Rewriting: " + rewrite.size());
                //System.out.println(rewrite);
                rewrite = RewritingAlgorithm.rewrite(parse, i, approximating, relaxing, true);
                System.out.println("Rewriting containment: " + rewrite.size());
                //new ApproxRelaxEvaluator(datasetYago,rewrite).getRemainingAnswers();
                //System.out.println(rewrite);
                //rewrite = RewritingAlgorithm.rewrite(parse, i, approximating, relaxing, graphYGraph2, false);
                //System.out.println("Rewriting summary2: " + rewrite.size());
                //new ApproxRelaxEvaluator(datasetYago,rewrite).getRemainingAnswers();
                //System.out.println(rewrite);
                //rewrite = RewritingAlgorithm.rewrite(parse, i, approximating, relaxing, graphYGraph2, true);
                //System.out.println("Rewriting containment and summary2: " + rewrite.size());
                //new ApproxRelaxEvaluator(datasetYago,rewrite).getRemainingAnswers();
                //System.out.println(rewrite);
                //rewrite = RewritingAlgorithm.rewrite(parse, i, approximating, relaxing, graphYGraph3, false);
                //System.out.println("Rewriting summary3: " + rewrite.size());
                //System.out.println("Answers: " + new ApproxRelaxEvaluator(datasetYago, rewrite).getRemainingAnswers().size());
                //new ApproxRelaxEvaluator(datasetYago,rewrite).getRemainingAnswers();
                //System.out.println(rewrite);
                rewrite = RewritingAlgorithm.rewrite(parse, i, approximating, relaxing, graphYGraph3, true);
                System.out.println("Rewriting containment and summary3: " + rewrite.size());
                //new ApproxRelaxEvaluator(datasetYago,rewrite).getRemainingAnswers();
                //System.out.println(rewrite);
            }
        }
    }

    private static void dbPediaRun(Approximating approximating) throws CostException, OntologyException, OptimisationSchemaException, ParseException, OptimisationContainmentException {
        Dataset datasetDBPedia = TDB2Factory.connectDataset(DBPediaData);
        Ontology ontologyDBPedia = new Ontology(DBPediaOntology);
        Relaxing relaxing = new Relaxing(1, 1, 1, 1, ontologyDBPedia);
        Graph graphDBPedia = GraphUtils.loadGraphFromFile(new File(DBPediaSummary2));
        for (String dbPediaQuery : DBPediaQueries) {
            SPARQL<?> parse = SparqlParser.parse(dbPediaQuery);
            System.out.println("query : " + parse);
            for (int i = 1; i <= 3; i++) {
                List<QueryCost<CQSPARQL>> rewrite = RewritingAlgorithm.rewrite(parse, i, approximating, relaxing, false);
                //System.out.println("Rewriting: " + rewrite.size());
                rewrite = RewritingAlgorithm.rewrite(parse, i, approximating, relaxing, true);
                System.out.println("Rewriting containment: " + rewrite.size());
                //rewrite = RewritingAlgorithm.rewrite(parse, i, approximating, relaxing, graphDBPedia, false);
                //System.out.println("Rewriting summary: " + rewrite.size());
                rewrite = RewritingAlgorithm.rewrite(parse, i, approximating, relaxing, graphDBPedia, true);
                System.out.println("Rewriting containment and summary: " + rewrite.size());
            }
        }
    }

}
