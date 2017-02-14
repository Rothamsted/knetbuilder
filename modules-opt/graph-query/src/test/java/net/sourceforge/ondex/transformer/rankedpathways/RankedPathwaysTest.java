package net.sourceforge.ondex.transformer.rankedpathways;

import junit.framework.TestCase;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.algorithm.graphquery.nodepath.EvidencePathNode;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.logging.ONDEXLogger;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author hindlem
 */
public class RankedPathwaysTest extends TestCase {

    private ONDEXGraph aog;

    private ConceptClass ccTS;
    private ConceptClass ccProtein;
    private ConceptClass ccTF;
    private ConceptClass ccGene;
    private ConceptClass ccEnzyme;
    private ConceptClass ccReaction;
    private ConceptClass ccPathway;

//	private RelationType rts_Encodes;
    private RelationType rts_is_a;
    private RelationType rts_m_isp;
//	private RelationType rts_interacts;
//	private RelationType rts_regulates;
    private RelationType rts_cats;
    private RelationType rts_h_s_s;

    private EvidenceType et;

    private DataSource dataSource;

    private String file;

    private AttributeName att_blev;
    private AttributeName score;

    @Override
    protected void setUp() throws Exception {
        aog = new MemoryONDEXGraph("testGraph");
        ONDEXEventHandler.getEventHandlerForSID(aog.getSID()).addONDEXONDEXListener(new ONDEXLogger());

        ccTS = aog.getMetaData().getFactory().createConceptClass("TARGETSEQUENCE");
        ccProtein = aog.getMetaData().getFactory().createConceptClass("Protein");
        aog.getMetaData().getFactory().createConceptClass("TF");
        aog.getMetaData().getFactory().createConceptClass("Gene");
        ccEnzyme = aog.getMetaData().getFactory().createConceptClass("Enzyme");
        ccReaction = aog.getMetaData().getFactory().createConceptClass("Reaction");
        ccPathway = aog.getMetaData().getFactory().createConceptClass("Pathway");

        aog.getMetaData().getFactory().createRelationType("en_by");
        rts_is_a = aog.getMetaData().getFactory().createRelationType("is_a");
        rts_m_isp = aog.getMetaData().getFactory().createRelationType("m_isp");
        aog.getMetaData().getFactory().createRelationType("interacts");
        aog.getMetaData().getFactory().createRelationType("rg_by");
        rts_cats = aog.getMetaData().getFactory().createRelationType("cat_by");
        rts_h_s_s = aog.getMetaData().getFactory().createRelationType("h_s_s");

        et = aog.getMetaData().getFactory().createEvidenceType("I_made_it_up");
        dataSource = aog.getMetaData().getFactory().createDataSource("matts_db");

        att_blev = aog.getMetaData().getFactory().createAttributeName("BLEV", Double.class);
        score = aog.getMetaData().getFactory().createAttributeName("SCORE", Integer.class);

        try {
            file = File.createTempFile("tmp" + System.currentTimeMillis(), "tmp").getAbsolutePath();
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            //write states
            bw.append("#Finite States *=start state ^=end state");
            bw.newLine();
            bw.append("1*	TARGETSEQUENCE"); //36
            bw.newLine();
            bw.append("2	Protein"); //37
            bw.newLine();
            bw.append("3	Gene");//38
            bw.newLine();
            bw.append("4	TF"); //39
            bw.newLine();
            bw.append("5	Enzyme"); //40
            bw.newLine();
            bw.append("6	Reaction");        //41
            bw.newLine();
            bw.append("7^	Pathway"); //42
            bw.newLine();
            //write transitions
            bw.append("#Transitions");
            bw.newLine();
            bw.append("1-2	h_s_s		d"); //41
            bw.newLine();
            bw.append("2-5	is_a");            //42
            bw.newLine();
            bw.append("5-6	cat_by		d");            //43
            bw.newLine();
            bw.append("6-7	m_isp");            //44
            bw.newLine();
            bw.append("2-4	is_a");            //45
            bw.newLine();
            bw.append("4-3	rg_by		d");            //46
            bw.newLine();
            bw.append("2-3	en_by");                    //47
            bw.newLine();
            bw.append("2-2	h_s_s");                    //48
            bw.newLine();
            bw.append("#Weightings on transitions	AttributeName	Modifiers(CSV i=inverted lower is better, m=modulus values e.g. (i,m))	Relative Rank	TYPE(sum, mean, probability)");
            bw.newLine();
            bw.append("1-2	BLEV	i	1	probability");
            bw.newLine();
            bw.append("2-2	BLEV	i	3	probability");
            bw.newLine();
            bw.append("5-6	SCORE		2	sum"); //this is biologicaly meaningless its a test
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tearDown() {
        aog = null;
    }

    @Test
    public void testBasic() throws InvalidPluginArgumentException, IOException {

        ONDEXConcept ts = aog.getFactory().createConcept("TARGETSEQUENCE", dataSource, ccTS, et);
        ONDEXConcept p = aog.getFactory().createConcept("Protein", dataSource, ccProtein, et);
        ONDEXConcept e = aog.getFactory().createConcept("Enzyme", dataSource, ccEnzyme, et);
        ONDEXConcept r = aog.getFactory().createConcept("Reaction", dataSource, ccReaction, et);
        ONDEXConcept path = aog.getFactory().createConcept("Pathway", dataSource, ccPathway, et);

        aog.getFactory().createRelation(ts, p, rts_h_s_s, et);
        aog.getFactory().createRelation(p, e, rts_is_a, et);
        aog.getFactory().createRelation(e, r, rts_cats, et);
        aog.getFactory().createRelation(r, path, rts_m_isp, et);

        Transformer transformer = new Transformer();
        transformer.setONDEXGraph(aog);

        ONDEXPluginArguments ta = new ONDEXPluginArguments(transformer.getArgumentDefinitions());
        ta.addOption(Transformer.STATE_MACHINE_DEF_ARG, file);
        ta.addOption(Transformer.MAKE_TAGS_VISIBLE_ARG, true);
        ta.addOption(Transformer.MAX_PATHWAY_LENGTH_ARG, Integer.MAX_VALUE);
        ta.addOption(Transformer.TRANSLATE_TAXID_ARG, Boolean.FALSE);
        ta.addOption(Transformer.LINKS_ARG, Boolean.FALSE);
        ta.addOption(Transformer.ZIP_FILE_ARG, Boolean.FALSE);
        ta.addOption(Transformer.INCLUDE_UNRANKABLE_ARG, Boolean.TRUE);

        transformer.setArguments(ta);

        transformer.start();

        Map<ONDEXConcept, Set<EvidencePathNode>> paths = transformer.getPaths();
        assertEquals(1, paths.size());
        assertEquals(5, aog.getConcepts().size());
        assertEquals(4, aog.getRelations().size());

    }

    @Test
    public void testLowerRankedBranch() throws InvalidPluginArgumentException, IOException {

        ONDEXConcept ts = aog.getFactory().createConcept("TARGETSEQUENCE", dataSource, ccTS, et);
        ONDEXConcept p = aog.getFactory().createConcept("Protein", dataSource, ccProtein, et);
        ONDEXConcept p2 = aog.getFactory().createConcept("Protein2", dataSource, ccProtein, et);

        ONDEXConcept e = aog.getFactory().createConcept("Enzyme", dataSource, ccEnzyme, et);
        ONDEXConcept r = aog.getFactory().createConcept("Reaction", dataSource, ccReaction, et);
        ONDEXConcept path = aog.getFactory().createConcept("Pathway", dataSource, ccPathway, et);

        //better route
        ONDEXRelation r1 = aog.getFactory().createRelation(ts, p, rts_h_s_s, et);
        r1.createAttribute(att_blev, 0.0005d, false);
        ONDEXRelation r2 = aog.getFactory().createRelation(p, ts, rts_h_s_s, et);
        r2.createAttribute(att_blev, 0.0005d, false);
        ONDEXRelation r3 = aog.getFactory().createRelation(p, p, rts_h_s_s, et);
        r3.createAttribute(att_blev, 0.0005d, false);

        //worse route
        ONDEXRelation r4 = aog.getFactory().createRelation(ts, p2, rts_h_s_s, et);
        r4.createAttribute(att_blev, 0.05d, false);

        assertEquals(r1, aog.getRelation(ts, p, rts_h_s_s));
        assertEquals(r4, aog.getRelation(ts, p2, rts_h_s_s));
        assertEquals(r3, aog.getRelation(p, p, rts_h_s_s));

        aog.getFactory().createRelation(p, e, rts_is_a, et);
        aog.getFactory().createRelation(p2, e, rts_is_a, et);
        aog.getFactory().createRelation(e, r, rts_cats, et);
        aog.getFactory().createRelation(r, path, rts_m_isp, et);

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

        Map<ONDEXConcept, Set<EvidencePathNode>> paths = transformer.getPaths();
        assertEquals(1, paths.size());

        for (Set<EvidencePathNode> pathsOnCid : paths.values()) {
            System.out.println("new seed");
            for (EvidencePathNode pathI : pathsOnCid) {
                System.out.println("Path " + pathI);
            }
        }

        assertEquals(null, aog.getRelation(ts, p2, rts_h_s_s));
        assertEquals(r2, aog.getRelation(p, ts, rts_h_s_s));
        assertEquals(r1, aog.getRelation(ts, p, rts_h_s_s));
    }

    @Test
    public void testWithLimit() throws InvalidPluginArgumentException, IOException {

        ONDEXConcept ts = aog.getFactory().createConcept("TARGETSEQUENCE", dataSource, ccTS, et);
        ONDEXConcept p = aog.getFactory().createConcept("Protein", dataSource, ccProtein, et);
        ONDEXConcept p2 = aog.getFactory().createConcept("Protein2", dataSource, ccProtein, et);

        ONDEXConcept e = aog.getFactory().createConcept("Enzyme", dataSource, ccEnzyme, et);
        ONDEXConcept r = aog.getFactory().createConcept("Reaction", dataSource, ccReaction, et);
        ONDEXConcept path = aog.getFactory().createConcept("Pathway", dataSource, ccPathway, et);

        //better route
        ONDEXRelation r1 = aog.getFactory().createRelation(ts, p, rts_h_s_s, et);
        r1.createAttribute(att_blev, 0.0005d, false);
        ONDEXRelation r2 = aog.getFactory().createRelation(p, ts, rts_h_s_s, et);
        r2.createAttribute(att_blev, 0.0005d, false);

        //worse route
        ONDEXRelation r3 = aog.getFactory().createRelation(ts, p2, rts_h_s_s, et);
        r3.createAttribute(att_blev, 0.05d, false);

        aog.getFactory().createRelation(p, e, rts_is_a, et);
        aog.getFactory().createRelation(p2, e, rts_is_a, et);
        aog.getFactory().createRelation(e, r, rts_cats, et);
        aog.getFactory().createRelation(r, path, rts_m_isp, et);

        Transformer transformer = new Transformer();
        transformer.setONDEXGraph(aog);

        ONDEXPluginArguments ta = new ONDEXPluginArguments(transformer.getArgumentDefinitions());
        ta.addOption(Transformer.STATE_MACHINE_DEF_ARG, file);
        ta.addOption(Transformer.MAKE_TAGS_VISIBLE_ARG, true);
        ta.addOption(Transformer.MAX_PATHWAY_LENGTH_ARG, 5);
        ta.addOption(Transformer.TRANSLATE_TAXID_ARG, Boolean.FALSE);
        ta.addOption(Transformer.LINKS_ARG, Boolean.FALSE);
        ta.addOption(Transformer.ZIP_FILE_ARG, Boolean.FALSE);
        ta.addOption(Transformer.INCLUDE_UNRANKABLE_ARG, Boolean.TRUE);

        transformer.setArguments(ta);

        transformer.start();

        Map<ONDEXConcept, Set<EvidencePathNode>> paths = transformer.getPaths();
        assertEquals(1, paths.size());

        for (Set<EvidencePathNode> pathsOnCid : paths.values()) {
            System.out.println("new seed");
            for (EvidencePathNode pathI : pathsOnCid) {
                System.out.println("Path " + pathI);
            }
        }

        assertEquals(null, aog.getRelation(ts, p2, rts_h_s_s));
        assertEquals(r1, aog.getRelation(ts, p, rts_h_s_s));


        ta = new ONDEXPluginArguments(transformer.getArgumentDefinitions());
        ta.addOption(Transformer.STATE_MACHINE_DEF_ARG, file);
        ta.addOption(Transformer.MAKE_TAGS_VISIBLE_ARG, true);
        ta.addOption(Transformer.MAX_PATHWAY_LENGTH_ARG, 3);
        ta.addOption(Transformer.TRANSLATE_TAXID_ARG, Boolean.FALSE);
        ta.addOption(Transformer.LINKS_ARG, Boolean.FALSE);
        ta.addOption(Transformer.ZIP_FILE_ARG, Boolean.FALSE);
        ta.addOption(Transformer.INCLUDE_UNRANKABLE_ARG, Boolean.TRUE);

        transformer.setArguments(ta);

        transformer.start();

        assertEquals(0, aog.getRelations().size());
    }


    @Test
    public void testNonInvertedValue() throws InvalidPluginArgumentException, IOException {
        ONDEXConcept ts = aog.getFactory().createConcept("TARGETSEQUENCE", dataSource, ccTS, et);
        ONDEXConcept p = aog.getFactory().createConcept("Protein", dataSource, ccProtein, et);
        ONDEXConcept e = aog.getFactory().createConcept("Enzyme", dataSource, ccEnzyme, et);
        ONDEXConcept e2 = aog.getFactory().createConcept("Enzyme2", dataSource, ccEnzyme, et);
        ONDEXConcept r = aog.getFactory().createConcept("Reaction", dataSource, ccReaction, et);
        ONDEXConcept path = aog.getFactory().createConcept("Pathway", dataSource, ccPathway, et);

        ONDEXRelation r1 = aog.getFactory().createRelation(ts, p, rts_h_s_s, et);
        r1.createAttribute(att_blev, 0.0005d, false);

        aog.getFactory().createRelation(p, e, rts_is_a, et);
        aog.getFactory().createRelation(p, e2, rts_is_a, et);

        ONDEXRelation cat1 = aog.getFactory().createRelation(e, r, rts_cats, et);
        cat1.createAttribute(score, 1, false);

        ONDEXRelation cat2 = aog.getFactory().createRelation(e2, r, rts_cats, et);
        cat2.createAttribute(score, 5, false);

        assertEquals(cat1, aog.getRelation(e, r, rts_cats));
        assertEquals(cat2, aog.getRelation(e2, r, rts_cats));

        aog.getFactory().createRelation(r, path, rts_m_isp, et);

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

        Map<ONDEXConcept, Set<EvidencePathNode>> paths = transformer.getPaths();
        assertEquals(1, paths.size());
        assertEquals(null, aog.getRelation(e, r, rts_cats));
        assertEquals(cat2, aog.getRelation(e2, r, rts_cats));
    }

}
