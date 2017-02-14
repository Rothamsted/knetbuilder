package net.sourceforge.ondex.transformer.allcliques;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.RangeArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

import java.util.*;

import static net.sourceforge.ondex.transformer.allcliques.ArgumentNames.*;
import static net.sourceforge.ondex.transformer.allcliques.MetaData.*;

/**
 * Creates relations indicating cliques in the graph.
 *
 * @author hindlem
 */
@Authors(authors = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Transformer extends ONDEXTransformer
{

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new StringArgumentDefinition(RELATION_TYPE_ARG,
                        RELATION_TYPE_ARG_DESC, true, null, false),
                new StringArgumentDefinition(CONCEPCLASS_ARG,
                        CONCEPCLASS_ARG_DESC, true, null, false),
                new RangeArgumentDefinition<Integer>(MIN_CLIQUES_ARG,
                        MIN_CLIQUES_ARG_DESC, false, 2, 2,
                        Integer.MAX_VALUE, Integer.class),
                new RangeArgumentDefinition<Integer>(MAX_CLIQUES_ARG,
                        MAX_CLIQUES_ARG_DESC, false, Integer.MAX_VALUE, 2,
                        Integer.MAX_VALUE, Integer.class),
                new BooleanArgumentDefinition(CLIQUES_REL_ARG,
                        CLIQUES_REL_ARG_DESC, false, true)};
    }

    @Override
    public String getName() {
        return "all cliques";
    }

    @Override
    public String getVersion() {
        return "alpha";
    }

    @Override
    public String getId() {
        return "allcliques clustering";
    }


    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws Exception {

        RelationType rt = graph.getMetaData().getRelationType(
                args.getUniqueValue(RELATION_TYPE_ARG).toString());
        ConceptClass cc = graph.getMetaData().getConceptClass(
                args.getUniqueValue(CONCEPCLASS_ARG).toString());

        Boolean createCliqueRelations = (Boolean) args
                .getUniqueValue(CLIQUES_REL_ARG);

        int minCliqueSize = (Integer) args.getUniqueValue(MIN_CLIQUES_ARG);
        int maxCliqueSize = (Integer) args.getUniqueValue(MAX_CLIQUES_ARG);

        Set<ONDEXRelation> rts = graph.getRelationsOfConceptClass(cc);

        Set<ONDEXRelation> relations = graph
                .getRelationsOfRelationType(rt);
        relations.retainAll(rts);

        ConceptClass clique_cc = graph.getMetaData().getConceptClass(
                CLIQUE_CC);
        if (clique_cc == null) {
            clique_cc = graph.getMetaData().createConceptClass(CLIQUE_CC,
                    CLIQUE_CC, "A complete subgraph of a undirected graph",
                    null);
        }

        AttributeName attribute_order = graph.getMetaData()
                .getAttributeName(ORDER_ATTRIBUTE);
        if (attribute_order == null) {
            attribute_order = graph.getMetaData()
                    .createAttributeName(ORDER_ATTRIBUTE, ORDER_ATTRIBUTE,
                            "Number of Concepts in subgraph", null,
                            Integer.class, null);
        }

        DataSource dataSource = graph.getMetaData().getDataSource(CLIQUE_CV);
        if (dataSource == null) {
            dataSource = graph.getMetaData().createDataSource(CLIQUE_CV, CLIQUE_CV,
                    "Created from Transformer: " + getName());
        }

        EvidenceType evidence = graph.getMetaData().getEvidenceType(
                EVIDENCE);
        if (evidence == null) {
            evidence = graph.getMetaData().getFactory()
                    .createEvidenceType(EVIDENCE);
        }

        RelationType rtIntersection = graph.getMetaData()
                .getRelationType(INTERSECTION);
        if (rtIntersection == null) {
            rtIntersection = graph.getMetaData().getFactory()
                    .createRelationType(INTERSECTION);
        }

        RelationType rtBridge = graph.getMetaData().getRelationType(
                BRIDGE);
        if (rtBridge == null) {
            rtBridge = graph.getMetaData().getFactory()
                    .createRelationType(BRIDGE);
        }

        List<EvidenceType> evidences = new ArrayList<EvidenceType>(1);
        evidences.add(evidence);

        List<Integer> nodeiList = new ArrayList<Integer>();
        List<Integer> nodejList = new ArrayList<Integer>();

        // guarentees unidirectionality
        Map<Integer, List<Integer>> relationIndex = new HashMap<Integer, List<Integer>>();

        for (ONDEXRelation relation : relations) {
            if (relation.getKey().getFromID() != relation.getKey().getToID()) {
                ONDEXConcept from = relation.getFromConcept();
                ONDEXConcept to = relation.getToConcept();
                if (from.getOfType().equals(cc) && to.getOfType().equals(cc)) {

                    int fromId = from.getId();
                    int toId = to.getId();

                    if (fromId > toId) {
                        fromId = to.getId();
                        toId = from.getId();
                    }

                    List<Integer> listTo = relationIndex.get(fromId);
                    if (listTo == null) {
                        listTo = new ArrayList<Integer>();
                        relationIndex.put(fromId, listTo);
                    }
                    // if it doesn't exist add it
                    if (!listTo.contains(toId)) {
                        listTo.add(toId);
                        nodeiList.add(fromId);
                        nodejList.add(toId);
                    }
                }
            }
        }

        HashSet<Integer> nodeIds = new HashSet<Integer>();
        nodeIds.addAll(nodeiList);
        nodeIds.addAll(nodejList);

        int n = nodeIds.size();
        int m = nodeiList.size();
        int[] nodei = toIntArray(nodeiList);
        int[] nodej = toIntArray(nodejList);

        relationIndex = null;
        nodeiList = null;
        nodejList = null;

        System.out.println("Searching for cliques in " + n + " nodes and " + m
                + " edges");
        List<int[]> cliques = AllCliques.allCliques(n, m, nodei, nodej,
                minCliqueSize, maxCliqueSize);
        System.out.println("Number of all-cliques = " + cliques.size());

        // System.out.println("Checking for internal cliques");
        // //check for internal cliques
        // Set<Integer> cliquesToRemove = new HashSet<Integer>();
        // int cliqueNum = 0;
        // for (int[] cliqueA: cliques) {
        // if (cliqueNum+1%10000==0)
        // System.out.println(cliqueNum+1+" of "+cliques.size());
        // for (int[] cliqueB: cliques) {
        // if (cliqueA != cliqueB && isSubsetOfArray(cliqueA, cliqueB)) {
        // cliquesToRemove.add(cliqueNum);
        // }
        // }
        // cliqueNum++;
        // }
        //
        // System.out.println("Removing "+cliquesToRemove.size()+" cliques");
        // for(int clique: cliquesToRemove) {
        // cliques.remove(clique);
        // }

        System.out.println("Number of parent-cliques = " + cliques.size());

        /*
           * int c = 0; System.out.println("Number of cliques = " +
           * cliques.size()); for (int[] clique: cliques) { c++;
           * System.out.print("\nnodes of clique "+c+": "); for (int i: clique)
           * System.out.printf("\t%3d", i); } System.out.println();
           */
        int countrelations = 0;

        HashMap<Integer, Integer> cliqueToConceptId = new HashMap<Integer, Integer>(
                cliques.size());
        HashMap<Integer, Set<Integer>> cliqueTocliqueIntersection = new HashMap<Integer, Set<Integer>>(
                cliques.size());
        HashMap<Integer, Set<Integer>> cliqueTocliqueBridge = new HashMap<Integer, Set<Integer>>(
                cliques.size());

        System.out
                .println("Creating clique nodes and indexing clique connectivity");

        for (int i = 0; i < cliques.size(); i++) {
            if (i + 1 % 10000 == 0)
                System.out.println(i + 1 + " of " + cliques.size());

            int nodesInClique = cliques.get(i).length;
            Set<Integer> cliqueNodes = new HashSet<Integer>();
            for (int id : cliques.get(i)) {
                cliqueNodes.add(id);
            }

            ONDEXConcept conceptContext = graph.createConcept("Clique",
                    "Clique of " + nodesInClique + " nodes", "", dataSource, clique_cc,
                    evidences);
            conceptContext.createAttribute(attribute_order, nodesInClique, false);
            cliqueToConceptId.put(i, conceptContext.getId());

            Set<RelationKey> relationAdded = new HashSet<RelationKey>();

            for (int cId : cliqueNodes) {
                ONDEXConcept cliqueConcept = graph.getConcept(cId);
                cliqueConcept.addTag(conceptContext);

                // add contexts for internal relations
                for (ONDEXRelation cRelation : graph.getRelationsOfConcept(cliqueConcept)) {
                    if (cRelation.getOfType().equals(rt)) {

                        Integer from = cRelation.getKey().getFromID();
                        Integer to = cRelation.getKey().getToID();

                        if (cliqueNodes.contains(from)
                                && cliqueNodes.contains(to)
                                && !relationAdded.contains(cRelation.getKey())) {
                            cRelation.addTag(conceptContext);
                            countrelations++;
                            relationAdded.add(cRelation.getKey());
                        } else {
                            ONDEXConcept targetConcept = null;
                            if (from.equals(cliqueConcept.getId())
                                    && !to.equals(cliqueConcept.getId())) {
                                targetConcept = cRelation.getToConcept();
                            } else if (to.equals(cliqueConcept.getId())
                                    && !from.equals(cliqueConcept.getId())) {
                                targetConcept = cRelation.getFromConcept();
                            }

                            if (targetConcept != null) {
                                if (createCliqueRelations) {
                                    // check bridging with other cliques
                                    for (int j = 0; j < cliques.size(); j++) {
                                        int[] comparedToClique = cliques.get(j);
                                        // check if the external relation
                                        // bridges to another clique
                                        if (Arrays.binarySearch(
                                                comparedToClique, targetConcept
                                                        .getId()) > -1) {
                                            int fromClique = i;
                                            int toClique = j;

                                            if (fromClique > toClique) {
                                                fromClique = j;
                                                toClique = i;
                                            }

                                            Set<Integer> bridges = cliqueTocliqueBridge
                                                    .get(fromClique);
                                            if (bridges == null) {
                                                bridges = new HashSet<Integer>();
                                                cliqueTocliqueBridge.put(
                                                        fromClique, bridges);
                                            }
                                            bridges.add(toClique);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (createCliqueRelations) {
                    // check intersection with other cliques
                    for (int j = 0; j < cliques.size(); j++) {
                        int[] comparedToClique = cliques.get(j);
                        if (Arrays.binarySearch(comparedToClique, cId) > -1) {

                            int fromClique = i;
                            int toClique = j;

                            if (fromClique > toClique) {
                                fromClique = j;
                                toClique = i;
                            }

                            Set<Integer> intersections = cliqueTocliqueIntersection
                                    .get(fromClique);
                            if (intersections == null) {
                                intersections = new HashSet<Integer>();
                                cliqueTocliqueIntersection.put(fromClique,
                                        intersections);
                            }
                            intersections.add(toClique);
                        }
                    }
                }
            }
        }
        if (createCliqueRelations) {
            System.out.println("Creating clique intersection relations");
            for (Integer clique : cliqueTocliqueIntersection.keySet()) {
                ONDEXConcept fromClique = graph.getConcept(cliqueToConceptId
                        .get(clique));
                for (Integer intersection : cliqueTocliqueIntersection
                        .get(clique)) {
                    ONDEXConcept intersectionClique = graph
                            .getConcept(cliqueToConceptId.get(intersection));
                    graph.getFactory().createRelation(fromClique,
                            intersectionClique, rtIntersection, evidence);
                }
            }

            System.out.println("Creating clique bridge relations");
            for (Integer clique : cliqueTocliqueBridge.keySet()) {
                ONDEXConcept fromClique = graph.getConcept(cliqueToConceptId
                        .get(clique));
                for (Integer bridge : cliqueTocliqueBridge.get(clique)) {
                    ONDEXConcept bridgeClique = graph
                            .getConcept(cliqueToConceptId.get(bridge));
                    graph.getFactory().createRelation(fromClique, bridgeClique,
                            rtBridge, evidence);
                }
            }
        }

        System.out.println(cliqueToConceptId.size() + " cliques "
                + countrelations + " contexts added");
    }

    /**
     * Creates a
     *
     * @param nodeList list of nodes to add
     * @return
     */
    private int[] toIntArray(List<Integer> nodeList) {
        int[] nodes = new int[nodeList.size() + 1];
        nodes[0] = 0;
        for (int i = 0; i < nodeList.size(); i++) {
            nodes[i + 1] = nodeList.get(i);
        }
        return nodes;
    }
/*
	private static boolean isSubsetOfArray(int[] a, int[] b) {
		if (a.length > b.length) {
			return false;
		}
		int bIndex = 0;
		for (int i = 0; i < a.length; i++) {
			while (a[i] != b[bIndex]) {
				if (bIndex == b.length - 1 || a[i] < b[bIndex]) {
					return false;
				}
				bIndex++;
			}
		}
		return true;
	}

	private static double fractionalIntersectionOfArrays(int[] a, int[] b) {
		double sharedNodes = 0.0d;
		int j = 0;
		for (int i = 0; i < a.length; i++) {
			for (; j < b.length; j++) {
				if (a[i] == b[j]) {
					sharedNodes++;
					break;
				} else if (a[i] < b[j])
					break;
			}
		}
		return sharedNodes / ((double) (Integer) a.length);
	}
	*/

}
