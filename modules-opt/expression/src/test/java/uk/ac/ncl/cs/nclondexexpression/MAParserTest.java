/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.nclondexexpression;

import java.util.Map;
import java.util.Set;
import junit.framework.Assert;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.oxl.Parser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jweile
 */
public class MAParserTest {

    private ONDEXGraph graph;

    public MAParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        graph = new MemoryONDEXGraph("test");

        ONDEXParser oxlParser = new Parser();
        oxlParser.setONDEXGraph(graph);
        ONDEXPluginArguments args = new ONDEXPluginArguments(oxlParser.getArgumentDefinitions());
        args.addOption(FileArgumentDefinition.INPUT_FILE, "src/test/resources/ondex_metadata.xml");
        oxlParser.setArguments(args);
        oxlParser.start();
    }

    @After
    public void tearDown() {
        graph = null;
    }

    
     @Test
     public void testParser() throws Exception {

        ONDEXParser parser = new MAParser();

        parser.setONDEXGraph(graph);

        ONDEXPluginArguments args = new ONDEXPluginArguments(parser.getArgumentDefinitions());
        args.addOption(FileArgumentDefinition.INPUT_FILE, "src/test/resources/mockup.tsv");
        parser.setArguments(args);

        parser.start();

        AttributeName anExp = graph.getMetaData().getAttributeName("EXPMAP");

        Set<ONDEXConcept> concepts = graph.getConcepts();
        Assert.assertEquals("Incorrect number of concepts created", 2, concepts.size());

        for (ONDEXConcept c: concepts) {
            Attribute gds = c.getAttribute(anExp);
            Map<String,Integer> map = (Map<String,Integer>) gds.getValue();
            Assert.assertEquals("Incorrect number of expression values", 2, map.size());
        }

     }

}