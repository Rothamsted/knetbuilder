package net.sourceforge.ondex;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.init.ArgumentDescription;
import net.sourceforge.ondex.producer.ProducerONDEXPlugin;

import java.util.Collection;
import java.util.Collections;

/**
* Created by IntelliJ IDEA.
* User: nmrp3
* Date: 14-Jul-2010
* Time: 15:34:39
* To change this template use File | Settings | File Templates.
*/
public class DummyProducer extends ProducerONDEXPlugin
{
    @Override
    public String getId()
    {
        return "dummyProducer";
    }

    @Override
    public String getName()
    {
        return "dummy producer";
    }

    @Override
    public String getVersion()
    {
        return "test";
    }

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions()
    {
        return new ArgumentDefinition<?>[0];
    }

    @Override
    public void start() throws Exception
    {
        super.resultTypes = new Class[] { ONDEXGraph.class };
        super.results = new Object [] { null };
    }

    @Override
    public boolean requiresIndexedGraph()
    {
        return false;
    }

    @Override
    public String[] requiresValidators()
    {
        return new String[0];
    }


    @Override
    public Collection<ArgumentDescription> getArgumentDescriptions(int position)
    {
        ArgumentDescription ab = new ArgumentDescription();

        ab.setInputId(position);
        ab.setName("Graph id");
        ab.setInteranlName("graphId");
        ab.setDescription("Name of the new graph that will be created");
        ab.setDefaultValue("default");
        ab.setCls("java.lang.String");
        ab.setIsRequired(true);
        ab.setType("field");
        ab.setIsOutputObject(true);
        //ab.isSettingsArgument(true);

        return Collections.singleton(ab);
    }
}
