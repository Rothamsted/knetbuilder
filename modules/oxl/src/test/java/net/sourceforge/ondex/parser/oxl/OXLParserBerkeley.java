/**
 *
 */
package net.sourceforge.ondex.parser.oxl;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.core.persistent.BerkeleyEnv;
import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import net.sourceforge.ondex.export.oxl.Export;
import net.sourceforge.ondex.logging.ONDEXLogger;
import net.sourceforge.ondex.tools.DirUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author hindlem, taubertj
 */
public class OXLParserBerkeley extends TestCase {

    private ONDEXGraph og;
    
	private BerkeleyEnv env;

	private String path;
    
    private File testfile;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    @Override
    public void setUp() throws Exception {
    	String name = this.getClass().getName();
    	
    	path = Config.ondexDir + File.separator + "dbs" + File.separator + name;

		File dir = new File(path);
		if (dir.exists())
			DirUtils.deleteTree(dir);
		dir.deleteOnExit();

		dir.mkdir();
		assertTrue(dir.getAbsolutePath(), dir.canRead());
		assertTrue(dir.getAbsolutePath(), dir.canWrite());

		env = new BerkeleyEnv(path, name, new ONDEXLogger());
		og = env.getAbstractONDEXGraph();
		
        testfile = new File(System.getProperty("java.io.tmpdir") + File.separator + "testoxl.xml");
        testfile.deleteOnExit();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    @Override
    public void tearDown() throws Exception {
		env.cleanup();
		env = null;
		DirUtils.deleteTree(path);
        testfile.delete();
    }

    @Test
    public void testRoundTrip() throws JAXBException, XMLStreamException, IOException, PluginConfigurationException {

        //create test graph

        ONDEXGraph g2 = new MemoryONDEXGraph("test2");
    	
        DataSource dataSource = g2.getMetaData().getFactory().createDataSource("cv");
        ConceptClass cc = g2.getMetaData().getFactory().createConceptClass("cc");
        EvidenceType et = g2.getMetaData().getFactory().createEvidenceType("et");

        ONDEXConcept c = g2.getFactory().createConcept("concept", dataSource, cc, et);

        AttributeName an = g2.getMetaData().getFactory().createAttributeName("an", List.class);
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);
        c.createAttribute(an, list, false);

        AttributeName colourAtt = g2.getMetaData().getFactory().createAttributeName("colour", Color.class);
        Color colour = Color.WHITE;
        c.createAttribute(colourAtt, colour, false);

        ONDEXConcept c2 = g2.getFactory().createConcept("concept2", dataSource, cc, et);

        RelationType rt = g2.getMetaData().getFactory().createRelationType("rt");

        ONDEXRelation relation = g2.getFactory().createRelation(c, c2, rt, et);

        relation.createAttribute(an, list, false);
        relation.createAttribute(colourAtt, colour, false);

        //export

        Export export = new Export();

        ONDEXPluginArguments ea = new ONDEXPluginArguments(export.getArgumentDefinitions());
        ea.setOption(FileArgumentDefinition.EXPORT_FILE, testfile.getAbsolutePath());
        ea.setOption(net.sourceforge.ondex.export.oxl.ArgumentNames.EXPORT_AS_ZIP_FILE, false);

        export.setONDEXGraph(g2);
        export.setArguments(ea);
        export.start();

        //import

        Parser parser = new Parser();

        ONDEXPluginArguments pa = new ONDEXPluginArguments(parser.getArgumentDefinitions());
        pa.setOption(FileArgumentDefinition.INPUT_FILE, testfile.getAbsolutePath());

        parser.setONDEXGraph(og);
        parser.setArguments(pa);
        parser.start();

        //check results

        assertEquals("There should be two concepts.", 2, og.getConcepts().size());
        for (ONDEXConcept concept : og.getConcepts()) {

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

        assertEquals("There should be one relation.", 1, og.getRelations().size());
        for (ONDEXRelation relationT : og.getRelations()) {

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
