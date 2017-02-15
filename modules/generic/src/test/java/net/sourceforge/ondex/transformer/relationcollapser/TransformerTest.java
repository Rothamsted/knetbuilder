package net.sourceforge.ondex.transformer.relationcollapser;

import junit.framework.TestCase;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import net.sourceforge.ondex.filter.conceptclass.Filter;
import net.sourceforge.ondex.logging.ONDEXLogger;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class TransformerTest extends TestCase {

    ONDEXLogger coreLogger = new ONDEXLogger();

    ONDEXLogger pluginLogger = new ONDEXLogger();

    ONDEXGraph og = null;

    RelationType rt = null;

    DataSource dataSource1 = null;

    DataSource dataSource2 = null;

    ConceptClass cc1 = null;

    ConceptClass cc2 = null;

    @Test
    public void testStart() throws Throwable {

        Transformer t = new Transformer();
        t.addONDEXListener(pluginLogger);
        assertNotNull("tranformer null", t);

        ONDEXPluginArguments ta = new ONDEXPluginArguments(t
                .getArgumentDefinitions());
        assertNotNull("ta null", ta);
        ta.addOption(Transformer.RELATION_TYPE_ARG, rt.getId());
        ta.addOption(Transformer.CLONE_ATTRIBUTES_ARG, Boolean.TRUE);

        // initialise transformer
        t.setArguments(ta);
        t.setONDEXGraph(og);
        t.start();

        assertEquals("wrong number of concepts", 1, og.getConcepts().size());
        assertEquals("wrong number of relations", 0, og.getRelations().size());

        ONDEXConcept c = og.getConcepts().iterator().next();
        assertNotNull("merged concept null", c);
        assertEquals("wrong cv", dataSource2.getId() + ":" + dataSource1.getId(), c
                .getElementOf().getId());
        assertEquals("wrong cc", cc2.getId() + ":" + cc1.getId(), c.getOfType()
                .getId());
        for (Attribute attribute : c.getAttributes()) {
            System.out.println(attribute.getOfType().getId() + " = " + attribute.getValue());
        }

        // this is old collapser behaviour
        assertEquals("wrong number of Attribute", 2, c.getAttributes().size());
        //assertEquals("wrong number of Attribute in list", 2, ((Collection) c.getGDSs().iterator().next().getValue()).size());
    }

    @Override
    protected void setUp() throws Exception {
        og = new MemoryONDEXGraph("test");
        assertNotNull("graph null", og);
        ONDEXEventHandler.getEventHandlerForSID(og.getSID())
                .addONDEXONDEXListener(coreLogger);

        dataSource1 = og.getMetaData().createDataSource("unknown", "", "");
        assertNotNull("dataSource1 null", dataSource1);

        dataSource2 = og.getMetaData().createDataSource("PHI", "", "");
        assertNotNull("dataSource2 null", dataSource2);

        cc1 = og.getMetaData().createConceptClass("Protein", "", "", null);
        assertNotNull("cc1 null", cc1);

        cc2 = og.getMetaData().createConceptClass("Interaction", "", "", null);
        assertNotNull("cc2 null", cc2);

        EvidenceType et1 = og.getMetaData().createEvidenceType("IMPD", "", "");
        assertNotNull("et1 null", et1);
        Collection<EvidenceType> ets1 = new ArrayList<EvidenceType>();
        ets1.add(et1);

        EvidenceType et2 = og.getMetaData().createEvidenceType("M", "", "");
        assertNotNull("et2 null", et2);
        Collection<EvidenceType> ets2 = new ArrayList<EvidenceType>();
        ets2.add(et2);

        AttributeName pheno = og.getMetaData().createAttributeName("pheno", "", "", null, String.class, null);
        assertNotNull("pheno null", pheno);

        rt = og.getMetaData().createRelationType("int_w", "", "", "", false,
                false, false, false, null);
        assertNotNull("rt null", rt);

        ONDEXConcept c1 = og.createConcept("c1", "", "", dataSource1, cc1, ets1);
        assertNotNull("c1 null", c1);
        assertNotNull("gds1 null", c1.createAttribute(pheno, "value1", false));

        ONDEXConcept c2 = og.createConcept("c2", "", "", dataSource2, cc2, ets2);
        assertNotNull("c2 null", c2);
        assertNotNull("gds2 null", c2.createAttribute(pheno, "value2", false));

        ONDEXRelation r = og.createRelation(c1, c2, rt, ets1);
        assertNotNull("r null", r);
    }


    @Test
    public void testStartWithOXL() throws Throwable {
        og = new MemoryONDEXGraph("test");
        ONDEXEventHandler.getEventHandlerForSID(og.getSID())
                .addONDEXONDEXListener(coreLogger);

        parseOXLFile();

        Filter f = new Filter();
        f.addONDEXListener(pluginLogger);
        assertNotNull("filter null", f);

        ONDEXPluginArguments fa = new ONDEXPluginArguments(f.getArgumentDefinitions());
        fa.addOption(Filter.TARGETCC_ARG, "Publication");
        fa.addOption(Filter.TARGETCC_ARG, "Organism");
        fa.addOption(Filter.TARGETCC_ARG, "Class");
        fa.addOption(Filter.TARGETCC_ARG, "Disease");
        fa.addOption(Filter.EXCLUDE_ARG, true);

        // filter graph down
        f.setONDEXGraph(og);
        f.setArguments(fa);
        f.start();

        og = new MemoryONDEXGraph("test");
        ONDEXEventHandler.getEventHandlerForSID(og.getSID())
                .addONDEXONDEXListener(coreLogger);
        f.copyResultsToNewGraph(og);

        assertEquals("publication still there", 0, og
                .getConceptsOfConceptClass(
                        og.getMetaData().getConceptClass("Publication")).size());
        assertEquals("organism still there", 0, og.getConceptsOfConceptClass(
                og.getMetaData().getConceptClass("Organism")).size());
        assertEquals("class still there", 0, og.getConceptsOfConceptClass(
                og.getMetaData().getConceptClass("Class")).size());
        assertEquals("disease still there", 0, og.getConceptsOfConceptClass(
                og.getMetaData().getConceptClass("Disease")).size());
        assertEquals("wrong number concepts", 3812, og.getConcepts().size());
        assertEquals("wrong number relations", 2123, og.getRelations().size());

        // start unconnected filter
        net.sourceforge.ondex.filter.unconnected.Filter fi = new net.sourceforge.ondex.filter.unconnected.Filter();
        fi.addONDEXListener(pluginLogger);

        fa = new ONDEXPluginArguments(fi.getArgumentDefinitions());
        fi.setArguments(fa);
        fi.setONDEXGraph(og);
        fi.start();

        og = new MemoryONDEXGraph("test");
        ONDEXEventHandler.getEventHandlerForSID(og.getSID())
                .addONDEXONDEXListener(coreLogger);
        fi.copyResultsToNewGraph(og);

        assertEquals("wrong number concepts", 2246, og.getConcepts().size());
        assertEquals("wrong number relations", 2123, og.getRelations().size());

        Transformer t = new Transformer();
        t.addONDEXListener(pluginLogger);
        assertNotNull("tranformer null", t);

        ONDEXPluginArguments ta = new ONDEXPluginArguments(t
                .getArgumentDefinitions());
        assertNotNull("ta null", ta);
        ta.addOption(Transformer.RELATION_TYPE_ARG, "int_w");
        ta.addOption(Transformer.CLONE_ATTRIBUTES_ARG, Boolean.TRUE);
        ta.addOption(Transformer.CONCEPTCLASS_RESTRICTION_ARG, "Protein,Interaction");
        ta.addOption(Transformer.DATASOURCE_RESTRICTION_ARG, "PHI,PHI");

        // initialise transformer
        t.setArguments(ta);
        t.setONDEXGraph(og);
        t.start();

        assertTrue("wrong number of concepts", og.getConcepts().size() < 2246);
        assertTrue("wrong number of relations", og.getRelations().size() < 2123);
    }

    private void parseOXLFile() throws PluginConfigurationException {
        net.sourceforge.ondex.parser.oxl.Parser oxlParser = new net.sourceforge.ondex.parser.oxl.Parser();

        ONDEXPluginArguments pa = new ONDEXPluginArguments(oxlParser
                .getArgumentDefinitions());
        pa.addOption(
                net.sourceforge.ondex.parser.oxl.ArgumentNames.IMPORTFILE_ARG,
                new File("").getAbsolutePath() + File.separator + "src"
                        + File.separator + "test" + File.separator
                        + "resources" + File.separator + "net" + File.separator
                        + "sourceforge" + File.separator + "ondex"
                        + File.separator + "transformer" + File.separator
                        + "relationcollapser" + File.separator
                        + "pre-integrated_step_inparanoid_results.xml.gz");

        oxlParser.setArguments(pa);
        oxlParser.setONDEXGraph(og);
        oxlParser.start();
    }

    @Override
    protected void tearDown() throws Exception {
        og = null;
    }


}
