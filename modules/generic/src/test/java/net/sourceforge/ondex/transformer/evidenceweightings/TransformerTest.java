/**
 *
 */
package net.sourceforge.ondex.transformer.evidenceweightings;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Properties;

/**
 * @author hindlem
 */
public class TransformerTest {

    private EvidenceType ev_ida;
    private EvidenceType ev_iss;
    private EvidenceType ev_iea;
    private EvidenceType ev_tas;

    private EvidenceType ev_db;

    private ConceptClass goTerm;
    private ConceptClass protein;

    private RelationType in_cat;

    private DataSource dataSource;

    private ONDEXGraph aog;
    private ONDEXRelation r1;
    private ONDEXRelation r2;
    private ONDEXRelation r4;
    private ONDEXRelation r3;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

        aog = new MemoryONDEXGraph("testGraph");

        ev_db = aog.getMetaData().getFactory().createEvidenceType("DB");

        ev_ida = aog.getMetaData().getFactory().createEvidenceType("IDA");
        ev_iss = aog.getMetaData().getFactory().createEvidenceType("ISS");
        ev_tas = aog.getMetaData().getFactory().createEvidenceType("TAS");
        ev_iea = aog.getMetaData().getFactory().createEvidenceType("IEA");

        goTerm = aog.getMetaData().getFactory().createConceptClass("GOTerm");
        protein = aog.getMetaData().getFactory().createConceptClass("Protein");

        in_cat = aog.getMetaData().getFactory().createRelationType("in_cat");

        dataSource = aog.getMetaData().getFactory().createDataSource("MY_TEST_CASE");

        ONDEXConcept go_term = aog.getFactory().createConcept("go_term_1", dataSource,
                goTerm, ev_db);
        ONDEXConcept go_term2 = aog.getFactory().createConcept("go_term_2", dataSource,
                goTerm, ev_db);

        ONDEXConcept protein1 = aog.getFactory().createConcept("protein1", dataSource,
                protein, ev_db);
        ONDEXConcept protein2 = aog.getFactory().createConcept("protein2", dataSource,
                protein, ev_db);
        ONDEXConcept protein3 = aog.getFactory().createConcept("protein3", dataSource,
                protein, ev_db);
        ONDEXConcept protein4 = aog.getFactory().createConcept("protein4", dataSource,
                protein, ev_db);

        HashSet<EvidenceType> evi = new HashSet<EvidenceType>();
        evi.add(ev_tas);
        evi.add(ev_iea);

        r1 = aog.getFactory().createRelation(protein1, go_term, in_cat, ev_ida);
        r2 = aog.getFactory().createRelation(protein2, go_term, in_cat, ev_iss);
        r3 = aog.getFactory()
                .createRelation(protein3, go_term2, in_cat, ev_tas);
        r4 = aog.createRelation(protein4, go_term2, in_cat, evi);

    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void testAnnotateGraph() throws Exception {
        Transformer transformer = new Transformer();
        transformer.setONDEXGraph(aog);

        Properties p = new Properties();
        p.put("IDA", "1.0");
        p.put("ISS", "0.9");
        p.put("TAS", "0.9");
        p.put("IEA", "0.5");

        transformer.annotateGraph(p, true, false);

        AttributeName att = aog.getMetaData().getAttributeName(
                MetaData.ATT_GO_EC_WEIGHTINGS);

        Attribute attribute1 = r1.getAttribute(att);
        Attribute attribute2 = r2.getAttribute(att);
        Attribute attribute3 = r3.getAttribute(att);
        Attribute attribute4 = r4.getAttribute(att);

        Assert.assertNotNull(attribute1);
        Assert.assertNotNull(attribute2);
        Assert.assertNotNull(attribute3);
        Assert.assertNotNull(attribute4);

        Assert.assertEquals(1d, (Double) attribute1.getValue(), 0);
        Assert.assertEquals(0.9d, (Double) attribute2.getValue(), 0);
        Assert.assertEquals(0.9d, (Double) attribute3.getValue(), 0);
        Assert.assertEquals(0.9d, (Double) attribute4.getValue(), 0);
    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void testStart() throws Exception {
        Transformer transformer = new Transformer();
        transformer.setONDEXGraph(aog);

        File file = File
                .createTempFile("prop", "" + System.currentTimeMillis());
        file.deleteOnExit();

        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write("IDA=1.0");
        bw.newLine();
        bw.write("ISS=0.9");
        bw.newLine();
        bw.write("TAS=0.9");
        bw.newLine();
        bw.write("IEA=0.5");
        bw.newLine();
        bw.flush();

        ONDEXPluginArguments ta = new ONDEXPluginArguments(transformer.getArgumentDefinitions());
        ta.setOption(FileArgumentDefinition.INPUT_FILE, file.getAbsolutePath());
        ta.setOption(ArgumentNames.ANALYSE_RELATIONS_ARG, true);
        ta.setOption(ArgumentNames.ANALYSE_CONCEPTS_ARG, false);

        transformer.setArguments(ta);
        transformer.start();

        AttributeName att = aog.getMetaData().getAttributeName(
                MetaData.ATT_GO_EC_WEIGHTINGS);

        Attribute attribute1 = r1.getAttribute(att);
        Attribute attribute2 = r2.getAttribute(att);
        Attribute attribute3 = r3.getAttribute(att);
        Attribute attribute4 = r4.getAttribute(att);

        Assert.assertNotNull(attribute1);
        Assert.assertNotNull(attribute2);
        Assert.assertNotNull(attribute3);
        Assert.assertNotNull(attribute4);

        Assert.assertEquals(1d, (Double) attribute1.getValue(), 0);
        Assert.assertEquals(0.9d, (Double) attribute2.getValue(), 0);
        Assert.assertEquals(0.9d, (Double) attribute3.getValue(), 0);
        Assert.assertEquals(0.9d, (Double) attribute4.getValue(), 0);

    }

}
