/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.nclondexexpression;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.IntegerRangeArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.args.FloatRangeArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

import de.mpg.mpiinf.csb.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXRelation;
import uk.ac.ncl.cs.nclondexexpression.tools.DataSourceArgumentDefinition;
import uk.ac.ncl.cs.nclondexexpression.tools.DefaultPathTemplates;
import uk.ac.ncl.cs.nclondexexpression.tools.Path;
import uk.ac.ncl.cs.nclondexexpression.tools.PathSearcher;
import uk.ac.ncl.cs.nclondexexpression.tools.PathTemplate;

/**
 *
 * @author Nicolas Alcaraz Millman
 */
public class KPMTransformer extends ONDEXTransformer {

    // Indexing of concepts
    private HashMap<String, ONDEXConcept> idToConcept = new HashMap<String, ONDEXConcept>();

    // Indexing of relations
    private HashMap<String, ONDEXRelation> idToRelation = new HashMap<String, ONDEXRelation>();

    @Override
    public String getId() {
        return "KPM";
    }

    @Override
    public String getName() {
        return "Key Pathway Miner";
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {

        /** Basic parameters **/
        String argname1 = "K";
        String argdesc1 = "Allowed Exceptions in network";
        String argname2 = "L";
        String argdesc2 = "Allowed Exceptions in expression paths";

        IntegerRangeArgumentDefinition iad1 = 
                new IntegerRangeArgumentDefinition(argname1, argdesc1, true,
                    0, 0, 10000);
        IntegerRangeArgumentDefinition iad2 = 
                new IntegerRangeArgumentDefinition(argname2, argdesc2, true,
                    0, 0, 30);

        /** Advanced parameters **/

        String argname3 = "topKResults";
        String argdesc3 = "Number of results to show";

        IntegerRangeArgumentDefinition iad3 = 
                new IntegerRangeArgumentDefinition(argname3, argdesc3, false,
                    10, 0, 100);

        String argname4 = "sortTopKby";
        String argdesc4 = "Criteria to sort top results by";
               
        StringArgumentDefinition sad1 = new StringArgumentDefinition(argname4, 
                argdesc4, false, String.valueOf(SortAntsBy.NO_NODES), false);


    String argname5 = "alpha";
        String argdesc5 = "Parameter that gives importance to pheromone on edge";

        FloatRangeArgumentDefinition fad1 = 
                new FloatRangeArgumentDefinition(argname5, argdesc5, false,
                    5.0f, 1.0f, 5.0f);

        String argname6 = "beta";
        String argdesc6 = "Parameter that gives importance to desirability of edge";
        
        FloatRangeArgumentDefinition fad2 =
                new FloatRangeArgumentDefinition(argname6, argdesc6,
                    false, 2.0f, 1.0f, 5.0f);


        String argname7 = "rho";
        String argdesc7 = "Parameter that defines the pheromone decay rate";

        FloatRangeArgumentDefinition fad3 =
                new FloatRangeArgumentDefinition(argname7, argdesc7,
                    false, 0.01f, 0.0f, 1.0f);


        String argname8 = "randomFactor";
        String argdesc8 = "Parameter that controls the randomness of the search";

        FloatRangeArgumentDefinition fad4 =
                new FloatRangeArgumentDefinition(argname8, argdesc8,
                    false, 0.01f, 0.0f, 1.0f);

        String argname9 = "antPercentage";
        String argdesc9 = "Percentage of ants created relative to the number of nodes";

        FloatRangeArgumentDefinition fad5 =
                new FloatRangeArgumentDefinition(argname9, argdesc9,
                    false, 0.01f, 0.001f, 1.0f);

        String argname10 = "maxIterations";
        String argdesc10 = "Maximum number of iterations the algorithm will be allowed to run";

        IntegerRangeArgumentDefinition iad4 =
                new IntegerRangeArgumentDefinition(argname10, argdesc10, false,
                    200, 1, 10000);

        String argname11 = "stopIterations";
        String argdesc11 = "Number of iterations with no change in results before stopping algorithm";

        IntegerRangeArgumentDefinition iad5 =
                new IntegerRangeArgumentDefinition(argname11, argdesc11, false,
                    5, 1, 9999);


        String argname12 = "warmUpIterations";
        String argdesc12 = "Number of warm up iterations where no results will be stored";

        IntegerRangeArgumentDefinition iad6 =
                new IntegerRangeArgumentDefinition(argname12, argdesc12, false,
                    5, 0, 9998);

        String argname13 = "Namespace";
        String argdesc13 = "Gene namespace";
        DataSourceArgumentDefinition ns = new DataSourceArgumentDefinition(argname13, argdesc13, true);

        String argname14 = "threshold";
        String argdesc14 = "Differential expression threshold";

        FloatRangeArgumentDefinition ths =
                new FloatRangeArgumentDefinition(argname14, argdesc14,
                    false, 0.6f, 0.0f, 10.0f);


        String argname15 = "isDirected";
        String argdesc15 = "Flag to state if network is directed or not";

        BooleanArgumentDefinition bad = new BooleanArgumentDefinition(argname15,
                   argdesc15, false, false);

        String argname16 = "interactionType";
        String argdesc16 = "Interaction type";

        StringArgumentDefinition it = new StringArgumentDefinition(argname16,
                  argdesc16, true, null, false);

        return new ArgumentDefinition<?>[]{iad1, iad2, iad3, sad1, fad1, fad2,
                    fad3, fad4, fad5, iad4, iad5, iad6, ns, ths, bad, it};
    }

    

    @Override
    public void start() throws Exception {

        // Create the network
        KPMNetwork g = createKPMNetwork();

        // Get the parameters
        int geneExceptions = (Integer)getArguments().getUniqueValue("K");
        int caseExceptions = (Integer)getArguments().getUniqueValue("L");
        int topKResults = (Integer)getArguments().getUniqueValue("topKResults");
        char sortTopKBy =  ((String)getArguments().getUniqueValue("sortTopKby")).charAt(0);
        double alpha = ((Float)getArguments().getUniqueValue("alpha")).doubleValue();
        double beta = ((Float)getArguments().getUniqueValue("beta")).doubleValue();
        double rho = ((Float)getArguments().getUniqueValue("rho")).doubleValue();
        double randomFactor = ((Float)getArguments().getUniqueValue("randomFactor")).doubleValue();
        double antPercentage = ((Float)getArguments().getUniqueValue("antPercentage")).doubleValue();
        int maxIterations = (Integer)getArguments().getUniqueValue("maxIterations");
        int stopIterations = (Integer)getArguments().getUniqueValue("stopIterations");
        int warmUpIterations = (Integer)getArguments().getUniqueValue("warmUpIterations");

        KeyPathwayMiner kpm = new KeyPathwayMiner(g, geneExceptions, caseExceptions, topKResults, sortTopKBy,
                alpha, beta, rho, randomFactor, antPercentage, maxIterations,
                stopIterations, warmUpIterations);
                /*
                 * removed stopIterations parameter
                 * added:
                 * maxRunsWOChange
                 * placementStrategy
                 * profile
                 */
        // Run the algorithm
        kpm.start();

        // Sort results and transform to ONDEX contexts
        LinkedList<Ant> topAnts = kpm.getTopKAnts();
        Collections.sort(topAnts, new SortAntsBy(SortAntsBy.NO_NODES));
        Collections.reverse(topAnts);

        ConceptClass ccDPathway = requireConceptClass("Path");
        EvidenceType etTransf = requireEvidenceType("InferredByTransformation");
        DataSource cv = requireDataSource("UC");

        int rank = 1;
        for (Ant ant: topAnts) {
            String dPathwayId = "dp-" + rank;
            ONDEXConcept dPathway = graph.getFactory().createConcept(dPathwayId, cv, ccDPathway, etTransf);
                  
            for (GeneEdge edge: ant.getVisitedEdges().values()) {
                HashSet pathSet = (HashSet)edge.getPaths();
                for (Object obj: pathSet) {
                    Path path = (Path)obj;
                    // Check here in case duplicated contexts are a problem
                    for (ONDEXEntity element: path.getElements()) {
                        element.addTag(dPathway);
                    }
                }                
            }
            
            rank++;
        }

    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    
    private KPMNetwork createKPMNetwork() throws Exception {
        HashMap<String, int[]> nodeIdToCases = new HashMap<String, int[]>();
        LinkedList<String[]> edgeList = new LinkedList<String[]>();
        HashMap<String, String> nodeIdToGeneId = new HashMap<String, String>();
        HashMap<String, Set> edgeIdToPathSet = new HashMap<String, Set>();

        /*String namespaceID = (String)getArguments().getUniqueValue("Namespace");
        DataSource geneNamespace = requireDataSource(namespaceID);*/
        DataSource geneNamespace = (DataSource)getArguments().getUniqueValue("Namespace");

        Float threshold = (Float)getArguments().getUniqueValue("threshold");
        String interactionType = (String)getArguments().getUniqueValue("interactionType");

        AttributeName anExpression = graph.getMetaData().getAttributeName("EXPMAP");        

        for (ONDEXConcept concept: graph.getConceptsOfConceptClass(graph.getMetaData().getConceptClass("Gene"))) {
            

            String nodeId = getAccessionOfNamespace(concept, geneNamespace);
                   
            nodeIdToGeneId.put(nodeId, Integer.toString(concept.getId()));

            
            if (concept.getAttribute(anExpression) == null) {
                continue;
            }
            Map<String, Double> expMap = (Map<String,Double>)concept.getAttribute(anExpression).getValue();

            int[] expValues = new int[expMap.size()];
            Iterator<Double> expIterator = expMap.values().iterator();
            for (int i = 0; i < expValues.length; i++) {
                double expressionValue = expIterator.next();
                int label = 0;
                if (expressionValue < -threshold) {
                    label = -1;
                } else if (expressionValue > threshold) {
                    label = 1;
                }
                expValues[i] = label;
            }

            nodeIdToCases.put(nodeId, expValues);
            idToConcept.put(nodeId, concept);
        }


        DefaultPathTemplates templates = new DefaultPathTemplates(graph);

        PathTemplate template = templates.getTemplate(interactionType);

        PathSearcher searcher = new PathSearcher(template, graph);
        searcher.search();

        Path path;
        

        while ((path = searcher.nextPath()) != null) {

            ONDEXConcept gene1 = path.head();
          
            String fromNodeId = getAccessionOfNamespace(gene1, geneNamespace);
          
            ONDEXConcept gene2 = path.tail();
          
            String toNodeId = getAccessionOfNamespace(gene2, geneNamespace);

            if (gene1.getAttribute(anExpression) == null || gene1.getAttribute(anExpression) == null) {
                continue;
            }
            String edgeId = fromNodeId+" - "+toNodeId;

            
            if (edgeIdToPathSet.containsKey(edgeId)) {
                Set pathSet = edgeIdToPathSet.get(edgeId);
                pathSet.add(path);
                edgeIdToPathSet.put(edgeId, pathSet);
            } else {
                Set pathSet = new HashSet<Path>();
                pathSet.add(path);
                edgeIdToPathSet.put(edgeId, pathSet);
                String[] pair = new String[2];
                pair[0] = fromNodeId;
                pair[1] = toNodeId;
                if (nodeIdToCases.containsKey(fromNodeId) && nodeIdToCases.containsKey(toNodeId)){
                    edgeList.add(pair);
                }
                
                
            }
        }

        System.out.println("edgeList.size() = "+edgeList.size());

        Boolean isDirected = (Boolean)getArguments().getUniqueValue("isDirected");
        System.out.println("Number of nodes: "+nodeIdToCases.size());
        KPMNetwork kpmn = new KPMNetwork(isDirected, nodeIdToCases, edgeList, nodeIdToGeneId, edgeIdToPathSet);
        return kpmn;

    }

    private String getAccessionOfNamespace(ONDEXConcept concept, DataSource geneNamespace) {
        for (ConceptAccession accession: concept.getConceptAccessions()) {
            if (accession.getElementOf().equals(geneNamespace)) {
                return accession.getAccession();
            }
        }

        return null;
    }

}
