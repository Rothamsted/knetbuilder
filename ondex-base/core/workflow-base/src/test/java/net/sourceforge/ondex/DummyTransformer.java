package net.sourceforge.ondex;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

/**
* Created by IntelliJ IDEA.
* User: nmrp3
* Date: 14-Jul-2010
* Time: 15:40:16
* To change this template use File | Settings | File Templates.
*/
public class DummyTransformer extends ONDEXTransformer
{
    @Override
    public String getId()
    {
        return "dummyTransformer";
    }

    @Override
    public String getName()
    {
        return "dummy transformer";
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
        //T do nothing
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
