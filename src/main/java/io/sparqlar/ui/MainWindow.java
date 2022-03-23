package io.sparqlar.ui;

import io.sparqlar.executors.ApproxRelaxEvaluator;
import io.sparqlar.executors.CachedApproxRelaxEvaluator;
import io.sparqlar.executors.Evaluator;
import io.sparqlar.executors.SimpleEvaluator;
import io.sparqlar.optimisation.Graph;
import io.sparqlar.optimisation.GraphUtils;
import io.sparqlar.parser.ParseException;
import io.sparqlar.parser.SparqlParser;
import io.sparqlar.rewriting.*;
import io.sparqlar.rewriting.exceptions.CostException;
import io.sparqlar.rewriting.exceptions.OntologyException;
import io.sparqlar.rewriting.exceptions.OptimisationContainmentException;
import io.sparqlar.rewriting.exceptions.OptimisationSchemaException;
import io.sparqlar.sparqlardc.CQSPARQL;
import io.sparqlar.sparqlardc.SPARQL;
import io.sparqlar.sparqlardc.terms.URI;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb2.TDB2Factory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

public final class MainWindow extends JFrame {
    private static final Logger logger = Logger.getLogger(MainWindow.class.getName());
    private Ontology ontology;
    private Dataset dataset;
    private Graph schema;
    private HashSet<URI> noDeletion;
    private HashSet<URI> noSubstitution;


    private JTextArea queryPanel;
    private JButton showRewrittenQueryButton;
    private JButton loadDatasetButton;
    private JButton executeQueryButton;
    private JButton loadOntologyButton;
    private JButton executeRelaxApproxButton;
    private JTextField delCost;
    private JTextField insCost;
    private JTextField subCost;
    private JTextField subPCost;
    private JTextField subCCost;
    private JTextField domCost;
    private JTextField rangeCost;
    private JCheckBox delCheckBox;
    private JCheckBox insCheckBox;
    private JCheckBox subsCheckBox;
    private JCheckBox subPCheckBox;
    private JCheckBox subCCheckBox;
    private JCheckBox domCheckBox;
    private JCheckBox rangeCheckBox;
    private JButton choosePredicatesDelButton;
    private JButton choosePredicatesSubsButton;
    private JTextField maxCost;
    private JTextField ansPerScreen;
    private JCheckBox cacheCheckBox;
    private JCheckBox containmentCheckBox;
    private JLabel dataSetPath;
    private JLabel ontologyPath;
    private JPanel mainWindow;
    private JButton loadSchemaButton;
    private JLabel schemaPath;
    private JCheckBox schemaCheckBox;
    private JButton allAnswersRelaxApproxButton;

    public MainWindow() {
        super("Flexible Querying - SPARQLAR");
        setContentPane(mainWindow);
        showRewrittenQueryButton.addActionListener(e -> displayQueries());
        choosePredicatesDelButton.addActionListener(e ->
                choosePredicatesDelButtonActionPerformed(noDeletion, true));
        choosePredicatesSubsButton.addActionListener(e ->
                choosePredicatesDelButtonActionPerformed(noSubstitution, false));
        executeQueryButton.addActionListener(e -> displayAnswers(true, false));
        executeRelaxApproxButton.addActionListener(e -> displayAnswers(false, false));
        loadOntologyButton.addActionListener(e -> loadOntology());
        loadDatasetButton.addActionListener(e -> loadDataset());
        loadSchemaButton.addActionListener(e -> loadSchema());
        allAnswersRelaxApproxButton.addActionListener(e -> displayAnswers(false, true));
        noDeletion = new HashSet<>();
        noSubstitution = new HashSet<>();
        pack();
        setVisible(true);
    }

    private void loadSchema() {
        JFileChooser choose = new JFileChooser(System.getProperty("user.home"));
        choose.showOpenDialog(this);
        File selectedFile = choose.getSelectedFile();
        if (selectedFile != null) {
            try {
                schema = GraphUtils.loadGraphFromFile(selectedFile);
                schemaPath.setText(selectedFile.toString());
                schemaCheckBox.setSelected(true);
            } catch (Exception e) {
                handleExceptionAndDisplayMessage(e);
            }
        }
    }

    private void displayQueries() {
        try {
            new QueryList(this, false, getRewrittenQuery());
        } catch (Exception ex) {
            handleExceptionAndDisplayMessage(ex);
        }
    }

    private void displayAnswers(boolean exact, boolean allAnswers) {
        Evaluator evaluator = null;
        try {
            evaluator = getEvaluator(exact);
            new AnswersForm(this, true, evaluator, allAnswers, Integer.parseInt(ansPerScreen.getText()));
        } catch (Exception ex) {
            handleExceptionAndDisplayMessage(ex);
        }
        if (evaluator != null) {
            evaluator.close();
        }
    }

    private void handleExceptionAndDisplayMessage(Exception e) {
        if (e instanceof OntologyException) {
            JOptionPane.showMessageDialog(this, e, "Ontology Error", JOptionPane.ERROR_MESSAGE);
        } else if (e instanceof CostException) {
            JOptionPane.showMessageDialog(this, e, "Cost Error", JOptionPane.ERROR_MESSAGE);
        } else if (e instanceof ParseException || e instanceof NumberFormatException) {
            JOptionPane.showMessageDialog(this, e, "Parsing Error", JOptionPane.ERROR_MESSAGE);
        } else if (e instanceof OptimisationSchemaException) {
            JOptionPane.showMessageDialog(this, e, "Schema optimisation Error", JOptionPane.ERROR_MESSAGE);
        } else if (e instanceof OptimisationContainmentException) {
            JOptionPane.showMessageDialog(this, e, "Containment optimisation Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, e, "Generic Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void loadDataset() {
        JFileChooser choose = new JFileChooser(System.getProperty("user.home"));
        choose.showOpenDialog(this);
        if (choose.getSelectedFile() == null) {
            return;
        }
        try {
            dataset = TDB2Factory.connectDataset(choose.getSelectedFile().getParent());
        } catch (Exception ex) {
            File selectedFile = choose.getSelectedFile().getParentFile();
            Model read = ModelFactory.createDefaultModel();
            for (File f : selectedFile.listFiles()) {
                logger.info(f.toString());
                read.add(read.read(f.toURI().toString()));
            }
            dataset = DatasetFactory.create(read);
        }
        dataSetPath.setText(choose.getSelectedFile().getParent());
    }

    private void loadOntology() {
        JFileChooser choose = new JFileChooser(System.getProperty("user.home"));
        choose.showOpenDialog(this);
        if (choose.getSelectedFile() == null) {
            return;
        }
        String path = choose.getSelectedFile().toURI().toString();
        ontology = new Ontology(path);
        ontologyPath.setText(choose.getSelectedFile().toString());
    }

    private Evaluator getEvaluator(boolean exact) throws ParseException, CostException, OntologyException, OptimisationSchemaException, OptimisationContainmentException {
        if (exact) {
            SPARQL<?> query = SparqlParser.parse(queryPanel.getText());
            return new SimpleEvaluator(dataset, new QueryCost(query, 0));
        }
        List<QueryCost<CQSPARQL>> rewrittenQuery = getRewrittenQuery();
        if (cacheCheckBox.isSelected()) {
            return new CachedApproxRelaxEvaluator(dataset, rewrittenQuery);
        }
        return new ApproxRelaxEvaluator(dataset, rewrittenQuery);
    }

    private void choosePredicatesDelButtonActionPerformed(HashSet<URI> predicates, boolean del) {
        try {
            new ChoosePredicates(this, true, SparqlParser.parse(queryPanel.getText()).getApproximatedURIs(), predicates, del);
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this, ex, "Parsing Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<QueryCost<CQSPARQL>> getRewrittenQuery() throws ParseException, CostException, OntologyException, OptimisationSchemaException, OptimisationContainmentException {
        SPARQL<?> parse = SparqlParser.parse(queryPanel.getText());
        float Mc = Float.parseFloat(this.maxCost.getText());
        if (Mc < 0) {
            throw CostException.buildCostException(true);
        }
        if (schema == null || !schemaCheckBox.isSelected()) {
            if (schemaCheckBox.isSelected()) {
                throw new OptimisationSchemaException("Schema is missing. Uncheck schema optimisation.");
            }
            return RewritingAlgorithm.rewrite(parse, Mc, loadApproximating(), loadRelaxing(), containmentCheckBox.isSelected());
        } else {
            return RewritingAlgorithm.rewrite(parse, Mc, loadApproximating(), loadRelaxing(), schema, containmentCheckBox.isSelected());
        }
    }

    private Relaxing loadRelaxing() throws CostException, OntologyException {
        float Mc = Float.parseFloat(maxCost.getText());
        float spc = (subPCheckBox.isSelected()) ? Float.parseFloat(subPCost.getText()) : Mc + 1;
        float scc = (subCCheckBox.isSelected()) ? Float.parseFloat(subCCost.getText()) : Mc + 1;
        float domc = (domCheckBox.isSelected()) ? Float.parseFloat(domCost.getText()) : Mc + 1;
        float rangec = (rangeCheckBox.isSelected()) ? Float.parseFloat(rangeCost.getText()) : Mc + 1;
        return new Relaxing(spc, scc, domc, rangec, ontology);
    }

    private Approximating loadApproximating() throws CostException {
        float Mc = Float.parseFloat(maxCost.getText());
        float dc = (delCheckBox.isSelected()) ? Float.parseFloat(delCost.getText()) : Mc + 1;
        float ic = (insCheckBox.isSelected()) ? Float.parseFloat(insCost.getText()) : Mc + 1;
        float sc = (subsCheckBox.isSelected()) ? Float.parseFloat(subCost.getText()) : Mc + 1;
        return new Approximating(dc, ic, sc, noSubstitution, noDeletion);

    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainWindow = new JPanel();
        mainWindow.setLayout(new GridBagLayout());
        mainWindow.setMinimumSize(new Dimension(500, 500));
        mainWindow.setPreferredSize(new Dimension(1000, 500));
        queryPanel = new JTextArea();
        queryPanel.setBackground(new Color(-1));
        queryPanel.setForeground(new Color(-16777216));
        queryPanel.setText("prefix  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \nprefix  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n\nSelect * \n    where{\n        approx(?x rdf:type ?y) .\n \t\t?z rdfs:subClassOf ?y \t\n    }");
        queryPanel.putClientProperty("html.disable", Boolean.TRUE);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.gridheight = 9;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainWindow.add(queryPanel, gbc);
        showRewrittenQueryButton = new JButton();
        showRewrittenQueryButton.setText("Show Rewritten Query");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 9;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(showRewrittenQueryButton, gbc);
        loadDatasetButton = new JButton();
        loadDatasetButton.setText("Load Dataset");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 10;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(loadDatasetButton, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("Approximation:");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(label1, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Relaxation:");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 4;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(label2, gbc);
        delCost = new JTextField();
        delCost.setText("1");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainWindow.add(delCost, gbc);
        insCost = new JTextField();
        insCost.setText("1");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainWindow.add(insCost, gbc);
        subCost = new JTextField();
        subCost.setText("1");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainWindow.add(subCost, gbc);
        subPCost = new JTextField();
        subPCost.setText("1");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainWindow.add(subPCost, gbc);
        subCCost = new JTextField();
        subCCost.setText("1");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 6;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainWindow.add(subCCost, gbc);
        domCost = new JTextField();
        domCost.setText("1");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 7;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainWindow.add(domCost, gbc);
        rangeCost = new JTextField();
        rangeCost.setText("1");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 8;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainWindow.add(rangeCost, gbc);
        delCheckBox = new JCheckBox();
        delCheckBox.setSelected(true);
        delCheckBox.setText("Del");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(delCheckBox, gbc);
        insCheckBox = new JCheckBox();
        insCheckBox.setSelected(true);
        insCheckBox.setText("Ins");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(insCheckBox, gbc);
        subsCheckBox = new JCheckBox();
        subsCheckBox.setSelected(true);
        subsCheckBox.setText("Subs");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(subsCheckBox, gbc);
        subPCheckBox = new JCheckBox();
        subPCheckBox.setSelected(true);
        subPCheckBox.setText("SubP");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(subPCheckBox, gbc);
        subCCheckBox = new JCheckBox();
        subCCheckBox.setSelected(true);
        subCCheckBox.setText("SubC");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 6;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(subCCheckBox, gbc);
        domCheckBox = new JCheckBox();
        domCheckBox.setSelected(true);
        domCheckBox.setText("Dom");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 7;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(domCheckBox, gbc);
        rangeCheckBox = new JCheckBox();
        rangeCheckBox.setSelected(true);
        rangeCheckBox.setText("Range");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 8;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(rangeCheckBox, gbc);
        choosePredicatesDelButton = new JButton();
        choosePredicatesDelButton.setText("Choose Predicates");
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(choosePredicatesDelButton, gbc);
        choosePredicatesSubsButton = new JButton();
        choosePredicatesSubsButton.setText("Choose Predicates");
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(choosePredicatesSubsButton, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Maximum Cost:");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 9;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(label3, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("Answers per screen:");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 10;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(label4, gbc);
        maxCost = new JTextField();
        maxCost.setText("1");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 9;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainWindow.add(maxCost, gbc);
        ansPerScreen = new JTextField();
        ansPerScreen.setText("100");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 10;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainWindow.add(ansPerScreen, gbc);
        cacheCheckBox = new JCheckBox();
        cacheCheckBox.setText("Cache");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 9;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(cacheCheckBox, gbc);
        containmentCheckBox = new JCheckBox();
        containmentCheckBox.setText("Containment");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 9;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(containmentCheckBox, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("Dataset Path: ");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 10;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(label5, gbc);
        executeRelaxApproxButton = new JButton();
        executeRelaxApproxButton.setText("Execute Relax/Approx");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 11;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(executeRelaxApproxButton, gbc);
        executeQueryButton = new JButton();
        executeQueryButton.setText("Execute Query");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 11;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(executeQueryButton, gbc);
        loadOntologyButton = new JButton();
        loadOntologyButton.setText("Load Ontology");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 11;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(loadOntologyButton, gbc);
        final JLabel label6 = new JLabel();
        label6.setText("Ontology Path:");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 11;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(label6, gbc);
        dataSetPath = new JLabel();
        dataSetPath.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 10;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(dataSetPath, gbc);
        ontologyPath = new JLabel();
        ontologyPath.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 11;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(ontologyPath, gbc);
        loadSchemaButton = new JButton();
        loadSchemaButton.setText("Load Schema");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 12;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(loadSchemaButton, gbc);
        final JLabel label7 = new JLabel();
        label7.setText("Schema Path:");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 12;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(label7, gbc);
        schemaPath = new JLabel();
        schemaPath.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 12;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(schemaPath, gbc);
        schemaCheckBox = new JCheckBox();
        schemaCheckBox.setText("Schema");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 9;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(schemaCheckBox, gbc);
        allAnswersRelaxApproxButton = new JButton();
        allAnswersRelaxApproxButton.setText("All Answers Relax/Approx");
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 11;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainWindow.add(allAnswersRelaxApproxButton, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainWindow;
    }
}
