package net.sourceforge.ondex.transformer.buildhierarchy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

/**
 * computes a graph hierarchy model consisting of a dendrogram and edge probability
 * parameters based on MCMC and maximum likelihood methods.
 * see DOI: 10.1007/978-3-540-73133-7_1
 *
 * @author Jochen Weile, B.Sc., Original algorithm: Dr. Aaron Clauset
 */
@Authors(authors = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Transformer extends ONDEXTransformer implements ArgumentNames {

    //####FIELDS####

    /**
     * user selected TaxIDs.
     */
    private HashSet<Integer> taxids;

    /**
     * user selected CVs.
     */
    private HashSet<DataSource> cvs;

    /**
     * the CCs the user selected.
     */
    private HashSet<ConceptClass> ccs;

    /**
     * set of concepts, that this transformer uses.
     */
    private HashSet<Integer> usedConcepts;


    /**
     * CC hierarchy.
     */
    private ConceptClass cc_hierarchy;

    /**
     * RTS is part of.
     */
    private RelationType rts_isp;

    /**
     * AttributeName taxid, theta, logL.
     */
    private AttributeName an_taxid, an_theta, an_logl;

    /**
     * DataSource for hierarchy.
     */
    private DataSource dataSource_unknown;

    /**
     * Evidence for hierarchy.
     */
    private EvidenceType ev;

    /**
     * internal indexing of concepts so it can be used as an array index.
     */
    private HashMap<Integer, Integer> iid2cid, cid2iid;

    /**
     * Matrix storing edge id for each pair of concepts.
     */
    private int[][] edgeMatrix;

//	/**
//	 * maps the node ids to their instances.
//	 */
//	private Node[] nid2node;//, nid2node_tmp;
//	
//	/**
//	 * the hierarchy root.
//	 */
//	private Node root;

    /**
     * the currently active dendrogram.
     */
    private Dendrogram d_curr;

    /**
     * the consensus buildup set.
     */
    private ConsensusSet consensusSet;

    //$$$$CONSTANTS$$$$

    /**
     * last likelihood record.
     */
    private double logL_record = Double.NEGATIVE_INFINITY;

    /**
     * debug flag.
     */
    private static final boolean DEBUG = false;

//	/**
//	 * maximum cycles.
//	 */
//	private static final int delta_sat = 50000;//CYCLES = 1200000, delta_sat = 100000;

//	/**
//	 * pseudocount multiplicators.
//	 */
//	private static final double LAMBDA = 0.0, KAPPA = 0.0;//LAMBDA = .3, KAPPA = .8;

    /**
     * do consensus?
     */
    private static final boolean CONSENSUS = true;


    //####METHODS####

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#start()
     */
    @Override
    public void start() throws InvalidPluginArgumentException {
        fireEventOccurred(new GeneralOutputEvent("\npreparing...", ""));
        fetchArguments();
        fetchMetaData();

        createWorkingSet();

        fireEventOccurred(new GeneralOutputEvent("\npreprocessing...", ""));
        indexGraph();
        d_curr = buildTemplateTree();

        fireEventOccurred(new GeneralOutputEvent("\nrunning...", ""));
        runMCMC();

        fireEventOccurred(new GeneralOutputEvent("\npostprocessing...", ""));

        if (CONSENSUS) {
            Cluster root = consensusSet.buildConsensus();
            if (root != null)
                buildOutput(root);
        } else {
            buildOutput(d_curr.getRoot());
        }

        fireEventOccurred(new GeneralOutputEvent("\ndone", ""));
    }

    /**
     * runs the actual MCMC algorithm.
     */
    private void runMCMC() {
        try {
            double delta_sat = (4000.0 * ((double) usedConcepts.size()));

            int run = 1;
            long start = System.currentTimeMillis();
            long interim_time = start;

            StatsRecorder.reset();
            BufferedWriter w = new BufferedWriter(new FileWriter(net.sourceforge.ondex.config.Config.ondexDir + System.getProperty("file.separator") + "mcmc_" + System.currentTimeMillis() + ".txt"));
            int logging_step = (((int) Math.pow((double) usedConcepts.size(), 2.0)) / 32000) + 1;

            boolean inSaturation = false, terminate = false, record = false;
            int time_lastRecord = 0;

            double consensus_sampling_min = Double.POSITIVE_INFINITY;
            consensusSet = new ConsensusSet(usedConcepts.size());
            int num_clusters = Integer.MAX_VALUE, diff_clust = Integer.MAX_VALUE;

            while (!terminate || !(record || CONSENSUS)) {
                if (DEBUG) System.out.print(run + "\t");

                record = false;
                doStepMCMC();
                d_curr.calcLogLikelihood();

                if (d_curr.getLogLikelihood() > logL_record) {
                    logL_record = d_curr.getLogLikelihood();
                    time_lastRecord = run;
                    record = true;
                }

                if (!inSaturation) {
                    if (run - time_lastRecord > delta_sat) {
                        double consensus_sampling_offset = Math.sqrt(-logL_record / 4.0);
                        consensus_sampling_min = logL_record - consensus_sampling_offset;
                        inSaturation = true;
                        fireEventOccurred(new GeneralOutputEvent("\n\nsaturation phase reached at logL" + logL_record + ", start feeding consensus set...\n", ""));
                    }
                } else if (d_curr.getLogLikelihood() > consensus_sampling_min) {
                    consensusSet.registerTree(d_curr);
                    if (consensusSet.getFeeds() % usedConcepts.size() == 0) {
                        diff_clust = num_clusters - consensusSet.getNumberOfConsensusClusters();
                        terminate = (diff_clust == 0) && (consensusSet.getFeeds() > 10 * usedConcepts.size());
                        num_clusters = consensusSet.getNumberOfConsensusClusters();
                    }
                }

                if (DEBUG) System.out.println("" + d_curr.getLogLikelihood());

                //report to console
                if (System.currentTimeMillis() - interim_time > 60000L) {
//					int percent = (int)((((double)run)/((double)CYCLES))*100.0);
                    int time = (int) ((System.currentTimeMillis() - start) / 60000L);
                    if (!inSaturation)
                        fireEventOccurred(new GeneralOutputEvent("\ntime: " + time + "min\tlogL: " + logL_record, ""));
                    else {
                        if (diff_clust != Integer.MAX_VALUE)
                            fireEventOccurred(new GeneralOutputEvent("\ntime: " + time + "min\tlogL: " + logL_record + "\tdelta: " + diff_clust + "\tfeeds: " + consensusSet.getFeeds(), ""));
                        else
                            fireEventOccurred(new GeneralOutputEvent("\ntime: " + time + "min\tlogL: " + logL_record + "\tdelta: +inf\tfeeds: " + consensusSet.getFeeds(), ""));
                    }
                    interim_time = System.currentTimeMillis();
                }

                //write log
                if (run % logging_step == 0)
                    w.write(run + "\t" + d_curr.getLogLikelihood() + "\n");

                run++;
            }
            w.close();
            StatsRecorder.getInstance((int) ((-d_curr.getLogLikelihood()) + 1)).write();
            long time = System.currentTimeMillis() - start;
            int min = ((int) time) / 60000;
            int sec = (((int) time) / 1000) % 60;
            if (DEBUG) System.out.println("took: " + min + ":" + sec);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * mutates the current tree at a random position and repeats this
     * until the logL is better or within a defined proportion below.
     */
    private void doStepMCMC() {

        Node n_i = null, n_c = null, n_p = null, n_s = null, n_i_tmp = null, n_p_tmp = null;
        HashSet<Integer> leaves_i_tmp = null;

        boolean accept = false;

        int rejections = 0;

        while (!accept) {
            int side = randomLeftRight();
            int otherside = side == 0 ? 1 : 0;
            do n_i = d_curr.getNodeTable()[randomIndex()]; while (n_i.isRoot() || n_i.isLeaf());

            n_c = n_i.getChild(side);
            n_p = n_i.parent;
            n_s = n_p.getSiblingOf(n_i);

            n_i_tmp = computeEThetaF(n_s.getLeaves(), n_i.getChild(otherside).getLeaves());
            leaves_i_tmp = new HashSet<Integer>();
            leaves_i_tmp.addAll(n_s.getLeaves());
            leaves_i_tmp.addAll(n_i.getChild(otherside).getLeaves());
            n_p_tmp = computeEThetaF(n_c.getLeaves(), leaves_i_tmp);

            double deltaF_old = n_i.logF + n_p.logF;
            double deltaF_new = n_i_tmp.logF + n_p_tmp.logF;

            double logL_change = deltaF_new - deltaF_old;
            StatsRecorder.getInstance((int) ((-d_curr.getLogLikelihood()) + 1)).record(logL_change);

//			if (deltaF_new > deltaF_old)
//				accept = true;
//			else if (rejections > usedConcepts.size() / 10){
//				double logL_new = d_curr.getLogLikelihood() - deltaF_old + deltaF_new;
//				//detailed balance case
//				accept = Math.exp(d_curr.getLogLikelihood()) - Math.exp(logL_new) <= Math.exp(logL_change);
//			}

            accept = StatsRecorder.getInstance((int) ((-d_curr.getLogLikelihood()) + 1)).acceptable(deltaF_old, deltaF_new);

            if (!accept)
                rejections++;

            if (DEBUG && (rejections % 1000 == 0)) System.out.print(".");
        }

        if (DEBUG) System.out.print(rejections + "\t");

        n_p.replaceChild(n_s, n_c);
        n_i.replaceChild(n_c, n_s);

        n_p.e = n_p_tmp.e;
        n_p.theta = n_p_tmp.theta;
        n_p.logF = n_p_tmp.logF;

        n_i.e = n_i_tmp.e;
        n_i.theta = n_i_tmp.theta;
        n_i.logF = n_i_tmp.logF;
        n_i.setLeaves(leaves_i_tmp);

    }

    /**
     * recursively builds an output graph based on a
     * single binary tree.
     *
     * @param n input node. (call this method with the root node)
     */
    private ONDEXConcept buildOutput(Node n) {
        ONDEXConcept c;
        if (n.isLeaf()) {
            c = graph.getConcept(iid2cid.get(n.getId()));
        } else {
            c = graph.getFactory().createConcept("hierarchy_node:" + n.getId(), dataSource_unknown, cc_hierarchy, ev);

            c.createAttribute(an_theta, new Double(n.theta), false);
            c.createAttribute(an_logl, new Double(n.logF), false);

            ONDEXConcept cl = buildOutput(n.getChild(Node.LEFT));
            ONDEXConcept cr = buildOutput(n.getChild(Node.RIGHT));

            graph.getFactory().createRelation(cl, c, rts_isp, ev);
            graph.getFactory().createRelation(cr, c, rts_isp, ev);
        }
        return c;
    }

    /**
     * recursively builds the output graph based on the
     * consensus tree.
     *
     * @param n input cluster (call this method with the root cluster)
     */
    private ONDEXConcept buildOutput(Cluster n) {
        ONDEXConcept c = graph.getFactory().createConcept("hierarchy_node:" + n.toString(), dataSource_unknown, cc_hierarchy, ev);
        BitSet kidleaves = new BitSet(usedConcepts.size());
        if (n.getChildren() != null) {
            for (Cluster kid : n.getChildren()) {
                ONDEXConcept c_kid = buildOutput(kid);
                kidleaves.or((BitSet) kid.getKey());
                graph.getFactory().createRelation(c_kid, c, rts_isp, ev);
            }
        }

        BitSet leaves = (BitSet) n.getKey().clone();
        leaves.andNot((BitSet) kidleaves);

        for (int i = leaves.nextSetBit(0); i > -1; i = leaves.nextSetBit(i + 1)) {
            ONDEXConcept cl = graph.getConcept(iid2cid.get(i));
            graph.getFactory().createRelation(cl, c, rts_isp, ev);
        }

        return c;
    }

    /**
     * computes the values of theta and e for the given node and
     * stores them inside the node.
     *
     * @param n the node.
     */
    private void computeAndSetThetaE(Node n) {

        Node dummy = computeEThetaF(n.getChild(Node.LEFT).getLeaves(),
                n.getChild(Node.RIGHT).getLeaves());

        n.e = dummy.e;
        n.theta = dummy.theta;
        n.logF = dummy.logF;

    }

    /**
     * computes the values of theta and e for the given node and
     *
     * @param left
     * @param right
     * @return a dummy node serving as a container for the values.
     */
    private Node computeEThetaF(HashSet<Integer> left, HashSet<Integer> right) {

        int e = 0;
        for (int l_i : left) {
            for (int l_j : right) {
                if (edgeMatrix[l_i][l_j] > -1)
                    e++;
            }
        }

        double l = (double) left.size();
        double r = (double) right.size();

        double theta = ((double) e) / (l * r);

//		//punish linearization
//		double anti_l = -KAPPA * Math.abs((double)left.size() - (double)right.size());

        double log_f_i;
        if (theta == 1) {
            log_f_i = 0;
//			log_f_i += anti_l;
        } else if (theta == 0) {
//			//pseudocounts
//			double l_tot = (double)usedConcepts.size();
//			log_f_i = -LAMBDA * (l_tot / Math.min(l,r));
            log_f_i = 0;
        } else {
            log_f_i = e * Math.log(theta) + (l * r - e) * Math.log(1.0 - theta);
//			log_f_i += anti_l;
        }


        Node dummy = new Node(e, theta, log_f_i);
        return dummy;
    }

    /**
     * returns a random index of an internal node of the tree.
     */
    private int randomIndex() {
        double r = Math.random();
        double width = usedConcepts.size() * 2.0 - 2.0;
//		int min = usedConcepts.size();
        int i = ((int) Math.rint(r * width));// + min;
        return i;
    }

    /**
     * returns randomly 1 or 0.
     */
    private int randomLeftRight() {
        double r = Math.random();
        int i = (int) (r * 2.0);
        return i;
    }


//	/**
//	 * calculates the log likelihood of the 
//	 * current tree
//	 * @return its subtree's log likelihood.
//	 */
//	private double calcLogLikelihood(Dendrogram d) {
//		double logli = 0.0;
//		for (Node n : d.id2node) {
//			if (!n.isLeaf()) {
//				logli += n.logF;
//			}
//		}
//		return logli;
//	}

    /**
     * builds the first template tree serving as
     * the first state of the MCMC.
     * Runtime: O(1/2 * (n^2 - n)) = O(n^2)   --- n = #concepts
     */
    private Dendrogram buildTemplateTree() {
        PriorityQueue<Node> open = new PriorityQueue<Node>(usedConcepts.size(), new Comparator<Node>() {

            @Override
            public int compare(Node o1, Node o2) {
                if (o1.getId() < o2.getId())
                    return -1;
                if (o1.getId() == o2.getId())
                    return 0;
                else
                    return 1;
            }

        });

        Node.resetIdCounter();

        Node[] nid2node = new Node[(2 * usedConcepts.size()) - 1];

        for (int i = 0; i < usedConcepts.size(); i++) {
            /*
                * DON'T BE CONFUSED:
                * the concept's iids are all numbers between
                * 0 and usedConcepts.size() so they can serve
                * as array indices. So in this case I can just
                * create a leaf node for each number between
                * 0 and usedConcepts.size() and magically: it's the same.
                */
            Node leaf = new Node(i);
            open.offer(leaf);

            nid2node[leaf.getId()] = leaf;
        }

        /*
           * -2 + 1 = -1
           * => the queue shrinks by one in each cycle.
           */
        while (open.size() > 1) {
            Node c1 = open.poll();
            Node c2 = open.poll();
            Node p = new Node(c1, c2);

            computeAndSetThetaE(p);

            open.offer(p);
            nid2node[p.getId()] = p;
        }

        //the only element left in the queue is the root.
        Node root = open.poll();

        Dendrogram d = new Dendrogram(root, nid2node);
        return d;
    }

    /**
     * creates indices and matrices for the graph.
     * Runtime O(n+e)      --- n = #concepts, e = #edges
     */
    private void indexGraph() {
        iid2cid = new HashMap<Integer, Integer>();
        cid2iid = new HashMap<Integer, Integer>();

        int i = 0;
        for (int id : usedConcepts) {
            iid2cid.put(i, id);
            cid2iid.put(id, i);
            i++;
        }

        int num_c = usedConcepts.size();

        edgeMatrix = new int[num_c][num_c];
        for (int j = 0; j < num_c; j++) {
            for (int k = 0; k < num_c; k++) {
                edgeMatrix[j][k] = -1;
            }
        }

        for (ONDEXRelation r : graph.getRelations()) {
            int rid = r.getId();
            int fid = r.getFromConcept().getId();
            int tid = r.getToConcept().getId();
            Integer fiid = cid2iid.get(fid);
            Integer tiid = cid2iid.get(tid);
            if (fiid != null && tiid != null) {
                edgeMatrix[fiid][tiid] = rid;
                edgeMatrix[tiid][fiid] = rid;
            }
        }
    }

    /**
     * creates the set of used concepts by evaluating the user arguments about
     * cc, cv and taxid restrictions.
     */
    private void createWorkingSet() {
        HashSet<Integer> idOfTaxid = new HashSet<Integer>(),
                idOfCV = new HashSet<Integer>(),
                idOfCC = new HashSet<Integer>();

        if (taxids.size() > 0) {
            for (ONDEXConcept c : graph.getConceptsOfAttributeName(an_taxid)) {
                Attribute attribute = c.getAttribute(an_taxid);
                int tax_curr = Integer.parseInt((String) attribute.getValue());
                if (taxids.contains(new Integer(tax_curr)))
                    idOfTaxid.add(c.getId());
            }
        } else {
            for (ONDEXConcept c : graph.getConcepts()) {
                idOfTaxid.add(c.getId());
            }
        }

        for (ConceptClass cc : ccs) {
            for (ONDEXConcept c : graph.getConceptsOfConceptClass(cc)) {
                idOfCC.add(c.getId());
            }
        }
        if (idOfCC.size() == 0) {
            for (ONDEXConcept c : graph.getConcepts()) {
                idOfCC.add(c.getId());
            }
        }

        for (DataSource dataSource : cvs) {
            for (ONDEXConcept c : graph.getConceptsOfDataSource(dataSource)) {
                idOfCV.add(c.getId());
            }
        }
        if (idOfCV.size() == 0) {
            for (ONDEXConcept c : graph.getConcepts()) {
                idOfCV.add(c.getId());
            }
        }

        usedConcepts = new HashSet<Integer>();
        for (Integer id : idOfTaxid) {
            if (idOfCC.contains(id) && idOfCV.contains(id))
                usedConcepts.add(id);
        }

    }

    /**
     * fetches and checks the required metadata.
     */
    private void fetchMetaData() {
        cc_hierarchy = graph.getMetaData().getConceptClass("HierarchyNode");
        if (cc_hierarchy == null)
            fireEventOccurred(new ConceptClassMissingEvent("CC HierarchyNode is missing!", getName()));

        rts_isp = graph.getMetaData().getRelationType("member_of");
        if (rts_isp == null)
            fireEventOccurred(new RelationTypeMissingEvent("RT is_p is missing!", getName()));

        an_taxid = graph.getMetaData().getAttributeName("TAXID");
        if (an_taxid == null)
            fireEventOccurred(new AttributeNameMissingEvent("AN TAXID is missing!", getName()));

        an_theta = graph.getMetaData().getAttributeName("THETA");
        if (an_theta == null)
            fireEventOccurred(new AttributeNameMissingEvent("AN THETA is missing!", getName()));

        an_logl = graph.getMetaData().getAttributeName("LOGL");
        if (an_logl == null)
            fireEventOccurred(new AttributeNameMissingEvent("AN LOGL is missing!", getName()));

        dataSource_unknown = graph.getMetaData().getDataSource("unknown");
        if (dataSource_unknown == null)
            fireEventOccurred(new DataSourceMissingEvent("DataSource unknown is missing", getName()));

        ev = graph.getMetaData().getEvidenceType("STOCHASTIC");
        if (ev == null)
            fireEventOccurred(new EvidenceTypeMissingEvent("Evidence STOCHASTIC is missing!", getName()));
    }

    /**
     * fetches and checks the arguments.
     */
    private void fetchArguments() throws InvalidPluginArgumentException {

        String[] os = (String[]) args.getObjectValueArray(TAXID_ARG);
        taxids = new HashSet<Integer>();
        for (String taxid : os) {
            try {
                taxids.add(Integer.parseInt(taxid));
            } catch (NumberFormatException nfe) {
                fireEventOccurred(new WrongParameterEvent(taxid + " is no TaxID!", getName()));
            }
        }

        os = (String[]) args.getObjectValueArray(CONCEPTCLASS_RESTRICTION_ARG);
        ccs = new HashSet<ConceptClass>();
        for (String ccid : os) {
            ConceptClass cc = graph.getMetaData().getConceptClass(ccid);
            if (cc != null)
                ccs.add(cc);
            else
                fireEventOccurred(new WrongParameterEvent(ccid + " is no ConceptClass!", getName()));
        }


        os = (String[]) args.getObjectValueArray(DATASOURCE_RESTRICTION_ARG);
        cvs = new HashSet<DataSource>();
        for (String cvid : os) {
            DataSource dataSource = graph.getMetaData().getDataSource(cvid);
            if (dataSource != null)
                cvs.add(dataSource);
            else
                fireEventOccurred(new WrongParameterEvent(cvid + " is no DataSource!", getName()));
        }

    }

    /**
     * @see net.sourceforge.ondex.transformer.ONDEXTransformer#getArgumentDefinitions()
     */
    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new StringArgumentDefinition(TAXID_ARG, TAXID_ARG_DESC, false, null, true),
                new StringArgumentDefinition(CONCEPTCLASS_RESTRICTION_ARG, CONCEPTCLASS_RESTRICTION_ARG_DESC, false, null, true),
                new StringArgumentDefinition(DATASOURCE_RESTRICTION_ARG, DATASOURCE_RESTRICTION_ARG_DESC, false, null, true)
        };
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#getName()
     */
    @Override
    public String getName() {
        return "Graph hierarchy builder";
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#getVersion()
     */
    @Override
    public String getVersion() {
        return "10.06.2008";
    }

    @Override
    public String getId() {
        return "buildhierarchy";
    }


    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#requiresIndexedGraph()
     */
    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    public String[] requiresValidators() {
        return new String[0];
    }

}
