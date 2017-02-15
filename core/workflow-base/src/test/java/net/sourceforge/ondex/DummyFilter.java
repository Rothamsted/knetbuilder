package net.sourceforge.ondex;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.filter.ONDEXFilter;

import java.util.Collections;
import java.util.Set;

/**
* Created by IntelliJ IDEA.
* User: nmrp3
* Date: 14-Jul-2010
* Time: 15:36:30
* To change this template use File | Settings | File Templates.
*/
public class DummyFilter extends ONDEXFilter
{
    @Override
    public void copyResultsToNewGraph(ONDEXGraph exportGraph)
    {
        // ignore
    }

    @Override
    public Set<ONDEXConcept> getVisibleConcepts()
    {
        return Collections.emptySet();
    }

    @Override
    public Set<ONDEXRelation> getVisibleRelations()
    {
        return Collections.emptySet();
    }

    @Override
    public String getId()
    {
        return "dummyFilter";
    }

    @Override
    public String getName()
    {
        return "dummy filter";
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
    {}

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
