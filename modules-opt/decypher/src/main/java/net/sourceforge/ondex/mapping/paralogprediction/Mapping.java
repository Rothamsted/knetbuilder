package net.sourceforge.ondex.mapping.paralogprediction;

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
import net.sourceforge.ondex.programcalls.exceptions.AlgorithmNotSupportedException;
import net.sourceforge.ondex.programcalls.exceptions.MissingFileException;

import java.io.IOException;
import java.util.*;

/**
 * Predicts paralogs within species based on a bitscore cutoff of similar proteins (bitscores, unlike evalues do not vary with the size of the database), matches based on reciprocal blast.
 *
 * @author hindlem
 */
@Authors(authors = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
@Custodians(custodians = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
public class Mapping extends ONDEXMapping implements ArgumentNames, MetaData {

    private static final String AA = "AA";
    private static final String NA = "NA";

    private static Mapping instance; //used for logging

    private EvidenceType eviType;//blast_evidence
    private AttributeName eValue;
    private AttributeName bitScore;
    private RelationType rtSet;//similar sequence
    private AttributeName seq_overlap;
    private AttributeName frame;
    private AttributeName query_length;
    private AttributeName target_length;

    /**
     * NB: Overrides previous instance logging
     */
    public Mapping() {
        instance = this;
    }

    @Override
    public void start() throws InvalidPluginArgumentException, IOException {

        eviType = graph.getMetaData().getEvidenceType(EVIDENCE_BLAST);
        if (eviType == null) {
            ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new EvidenceTypeMissingEvent(
                    RT_HAS_SIMILAR_SEQUENCE, Mapping.getCurrentMethodName()));
        }

        eValue = graph.getMetaData().getAttributeName(ATT_E_VALUE);
        if (eValue == null) {
            ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new AttributeNameMissingEvent(
                    RT_HAS_SIMILAR_SEQUENCE, Mapping.getCurrentMethodName()));
        }

        bitScore = graph.getMetaData().getAttributeName(ATT_BITSCORE);
        if (bitScore == null) {
            ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new AttributeNameMissingEvent(
                    ATT_BITSCORE, Mapping.getCurrentMethodName()));
        }

        AttributeName coverage = graph.getMetaData().getAttributeName(MetaData.ATT_COVERAGE);
        if (coverage == null) {
            coverage = graph.getMetaData().getFactory().createAttributeName(ATT_COVERAGE, Double.class);
        }

        // get the relationtypeset and evidencetype for this mapping
        rtSet = graph.getMetaData().getRelationType(RT_HAS_SIMILAR_SEQUENCE);
        if (rtSet == null) {
            RelationType rt = graph.getMetaData().getRelationType(
                    RT_HAS_SIMILAR_SEQUENCE);
            if (rt == null) {
                ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new RelationTypeMissingEvent(
                        RT_HAS_SIMILAR_SEQUENCE, Mapping.getCurrentMethodName()));
            }
            rtSet = graph.getMetaData().getFactory().createRelationType(
                    RT_HAS_SIMILAR_SEQUENCE, rt);
        }

        seq_overlap = graph.getMetaData().getAttributeName(MetaData.ATT_OVERLAP);
        if (seq_overlap == null) {
            seq_overlap = graph.getMetaData().getFactory().createAttributeName(MetaData.ATT_OVERLAP, "Alignment overlap", Integer.class);
        }

        frame = graph.getMetaData().getAttributeName(MetaData.ATT_FRAME);
        if (frame == null) {
            frame = graph.getMetaData().getFactory().createAttributeName(MetaData.ATT_FRAME, "Translation fame", Integer.class);
        }

        query_length = graph.getMetaData().getAttributeName(MetaData.ATT_QUERY_LENGTH);
        if (query_length == null) {
            query_length = graph.getMetaData().getFactory().createAttributeName(MetaData.ATT_QUERY_LENGTH, "Length of query sequence", Integer.class);
        }

        target_length = graph.getMetaData().getAttributeName(MetaData.ATT_TARGET_LENGTH);
        if (target_length == null) {
            target_length = graph.getMetaData().getFactory().createAttributeName(MetaData.ATT_TARGET_LENGTH, "Length of target sequence", Integer.class);
        }

        HashSet<String> taxIds = new HashSet<String>();

        for (String taxId : (String[]) args.getObjectValueArray(TAX_ID_ARG)) {
            taxIds.add(taxId);
        }


        String sequenceType = (String) args.getUniqueValue(SEQ_TYPE_ARG);

        Integer speciesSeqSize = (Integer) args.getUniqueValue(SPECIES_SEQUENCE_SIZE_ARG);

        AttributeName att = graph.getMetaData().getAttributeName(
                sequenceType);
        AttributeName taxId = graph.getMetaData().getAttributeName(
                ATT_TAXID);

        //index for taxid -> set of conceptIDs
        Map<Integer, Set<Integer>> index = new HashMap<Integer, Set<Integer>>();

        Set<ONDEXConcept> allSequences = graph.getConceptsOfAttributeName(att);
        Set<ONDEXConcept> allTaxId = graph.getConceptsOfAttributeName(taxId);
        allSequences.retainAll(allTaxId);

        System.out.println("Creating index"); //of all TaxIDs and their assigned concepts

        for (ONDEXConcept queryConcept : allSequences) {

            Attribute attribute = queryConcept.getAttribute(taxId);
            if (attribute != null) {
                String value = (String) attribute.getValue();

                //if a tax is specified then limit
                if (taxIds.size() == 0 || taxIds.contains(value)) {

                    int taxIdVal = Integer.valueOf(value);

                    Set<Integer> setCon = index.get(taxIdVal);
                    if (setCon == null) {
                        setCon = new HashSet<Integer>();
                        index.put(taxIdVal, setCon);
                    }
                    setCon.add(queryConcept.getId());
                }
            }
        }

        System.out.println("Paralog prediction found " + index.size() + " species in the database");

        //filter out the databases too small to consider
        Iterator<Integer> taxids = index.keySet().iterator();//keys are TaxIDs
        while (taxids.hasNext()) {
            int taxIdVals = taxids.next();
            Set<Integer> sequences = index.get(taxIdVals);
            if (sequences.size() < speciesSeqSize) {
                index.remove(taxIdVals);
            }
        }

        System.out.println("Paralog prediction found " + index.size() + " qualifing species in the database");


        Float evalue = ((Number) args.getUniqueValue(E_VALUE_ARG)).floatValue();
        Integer bitscore = (Integer) args.getUniqueValue(BITSCORE_ARG);
        Integer cutoff = (Integer) args.getUniqueValue(CUTOFF_ARG);
        Float overlap = ((Number) args.getUniqueValue(OVERLAP_ARG)).floatValue();
        String programDir = (String) args.getUniqueValue(PROGRAM_DIR_ARG);

        System.out.println("Running paralog prediction");

        DecypherAlignment dcAlign = null;

        String algo = null;
        if (sequenceType.equalsIgnoreCase(AA)) {
            algo = DecypherAlignment.ALGO_BLASTP;
        } else if (sequenceType.equalsIgnoreCase(NA)) {
            algo = DecypherAlignment.ALGO_BLASTN;
        } else {
            fireEventOccurred(new WrongParameterEvent(
                    "Decypher can not Align these types of sequences :"
                            + " " + sequenceType + "->"
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

        int speciesNumber = 0;
        int totalSpecies = index.keySet().size();
        Iterator<Integer> keys = index.keySet().iterator();//keys are TaxIDs
        while (keys.hasNext()) {
            int taxIdVals = keys.next();
            speciesNumber++;
            Set<Integer> sequences = index.get(taxIdVals);

            fireEventOccurred(new GeneralOutputEvent(
                    "Finding paralogs for taxid: " + taxIdVals + " on "
                            + sequences.size() + " sequences (" + speciesNumber + " of " + totalSpecies + ")",
                    getCurrentMethodName()));

            Set<ONDEXConcept> seqToBLAST = BitSetFunctions.create(
                    graph, ONDEXConcept.class, sequences);

            fireEventOccurred(new GeneralOutputEvent(
                    "Running "
                            + SequenceAlignmentProgramArgumentDefinition.DECYPHER,
                    getCurrentMethodName()));
            try {

                Collection<Match> resultList = dcAlign.query(graph,
                        seqToBLAST, seqToBLAST, algo);

                //maps queryID -> Set of matches
                Map<Integer, Set<Match>> indexOnQueryId = new HashMap<Integer, Set<Match>>();
                System.out.println("Indexing matches");
                Iterator<Match> matches = resultList.iterator();
                while (matches.hasNext()) {
                    Match match = matches.next();
                    Set<Match> qMatches = indexOnQueryId.get(match.getQueryId());
                    if (qMatches == null) {
                        qMatches = new HashSet<Match>();
                        indexOnQueryId.put(match.getQueryId(), qMatches);
                    }
                    qMatches.add(match);
                }
                resultList = null; // clear old results

                Iterator<Integer> querIt = indexOnQueryId.keySet().iterator();
                while (querIt.hasNext()) {
                    int query = querIt.next();
                    ONDEXConcept queryConcept = graph.getConcept(query);

                    Iterator<Match> matchIt = indexOnQueryId.get(query).iterator();
                    while (matchIt.hasNext()) {
                        Match hit = matchIt.next();
                        if (query != hit.getTargetId()
                                && (bitscore == null
                                || bitscore <= hit.getScore())) {

                            //check for reciprical hits
                            Set<Match> recipricalHits = indexOnQueryId.get(hit.getTargetId());
                            boolean found = false;
                            if (recipricalHits != null) { //nothing to look for
                                for (Match hitr : recipricalHits) {
                                    if (hitr.getTargetId() == query)
                                        found = true;
                                }
                            }

                            if (found) {
                                ONDEXConcept toConcept = graph.getConcept(hit.getTargetId());

                                ONDEXRelation relation = graph.getRelation(queryConcept, toConcept, rtSet);
                                // try if relation was already created
                                if (relation == null) {
                                    // create a new relation between the two
                                    // concepts
                                    relation = graph.getFactory().createRelation(
                                            queryConcept, toConcept, rtSet,
                                            eviType);

                                    relation.createAttribute(eValue, Double.valueOf(hit.getEValue()), false);
                                    relation.createAttribute(bitScore, Double.valueOf(hit.getScore()), false);
                                    relation.createAttribute(coverage, Double.valueOf(hit.geQueryCoverageSequence()), false);

                                    relation.createAttribute(seq_overlap, Integer.valueOf(hit.getOverlapingLength()), false);
                                    relation.createAttribute(frame, Integer.valueOf(hit.getQueryFrame()), false);
                                    relation.createAttribute(query_length, Integer.valueOf(hit.getLengthOfQuerySequence()), false);
                                    relation.createAttribute(target_length, Integer.valueOf(hit.getLengthOfTargetSequence()), false);
                                }
                            }
                        }
                    }
                }
                System.runFinalization();
            } catch (MissingFileException e) {
                e.printStackTrace();
            } catch (AlgorithmNotSupportedException e) {
                e.printStackTrace();
            }
        }

    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {

        HashSet<ArgumentDefinition<?>> extendedDefinition = new HashSet<ArgumentDefinition<?>>();

        // Add blast program params
        extendedDefinition.add(new RangeArgumentDefinition<Float>(E_VALUE_ARG,
                E_VALUE_DESC, true, 0.001F, 0f, Float.MAX_VALUE, Float.class));
        extendedDefinition.add(new RangeArgumentDefinition<Integer>(BITSCORE_ARG,
                BITSCORE_DESC, true, 0, 0, Integer.MAX_VALUE, Integer.class));
        extendedDefinition.add(new StringArgumentDefinition(
                TAX_ID_ARG, TAX_ID_ARG_DESC,
                false, null, true));
        extendedDefinition.add(new SequenceTypeArgumentDefinition(SEQ_TYPE_ARG,
                true, ATT_AMINO_ACID_SEQ));
        extendedDefinition.add(new FileArgumentDefinition(PROGRAM_DIR_ARG,
                PROGRAM_DIR_DESC, true, true, true));
        extendedDefinition.add(new RangeArgumentDefinition<Float>(OVERLAP_ARG,
                OVERLAP_DESC, false, 0.25F, 0f, Float.MAX_VALUE, Float.class));
        extendedDefinition.add(new RangeArgumentDefinition<Integer>(CUTOFF_ARG,
                CUTOFF_DESC, false, 5, 0, Integer.MAX_VALUE, Integer.class));
        extendedDefinition.add(new RangeArgumentDefinition<Integer>(SPECIES_SEQUENCE_SIZE_ARG,
                SPECIES_SEQUENCE_SIZE_ARG_DESC, false, 2, 2, Integer.MAX_VALUE, Integer.class));
        return extendedDefinition
                .toArray(new ArgumentDefinition<?>[extendedDefinition.size()]);

    }

    public String getName() {
        return "Sequence based Paralog prediction";
    }

    public String getVersion() {
        return "29.01.2008";
    }

    @Override
    public String getId() {
        return "paralogprediction";
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

    public String[] requiresValidators() {
        return new String[0];
    }
}
