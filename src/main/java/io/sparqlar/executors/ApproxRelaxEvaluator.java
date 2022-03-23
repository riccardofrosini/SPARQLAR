package io.sparqlar.executors;

import io.sparqlar.executors.answers.AnswersContainer;
import io.sparqlar.rewriting.QueryCost;
import io.sparqlar.sparqlardc.CQSPARQL;
import org.apache.jena.query.Dataset;

import java.util.List;
import java.util.logging.Logger;

public class ApproxRelaxEvaluator extends Evaluator {
    private static final Logger logger = Logger.getLogger(ApproxRelaxEvaluator.class.getName());
    private final List<QueryCost<CQSPARQL>> queries;
    private SimpleEvaluator simpleEvaluator;
    private int index;

    public ApproxRelaxEvaluator(Dataset dataset, List<QueryCost<CQSPARQL>> queries) {
        super(dataset);
        this.queries = queries;
        this.index = 0;
        this.firstExecution = true;
        this.simpleEvaluator = new SimpleEvaluator(dataset, queries.get(index));
    }

    @Override
    public AnswersContainer getNextAnswers(int answers) {
        firstExecution();
        AnswersContainer nextAnswers = new AnswersContainer(queries.get(0).getQuery().copySelectVariables());
        if (moreAnswers) {
            do {
                if (!simpleEvaluator.hasMoreAnswers()) {
                    index++;
                    if (queries.size() == index) {
                        moreAnswers = false;
                        simpleEvaluator.close();
                        return nextAnswers;
                    }
                    simpleEvaluator.close();
                    simpleEvaluator = new SimpleEvaluator(dataset, queries.get(index));
                }
                nextAnswers = AnswersContainer.union(nextAnswers, simpleEvaluator.getNextAnswers(answers - nextAnswers.size()));
            } while (nextAnswers.size() < answers);
        } else {
            simpleEvaluator.close();
        }
        return nextAnswers;
    }

    @Override
    public AnswersContainer getRemainingAnswers() {
        long l = System.currentTimeMillis();
        firstExecution();
        AnswersContainer nextAnswers = new AnswersContainer(queries.get(0).getQuery().copySelectVariables());
        if (moreAnswers) {
            while (true) {
                if (!simpleEvaluator.hasMoreAnswers()) {
                    index++;
                    if (queries.size() == index) {
                        moreAnswers = false;
                        logger.info("Execution time: " + (System.currentTimeMillis() - l));
                        logger.info("Answers: " + nextAnswers.size());
                        simpleEvaluator.close();
                        return nextAnswers;
                    }
                    simpleEvaluator.close();
                    simpleEvaluator = new SimpleEvaluator(dataset, queries.get(index));
                }
                nextAnswers = AnswersContainer.union(nextAnswers, simpleEvaluator.getRemainingAnswers());
            }
        } else {
            simpleEvaluator.close();
        }
        logger.info("Execution time: " + (System.currentTimeMillis() - l));
        logger.info("Answers: " + nextAnswers.size());
        return nextAnswers;
    }

    protected void firstExecution() {
        if (firstExecution) {
            this.simpleEvaluator.firstExecution();
            this.moreAnswers = simpleEvaluator.hasMoreAnswers() || this.queries.size() > 1;
            this.firstExecution = false;
        }
    }
}
