package net.sourceforge.ondex.mapping.orthologprediction;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.*;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.*;
import net.sourceforge.ondex.mapping.ONDEXMapping;
import net.sourceforge.ondex.programcalls.Match;
import net.sourceforge.ondex.programcalls.decypher.DecypherAlignment;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Predicts orthologs between species based on a best bidirectional hits.
 *
 * @author hindlem, weilej
 */
@Authors(authors = {"Matthew Hindle", "Jochen Weile"}, emails = {"matthew_hindle at users.sourceforge.net", "jweile at users.sourceforge.net"})
@Custodians(custodians = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
public class Mapping extends ONDEXMapping implements ArgumentNames, MetaData {

    private static final String AA = "AA";
    private static final String NA = "NA";

    private static Mapping instance; //used for logging

    private EvidenceType eviType_blast;
    private AttributeName attN_eValue;
    private AttributeName attN_bitScore;
    private RelationType rtSet_simSeq;
    private AttributeName attN_seqType;
    private AttributeName attN_taxId;

    private String sequenceType;
    private Float evalue;
    private Integer bitscore;
    private Integer cutoff;
    private Float overlap;

    private String programDir;
    private String program;

    private DecypherAlignment dcAlign;
    private String algo;


    private Collection<Match> resultList_fwd;
    private Collection<Match> resultList_bck;


    /**
     * NB: Overrides previous instance logging
     */
    public Mapping() {
        instance = this;
    }

    @Override
    public void start() throws Exception {
        setupMetaData();

        Map<Integer, Set<Integer>> taxId2CidSet = createTaxIdIndex();
        int[] taxIDArray = createTaxIdArray(taxId2CidSet);

        setupDecipherAlignment();

        if (algo != null && dcAlign != null) {
            runMapping(taxIDArray, taxId2CidSet);
        } else {
            fireEventOccurred(new WrongParameterEvent(
                    "Alignment could not be initialized with these parameters. "
                    , getCurrentMethodName()));
        }
    }

    private void setupMetaData() throws InvalidPluginArgumentException {
        eviType_blast = graph.getMetaData().getEvidenceType(EVIDENCE_BLAST);
        if (eviType_blast == null) {
            ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new EvidenceTypeMissingEvent(
                    RT_HAS_SIMILAR_SEQUENCE, Mapping.getCurrentMethodName()));
        }

        attN_eValue = graph.getMetaData().getAttributeName(ATT_E_VALUE);
        if (attN_eValue == null) {
            ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new AttributeNameMissingEvent(
                    RT_HAS_SIMILAR_SEQUENCE, Mapping.getCurrentMethodName()));
        }

        attN_bitScore = graph.getMetaData().getAttributeName(ATT_BITSCORE);
        if (attN_bitScore == null) {
            ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new AttributeNameMissingEvent(
                    ATT_BITSCORE, Mapping.getCurrentMethodName()));
        }

        // get the relationtypeset and evidencetype for this mapping
        rtSet_simSeq = graph.getMetaData().getRelationType(RT_HAS_SIMILAR_SEQUENCE);
        if (rtSet_simSeq == null) {
            RelationType rt = graph.getMetaData().getRelationType(RT_HAS_SIMILAR_SEQUENCE);
            if (rt == null) {
                ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new RelationTypeMissingEvent(
                        RT_HAS_SIMILAR_SEQUENCE, Mapping.getCurrentMethodName()));
            }
            rtSet_simSeq = graph.getMetaData().getFactory().createRelationType(RT_HAS_SIMILAR_SEQUENCE, rt);
        }

        sequenceType = (String) args.getUniqueValue(SEQ_TYPE_ARG);

        attN_seqType = graph.getMetaData().getAttributeName(sequenceType);
        attN_taxId = graph.getMetaData().getAttributeName(ATT_TAXID);

        evalue = ((Number) args.getUniqueValue(E_VALUE_ARG)).floatValue();
        bitscore = (Integer) args.getUniqueValue(BITSCORE_ARG);
        cutoff = (Integer) args.getUniqueValue(CUTOFF_ARG);
        overlap = ((Number) args.getUniqueValue(OVERLAP_ARG)).floatValue();
        programDir = (String) args.getUniqueValue(PROGRAM_DIR_ARG);
        program = (String) args.getUniqueValue(SEQ_ALIGNMENT_PROG_ARG);
    }

    private Map<Integer, Set<Integer>> createTaxIdIndex() throws InvalidPluginArgumentException {
        Set<String> taxIds = new HashSet<String>();
        taxIds.addAll(Arrays.asList((String[]) args.getObjectValueArray(TAX_ID_ARG)));

        Map<Integer, Set<Integer>> taxId2CidSet = new HashMap<Integer, Set<Integer>>();

        Set<ONDEXConcept> allSequences = graph.getConceptsOfAttributeName(attN_seqType);
        Set<ONDEXConcept> allTaxId = graph.getConceptsOfAttributeName(attN_taxId);
        Set<ONDEXConcept> conceptsWithTaxIdAndSeq = BitSetFunctions.and(allSequences, allTaxId);

        System.out.println("Creating index");

        for (ONDEXConcept queryConcept: conceptsWithTaxIdAndSeq) {
            Attribute attribute = queryConcept.getAttribute(attN_taxId);
            if (attribute != null) {
                String value = (String) attribute.getValue();

                //if a tax is specified then limit
                if (taxIds.size() == 0 || taxIds.contains(value)) {

                    int taxIdVal = Integer.valueOf(value);

                    Set<Integer> setCon = taxId2CidSet.get(taxIdVal);
                    if (setCon == null) {
                        setCon = new HashSet<Integer>();
                        taxId2CidSet.put(taxIdVal, setCon);
                    }
                    setCon.add(queryConcept.getId());
                }
            }
        }

        return taxId2CidSet;
    }

    private int[] createTaxIdArray(Map<Integer, Set<Integer>> taxId2CidSet) {
        int num_taxIDs = taxId2CidSet.keySet().size();
        int[] taxIDArray = new int[num_taxIDs];
        Iterator<Integer> taxid_it = taxId2CidSet.keySet().iterator();
        for (int i = 0; taxid_it.hasNext(); i++)
            taxIDArray[i] = taxid_it.next();
        return taxIDArray;
    }

    private void setupDecipherAlignment() {
        algo = null;
        if (sequenceType.equalsIgnoreCase(AA)) {
            algo = DecypherAlignment.ALGO_BLASTP;
        } else if (sequenceType.equalsIgnoreCase(NA)) {
            algo = DecypherAlignment.ALGO_BLASTN;
        } else {
            fireEventOccurred(new WrongParameterEvent(
                    "Selected Program can not Align these types of sequences :"
                            + program + " " + sequenceType + "->"
                            + sequenceType, getCurrentMethodName()));
        }
        try {
            int dcbit = 0;
            if (bitscore != null) dcbit = bitscore;
            dcAlign = new DecypherAlignment(net.sourceforge.ondex.config.Config.ondexDir,
                    programDir, cutoff, overlap, evalue, dcbit, 60, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runMapping(int[] taxIDArray, Map<Integer, Set<Integer>> taxId2CidSet) throws Exception{
        System.out.println("Running ortholog prediction");
        for (int i = 1; i < taxIDArray.length; i++) {

            Set<Integer> cidISet = taxId2CidSet.get(taxIDArray[i]);
            BitSet sbs_i = new BitSet(cidISet.size());//set of concept ids containing the seqs
            for (int cid : cidISet) {
                sbs_i.set(cid);
            }

            for (int j = 0; j < i; j++) {

                Set<Integer> cidJSet = taxId2CidSet.get(taxIDArray[j]);
                BitSet sbs_j = new BitSet(cidJSet.size());//set of concept ids containing the seqs
                for (int cid : cidJSet) {
                    sbs_j.set(cid);
                }

                fireEventOccurred(new GeneralOutputEvent(
                        "Finding orthologs between " + taxIDArray[i] + " and "
                                + taxIDArray[j] + " (blasting " + sbs_i.length() + "x" + sbs_j.length() + " sequences)",
                        getCurrentMethodName()));

                final Set<ONDEXConcept> seqToBLAST_i = BitSetFunctions.create(
                        graph, ONDEXConcept.class, sbs_i);
                final Set<ONDEXConcept> seqToBLAST_j = BitSetFunctions.create(
                        graph, ONDEXConcept.class, sbs_j);

                if (program.equalsIgnoreCase(SequenceAlignmentProgramArgumentDefinition.DECYPHER)) {
                    fireEventOccurred(new GeneralOutputEvent(
                            "Running " + SequenceAlignmentProgramArgumentDefinition.DECYPHER,
                            getCurrentMethodName()));
                    try {
                        final Semaphore sem_fwd = new Semaphore(1);
                        final Semaphore sem_bck = new Semaphore(1);
                        Thread thread_fwd = new Thread("BLAST forward") {
                            public void run() {
                                try {
                                    sem_fwd.acquire();
                                    resultList_fwd = dcAlign.query(graph,
                                            seqToBLAST_i, seqToBLAST_j, algo);
                                    sem_fwd.release();
                                } catch (Exception e) {
                                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }
                            }
                        };
                        Thread thread_bck = new Thread("BLAST backward") {
                            public void run() {
                                try {
                                    sem_bck.acquire();
                                    resultList_bck = dcAlign.query(graph,
                                            seqToBLAST_j, seqToBLAST_i, algo);
                                    sem_bck.release();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        thread_fwd.run();
                        thread_bck.run();
                        Thread.sleep(1000);
                        sem_fwd.acquire();
                        sem_bck.acquire();
//						Set<Match> resultList_fwd = dcAlign.query(s, graph,
//								seqToBLAST_i, seqToBLAST_j, algo);
//						Set<Match> resultList_bck = dcAlign.query(s, graph,
//								seqToBLAST_j, seqToBLAST_i, algo);

                        //POSTPROCESSING OF MATCHES

                        System.out.println("Postprocessing matches...");
                        Collection<BidirectionalHit> bdhs =
                                extractBestBDHs(resultList_fwd, resultList_bck);

                        resultList_fwd = null;
                        resultList_bck = null;

                        System.out.println("creating relations...");

                        for (BidirectionalHit hit : bdhs) {
                            int fromCid = hit.m1.getQueryId();
                            int toCid = hit.m1.getTargetId();
                            ONDEXConcept fromC = graph.getConcept(fromCid);
                            ONDEXConcept toC = graph.getConcept(toCid);
                            ONDEXRelation r = graph.getRelation(fromC, toC, rtSet_simSeq);
                            if (r == null) {
                                r = graph.getFactory().createRelation(fromC, toC, rtSet_simSeq, eviType_blast);
                                r.createAttribute(attN_eValue,
                                        hit.getOverallEValue(),
                                        false);
                                r.createAttribute(attN_bitScore,
                                        hit.getOverallScore(),
                                        false);
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else {
                    if (program != null) {
                        fireEventOccurred(new WrongParameterEvent(
                                "Parameter for SequenceAlignmentProgram is not recognized :"
                                        + program, getCurrentMethodName()));
                    } else {
                        fireEventOccurred(new WrongParameterEvent(
                                "Parameter SequenceAlignmentProgram is not set",
                                getCurrentMethodName()));
                    }
                    return;
                }
            }
        }
    }

    private Set<BidirectionalHit> extractBestBDHs(Collection<Match> resultList_fwd, Collection<Match> resultList_bck) {

        //index best forward matches
        Map<Integer, Match> indexBestMatchOnId_fwd = new HashMap<Integer, Match>();
        Iterator<Match> matches = resultList_fwd.iterator();
        while (matches.hasNext()) {
            Match match = matches.next();
            Match old_match = indexBestMatchOnId_fwd.get(match.getQueryId());
            if (old_match == null || (match.getScore() > old_match.getScore())) {
                indexBestMatchOnId_fwd.put(match.getQueryId(), match);
            }
        }
        System.out.println("\n\nBestHits forward: " + indexBestMatchOnId_fwd.size());

        //index best backwards matches
        Map<Integer, Match> indexBestMatchOnId_bck = new HashMap<Integer, Match>();
        matches = resultList_bck.iterator();
        while (matches.hasNext()) {
            Match match = matches.next();
            Match old_match = indexBestMatchOnId_bck.get(match.getQueryId());
            if (old_match == null || (match.getScore() > old_match.getScore())) {
                indexBestMatchOnId_bck.put(match.getQueryId(), match);
            }
        }
        System.out.println("BestHits backwards: " + indexBestMatchOnId_bck.size());

        //create set of best bidirectional hits
        Set<BidirectionalHit> bestBDHs = new HashSet<BidirectionalHit>();
        Iterator<Integer> cid_it = indexBestMatchOnId_fwd.keySet().iterator();
        while (cid_it.hasNext()) {
            int queryID = cid_it.next();
            Match m_fwd = indexBestMatchOnId_fwd.get(queryID);
            if (m_fwd != null) {
                int targetID = m_fwd.getTargetId();
                Match m_bck = indexBestMatchOnId_bck.get(targetID);
                if (m_bck != null) {
                    if (m_bck.getTargetId() == queryID) {//then it's a BDH
                        BidirectionalHit bdh = new BidirectionalHit(m_fwd, m_bck);
                        bestBDHs.add(bdh);
                    }
                }
            }
        }

        System.out.println("\nBBDHs: " + bestBDHs.size() + "\n\n");

        return bestBDHs;
    }


    public ArgumentDefinition<?>[] getArgumentDefinitions() {

        Set<ArgumentDefinition<?>> extendedDefinition = new HashSet<ArgumentDefinition<?>>();

        // Add blast program params
        extendedDefinition.add(new RangeArgumentDefinition<Float>(E_VALUE_ARG,
                E_VALUE_DESC, true, 0.000001F, 0f, Float.MAX_VALUE, Float.class));

        extendedDefinition.add(new RangeArgumentDefinition<Integer>(BITSCORE_ARG,
                BITSCORE_DESC, true, 200, 0, Integer.MAX_VALUE, Integer.class));

        extendedDefinition.add(new StringArgumentDefinition(TAX_ID_ARG,
                TAX_ID_ARG_DESC, false, null, true));

        extendedDefinition.add(new SequenceTypeArgumentDefinition(SEQ_TYPE_ARG,
                true, ATT_AMINO_ACID_SEQ));

        extendedDefinition.add(new FileArgumentDefinition(PROGRAM_DIR_ARG,
                PROGRAM_DIR_DESC, true, true, true));

        extendedDefinition.add(new SequenceAlignmentProgramArgumentDefinition(SEQ_ALIGNMENT_PROG_ARG,
                true, SequenceAlignmentProgramArgumentDefinition.DECYPHER));

        extendedDefinition.add(new RangeArgumentDefinition<Float>(OVERLAP_ARG,
                OVERLAP_DESC, false, 0.25F, 0F, Float.MAX_VALUE, Float.class));

        extendedDefinition.add(new RangeArgumentDefinition<Integer>(CUTOFF_ARG,
                CUTOFF_DESC, false, 5, 0, Integer.MAX_VALUE, Integer.class));

        return extendedDefinition
                .toArray(new ArgumentDefinition<?>[extendedDefinition.size()]);

    }

    public String getName() {
        return "Sequence based Ortholog prediction";
    }

    public String getVersion() {
        return "09.05.2008";
    }

    @Override
    public String getId() {
        return "orthologprediction";
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
        return "[CLASS:" + className + " - METHOD:" + name + " LINE:" + line
                + "]";
    }

    public static void propagateEventOccurred(EventType et) {
        if (instance != null)
            instance.fireEventOccurred(et);
    }

    private class BidirectionalHit {
        public Match m1, m2;

        public BidirectionalHit(Match m1, Match m2) {
            this.m1 = m1;
            this.m2 = m2;
        }

        public double getOverallScore() {
            return getWorseScore();
        }

        public double getOverallEValue() {
            return getWorseEValue();
        }

        private double getWorseEValue() {
            if (m1.getEValue() > m2.getEValue())
                return m1.getEValue();
            else
                return m1.getEValue();
        }

        @SuppressWarnings("unused")
        private double getScoreOfShortestQuery() {
            int l1 = m1.getLengthOfQuerySequence();
            int l2 = m2.getLengthOfQuerySequence();
            if (l1 > l2)
                return m1.getScore();
            else
                return m2.getScore();
        }

        private double getWorseScore() {
            if (m1.getScore() < m2.getScore())
                return m1.getScore();
            else
                return m2.getScore();
        }

        @SuppressWarnings("unused")
        private double getAverageScore() {
            return (m1.getScore() + m2.getScore()) / 2.0D;
        }
    }

    public String[] requiresValidators() {
        return new String[0];
    }
}
