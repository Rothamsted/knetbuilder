package net.sourceforge.ondex.filter.pfambasedortholog;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.algorithm.annotationquality.GOTreeParser;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.InconsistencyEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;

/**
 * The Pfam-based ortholg filter is designed to be executed on a graph that contains
 * two proteomes or more, that have orthologs mapped via inparanoid with a very low threshold
 * and that are also mapped to their protein families via the sequence2pfam-mapping.
 * The filter then removes all orthology-relations that are probably wrong. To determine this
 * it uses three different criteria:
 * - Number of shared protein families
 * - share of related GO terms
 * - original inparanoid confidence score
 * Either the first two values or the third value must exceed the thresholds given by the user.
 *
 * @author Jochen Weile, B.Sc.
 */
public class Filter extends ONDEXFilter implements ArgumentNames {


    //####FIELDS####

    /**
     * debug flag.
     */
    private final boolean DEBUG = false;

//	/**
//	 * a vector of all taxids to be examined
//	 */
//	private Vector<Integer> taxids;

    /**
     * All required concept classes.
     */
    private ConceptClass cc_protFam, cc_protein;

    /**
     * All required RTSs.
     */
    private RelationType rts_seq2pfam, rts_ortholog;

    /**
     * All required AttributeNames.
     */
    private AttributeName an_taxid, an_pfamhits, an_goscore, an_conf;

    /**
     * All required CVs.
     */
    private DataSource dataSource_go;

    /**
     * A set containing the ids of all ortholog relations that will be kept by the filter.
     */
    private Set<Integer> seqSimRelationsToKeep;

    /**
     * Contains the complete GO database structure.
     */
    private GOTreeParser goIndex;

    /**
     * The GO score threshold.
     */
    private double go_threshold;

    /**
     * The Pfam intersection size threshold.
     */
    private int intersection_threshold;

    /**
     * The inparanoid confidence threshold.
     */
    private int confidence_threshold;

    /**
     * A cutoff for the goterm depth computation.
     * The breadth first search terminates at the given depth.
     */
    private int depth_cutoff;


    /**
     * Constants for the different scoring models.
     */
    private static final int SCOREMODE_MEAN = 0, SCOREMODE_MAX = 1, SCOREMODE_MIN = 2;

    /**
     * The currently selected scoring model.
     */
    private int scoreMode = SCOREMODE_MEAN;


    //####CONSTRUCTOR####

    /**
     * The constructor
     */
    public Filter() {
    }


    //####METHODS####

    /**
     * @see net.sourceforge.ondex.filter.ONDEXFilter#start()
     */
    @Override
    public void start() throws InvalidPluginArgumentException {
        System.out.println("fetching meta data...");
        boolean continueRun = fetchMetaData();


        if (continueRun) {
            System.out.println("checking graph requirements...");
            continueRun = checkGraphRequirements();
        }


        if (continueRun) {
            System.out.println("fetching arguments...");
//			taxids = new Vector<Integer>();
            continueRun = fetchArguments();
        }

        if (continueRun) {

            seqSimRelationsToKeep = new HashSet<Integer>();

            if (DEBUG) printNumPfamMappings();

            System.out.println("running filter...");
            filterOutFalseOrthologs();
            System.out.println("done!");
        }

    }

    /**
     * Debug output method.
     */
    private void printNumPfamMappings() {
        Set<ONDEXRelation> v = graph.getRelationsOfRelationType(rts_seq2pfam);
        System.out.println("Number of pfam mappings: " + v.size());
    }

    /**
     * the actual filter algorithm.
     */
    private void filterOutFalseOrthologs() {
        for (ONDEXRelation ortho_relation : graph.getRelationsOfRelationType(rts_ortholog)) {
            ONDEXConcept p1 = ortho_relation.getFromConcept();
            ONDEXConcept p2 = ortho_relation.getToConcept();
            HashSet<ONDEXConcept> fromSet = getConnectedPfams(p1);
            HashSet<ONDEXConcept> toSet = getConnectedPfams(p2);
            int pfamMatchScore = intersectSets(fromSet, toSet).size();
            if (pfamMatchScore >= intersection_threshold) {
                double goScore = calcAnnotationScore(p1, p2);
                if (goScore >= go_threshold) {
                    seqSimRelationsToKeep.add(ortho_relation.getId());
                    Attribute attribute = ortho_relation.getAttribute(an_goscore);
                    if (attribute == null)
                        ortho_relation.createAttribute(an_goscore, goScore, false);
                    else
                        attribute.setValue(goScore);
                    attribute = ortho_relation.getAttribute(an_pfamhits);
                    if (attribute == null)
                        ortho_relation.createAttribute(an_pfamhits, pfamMatchScore, false);
                    else
                        attribute.setValue(pfamMatchScore);

                    if (DEBUG) System.out.println("match found: id" + ortho_relation.getId() +
                            " GO score: " + goScore +
                            " Pfam hits: " + pfamMatchScore);
                }
            } else {//second chance
                Attribute attribute_conf = ortho_relation.getAttribute(an_conf);
                if (attribute_conf != null) {
                    double conf_curr = (Double) attribute_conf.getValue();
                    if (conf_curr > confidence_threshold) {
                        if (DEBUG) System.out.println("Taken by second chance: " + ortho_relation.getId() +
                                " conf: " + conf_curr);
                        seqSimRelationsToKeep.add(ortho_relation.getId());
                    }
                }
            }
        }
    }

    /**
     * returns a hashset with the protein families that are connected with the
     * given concept.
     *
     * @param c the concept
     * @return a hashset of pfams that are connected
     */
    private HashSet<ONDEXConcept> getConnectedPfams(ONDEXConcept c) {
        HashSet<ONDEXConcept> set = new HashSet<ONDEXConcept>();
        for (ONDEXRelation inOrOutRelation : graph.getRelationsOfConcept(c)) {
            if (inOrOutRelation.getOfType().equals(rts_seq2pfam)) {
                ONDEXConcept otherEnd = (inOrOutRelation.getFromConcept().equals(c)) ?
                        inOrOutRelation.getToConcept() :
                        inOrOutRelation.getFromConcept();
                if (otherEnd.getOfType().equals(cc_protFam)) {
                    set.add(otherEnd);
                }
            }
        }
        return set;
    }

    /**
     * intersects the two given hashsets.
     *
     * @param set1 one set
     * @param set2 another set
     * @return a set with all elements that occurr in both input sets.
     */
    private HashSet<ONDEXConcept> intersectSets(HashSet<ONDEXConcept> set1, HashSet<ONDEXConcept> set2) {
        HashSet<ONDEXConcept> setOut = new HashSet<ONDEXConcept>();
        for (ONDEXConcept c : set1) {
            if (set2.contains(c))
                setOut.add(c);
        }
        return setOut;
    }

    /**
     * calculates the GO annotation match score for both given concepts.
     * the score is the computed according to the selected scoring model;
     * so it is either a maximzation, minimazation or the average of the
     * three scores for the three different GO namespaces. The number is basically
     * the share of similar terms in the namespace.
     *
     * @param p1 a concept.
     * @param p2 another concept.
     * @return the GO annotation match score.
     */
    private double calcAnnotationScore(ONDEXConcept p1, ONDEXConcept p2) {
        Set<Integer> goTerms1 = getNonRedundantGoaForProtein(p1);
        Set<Integer> goTerms2 = getNonRedundantGoaForProtein(p2);
        if ((!goTerms1.isEmpty()) && (!goTerms2.isEmpty())) {
            Integer[][] termGroup1 = groupTerms(goTerms1);
            Integer[][] termGroup2 = groupTerms(goTerms2);
            double[] scores = computeSingleScores(termGroup1, termGroup2);
            switch (scoreMode) {
                case SCOREMODE_MEAN:
                    return (scores[0] + scores[1] + scores[2]) / 3.0;
                case SCOREMODE_MIN:
                    double min = Double.POSITIVE_INFINITY;
                    for (int i = 0; i < 3; i++)
                        if (scores[i] < min) min = scores[i];
                    return min;
                case SCOREMODE_MAX:
                    double max = Double.NEGATIVE_INFINITY;
                    for (int i = 0; i < 3; i++)
                        if (scores[i] > max) min = scores[i]; // fixme: shouldn't this me max= ?
                    return max;
            }
        }
        return 1.0;
    }

    /**
     * returns a set of nonredundant GO annotations for a given concept.
     * nonredundant means that none of the yielded terms are subterms of another.
     *
     * @param prot the query concept
     * @return a set of goterm ids.
     */
    private Set<Integer> getNonRedundantGoaForProtein(ONDEXConcept prot) {
        HashSet<Integer> goaVec = new HashSet<Integer>();
        for (ConceptAccession acc : prot.getConceptAccessions()) {
            if (acc.getElementOf().equals(dataSource_go)) {
                int goInt = Integer.parseInt(acc.getAccession().split(":")[1]);
                goaVec.add(goInt);
            }
        }
        Iterator<Integer> goa_it = goaVec.iterator();
        int[] goas = new int[goaVec.size()];
        for (int i = 0; i < goas.length; i++)
            goas[i] = goa_it.next();

        HashSet<Integer> out = new HashSet<Integer>();

        for (int i = 1; i < goas.length; i++) {
            if (goIndex.getEntry(goas[i]) == null) {
                out.add(goas[i]);
                continue;
            }
            for (int j = 0; j < i; j++) {
                if (goIndex.getEntry(goas[j]) == null) {
                    out.add(goas[j]);
                    continue;
                }
                int dist_fwd = goIndex.getDirectedDistanceBetweenTerms(goas[i], goas[j], Integer.MAX_VALUE);
                int dist_bck = goIndex.getDirectedDistanceBetweenTerms(goas[j], goas[i], Integer.MAX_VALUE);
                if (dist_fwd > -1)
                    out.add(goas[j]);
                else if (dist_bck > -1)
                    out.add(goas[i]);
            }
        }

        goaVec.removeAll(out);

        return goaVec;
    }

    /**
     * groups the set of go terms into their three different namespaces.
     *
     * @param goTerms a set of go terms.
     * @return a 2D array of go terms, the first dimension is for the namespace, the second for the term.
     */
    private Integer[][] groupTerms(Set<Integer> goTerms) {
        Vector<Integer> set0 = new Vector<Integer>();
        Vector<Integer> set1 = new Vector<Integer>();
        Vector<Integer> set2 = new Vector<Integer>();
        for (Integer i : goTerms) {
            int domain = goIndex.getEntry(i).getDomain();
            if (domain == 0)
                set0.add(i);
            else if (domain == 1)
                set1.add(i);
            else if (domain == 2)
                set2.add(i);
        }

        Integer[][] sets = new Integer[3][];
        sets[0] = set0.toArray(new Integer[set0.size()]);
        sets[1] = set1.toArray(new Integer[set1.size()]);
        sets[2] = set2.toArray(new Integer[set2.size()]);

        return sets;
    }

    /**
     * computes the three single go match scores of two term sets for the three different go namespaces.
     *
     * @param sets1 a 2d array holding a grouped set of go terms
     * @param sets2 another 2d array holding a grouped set of go terms
     * @return a double array with the three scores.
     */
    private double[] computeSingleScores(Integer[][] sets1, Integer[][] sets2) {
        double[] scores = new double[3];
        for (int i = 0; i < 3; i++) {
            int min_entries = (sets1[i].length < sets2[i].length) ? sets1[i].length : sets2[i].length;
            if (min_entries == 0) {
                scores[i] = 1.0;
                continue;
            }
            int hits = 0;
            for (int j = 0; j < sets1[i].length; j++) {
                for (int k = 0; k < sets2[i].length; k++) {
                    int d = goIndex.getDistanceBetweenTerms(sets1[i][j], sets2[i][k], depth_cutoff);
                    if (d > -1)
                        hits++;
                    if (hits == min_entries)
                        break;
                }
                if (hits == min_entries)
                    break;
            }
            scores[i] = ((double) hits) / ((double) min_entries);
        }
        return scores;
    }

    /**
     * loads the required metadata
     *
     * @return if everything is ok
     */
    private boolean fetchMetaData() {
        boolean continueRun = true;

        //ConceptClasses
        cc_protein = graph.getMetaData().getConceptClass("Protein");
        if (cc_protein == null) {
            continueRun = false;
            fireEventOccurred(new ConceptClassMissingEvent("Missing ConceptClass: Protein", getName()));
        }

        cc_protFam = graph.getMetaData().getConceptClass("ProtDomain");
        if (cc_protFam == null) {
            continueRun = false;
            fireEventOccurred(new ConceptClassMissingEvent("Missing ConceptClass: Protein Domain", getName()));
        }


        //RelationTypes
        rts_seq2pfam = graph.getMetaData().getRelationType("member_of");
        if (rts_seq2pfam == null) {
            continueRun = false;
            fireEventOccurred(new RelationTypeMissingEvent("Missing RelationType: member_is_part_of", getName()));
        }

        rts_ortholog = graph.getMetaData().getRelationType("ortho");
        if (rts_ortholog == null) {
            continueRun = false;
            fireEventOccurred(new RelationTypeMissingEvent("Missing RelationType: ortholog", getName()));
        }


        //AttributeNames
        an_taxid = graph.getMetaData().getAttributeName("TAXID");
        if (an_taxid == null) {
            continueRun = false;
            fireEventOccurred(new AttributeNameMissingEvent("Missing AttributeName: TAXID", getName()));
        }

        an_pfamhits = graph.getMetaData().getAttributeName("PFAMHITS");
        if (an_pfamhits == null) {
            continueRun = false;
            fireEventOccurred(new AttributeNameMissingEvent("Missing AttributeName: PFAMHITS", getName()));
        }

        an_goscore = graph.getMetaData().getAttributeName("GOMATCHSCORE");
        if (an_goscore == null) {
            continueRun = false;
            fireEventOccurred(new AttributeNameMissingEvent("Missing AttributeName: GOMATCHSCORE", getName()));
        }

        an_conf = graph.getMetaData().getAttributeName("CONF");
        if (an_conf == null) {
            continueRun = false;
            fireEventOccurred(new AttributeNameMissingEvent("Missing AttributeName: CONF", getName()));
        }

        //CVs
        dataSource_go = graph.getMetaData().getDataSource("GO");
        if (dataSource_go == null) {
            continueRun = false;
            fireEventOccurred(new DataSourceMissingEvent("Missing DataSource: GO", getName()));
        }

        return continueRun;
    }

    /**
     * checks all requirements in the graph.
     *
     * @return if everything is ok.
     */
    private boolean checkGraphRequirements() {
        boolean continueRun = true;

        if (graph.getConceptsOfConceptClass(cc_protFam).isEmpty()) {
            continueRun = false;
            fireEventOccurred(new InconsistencyEvent("Graph does not contain protein families.", getName()));
        }

        if (graph.getConceptsOfConceptClass(cc_protein).isEmpty()) {
            continueRun = false;
            fireEventOccurred(new InconsistencyEvent("Graph does not contain proteins.", getName()));
        }

        if (graph.getRelationsOfRelationType(rts_ortholog).isEmpty()) {
            continueRun = false;
            fireEventOccurred(new InconsistencyEvent("Graph does not contain ortholog relations.", getName()));
        }

        if (graph.getRelationsOfRelationType(rts_seq2pfam).isEmpty()) {
            continueRun = false;
            fireEventOccurred(new InconsistencyEvent("Graph does not contain protein to pfam mappings.", getName()));
        }


        return continueRun;
    }

    /**
     * loads the filter arguments.
     *
     * @return if everything is ok.
     */
    private boolean fetchArguments() throws InvalidPluginArgumentException {
        boolean continueRun = true;
//		Object[] os;
        Object o;
        int i;
        double d;

//		os = args.getObjectValueArray(TAXID_ARG);
//		for (Object o_i : os) {
//			if (o_i instanceof String) {
//				try {
//					i = Integer.parseInt((String)o_i);
//					taxids.add(i);
//				} catch (NumberFormatException nfe) {
//					continueRun = false;
//					fireEventOccurred(new WrongParameterEvent("Wrong TaxID parameter: "+o_i, getName()));
//				}
//			}
//			else {
//				continueRun = false;
//				fireEventOccurred(new WrongParameterEvent("Wrong TaxID parameter: "+o_i, getName()));
//			}
//		}

        //conf threshold
        o = args.getUniqueValue(CONF_ARG);
        if (o instanceof String) {
            try {
                i = Integer.parseInt((String) o);
                confidence_threshold = i;
            } catch (NumberFormatException nfe) {
                continueRun = false;
                fireEventOccurred(new WrongParameterEvent("Wrong confidence threshold parameter: " + o, getName()));
            }
        } else {
            continueRun = false;
            fireEventOccurred(new WrongParameterEvent("Wrong confidence threshold parameter: " + o, getName()));
        }

        //threshold
        o = args.getUniqueValue(THRESHOLD_ARG);
        if (o instanceof String) {
            try {
                d = Double.parseDouble((String) o);
                go_threshold = d;
            } catch (NumberFormatException nfe) {
                continueRun = false;
                fireEventOccurred(new WrongParameterEvent("Wrong threshold parameter: " + o, getName()));
            }
        } else {
            continueRun = false;
            fireEventOccurred(new WrongParameterEvent("Wrong threshold parameter: " + o, getName()));
        }

        //cutoff
        o = args.getUniqueValue(INTERSECTION_ARG);
        if (o instanceof String) {
            try {
                i = Integer.parseInt((String) o);
                intersection_threshold = i;
            } catch (NumberFormatException nfe) {
                continueRun = false;
                fireEventOccurred(new WrongParameterEvent("Wrong intersection cutoff parameter: " + o, getName()));
            }
        } else {
            continueRun = false;
            fireEventOccurred(new WrongParameterEvent("Wrong intersection cutoff parameter: " + o, getName()));
        }

        //cutoff
        o = args.getUniqueValue(TERM_DEPTH_CUTOFF);
        if (o instanceof String) {
            try {
                i = Integer.parseInt((String) o);
                depth_cutoff = i;
            } catch (NumberFormatException nfe) {
                continueRun = false;
                fireEventOccurred(new WrongParameterEvent("Wrong term depth cutoff parameter: " + o, getName()));
            }
        } else {
            continueRun = false;
            fireEventOccurred(new WrongParameterEvent("Wrong term depth cutoff parameter: " + o, getName()));
        }

        //gofile
        o = args.getUniqueValue(GOFILE_ARG);
        if (o instanceof String) {
            String fileStr = (String) o;
            File file = new File(fileStr);
            if (!file.isAbsolute())
                fileStr = net.sourceforge.ondex.config.Config.ondexDir + fileStr;
            file = new File(fileStr);
            if (!file.exists()) {
                continueRun = false;
                fireEventOccurred(new WrongParameterEvent("File " + o + "does not exist", getName()));
            } else {
                try {
                    goIndex = new GOTreeParser(fileStr);
                    goIndex.parseOboFile();
                } catch (IOException e) {
                    continueRun = false;
                    fireEventOccurred(new WrongParameterEvent("File " + o + " is corrupt!", getName()));
                }
            }
        } else {
            continueRun = false;
            fireEventOccurred(new WrongParameterEvent("Wrong threshold parameter: " + o, getName()));
        }

        return continueRun;
    }


    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#getArgumentDefinitions()
     */
    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
//				new StringArgumentDefinition(TAXID_ARG, TAXID_ARG_DESC, true, null, true) ,
                new StringArgumentDefinition(CONF_ARG, CONF_ARG_DESC, true, "100", false),
                new StringArgumentDefinition(THRESHOLD_ARG, THRESHOLD_ARG_DESC, true, "0.75", false),
                new StringArgumentDefinition(INTERSECTION_ARG, INTERSECTION_ARG_DESC, true, "2", false),
                new StringArgumentDefinition(TERM_DEPTH_CUTOFF, TERM_DEPTH_CUTOFF_DESC, true, "4", false),
                new StringArgumentDefinition(GOFILE_ARG, GOFILE_ARG_DESC, true, null, false)
        };
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#getName()
     */
    @Override
    public String getName() {
        return "Pfam-based ortholog prediction filter";
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#getVersion()
     */
    @Override
    public String getVersion() {
        return "16.05.2008";
    }

    @Override
    public String getId() {
        return "pfambasedortholog";
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#requiresIndexedGraph()
     */
    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    /**
     * @see net.sourceforge.ondex.filter.ONDEXFilter#copyResultsToNewGraph(net.sourceforge.ondex.core.base.AbstractONDEXGraph)
     */
    @Override
    public void copyResultsToNewGraph(ONDEXGraph exportGraph) {
        ONDEXGraphCloner graphCloner = new ONDEXGraphCloner(graph, exportGraph);
        for (ONDEXConcept c : getVisibleConcepts()) {
            graphCloner.cloneConcept(c);
        }
        for (ONDEXRelation r : getVisibleRelations()) {
            graphCloner.cloneRelation(r);
        }
    }

    /**
     * @see net.sourceforge.ondex.filter.ONDEXFilter#getVisibleConcepts()
     */
    @Override
    public Set<ONDEXConcept> getVisibleConcepts() {
        return graph.getConcepts();
    }

    /**
     * @see net.sourceforge.ondex.filter.ONDEXFilter#getVisibleRelations()
     */
    @Override
    public Set<ONDEXRelation> getVisibleRelations() {
        if (seqSimRelationsToKeep == null)
            return graph.getRelations();
        Set<ONDEXRelation> toKeep = BitSetFunctions.create(graph, ONDEXRelation.class, seqSimRelationsToKeep);
        Set<ONDEXRelation> toDelete = BitSetFunctions.andNot(graph.getRelationsOfRelationType(rts_ortholog), toKeep);
        Set<ONDEXRelation> out = BitSetFunctions.copy(graph.getRelations());
        out.removeAll(toDelete);
        // returns:  (all \ (ortholog \ keep))
        return out;
    }

    public String[] requiresValidators() {
        return new String[0];
    }

}
