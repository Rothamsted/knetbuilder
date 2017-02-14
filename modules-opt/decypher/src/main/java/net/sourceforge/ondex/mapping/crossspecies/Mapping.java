package net.sourceforge.ondex.mapping.crossspecies;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.*;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.NullValueEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.mapping.ONDEXMapping;
import net.sourceforge.ondex.programcalls.Match;
import net.sourceforge.ondex.programcalls.decypher.DecypherAlignment;

import java.util.*;

/**
 * This plugin is not intended for generic usage!
 * Implements a decypher blast based mapping of one conceptClass sequence type to another (or the same).
 *
 * @author hindlem
 */
@Authors(authors = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
@Custodians(custodians = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
@Status(status = StatusType.EXPERIMENTAL)
public class Mapping extends ONDEXMapping implements ArgumentNames, MetaData {

    private static Mapping instance; //used for logging

    /**
     * NB: Overrides previous instance logging
     */
    public Mapping() {
        instance = this;
    }

    private static final char DELIM = ':'; //used to construct unique keys
    private static final String AA = "AA";
    private static final String NA = "NA";

    public String getName() {
        return "Cross species sequence mapping";
    }

    public String getVersion() {
        return new String("29.01.2008");
    }

    @Override
    public String getId() {
        return "crossspecies";
    }


    /**
     * @author hindlem
     */
    enum ComparisonType {
        COVERAGE_TARGET_SEQUENCE,
        COVERAGE_QUERY_SEQUENCE,
        COVERAGE_SHORTEST_SEQUENCE,
        COVERAGE_LONGEST_SEQUENCE,
        BITSCORE,
        OVERLAP,
        EVALUE;

        /**
         * @param name
         */
        public static ComparisonType getType(String name) {
            if (name.equalsIgnoreCase("coverage_target")) {
                return COVERAGE_TARGET_SEQUENCE;
            } else if (name.equalsIgnoreCase("coverage_query")) {
                return COVERAGE_QUERY_SEQUENCE;
            } else if (name.equalsIgnoreCase("coverage_shortest")) {
                return COVERAGE_SHORTEST_SEQUENCE;
            } else if (name.equalsIgnoreCase("coverage_longest")) {
                return COVERAGE_LONGEST_SEQUENCE;
            } else if (name.equalsIgnoreCase("bitscore")) {
                return BITSCORE;
            } else if (name.equalsIgnoreCase("overlap")) {
                return OVERLAP;
            } else if (name.equalsIgnoreCase("evalue")) {
                return EVALUE;
            }
            throw new RuntimeException("type " + name + " unknown as alignment comparison type");
        }
    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {

        HashSet<ArgumentDefinition<?>> extendedDefinition = new HashSet<ArgumentDefinition<?>>();

        //Add blast program params
        extendedDefinition.add(new RangeArgumentDefinition<Float>(E_VALUE_ARG, E_VALUE_DESC, true, 0.000001F, Float.MIN_VALUE, Float.MAX_VALUE, Float.class));
        extendedDefinition.add(new SequenceTypeArgumentDefinition(QUERY_SEQ_TYPE_ARG, true, ATT_AMINO_ACID_SEQ));
        extendedDefinition.add(new SequenceTypeArgumentDefinition(TARGET_SEQ_TYPE_ARG, true, ATT_AMINO_ACID_SEQ));
        extendedDefinition.add(new FileArgumentDefinition(PROGRAM_DIR_ARG, PROGRAM_DIR_DESC, true, true, true));
//		extendedDefinition.add(new IntegerRangeArgumentDefinition(PHMEMORY_ARG, PHMEMORY_ARG_DESC,false, 500, 0, Integer.MAX_VALUE));
//		extendedDefinition.add(new IntegerRangeArgumentDefinition(PROCESSORS_ARG, false, 1, 1, Integer.MAX_VALUE));
        extendedDefinition.add(new RangeArgumentDefinition<Float>(OVERLAP_ARG, OVERLAP_DESC, false, 0.25F, 0f, Float.MAX_VALUE, Float.class));
        extendedDefinition.add(new RangeArgumentDefinition<Integer>(ALIGNMENTS_CUTOFF_ARG, ALIGNMENTS_CUTOFF_ARG, false, 5, 0, Integer.MAX_VALUE, Integer.class));

        //Add additional Arguments here
        extendedDefinition.add(new StringArgumentDefinition(QUERY_CC_ARG, QUERY_CC_DESC, false, null, false));
        extendedDefinition.add(new StringArgumentDefinition(TARGET_CC_ARG, TARGET_CC_DESC, false, null, false));
        extendedDefinition.add(new BooleanArgumentDefinition(ENTRY_POINT_IS_TAXID_ARG, ENTRY_POINT_IS_TAXID_DESC, false, true));
        extendedDefinition.add(new BooleanArgumentDefinition(ENTRY_POINT_IS_CV_ARG, ENTRY_POINT_IS_CV_DESC, false, false));
        extendedDefinition.add(new BooleanArgumentDefinition(PER_SPECIES_BLAST_ARG, PER_SPECIES_BLAST_ARG_DESC, false, true));
        extendedDefinition.add(new RangeArgumentDefinition<Integer>(ALIGNMENT_THRESHOLD_ON_ENTRY_POINT_ARG, ALIGNMENT_THRESHOLD_ON_ENTRY_POINT_DESC, false, 1, -1, Integer.MAX_VALUE, Integer.class));
        extendedDefinition.add(new StringArgumentDefinition(ALIGNMENT_TYPE_ARG, ALIGNMENT_TYPE_DESC, true, "bitscore", false));
        extendedDefinition.add(new StringArgumentDefinition(QUERY_TAXID_ARG, QUERY_TAXID_ARG, true, "3702", false));
        extendedDefinition.add(new RangeArgumentDefinition<Integer>(BITSCORE_ARG, BITSCORE_DESC, true, 0, 0, Integer.MAX_VALUE, Integer.class));
        extendedDefinition.add(new RangeArgumentDefinition<Integer>(ALINGMENTS_PER_QUERY_ARG, ALINGMENTS_PER_QUERY_DESC, false, 60, 0, Integer.MAX_VALUE, Integer.class));
        extendedDefinition.add(new RangeArgumentDefinition<Integer>(SPECIES_SEQUENCE_SIZE_ARG,
                SPECIES_SEQUENCE_SIZE_ARG_DESC, false, 2, 0, Integer.MAX_VALUE, Integer.class));

        return extendedDefinition.toArray(new ArgumentDefinition<?>[extendedDefinition.size()]);
    }

    @Override
    public void start() throws Exception {

        String querySequenceType = (String) args.getUniqueValue(QUERY_SEQ_TYPE_ARG);
        String targetSequenceType = (String) args.getUniqueValue(TARGET_SEQ_TYPE_ARG);

        AttributeName queryAtt = graph.getMetaData().getAttributeName(querySequenceType);
        AttributeName targetAtt = graph.getMetaData().getAttributeName(targetSequenceType);
        AttributeName taxId = graph.getMetaData().getAttributeName(ATT_TAXID);

        ComparisonType comparisonType = ComparisonType.getType((String) args.getUniqueValue(ALIGNMENT_TYPE_ARG));


        int queryTaxID = -1;

        if (args.getUniqueValue(QUERY_TAXID_ARG) != null) {
            if (args.getUniqueValue(QUERY_TAXID_ARG) instanceof String) {
                queryTaxID = Integer.parseInt((String) args.getUniqueValue(QUERY_TAXID_ARG));
            } else {
                queryTaxID = Integer.parseInt(args.getUniqueValue(QUERY_TAXID_ARG).toString());
            }
        }


        boolean entryPointIsTAXID = (Boolean) args.getUniqueValue(ENTRY_POINT_IS_TAXID_ARG);
        boolean entryPointIsCV = (Boolean) args.getUniqueValue(ENTRY_POINT_IS_CV_ARG);
        boolean perSpeciesBlast = (Boolean) args.getUniqueValue(PER_SPECIES_BLAST_ARG);


        if (!entryPointIsTAXID && !entryPointIsCV) {
            fireEventOccurred(new WrongParameterEvent("No valid entry point specified aborting " + this.getName(), getCurrentMethodName()));
            return;
        }

        ConceptClass ccQuery = null;
        {
            String ccQName = (String) args.getUniqueValue(QUERY_CC_ARG);
            if (ccQName != null) {
                ccQuery = graph.getMetaData()
                        .getConceptClass(ccQName);
                if (ccQuery == null) {
                    fireEventOccurred(new WrongParameterEvent("Concept Class "
                            + ccQName + " for " + QUERY_CC_ARG
                            + " is not a valid concept class",
                            getCurrentMethodName()));
                    return;
                }
            }
        }


        //get CC restriction for Target if any
        ConceptClass ccTarget = null;
        {
            String ccTName = (String) args.getUniqueValue(TARGET_CC_ARG);
            if (ccTName != null) {
                ccTarget = graph.getMetaData().getConceptClass(ccTName);
                if (ccTarget == null) {
                    fireEventOccurred(new WrongParameterEvent("Concept Class "
                            + ccTName + " for " + TARGET_CC_ARG
                            + " is not a valid concept class",
                            getCurrentMethodName()));
                    return;
                }
            }
        }

        Set<ONDEXConcept> potentTargets = getSet(graph, ccTarget, targetAtt, taxId);

        if (queryTaxID > -1) {
            potentTargets = excludeTaxId(graph,
                    potentTargets,
                    taxId,
                    queryTaxID);
        }

        fireEventOccurred(new GeneralOutputEvent(potentTargets.size() + " potentiol target concepts found", getCurrentMethodName()));


        //index for taxid -> set of conceptIDs
        Map<Integer, Set<Integer>> index = new HashMap<Integer, Set<Integer>>();

        System.out.println("Creating index"); //of all TaxIDs and their assigned concepts

        for (ONDEXConcept concept : potentTargets) {

            Attribute attribute = concept.getAttribute(taxId);
            if (attribute != null) {
                String value = (String) attribute.getValue();
                int taxIdVal = Integer.valueOf(value);

                Set<Integer> setCon = index.get(taxIdVal);
                if (setCon == null) {
                    setCon = new HashSet<Integer>();
                    index.put(taxIdVal, setCon);
                }
                setCon.add(concept.getId());
            }
        }

        Integer speciesSeqSize = (Integer) args.getUniqueValue(SPECIES_SEQUENCE_SIZE_ARG);

        //filter out the databases too small to consider
        for (Integer taxIdVals : index.keySet()) {
            Set<Integer> sequences = index.get(taxIdVals);
            if (sequences.size() < speciesSeqSize) {
                index.remove(taxIdVals);
            }
        }

        System.out.println("Cross species ortholog prediction found " + index.size() + " qualifing species in the database");

        BitSet sbs = new BitSet();

        Set<ONDEXConcept> queryConcepts;

        if (queryTaxID > -1) { //if taxid specified
            queryConcepts = getSet(graph, ccQuery, queryAtt, taxId);
        } else { //else use all taxids of conceptclass sequence type combo
            queryConcepts = getSet(graph, ccQuery, queryAtt, null);
        }

        for (ONDEXConcept queryConcept : queryConcepts) {
            Attribute attribute = queryConcept.getAttribute(taxId);

            int taxid = -1;

            if (queryTaxID > -1) {
                Object value = attribute.getValue();

                //allow for integer or string objects
                if (value instanceof String) {
                    taxid = Integer.parseInt((String) value);
                } else {
                    taxid = (Integer) value;
                }

                if (queryTaxID != taxid) {
                    continue;
                }

            }
            sbs.set(queryConcept.getId());

        }

        queryConcepts = BitSetFunctions.create(graph, ONDEXConcept.class, sbs);

        fireEventOccurred(new GeneralOutputEvent(queryConcepts.size() + " total query concepts found", getCurrentMethodName()));

        float evalue = ((Number) args.getUniqueValue(E_VALUE_ARG)).floatValue();
        Integer alignments_cutoff = (Integer) args.getUniqueValue(ALIGNMENTS_CUTOFF_ARG);
        float overlap = ((Number) args.getUniqueValue(OVERLAP_ARG)).floatValue();
        String programDir = (String) args.getUniqueValue(PROGRAM_DIR_ARG);
//		the number of top alignments
        int numberOfAlignments = (Integer) args.getUniqueValue(ALIGNMENT_THRESHOLD_ON_ENTRY_POINT_ARG);
        int bitscore = (Integer) args.getUniqueValue(BITSCORE_ARG);
        int alignments_per_query = (Integer) args.getUniqueValue(ALINGMENTS_PER_QUERY_ARG);

        int spec = 0;
        int allSpecies = index.keySet().size();

        if (perSpeciesBlast) { //per species blast

            for (Integer species : index.keySet()) {
                spec++;
                Set<ONDEXConcept> targets = BitSetFunctions.create(graph, ONDEXConcept.class, index.get(species));

                System.out.println("Running DeCycpher on TAXID:" + species + " with " + targets.size() + " sequences/ species " + spec + " out of " + allSpecies);

                Map<Integer, List<Match>> results = null;
                try {
                    results = runDecypher(
                            programDir,
                            overlap,
                            bitscore,
                            alignments_cutoff,
                            alignments_per_query,
                            evalue,
                            queryTaxID,
                            querySequenceType, targetSequenceType, queryConcepts, targets, comparisonType);
                    processDecypherResults(results, numberOfAlignments,
                            entryPointIsTAXID,
                            entryPointIsCV,
                            comparisonType);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        } else {

            Set<Integer> allSequences = new HashSet<Integer>(index.keySet().size() * speciesSeqSize);

            Collection<Set<Integer>> sequences = index.values();
            for (Set<Integer> speciesSeqSet : sequences) {
                allSequences.addAll(speciesSeqSet);
            }

            System.out.println("Running DeCycpher on " + index.keySet().size() + " species (all at once) with " + sequences.size() + " sequences");

            Set<ONDEXConcept> targets = BitSetFunctions.create(graph, ONDEXConcept.class, allSequences);


            Map<Integer, List<Match>> results = runDecypher(
                    programDir,
                    overlap,
                    bitscore,
                    alignments_cutoff,
                    alignments_per_query,
                    evalue,
                    queryTaxID,
                    querySequenceType, targetSequenceType, queryConcepts, targets, comparisonType);
            processDecypherResults(results, numberOfAlignments,
                    entryPointIsTAXID,
                    entryPointIsCV,
                    comparisonType);

        }
    }

    /**
     * @param programDir         the decypher bin directory
     * @param overlap            the min % of overlap that the query has with the target
     * @param bitscoreCutoff     the bitscore cut-off
     * @param evalue             the maximum e-value to tolerate
     * @param queryTaxID         the taxid of the queryid (use -1 if none)
     * @param querySequenceType  AA/NA?
     * @param targetSequenceType AA/NA?
     * @param queryConcepts      all concept to query on
     * @param targets            all targets to query on
     * @param comparisonType     the score to pre-sort on
     * @return map of ondex concept id (query), to matches sorted on comparison type
     */
    private Map<Integer, List<Match>> runDecypher(String programDir,
                                                  float overlap,
                                                  int bitscoreCutoff,
                                                  int alignments_cutoff,
                                                  int alignmentsReturned,
                                                  float evalue,
                                                  int queryTaxID,
                                                  String querySequenceType,
                                                  String targetSequenceType,
                                                  Set<ONDEXConcept> queryConcepts,
                                                  Set<ONDEXConcept> targets,
                                                  ComparisonType comparisonType) throws Exception {
        Map<Integer, List<Match>> indexOnQueryId = null;

        fireEventOccurred(new GeneralOutputEvent("Running " + SequenceAlignmentProgramArgumentDefinition.DECYPHER, getCurrentMethodName()));

        DecypherAlignment dcAlign = new DecypherAlignment(
                net.sourceforge.ondex.config.Config.ondexDir,
                programDir,
                alignments_cutoff,
                overlap,
                evalue,
                bitscoreCutoff,
                alignmentsReturned,
                queryTaxID > -1);
        String algo = null;
        if (querySequenceType.equalsIgnoreCase(AA) &&
                targetSequenceType.equalsIgnoreCase(AA)) {
            algo = DecypherAlignment.ALGO_BLASTP;
        } else if (querySequenceType.equalsIgnoreCase(NA) &&
                targetSequenceType.equalsIgnoreCase(NA)) {
            algo = DecypherAlignment.ALGO_BLASTN;
        } else if (querySequenceType.equalsIgnoreCase(NA) &&
                targetSequenceType.equalsIgnoreCase(AA)) {
            algo = DecypherAlignment.ALGO_BLASTX;
        } else if (querySequenceType.equalsIgnoreCase(NA) &&
                targetSequenceType.equalsIgnoreCase(AA)) {
            algo = DecypherAlignment.ALGO_TBLASTN;
        } else {
            fireEventOccurred(new WrongParameterEvent("Selected Program can not Align these types of sequences :" + querySequenceType + "->" + targetSequenceType, getCurrentMethodName()));
        }
        indexOnQueryId = new HashMap<Integer, List<Match>>();

        Collection<Match> resultList = dcAlign.query(graph, queryConcepts, targets, algo);

        if (resultList != null) {

            // first added matches indexed by queryId
            for (Match m : resultList) {
                if (!indexOnQueryId.containsKey(m.getQueryId()))
                    indexOnQueryId.put(m.getQueryId(), new ArrayList<Match>());
                indexOnQueryId.get(m.getQueryId()).add(m);
            }

            // second sort matches for each queryId
            for (Integer id : indexOnQueryId.keySet()) {
                List<Match> list = indexOnQueryId.get(id);
                // convert to array for faster sorting
                Match[] array = list.toArray(new Match[list.size()]);
                // sorting here
                Arrays.sort(array, new SequenceSorter(comparisonType));
                // re-add sorted list
                indexOnQueryId.put(id, Arrays.asList(array));
            }
        }


        return indexOnQueryId;
    }

    /**
     * Processes the decypher matches and creates Ondex relations on them
     *
     * @param indexOnQueryId     the results from runDecypher query id (ondex concept id) to decypher matches
     * @param numberOfAlignments the number of 'best alignments' to keep
     * @param entryPointIsTAXID  one best alignment per species?
     * @param entryPointIsCV     one best alignment per cv?
     * @param comparisonType     the alignment score to allign on
     */
    private void processDecypherResults(
            Map<Integer, List<Match>> indexOnQueryId,
            int numberOfAlignments,
            boolean entryPointIsTAXID,
            boolean entryPointIsCV,
            ComparisonType comparisonType) {
        System.out.println("Building index on " + indexOnQueryId.size() + " concepts");

        //buffer for constructing uniqueID
        StringBuilder id = new StringBuilder(15);

        //hash for counting groups
        Map<String, Integer> groupsCount = new HashMap<String, Integer>(5);

        Iterator<Integer> it = indexOnQueryId.keySet().iterator();
        while (it.hasNext()) {
            int queryId = it.next();
            List<Match> hits = indexOnQueryId.get(queryId);

            //The results on this match go here
            Map<String, List<Match>> entryPoint2topScores = new HashMap<String, List<Match>>();

//			Add all the alignment matching hits
            if (numberOfAlignments > 0 || numberOfAlignments == -1) {

                groupsCount.clear();

                Iterator<Match> hitIt = hits.iterator();
                while (hitIt.hasNext()) {
                    Match hit = hitIt.next();

                    if (queryId == hit.getTargetId()) {
                        continue;
                    }

                    if (entryPointIsTAXID) {
                        id.append(hit.getTargetTaxId());
                    }

                    if (entryPointIsCV) {
                        if (id.length() > 0) {
                            id.append(DELIM);
                        }
                        id.append(graph.getConcept(hit.getTargetId()).getElementOf());
                    }

                    String uniqueID = id.toString();
                    id.setLength(0);

                    List<Match> entryPointHits = entryPoint2topScores.get(uniqueID);

                    if (entryPointHits == null) {
                        entryPointHits = new ArrayList<Match>();
                        entryPoint2topScores.put(uniqueID, entryPointHits);
                        groupsCount.put(uniqueID, 0);
                    }

                    //check if replicates in collection
                    if (entryPointHits.contains(hit)) {
                        continue;
                    }

                    int groups = groupsCount.get(uniqueID);

                    boolean groupTogether = false;

                    if (entryPointHits.size() > 0) {
                        double last = getComparisonValue(entryPointHits.get(entryPointHits.size() - 1), comparisonType);
                        groupTogether = (last == getComparisonValue(hit, comparisonType));
                    }

                    if (groups < numberOfAlignments || numberOfAlignments == -1 || groupTogether) {
                        entryPointHits.add(hit);
                        if (!groupTogether) {
                            groupsCount.put(uniqueID, groups + 1);
                        }
                    }
                }
            }

            hits = null; // remove from memory
            it.remove();// remove from memory

            ONDEXConcept fromConcept = null;
            try {
                fromConcept = graph.getConcept(queryId);
            } catch (NumberFormatException e) {
                fireEventOccurred(new NullValueEvent("Concept id match is not recognized " + queryId, getCurrentMethodName()));
                continue;
            }

            GenericBLASTMatchWriter writer = new GenericBLASTMatchWriter(graph);

            //Add for each taxId the sum of qualifing hits
            Iterator<String> entryPointIds = entryPoint2topScores.keySet().iterator();
            while (entryPointIds.hasNext()) {
                String entryPointId = entryPointIds.next();
                List<Match> conceptIdsToAdd = entryPoint2topScores.get(entryPointId);

                if (DEBUG)
                    System.out.println("Found " + conceptIdsToAdd.size() + " hss for " + entryPointId + " on " + fromConcept.getPID());

                writer.addHitsToONDEXGraph(conceptIdsToAdd, fromConcept);
            }
        }
    }

    /**
     * @param hit
     * @param comparisonType
     * @return a comparison value where higher is always better
     */
    private double getComparisonValue(Match hit, ComparisonType comparisonType) {
        if (comparisonType.equals(ComparisonType.COVERAGE_SHORTEST_SEQUENCE)) {
            return hit.getCoverageSmallestSequence();
        } else if (comparisonType.equals(ComparisonType.COVERAGE_LONGEST_SEQUENCE)) {
            return hit.getCoverageLongestSequence();
        } else if (comparisonType.equals(ComparisonType.BITSCORE)) {
            return hit.getScore();
        } else if (comparisonType.equals(ComparisonType.COVERAGE_QUERY_SEQUENCE)) {
            return hit.geQueryCoverageSequence();
        } else if (comparisonType.equals(ComparisonType.COVERAGE_TARGET_SEQUENCE)) {
            return hit.geTargetCoverageSequence();
        } else if (comparisonType.equals(ComparisonType.OVERLAP)) {
            return hit.getOverlapingLength();
        } else if (comparisonType.equals(ComparisonType.EVALUE)) {
            return 1d - hit.getEValue();
        }
        throw new RuntimeException(comparisonType + ": comparison type unknown");
    }

    /**
     * returns a view of concepts that meet both criteria
     *
     * @param graph the currentGraph
     * @param taxId
     * @return concept that are of specified cc and have the specified att
     */
    private Set<ONDEXConcept> getSet(ONDEXGraph graph, ConceptClass ccName, AttributeName sequenceTypeName, AttributeName taxId) {

        Set<ONDEXConcept> concepts = BitSetFunctions.copy(graph.getConceptsOfAttributeName(sequenceTypeName));
        fireEventOccurred(new GeneralOutputEvent(concepts.size() + " concepts found with att " + sequenceTypeName.getId(), getCurrentMethodName()));

        if (taxId != null) {
            Set<ONDEXConcept> taxConcepts = graph.getConceptsOfAttributeName(taxId);
            fireEventOccurred(new GeneralOutputEvent(taxConcepts.size() + " concepts found with taxId ", getCurrentMethodName()));

            concepts.retainAll(taxConcepts);
        }

        if (ccName != null) {
            concepts.retainAll(graph.getConceptsOfConceptClass(ccName));
            fireEventOccurred(new GeneralOutputEvent(concepts.size() + " concepts found with att " + sequenceTypeName.getId() + " and taxId and cc " + ccName.getId(), getCurrentMethodName()));
        }

        return concepts;
    }

    /**
     * @param graph the currentGraph
     * @param view  and ondexView
     * @return an att value to concept index
     */
    private Set<ONDEXConcept> excludeTaxId(
            ONDEXGraph graph,
            Set<ONDEXConcept> view,
            AttributeName taxId,
            int taxid) {

        BitSet sbs = new BitSet();

        Set<ONDEXConcept> taxIDName = graph.getConceptsOfAttributeName(taxId);
        Set<ONDEXConcept> newView = BitSetFunctions.copy(view);
        newView.retainAll(taxIDName);

        for (ONDEXConcept queryConcept : newView) {
            Attribute attribute = queryConcept.getAttribute(taxId);
            if (attribute != null) {
                Object value = attribute.getValue();
                int id;
                //allow for integer or string objects
                if (value instanceof String) {
                    id = Integer.parseInt((String) value);
                } else {
                    id = (Integer) value;
                }
                if (id != taxid) {
                    sbs.set(queryConcept.getId());
                }
            }
        }
        return BitSetFunctions.create(graph, ONDEXConcept.class, sbs);
    }

    /**
     * @author hindlem
     */
    class SequenceSorter implements Comparator<Match> {

        private ComparisonType type;

        public SequenceSorter(ComparisonType type) {
            this.type = type;
        }

        public int compare(Match o1, Match o2) {
            return (int) (getComparisonValue(o2, type) - getComparisonValue(o1, type));
        }
    }


    public boolean requiresIndexedGraph() {
        return false;
    }

    /**
     * Convenience method for outputing the current method name in a dynamic way
     *
     * @return the calling method name
     */
    public static String getCurrentMethodName() {
        Exception e = new Exception();
        StackTraceElement trace = e.fillInStackTrace().getStackTrace()[1];
        String name = trace.getMethodName();
        String className = trace.getClassName();
        int line = trace.getLineNumber();
        return "[CLASS:" + className + " - METHOD:" + name + " LINE:" + line + "]";
    }

    public static void propagateEventOccurred(EventType et) {
        if (instance != null)
            instance.fireEventOccurred(et);
    }

    public String[] requiresValidators() {
        return new String[0];
    }
}
