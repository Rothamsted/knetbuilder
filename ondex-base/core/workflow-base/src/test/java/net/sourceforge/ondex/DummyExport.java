package net.sourceforge.ondex;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.export.ONDEXExport;

/**
* Created by IntelliJ IDEA.
* User: nmrp3
* Date: 14-Jul-2010
* Time: 15:35:42
* To change this template use File | Settings | File Templates.
*/
public class DummyExport extends ONDEXExport
{
    @Override
    public String getId()
    {
        return "dummyExport";
    }

    @Override
    public String getName()
    {
        return "dummy export";
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
