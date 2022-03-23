package io.sparqlar.executors;

import io.sparqlar.executors.answers.AnswersContainer;
import io.sparqlar.rewriting.QueryCost;
import io.sparqlar.sparqlardc.CQSPARQL;
import io.sparqlar.sparqlardc.terms.Variable;
import org.apache.jena.query.Dataset;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

public class CachedApproxRelaxEvaluator extends Evaluator {
    private static final Logger logger = Logger.getLogger(CachedApproxRelaxEvaluator.class.getName());
    private HashMap<CQSPARQL, AnswersContainer> cache;
    private final List<QueryCost<CQSPARQL>> queries;


    public CachedApproxRelaxEvaluator(Dataset dataset, List<QueryCost<CQSPARQL>> queries) {
        super(dataset);
        this.queries = queries;
        this.cache = new HashMap<>();
    }

    @Override
    public AnswersContainer getNextAnswers(int answers) {
        throw new RuntimeException("Method not implemented as unclear how to limit answers to exact value with caching system");
    }

    @Override
    public AnswersContainer getRemainingAnswers() {
        long l = System.currentTimeMillis();
        firstExecution();
        AnswersContainer nextAnswers = new AnswersContainer(queries.get(0).getQuery().copySelectVariables());
        for (QueryCost<CQSPARQL> query : queries) {
            List<Variable> variables = query.getQuery().copySelectVariables();
            HashSet<CQSPARQL> connectedTriplePatterns = query.getQuery().getConnectedTriplePatterns();
            for (CQSPARQL connectedTriplePattern : connectedTriplePatterns) {
                if (!cache.containsKey(connectedTriplePattern) && cache.values().stream().map(AnswersContainer::size).reduce(Integer::sum).orElse(0) < 1000000) {
                    SimpleEvaluator simpleEvaluator = new SimpleEvaluator(dataset, new QueryCost<>(connectedTriplePattern, 0));
                    cache.put(connectedTriplePattern, simpleEvaluator.getRemainingAnswers());
                    simpleEvaluator.close();
                }
            }

            CQSPARQL connectedTriplePattern = null;
            CQSPARQL complementary = null;
            for (CQSPARQL connectedTriplePatternTemp : connectedTriplePatterns) {
                CQSPARQL complementaryTemp = query.getQuery().getComplementary(connectedTriplePatternTemp);
                if (cache.containsKey(complementaryTemp) && cache.containsKey(connectedTriplePatternTemp)) {
                    connectedTriplePattern = connectedTriplePatternTemp;
                    complementary = complementaryTemp;
                    break;
                } else if (cache.containsKey(connectedTriplePatternTemp)) {
                    connectedTriplePattern = connectedTriplePatternTemp;
                    complementary = null;
                } else if (cache.containsKey(complementaryTemp)) {
                    connectedTriplePattern = null;
                    complementary = complementaryTemp;
                }
            }

            if (cache.containsKey(complementary) && cache.containsKey(connectedTriplePattern)) {
                AnswersContainer ans = AnswersContainer.join(cache.get(complementary), cache.get(connectedTriplePattern));
                ans.updateCostQuery(query.getQuery(), query.getCost());
                nextAnswers = AnswersContainer.union(nextAnswers, AnswersContainer.project(ans, variables));
            } else if (cache.containsKey(connectedTriplePattern)) {
                SimpleEvaluator simpleEvaluator = new SimpleEvaluator(dataset, new QueryCost<>(complementary, query.getCost()));
                AnswersContainer answersContainer = AnswersContainer.project(AnswersContainer.join(cache.get(connectedTriplePattern), simpleEvaluator.getRemainingAnswers()), variables);
                answersContainer.updateCostQuery(query.getQuery(), query.getCost());
                nextAnswers = AnswersContainer.union(nextAnswers, answersContainer);
                simpleEvaluator.close();
            } else if (cache.containsKey(complementary)) {
                SimpleEvaluator simpleEvaluator = new SimpleEvaluator(dataset, new QueryCost<>(connectedTriplePattern, query.getCost()));
                AnswersContainer answersContainer = AnswersContainer.project(AnswersContainer.join(cache.get(complementary), simpleEvaluator.getRemainingAnswers()), variables);
                answersContainer.updateCostQuery(query.getQuery(), query.getCost());
                nextAnswers = AnswersContainer.union(nextAnswers, answersContainer);
                simpleEvaluator.close();
            } else {
                SimpleEvaluator simpleEvaluator = new SimpleEvaluator(dataset, query);
                nextAnswers = AnswersContainer.union(nextAnswers, simpleEvaluator.getRemainingAnswers());
            }

        }

        logger.info("Execution time: " + (System.currentTimeMillis() - l));
        logger.info("Answers: " + nextAnswers.size());
        return nextAnswers;
    }

    @Override
    protected void firstExecution() {
        throw new RuntimeException("Not implemented as not needed");
    }
}
