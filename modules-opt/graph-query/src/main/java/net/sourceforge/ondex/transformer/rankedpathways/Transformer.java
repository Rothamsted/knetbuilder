package net.sourceforge.ondex.transformer.rankedpathways;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.algorithm.graphquery.GraphTraverser;
import net.sourceforge.ondex.algorithm.graphquery.StateMachine;
import net.sourceforge.ondex.algorithm.graphquery.exceptions.InvalidFileException;
import net.sourceforge.ondex.algorithm.graphquery.exceptions.StateMachineInvalidException;
import net.sourceforge.ondex.algorithm.graphquery.flatfile.StateMachineFlatFileParser;
import net.sourceforge.ondex.algorithm.graphquery.nodepath.EvidencePathNode;
import net.sourceforge.ondex.algorithm.graphquery.nodepath.SerializablePathNode;
import net.sourceforge.ondex.algorithm.graphquery.pathrank.NumericalRank;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.annotations.metadata.AttributeNameRequired;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.RangeArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.config.OndexJAXBContextRegistry;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.tools.tab.exporter.OndexPathPrinter;
import net.sourceforge.ondex.tools.tab.importer.params.FieldArgumentParser;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

/**
 * Used the NDFSA graph traversal algorithm to find paths through the graph and then ranks and filters those paths based on user defined weightings
 *
 * @author hindlem
 */
@Status(description = "Used in Matthew Hindle's PhD thesis: contact matthew.hindle(a|t)rothamsted.ac.uk if you are interested", status = StatusType.EXPERIMENTAL)
@AttributeNameRequired(ids = {MetaData.Path_ATTNAME})
@Custodians(custodians = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
public class Transformer extends ONDEXTransformer implements ArgumentNames {

    private boolean contextsVisible = false;
    private Map<ONDEXConcept, Set<EvidencePathNode>> paths;
    private AttributeName pathGDSs;


    /**
     * The best paths found on the previous run
     *
     * @return
     */
    public Map<ONDEXConcept, Set<EvidencePathNode>> getPaths() {
        return paths;
    }

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new FileArgumentDefinition(STATE_MACHINE_DEF_ARG, STATE_MACHINE_DEF_ARG_DESC, false, true, false, false),
                new BooleanArgumentDefinition(MAKE_TAGS_VISIBLE_ARG, MAKE_TAGS_VISIBLE_ARG_DESC, false, true),
                new RangeArgumentDefinition<Integer>(MAX_PATHWAY_LENGTH_ARG, MAX_PATHWAY_LENGTH_ARG_DESC, false, Integer.MAX_VALUE, 0, Integer.MAX_VALUE, Integer.class),
                new StringArgumentDefinition(INCLUDE_CONCEPT_CLASS_ARG, INCLUDE_CONCEPT_CLASS_DESC, false, null, true),
                //additional paramiters for export
                new StringArgumentDefinition(FILE_ARG, FILE_ARG_DESC, false, null, false),
                new BooleanArgumentDefinition(LINKS_ARG, LINKS_ARG_DESC, false, false),
                new BooleanArgumentDefinition(TRANSLATE_TAXID_ARG, TRANSLATE_TAXID_ARG_DESC, false, false),
                new StringArgumentDefinition(ATTRIB_ARG, ATTRIB_ARG_DESC, false, null, true),
                new BooleanArgumentDefinition(ZIP_FILE_ARG, ZIP_FILE_ARG_DESC, false, true),
                new BooleanArgumentDefinition(INCLUDE_UNRANKABLE_ARG, INCLUDE_UNRANKABLE_ARG_DESC, false, true),
                new BooleanArgumentDefinition(ADD_TAGS_ARG, ADD_TAGS_ARG_DESC, false, false),
                new BooleanArgumentDefinition(FILTER_REDUNDANT_ARG, FILTER_REDUNDANT_ARG_DESC, true, false)
        };
    }

    @Override
    public String getName() {
        return "ranked pathways";
    }

    @Override
    public String getVersion() {
        return "alpha";
    }

    @Override
    public String getId() {
        return "rankedpathways";
    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public String[] requiresValidators() {
        return new String[]{"htmlaccessionlink", "scientificspeciesname"};
    }

    @Override
    public void start() throws InvalidPluginArgumentException, IOException {

        pathGDSs = graph.getMetaData().getAttributeName("Pathways");
        if (pathGDSs == null)
            pathGDSs = graph.getMetaData().getFactory()
                    .createAttributeName(MetaData.Path_ATTNAME, List.class);

        OndexJAXBContextRegistry.instance().addClassBindings(SerializablePathNode.class);

        int maxPathwayLength = (Integer) args.getUniqueValue(MAX_PATHWAY_LENGTH_ARG);
        contextsVisible = (Boolean) args.getUniqueValue(MAKE_TAGS_VISIBLE_ARG);
        List<String> includeConceptClasses = (List<String>) args.getObjectValueList(INCLUDE_CONCEPT_CLASS_ARG);

        boolean useLinks = (Boolean) args.getUniqueValue(LINKS_ARG);
        boolean translateTaxID = (Boolean) args.getUniqueValue(TRANSLATE_TAXID_ARG);
        String exportFile = (String) args.getUniqueValue(FILE_ARG);
        boolean zipFile = (Boolean) args.getUniqueValue(ZIP_FILE_ARG);

        boolean includeUnrankable = (Boolean) args.getUniqueValue(INCLUDE_UNRANKABLE_ARG);

        boolean removeRedundantOnDupEvi = (Boolean) args.getUniqueValue(FILTER_REDUNDANT_ARG);

        //parse attributes
        String[] attArgs = (String[]) args.getObjectValueArray(ATTRIB_ARG);
        FieldArgumentParser parser = new FieldArgumentParser(useLinks, translateTaxID);
        if (attArgs != null) {
            for (String arg : attArgs) {
                parser.parseArguments(arg, graph);
            }
        }

        boolean printToFile = false;
        if (exportFile != null && exportFile.length() > 0 && attArgs != null) {
            printToFile = true;
        }

        Set<ConceptClass> ccs_include = new HashSet<ConceptClass>();
        if (includeConceptClasses != null) {
            for (String cc_st : includeConceptClasses) {
                ConceptClass cc = graph.getMetaData().getConceptClass(cc_st);
                if (cc != null) {
                    ccs_include.add(cc);
                } else {
                    fireEventOccurred(new GeneralOutputEvent("ConceptClass " + cc_st + " not found", this.getClass().getName() + ".start()"));
                }
            }
        }

        File file = translateToFile(args.getUniqueValue(STATE_MACHINE_DEF_ARG));

        int maxLength = 0; //max length of found routes

        try {
            StateMachineFlatFileParser smp = new StateMachineFlatFileParser();
            smp.parseFile(file, graph);
            StateMachine stateMachine = smp.getStateMachine();

            Map<Integer, List<NumericalRank>> priorityOfRankIndex
                    = calculatePriorityOfRankIndex(smp.getRanks());

            Set<ONDEXConcept> concepts = graph.getConceptsOfConceptClass(stateMachine.getStart().getValidConceptClass());
            int conceptNumber = concepts.size();


            Integer[] priorities = priorityOfRankIndex.keySet().toArray(new Integer[priorityOfRankIndex.keySet().size()]);
            Arrays.sort(priorities);

            System.out.println("Traversing graph for " + conceptNumber + " seeds");

            GraphTraverser traverser = new GraphTraverser(stateMachine);
            traverser.setMaxLengthOfAnyRoute(maxPathwayLength);

            Map<ONDEXConcept, List<EvidencePathNode>> pathList = traverser.traverseGraph(graph, concepts, new FilterPathResults(priorityOfRankIndex,
                    priorities, includeUnrankable, removeRedundantOnDupEvi));

            paths = new HashMap<ONDEXConcept, Set<EvidencePathNode>>(pathList.size());
            for (ONDEXConcept concept : pathList.keySet()) {
                paths.put(concept, new HashSet<EvidencePathNode>(pathList.get(concept)));
            }

        } catch (InvalidFileException e) {
            e.printStackTrace();
        } catch (StateMachineInvalidException e) {
            e.printStackTrace();
        }
        System.out.println("\nFiltering Graph " + paths.size() + " starts ");
        filterGraph(paths, ccs_include, (Boolean) args.getUniqueValue(ADD_TAGS_ARG));

        System.out.println("max route Length:" + maxLength);

        if (printToFile) {
            try {
                OndexPathPrinter printer = new OndexPathPrinter(parser.getAttributeModel(),
                        new File(exportFile), maxLength, graph, zipFile);
                for (Set<EvidencePathNode> routesForEntry : paths.values()) {
                    for (EvidencePathNode path : routesForEntry) {
                        printer.printPath(path);
                    }
                }
                printer.close();
            } catch (NullValueException e) {
                e.printStackTrace();
            } catch (AccessDeniedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Map<Integer, List<NumericalRank>> calculatePriorityOfRankIndex(List<NumericalRank> ranks) {
        Map<Integer, List<NumericalRank>> priorityOfRankIndex = new HashMap<Integer, List<NumericalRank>>();
        for (NumericalRank rank : ranks) {
            int levelRank = rank.getRelativeRankofRank();
            List<NumericalRank> level = priorityOfRankIndex.get(levelRank);
            if (level == null) {
                level = new ArrayList<NumericalRank>();
                priorityOfRankIndex.put(levelRank, level);
            }
            level.add(rank);
        }
        return priorityOfRankIndex;
    }

    /**
     * @param paths2
     * @param ccs_include
     * @param addContexts
     */
    private void filterGraph(Map<ONDEXConcept, Set<EvidencePathNode>> paths2,
                             Set<ConceptClass> ccs_include, boolean addContexts) {

        System.out.println("Calculating visible concepts and relations (and Context dependencies) for " + paths2.size() + " paths");

        Set<ONDEXConcept> visibleConcepts = new HashSet<ONDEXConcept>();
        Set<ONDEXRelation> visibleRelations = new HashSet<ONDEXRelation>();

        int previouslyComplete = 0;
        int pathNumber = paths2.size();
        int pathDone = 0;

        for (ONDEXConcept startConcept : paths2.keySet()) {
            Set<EvidencePathNode> pathSet = paths2.get(startConcept);
            pathDone++;

            List<SerializablePathNode> paths = new ArrayList<SerializablePathNode>(pathSet.size());
            for (EvidencePathNode path : pathSet)
                paths.add(new SerializablePathNode(path));
            startConcept.createAttribute(pathGDSs, paths, false);

            for (EvidencePathNode path : pathSet) {
                ONDEXConcept finalConcept = (ONDEXConcept) path.getEntity();

                for (ONDEXRelation relation : (Set<ONDEXRelation>) path.getAllRelations()) {

                    if (addContexts) relation.addTag(startConcept);//context from start
                    if (addContexts) relation.addTag(finalConcept);//context from end

                    visibleRelations.add(relation);
                    if (addContexts) checkContexts(relation, startConcept);

                    ONDEXConcept from = relation.getFromConcept();
                    if (addContexts) from.addTag(startConcept);//context from start
                    if (addContexts) from.addTag(finalConcept);//context from end

                    visibleConcepts.add(from);
                    if (addContexts) checkContexts(from, startConcept);

                    ONDEXConcept to = relation.getToConcept();
                    if (addContexts) to.addTag(startConcept);//context from start
                    if (addContexts) to.addTag(finalConcept);//context from end

                    visibleConcepts.add(to);
                    if (addContexts) checkContexts(to, startConcept);

                }
            }
            int complete = Math.round(((float) pathDone / (float) pathNumber) * 100f);
            if (complete > previouslyComplete) {
                System.out.println("Visibility index built on " + complete + "% of paths");
                previouslyComplete = complete;
            }
        }
        System.out.println("Visibility index complete");

        if (contextsVisible) {
            Set<ONDEXConcept> contexts = new HashSet<ONDEXConcept>();

            for (ONDEXConcept visibleConcept : visibleConcepts)
                contexts.addAll(visibleConcept.getTags());

            for (ONDEXRelation visibleRelation : visibleRelations)
                contexts.addAll(visibleRelation.getTags());
            System.out.println("adding " + contexts.size() + " contexts that also need to be visible");
            visibleConcepts.addAll(contexts);
        }

        for (ConceptClass cc : ccs_include) {
            System.out.println("Setting concept of ConceptClass " + cc.getId() + " visible");
            Set<ONDEXConcept> concepts = graph.getConceptsOfConceptClass(cc);
            visibleConcepts.addAll(graph.getConceptsOfConceptClass(cc));
            for (ONDEXConcept concept : concepts) {
                visibleRelations.addAll(graph.getRelationsOfConcept(concept));
            }
        }

        System.out.println(visibleRelations.size() + " relations should be visible from paths");
        System.out.println(visibleConcepts.size() + " concepts should be visible from paths");

        Set<ONDEXRelation> relationsToDelete = BitSetFunctions.copy(graph.getRelations());
        relationsToDelete.removeAll(visibleRelations);

        System.out.println("removing " + relationsToDelete.size() + " relations from graph");

        for (ONDEXRelation relation : relationsToDelete)
            graph.deleteRelation(relation.getId());

        Set<ONDEXConcept> conceptsToDelete = BitSetFunctions.copy(graph.getConcepts());
        conceptsToDelete.removeAll(visibleConcepts);

        System.out.println("Removing " + conceptsToDelete.size() + " concepts");

        for (ONDEXConcept concept : conceptsToDelete)
            graph.deleteConcept(concept.getId());
    }

    private void checkContexts(ONDEXRelation relation, ONDEXConcept concept) {
        Set<ONDEXConcept> contexts = relation.getTags();
        if (!contexts.contains(concept)) {
            relation.addTag(concept);
        }
    }

    private void checkContexts(ONDEXConcept concept1, ONDEXConcept concept) {
        Set<ONDEXConcept> contexts = concept1.getTags();
        if (!contexts.contains(concept)) {
            concept1.addTag(concept);
        }
    }

    private File translateToFile(Object obj) {
        File file;

        if (obj instanceof File) {
            file = (File) obj;
        } else if (obj instanceof String) {
            file = new File((String) obj);
        } else {
            throw new RuntimeException("Invalid object format for File found :" + obj.getClass());
        }
        return file;
    }


}
