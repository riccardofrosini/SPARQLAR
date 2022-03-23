package io.sparqlar.executors;

import io.sparqlar.executors.answers.AnswersContainer;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ResultSet;

/**
 * @author riccardo
 */
public abstract class Evaluator {

    protected final Dataset dataset;
    protected ResultSet resultSet;
    protected boolean moreAnswers;
    protected boolean firstExecution;

    public Evaluator(Dataset dataset) {
        this.dataset = dataset;
    }

    public abstract AnswersContainer getNextAnswers(int answers);

    public abstract AnswersContainer getRemainingAnswers();

    public boolean hasMoreAnswers() {
        return moreAnswers;
    }

    public void close() {
        dataset.end();
    }

    protected abstract void firstExecution();
}
