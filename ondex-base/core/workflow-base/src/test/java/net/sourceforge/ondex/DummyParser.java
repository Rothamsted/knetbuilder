package net.sourceforge.ondex;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.parser.ONDEXParser;

/**
* Created by IntelliJ IDEA.
* User: nmrp3
* Date: 14-Jul-2010
* Time: 15:38:37
* To change this template use File | Settings | File Templates.
*/
public class DummyParser extends ONDEXParser
{
    @Override
    public String getId()
    {
        return "dummyParser";
    }

    @Override
    public String getName()
    {
        return "dummy parser";
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
        // do nothing
    }

    @Override
    public String[] requiresValidators()
    {
        return new String[0];
    }
}
