/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.nogold;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FloatRangeArgumentDefinition;
import net.sourceforge.ondex.args.IntegerRangeArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.InconsistencyEvent;
import net.sourceforge.ondex.exception.type.PluginException;
import net.sourceforge.ondex.transformer.ONDEXTransformer;
import org.apache.log4j.Level;

/**
 * An Ondex transformer that runs the NoGold fully bayesian integration method.
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class NoGold extends ONDEXTransformer {

    public static final String INTERACTION_CC_ARG = "interactionCC";
    public static final String INTERACTION_CC_ARG_DESC = "Target concept class. e.g. 'Interaction'";

    public static final String MAX_CYCLES_ARG = "maxCycles";
    public static final String MAX_CYCLES_ARG_DESC = "maximum number of cycles";

    public static final String MIN_SAMPLES_ARG = "minSamples";
    public static final String MIN_SAMPLES_ARG_DESC = "minimum number of samples to take";

    public static final String AC_LAG_ARG = "acLag";
    public static final String AC_LAG_ARG_DESC = "autocorrelation lag";

    public static final String AC_THRESHOLD_ARG = "acThreashold";
    public static final String AC_THRESHOLD_ARG_DESC = "autocorrelation threshold";

    public static final String BURNIN_CYCLES_ARG = "burnIn";
    public static final String BURNIN_CYCLES_ARG_DESC = "Number of cycles to spend on burn-in phase";

    public static final String TP_OFFSET_ARG = "tpOffset";
    public static final String TP_OFFSET_ARG_DESC = "TP offset";

    public static final String FP_OFFSET_ARG = "fpOffset";
    public static final String FP_OFFSET_ARG_DESC = "FP offset";

    public static final String TN_OFFSET_ARG = "tnOffset";
    public static final String TN_OFFSET_ARG_DESC = "TN offset";

    public static final String FN_OFFSET_ARG = "fnOffset";
    public static final String FN_OFFSET_ARG_DESC = "FN offset";

    public static final String MIN_EXP_SIZE_ARG = "minExpSize";
    public static final String MIN_EXP_SIZE_ARG_DESC = "Minimum experiment size";

    public static final String AVERAGE_DEGREE_ARG = "avDegree";
    public static final String AVERAGE_DEGREE_ARG_DESC = "average node degree";

    @Override
    public String getId() {
        return "nogold";
    }

    @Override
    public String getName() {
        return "NoGold probabilistic integration";
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    /**
     * get argument no-gold probabilistic integrator
     * @return
     */
    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[] {
            new StringArgumentDefinition(INTERACTION_CC_ARG, INTERACTION_CC_ARG_DESC, true, "Interaction", false),
            new IntegerRangeArgumentDefinition(AC_LAG_ARG, AC_LAG_ARG_DESC, false, 10, 1, Integer.MAX_VALUE),
            new FloatRangeArgumentDefinition(AC_THRESHOLD_ARG, AC_THRESHOLD_ARG_DESC, false, .2f, 0.0f, Float.MAX_VALUE),
            new IntegerRangeArgumentDefinition(BURNIN_CYCLES_ARG, BURNIN_CYCLES_ARG_DESC, false, 100, 0, Integer.MAX_VALUE),
            new FloatRangeArgumentDefinition(TP_OFFSET_ARG, TP_OFFSET_ARG_DESC, false, 1.0f, 0.0f, Float.MAX_VALUE),
            new FloatRangeArgumentDefinition(FP_OFFSET_ARG, FP_OFFSET_ARG_DESC, false, 1.0f, 0.0f, Float.MAX_VALUE),
            new FloatRangeArgumentDefinition(TN_OFFSET_ARG, TN_OFFSET_ARG_DESC, false, 1.0f, 0.0f, Float.MAX_VALUE),
            new FloatRangeArgumentDefinition(FN_OFFSET_ARG, FN_OFFSET_ARG_DESC, false, 1.0f, 0.0f, Float.MAX_VALUE),
            new FloatRangeArgumentDefinition(AVERAGE_DEGREE_ARG, AVERAGE_DEGREE_ARG_DESC, false, 2.0f, 0.0f, Float.MAX_VALUE),
            new IntegerRangeArgumentDefinition(MIN_EXP_SIZE_ARG, MIN_EXP_SIZE_ARG_DESC, true, 20, 2, Integer.MAX_VALUE),
            new IntegerRangeArgumentDefinition(MIN_SAMPLES_ARG, MIN_SAMPLES_ARG_DESC, true, 500, 1, Integer.MAX_VALUE),
            new IntegerRangeArgumentDefinition(MAX_CYCLES_ARG, MAX_CYCLES_ARG_DESC, true, 100000, 2, Integer.MAX_VALUE),
        };
    }

    /**
     * starts the algorithm
     * @throws Exception
     */
    @Override
    public void start() throws Exception {

        //map linking edges to interaction concepts
        //will get filled by the extractNetworkData() method
        Map<SetOfTwo<Integer>,ONDEXConcept> edge2interaction =
                new HashMap<SetOfTwo<Integer>, ONDEXConcept>();

        //minimum experiment size
        int minExpSize = (Integer)getArgument(MIN_EXP_SIZE_ARG);

        Collection<Network> experiments = extractNetworkData(edge2interaction, minExpSize);

        //configure MCMC
        FBM fbm = new FBM();
        fbm.setExperiments(experiments);
        fbm.setMinSamples((Integer)getArgument(MIN_SAMPLES_ARG));
        fbm.setMaxCycles((Integer)getArgument(MAX_CYCLES_ARG));
        fbm.setAcLag((Integer)getArgument(AC_LAG_ARG));
        fbm.setAcThreshold((Float)getArgument(AC_THRESHOLD_ARG));
        fbm.setBurnIn((Integer)getArgument(BURNIN_CYCLES_ARG));
        fbm.setAvDegree((Float)getArgument(AVERAGE_DEGREE_ARG));
        fbm.setTpOffset((Float)getArgument(TP_OFFSET_ARG));
        fbm.setFpOffset((Float)getArgument(FP_OFFSET_ARG));
        fbm.setTnOffset((Float)getArgument(TN_OFFSET_ARG));
        fbm.setFnOffset((Float)getArgument(FN_OFFSET_ARG));

        //run MCMC
        Map<SetOfTwo<Integer>,Double> probs = fbm.runMCMC();

        //apply results
        annotateInteractions(edge2interaction, probs);

        //output probabilities to file (for R histograms)
        writeProbsToFile(probs);

    }

    private Object getArgument(String id) throws PluginException {
        Object out = getArguments().getUniqueValue(id);
        if (out == null) {
            for (ArgumentDefinition<?> d : getArgumentDefinitions()) {
                if (d.getName().equals(id)) {
                    return d.getDefaultValue();
                }
            }
            throw new PluginException("Argument ID mismatch. "
                    + "Report this is as a bug if you see this message.");
        } else {
            return out;
        }
    }

    /**
     * Extract the relevant network from the ondex graph
     * @param edge2interaction map linking network edge objects to interaction concepts
     * @param minExpSize minimum experiment size
     * @return a list of experimental networks
     * @throws PluginException
     */
    private List<Network> extractNetworkData(Map<SetOfTwo<Integer>,
            ONDEXConcept> edge2interaction, int minExpSize)
            throws PluginException {
        
        //get target concept class
        ConceptClass interactionCC = requireConceptClass(
                (String)getArgument(INTERACTION_CC_ARG)
        );

        //get required metadata
        ConceptClass ccExp = requireConceptClass("Experiment");
        RelationType rtObs = requireRelationType("observed_in");
        RelationType rtPartic = requireRelationType("participates_in");
        AttributeName anNeg = requireAttributeName("negative");

        Map<ONDEXConcept,Network> concept2Experiment = new HashMap<ONDEXConcept, Network>();

        int emptyCount = 0, multiCount = 0, noEvidenceCount = 0;

        //extract experimental data
        //for interaction concept each concept
        for (ONDEXConcept cInt : graph.getConcepts()) {

            //ensure only interaction concepts are processed below
            if (!cInt.inheritedFrom(interactionCC)) {
                continue;
            }

            //determine if negative interaction
            Attribute aNeg = cInt.getAttribute(anNeg);
            boolean neg = aNeg != null && aNeg.getValue().equals(true);

            //prepare lists of experiments and gene/protein nodes
            List<ONDEXConcept> cExps = new ArrayList<ONDEXConcept>(8);
            List<ONDEXConcept> cNodes = new ArrayList<ONDEXConcept>(2);

            //search for experiments and gene/protein nodes
            for (ONDEXRelation r : graph.getRelationsOfConcept(cInt)) {
                if (r.inheritedFrom(rtObs)) {
                    //experiments
                    ONDEXConcept cExp = r.getToConcept();
                    if (!cExp.inheritedFrom(ccExp)) {
                        complain("Interaction observed in non-experiment");
                        continue;
                    }
                    cExps.add(cExp);
                } else if (r.inheritedFrom(rtPartic)) {
                    //nodes
                    ONDEXConcept cNode = r.getFromConcept();
                    cNodes.add(cNode);
                }
            }

            //check for potential problems
            if (cNodes.size() < 2) {
                emptyCount++;
                continue;
            }
            if (cNodes.size() > 2) {
                multiCount++;
                continue;
            }
            if (cExps.size() < 1) {
                noEvidenceCount++;
                continue;
            }

            //fill experiments with content
            for (ONDEXConcept cExp : cExps) {

                //retrive or create experimental network
                Network expNet = concept2Experiment.get(cExp);
                if (expNet == null) {
                    expNet = new Network(cExp.getId());
                    concept2Experiment.put(cExp,expNet);
                }

                //add edge (or non-edge)
                int a = cNodes.get(0).getId();
                int b = cNodes.get(1).getId();
                SetOfTwo<Integer> edge = new SetOfTwo<Integer>(a, b);
                if (neg) {
                    expNet.addNegative(edge);
                } else {
                    expNet.add(edge);
                }
                edge2interaction.put(edge,cInt);

            }

        }

        //log errors:
        if (multiCount > 0) {
            log("Skipped "+multiCount+" multidimensional interaction.");
        }
        if (emptyCount > 0) {
            complain("Warning: Skipped "+emptyCount+" empty interaction concepts!");
        }
        if (noEvidenceCount > 0) {
            complain("Warning: Skipped "+noEvidenceCount+" interactions without evidence!");
        }

        int smallCount = 0;

        //filter experiments by size
        int numNets = concept2Experiment.values().size();
        List<Network> finalExperiments = new ArrayList<Network>(numNets);
        for (Network exp : concept2Experiment.values()) {
            if (exp.getNodes().size() > minExpSize) {
                finalExperiments.add(exp);
            } else {
                smallCount++;
            }
        }

        if (smallCount > 0) {
            log("Skipped "+smallCount+" small experiments");
        }

        return finalExperiments;
    }


    /**
     * Transfers the probabilities to the interaction nodes
     * @param edge2interaction
     * @param probs
     * @throws PluginException
     */
    private void annotateInteractions(
            Map<SetOfTwo<Integer>,ONDEXConcept> edge2interaction,
            Map<SetOfTwo<Integer>, Double> probs)
            throws PluginException {

        //get metadata
        AttributeName anProb = requireAttributeName("existenceProbability");

        //get target concept class
        ConceptClass targetCC = requireConceptClass(
                (String)getArgument(INTERACTION_CC_ARG)
        );

        //for each edge
        for (SetOfTwo<Integer> edge : probs.keySet()) {

            //get the corresponding interaction concept
            ONDEXConcept cInt = edge2interaction.get(edge);
            if (!cInt.inheritedFrom(targetCC)) {
                complain("Interaction concept mismatch. This is a bug!!");
            }

            //and attach the probability value to it
            double p = probs.get(edge);
            cInt.createAttribute(anProb, p, false);
            
        }

    }


    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    private void complain(String string) {
        EventType e = new InconsistencyEvent(string, "");
        e.setLog4jLevel(Level.WARN);
        fireEventOccurred(e);
    }

    private void log(String string) {
        EventType e = new GeneralOutputEvent(string, "");
        e.setLog4jLevel(Level.INFO);
        fireEventOccurred(e);
    }

    /**
     * Writes probabilities to file. (For R histograms)
     * @param probs
     */
    private void writeProbsToFile(Map<SetOfTwo<Integer>, Double> probs) {
        BufferedWriter w = null;
        try {
            w = new BufferedWriter(new FileWriter("probabilities.csv"));
            for (Entry<SetOfTwo<Integer>,Double> entry : probs.entrySet()) {
                w.write(String.format("%.8f",entry.getValue()));
                w.write("\n");
            }
        } catch (IOException ioe) {
            complain(ioe.getMessage());
        } finally {
            try {
                w.close();
            } catch (IOException ioe) {
                complain(ioe.getMessage());
            }
        }
    }

}
