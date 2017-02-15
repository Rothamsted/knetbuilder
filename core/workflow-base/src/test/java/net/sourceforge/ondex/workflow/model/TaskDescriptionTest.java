package net.sourceforge.ondex.workflow.model;

import static org.junit.Assert.*;

import java.util.Collections;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.init.PluginType;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by IntelliJ IDEA.
 * User: nmrp3
 * Date: 14-Jul-2010
 * Time: 15:20:29
 * To change this template use File | Settings | File Templates.
 */
@RunWith(JMock.class)
public class TaskDescriptionTest
{
    Mockery context = new JUnit4Mockery();

    @Test
    public void testExport() throws Exception, PluginType.UnknownPluginTypeException
    {
      /*  ONDEXPlugin plugin = new DummyExport();
        int outputCount = 0;
        UUID uuid = UUID.randomUUID();

        PluginBean pb = new PluginBean();
        pb.setCls(plugin.getClass().getCanonicalName());
        pb.setName(plugin.getName());
        pb.setGUIType("plugin");
        pb.setArgDef(plugin.getArgumentBeans(0).toArray(new ArgumentBean[0]));

        PluginConfiguration pc = new PluginConfiguration(pb);
        pc.addArgument("graphId", "testGraph");
        
        TaskDescription td = new TaskDescription();
        Processor p = td.configure(pc);
        assertNotNull("Processor produced by TaskDescriptor must not be null.", p);
        
        ResourcePool rp = new ResourcePool();
        rp.putResource(uuid, dummyGraph(), true);

        UUID[] result = p.execute(rp);

        assertEquals("Unexpected number of results produced.", outputCount, result.length);
*/    
    //TODO: re-enable test once Artem is back
    	assertTrue(true);
    }

    private ONDEXGraph dummyGraph()
    {
        final ONDEXGraph graph = context.mock(ONDEXGraph.class);
        context.checking(new Expectations() {{
            allowing(graph).getConcepts(); will(returnValue(Collections.emptySet()));
            allowing(graph).getRelations(); will(returnValue(Collections.emptySet()));
            allowing(graph).getName(); will(returnValue("testGraph"));
        }});

        return graph;
    }
}
