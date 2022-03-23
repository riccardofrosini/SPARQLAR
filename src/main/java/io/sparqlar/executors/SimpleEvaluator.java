package io.sparqlar.executors;

import io.sparqlar.executors.answers.AnswerCost;
import io.sparqlar.executors.answers.AnswersContainer;
import io.sparqlar.rewriting.QueryCost;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ReadWrite;

import java.util.logging.Logger;

public class SimpleEvaluator extends Evaluator {

    private static final Logger logger = Logger.getLogger(SimpleEvaluator.class.getName());
    protected final QueryCost query;
    protected final QueryExecution queryExecution;

    public SimpleEvaluator(Dataset dataset, QueryCost query) {
        super(dataset);
        dataset.begin(ReadWrite.READ);
        this.queryExecution = QueryExecutionFactory.create(query.getQuery().toStringExecutable(), dataset);
        this.query = query;
        this.firstExecution = true;
    }

    @Override
    public AnswersContainer getNextAnswers(int answers) {
        firstExecution();
        AnswersContainer answersContainer = new AnswersContainer(query.getQuery().copySelectVariables());
        if (moreAnswers) {
            if (query.isAsk()) {
                if (queryExecution.execAsk()) answersContainer.add(new AnswerCost(query.getCost(), query.getQuery()));
                moreAnswers = false;
            } else if (query.isEmptyQuery()) {
                answersContainer.add(new AnswerCost(query.getCost(), query.getQuery()));
                moreAnswers = false;
            } else {
                for (int i = 0; i < answers && resultSet.hasNext(); ) {
                    if (answersContainer.add(new AnswerCost(resultSet.next(), query.getCost(), query.getQuery()))) {
                        i++;
                    }
                }
                if (!resultSet.hasNext()) {
                    moreAnswers = false;
                }
            }
        }
        return answersContainer;
    }

    @Override
    public AnswersContainer getRemainingAnswers() {
        long l = System.currentTimeMillis();
        firstExecution();
        AnswersContainer answersContainer = new AnswersContainer(query.getQuery().copySelectVariables());
        if (moreAnswers) {
            if (query.isAsk() && moreAnswers) {
                if (queryExecution.execAsk()) answersContainer.add(new AnswerCost(query.getCost(), query.getQuery()));
                moreAnswers = false;
            } else if (query.isEmptyQuery() && moreAnswers) {
                answersContainer.add(new AnswerCost(query.getCost(), query.getQuery()));
                moreAnswers = false;
            } else {
                while (resultSet.hasNext()) {
                    answersContainer.add(new AnswerCost(resultSet.next(), query.getCost(), query.getQuery()));
                }
                moreAnswers = false;
            }
        }
        logger.info("Execution time: " + (System.currentTimeMillis() - l));
        logger.info("Answers: " + answersContainer.size());
        return answersContainer;
    }

    protected void firstExecution() {
        if (firstExecution) {
            logger.info("Executing Query: ");
            logger.info(this.query.getQuery().toString());
            if (!query.isAsk()) {
                this.resultSet = queryExecution.execSelect();
                this.moreAnswers = resultSet.hasNext();
            } else {
                this.moreAnswers = true;
            }
            firstExecution = false;
        }
    }
}
