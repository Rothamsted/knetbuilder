package net.sourceforge.ondex.mapping.lowmemoryaccessionbased;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.config.LuceneRegistry;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.core.searchable.LuceneEnv;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.logging.ONDEXLogger;
import net.sourceforge.ondex.tools.DirUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Unit test for accession based mapping.
 *
 * @author peschr, hindlem
 */
public class MappingTest {

    protected String ondexDir = "data";

    protected MemoryONDEXGraph graph;

    private ONDEXLogger coreLogger = new ONDEXLogger();

    // map within concept class
    private ConceptClass ccProtein;

    // standard evidence type
    private EvidenceType etIMPD;

    // map between two CVs
    private DataSource dataSourceUniProt, dataSourceTair;

    private ConceptClass ccThing;

    private AttributeName taxid;

    private LuceneEnv lenv;

    /**
     * Static Strings for meta data.
     *
     * @author peschr
     */
    interface MetaData {
        public static String CC_Protein = "Protein";

        public static String CC_Thing = "Thing";

        public static String IMPD = "IMPD";

        public static String CV_UniProt = "UNIPROTKB";

        public static String CV_TAIR = "TAIR";

        public static String ATT_TAXID = "TAXID";
    }

    @Before
    public void setUp() {

        Assert.assertNotNull(ondexDir);
        graph = new MemoryONDEXGraph("test");

        ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
                .addONDEXONDEXListener(coreLogger);

        // setup meta data
        ccProtein = graph.getMetaData().getFactory().createConceptClass(
                MetaData.CC_Protein);

        ccThing = graph.getMetaData().getFactory().createConceptClass(
                MetaData.CC_Thing);

        etIMPD = graph.getMetaData().getFactory().createEvidenceType(
                MetaData.IMPD);
        dataSourceUniProt = graph.getMetaData().getFactory().createDataSource(
                MetaData.CV_UniProt);
        dataSourceTair = graph.getMetaData().getFactory().createDataSource(MetaData.CV_TAIR);

        taxid = graph.getMetaData().getFactory().createAttributeName(
                MetaData.ATT_TAXID, String.class);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        if (lenv != null) {
            lenv.cleanup();
            lenv.closeIndex();
        }
        lenv = null;
    }

    @Test
    public void testMapping() throws Exception {

        // RelationType rtSet = graph.getONDEXGraphData(s).getRelationType(s,
        // "is_p");
        // System.out.println(rtSet);

        // populate graph
        ONDEXConcept concept = graph.getFactory().createConcept("1234",
                dataSourceUniProt, ccProtein, etIMPD);
        concept.createConceptAccession("0003968", dataSourceUniProt, false);
        concept.createConceptAccession("aba0003969", dataSourceTair, false);
        concept.createConceptAccession("00039610", dataSourceTair, false);

        concept = graph.getFactory().createConcept("1236", dataSourceTair, ccThing,
                etIMPD);
        concept.createConceptAccession("0003968", dataSourceUniProt, false);
        concept.createConceptAccession("aba0003969", dataSourceTair, false);
        concept.createConceptAccession("123", dataSourceTair, false);

        concept = graph.getFactory().createConcept("1239", dataSourceUniProt,
                ccProtein, etIMPD);
        concept.createConceptAccession("ABA0003969", dataSourceTair, false);
        concept.createConceptAccession("bla", dataSourceTair, false);

        concept = graph.getFactory().createConcept("12310a", dataSourceUniProt,
                ccProtein, etIMPD);
        concept.createConceptAccession("bla", dataSourceTair, false);

        concept = graph.getFactory().createConcept("12310b", dataSourceUniProt,
                ccProtein, etIMPD);
        concept.createConceptAccession("bla", dataSourceTair, true);

        // perform mapping
        Mapping m = new Mapping();

        // this have to be called after the relations and concepts are created
        LuceneEnv env = loadLuceneEnv(graph);
        ONDEXPluginArguments arg = new ONDEXPluginArguments(m.getArgumentDefinitions());
        LuceneRegistry.sid2luceneEnv.put(graph.getSID(), env);
        arg.addOption(ArgumentNames.RELATION_TYPE_ARG, "equ");
        arg.addOption(ArgumentNames.EQUIVALENT_CC_ARG, "Thing,Protein");
        arg.addOption(ArgumentNames.IGNORE_AMBIGUOUS_ARG, false);
        arg.addOption(ArgumentNames.WITHIN_DATASOURCE_ARG, false);


        m.setArguments(arg);
        m.setONDEXGraph(graph);
        m.start();

        Assert.assertEquals(2, graph.getRelations().size());
    }

    @Test
    public void testGDSMapping() throws Exception {

        // RelationType rtSet = graph.getONDEXGraphData(s).getRelationType(s,
        // "is_p");
        // System.out.println(rtSet);

        // populate graph
        ONDEXConcept concept = graph.getFactory().createConcept("1234",
                dataSourceUniProt, ccProtein, etIMPD);
        concept.createConceptAccession("0003968", dataSourceUniProt, false);
        concept.createConceptAccession("aba0003969", dataSourceTair, false);
        concept.createConceptAccession("00039610", dataSourceTair, false);
        concept.createAttribute(taxid, "3705", false);
        Integer first = concept.getId();

        concept = graph.getFactory().createConcept("1236", dataSourceTair, ccThing,
                etIMPD);
        concept.createConceptAccession("0003968", dataSourceUniProt, false);
        concept.createConceptAccession("aba0003969", dataSourceTair, false);
        concept.createConceptAccession("123", dataSourceTair, false);
        concept.createAttribute(taxid, "3705", false);
        Integer second = concept.getId();

        concept = graph.getFactory().createConcept("1239", dataSourceUniProt,
                ccProtein, etIMPD);
        concept.createConceptAccession("aba0003969", dataSourceTair, false);
        concept.createConceptAccession("bla", dataSourceTair, false);
        concept.createAttribute(taxid, "3706", false);

        concept = graph.getFactory().createConcept("12310a", dataSourceUniProt,
                ccProtein, etIMPD);
        concept.createConceptAccession("bla", dataSourceTair, false);
        concept.createAttribute(taxid, "3702", false);

        concept = graph.getFactory().createConcept("12310b", dataSourceTair, ccProtein,
                etIMPD);
        concept.createConceptAccession("bla", dataSourceTair, true);
        concept.createAttribute(taxid, "3702", false);

        // perform mapping
        Mapping m = new Mapping();

        // this have to be called after the relations and concepts are created
        LuceneEnv env = loadLuceneEnv(graph);
        ONDEXPluginArguments arg = new ONDEXPluginArguments(m.getArgumentDefinitions());
        LuceneRegistry.sid2luceneEnv.put(graph.getSID(), env);
        arg.addOption(ArgumentNames.RELATION_TYPE_ARG, "equ");
        arg.addOption(ArgumentNames.EQUIVALENT_CC_ARG, "Thing,Protein");
        arg.addOption(ArgumentNames.IGNORE_AMBIGUOUS_ARG, false);
        arg.addOption(ArgumentNames.WITHIN_DATASOURCE_ARG, false);
        arg.addOption(ArgumentNames.ATTRIBUTE_EQUALS_ARG, "TAXID");

        m.setArguments(arg);
        m.setONDEXGraph(graph);
        m.start();

        Assert.assertEquals(1, graph.getRelations().size());

        RelationKey key = graph.getRelations().iterator().next().getKey();
        Assert.assertEquals(first.intValue(), key.getFromID());
        Assert.assertEquals(second.intValue(), key.getToID());

    }

    @Test
    public void testMapping2() throws Exception {

        for (int i = 0; i < 100; i++) {
            ONDEXConcept concept = graph.getFactory().createConcept("12310as",
                    graph.getMetaData().getFactory().createDataSource(i + "a"),
                    ccProtein, etIMPD);
            concept.createConceptAccession("bla", dataSourceTair, false);
        }

        for (int i = 0; i < 100; i++) {
            ONDEXConcept concept = graph.getFactory().createConcept("12310bs",
                    graph.getMetaData().getFactory().createDataSource(i + "b"),
                    ccProtein, etIMPD);
            concept.createConceptAccession("bla", dataSourceTair, false);
        }

        for (int i = 0; i < 100; i++) {
            ONDEXConcept concept = graph.getFactory().createConcept("12310bs",
                    graph.getMetaData().getFactory().createDataSource(i + "c"),
                    ccProtein, etIMPD);
            concept.createConceptAccession("bla", dataSourceTair, false);
        }

        // perform mapping
        Mapping m = new Mapping();

        // this have to be called after the relations and concepts are created
        LuceneEnv env = loadLuceneEnv(graph);
        ONDEXPluginArguments arg = new ONDEXPluginArguments(m.getArgumentDefinitions());
        LuceneRegistry.sid2luceneEnv.put(graph.getSID(), env);
        arg.addOption(ArgumentNames.RELATION_TYPE_ARG, "equ");

        arg.addOption(ArgumentNames.IGNORE_AMBIGUOUS_ARG, false);
        arg.addOption(ArgumentNames.WITHIN_DATASOURCE_ARG, false);


        m.setArguments(arg);
        m.setONDEXGraph(graph);
        m.start();

        Assert.assertEquals((100 * 299) * 3 / 2, graph.getRelations()
                .size());
    }

    @Test
    public void testhugeSet() throws Exception {

        for (int i = 0; i < 200; i++) {
            ONDEXConcept concept = graph.getFactory().createConcept("12310as",
                    dataSourceTair, ccProtein, etIMPD);
            concept.createConceptAccession("bla", dataSourceTair, false);
        }

        for (int i = 0; i < 200; i++) {
            ONDEXConcept concept = graph.getFactory().createConcept("12310bs",
                    dataSourceUniProt, ccProtein, etIMPD);
            concept.createConceptAccession("bla", dataSourceTair, false);
        }

        // perform mapping
        Mapping m = new Mapping();

        // this have to be called after the relations and concepts are created
        LuceneEnv env = loadLuceneEnv(graph);
        ONDEXPluginArguments arg = new ONDEXPluginArguments(m.getArgumentDefinitions());
        LuceneRegistry.sid2luceneEnv.put(graph.getSID(), env);
        arg.addOption(ArgumentNames.RELATION_TYPE_ARG, "equ");
        arg.addOption(ArgumentNames.EQUIVALENT_CC_ARG, "Thing,Protein");
        arg.addOption(ArgumentNames.IGNORE_AMBIGUOUS_ARG, false);
        arg.addOption(ArgumentNames.WITHIN_DATASOURCE_ARG, false);

        m.setArguments(arg);
        m.setONDEXGraph(graph);
        m.start();

        Assert.assertEquals(200 * 200, graph.getRelations().size());
    }

    @Test
    public void testWithinCV() throws InvalidPluginArgumentException {
        ONDEXConcept concept = graph.getFactory().createConcept("1234",
                dataSourceUniProt, ccProtein, etIMPD);
        concept.createConceptAccession("0003968", dataSourceUniProt, false);
        concept.createConceptAccession("aba0003969", dataSourceTair, false);
        concept.createConceptAccession("00039610", dataSourceTair, false);

        concept = graph.getFactory().createConcept("1236", dataSourceTair, ccThing,
                etIMPD);
        concept.createConceptAccession("0003968", dataSourceUniProt, false);
        concept.createConceptAccession("aba0003969", dataSourceTair, false);
        concept.createConceptAccession("123", dataSourceTair, false);

        concept = graph.getFactory().createConcept("1239", dataSourceUniProt,
                ccProtein, etIMPD);
        concept.createConceptAccession("aba0003969", dataSourceTair, false);
        concept.createConceptAccession("bla", dataSourceTair, false);

        concept = graph.getFactory().createConcept("12310a", dataSourceUniProt,
                ccProtein, etIMPD);
        concept.createConceptAccession("bla", dataSourceTair, false);

        concept = graph.getFactory().createConcept("12310b", dataSourceUniProt,
                ccProtein, etIMPD);
        concept.createConceptAccession("bla", dataSourceTair, true);

        // perform mapping
        Mapping m = new Mapping();

        // this have to be called after the relations and concepts are created
        LuceneEnv env = loadLuceneEnv(graph);
        ONDEXPluginArguments arg = new ONDEXPluginArguments(m.getArgumentDefinitions());
        LuceneRegistry.sid2luceneEnv.put(graph.getSID(), env);
        arg.addOption(ArgumentNames.RELATION_TYPE_ARG, "equ");
        arg.addOption(ArgumentNames.EQUIVALENT_CC_ARG, "Thing,Protein");
        arg.addOption(ArgumentNames.IGNORE_AMBIGUOUS_ARG, false);
        arg.addOption(ArgumentNames.WITHIN_DATASOURCE_ARG, true);

        m.setArguments(arg);
        m.setONDEXGraph(graph);
        m.start();

        Assert.assertEquals(4, graph.getRelations().size());
    }

    public LuceneEnv loadLuceneEnv(MemoryONDEXGraph graph) {

        String dir = ondexDir + File.separator + "index" + File.separator
                + System.currentTimeMillis();

        try {
            DirUtils.deleteTree(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        lenv = new LuceneEnv(dir, true);
        lenv.setONDEXGraph(graph);

        return lenv;
    }

}
