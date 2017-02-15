package net.sourceforge.ondex.init;

import net.sourceforge.ondex.*;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.workflow.engine.PluginProcessor;
import net.sourceforge.ondex.workflow.engine.Processor;
import net.sourceforge.ondex.workflow.engine.ResourcePool;
import net.sourceforge.ondex.workflow.model.PluginAndArgs;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;

/**
 * Test the functionality of PluginProcessor.
 *
 * @author Matthew Pocock
 */
@RunWith(JMock.class)
public class PluginProcessorTest
{
    Mockery context = new JUnit4Mockery();
    
    private void pluginTest(Class<? extends ONDEXPlugin> pluginClass, int outputCount) throws Exception
    {
        UUID uuid = UUID.randomUUID();
        ONDEXPlugin plugin = pluginClass.newInstance();
        PluginProcessor pp = new PluginProcessor(pluginClass, null);
        pp.configure(
                new Processor.Argument[] {
                        new Processor.Argument(
                                new PluginAndArgs(plugin, new ONDEXPluginArguments(plugin.getArgumentDefinitions())),
                                uuid) },
                new UUID[outputCount]);
        
        ResourcePool rp = new ResourcePool();
        rp.putResource(uuid, dummyGraph(), false);

        UUID[] result = pp.execute(rp);
        assertEquals("Unexpected number of results produced.", outputCount, result.length);
    }

    @Test
    public void testProducer() throws Exception
    {
        pluginTest(DummyProducer.class, 1);
    }

    @Test
    public void testExport() throws Exception
    {
        pluginTest(DummyExport.class, 0);
    }

    @Test
    public void testFilter() throws Exception
    {
        pluginTest(DummyFilter.class, 1);
    }

    @Test
    public void testMapping() throws Exception
    {
        pluginTest(DummyMapping.class, 0);
    }

    @Test public void testParser() throws Exception
    {
        pluginTest(DummyParser.class, 0);
    }
   
    @Test
    public void testTransformer() throws Exception
    {
        pluginTest(DummyTransformer.class, 0);
    }

    private ONDEXGraph dummyGraph()
    {
        final ONDEXGraph graph = context.mock(ONDEXGraph.class);
        context.checking(new Expectations() {{
            allowing(graph).getConcepts(); will(returnValue(Collections.emptySet()));
            allowing(graph).getRelations(); will(returnValue(Collections.emptySet()));
        }});

        return graph;
    }

}
