package net.sourceforge.ondex.scripting.sparql;

import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.ArrayKey;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.core.util.QuerySetParser;
import net.sourceforge.ondex.scripting.CommandInterpreter;
import net.sourceforge.ondex.scripting.OutputPrinter;
import net.sourceforge.ondex.scripting.ProcessingCheckpoint;
import net.sourceforge.ondex.scripting.ProxyTemplate;
import net.sourceforge.ondex.scripting.wrappers.OndexScriptingInitialiser;

import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import org.apache.log4j.Logger;

/**
 * @author lysenkoa SPARQL parser. Maps the SPARQL commands to appropriate java
 * functions in ONDEX/OVTK.
 *
 * TODO: Clarify: is this meant as a single instance object, or multiple console
 * can be present at the same time ?
 */
public class SPARQLInterpreter implements CommandInterpreter {
    /*
     * Object fields: the following are related to the object creation. They don't change after construction.
     */

    private static SPARQLInterpreter currentInstance;
    private GraphManager gm;
    private final List<CommandHandler> handlers;
    private OutputPrinter out; // TODO Clarify: can this change during execution ? - Yes, it can - if needed
    private boolean isReady = false;

    private QuerySetParser querySet = new QuerySetParser();

    private static final Logger log = Logger.getLogger(SPARQLInterpreter.class);

    private SPARQLInterpreter() {
        this.gm = new GraphManager();
        handlers = Arrays.asList(new ConnectHandler(), new PrefixHandler(), new UseHandler(), new SparqlHandler());
        prefixes = new ArrayList<String>();
        currentInstance = this;

    }

    /**
     * Attempts to load the default configuration, a minimum set of commands
     * required for correct functioning of the interpreter.
     *
     * @return success/failure as a boolean
     */
    public boolean configure() {
        if (!isReady) {
            boolean failed = false;
            try {
                querySet.load("default");
                if (querySet.getList("prefixes") == null) {
                    failed = true;
                }
                if (querySet.getQuery("reified_type") == null) {
                    failed = true;
                }
                if (querySet.getQuery("types_query") == null) {
                    failed = true;
                }
                if (querySet.getQuery("reified_attribute_query") == null) {
                    failed = true;
                }
                if (querySet.getQuery("reified_query") == null) {
                    failed = true;
                }
                if (querySet.getQuery("concept_attribute_querry") == null) {
                    failed = true;
                }
            } catch (Exception e) {
                failed = true;
                System.out.println("SPAQL interpreter not initialised (" + e.getMessage() + ")");
            }
            if (!failed) {
                isReady = true;
                resetCache();
            }
        }
        return isReady;
    }

    public static SPARQLInterpreter getCurrentInstance() {
        if (currentInstance == null) {
            currentInstance = new SPARQLInterpreter();
        }
        return currentInstance;
    }

    public QuerySetParser getQuerySetParser() {
        return querySet;
    }

    /////////////////////
    /*
     * Execution related variables, change as a consequence of command issues
     */
    private String myEndPoint;
    private ProcessingCheckpoint pc; //TODO Clarify: what is it ?

    // Cache. Some cache is in gm (Ondex relations and Concepts, indexed on Nodes).
    private HashSet<String> alreadyAskedNodeAttributes;
    private HashSet<String> alreadyAskedRelationAttributes;

    private void resetCache() {
        // TODO resets memory of what was already asked.
        // Perhaps it make sense to keep memory of edges and nodes, but we need to forget attributes if we change endpoint.
        alreadyAskedNodeAttributes = new HashSet<String>();
        alreadyAskedRelationAttributes = new HashSet<String>();
    }

    // Cache-like. They may change after some reset operations.
    private final List<String> prefixes;
    private BitSet affectedeConcepts;
    private BitSet affectedeRelations;

    public BitSet getAffectedConcepts() {
        return affectedeConcepts;
    }

    public BitSet getAffectedRelations() {
        return affectedeRelations;
    }

    // Optimization parameters
    private int numberOfClassesLimit = 5;

    /**
     *
     * // these are now read from a file. The name of the set is the same as
     * the .sqs file in the $ONDEX_DIR\config\query_sets directory // A URL
     * which links a node representing a relation to its type which defines it
     * as a relation. public String REIFIED_TYPE =
     * "http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement";
     *
     * // $$URI$$ is the URI of the entity representing the reified relation.
     * The query must return a triple ?s ?p ?o representing the relation (?p
     * predicate). This is limited, but this is what is supported now! public
     * String REIFIED_QUERY ="construct {?s ?p ?o} where { GRAPH ?g {" +
     * "<~~URI~~> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> ?s. " +
     * "<~~URI~~> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> ?p. " +
     * "<~~URI~~> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> ?o. }}";
     * public String REIFIED_ATTRIBUTE_QUERY="select ?p ?o where { graph ?g {"
     * +"<~~URI~~> ?p ?o . }}"; public String TYPES_QUERY="select distinct ?x
     * where { GRAPH ?g { <~~URI~~>
     * <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?x}} LIMIT 5"; public
     * String[] CLASSES_PREFRENCE_LIST=null; public String
     * CONCEPT_ATTRIBUTES_QUERY="select ?p ?o where {graph ?g {<~~URI~~> ?p ?o}}
     * limit 20";
     */
    /**
     * Main command parsing "loop". Plus function related to the SPARQL console
     * operations.
     */
    // TODO Clarify: this is called at each double enter, and loops over handlers, corresponding to interpreters of a single command.
    // TODO Curiosity: a simple switch would have been more efficient, though this is very marginal.
    public String process(String command, OutputPrinter out) {
        if (!isReady) {
            return "Interpreter disabled, as the configuration was missing/corrupted";
        }
        this.out = out;
        if (pc != null) {
            pc.processingStarted();
        }

        String exception = null;
        try {
            for (CommandHandler h : handlers) {
                Callable c = h.canHandle(command);
                if (c != null) {
                    c.call();
                    break;
                }
            }
        } catch (Exception e) {
            exception = e.getMessage();
            e.printStackTrace();
        } finally {
            if (pc != null) {
                pc.processingFinished();
            }

            if (exception != null) {
                out.print("\n" + exception);
            }
            out.prompt();

        }
        return null;
    }

    public void silentProcess(String command, OutputPrinter out) {
        if (!isReady) {
            System.out.println("Interpreter disabled, as the configuration was missing/corrupted");
            return;
        }
        this.out = out;
        if (pc != null) {
            pc.processingStarted();
        }

        String exception = null;
        try {
            for (CommandHandler h : handlers) {
                Callable c = h.canHandle(command);
                if (c != null) {
                    c.call();
                    break;
                }
            }
        } catch (Exception e) {
            exception = e.getMessage();
            e.printStackTrace();
        } finally {
            if (pc != null) {
                pc.processingFinished();
            }
        }
    }

    public String getPrompt() {
        return "SPARQL>";
    }

    public String getWelcomeMessage() {
        return "SPARQL interpreter loaded successfully";
    }

    @Override
    public List<Class<?>> getDependancies() {
        return new ArrayList<Class<?>>();
    }

    @Override
    public void initialize(ProxyTemplate... proxyTemplates) {

    }

    @Override
    public boolean isInitialised() {
        return true;
    }

    @Override
    public void setProcessingCheckpoint(ProcessingCheckpoint pc) {
        this.pc = pc;
    }

    private static interface CommandHandler {

        public Callable canHandle(String commandt) throws Exception;
    }

    private class ConnectHandler implements CommandHandler {

        String pattern = "connect\\s*?\\(\\\"([^\"]+)\\\"(,\\s*?\\\"([^\"]+?)\\\",\\s*?\\\"(.+?)\\\")??\\)";
        public final Pattern loadPath = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE); //TODO

        @Override
        public Callable canHandle(String command) {
            final Matcher m = loadPath.matcher(command);
            if (m.find()) {
                final String url = m.group(1);
                if (url != null) {
                    return new Callable() {
                        @Override
                        public Object call() throws Exception {
                            myEndPoint = url;

                            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

                            if (m.group(1) != null) {
                                log.debug("user and password found");
                                String user = m.group(3);
                                String passwd = m.group(4);
                                byte[] encoded = org.apache.commons.codec.binary.Base64.encodeBase64(String.valueOf(user + ":" + passwd).getBytes());
                                connection.setRequestProperty("Authorization", "Basic " + new String(encoded));
                            }

                            int status = connection.getResponseCode();
                            String responce;
                            if (status == 200) {
                                responce = "Successfully connected to server (" + String.valueOf(status) + ")";
                            } else if (status == 401) {
                                responce = "Error: Not authorised to access this server (" + String.valueOf(status) + ")";
                            } else if (status == -1) {
                                responce = "Error: No valid server found at the location specified (" + String.valueOf(status) + ")";
                            } else {
                                responce = "Error: code returned by server: " + String.valueOf(status);
                            }
                            connection.disconnect();
                            return responce;
                        }
                    };
                }
            }

            return null;
        }
    }

    private class UseHandler implements CommandHandler {

        //TODO Which pattern ? Could be a URL for file system, or just the file system....
        public final Pattern usePath = Pattern.compile("use\\s*?\\(\\\"(.+?)\\\"\\)", Pattern.CASE_INSENSITIVE);

        @Override
        public Callable canHandle(String command) {
            Matcher m = usePath.matcher(command);
            if (m.find()) {
                final String match = m.group(1);
                if (match != null) {
                    return new Callable() {
                        @Override
                        public Object call() throws Exception {
                            querySet.load(match);
                            prefixes.clear();
                            prefixes.addAll(querySet.getList("prefixes"));
                            resetCache();		// This seems reasonable TODO probably also after we change endpoint (what happens if same attribute/value twice ?)
                            return "Loaded the query set " + match;
                        }
                    };
                }
            }

            return null;
        }
    }

    private class PrefixHandler implements CommandHandler {

        private Pattern pfxP = Pattern.compile("\\s*((?:SET)|(?:CLEAR)|(?:SHOW))\\s+(PREFIX\\s+.+)\\s*",
                Pattern.CASE_INSENSITIVE);

        @Override
        public Callable canHandle(String command)
                throws Exception {
            Matcher sm = pfxP.matcher(command);
            if (sm.matches()) {
                String pfxCmd = sm.group(1);
                final String pfxBody = sm.group(2);

                if (pfxCmd.equalsIgnoreCase("SET")) {
                    return new Callable() {
                        @Override
                        public Object call() throws Exception {
                            prefixes.add(pfxBody);
                            gm.removePrefix(pfxBody);
                            return null;
                        }
                    };
                }

                if (pfxCmd.equalsIgnoreCase("CLEAR")) {
                    return new Callable() {
                        @Override
                        public Object call() throws Exception {
                            for (Iterator<String> iterator = prefixes.iterator(); iterator.hasNext();) {
                                String pfx = iterator.next();
                                if (pfx.startsWith(pfxBody)) {
                                    iterator.remove();
                                    gm.removePrefix(pfxBody);
                                }
                            }

                            return null;
                        }
                    };
                }

                if (pfxCmd.equalsIgnoreCase("SHOW")) {
                    return new Callable() {
                        @Override
                        public Object call() throws Exception {
                            for (String pfx : prefixes) {
                                out.print(pfx);
                            }

                            return null;
                        }
                    };
                }
            }

            return null;
        }
    }

    private class ExpandHandler implements CommandHandler {

        public final Pattern loadPath = Pattern.compile("^ *?expand *?$", Pattern.CASE_INSENSITIVE);

        @Override
        public Callable canHandle(String command) {
            if (myEndPoint == null) {
                out.printAndPrompt("Error - no endpoint was selected.");
                return null;
            } else {
                Matcher m = loadPath.matcher(command);
                if (m.find()) {
                    final String match = m.group(1);
                    if (match != null) {
                        return new Callable() {
                            @Override
                            public Object call() throws Exception {
                                ONDEXGraph graph = null;
                                try {
                                    graph = OndexScriptingInitialiser.getAbstractONDEXGraph();
                                } catch (Exception e1) {
                                    out.printAndPrompt("Error - no empty graph found");
                                }

                                if (graph == null) {
                                    out.printAndPrompt("Error - no empty graph found");
                                    return null;
                                }

                                gm.setGraph(graph);
                                gm.startProcessing();
                                gm.setEndpointURI(myEndPoint);
                                try {
                                    Map<String, ONDEXConcept> selectedIndex = gm.getIndex();

                                    //TODO expand code goes here
                                    Model resultModel = null;
                                    com.hp.hpl.jena.util.iterator.ExtendedIterator<Triple> it = resultModel.getGraph().find(Node.ANY, Node.ANY, Node.ANY);
                                    while (it.hasNext()) {
                                        Triple t = it.next();
                                        Node s = t.getSubject();
                                        ONDEXConcept cS = gm.getExistingConcept(s);
                                        if (s != null) {
                                            Node p = t.getPredicate();
                                            Node o = t.getObject();
                                            if (o.isURI()) {
                                                ONDEXConcept cO = getOrCreateOndexConcept(o);
                                                ONDEXRelation r = gm.createRelation(cS, cO, p);
                                            } else if (o.isLiteral()) {
                                                if (p.isURI()) {
                                                    gm.addAttribute(cS, o, p.getLocalName());
                                                } else if (p.isBlank()) {
                                                    gm.addAttribute(cS, o, p.getBlankNodeId().getLabelString());
                                                }
                                            }
                                        }
                                    }
                                } finally {
                                    gm.finishProcessing();
                                }
                                return null;
                            }
                        };
                    }
                }
                return null;
            }
        }
    }

    // TODO here
    private class SparqlHandler implements CommandHandler {

        @Override
        public Callable canHandle(final String command) {
            if (myEndPoint == null) {
                out.printAndPrompt("Error - no endpoint was selected.");
                return null;
            } else {
                return new Callable() {
                    @Override
                    public Object call()
                            throws Exception {
                        ONDEXGraph graph = null;
                        try {
                            graph = OndexScriptingInitialiser.getAbstractONDEXGraph();
                        } catch (Exception e1) {
                            out.printAndPrompt("Error - no empty graph found");
                        }

                        if (graph == null) {
                            out.printAndPrompt("Error - no empty graph found");
                            return null;
                        }

                        gm.setGraph(graph);
                        gm.startProcessing();
                        gm.updateIndex();
                        gm.setEndpointURI(myEndPoint);
                        try {
                            StringBuilder qb = new StringBuilder();
                            for (String p : prefixes) {
                                qb.append(p).append("\n");
                            }
                            qb.append(command);
                            String finalCommand = qb.toString();

                            Syntax mySyntax = Syntax.lookup(finalCommand);
                            System.out.println(mySyntax);

                            System.out.println(finalCommand);
                            /*
                             * TODO: still a problem ?
                             * Due to conflicting libraries, we cannot parse queries with extended sparql features.
                             * We circumvent this problem (short term hack) by asking directly to end-point, in case of problems.
                             * We only do this for construct queries.
                             */

                            Query myQuery = null;
                            QueryExecution queryExecution = null;
                            ResultSet queryResults = null;
                            Model resultModel = null;

                            boolean hackIsOn = false;
                            try {
                                myQuery = QueryFactory.create(finalCommand, Syntax.syntaxSPARQL);
                                queryExecution = QueryExecutionFactory.sparqlService(myEndPoint, finalCommand);
                            } catch (Exception e) {
                                hackIsOn = true;
                                System.out.println("Cannot parse the query, asking directly the endpoint");
                            }

                            if (!hackIsOn) {
                                if (myQuery.isSelectType()) {
                                    queryResults = queryExecution.execSelect();
                                    select(queryResults);
                                } else if (myQuery.isConstructType()) {
                                    resultModel = queryExecution.execConstruct();
                                    construct(resultModel);
                                } else if (myQuery.isAskType()) {
                                    // Do nothing
                                } else if (myQuery.isDescribeType()) {
                                    // TODO: should be processed as construct
                                }
                            } else {
                                //TODO should replace.
                                String queryURL = myEndPoint + "?query=" + java.net.URLEncoder.encode(finalCommand); //it's a hack anyway...
                                System.out.println(queryURL);
                                resultModel = ModelFactory.createDefaultModel();
                                resultModel.read(queryURL);

                                construct(resultModel);
                            }

                            /*
                             *  TODO this must be some Ondex flush function, after which which relations and which concepts have been affected is recorded,
                             *  at least to highlight last added elements ?
                             *  What is the processing checkpoint ?
                             *  At first this "may" be related to caching queries (or better, not asking twice). 
                             */
                            BitSet[] cr = gm.finishProcessing();
                            affectedeRelations = cr[1];
                            affectedeConcepts = cr[0];
                            if (pc != null) {

                                Set<ONDEXConcept> selectedConcepts = BitSetFunctions.create(graph, ONDEXConcept.class, cr[0]);
                                Set<ONDEXRelation> selectedRelations = BitSetFunctions.create(graph, ONDEXRelation.class, cr[1]);
                                pc.setSelectedSubset(graph, selectedConcepts, selectedRelations);
                            }
                            return null;
                        } finally {
                            gm.finishProcessing();
                        }
                    }
                };
            }
        }
    }

    //========================================================
    //======Results-parsing methods are below=================
    //========================================================

    /*
     * Parse the results of a select queries (a matrix).
     * It iterates elements by elements and:
     * of an element is a literal -> print on consolle (ony if only literals are asked for)
     * if an element is a URI -> check types.
     * if type is not a REIFIED_STATEMENT -> add node (end)
     * if it is a reified statement -> ExpandReifiedStatements (end)
     */
    private void select(ResultSet queryResults) throws InvocationTargetException, IllegalAccessException {
        System.out.println("PROCESSING SELECT");
        Set<Resource> resources = new HashSet<Resource>();
        Set<Literal> literals = new HashSet<Literal>();
        int numberOfResults = 0;
        while (queryResults.hasNext()) {
            QuerySolution res = queryResults.next();
            for (String var : queryResults.getResultVars()) {
                RDFNode n = res.get(var);
                if (n.isResource()) {
                    resources.add((Resource) n);
                    numberOfResults++;
                } else if (n.isLiteral()) {
                    literals.add((Literal) n);
                }
            }
        }
        for (Resource r : resources) {
            String nodeUri = r.getURI();
            if (nodeUri != null) {
                List<String> types = checkForURITypes(r.asNode());	// Here we ask for types
                boolean isConcept = true;
                for (String type : types) {	// is this a reification ?
                    System.out.println("DEBUG: FOUND TYPE: " + type);
                    System.out.println("DEBUG: mathcing against: " + querySet.getQuery("reified_type").trim());
                    if (type.equals(querySet.getQuery("reified_type").trim())) { //REIFIED_TYPE
                        isConcept = false;
                        expandReifiedStatement(nodeUri);
                    }
                }
                if (isConcept) {										// if we are here, this must be a Resource
                    getOrCreateOndexConcept(r.asNode());
                }
				// if this is a statement then fetch all edge and annotations
                // if edge exists, add annotation, otherwise add edge and nodes

                // How can it be the statement and what checks would reveal it?
                // And how to extract the information that is to go into the
                // attribute and relations from this set?
            }
        }
        if (resources.isEmpty()) {
            // Note: here we print literals if the result is only literals, which makes sense.
            for (Literal l : literals) {
                String value = l.getString();
                out.print(value);
            }
        }
    }

    /**
     * Check for a list of types associated to a resource TODO the LIMIT in
     * query could be made parametric depending on an optimization parameter.
     */
    public List<String> checkForURITypes(Node node) {
        System.out.println("DEBUG: checking types");
        if (myEndPoint == null) {
            return new ArrayList<String>(0);
        }
        String nodeUri = node.getURI();
        if (nodeUri == null || nodeUri.length() == 0) {
            return Collections.emptyList();
        }
        System.out.println("DEBUG: url: " + nodeUri);
        StringBuilder qb = new StringBuilder();
        //for(String p : prefixes) {
        //	qb.append(p).append("\n");
        //}
        String myQueryString = new String(querySet.getQuery("types_query")); //TYPES_QUERY
        myQueryString = myQueryString.replace("~~URI~~", nodeUri);
        qb.append(myQueryString);
        String finalQuery = qb.toString();
        System.out.println("DEBUG: checking for types with the following query:");
        System.out.println(finalQuery);
        Query myQuery = QueryFactory.create(finalQuery);
        List<String> result = new LinkedList<String>();
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(myEndPoint, finalQuery);
        ResultSet queryResults = queryExecution.execSelect();
        while (queryResults.hasNext()) {
            QuerySolution res = queryResults.next();
            for (String var : myQuery.getResultVars()) {
                RDFNode n = res.get(var);
                if (n.isURIResource()) {
                    // NOTE: we are passing full URIs, for the time being
                    result.add(n.asNode().getURI());
                }
            }
        }
        return result;
    }

    /**
     * Expand a reified statement (add nodes, and relations). If the expansion
     * doesn't succeed, it returns a null relation
     *
     * @param uri
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private ONDEXRelation expandReifiedStatement(String uri) throws InvocationTargetException, IllegalAccessException {
        ONDEXRelation r = null;

        System.out.println("DEBUG: REIFIED: Processing reified: " + uri);
        StringBuilder qb = new StringBuilder();
        //for(String p : prefixes) {
        //	qb.append(p).append("\n");
        //}
        String myQueryString = new String(querySet.getQuery("reified_query")); //REIFIED_QUERY
        myQueryString = myQueryString.replace("~~URI~~", uri);
        qb.append(myQueryString);
        String fetchReifiedQuery = qb.toString();
        System.out.println(fetchReifiedQuery);
        Query myQuery = QueryFactory.create(fetchReifiedQuery);
        QueryExecution statementQueryExecution = QueryExecutionFactory.sparqlService(myEndPoint, myQuery);

        Model resultModel = statementQueryExecution.execConstruct();

        com.hp.hpl.jena.util.iterator.ExtendedIterator<Triple> it = resultModel.getGraph().find(Node.ANY, Node.ANY, Node.ANY);
        // Note: we don't really iterate as only one result is expected.
        if (!it.hasNext()) {
            return r;
        }
        Triple t = it.next();
        Node s = t.getSubject();
        Node p = t.getPredicate();
        Node o = t.getObject();
        // NOTE: we don't really care that the actual variables are ?s ?p ?o

        if (!p.isURI()) {
            return r;	// If the property is not an URI the relation expansion has failed.
        }
        if (!s.isURI()) {
            return r;	// If the subject is not an URI the relation expansion has failed.
        }
        if (!o.isURI()) {
            return r;	// If the object is not an URI the relation expansion has failed.
        }		// Note that we don't expand reification with blank nodes as we won't know what to do with them

        ONDEXConcept cS = addOrCreateOrFillOndexCFromNode(s); // Caching is done on gm.... TODO: clarify: is it done as an hash on he Jena Node object ?
        ONDEXConcept cO = addOrCreateOrFillOndexCFromNode(o); // Caching is done on gm.... TODO: clarify: is it done as an hash on he Jena Node object ?
        r = gm.createRelation(cS, cO, p); // this should have some caching in gm

        if (!alreadyAskedRelationAttributes.contains(uri)) {
            StringBuilder qb2 = new StringBuilder();
            myQueryString = new String(querySet.getQuery("reified_attribute_query")); //REIFIED_ATTRIBUTE_QUERY
            myQueryString = myQueryString.replace("~~URI~~", uri);
            qb2.append(myQueryString);
            String finalQuery2 = qb2.toString();
            System.out.println("DEBUG: REIFIED: query for reified attributes :");
            System.out.println(finalQuery2);

            QueryExecution gdsQueryExecution = QueryExecutionFactory.sparqlService(myEndPoint, finalQuery2);
            ResultSet queryResults = gdsQueryExecution.execSelect();
            while (queryResults.hasNext()) {
                QuerySolution res = queryResults.next();
                RDFNode gdsProperty = res.get("?p");
                RDFNode gdsObject = res.get("?o");
                if (gdsObject.isLiteral()) {
                    System.out.println("Pro Attribute: " + gdsProperty.toString() + " " + gdsObject.toString());
                    String propString = ((Resource) (gdsProperty)).getLocalName();
                    Object propValue = ((Literal) (gdsObject)).getValue();
                    gm.addAttribute(r, propString, propValue);
                }
            }

            alreadyAskedRelationAttributes.add(uri);
        }

        return r;
    }

    ONDEXConcept addOrCreateOrFillOndexCFromNode(Node n) {
        ONDEXConcept result = getOrCreateOndexConcept(n); //Caching is done on gm.... TODO: clarify: is it done as an hash on he Jena Node object ?
        //Do we know attributes ?
        if (!alreadyAskedNodeAttributes.contains(n.getURI())) {
            //fill it
            StringBuilder qb2 = new StringBuilder();
            String myQueryString = new String(querySet.getQuery("concept_attribute_querry")); //CONCEPT_ATTRIBUTES_QUERY
            myQueryString = myQueryString.replace("~~URI~~", n.getURI());
            qb2.append(myQueryString);
            String finalQuery2 = qb2.toString();
            System.out.println("DEBUG: ADD_NODE: query for node attributes:");
            System.out.println(finalQuery2);

            QueryExecution gdsQueryExecution = QueryExecutionFactory.sparqlService(myEndPoint, finalQuery2);
            ResultSet queryResults = gdsQueryExecution.execSelect();
            while (queryResults.hasNext()) {
                QuerySolution res = queryResults.next();
                RDFNode gdsProperty = res.get("?p");
                RDFNode gdsObject = res.get("?o");
                if (gdsObject.isLiteral()) {
                    System.out.println("Pro Attribute: " + gdsProperty.toString() + " " + gdsObject.toString());
                    String propString = ((Resource) (gdsProperty)).getLocalName();
                    Object propValue = ((Literal) (gdsObject)).getValue();
                    gm.addAttribute(result, propString, propValue);
                }
            }

            // end fill it
            alreadyAskedNodeAttributes.add(n.getURI());
        }

        return result;
    }

    public void loadFromURL(String url) {
        if (isReady) {
            out.printAndPrompt("Interpreter disabled, as the configuration was missing/corrupted");
            return;
        }
        String temp = myEndPoint;
        ONDEXGraph graph = null;
        try {
            graph = OndexScriptingInitialiser.getAbstractONDEXGraph();
        } catch (Exception e1) {
            out.printAndPrompt("Error - no empty graph found");
        }

        if (graph == null) {
            out.printAndPrompt("Error - no empty graph found");
            return;
        }
        try {
            gm.setGraph(graph);
            gm.startProcessing();
            int index = url.lastIndexOf("\\");
            String cv = "IMPD";
            if (index > -1) {
                cv = url.substring(0, index);
            }
            gm.setEndpointURI(cv);
            Model model = ModelFactory.createDefaultModel();
            model.read(url);
            this.construct(model);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            BitSet[] cr = gm.finishProcessing();
            affectedeRelations = cr[1];
            affectedeConcepts = cr[0];
            if (pc != null) {
                Set<ONDEXConcept> selectedConcepts = BitSetFunctions.create(graph, ONDEXConcept.class, cr[0]);
                Set<ONDEXRelation> selectedRelations = BitSetFunctions.create(graph, ONDEXRelation.class, cr[1]);
                pc.setSelectedSubset(graph, selectedConcepts, selectedRelations);
            }
            if (temp != null) {
                gm.setEndpointURI(temp);
            }
        }
    }

    private void construct(Model resultModel) throws InvocationTargetException, IllegalAccessException {
        com.hp.hpl.jena.util.iterator.ExtendedIterator<Triple> it = resultModel.getGraph().find(Node.ANY, Node.ANY, Node.ANY);
        while (it.hasNext()) {
            System.out.print("checking triple... ");
            Triple t = it.next();
            Node s = t.getSubject();
            ONDEXConcept cS = getOrCreateOndexConcept(s);
            Node p = t.getPredicate();
            if (p.isURI()) {
                System.out.println("Property is a URI");
            }
            Node o = t.getObject();
            if (o.isURI()) {
                System.out.println("object is URI");
                ONDEXConcept cO = getOrCreateOndexConcept(o);
                ONDEXRelation r = gm.createRelation(cS, cO, p);
                System.err.println(r);
            } else if (o.isLiteral()) {
                System.out.println("object is Literal");
                if (p.isURI()) {
                    gm.addAttribute(cS, o, p.getLocalName());
                } else if (p.isBlank()) {
                    gm.addAttribute(cS, o, p.getBlankNodeId().getLabelString());
                }
            }
        }
    }

    private ONDEXConcept getOrCreateOndexConcept(Node n) {
        ONDEXConcept result = gm.getExistingConcept(n);
        if (result != null) {
            return result;
        }
        return gm.createConceptForNode(n, checkForURITypes(n));
    }

    //TODO Change the code to use this class?
    /**
     * Processes the statements in batch
     *
     * @author lysenkoa
     *
     */
    private class BatchProcessor {

        private Set<Node> nodes = new HashSet<Node>();
        private MultiMap<ArrayKey<Node>, Node> toRelations = new MultiHashMap<ArrayKey<Node>, Node>();
        private MultiMap<Node, ArrayKey<Object>> toAttributes = new MultiHashMap<Node, ArrayKey<Object>>();
        private Map<String, List<String>> concpetClasses = new HashMap<String, List<String>>();

        public void addNode(Node n) {
            nodes.add(n);
        }

        public Map<Node, List<String>> getTypes(Set<Node> nodes) {
            Map<Node, List<String>> result = new HashMap<Node, List<String>>();
            //TODO - the magic query to retrieve the types in bulk goes here
            return result;
        }

        public void addRelation(Node from, Node to, Node relation) {
            nodes.add(from);
            nodes.add(to);
            toRelations.put(new ArrayKey<Node>(new Node[]{from, to}), relation);
        }

        public void addAttribute(Node n, String type, Object value) {
            nodes.add(n);
            toAttributes.put(n, new ArrayKey<Object>(new Object[]{type, value}));
        }

        public void process() throws InvocationTargetException, IllegalAccessException {
            Map<Node, ONDEXConcept> concepts = new HashMap<Node, ONDEXConcept>();
            Map<Node, ONDEXRelation> relations = new HashMap<Node, ONDEXRelation>();
            Set<Node> toCreate = new HashSet<Node>();
            for (Node n : nodes) {
                ONDEXConcept result = gm.getExistingConcept(n);
                if (result != null) {
                    concepts.put(n, result);
                } else {
                    toCreate.add(n);
                }
            }
            for (Entry<Node, List<String>> ent : getTypes(toCreate).entrySet()) {
                if (ent.getValue().contains(querySet.getQuery("reified_type"))) { //REIFIED_TYPE
                    ONDEXRelation r = expandReifiedStatement(ent.getKey().getURI());
                    if (r == null) {
                        System.err.println("Could not create reefied " + ent.getKey());
                    }
                    relations.put(ent.getKey(), r);
                } else {
                    concepts.put(ent.getKey(), gm.createConceptForNode(ent.getKey(), ent.getValue()));
                }

            }
            for (Entry<ArrayKey<Node>, Collection<Node>> ent : toRelations.map().entrySet()) {
                ONDEXConcept from = concepts.get(ent.getKey().getArray()[0]);
                ONDEXConcept to = concepts.get(ent.getKey().getArray()[1]);
                //TODO What is supposed to happen if the node was reified?
                if (from == null || to == null) {
                    System.err.println("Missing concept " + ent.getKey().getArray()[0]);
                    continue;
                }
                for (Node r : ent.getValue()) {
                    gm.createRelation(from, to, r);
                }
            }
            for (Entry<Node, Collection<ArrayKey<Object>>> ent : toAttributes.map().entrySet()) {
                ONDEXEntity entity = concepts.get(ent.getKey());
                if (entity == null) {
                    entity = relations.get(ent.getKey());
                    if (entity == null) {
                        System.err.println("Could not find " + ent.getKey());
                    }
                }
                for (ArrayKey<Object> atts : ent.getValue()) {
                    Object[] array = atts.getArray();
                    if (array.length == 2) {
                        gm.addAttribute(entity, (String) array[0], array[1]);
                    } else if (array.length == 3) {
                        gm.addAttribute(entity, (String) array[0], array[1], (Class) array[2]);
                    }
                }
            }
            reset();
        }

        public void reset() {
            nodes.clear();
            toRelations.clear();
            toAttributes.clear();
            concpetClasses.clear();
        }
    }
}
