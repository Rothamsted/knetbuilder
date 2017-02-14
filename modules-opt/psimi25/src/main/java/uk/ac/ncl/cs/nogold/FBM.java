/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.nogold;

import cern.jet.random.Beta;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of the fully Bayesian MCMC-based probabilistic integration method.
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class FBM {

    /**
     * Set of all edges over all datasets
     */
    private Set<SetOfTwo<Integer>> allEdges;

    /**
     * Number of nodes in the global network
     */
    private int numNodes;

    /**
     * Average expected node degree (for computing prior)
     */
    private double avDegree = 2.0;


    /**
     * collection of experimental networks
     */
    private Collection<Network> experiments;

    /**
     * Maps each input graph to its current error rate estimates
     */
    private Map<Network,ErrorRates> errorRates;

    /**
     * Maps each edge to its current posterior
     * existence probability estimate.
     */
    private Map<SetOfTwo<Integer>,Double> probabilities = new HashMap<SetOfTwo<Integer>, Double>();

    /**
     * Set of edges in the consensus graph edges
     */
    private Set<SetOfTwo<Integer>> intermediateGraph = new HashSet<SetOfTwo<Integer>>();

    /**
     * Beta-distributed random sampler
     */
    private Beta beta;

    /**
     * Uniform-distributed random sampler
     */
    private Uniform uniform;

    //initialize random samplers
    {
        RandomEngine e = new MersenneTwister(new Date());
        beta = new Beta(1,1,e);
        uniform = new Uniform(e);
    }

    /**
     * maximal number of cycles to perform
     */
    private int maxCycles;

    /**
     * minimal number of cycles to perform
     */
    private int minSamples;

    /**
     * autocorrelation lag for deterimining thinning value.
     */
    private int acLag = 10;

    /**
     * autocorrelation threshold for determining thinning value.
     */
    private double acThreshold = .2;

    /**
     * number of burn-in cycles
     */
    private int burnIn = 100;

    /**
     * offsets for error rate sampler.
     */
    private double tpOffset = 1, tnOffset = 1, fpOffset = 1, fnOffset = 1;


    /**
     * runs the algorithm
     * @return a map linking each edge to its existence probability
     */
    public Map<SetOfTwo<Integer>,Double> runMCMC() {

        //init burn-in recorder (determines thinning value)
        BurnIn burnInRecorder = new BurnIn(burnIn, experiments.size(), acLag, acThreshold);

        //records running averages for each edge probability.
        EdgeAverager edgeAverager = new EdgeAverager();

        //generate consensus graph
        performUnion();

        //calculate inital error rates ("theta") for each input graph
        initErrorRates();

        //compute prior probability for an edge to exist ("q")
        int v2 = (numNodes * numNodes - numNodes) / 2;
        double prior = avDegree * (double)numNodes / (double)v2;


        boolean isBurnInPhase = true;
        int skip = 1;//thinning value

        //Main loop.
        for (int i = 0; i < maxCycles; i++) {

            //calculate posterior edge probabilites based on latest theta
            updateProbabilities(prior);

            //use posterior edge probabilites to sample a consensus graph ("G")
            sampleIntermediateGraph();

            //sample a new theta based on G
            sampleErrorRates();

            if (isBurnInPhase) {

                if (!burnInRecorder.record(errorRates)) {
                    skip = burnInRecorder.computeThinningFactor(20);
                    burnInRecorder = null;//free up memory
                    isBurnInPhase = false;
                }

            } else if (i % skip == 0) {//if it's a non-skip cycle

                edgeAverager.update(intermediateGraph);
                if (edgeAverager.getNumSamples() >= minSamples) {
                    break;//then we have enough samples.
                }

            }

            printProgress(edgeAverager.getNumSamples(), i);

        }

        //apply average theta to generate final graph
        return edgeAverager.getEdgeAverages();

    }

    /**
     * Calculates inital error rates for each dataset
     */
    private void initErrorRates() {

        //init map
        errorRates = new HashMap<Network,ErrorRates>();

        //assign random beta(1,1) value.
        for (Network eviGraph : experiments) {
            double fpr = beta.nextDouble(fpOffset,tnOffset);
            double fnr = beta.nextDouble(fnOffset,tpOffset);
            errorRates.put(eviGraph, new ErrorRates(fpr, fnr));
        }

    }
    

    private void updateProbabilities(double prior) {

        //convert prior probability to log odds.
        double logPriorOdds = Math.log(prior / (1.0 - prior));

        //For each edge
        for (SetOfTwo<Integer> edge : allEdges) {

            //K = prior + ...
            double k = logPriorOdds;

            // + sum_i (wrt evidences) of...
            for (Network eviGraph : experiments) {
                //only if the graph contains the node pair in question
                if (containsNodes(eviGraph, edge)) {

                    //lambda_i (Bayes factor) depending on positive or negative reporting
                    ErrorRates bf = errorRates.get(eviGraph);

                    if(eviGraph.contains(edge)) {
                        k += bf.getLogPositiveBayesFactor();
                    } else {
                        k += bf.getLogNegativeBayesFactor();
                    }
                }
            }

            //convert log odds back to probability and store for edge
            double p = Math.exp(k) / (1.0 + Math.exp(k));
            probabilities.put(edge,p);
        }
    }

    /**
     * whether or not the given network contains the two nodes given in the setoftwo.
     * @param e the network
     * @param twoNodes the two nodes.
     * @return 
     */
    private boolean containsNodes(Network e, SetOfTwo<Integer> twoNodes) {
        return e.contains(twoNodes.getA()) && e.contains(twoNodes.getB());
    }


    /**
     * Sample a new theta based on current G
     */
    private void sampleErrorRates() {

        for (Network ex : experiments) {

            Errors errors = new Errors(intermediateGraph, ex);

            //sample false postive rate from beta
            double fprSample = beta.nextDouble(errors.getFp()+fpOffset, errors.getTn()+tnOffset);

            //sample false negative rate from beta
            double fnrSample = beta.nextDouble(errors.getFn()+fnOffset, errors.getTp()+tpOffset);

            //store samples and densities
            ErrorRates e = new ErrorRates(fprSample, fnrSample);
            errorRates.put(ex, e);
        }

    }

    /**
     * sample intermediate consensus graph (G) based on p
     */
    private void sampleIntermediateGraph() {
        //for each edge that has at least one supporting evidence (others have p=0 anyway)
        for (SetOfTwo<Integer> edge : allEdges) {

            //if uniform random > p(edge) create it otherwise remove it
            double p = probabilities.get(edge);
            if (uniform.nextDouble() < p) {
                intermediateGraph.add(edge);
            } else {
                intermediateGraph.remove(edge);
            }
        }
    }

    private void performUnion() {
        Set<Integer> allNodes = new HashSet<Integer>();
        allEdges = new HashSet<SetOfTwo<Integer>>();
        for (Network ex : experiments) {
            allNodes.addAll(ex.getNodes());
            allEdges.addAll(ex.getEdges());
        }
        numNodes = allNodes.size();
    }

    public void setAcLag(int acLag) {
        this.acLag = acLag;
    }

    public void setAcThreshold(double acThreshold) {
        this.acThreshold = acThreshold;
    }

    public void setBurnIn(int burnIn) {
        this.burnIn = burnIn;
    }

    public void setExperiments(Collection<Network> experiments) {
        this.experiments = experiments;
    }

    public void setMaxCycles(int maxCycles) {
        this.maxCycles = maxCycles;
    }

    public void setMinSamples(int minCycles) {
        this.minSamples = minCycles;
    }

    public void setFnOffset(double fnOffset) {
        this.fnOffset = fnOffset;
    }

    public void setFpOffset(double fpOffset) {
        this.fpOffset = fpOffset;
    }

    public void setTnOffset(double tnOffset) {
        this.tnOffset = tnOffset;
    }

    public void setTpOffset(double tpOffset) {
        this.tpOffset = tpOffset;
    }


    public void setAvDegree(double avDegree) {
        this.avDegree = avDegree;
    }

    private int lastPercent = 0;
    private void printProgress(int numSamples, int cycle) {
        int samplePercent = numSamples * 100 / minSamples;
        int cyclePercent = cycle * 100 / maxCycles;
        int maxPercent = samplePercent > cyclePercent ?
            samplePercent : cyclePercent;
        if (maxPercent > lastPercent) {
            System.out.println("MCMC: "+maxPercent+"%");
            lastPercent = maxPercent;
        }
    }
    
    

}
