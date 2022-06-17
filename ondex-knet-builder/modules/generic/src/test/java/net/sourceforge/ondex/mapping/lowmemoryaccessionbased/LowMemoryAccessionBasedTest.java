/**
 *
 */
package net.sourceforge.ondex.mapping.lowmemoryaccessionbased;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.core.searchable.LuceneEnv;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import net.sourceforge.ondex.logging.ONDEXLogger;
import net.sourceforge.ondex.lucene.config.LuceneRegistry;
import net.sourceforge.ondex.parser.oxl.Parser;
import net.sourceforge.ondex.tools.DirUtils;
import net.sourceforge.ondex.workflow.ONDEXPluginArguments;

/**
 * A applied test case designed by catherine.c, implemented by matt.h
 *
 * @author hindlem, catherinec
 */
public class LowMemoryAccessionBasedTest {

    private static final String LUCENE_DIR = "target/index";
    private MemoryONDEXGraph graph;
    private ONDEXLogger coreLogger = new ONDEXLogger();

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

        graph = new MemoryONDEXGraph("test");

        ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
                .addONDEXONDEXListener(coreLogger);

    }

    private void parseOXLFile() throws PluginConfigurationException
    {
        Parser oxlParser = new net.sourceforge.ondex.parser.oxl.Parser();

        ONDEXPluginArguments pa = new ONDEXPluginArguments(oxlParser.getArgumentDefinitions());
        pa.addOption(
                net.sourceforge.ondex.parser.oxl.ArgumentNames.IMPORTFILE_ARG,
                new File("").getAbsolutePath() + File.separator + "src"
                        + File.separator + "test" + File.separator
                        + "resources" + File.separator + "net" + File.separator
                        + "sourceforge" + File.separator + "ondex"
                        + File.separator + "mapping" + File.separator
                        + "lowmemoryaccessionbased" + File.separator
                        + "testAccmap.xml");
        pa.addOption(
                net.sourceforge.ondex.parser.oxl.ArgumentNames.IGNORE_ATTRIBUTE_ARG,
                Boolean.FALSE.toString());

        oxlParser.setArguments(pa);
        oxlParser.setONDEXGraph(graph);
        oxlParser.start();
    }

    /**
     * Test method for
     * {@link net.sourceforge.ondex.mapping.lowmemoryaccessionbased.Mapping#start()}
     * <p/>
     * Tests a precreated graph in OXL described in the following table:
     * <table border="3" bordercolor="" width="100" bgcolor="">
     * <tr>
     * <td>OXL id--></td>
     * <td>1</td>
     * <td>2</td>
     * <td>3</td>
     * <td>4</td>
     * <td>5</td>
     * <td>6</td>
     * </tr>
     * <tr>
     * <td>ConceptClass</td>
     * <td>cc1</td>
     * <td>cc1</td>
     * <td>cc1</td>
     * <td>cc1</td>
     * <td>cc1</td>
     * <td>cc2</td>
     * </tr>
     * <tr>
     * <td>DataSource</td>
     * <td>dataSource1</td>
     * <td>dataSource1</td>
     * <td>dataSource2</td>
     * <td>dataSource1</td>
     * <td>dataSource2</td>
     * <td>dataSource2</td>
     * </tr>
     * <tr>
     * <td>DataSource for Accession</td>
     * <td>dataSource1</td>
     * <td>dataSource1</td>
     * <td>dataSource1</td>
     * <td>cv10</td>
     * <td>dataSource1</td>
     * <td>cv10</td>
     * </tr>
     * <tr>
     * <td>Accession Value</td>
     * <td>10</td>
     * <td>10</td>
     * <td>10</td>
     * <td>10</td>
     * <td>10</td>
     * <td>10</td>
     * </tr>
     * <tr>
     * <td>DataSource for Accession 2</td>
     * <td>na</td>
     * <td>na</td>
     * <td>dataSource1</td>
     * <td>na</td>
     * <td>na</td>
     * <td>na</td>
     * </tr>
     * <tr>
     * <td>Accession Value 2</td>
     * <td>na</td>
     * <td>na</td>
     * <td>10</td>
     * <td>na</td>
     * <td>na</td>
     * <td>na</td>
     * </tr>
     * <tr>
     * <td>Is Ambiguous Accesson?</td>
     * <td>false</td>
     * <td>false</td>
     * <td>false</td>
     * <td>false</td>
     * <td>false</td>
     * <td>false</td>
     * </tr>
     * </table>
     */
    @Test
    public void testApplication() throws Exception
    {

        parseOXLFile();

        Mapping lma_mapping = new Mapping();

        // this have to be called after the relations and concepts are created
        loadLuceneEnv(graph);
        ONDEXPluginArguments arg = new ONDEXPluginArguments(lma_mapping.getArgumentDefinitions());
        arg.addOption(ArgumentNames.RELATION_TYPE_ARG, "equ");
        // arg.addOption(ArgumentNames.EQUIVALENT_CC_ARG, "Thing,Protein");
        arg.addOption(ArgumentNames.IGNORE_AMBIGUOUS_ARG, false);
        arg.addOption(ArgumentNames.WITHIN_DATASOURCE_ARG, false);

        lma_mapping.setArguments(arg);
        lma_mapping.setONDEXGraph(graph);
        lma_mapping.start();

        Boolean[][] results = new Boolean[7][7];
        for (Boolean[] row : results)
            Arrays.fill(row, Boolean.FALSE);

        results[1][3] = true;
        results[1][5] = true;
        results[2][3] = true;
        results[2][5] = true;
        results[3][1] = true;
        results[3][2] = true;
        results[3][4] = true;
        results[4][3] = true;
        results[4][5] = true;
        results[5][1] = true;
        results[5][2] = true;
        results[5][4] = true;

        Set<ONDEXRelation> mappings = graph
                .getRelationsOfRelationType(graph.getMetaData()
                        .getRelationType("equ"));
        for (ONDEXRelation relation : mappings) {
            Integer from = relation.getKey().getFromID();
            Integer to = relation.getKey().getToID();
            Assert.assertTrue(from + "-" + to + " mapping invalid",
                    results[from][to]);
        }
    }

    public static LuceneEnv loadLuceneEnv(ONDEXGraph graph) throws IOException 
    {
        DirUtils.deleteTree(LUCENE_DIR);

        LuceneEnv lenv = new LuceneEnv(LUCENE_DIR, true);
        lenv.setONDEXGraph(graph);

        LuceneRegistry.sid2luceneEnv.put(graph.getSID(), lenv );
        
        return lenv;
    }

}
