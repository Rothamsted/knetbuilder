package net.sourceforge.ondex.mapping.sequence2pfam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.RangeArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.config.LuceneRegistry;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.searchable.LuceneConcept;
import net.sourceforge.ondex.core.searchable.LuceneEnv;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.mapping.ONDEXMapping;
import net.sourceforge.ondex.mapping.sequence2pfam.method.IMethod;
import net.sourceforge.ondex.programcalls.HMMMatch;

import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.RNASequence;
import org.biojava3.core.sequence.transcription.Frame;

/**
 * Sequence to Pfam mapping method. Maps a protein whith an attached amino acid
 * sequence to pfam protein-family entries. TODO replace with a much smarter
 * solution!
 *
 * @author peschr, hindlem
 */
@Authors(authors = {"Matthew Hindle", "Robert Pesch"}, emails = {"matthew_hindle at users.sourceforge.net", ""})
@Custodians(custodians = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
public class Mapping extends ONDEXMapping
{

    private AttributeName atrEvalue;
    private AttributeName atrBitscore;

    private ConceptClass ccSource;
    private AttributeName atSource;

    private RelationType rtMemberIsPartOf = null;
    private File fastaFile;

    private AttributeName atrDomainEvalue;
    private AttributeName atrDomainBitscore;
    private AttributeName atrTranslationFrame;

    // list of all proteins
    // private HashMap<String, ONDEXConcept> writtenProteins = new
    // HashMap<String,ONDEXConcept>();

    /**
     * Calls the super constructor
     */
    public Mapping() {
    }

    /**
     * Checks if a Pfam accession is in the
     * <code>Set<ConceptAccession></code> Iterator
     *
     * @param accessions - accessions which should be checked
     * @param graph      - AbstractONDEXGraph
     * @return boolean - true, if the protein has pfam information attached
     */
    private boolean hasPfamInformationAttached(
            Set<ConceptAccession> accessions,
            ONDEXGraph graph) {
        DataSource pfam = graph.getMetaData().getDataSource(MetaData.CV_PFAM);
        for (ConceptAccession accession : accessions) {
            if (accession.getElementOf().equals(pfam)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the method which is definied by <code>METHOD_ARG<code> PluginArgument.
     *
     * @return IMethod - an implementation of the IMethod interface
     * @throws MethodNotFoundException - if the method can't be found
     */
    public IMethod getMethod(ONDEXGraph graph)
            throws MethodNotFoundException, InvalidPluginArgumentException {

        String methodName = (String) args
                .getUniqueValue(ArgumentNames.METHOD_ARG);

        IMethod method = null;
        try {
            Class<?> c = Class.forName(this.getClass().getPackage().getName()
                    + ".method." + methodName);
            if (c.getConstructors().length != 1) {
                throw new MethodNotFoundException();
            }
            method = (IMethod) c.getConstructors()[0].newInstance(new Object[]{
                    super.args.getUniqueValue(ArgumentNames.PROGRAM_DIR_ARG)
                            .toString(),
                    super.args.getUniqueValue(ArgumentNames.PFAM_PATH_ARG)
                            .toString(),
                    fastaFile.getAbsolutePath(),
                    (String) super.args.getUniqueValue(ArgumentNames.EVALUE_ARG)
                            .toString(),
                    (String) super.args.getUniqueValue(
                            ArgumentNames.BIT_SCORE_ARG).toString(),
                    (String) super.args
                            .getUniqueValue(ArgumentNames.HMM_THRESHOLDS_ARG),
                    graph, atSource, ccSource});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return method;
    }

    /**
     * Adds a sequence to the fasta styled database
     *
     * @param b       - BufferedWriter
     * @param concept - to store concept
     * @return if the entry was really added (could be false if the
     *         <code>IGNORE_PFAM_ANNOTATION_ARG</code> argument was changed
     * @throws IOException
     */
    private boolean addEntryToFastaDatabase(BufferedWriter b,
                                            ONDEXConcept concept, ONDEXGraph graph)
            throws IOException, InvalidPluginArgumentException {

        if (concept.getAttribute(atSource) == null
                || ((Boolean) super.args
                .getUniqueValue(ArgumentNames.IGNORE_PFAM_ANNOTATION_ARG) == false && this
                .hasPfamInformationAttached(concept
                        .getConceptAccessions(), graph)))
            return false;
        Integer id = concept.getId();
        String sequence = (String) concept.getAttribute(atSource).getValue();
        b.write(">" + id + "\n" + sequence + "\n");
        // writtenProteins.put(id, concept);
        return true;
    }

    private void translateToFASTAFile(File f, Set<ONDEXConcept> concepts, ONDEXGraph graph) throws IOException, InvalidPluginArgumentException {

        FileOutputStream os = new FileOutputStream(f);
        OutputStreamWriter bw = new OutputStreamWriter(os);
        for (ONDEXConcept concept : concepts) {
            if (concept.getAttribute(atSource) == null
                    || ((Boolean) super.args
                    .getUniqueValue(ArgumentNames.IGNORE_PFAM_ANNOTATION_ARG) == false && this
                    .hasPfamInformationAttached(concept
                            .getConceptAccessions(), graph))) {
                //
            } else {
                String sequence = (String) concept.getAttribute(atSource).getValue();

                // for each frame
                int i = 0;
                for (Frame frame : Frame.getAllFrames()) {
                	DNASequence dna = new DNASequence(sequence);
                	RNASequence rna = dna.getRNASequence(frame); 
                	ProteinSequence prot = rna.getProteinSequence();
                    String id = concept.getId() + ":" + Integer.toString(i + 1);
                    bw.write(">"+id+"\n");
                    bw.write(prot.getSequenceAsString()+"\n");
                    i++;
                }
            }
        }
        os.close();
    }

    /**
     * Is thrown in the cause that the given <code>METHOD_ARG<code> is not valid
     *
     * @author peschr
     */
    public class MethodNotFoundException extends Exception {
        private static final long serialVersionUID = -4322618302946140535L;
    }

    @Override
    public void start() throws InvalidPluginArgumentException {

        EvidenceType etIMPD = graph.getMetaData().getEvidenceType("IMPD");
        ConceptClass ccProteinFamily = graph.getMetaData().getConceptClass(
                MetaData.CC_ProteinFamily);

        DataSource dataSource = graph.getMetaData().getDataSource("Seq2Pfam");
        if (dataSource == null) {
            dataSource = graph.getMetaData().createDataSource("Seq2Pfam", "Sequence to Pfam Mapping", "Concepts created on the fly by sequence to pfam");
        }

        ccSource = graph.getMetaData().getConceptClass(
                args.getUniqueValue(ArgumentNames.CONCEPT_CLASS_ARG).toString());
        atSource = graph.getMetaData().getAttributeName(
                args.getUniqueValue(ArgumentNames.ATTRIBUTE_ARG).toString());

        rtMemberIsPartOf = graph.getMetaData().getRelationType(
                MetaData.RT_MEMBER_IS_PART_OF);
        atrEvalue = graph.getMetaData().getAttributeName(
                MetaData.ATT_EVALUE);
        atrBitscore = graph.getMetaData().getAttributeName(
                MetaData.ATT_BITSCORE);

        atrDomainEvalue = graph.getMetaData().getFactory().createAttributeName(
                "BD_Evalue", "Bestdomain EValue", Double.class);
        atrDomainBitscore = graph.getMetaData().getFactory().createAttributeName(
                "BD_Score", "Best domain Score", Double.class);

        atrTranslationFrame = graph.getMetaData().getFactory().createAttributeName(
                "TranslationFrame", "TranslationFrame", Integer.class);

        fastaFile = new File(super.args.getUniqueValue("TmpDir").toString());

        if (!((String) args.getUniqueValue(ArgumentNames.METHOD_ARG)).equalsIgnoreCase("decypher")) { // decypher creats its own file

            // create a fasta styled database
            try {
                fastaFile = File
                        .createTempFile("fasta_" + new Date(System.currentTimeMillis()).toString().replaceAll(" ", "_").replaceAll(":", "_"),
                                ".tmp", fastaFile);

                fastaFile.mkdirs();

                Set<ONDEXConcept> allConcepts = graph
                        .getConceptsOfConceptClass(ccSource);

                if (((String) args.getUniqueValue(ArgumentNames.METHOD_ARG)).equalsIgnoreCase("Hmmer")
                        && atSource.getId().equalsIgnoreCase("NA")) {
                    try {
                        translateToFASTAFile(fastaFile, allConcepts, graph);
                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                } else {

                    BufferedWriter b = new BufferedWriter(new FileWriter(fastaFile));
                    int writtenSequences = 0;
                    for (ONDEXConcept c : allConcepts) {
                        if (this.addEntryToFastaDatabase(b, c, graph))
                            writtenSequences++;
                    }
                    b.close();
                    ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new GeneralOutputEvent("Found "
                            + writtenSequences + " sequences", Mapping.class
                            .toString()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // resolve the method class
        IMethod method;
        try {
            method = this.getMethod(graph);
        } catch (MethodNotFoundException e1) {
            ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new GeneralOutputEvent("Unknown method ("
                    + args.getUniqueValue(ArgumentNames.METHOD_ARG).toString()
                    + "), check " + ArgumentNames.METHOD_ARG
                    + " parameter. Using blast instead", Mapping.class
                    .toString()));
            super.args.addOption(ArgumentNames.METHOD_ARG, "Blast");
            try {
                method = getMethod(graph);
            } catch (MethodNotFoundException e) {
                ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new GeneralOutputEvent(
                        "There are serious problems in locating the right mapping method",
                        Mapping.class.toString()));
                e.printStackTrace();
                return;
            }
        }
        try {

            // get the machtes
            Collection<HMMMatch> results = method.execute();
            ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new GeneralOutputEvent("Found "
                    + results.size() + " theoretical matches", Mapping.class
                    .toString()));
            // create relations between protein and family
            for (HMMMatch result : results) {
                LuceneEnv lenv = LuceneRegistry.sid2luceneEnv.get(graph.getSID());
                Set<ONDEXConcept> itResults = method
                        .searchMatchingConceptsInLuceneEnvironment(lenv, result); // lenv.searchConceptByConceptAccessionExact(s,result.getTargetAccession());
                if (itResults == null || itResults.size() == 0) {
                    ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new GeneralOutputEvent(
                            "Pfam family " + result + " was not found:  creating new one",
                            Mapping.class.toString()));

                    ONDEXConcept concept = graph.getFactory().createConcept(
                            result.getHmmAccession(),
                            dataSource, ccProteinFamily, etIMPD);
                    BitSet sbs = new BitSet();
                    sbs.set(concept.getId());
                    itResults = BitSetFunctions.create(graph, ONDEXConcept.class, sbs);
                }

                for (ONDEXConcept pfamConcept : itResults) {
                    if (pfamConcept instanceof LuceneConcept) {
                        pfamConcept = ((LuceneConcept) pfamConcept).getParent();
                    }

                    ONDEXConcept from = graph.getConcept(
                            result.getTargetId());

                    ONDEXRelation relation = graph.getRelation(from,
                            pfamConcept, rtMemberIsPartOf);
                    if (relation == null) {
                        relation = graph.getFactory().createRelation(from, pfamConcept,
                                rtMemberIsPartOf, method.getEvidenceType());

                        relation.createAttribute(atrEvalue,
                                result.getEValue(), false);

                        if (result.getScore() != -1)
                            relation.createAttribute(atrBitscore,
                                    result.getScore(), false);
                        if (result.getBestDomainEvalue() != null)
                            relation.createAttribute(atrDomainEvalue, result.getBestDomainEvalue(), false);

                        if (result.getBestDomainScore() != null)
                            relation.createAttribute(atrDomainBitscore, result.getBestDomainScore(), false);

                        if (result.getQueryFrame() != null)
                            relation.createAttribute(atrTranslationFrame, result.getQueryFrame(), false);

                    } else {
                        Attribute evalue = relation.getAttribute(atrEvalue);
                        Attribute bitscore = relation.getAttribute(atrBitscore);
                        Attribute domain_evalue = relation.getAttribute(atrDomainEvalue);
                        Attribute domain_bitscore = relation.getAttribute(atrDomainBitscore);
                        Attribute frame = relation.getAttribute(atrTranslationFrame);


                        if (evalue == null || result.getEValue() < ((Number) (evalue.getValue())).doubleValue()) {
                            evalue.setValue(result.getEValue());
                            if (bitscore == null) {
                                if (result.getScore() > -1) {
                                    relation.createAttribute(atrBitscore,
                                            result.getScore(), false);
                                }
                            } else {
                                if (result.getScore() > -1) {
                                    relation.deleteAttribute(atrBitscore); // remove
                                    // to
                                    // avoid
                                    // confusion
                                } else {
                                    bitscore.setValue(result.getScore());
                                }
                            }
                            addAttribute(result.getBestDomainEvalue(),
                                    domain_evalue, relation, atrDomainEvalue);

                            addAttribute(result.getBestDomainScore(),
                                    domain_bitscore, relation, atrDomainBitscore);

                            addAttribute(result.getQueryFrame(),
                                    frame, relation, atrTranslationFrame);
                        }
                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addAttribute(Object value, Attribute attribute, ONDEXRelation relation, AttributeName att) {
        if (value != null) {
            if (attribute == null) {
                relation.createAttribute(att, value, false);
            } else {
                attribute.setValue(value);
            }
        }
    }


    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        StringArgumentDefinition blastDir = new StringArgumentDefinition(
                ArgumentNames.PROGRAM_DIR_ARG,
                ArgumentNames.PROGRAM_DIR_ARG_DESC, true, null, false);
        StringArgumentDefinition pfamDir = new StringArgumentDefinition(
                ArgumentNames.PFAM_PATH_ARG, ArgumentNames.PFAM_PATH_ARG_DESC,
                true, null, false);
        StringArgumentDefinition tmpDir = new StringArgumentDefinition(
                ArgumentNames.TMP_DIR_ARG, ArgumentNames.TMP_DIR_ARG_DESC,
                true, null, false);
        StringArgumentDefinition method = new StringArgumentDefinition(
                ArgumentNames.METHOD_ARG, ArgumentNames.METHOD_ARG_DESC, true,
                "Blast", false);

        StringArgumentDefinition cc = new StringArgumentDefinition(
                ArgumentNames.CONCEPT_CLASS_ARG,
                ArgumentNames.CONCEPT_CLASS_ARG_DESC, true, "Protein", false);

        StringArgumentDefinition att = new StringArgumentDefinition(
                ArgumentNames.ATTRIBUTE_ARG, ArgumentNames.ATTRIBUTE_ARG_DESC,
                true, "AA", false);

        RangeArgumentDefinition<Float> eValue = new RangeArgumentDefinition<Float>(
                ArgumentNames.EVALUE_ARG, ArgumentNames.EVALUE_ARG_DESC, true,
                10F, 0F, Float.MAX_VALUE, Float.class);
        RangeArgumentDefinition<Integer> bitScore = new RangeArgumentDefinition<Integer>(
                ArgumentNames.BIT_SCORE_ARG, ArgumentNames.BIT_SCORE_ARG_DESC,
                false, 0, 0, Integer.MAX_VALUE, Integer.class);
        BooleanArgumentDefinition ignorePfam = new BooleanArgumentDefinition(
                ArgumentNames.IGNORE_PFAM_ANNOTATION_ARG,
                ArgumentNames.IGNORE_PFAM_ANNOTATION_ARG_DESC, false, false);
        StringArgumentDefinition hmmThre = new StringArgumentDefinition(
                ArgumentNames.HMM_THRESHOLDS_ARG,
                ArgumentNames.HMM_THRESHOLDS_DESC, false, null, false);

        return new ArgumentDefinition<?>[]{blastDir, pfamDir, tmpDir, eValue,
                method, bitScore, ignorePfam, hmmThre, cc, att};
    }

    public String getName() {
        return "sequence2pfam";
    }

    public String getVersion() {
        return "10.12.07";
    }

    @Override
    public String getId() {
        return "sequence2pfam";
    }


    public boolean requiresIndexedGraph() {
        return true;
    }

    public String[] requiresValidators() {
        return new String[0];
    }

}