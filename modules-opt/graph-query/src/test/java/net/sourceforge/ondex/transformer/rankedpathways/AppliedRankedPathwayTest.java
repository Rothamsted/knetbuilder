package net.sourceforge.ondex.transformer.rankedpathways;

import junit.framework.TestCase;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.logging.ONDEXLogger;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AppliedRankedPathwayTest extends TestCase {

    private final static String fsm_part_way_end_point =
            "#Finite States (required modifiers: *=start state ^=end state)	ConceptClass\n" +
                    "1*	TARGETSEQ\n" +
                    "2	Protein\n" +
                    "3	Gene\n" +
                    "4	TF\n" +
                    "5^	Enzyme\n" +
                    "6	Reaction\n" +
                    "7	Path\n" +
                    "#Transitions	RelationTypeSets	Max length\n" +
                    "1-2	h_s_s\n" +
                    "2-5	is_a\n" +
                    "5-6	ca_by\n" +
                    "6-6	m_isp\n" +
                    "2-4	is_a\n" +
                    "4-3	rg_by	u\n" +
                    "2-2	h_s_s	1\n" +
                    "2-3	en_by\n" +
                    "6-7	m_isp\n" +
                    "#Weightings on finite States\n" +
                    "#Weightings on transitions AttributeName	Modifiers(CSV i=inverted lower is better, m=modulus values, r=ratio e.g. (i,m))	Relative Rank	TYPE(sum, mean, probability (independent))\n" +
                    "1-2	BLEV	i	1	probability\n" +
                    "2-2	BLEV	i	2	probability\n";

    private ONDEXGraph aog;

    private ConceptClass ccTS;
    private ConceptClass ccProtein;
    private ConceptClass ccTF;
    private ConceptClass ccGene;
    private ConceptClass ccEnzyme;
    private ConceptClass ccReaction;
    private ConceptClass ccPathway;

    private RelationType rts_Encodes;
    private RelationType rts_is_a;
    private RelationType rts_m_isp;
    //	private RelationType rts_interacts;
    private RelationType rts_regulates;
    private RelationType rts_cats;
    private RelationType rts_h_s_s;

    private EvidenceType et;

    private DataSource dataSource;

    private String file;

    private AttributeName att_blev;
//	private AttributeName score;


    @Override
    protected void setUp() throws Exception {
        aog = new MemoryONDEXGraph("testGraph");
        ONDEXEventHandler.getEventHandlerForSID(aog.getSID()).addONDEXONDEXListener(new ONDEXLogger());

        ccTS = aog.getMetaData().getFactory().createConceptClass("TARGETSEQ");
        ccProtein = aog.getMetaData().getFactory().createConceptClass("Protein");
        ccTF = aog.getMetaData().getFactory().createConceptClass("TF");
        ccGene = aog.getMetaData().getFactory().createConceptClass("Gene");
        ccEnzyme = aog.getMetaData().getFactory().createConceptClass("Enzyme");
        ccReaction = aog.getMetaData().getFactory().createConceptClass("Reaction");
        ccPathway = aog.getMetaData().getFactory().createConceptClass("Path");

        rts_Encodes = aog.getMetaData().getFactory().createRelationType("en_by");
        rts_is_a = aog.getMetaData().getFactory().createRelationType("is_a");
        rts_m_isp = aog.getMetaData().getFactory().createRelationType("m_isp");
        //	rts_interacts = aog.getONDEXMetaData().createRelationType("interacts");
        rts_regulates = aog.getMetaData().getFactory().createRelationType("rg_by");
        rts_cats = aog.getMetaData().getFactory().createRelationType("ca_by");
        rts_h_s_s = aog.getMetaData().getFactory().createRelationType("h_s_s");

        et = aog.getMetaData().getFactory().createEvidenceType("I_made_it_up");
        dataSource = aog.getMetaData().getFactory().createDataSource("matts_db");

        att_blev = aog.getMetaData().getFactory().createAttributeName("BLEV", Double.class);
        //	score = aog.getONDEXMetaData().createAttributeName("SCORE", Integer.class);
    }

    @Test
    public void testPartWayFinalPoint() throws FileNotFoundException, IOException, XMLStreamException, JAXBException, InvalidPluginArgumentException {

        try {
            file = File.createTempFile("tmp", "file").getAbsolutePath();
            new File(file).deleteOnExit();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(fsm_part_way_end_point);
            writer.newLine();
            writer.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        List<ONDEXConcept> ts = new ArrayList<ONDEXConcept>();
        for (int i = 0; i < 500; i++) {
            ONDEXConcept targ = aog.getFactory().createConcept("TARGETSEQUENCE_" + i, dataSource, ccTS, et);
            ts.add(targ);
        }

        List<ONDEXConcept> proteins = new ArrayList<ONDEXConcept>();
        for (int i = 0; i < 500; i++) {
            ONDEXConcept p = aog.getFactory().createConcept("Protein_" + i, dataSource, ccProtein, et);
            proteins.add(p);
        }

        List<ONDEXConcept> tf_proteins = new ArrayList<ONDEXConcept>();
        for (int i = 0; i < 500; i++) {
            ONDEXConcept p = aog.getFactory().createConcept("Protein_" + i, dataSource, ccProtein, et);
            tf_proteins.add(p);
        }

        List<ONDEXConcept> genes = new ArrayList<ONDEXConcept>();
        for (int i = 0; i < 500; i++) {
            ONDEXConcept g = aog.getFactory().createConcept("Gene_" + i, dataSource, ccGene, et);
            genes.add(g);
        }

        List<ONDEXConcept> tfs = new ArrayList<ONDEXConcept>();
        for (int i = 0; i < 500; i++) {
            ONDEXConcept tf = aog.getFactory().createConcept("TF_" + i, dataSource, ccTF, et);
            tfs.add(tf);
        }

        List<ONDEXConcept> enzymes = new ArrayList<ONDEXConcept>();
        for (int i = 0; i < 500; i++) {
            ONDEXConcept e = aog.getFactory().createConcept("Enzyme_" + i, dataSource, ccEnzyme, et);
            enzymes.add(e);
        }

        List<ONDEXConcept> reactions = new ArrayList<ONDEXConcept>();
        for (int i = 0; i < 500; i++) {
            ONDEXConcept r = aog.getFactory().createConcept("Reaction_" + i, dataSource, ccReaction, et);
            reactions.add(r);
        }

        List<ONDEXConcept> paths = new ArrayList<ONDEXConcept>();
        for (int i = 0; i < 500; i++) {
            ONDEXConcept path = aog.getFactory().createConcept("Pathway_" + i, dataSource, ccPathway, et);
            paths.add(path);
        }

        int v = 2;
        //create orthologs
        Iterator<ONDEXConcept> prots = proteins.iterator();
        Iterator<ONDEXConcept> prots2 = tf_proteins.iterator();
        for (ONDEXConcept ats : ts) {
            for (int i = 0; i < 20; i++) {
                ONDEXConcept prot = null;
                if (prots.hasNext()) {
                    prot = prots.next();
                } else {
                    prots = proteins.iterator();
                    prot = prots.next();
                }
                v++;
                ONDEXRelation r = aog.getFactory().createRelation(ats, prot, rts_h_s_s, et);
                r.createAttribute(att_blev, 0.05d / v, false);

                if (prots2.hasNext()) {
                    prot = prots2.next();
                } else {
                    prots2 = proteins.iterator();
                    prot = prots2.next();
                }
                v++;
                ONDEXRelation r2 = aog.getFactory().createRelation(ats, prot, rts_h_s_s, et);
                r2.createAttribute(att_blev, 0.05d / v, false);
            }
        }

        for (int i = 0; i < proteins.size(); i++) {
            ONDEXRelation r = aog.getFactory().createRelation(proteins.get(i),
                    proteins.get(proteins.size() - (i + 1)), rts_h_s_s, et);
            r.createAttribute(att_blev, 0.0005d * i, false);
        }

        for (int i = 0; i < proteins.size(); i++) {
            aog.getFactory().createRelation(
                    proteins.get(i),
                    enzymes.get(i),
                    rts_is_a, et);
        }
        //protein --> tf
        for (int i = 0; i < tf_proteins.size(); i++) {
            aog.getFactory().createRelation(
                    tf_proteins.get(i),
                    tfs.get(i),
                    rts_is_a, et);
        }

        //tf --> gene
        for (int i = 0; i < tfs.size(); i++) {
            aog.getFactory().createRelation(
                    genes.get(i),
                    tfs.get(i),
                    rts_regulates, et);
        }

        //gene --> protein
        for (int i = 0; i < genes.size(); i++) {
            aog.getFactory().createRelation(
                    genes.get(i),
                    proteins.get(i),
                    rts_Encodes, et);
        }

        for (int i = 0; i < enzymes.size(); i++) {
            aog.getFactory().createRelation(
                    reactions.get(i),
                    enzymes.get(i),
                    rts_cats, et);
        }

        for (int i = 0; i < paths.size(); i++) {
            aog.getFactory().createRelation(
                    reactions.get(i),
                    paths.get(i),
                    rts_m_isp, et);
        }


        Transformer transformer = new Transformer();
        transformer.setONDEXGraph(aog);

        ONDEXPluginArguments ta = new ONDEXPluginArguments(transformer.getArgumentDefinitions());
        ta.addOption(Transformer.STATE_MACHINE_DEF_ARG, file);
        ta.addOption(Transformer.MAKE_TAGS_VISIBLE_ARG, true);
        ta.addOption(Transformer.MAX_PATHWAY_LENGTH_ARG, Integer.MAX_VALUE);
        ta.addOption(Transformer.TRANSLATE_TAXID_ARG, Boolean.FALSE);
        ta.addOption(Transformer.LINKS_ARG, Boolean.FALSE);
        ta.addOption(Transformer.ZIP_FILE_ARG, Boolean.FALSE);
        ta.addOption(Transformer.INCLUDE_UNRANKABLE_ARG, Boolean.FALSE);
        transformer.setArguments(ta);

        transformer.start();

//        Export oxlExport = new Export();
//
//        ExportArguments ea = new ONDEXPluginArguments(oxlExport.getArgumentDefinitions());
//        File file = File.createTempFile("tmp", "tmp");
//        System.out.println(file.getAbsolutePath());
//        //file.deleteOnExit();
//        ea.setOption(FileArgumentDefinition.EXPORT_FILE, file.getAbsolutePath());
//        oxlExport.setArguments(ea);
//        oxlExport.setONDEXGraph(aog);
//        oxlExport.start();

//        assertEquals(120, aog.getConcepts().size());
//        assertEquals(100, aog.getRelations().size());


    }

}
