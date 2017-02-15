package net.sourceforge.ondex.workflow.engine;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import net.sourceforge.ondex.ONDEXPlugin;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.config.BerkeleyRegistry;
import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.config.LuceneRegistry;
import net.sourceforge.ondex.config.ValidatorRegistry;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.core.persistent.AbstractONDEXPersistent;
import net.sourceforge.ondex.core.persistent.BerkeleyEnv;
import net.sourceforge.ondex.core.searchable.LuceneEnv;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.ONDEXEvent;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.ONDEXListener;
import net.sourceforge.ondex.event.type.EnvironmentVariable;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import net.sourceforge.ondex.export.ONDEXExport;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.init.Initialisation;
import net.sourceforge.ondex.logging.ONDEXLogger;
import net.sourceforge.ondex.mapping.ONDEXMapping;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.tools.DirUtils;
import net.sourceforge.ondex.transformer.ONDEXTransformer;
import net.sourceforge.ondex.validator.AbstractONDEXValidator;
import net.sourceforge.ondex.workflow.events.InvalidArgumentEvent;
import net.sourceforge.ondex.workflow.model.GraphInit;
import net.sourceforge.ondex.workflow.model.PluginAndArgs;

import org.apache.log4j.Level;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

/**
 * ONDEX Work flow is the main entry point for running ONDEX work flows specified
 * in ONDEXParamiters.xml To use initialise this class and run runWorkflow()
 *
 * @author hindlem, lysenkoa
 * @see it run!
 */

public class Engine {
    private final static Pattern delim = Pattern.compile(";");
    private static List<EventType> errors = new ArrayList<EventType>();
    private static String systemDataDirectory;
    private static Integer indexCounter = 0;
    private final List<ONDEXListener> listeners = new ArrayList<ONDEXListener>();
    private ONDEXLogger logger;
    private static Engine engine;
    private AbstractONDEXPersistent penv;
    private ONDEXLogger pluginLogger;
    private Map<ONDEXGraph, String> indexedGraphs = new HashMap<ONDEXGraph, String>();
    private Set<String> indeciesToRetain = new HashSet<String>();

    static {
        systemDataDirectory = Config.ondexDir;
        if (systemDataDirectory == null) {
            System.err.println("Warning ondex.dir not specified in System properties");
        } else if (systemDataDirectory.endsWith(File.separator)) {
            systemDataDirectory = systemDataDirectory.substring(0, systemDataDirectory.length() - 1);
        }
    }

    public static Engine getEngine() {
        if (engine == null) {
            engine = new Engine();
        }
        return engine;
    }

    public static void setOndexDir(String dir) {
        if (new File(dir).exists()) {
            Config.ondexDir = dir;
        } else {
            System.err.println("Warning " + dir + " does not exist!");
        }
    }

    /**
     * A direct constructor that does not require a xml work flow file. methods in
     * this class can be used to run a work flow directly Does not run a work flow
     * as non is specified
     */
    private Engine() {
        // create and add event listener
        logger = new ONDEXLogger();
        addONDEXListener(logger);
        pluginLogger = new ONDEXLogger();
        EnvironmentVariable ev = new EnvironmentVariable("ONDEX VAR=" + Config.ondexDir);
        ev.setLog4jLevel(Level.INFO);
        fireEventOccurred(ev);
    }

    public static ONDEXGraph getNewGraph(String name, String type, String storageDir) throws Exception {
        return getEngine().getNewGraph_internal(name, type, storageDir);
    }

    private ONDEXGraph getNewGraph_internal(String type, String name, String storageDir) throws Exception {
        boolean no_metadata = false;
        System.out.println("ondex.dir = " + Config.ondexDir);
        if (Config.ondexDir != null) {

            File file = new File(Config.ondexDir);
            file.mkdirs();
            if (!file.exists() || !file.isDirectory()) {
                System.err.println("ondex.dir " + Config.ondexDir + " specified in System properties is not not valid, does not exist or is not a Dir");
                no_metadata = true;
            }
       } else {
            System.err.println("ondex.dir " + Config.ondexDir + " specified in System properties is not not valid, does not exist or is not a Dir");
            no_metadata = true;
        }
        if (name == null) name = "temp_graph";
        if (storageDir == null || storageDir.equals("")) {
            storageDir = System.getProperty("java.io.tmpdir") + File.separator + name;
        }
        try {
            DirUtils.deleteTree(storageDir);
        } catch (IOException e) {
        }
        ONDEXGraph result = null;
        if (type.equalsIgnoreCase(GraphInit.BERKELEY)) {
            penv = new BerkeleyEnv(storageDir, name, logger);
            result = penv.getAbstractONDEXGraph();
            BerkeleyRegistry.sid2berkeleyEnv.put(result.getSID(), (BerkeleyEnv) penv);
            ONDEXEventHandler.getEventHandlerForSID(result.getSID()).addONDEXONDEXListener(logger);
        } else if (type.equalsIgnoreCase(GraphInit.MEMORY)) {
            penv = null;
            result = new MemoryONDEXGraph(name);
            ONDEXEventHandler.getEventHandlerForSID(result.getSID()).addONDEXONDEXListener(logger);
       }
        try {
            if (!no_metadata) {
                File metadata = new File(Config.ondexDir + File.separator + "xml" + File.separator + "ondex_metadata.xml");
               File xsd = new File(Config.ondexDir + File.separator + "xml" + File.separator + "ondex.xsd");
                Initialisation init = new Initialisation(metadata, xsd);
                //init..addONDEXListener(logger);
                init.initMetaData(result);
            }
        }
        catch (Exception e) {
            System.err.println("WARNING!!! The location currently set as 'ondex.dir' (" + Config.ondexDir + ") does not contain default ondex metadata or metadta file is corrupt.\nPlugins that need default metadata or validation may fail as a result.");
            System.err.println(e.getMessage());
        }
        return result;

    }

    /**
     * Runs an export plug-in on the specified graph
     *
     * @param exporter     the export to run
     * @param exportArgs   the args to run with
     * @param inputGraph   the graph to export
     * @throws Exception   if the export fails
     */
    public void runExport(ONDEXExport exporter, ONDEXPluginArguments exportArgs, ONDEXGraph inputGraph) throws Exception {
        if(inputGraph == null) throw new NullPointerException("Can not run plugin with a null inputGraph");
        if (exportArgs == null)
            exportArgs = new ONDEXPluginArguments(exporter.getArgumentDefinitions());

        String name = exporter.getName();

        exporter.setArguments(exportArgs);
        exporter.addONDEXListener(pluginLogger);

        if (exporter.requiresValidators() != null && exporter.requiresValidators().length > 0) {
            initializeValidators(exporter.requiresValidators(), inputGraph);
        }
        
        long start = System.currentTimeMillis();

        exporter.setONDEXGraph(inputGraph);

        exporter.start();

        fireEventOccurred(new GeneralOutputEvent("Exporting with " + name + " took " + ((System.currentTimeMillis() - start) / 1000) + " seconds", getCurrentMethodName()));
        if (penv != null) penv.commit();
    }

    /**
     * Runs a Filter producer on the input graph and applies results and there
     * tag dependencies to the output graph
     *
     * @param filter  the filter to run
     * @param args  the arguments to run with
     * @param graphInput  the graph to apply the filter on
     * @param graphOutput the graph to write results to (cloned from input graph)
     * @throws Exception  if the filter fails
     */
    public ONDEXGraph runFilter(ONDEXFilter filter, ONDEXPluginArguments args,
                                ONDEXGraph graphInput, ONDEXGraph graphOutput) throws Exception {
        if(graphInput == null) throw new NullPointerException("Can not run plugin with a null graphInput");
        if (args == null) args = new ONDEXPluginArguments(filter.getArgumentDefinitions());

        LuceneEnv lenv = null;
        if (filter.requiresIndexedGraph()) {
            lenv = getIndex(graphInput, filter.getName());
//			filterArgs.setIndexedEnv(lenv);
        }

        String name = filter.getName();
        filter.setArguments(args);

        long start = System.currentTimeMillis();
        filter.addONDEXListener(pluginLogger);
        filter.setONDEXGraph(graphInput);
        filter.start();

        fireEventOccurred(new GeneralOutputEvent("Filter returned " + filter.getVisibleConcepts().size() + " visible Concepts and " + filter.getVisibleRelations().size() + " visible relations", getCurrentMethodName()));

        Set<ONDEXRelation> relationsVisible = filter.getVisibleRelations();
        if (relationsVisible == null)
            throw new RuntimeException("filter.getVisibleRelations() returns null after start");

        Set<ONDEXConcept> conceptsVisible = filter.getVisibleConcepts();
        if (conceptsVisible == null)
            throw new RuntimeException("filter.getVisibleConcepts() returns null after start");


        if (graphOutput != null && !graphInput.equals(graphOutput)) {
            fireEventOccurred(new GeneralOutputEvent(filter.getName() + " filter complete cloning returned concept from " + graphInput.getName() + " to " + graphOutput.getName(), getCurrentMethodName()));
            filter.copyResultsToNewGraph(graphOutput);
        } else { // delete all not found concepts in the graph
            fireEventOccurred(new GeneralOutputEvent(filter.getName() + " filter complete removing non matching concepts from original graph as GraphInput is the same as GraphOutput", getCurrentMethodName()));
            fireEventOccurred(new GeneralOutputEvent("Identifying Tag dependencies on Relations ", getCurrentMethodName()));

            Set<ONDEXRelation> relationsToDelete = BitSetFunctions.copy(graphInput.getRelations());
            relationsToDelete.removeAll(relationsVisible);

            int relationTagsRemoved = 0;

            Iterator<ONDEXRelation> relIt = relationsVisible.iterator();
            while (relIt.hasNext()) {
                ONDEXRelation relation = relIt.next();
                for (ONDEXConcept tag : relation.getTags().toArray(new ONDEXConcept[0])) {
                    if (!conceptsVisible.contains(tag)) {
                        relation.removeTag(tag);
                        relationTagsRemoved++;
                    }
                }
            }
            
            fireEventOccurred(new GeneralOutputEvent("Removing " + relationsToDelete.size() + " relations", getCurrentMethodName()));

            for (ONDEXRelation relation : relationsToDelete) {
                graphInput.deleteRelation(relation.getId());
            }

            Set<ONDEXConcept> invisibleConcepts = BitSetFunctions.copy(graphInput.getConcepts());
            invisibleConcepts.removeAll(conceptsVisible);

            int conceptsTagsRemoved = 0;
            for (ONDEXConcept concept : conceptsVisible) {
                for (ONDEXConcept tag : concept.getTags().toArray(new ONDEXConcept[0])) {
                    if (!conceptsVisible.contains(tag)) {
                        concept.removeTag(tag);
                        conceptsTagsRemoved++;
                    }
                }
            }

            fireEventOccurred(new GeneralOutputEvent("Removed Tag on Concepts " + conceptsTagsRemoved + " and on Relations " + relationTagsRemoved, getCurrentMethodName()));

            fireEventOccurred(new GeneralOutputEvent("Removing " + invisibleConcepts.size() + " Concepts", getCurrentMethodName()));

            for (ONDEXConcept concept : invisibleConcepts) {
//				explicitly delete relations on deleted concepts (for safety)
                for (ONDEXRelation relation : graphInput.getRelationsOfConcept(concept).toArray(new ONDEXRelation[0])) {
                    graphInput.deleteRelation(relation.getId());
                }
                graphInput.deleteConcept(concept.getId());
            }
        }

        fireEventOccurred(new GeneralOutputEvent("Filter with " + name
                + " took " + ((System.currentTimeMillis() - start) / 1000) + " seconds",
                getCurrentMethodName()));

        removeIndex(graphInput, lenv);
        if (penv != null)
            penv.commit();
        return graphOutput;
    }

    /**
     * Runs a Transformer producer on the specified graph
     *
     * @param transformer      the Transformer to run
     * @param args  arguments for the transformer
     * @param graphInput the graph to use as input (and by implication output)
     * @throws Exception
     */
    public ONDEXGraph runTransformer(ONDEXTransformer transformer, ONDEXPluginArguments args, ONDEXGraph graphInput) throws Exception {
        if(graphInput == null) throw new NullPointerException("Can not run plugin with a null graphInput");
        if (args == null) args = new ONDEXPluginArguments(transformer.getArgumentDefinitions());

        LuceneEnv lenv = null;
        if (transformer.requiresIndexedGraph()) {
            lenv = getIndex(graphInput, transformer.getName());
//			interA.setIndexedEnv(lenv);
        }

//		interA.setPersistentEnv(penv);

        String name = transformer.getName();

        transformer.addONDEXListener(pluginLogger);
        transformer.setArguments(args);

        long start = System.currentTimeMillis();
        transformer.setONDEXGraph(graphInput);
        transformer.start();

        fireEventOccurred(new GeneralOutputEvent(name + " took " + ((System.currentTimeMillis() - start) / 1000) + " seconds", getCurrentMethodName()));
        removeIndex(graphInput, lenv);
        if (penv != null)
            penv.commit();
        return graphInput;
    }

    /**
     * Runs a Mapping producer
     *
     * @param mapping    the mapping to run
     * @param args  arguments for the mapping
     * @param graphInput the graph to use as input (and by implication output)
     */
    public ONDEXGraph runMapping(ONDEXMapping mapping, ONDEXPluginArguments args, ONDEXGraph graphInput) throws Exception {
        if(graphInput == null) throw new NullPointerException("Can not run plugin with a null graphInput");
        Set<ONDEXRelation> rit = graphInput.getRelations();
        if (rit == null)
            return graphInput;
        long relationsPre = rit.size();

        if (args == null) {
            args = new ONDEXPluginArguments(mapping.getArgumentDefinitions());
        }


        LuceneEnv lenv = null;
        if (mapping.requiresIndexedGraph()) {
            lenv = getIndex(graphInput, mapping.getName());
        }

        String name = mapping.getName();

        mapping.addONDEXListener(pluginLogger);
        mapping.setArguments(args);

        long start = System.currentTimeMillis();
        mapping.setONDEXGraph(graphInput);
        mapping.start();

        fireEventOccurred(new GeneralOutputEvent(name + " took " + +((System.currentTimeMillis() - start) / 1000) + " seconds", getCurrentMethodName()));

        rit = graphInput.getRelations();
        long relationsPost = rit.size() - relationsPre;

        fireEventOccurred(new GeneralOutputEvent("New Relations: " + relationsPost, getCurrentMethodName()));
        rit = null;
        removeIndex(graphInput, lenv);
        if (penv != null)
            penv.commit();

        return graphInput;
    }

    /**
     * Runs a parser producer
     *
     * @param parser the parser to run
     * @param args  arguments for the parser
     * @param graphInput the graph to use as input (and by implication output)
     * @throws Exception
     */
    public ONDEXGraph runParser(ONDEXParser parser, ONDEXPluginArguments args,  ONDEXGraph graphInput) throws Exception {
        if(graphInput == null) throw new NullPointerException("Can not run plugin with a null graphInput");
        if (args == null)
            args = new ONDEXPluginArguments(parser.getArgumentDefinitions());

        LuceneEnv lenv = null;
        if (parser.requiresIndexedGraph()) {
            lenv = getIndex(graphInput, parser.getName());
//			pargs.setIndexedEnv(lenv);
        }
//		pargs.setPersistentEnv(penv);
        parser.addONDEXListener(pluginLogger);
        parser.setArguments(args);

        if (parser.requiresValidators() != null && parser.requiresValidators().length > 0) {
            initializeValidators(parser.requiresValidators(), graphInput);
        }

        long start = System.currentTimeMillis();
        parser.setONDEXGraph(graphInput);
        parser.start();

        fireEventOccurred(new GeneralOutputEvent(parser.getName() + " took " + +((System.currentTimeMillis() - start) / 1000) + " seconds", getCurrentMethodName()));
        removeIndex(graphInput, lenv);
        if (penv != null) {
            penv.commit();
        }
        return graphInput;
    }


    public LuceneEnv getIndex(ONDEXGraph graph, String name) {//pluginInit.makeSimplePlugin().getName()
        if (penv != null) penv.commit();
        long start = System.currentTimeMillis();
        fireEventOccurred(new GeneralOutputEvent("Index required by " + name + " starting index", getCurrentMethodName()));
        String dir = indexedGraphs.get(graph);
        if (dir != null && new File(dir).exists()) {
            LuceneEnv lenv = new LuceneEnv(dir, false);
            lenv.addONDEXListener(logger);
            lenv.setONDEXGraph(graph);
            return lenv;
        }
        String graphdir = null;
        if (Config.ondexDir.endsWith(File.separator)) {
            graphdir = Config.ondexDir + "index" + File.separator + graph.getName() + File.separator + "index";

        } else {
            graphdir = Config.ondexDir + File.separator + "index" + File.separator + graph.getName() + File.separator + "index";

        }
        dir = graphdir + indexCounter;
        while (new File(dir).exists()) {
            dir = graphdir + indexCounter;
            indexCounter++;
        }
        LuceneEnv lenv = new LuceneEnv(dir, true);
        lenv.addONDEXListener(logger);
        lenv.setONDEXGraph(graph);
        LuceneRegistry.sid2luceneEnv.put(graph.getSID(), lenv);
        indexedGraphs.put(graph, dir);
        fireEventOccurred(new GeneralOutputEvent("Lucene took " + (System.currentTimeMillis() - start) + " msec.", getCurrentMethodName()));
        if (penv != null) penv.commit();
        return lenv;
    }

    public void removeIndex(ONDEXGraph graph, LuceneEnv lenv) {
        if (lenv != null)
            lenv.cleanup();
        String dir = indexedGraphs.get(graph);
        indexedGraphs.remove(graph);
        if (dir == null || indeciesToRetain.contains(dir)) return;
        try {
            DirUtils.deleteTree(dir);
        } catch (Exception e) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
            }
            try {
                DirUtils.deleteTree(dir);
            } catch (IOException e1) {
                System.err.println("Warning: could not delete " + dir + " - directory is locked by Windows.");
            }
        }
    }

    /**
     * Saves lucene index
     *
     * @param graph  - graph
     * @param newDir - directory to save index to
     */
    public static void saveIndex(ONDEXGraph graph, String newDir) {
        if (newDir != null) {
            LuceneEnv lenv = new LuceneEnv(newDir, true);
            lenv.addONDEXListener(engine.logger);
            lenv.setONDEXGraph(graph);
            engine.indexedGraphs.put(graph, newDir);
            engine.indeciesToRetain.add(newDir);
        }
    }


    /**
     * Loads lucene index
     *
     * @param graph - graph
     * @param dir   - directory with index
     */
    public static void loadIndex(ONDEXGraph graph, String dir) {
        engine.indeciesToRetain.add(dir);
        engine.indexedGraphs.put(graph, dir);
    }

    /**
     * Cleanup default graph if persistant and closes all existing graphs
     */
    public void cleanUp() {
        if (penv != null) {
            penv.cleanup();
        }
        List<ONDEXGraph> indexed = new LinkedList<ONDEXGraph>();
        Set<String> graphFolders = new HashSet<String>();
        for (Entry<ONDEXGraph, String> ent : indexedGraphs.entrySet()) {
            if (!indeciesToRetain.contains(ent.getValue())) graphFolders.add(new File(ent.getValue()).getParent());
            indexed.add(ent.getKey());
        }
        for (ONDEXGraph graph : indexed)
            removeIndex(graph, null);
        for (String graphDir : graphFolders) {
            try {
                DirUtils.deleteTree(graphDir);
            } catch (IOException e) {
            }
        }
    }

    private ONDEXLogger validatorLogger;

    /**
     * Initalize all validators in name list that have not already been
     * initialized
     *
     * @param validatorNames the names of validators which should be their package names
     *                       within net.sourceforge.ondex.validator.
     * @throws Exception *
     */
    public void initializeValidators(String[] validatorNames, ONDEXGraph graph) throws Exception {

        if (validatorLogger == null) {
            validatorLogger = new ONDEXLogger();
        }

        for (String validator : validatorNames) {
            String className = "net.sourceforge.ondex.validator."
                    + validator.toLowerCase() + ".Validator";

            if (ValidatorRegistry.validators.keySet().contains(className)) {
                continue; // already initialized
            }
            try {
                Class<?> validatorClass = Thread.currentThread().getContextClassLoader().loadClass(className);

                Class<?>[] args = new Class<?>[]{};
                Constructor<?> constructor = validatorClass.getClassLoader()
                        .loadClass(className).getConstructor(args);
                AbstractONDEXValidator validatorInstance = (AbstractONDEXValidator) constructor
                        .newInstance();
                ValidatorRegistry.validators.put(validator.toLowerCase(), validatorInstance);

                File vout = new File(Config.ondexDir
                        + File.separator + "dbs"
                        + File.separator + graph.getName()
                        + File.separator + "validatorsout"
                        + File.separator + validatorInstance.getName());
                vout.mkdirs();
                vout.deleteOnExit();

                ONDEXPluginArguments va = new ONDEXPluginArguments(validatorInstance.getArgumentDefinitions());


                if (va.hasArgument(FileArgumentDefinition.INPUT_DIR)) {
                    File dir = new File(Config.ondexDir
                            + File.separator + "importdata"
                            + File.separator + validator.toLowerCase());
                    dir.mkdirs();
                    va.addOption(FileArgumentDefinition.INPUT_DIR, dir.getAbsolutePath());
                }

                if (va.hasArgument(FileArgumentDefinition.EXPORT_DIR))
                    va.addOption(FileArgumentDefinition.EXPORT_DIR, vout.getAbsolutePath());

                if (validatorInstance.requiresIndexedGraph()) {
                    getIndex(graph, validatorInstance.getName());
                }

                validatorInstance.addONDEXListener(validatorLogger);
                validatorInstance.setArguments(va);
                validatorInstance.start();

            } catch (ClassNotFoundException e) {
                throw new Exception("Could not resolve validator " + className + ". Make sure the required jar is deployed to the libs directory and restart the program to correct this error.");
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Prints to System.out the current status of concepts and relation ect. in
     * the graph
     *
     * @param graph the graph to present statistics on
     */
    public void outputCurrentGraphStatistics(ONDEXGraph graph) {

        fireEventOccurred(new GeneralOutputEvent("\nGraph Statistics for "
                + graph.getName(), getCurrentMethodName()));

        Set<ONDEXConcept> cit = graph.getConcepts();
        fireEventOccurred(new GeneralOutputEvent("\nConcepts: " + cit.size(),
                getCurrentMethodName()));
        cit = null;

        Set<ONDEXRelation> rit = graph.getRelations();
        if (rit == null) {
            fireEventOccurred(new GeneralOutputEvent("\nRelations: 0",
                    getCurrentMethodName()));
        } else {
            fireEventOccurred(new GeneralOutputEvent("\nRelations: " + rit.size(),
                    getCurrentMethodName()));
        }

        rit = null;

        Set<DataSource> cvit = graph.getMetaData().getDataSources();
        fireEventOccurred(new GeneralOutputEvent("\nDataSources: " + cvit.size(), getCurrentMethodName()));
        cvit = null;

        Set<ConceptClass> ccit = graph.getMetaData()
                .getConceptClasses();
        fireEventOccurred(new GeneralOutputEvent("\nConceptClasses: " + ccit.size(), getCurrentMethodName()));
        ccit = null;

        Set<RelationType> rtit = graph.getMetaData()
                .getRelationTypes();
        fireEventOccurred(new GeneralOutputEvent("\nRelationTypes: " + rtit.size(), getCurrentMethodName()));
        rtit = null;
    }

    /**
     * Adds a ONDEX listener to the list.
     *
     * @param l -
     *          ONDEXListener
     */
    public void addONDEXListener(ONDEXListener l) {
        listeners.add(l);
    }

    /**
     * Removes a ONDEX listener listener from the list.
     *
     * @param l -
     *          ONDEXListener
     */
    public void removeONDEXListener(ONDEXListener l) {
        listeners.remove(l);
    }

    /**
     * Returns the list of ONDEX listener listeners.
     *
     * @return list of ONDEXListeners
     */
    public ONDEXListener[] getONDEXListeners() {
        return listeners.toArray(new ONDEXListener[listeners.size()]);
    }

    /**
     * Notify all listeners that have registered with this class.
     *
     * @param e -
     *          name of event
     */
    protected void fireEventOccurred(EventType e) {
        if (listeners.size() > 0) {
            // new ondex graph event
            ONDEXEvent oe = new ONDEXEvent(this, e);
            // notify all listeners
            for (ONDEXListener listener : listeners)
                listener.eventOccurred(oe);
        }
    }

    /**
     * Convenience method for outputing the current method name in a dynamic way
     *
     * @return the calling method name
     */
    public static String getCurrentMethodName() {
        StackTraceElement trace = new Exception().fillInStackTrace().getStackTrace()[1];
        String name = trace.getMethodName();
        String className = trace.getClassName();
        int line = trace.getLineNumber();
        return "[CLASS:" + className + " - METHOD:" + name + " LINE:" + line + "]";
    }

    @SuppressWarnings("unchecked")
    public static PluginAndArgs process(List<ValuePair<String, String>> toParse, String cls, String pluginCls)
            throws Exception
    {
        PluginAndArgs currentPluginAndArgs = new PluginAndArgs();

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            currentPluginAndArgs.setPlugin((ONDEXPlugin) cl.loadClass(pluginCls).getConstructor().newInstance());
            System.err.println("Class to load - "+cls);
            Class<?> argCls = cl.loadClass(cls);
            Constructor<?> constructor = argCls.getConstructor(ArgumentDefinition[].class);
            ArgumentDefinition<?>[] ad = currentPluginAndArgs.getPlugin().getArgumentDefinitions();
            if(ad == null){
            	throw new Exception(currentPluginAndArgs.getPlugin().getClass().getCanonicalName()+" - breaks the copntract on OndexPlugin! ArgumentDefinitions must not be null.");
            }
            currentPluginAndArgs.setArguments((ONDEXPluginArguments) constructor.newInstance(new Object[]{ad}));
        }
        catch (InstantiationException e) {
            throw new PluginConfigurationException(e);
        }
        catch (IllegalAccessException e) {
            throw new PluginConfigurationException(e);
        }
        catch (InvocationTargetException e) {
            throw new PluginConfigurationException(e);
        }
        catch (NoSuchMethodException e) {
            throw new PluginConfigurationException(e);
        }
        catch (ClassNotFoundException e) {
            throw new PluginConfigurationException(e);
        }

        for (ValuePair<String, String> ent : toParse) {

            String pname = ent.getKey();
            String text = ent.getValue();

            Map<String, List<?>> options = currentPluginAndArgs.getArguments().getOptions();
            List list = options.get(pname);
            if (list == null) {
                list = new ArrayList<Object>(1);
                options.put(pname, list);
            }
            String[] values = delim.split(text);
            for (String value : values) {
                list.add(castToPluginArgNativeObject(value, currentPluginAndArgs, pname));
            }
        }

        return currentPluginAndArgs;
    }

    /**
     * instanciates a producer paramiter from its name-value pair
     *
     * @param chars      the object as specified in the param
     * @param pluginAndArgs current producer
     * @param paramName  the name for this paramiter
     * @return the value in its native Java object form
     */
    private static Object castToPluginArgNativeObject(String chars, PluginAndArgs pluginAndArgs, String paramName) {

        ONDEXPlugin plugin = pluginAndArgs.getPlugin();

        if (plugin != null) {
            for (ArgumentDefinition<?> definition : plugin.getArgumentDefinitions()) {
                if (definition.getName().equalsIgnoreCase(paramName)) {
                    Object obj;
                    try {
                        obj = definition.parseString(chars);
                    } catch (Exception e) {
                        errors.add(new InvalidArgumentEvent("The " + pluginAndArgs + " Parameter " + paramName + " is invalid for value " + chars + " \n error:" + e.getMessage()));
                        return definition.getDefaultValue();
                    }
                    if (obj == null) {
                        errors.add(new InvalidArgumentEvent("The " + pluginAndArgs + " Parameter " + paramName + " does not support instansiation from String"));
                        return definition.getDefaultValue();
                    }
                    return obj;
                }
            }
            errors.add(new InvalidArgumentEvent("The " + plugin.getClass() + " Parameter " + paramName + " is not a argument"));
        }
        return null;
    }

    public static String getVersion(String file) throws XMLStreamException, IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        XMLInputFactory2 ifact = (XMLInputFactory2) XMLInputFactory.newInstance();
        ifact.configureForXmlConformance();
        XMLStreamReader2 staxXmlReader = (XMLStreamReader2) ifact.createXMLStreamReader(bis);

        String currentElement;

        while (staxXmlReader.hasNext()) {
            int event = staxXmlReader.next();

            switch (event) {

                case XMLStreamConstants.START_ELEMENT:
                    currentElement = staxXmlReader.getLocalName();
                    if (currentElement.equalsIgnoreCase("ONDEX")) {
                        for (int i = 0; i < staxXmlReader.getAttributeCount(); i++) {
                            if (staxXmlReader.getAttributeName(i).getLocalPart().equalsIgnoreCase("version")) {
                                String result = staxXmlReader.getAttributeValue(i);
                                bis.close();
                                return result;

                            }
                        }
                    }
            }
        }
        return "1.0";
    }

    /**
     * Copies diretory to another direcotry
     *
     * @param srcDir
     * @param dstDir
     * @throws java.io.IOException
     */
    public static void copyDirectory(File srcDir, File dstDir) throws IOException {
        if (srcDir.isDirectory()) {
            if (!dstDir.exists())
                dstDir.mkdir();
            for (String child : srcDir.list())
                copyDirectory(new File(srcDir, child), new File(dstDir, child));
        } else {
            copyFile(srcDir, dstDir);
        }
    }

    /**
     * Copies file to another file
     *
     * @param src source file
     * @param dst - destination file
     * @throws java.io.IOException
     */
    public static void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0)
            out.write(buf, 0, len);
        in.close();
        out.close();
    }

    /**
     * Converts a relative path to fully qualified path by prepending ondex dir
     *
     * @param fileOrDir - path to check
     * @return - absolute path to file or folder
     */
//    private static String verifyPath(String fileOrDir) {
//        if (!new File(fileOrDir).exists()) {
//            if (fileOrDir.startsWith(File.separator) || fileOrDir.startsWith("\\") || fileOrDir.startsWith("/")) {
//                fileOrDir = systemDataDirectory + fileOrDir;
//            } else {
//                fileOrDir = systemDataDirectory + File.separator + fileOrDir;
//            }
//        }
//        return fileOrDir;
//    }
}