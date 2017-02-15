/**
 *
 */
package net.sourceforge.ondex.parser.oxl;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import net.sourceforge.ondex.export.oxl.Export;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author hindlem
 */
public class OXLParser {

    private ONDEXGraph og;
    private File testfile;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        og = new MemoryONDEXGraph("test");
        testfile = new File(System.getProperty("java.io.tmpdir") + File.separator + "testoxl.xml");
        testfile.deleteOnExit();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        og = null;
        testfile.delete();
    }

    /**
     * Test method for {@link net.sourceforge.ondex.parser.oxl.Parser#start()}.
     *
     * @TODO implement tests here
     */
    @Test
    public void testStart() {
        //fail("Not yet implemented");
    }

    @Test
    public void testRoundTrip() throws JAXBException, XMLStreamException, IOException, PluginConfigurationException {

        //create test graph

        DataSource dataSource = og.getMetaData().getFactory().createDataSource("cv");
        ConceptClass cc = og.getMetaData().getFactory().createConceptClass("cc");
        EvidenceType et = og.getMetaData().getFactory().createEvidenceType("et");

        ONDEXConcept c = og.getFactory().createConcept("concept", dataSource, cc, et);

        AttributeName an = og.getMetaData().getFactory().createAttributeName("an", List.class);
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);
        c.createAttribute(an, list, false);

        AttributeName colourAtt = og.getMetaData().getFactory().createAttributeName("colour", Color.class);
        Color colour = Color.WHITE;
        c.createAttribute(colourAtt, colour, false);

        ONDEXConcept c2 = og.getFactory().createConcept("concept2", dataSource, cc, et);

        RelationType rt = og.getMetaData().getFactory().createRelationType("rt");

        ONDEXRelation relation = og.getFactory().createRelation(c, c2, rt, et);

        relation.createAttribute(an, list, false);
        relation.createAttribute(colourAtt, colour, false);

        //export

        Export export = new Export();

        ONDEXPluginArguments ea = new ONDEXPluginArguments(export.getArgumentDefinitions());
        ea.setOption(FileArgumentDefinition.EXPORT_FILE, testfile.getAbsolutePath());
        ea.setOption(net.sourceforge.ondex.export.oxl.ArgumentNames.EXPORT_AS_ZIP_FILE, false);

        export.setONDEXGraph(og);
        export.setArguments(ea);
        export.start();

        //import

        ONDEXGraph g2 = new MemoryONDEXGraph("test2");

        Parser parser = new Parser();

        ONDEXPluginArguments pa = new ONDEXPluginArguments(parser.getArgumentDefinitions());
        pa.setOption(FileArgumentDefinition.INPUT_FILE, testfile.getAbsolutePath());

        parser.setONDEXGraph(g2);
        parser.setArguments(pa);
        parser.start();

        //check results

        assertEquals("There should be two concepts.", 2, g2.getConcepts().size());
        for (ONDEXConcept concept : g2.getConcepts()) {

            if (concept.getPID().equals(c.getPID())) {
                assertEquals(2, concept.getAttributes().size());

                Attribute attribute = concept.getAttribute(an);
                list = (List<Integer>) attribute.getValue();
                assertEquals("There should be three values in the list.", 3, list.size());
                assertTrue(list.contains(1));
                assertTrue(list.contains(2));
                assertTrue(list.contains(3));

                Attribute attribute2 = concept.getAttribute(colourAtt);
                assertEquals(Color.WHITE, attribute2.getValue());
            } else {
                assertEquals(0, concept.getAttributes().size());
            }
        }

        assertEquals("There should be one relation.", 1, g2.getRelations().size());
        for (ONDEXRelation relationT : g2.getRelations()) {

            assertEquals(2, relationT.getAttributes().size());

            Attribute attribute = relationT.getAttribute(an);
            list = (List<Integer>) attribute.getValue();
            assertEquals("There should be three values in the list.", 3, list.size());
            assertTrue(list.contains(1));
            assertTrue(list.contains(2));
            assertTrue(list.contains(3));

            Attribute attribute2 = relationT.getAttribute(colourAtt);
            assertEquals(Color.WHITE, attribute2.getValue());
        }

    }

}
