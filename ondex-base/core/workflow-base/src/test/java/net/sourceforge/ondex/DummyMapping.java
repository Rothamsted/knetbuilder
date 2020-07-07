package net.sourceforge.ondex;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.mapping.ONDEXMapping;

/**
* Created by IntelliJ IDEA.
* User: nmrp3
* Date: 14-Jul-2010
* Time: 15:37:30
* To change this template use File | Settings | File Templates.
*/
public class DummyMapping extends ONDEXMapping
{
    @Override
    public void start() throws Exception
    {
        // do nothing
    }

    @Override
    public String getId()
    {
        return "dummyMapping";
    }

    @Override
    public String getName()
    {
        return "dummy mapping";
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
    public boolean requiresIndexedGraph()
    {
        return false;
    }

    @Override
    public String[] requiresValidators()
    {
        return new String[0];
    }
}
