//package net.sourceforge.ondex.producer.memorydist;
//
//import com.hazelcast.core.HazelcastInstance;
//import net.sourceforge.ondex.ONDEXPluginArguments;
//import net.sourceforge.ondex.annotations.Authors;
//import net.sourceforge.ondex.annotations.Custodians;
//import net.sourceforge.ondex.annotations.Status;
//import net.sourceforge.ondex.annotations.StatusType;
//import net.sourceforge.ondex.args.ArgumentDefinition;
//import net.sourceforge.ondex.args.StringArgumentDefinition;
//import net.sourceforge.ondex.config.Config;
//import net.sourceforge.ondex.init.ArgumentDescription;
//import net.sourceforge.ondex.init.Initialisation;
//import net.sourceforge.ondex.producer.ProducerONDEXPlugin;
//
//import java.io.File;
//import java.util.Collection;
//import java.util.Collections;
//import net.sourceforge.ondex.core.ONDEXGraph;
//import net.sourceforge.ondex.core.memorydist.DistributedMemoryONDEXGraph;
//import net.sourceforge.ondex.core.memorydist.HazelcastInstanceFactory;
//
///**
// * An experimental distributed shared memory implementation of an
// * ONDEX graph.
// *
// * @author Keith Flanagan
// */
//@Status(description = "A Hazelcast-based distributed shared memory graph implementation.", status = StatusType.EXPERIMENTAL)
//@Authors(authors = {"Keith Flanagan", "Matthew Pocock", "Jan Taubert"},
//         emails = {"k.s.flanagan at gmail.com", "drdozer at users.sourceforge.net", "jantaubert at users.sourceforge.net"})
//@Custodians(custodians = {"Keith Flanagan"}, emails = {"k.s.flanagan at gmail.com"})
//public class Producer extends ProducerONDEXPlugin
//{
//    private static HazelcastInstance hzSingleton;
//    public static final String GRAPH_NAME = "GraphName";
//
//    @Override
//    public Collection<ArgumentDescription> getArgumentDescriptions(int position)
//    {
//        ArgumentDescription ab = new ArgumentDescription();
//
//        ab.setInputId(position);
//        ab.setName("Graph id");
//        ab.setInteranlName("graphId");
//        ab.setDescription("Name of the new graph that will be created");
//        ab.setDefaultValue("default");
//        ab.setCls("java.lang.String");
//        ab.setIsRequired(true);
//        ab.setType("field");
//        ab.setIsOutputObject(true);
//        ab.setOutputId(0);
//        //ab.isSettingsArgument(true);
//
//        return Collections.singleton(ab);
//    }
//
//    @Override
//    public String getId()
//    {
//        return "distributedmemorygraph";
//    }
//
//    @Override
//    public String getName()
//    {
//        return "new distributed memory graph";
//    }
//
//    @Override
//    public String getVersion()
//    {
//        return "05/10/2012";
//    }
//
//    @Override
//    public ArgumentDefinition<?>[] getArgumentDefinitions()
//    {
//        return new ArgumentDefinition<?>[] {
//                new StringArgumentDefinition(GRAPH_NAME,
//            "The workflow name of the graph to make", false, "default", false)
//        };
//    }
//
//    @Override
//    public void start() throws Exception
//    {
//        final ONDEXPluginArguments args = getArguments();
//        if(args == null) throw new NullPointerException("Plugin arguments where null. Bailing out.");
//
//        Object gn = args.getUniqueValue(GRAPH_NAME);
//        if(gn == null) throw new NullPointerException("Required argument " + GRAPH_NAME + " missing.");
//
//        String name = gn.toString();
//
//        synchronized(this) {
//            if (hzSingleton == null) {
//                // Use the default configuration file for now.
//                hzSingleton = HazelcastInstanceFactory.createInstance();
//            }
//        }
//
//        DistributedMemoryONDEXGraph g = new DistributedMemoryONDEXGraph(hzSingleton, name);
//        File metadata = new File(Config.ondexDir + File.separator + "xml" + File.separator + "ondex_metadata.xml");
//        File xsd = new File(Config.ondexDir + File.separator + "xml" + File.separator + "ondex.xsd");
//        Initialisation init = new Initialisation(metadata, xsd);
//        init.initMetaData(g);
//        super.results = new Object[] { g };
//        super.resultTypes = new Class<?>[] {ONDEXGraph.class};
//    }
//
//    @Override
//    public boolean requiresIndexedGraph()
//    {
//        return false;
//    }
//
//    @Override
//    public String[] requiresValidators()
//    {
//        return new String[0];
//    }
//}
//
