/*
 * 
 * Author : Christopher Henard (christopher.henard@uni.lu)
 * Date : 01/11/2012
 * Copyright 2012 University of Luxembourg – Interdisciplinary Centre for Security Reliability and Trust (SnT)
 * All rights reserved
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pledge.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.StringTokenizer;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.Solver;
import org.sat4j.minisat.orders.RandomLiteralSelectionStrategy;
import org.sat4j.minisat.orders.RandomWalkDecorator;
import org.sat4j.minisat.orders.VarOrderHeap;
import org.sat4j.reader.DimacsReader;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ModelIterator;
import pledge.core.techniques.generation.EvolutionaryAlgorithm1Plus1;
import pledge.core.techniques.generation.GenerationTechnique;
import pledge.core.techniques.prioritization.PrioritizationTechnique;
import pledge.core.techniques.prioritization.SimilarityGreedy;
import pledge.core.techniques.prioritization.SimilarityNearOptimal;
import splar.core.constraints.CNFClause;
import splar.core.constraints.CNFFormula;
import splar.core.fm.FeatureModel;
import splar.core.fm.XMLFeatureModel;
import splar.plugins.reasoners.sat.sat4j.FMReasoningWithSAT;
import splar.plugins.reasoners.sat.sat4j.ReasoningWithSAT;

/**
 * This class represents the model of the application. It contains the methods 
 * to perform the business logic of the application: load a feature model,
 * perform the products' prioritization, etc.
 * @author Christopher Henard
 */
public class ModelPLEDGE extends Observable {

    private static final int SAT_TIMEOUT = 1000;
    private static final int ITERATOR_TIMEOUT = 150000;
    private static final String solverName = "MiniSAT";
    public static final String OR = "   OR   ";
    public static final String NOT = "! ";
    private static final IOrder order = new RandomWalkDecorator(new VarOrderHeap(new RandomLiteralSelectionStrategy()), 1);
    private static final String GLOBAL_ACTION_LOAD_FM = "Loading the Feature Model";
    private static final String GLOBAL_ACTION_LOAD_PRODUCTS = "Loading Products";
    private static final String GLOBAL_ACTION_GENERATING_PRODUCTS = "Generating products";
    private static final String GLOBAL_ACTION_PRIORITIZING_PRODUCTS = "Prioritizing products";
    private static final String GLOBAL_ACTION_COVERAGE = "Computing the coverage";
    private static final String CURRENT_ACTION_LOAD_CONSTRAINTS = "Loading the constraints...";
    private static final String CURRENT_ACTION_EXTRACT_FEATURES = "Extracting the features...";
    private static final String CURRENT_ACTION_EXTRACT_CONSTRAINTS = "Extracting the constraints...";
    private static final String CURRENT_ACTION_FINDING_CORE_DEAD_FEATURES = "Finding core and dead features...";
    private static final String CURRENT_ACTION_MODEL_PAIRS = "Computing the valid pairs of the model...";
    private static final String CURRENT_ACTION_PRODUCT_PAIRS = "Computing the pairs covered by the products...";
    private static final String CORE_FEATURE = "Core";
    private static final String DEAD_FEATURE = "Dead";
    private static final String FREE_FEATURE = "Free";

    public static enum FeatureModelFormat {

        SPLOT, DIMACS
    };
    private Solver solver;
    private ISolver solverIterator;
    private List<Integer> featuresIntList;
    private List<String> featuresList;
    private Map<String, Integer> namesToFeaturesInt;
    private List<String> featureModelConstraints;
    private List<String> featureModelConstraintsString;
    private FeatureModelFormat featureModelFormat;
    private String featureModelName;
    private boolean running, indeterminate;
    private String globalAction, currentAction;
    private List<String> coreFeatures, deadFeatures;
    private int progress;
    private List<Product> products;
    private List<GenerationTechnique> generationTechniques;
    private GenerationTechnique generationTechnique;
    private List<PrioritizationTechnique> prioritizationTechniques;
    private PrioritizationTechnique prioritizationTechnique;
    private long generationTimeMSAllowed = 60000;
    private int nbProductsToGenerate = 10;
    private String fmPath;
    private int currentConstraint = -1;

    /**
     * Creates the model of the application.
     */
    public ModelPLEDGE() {
        solver = null;
        solverIterator = null;
        featuresIntList = new ArrayList<Integer>();
        featuresList = new ArrayList<String>();
        namesToFeaturesInt = new HashMap<String, Integer>();
        featureModelConstraints = new ArrayList<String>();
        featureModelConstraintsString = new ArrayList<String>();
        coreFeatures = new ArrayList<String>();
        deadFeatures = new ArrayList<String>();
        products = null;
        running = false;
        indeterminate = true;
        progress = 0;
        generationTechniques = new ArrayList<GenerationTechnique>();
        generationTechniques.add(new EvolutionaryAlgorithm1Plus1());
        generationTechnique = generationTechniques.get(0);
        prioritizationTechniques = new ArrayList<PrioritizationTechnique>();
        prioritizationTechniques.add(new SimilarityGreedy());
        prioritizationTechniques.add(new SimilarityNearOptimal());
        prioritizationTechnique = prioritizationTechniques.get(0);
    }

    /**
     * Returns the format of the currently loaded feature model.
     * @return the format of the current feature model.
     */
    public FeatureModelFormat getFeatureModelFormat() {
        return featureModelFormat;
    }

    /**
     * Returns the name of the currently loaded feature model.
     * @return the name of the currently loaded feature model.
     */
    public String getFeatureModelName() {
        return featureModelName;
    }

    /**
     * Returns an indices list of the features.
     * @return a list containing the indices of the feature model's features.
     */
    public List<Integer> getFeaturesIntList() {
        return featuresIntList;
    }

    /**
     * returns the features' list of the feature model.
     * @return a list of features of the feature model.
     */
    public List<String> getFeaturesList() {
        return featuresList;
    }

    /**
     * Returns a mapping between each feature and its corresponding index.
     * @return a map containing the index of each feature.
     */
    public Map<String, Integer> getNamesToFeaturesInt() {
        return namesToFeaturesInt;
    }

    /**
     * Returns the constraints of the feature model.
     * @return a list containing the constraints of the feature model.
     */
    public List<String> getFeatureModelConstraints() {
        return featureModelConstraints;
    }

    /**
     * Returns the constraints of the feature model.
     * @return a list containing a String represent of the constraints of the feature model.
     */
    public List<String> getFeatureModelConstraintsString() {
        return featureModelConstraintsString;
    }

    /**
     * Returns the SAT solver.
     * @return the SAT solver asosciated to this model.
     */
    public Solver getSolver() {
        return solver;
    }

    /**
     * Checks if the application is running.
     * @return true if the program is running.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Returns the list of products currently loaded into the application.
     * @return the generated or priotized products.
     */
    public List<Product> getProducts() {
        return products;
    }

    /**
     * Returns the amount of time allowed for generating products.
     * @return the amount of time in seconds allowed for generating products.
     */
    public long getGenerationTimeMSAllowed() {
        return generationTimeMSAllowed;
    }

    /**
     * Specifies the amount of time allowed for generating products.
     * @param generationTimeMSAllowed the amount of time in seconds allowed for generating products.
     */
    public void setGenerationTimeMSAllowed(long generationTimeMSAllowed) {
        this.generationTimeMSAllowed = generationTimeMSAllowed;
        setChanged();
        notifyObservers();
    }

    /**
     * Returns the number of products to generate.
     * @return an integer representing the number of products to generate.
     */
    public int getNbProductsToGenerate() {
        return nbProductsToGenerate;
    }

    /**
     * Specifies the number of products to generate.
     * @param nbProductsToGenerate the number of products to generate.
     */
    public void setNbProductsToGenerate(int nbProductsToGenerate) {
        this.nbProductsToGenerate = nbProductsToGenerate;
        setChanged();
        notifyObservers();
    }

    /**
     * Specifies whether the application is running or not.
     * @param running a boolean indicating whether the application is running or not.
     */
    public void setRunning(boolean running) {
        this.running = running;
        if (!running) {
            indeterminate = true;
        }
        progress = 0;
        setChanged();
        notifyObservers();
    }

    /**
     * Specifies whether the duration of current action performed by the tool is indeterminate or not.
     * @return true if the duration of current action performed by the tool is indeterminate.
     */
    public boolean isIndeterminate() {
        return indeterminate;
    }

    /**
     * Return the progress of the current action performed by the tool (percentage)
     * @return an integer representing the percentage of the current action performed.
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Specifies the progress of the current action performed by the tool (percentage)
     * @param progress an integer representing the percentage of the current action performed.
     */
    public void setProgress(int progress) {
        this.progress = progress;
        setChanged();
        notifyObservers();
    }

    /**
     * Specifies wether the duration of current action performed by the tool is indeterminate or not.
     * @param indeterminate a boolean specifying wether the duration of current action performed by the tool is indeterminate or not.
     */
    public void setIndeterminate(boolean indeterminate) {
        this.indeterminate = indeterminate;
        setChanged();
        notifyObservers();
    }

    /**
     * Returns the solver iterator that is used to generate valid products.
     * @return the solver iterator that is used to generate valid products.
     */
    public ISolver getSolverIterator() {
        return solverIterator;
    }

    /**
     * Returns the current action performed by the tool.
     * @return a String representing the current action performed by the tool.
     */
    public String getCurrentAction() {
        return currentAction;
    }

    /**
     * Specifies the current action performed by the tool.
     * @param currentAction a String representing the action which is currently performed by the tool.
     */
    public void setCurrentAction(String currentAction) {
        this.currentAction = currentAction;
        setChanged();
        notifyObservers();
    }

    /**
     * Returns the current global action performed by the tool.
     * @return a String representing the current global action performed by the tool.
     */
    public String getGlobalAction() {
        return globalAction;
    }

    /**
     * Specifies the current global action performed by the tool.
     * @param globalAction a String representing the global action which is currently performed by the tool.
     */
    public void setGlobalAction(String globalAction) {
        this.globalAction = globalAction;
        setChanged();
        notifyObservers();
    }

    private void clean() {
        featuresIntList.clear();
        featuresList.clear();
        namesToFeaturesInt.clear();
        featureModelConstraints.clear();
        featureModelConstraintsString.clear();
        coreFeatures.clear();
        deadFeatures.clear();
        setChanged();
        notifyObservers();
    }

    /**
     * Returns the core features of the feature model.
     * @return the list of mandatory features of the feature model.
     */
    public List<String> getCoreFeatures() {
        return coreFeatures;
    }

    /**
     * Returns the dead features of the feature model.
     * @return the list of dead features of the feature model.
     */
    public List<String> getDeadFeatures() {
        return deadFeatures;
    }

    /**
     * Returns the specified generation technique.
     * @return the technique used by the tool to generate products.
     */
    public GenerationTechnique getGenerationTechnique() {
        return generationTechnique;
    }

    /**
     * Specifies the generation technique used by the tool.
     * @param name the name of the generation technique that has to be used to generate products.
     */
    public void SetGenerationTechniqueByName(String name) {
        for (GenerationTechnique gt : generationTechniques) {
            if (gt.getName().equals(name)) {
                generationTechnique = gt;
                break;
            }
        }

    }

    /**
     * Returns the generation techniques available.
     * @return a list containing the available generation techniques.
     */
    public List<GenerationTechnique> getGenerationTechniques() {
        return generationTechniques;
    }

    /**
     * Returns the specified prioritization technique.
     * @return the technique used by the tool to prioritize products.
     */
    public PrioritizationTechnique getPrioritizationTechnique() {
        return prioritizationTechnique;
    }

    /**
     * Specifies the prioritization technique used by the tool.
     * @param name the name of the prioritization technique that has to be used to prioritize    products.
     */
    public void SetPrioritizationTechniqueByName(String name) {
        for (PrioritizationTechnique pt : prioritizationTechniques) {
            if (pt.getName().equals(name)) {
                prioritizationTechnique = pt;
                break;
            }
        }

    }

    /**
     * Returns the prioritization techniques available.
     * @return a list containing the available prioritization techniques.
     */
    public List<PrioritizationTechnique> getPrioritizationTechniques() {
        return prioritizationTechniques;
    }

    /**
     * Load a feature model.
     * @param filePath the path to the feature model file.
     * @param format the format of the feature model.
     * @throws Exception if the file format is incorrect.
     */
    public void loadFeatureModel(String filePath, FeatureModelFormat format) throws Exception {
        setRunning(true);
        setIndeterminate(true);
        setGlobalAction(GLOBAL_ACTION_LOAD_FM);
        setCurrentAction(CURRENT_ACTION_LOAD_CONSTRAINTS);
        featureModelFormat = format;
        clean();
        featureModelName = new File(filePath).getName();
        featureModelName = featureModelName.substring(0, featureModelName.lastIndexOf("."));
        products = null;
        fmPath = filePath;
        switch (format) {

            case SPLOT:
                FeatureModel fm = new XMLFeatureModel(filePath, XMLFeatureModel.USE_VARIABLE_NAME_AS_ID);
                fm.loadModel();
                ReasoningWithSAT reasonerSAT = new FMReasoningWithSAT(solverName, fm, SAT_TIMEOUT);
                reasonerSAT.init();
                solver = (Solver) reasonerSAT.getSolver();
                String[] features = reasonerSAT.getVarIndex2NameMap();
                for (int i = 0; i < features.length; i++) {
                    String featureName = features[i];
                    featuresList.add(featureName);
                    int n = i + 1;
                    featuresIntList.add(n);
                    namesToFeaturesInt.put(featureName, n);
                }

                break;
            case DIMACS:
                ISolver dimacsSolver = SolverFactory.instance().createSolverByName(solverName);
                DimacsReader dr = new DimacsReader(dimacsSolver);
                dr.parseInstance(new FileReader(filePath));
                solver = (Solver) dimacsSolver;
                BufferedReader in = new BufferedReader(new FileReader(filePath));
                String line;
                int n = 0;
                while ((line = in.readLine()) != null && line.startsWith("c")) {
                    StringTokenizer st = new StringTokenizer(line.trim(), " ");
                    st.nextToken();
                    n++;
                    String sFeature = st.nextToken().replace('$', ' ').trim();
                    int feature = Integer.parseInt(sFeature);
                    if (n != feature) {
                        throw new Exception("Incorrect dimacs file, missing feature number " + n + " ?");
                    }
                    String featureName = st.nextToken();
                    featuresIntList.add(feature);
                    featuresList.add(featureName);
                    namesToFeaturesInt.put(featureName, feature);
                }
                in.close();
                break;
        }

        setCurrentAction(CURRENT_ACTION_EXTRACT_FEATURES);
        setIndeterminate(false);
        setProgress(0);
        int n = 1;
        int featuresCount = featuresIntList.size();
        while (n <= featuresCount) {
            featuresIntList.add(-n);
            n++;
            setProgress((int) (n / (double) featuresCount * 100));
        }

        if (solver != null) {
            solver.setTimeout(SAT_TIMEOUT);
        }


        solver.setOrder(order);
        solverIterator = new ModelIterator(solver);
        solverIterator.setTimeoutMs(ITERATOR_TIMEOUT);


        setCurrentAction(CURRENT_ACTION_EXTRACT_CONSTRAINTS);
        setProgress(0);
        int nConstraints = 0;
        switch (format) {

            case SPLOT:
                setIndeterminate(true);
                FeatureModel fm = new XMLFeatureModel(filePath, XMLFeatureModel.USE_VARIABLE_NAME_AS_ID);
                fm.loadModel();
                ReasoningWithSAT reasonerSAT = new FMReasoningWithSAT(solverName, fm, SAT_TIMEOUT);
                reasonerSAT.init();
                CNFFormula formula = fm.FM2CNF();
                nConstraints = formula.getClauses().size();
                setIndeterminate(false);
                int j = 0;

                for (CNFClause clause : formula.getClauses()) {

                    String cons = "";


                    for (int i = 0; i < clause.getLiterals().size(); i++) {
                        int signal = clause.getLiterals().get(i).isPositive() ? 1 : -1;
                        int varID = reasonerSAT.getVariableIndex(clause.getLiterals().get(i).getVariable().getID());

                        String f = featuresList.get(varID - 1);
                        if (signal < 0) {
                            f = NOT + f;
                        }

                        if (cons.equals("")) {
                            cons += f;
                        } else {
                            cons += OR + f;
                        }

                    }
                    featureModelConstraints.add(cons);
                    featureModelConstraintsString.add(cons);
                    setProgress((int) ((j + 1) / (double) nConstraints * 100));
                    j++;
                }

                break;
            case DIMACS:

                BufferedReader in = new BufferedReader(new FileReader(filePath));
                String line;

                while ((line = in.readLine()) != null) {
                    if (line.startsWith("p")) {
                        StringTokenizer st = new StringTokenizer(line.trim(), " ");
                        st.nextToken();
                        st.nextToken();
                        st.nextToken();
                        nConstraints = Integer.parseInt(st.nextToken());
                        break;

                    }
                }
                in.close();

                int i = 0;
                in = new BufferedReader(new FileReader(filePath));
                while ((line = in.readLine()) != null) {
                    if (!line.startsWith("c") && !line.startsWith("p")) {
                        String cons = "";
                        StringTokenizer st = new StringTokenizer(line.trim(), " ");

                        while (st.hasMoreTokens()) {
                            int f = Integer.parseInt(st.nextToken());

                            if (f != 0) {
                                if (cons.equals("")) {
                                    if (f > 0) {
                                        cons += featuresList.get((f - 1));

                                    } else {
                                        cons += NOT + featuresList.get((-f) - 1);
                                    }
                                } else {
                                    cons += OR;
                                    if (f > 0) {
                                        cons += featuresList.get((f - 1));

                                    } else {
                                        cons += NOT + featuresList.get((-f) - 1);
                                    }
                                }
                            }

                        }
                        featureModelConstraints.add(cons);
                        featureModelConstraintsString.add(cons);
                        setProgress((int) ((i + 1) / (double) nConstraints * 100));
                        i++;
                    }
                }
                in.close();

                break;
        }


//        for (int i = 0; i < nConstraints; i++) {
//            IConstr constraint = solver.getIthConstr(i);
//            if (constraint != null) {
//                featureModelConstraints.add(constraint);
//                StringBuilder stringConstraint = new StringBuilder();
//                int size = constraint.size();
//                for (int j = 0; j < size; j++) {
//                    boolean negative;
//                    int literal = constraint.get(j) / 2;
//                    negative = constraint.get(j) % 2 == 1;
//                    String feature = featuresList.get(literal - 1);
//                    if (negative) {
//                        stringConstraint.append(NOT);
//                    }
//                    stringConstraint.append(feature);
//                    if (j < size - 1) {
//                        stringConstraint.append(OR);
//                    }
//                    
//                }
//                featureModelConstraintsString.add(stringConstraint.toString());
//            }
//            
//            setProgress((int) ((i + 1) / (double) nConstraints * 100));
//        }

        setCurrentAction(CURRENT_ACTION_FINDING_CORE_DEAD_FEATURES);
        setProgress(0);
        n = 0;
        IVecInt vector = new VecInt();
        // Core and dead features
        for (String feature : featuresList) {
            int f = namesToFeaturesInt.get(feature);
            vector.clear();
            vector.push(-f);
            if (!solver.isSatisfiable(vector)) {
                coreFeatures.add(feature);
            }

            vector.clear();
            vector.push(f);
            if (!solver.isSatisfiable(vector)) {
                deadFeatures.add(feature);
            }
            n++;
            setProgress((int) ((n) / (double) featuresCount * 100));
        }

        setRunning(false);
        setChanged();
        notifyObservers(featureModelConstraints);
    }
    
    public void removeConstraint(int i){
        featureModelConstraintsString.remove(i);
        setChanged();
        notifyObservers(featureModelConstraints);
    }

    /**
     * Generate products.
     * @throws Exception if an error occurs during the generation.
     */
    public void generateProducts() throws Exception {
        setRunning(true);
        setIndeterminate(false);
        setGlobalAction(GLOBAL_ACTION_GENERATING_PRODUCTS);
        products = generationTechnique.generateProducts(this, nbProductsToGenerate, generationTimeMSAllowed, prioritizationTechnique);
        setRunning(false);
        setChanged();
        notifyObservers();
    }

    /**
     * Prioritize products.
     * @throws Exception if an error occur while prioritizing the products.
     */
    public void prioritizeProducts() throws Exception {
        setRunning(true);
        setIndeterminate(false);
        setGlobalAction(GLOBAL_ACTION_PRIORITIZING_PRODUCTS);
        products = prioritizationTechnique.prioritize(this, products);
        setRunning(false);
        setChanged();
        notifyObservers();
    }

    /**
     * Compute the valid pairs of the FM.
    
     */
    private Set<TSet> computeValidPairs() throws TimeoutException {
        Set<TSet> pairs = new HashSet<TSet>();

        List<Integer> extendedFeatures = new ArrayList<Integer>(featuresIntList.size() * 2);

        for (Integer i : featuresIntList) {
            extendedFeatures.add(i);
            extendedFeatures.add(-i);
        }

        int size = extendedFeatures.size();

        Util.nCk(size, 2, pairs, extendedFeatures, true, solver);

        return pairs;
    }

    /**
     * Compute the pairwise coverage of the products.
     * @return the pairwise coverage of the products.
     */
    public String getPairwiseCoverage() throws TimeoutException {
        setRunning(true);
        setIndeterminate(false);
        setGlobalAction(GLOBAL_ACTION_COVERAGE);
        setCurrentAction(CURRENT_ACTION_PRODUCT_PAIRS);

        Set<TSet> productsPairs = new HashSet<TSet>();

        int i = 0;
        for (Product p : products) {
            setCurrentAction(CURRENT_ACTION_PRODUCT_PAIRS + " product " + i);
            productsPairs.addAll(p.getCoveredPairs());
            setProgress((int) (((double) i / (double) products.size()) * 100.0));
            i++;
        }

        int d1 = productsPairs.size();
        int d2 =  0;
        double cov = 0;
        if (solver != null) {

            setIndeterminate(true);
            setCurrentAction(CURRENT_ACTION_MODEL_PAIRS);
            d2 = computeValidPairs().size();


            cov = (double) d1 / d2 * 100.0;
        }
        else
            cov = d1;
        setRunning(false);
        if (solver != null)
        return "Number of valid pairs of the model: " + d2 + "\nNumber of pairs covered by the products: " + d1 + "\n\nCoverage: " + new DecimalFormat("#.##").format(cov) + "%";
        else
            return "Number of pairs covered by the products: " + d1 ;
    }

    /**
     * Return the type of a given feature (i.e. core, dead or free)
     * @param feature the name of the feature.
     * @return a String representing the type of this feature (core, dead or free).
     */
    public String getFeatureType(String feature) {
        if (coreFeatures.contains(feature)) {
            return CORE_FEATURE;
        } else if (deadFeatures.contains(feature)) {
            return DEAD_FEATURE;
        } else {
            return FREE_FEATURE;
        }
    }

    private Product toProduct(int[] vector) {

        Product product = new Product();
        for (int i : vector) {
            product.add(i);
        }
        return product;
    }

    /**
     * returns n products obtained at random from the solver.
     * @param count the number of products to get.
     * @return the list of products obtained at random for the solver.
     */
    public List<Product> getUnpredictableProducts(int count) {
        List<Product> products = new ArrayList<Product>(count);

        while (products.size() < count) {

            try {
                if (solverIterator.isSatisfiable()) {
                    Product product = toProduct(solverIterator.model());

                    if (!products.contains(product)) {
                        products.add(product);
                    }

                } else {
                    switch (featureModelFormat) {

                        case SPLOT:
                            FeatureModel fm = new XMLFeatureModel(fmPath, XMLFeatureModel.USE_VARIABLE_NAME_AS_ID);
                            fm.loadModel();
                            ReasoningWithSAT reasonerSAT = new FMReasoningWithSAT(solverName, fm, SAT_TIMEOUT);
                            reasonerSAT.init();
                            solver = (Solver) reasonerSAT.getSolver();
                            break;
                        case DIMACS:
                            ISolver dimacsSolver = SolverFactory.instance().createSolverByName(solverName);
                            DimacsReader dr = new DimacsReader(dimacsSolver);
                            dr.parseInstance(new FileReader(fmPath));
                            solver = (Solver) dimacsSolver;
                            break;
                    }
                    solver.setTimeout(SAT_TIMEOUT);
                    solver.setOrder(order);
                    solverIterator = new ModelIterator(solver);
                    solverIterator.setTimeoutMs(ITERATOR_TIMEOUT);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return products;
    }

    /**
     * Save products to a file.
     * @param outFile the file to write the products in.
     * @throws Exception if an error occurs while writing the products to the file.
     */
    public void saveProducts(String outFile) throws Exception {



        BufferedWriter out = new BufferedWriter(new FileWriter(outFile));

        int featuresCount = featuresList.size();
        for (int i = 1; i <= featuresCount; i++) {
            out.write(i + "->" + this.featuresList.get(i - 1));
            out.newLine();
        }
        for (Product product : products) {
            int done = 0;
            for (Integer feature : product) {
                out.write("" + feature);
                if (done < product.size()) {
                    out.write(";");
                }
                done++;
            }

            out.newLine();
        }
        out.close();


    }

    /**
     * Load products from a file.
     * @param inFile the file to loead the products from.
     * @throws Exception if an error occurs while reading the products file.
     */
    public void loadProducts(String inFile) throws Exception {
        setRunning(true);
        setIndeterminate(true);
        setGlobalAction(GLOBAL_ACTION_LOAD_PRODUCTS);
        solver = null;
        solverIterator = null;
        featuresIntList = new ArrayList<Integer>();
        featuresList = new ArrayList<String>();
        namesToFeaturesInt = new HashMap<String, Integer>();
        featureModelConstraints = new ArrayList<String>();
        featureModelConstraintsString = new ArrayList<String>();
        coreFeatures = new ArrayList<String>();
        deadFeatures = new ArrayList<String>();
        BufferedReader in = new BufferedReader(new FileReader(inFile));

        products = new ArrayList<Product>();
        String line;

        while ((line = in.readLine()) != null) {
            if (!line.contains(">")) {
                Product p = new Product();
                setCurrentAction("Extracting product number" + products.size());
                StringTokenizer st = new StringTokenizer(line, ";");
                while (st.hasMoreTokens()) {
                    p.add(Integer.parseInt(st.nextToken()));
                }
                products.add(p);
            } else {
//                featuresList.add(line.substring(line.indexOf(">") + 1, line.length()));
            }
        }
        setRunning(false);
        setChanged();
        notifyObservers(this);
    }
    
    /**
     * Load products from a file, after having loaded a FM that corresponds to the products.
     * @param inFile the file to loead the products from.
     * @throws Exception if an error occurs while reading the products file.
     */
    public void loadProductsFM(String inFile) throws Exception {
        setRunning(true);
        setIndeterminate(true);
        setGlobalAction(GLOBAL_ACTION_LOAD_PRODUCTS);
//        solver = null;
//        solverIterator = null;
//        featuresIntList = new ArrayList<Integer>();
//        featuresList = new ArrayList<String>();
//        namesToFeaturesInt = new HashMap<String, Integer>();
//        featureModelConstraints = new ArrayList<String>();
//        featureModelConstraintsString = new ArrayList<String>();
//        coreFeatures = new ArrayList<String>();
//        deadFeatures = new ArrayList<String>();
        BufferedReader in = new BufferedReader(new FileReader(inFile));

        products = new ArrayList<Product>();
        String line;

        while ((line = in.readLine()) != null) {
            if (!line.contains(">")) {
                Product p = new Product();
                setCurrentAction("Extracting product number" + products.size());
                StringTokenizer st = new StringTokenizer(line, ";");
                while (st.hasMoreTokens()) {
                    p.add(Integer.parseInt(st.nextToken()));
                }
                products.add(p);
            } else {
//                featuresList.add(line.substring(line.indexOf(">") + 1, line.length()));
            }
        }
        setRunning(false);
        setChanged();
        notifyObservers(this);
    }

    /**
     * Quit the application.
     */
    public void quit() {
        System.exit(0);
    }

    public int getCurrentConstraint() {
        return currentConstraint;
    }

    public void setCurrentConstraint(int currentConstraint) {
        this.currentConstraint = currentConstraint;
        setChanged();
        notifyObservers();
    }
    
    
}
