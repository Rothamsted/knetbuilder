/**
 *
 */
package net.sourceforge.ondex;


import net.sourceforge.ondex.init.PluginDescription;
import net.sourceforge.ondex.init.PluginRegistry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author lysenkoa
 */
public class PluginRegistryTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testBeanParsingFromXML() {
        String currentdir = System.getProperties().getProperty("user.dir");
        if (currentdir.endsWith("project-files")) {
            System.getProperties().setProperty("user.dir", new File(currentdir).getParent() + "/ondex-parent/workflow/workflow-impl/");
        }

        try {
            PluginRegistry pr = PluginRegistry.init(true, "src" + File.separator +
                    "test" + File.separator +
                    "resources" + File.separator +
                    "net" + File.separator +
                    "sourceforge" + File.separator +
                    "ondex" + File.separator +
                    "workflow" + File.separator);

            List<PluginDescription> plugins = pr.getAllPlugins();
            for (PluginDescription plugin : plugins) {
                System.out.println(plugin.getCls());
            }

            //Assert.assertEquals(5 + 2 + 1, plugins.size());   //5 cycs..2 oxl...and a graph, only on local machine
            Assert.assertEquals(0, plugins.size());   // just one on server

            for (PluginDescription plugin : plugins) {
                if (plugin.getCls().equals("net.sourceforge.ondex.parser.aracyc.Parser")) {
                    Assert.assertEquals("Parser for the aracyc flatfile database. The data flow is " +
                            "Parser ->\n AbstractReader -> AbstractParser -> AbstractTransformer", plugin.getDescription());
                    Assert.assertEquals("parser", plugin.getOndexType().getName().toLowerCase());
                } else if (plugin.getCls().equals("net.sourceforge.ondex.parser.biocyc.Parser")) {
                    Assert.assertEquals("PlantCyc parser based on BioPax representation parsed using paxtools.", plugin.getDescription());
                    Assert.assertEquals("parser", plugin.getOndexType().getName().toLowerCase());
                } else if (plugin.getCls().equals("net.sourceforge.ondex.parser.ecocyc.Parser")) {
                    Assert.assertEquals("Parser for the ecocyc flatfile database. The data flow is " +
                            "Parser ->\n AbstractReader -> AbstractParser -> AbstractTransformer", plugin.getDescription());
                    Assert.assertEquals("parser", plugin.getOndexType().getName().toLowerCase());
                } else if (plugin.getCls().equals("net.sourceforge.ondex.parser.metacyc.Parser")) {
                    Assert.assertEquals("Parser for the aracyc flatfile database. The data flow is " +
                            "Parser ->\n AbstractReader -> AbstractParser -> AbstractTransformer", plugin.getDescription());
                    Assert.assertEquals("parser", plugin.getOndexType().getName().toLowerCase());
                } else if (plugin.getCls().equals("net.sourceforge.ondex.workflow.engine.Engine")) {
                    //its the default graph...ignore
                } else if (plugin.getCls().equals("net.sourceforge.ondex.export.oxl.Export")) {
                    //oxl
                } else if (plugin.getCls().equals("net.sourceforge.ondex.parser.oxl.Parser")) {
                    //oxl
                } else {
                    Assert.fail("unknown name parsed :" + plugin.getCls());
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


    }

}
