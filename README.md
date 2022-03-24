# SPARQLAR
SPARQLAR interpreter and evaluator with optimisations systems.


## Compiling and Running
The code has to be compiled using MAVEN command:
```mvn clean install```
This will generate a JAR file that can be executed using:
```java -jar SPARQLAR-1.0-SNAPSHOT.jar```

## Contents
pom.xml for managing the libraries and building the project.

SPARQLAR/src/main/resources/SparqlParser.jj  This file contains the java-compiler-compiler source code that can be usied to automatically generate 
the java code that parses SPARQLAR strings. The code is in SPARQLAR/src/main/java/io/sparqlar/parser/

SPARQLAR/src/main/java/io/sparqlar/executors/ this folder contains three SPARQL executors that use the Jena library to produce the responses.
The SimpleEvaluator.java executes one query at a time whereas the other two evaluators can execute several SPARQL queries (generated from the rewriting algorithm).

SPARQLAR/src/main/java/io/sparqlar/sparqlardc/ This folder contains the class representation of SPARQLAR queries.

SPARQLAR/src/main/java/io/sparqlar/rewriting/ This folder contains the rewriting algorithm for SPARQLAR. The algorithm will generate a list of SPARQL queries and their cost.

SPARQLAR/src/main/java/io/sparqlar/optimisation/ This folder contains utils for the summary optimisation algorithm and the query containment optimisation algorithm.
The actual optimisation algorith is in SPARQLAR/src/main/java/io/sparqlar/rewriting/RewritingAlgorithm.java 

SPARQLAR/src/main/java/io/sparqlar/ui/ This folder contains the UI code.

SPARQLAR/src/main/java/io/sparqlar/BuildSummary.java This file contains the algorithm that produces the summary from a database in TDB format. 
The code is a standalone project that is should be executed independently and is not parametrised (the folder needs to be changed directly from the code and executed).

## Editing and debugging
For editing and debuggning I would suggest to use "Intellij Idea" as the UI is implemented using the wysiwyg interface.

