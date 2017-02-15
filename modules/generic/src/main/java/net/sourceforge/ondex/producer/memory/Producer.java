package net.sourceforge.ondex.producer.memory;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.init.ArgumentDescription;
import net.sourceforge.ondex.init.Initialisation;
import net.sourceforge.ondex.producer.ProducerONDEXPlugin;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: nmrp3
 * Date: 08-Jul-2010
 * Time: 15:47:52
 * To change this template use File | Settings | File Templates.
 */
@Status(description = "Tested July 2010 (Jan Taubert)", status = StatusType.STABLE)
@Authors(authors = {"Matthew Pocock", "Jan Taubert"}, emails = {"drdozer at users.sourceforge.net", "jantaubert at users.sourceforge.net"})
@Custodians(custodians = {"Jan Taubert"}, emails = {"jantaubert at users.sourceforge.net"})
public class Producer extends ProducerONDEXPlugin
{
    public static final String GRAPH_NAME = "GraphName";

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
        ab.setOutputId(0);
        //ab.isSettingsArgument(true);
        
        return Collections.singleton(ab);
    }

    @Override
    public String getId()
    {
        return "memorygraph";
    }

    @Override
    public String getName()
    {
        return "new memory graph";
    }

    @Override
    public String getVersion()
    {
        return "08/07/2010";
    }

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions()
    {
        return new ArgumentDefinition<?>[] {
                new StringArgumentDefinition(GRAPH_NAME, "The workflow name of the graph to make", false, "default", false)
        };
    }

    @Override
    public void start() throws Exception
    {
        final ONDEXPluginArguments args = getArguments();
        if(args == null) throw new NullPointerException("Plugin arguments where null. Bailing out.");

        Object gn = args.getUniqueValue(GRAPH_NAME);
        if(gn == null) throw new NullPointerException("Required argument " + GRAPH_NAME + " missing.");

        String name = gn.toString();

        MemoryONDEXGraph g = new MemoryONDEXGraph(name);
        File metadata = new File(Config.ondexDir + File.separator + "xml" + File.separator + "ondex_metadata.xml");
        File xsd = new File(Config.ondexDir + File.separator + "xml" + File.separator + "ondex.xsd");
        Initialisation init = new Initialisation(metadata, xsd);
        init.initMetaData(g);
        super.results = new Object[] { g };
        super.resultTypes = new Class<?>[] {ONDEXGraph.class};
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
