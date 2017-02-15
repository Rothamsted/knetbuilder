package net.sourceforge.ondex.tools.dagreachablity;

import net.sourceforge.ondex.algorithm.graphquery.*;
import net.sourceforge.ondex.algorithm.graphquery.DirectedEdgeTransition.EdgeTreatment;
import net.sourceforge.ondex.algorithm.graphquery.exceptions.StateMachineInvalidException;
import net.sourceforge.ondex.algorithm.graphquery.nodepath.EvidencePathNode;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.util.BitSetFunctions;

import java.util.*;

/**
 * An analysis class for extracting properties of node pairs in a directed acyclic graph (e.g. GO)
 *
 * @author hindlem
 */
public class ReachabiltyDag {


    private ONDEXGraph graph;
    private ConceptClass c;
    private Set<RelationType> rs;
    private boolean directionIsToRoot;

    private ONDEXConcept[][] pathMatrix; //defines paths to the route/s of this DAG
    private int[][] reachabilityMatrix; //defines distance in relations between nodes
    private int[][][] shortestPathRefMatrix; //indexes the paths that are the shortest

    private Set<Integer> roots = new HashSet<Integer>(); //the root nodes of this DAG
    private Set<Integer> leafs = new HashSet<Integer>(); //the leafs nodes of this DAG


    /**
     * Constructs indecies of DAG for later queries
     *
     * @param graph             the graph where the DAG subgraph resides
     * @param c                 the conceptclass that defines members of the DAG subgraph (only one type at the moment: expanded on request)
     * @param rs                the relation types of the members of the DAG subgraph
     * @param directionIsToRoot do the relations point to the root? else they must all point away from the route (can be made relationtype specific on request)
     */
    public ReachabiltyDag(ONDEXGraph graph,
                          ConceptClass c,
                          Set<RelationType> rs,
                          boolean directionIsToRoot) {
        this.graph = graph;
        this.c = c;
        this.rs = rs;
        this.directionIsToRoot = directionIsToRoot;
        initialize();
    }

    /**
     * Constructs indecies of DAG for later queries
     */
    private void initialize() {
        computePathToRootMatrix(c, rs, directionIsToRoot);
        computePartialReachabilityMatrix(pathMatrix);
    }

    /**
     * Computes the shortest path between two elements of the DAG, will return 0 if the from and to id is the same, or -1 if there is no path between the elements (i.e. it't not a DAG);
     *
     * @param fromId the from concept ID
     * @param toId   the to concept ID
     * @return the distance between the two concepts in relations
     */
    public int getDistance(int fromId, int toId) {

        int distance = reachabilityMatrix[fromId][toId];

        if (fromId == toId) {
            return 0;
        }

        if (distance == -1) {
            int lowestValue = Integer.MAX_VALUE;

            //compute the indirect shortest path from paths to route
            for (int[] row : reachabilityMatrix) {
                if (row[fromId] == -1 || row[toId] == -1) {
                    continue;
                }

                int rowDistance = row[fromId] + row[toId];
                if (rowDistance < lowestValue) {
                    lowestValue = rowDistance;
                }

                if (lowestValue == 1) {
                    return lowestValue; //can't get lower than this
                }
            }
            if (lowestValue == Integer.MAX_VALUE) {
                //unreachable --not a DAG!
                reachabilityMatrix[fromId][toId] = -2;
                return -1;
            }
            reachabilityMatrix[fromId][toId] = lowestValue;
            distance = lowestValue;
        }
        return distance;
    }

    /**
     * Computes the closest common ancestor of the two elements in the DAG. NB. may return one of the concept id's if it a ancestor of the other.
     *
     * @param fromId the from conceptID
     * @param toId   the too conceptID
     * @return the closest common ancestor
     */
    public CommonAncestor[] getClosestCommonAncestors(ONDEXConcept fromId, ONDEXConcept toId) {

        int distance = reachabilityMatrix[fromId.getId()][toId.getId()];

        if (fromId == toId) {

            ONDEXConcept[][] shortestPath = getShortestPathsToRoot(fromId);

            ONDEXConcept[][] shortestPathBetween = new ONDEXConcept[1][];
            shortestPathBetween[0] = new ONDEXConcept[]{fromId};
            return new CommonAncestor[]{new CommonAncestor(fromId,
                    fromId,
                    fromId,
                    shortestPathBetween,
                    shortestPath)};
        }
        if (distance == -2) { //computed before
            return new CommonAncestor[0]; //nodes unreachable
        } else if (distance == -1) { //no distance computed yet may be an indirect relationship requiring back traversing the GO tree

            Map<Coordinate, Coordinate> pathPair = new HashMap<Coordinate, Coordinate>();
            int lowestValue = Integer.MAX_VALUE;

            //compute the indirect shortest path from paths to route
            for (int i = 0; i < reachabilityMatrix.length; i++) {
                int[] row = reachabilityMatrix[i];
                if (row[fromId.getId()] == -1 || row[toId.getId()] == -1) {
                    continue;
                }

                int rowDistance = row[fromId.getId()] + row[toId.getId()];
                if (rowDistance < lowestValue) {
                    pathPair.clear(); //these are longer distance pairs
                    lowestValue = rowDistance;
                    pathPair.put(new Coordinate(i, fromId.getId()), new Coordinate(i, toId.getId()));
                } else if (rowDistance == lowestValue) {
                    pathPair.put(new Coordinate(i, fromId.getId()), new Coordinate(i, toId.getId()));
                }
            }
            if (lowestValue == Integer.MAX_VALUE) {
                //unreachable --not a DAG!
                reachabilityMatrix[fromId.getId()][toId.getId()] = -2;
            }
            reachabilityMatrix[fromId.getId()][toId.getId()] = lowestValue;
            distance = lowestValue;


        } else {
            int[] shortestPaths = shortestPathRefMatrix[fromId.getId()][toId.getId()];

            Map<ONDEXConcept, ONDEXConcept[][]> map = new HashMap<ONDEXConcept, ONDEXConcept[][]>();

            for (int shortestPath : shortestPaths) {
                ONDEXConcept[] path = pathMatrix[shortestPath];

                int fromIndex = -1;
                int toIndex = -1;

                for (int i = 0; i < path.length; i++) {
                    if (path[i].equals(fromId)) {
                        fromIndex = i;
                    } else if (path[i].equals(toId)) {
                        toIndex = i;
                    }
                }

                if (fromIndex == -1 || toIndex == -1) {
                    throw new RuntimeException(
                            "data inconsistancy in shortestPathRefMatrix the from and to concepts where not found in this path");
                }

                int lowestConceptIndex = fromIndex > toIndex ? toIndex : fromIndex;
                int highestConceptIndex = fromIndex < toIndex ? toIndex : fromIndex;

                ONDEXConcept[][] trimedShortestPaths = map.get(path[highestConceptIndex]);

                ONDEXConcept[] pathBetweenNodes = Arrays.copyOfRange(path, lowestConceptIndex, highestConceptIndex + 1);


                if (trimedShortestPaths != null)
                    trimedShortestPaths = Arrays.copyOf(trimedShortestPaths, trimedShortestPaths.length + 1);
                else
                    trimedShortestPaths = new ONDEXConcept[1][];

                trimedShortestPaths[trimedShortestPaths.length - 1] = pathBetweenNodes;

                map.put(path[highestConceptIndex], trimedShortestPaths);
            }

            CommonAncestor[] ancestors = new CommonAncestor[map.size()];
            int position = 0;
            for (ONDEXConcept cidAncestor : map.keySet()) {
                ONDEXConcept[][] paths = map.get(cidAncestor);

                CommonAncestor ancestor = new CommonAncestor(
                        fromId,
                        toId,
                        cidAncestor,
                        paths,
                        getShortestPathsToRoot(cidAncestor));

                ancestors[position] = ancestor;
                position++;
            }
            return ancestors;
        }
        return null;
    }

    /**
     * @param fromId
     * @return the shortest paths to the root (all the same length): an array of concepts including the start concept and the root
     */
    public ONDEXConcept[][] getShortestPathsToRoot(ONDEXConcept fromId) {

        int currentShortest = Integer.MAX_VALUE;
        ONDEXConcept[][] shortestPaths = new ONDEXConcept[0][];

        for (ONDEXConcept[] path : pathMatrix) {
            for (int i = 0; i < path.length; i++) {
                ONDEXConcept cid = path[i];
                if (cid.equals(fromId)) {

                    int distanceToRoot = (path.length - i);
                    if (currentShortest > distanceToRoot) {
                        ONDEXConcept[] shortestPath = Arrays.copyOfRange(path, i, path.length);
                        currentShortest = shortestPath.length;
                        shortestPaths = new ONDEXConcept[1][];
                        shortestPaths[0] = shortestPath;

                    } else if (currentShortest == distanceToRoot) {
                        ONDEXConcept[][] newShortestPaths = Arrays.copyOf(shortestPaths, shortestPaths.length + 1);
                        newShortestPaths[newShortestPaths.length] = Arrays.copyOfRange(path, i, path.length);
                    }
                }

            }
        }
        return shortestPaths;
    }


    /**
     * @return
     */
    private void computePathToRootMatrix(ConceptClass c, Set<RelationType> rs, boolean directionIsToRoot) {

        findAllConnectedRootsAndLeafs(c, rs, directionIsToRoot);

        int[] rootsInt = new int[roots.size()];
        Iterator<Integer> it = roots.iterator();
        for (int i = 0; i < rootsInt.length; i++) {
            rootsInt[i] = it.next();
        }

        Set<ONDEXConcept[]> routesAsConceptIds = new HashSet<ONDEXConcept[]>();

        StateMachine sm = new StateMachine();
        try {
            State goTermState = new State(c);
            sm.setStartingState(goTermState);

            ConceptLinkedState finalState = new ConceptLinkedState(c, rootsInt);
            sm.addFinalState(finalState);

            for (RelationType r : rs) {
                Transition t = new DirectedEdgeTransition(r, Integer.MAX_VALUE, EdgeTreatment.FORWARD);
                sm.addStep(goTermState, t, goTermState);
                Transition t2 = new DirectedEdgeTransition(r, Integer.MAX_VALUE, EdgeTreatment.FORWARD);
                sm.addStep(goTermState, t2, finalState);
            }
        } catch (StateMachineInvalidException e) {
            e.printStackTrace();
        }

        for (int cid : leafs) {
            System.out.println("leaf" + cid);
            ONDEXConcept goTermConcept = graph.getConcept(Integer.valueOf(cid));

            GraphTraverser gt = new GraphTraverser(sm, Integer.MAX_VALUE);
            List<EvidencePathNode> paths = gt.traverseGraph(graph, goTermConcept, null);
            System.out.println(paths.size() + " paths found");
            for (EvidencePathNode path : paths) {
                List<ONDEXConcept> conceptPath = path.getConceptsInPositionOrder();
                routesAsConceptIds.add(conceptPath.toArray(new ONDEXConcept[conceptPath.size()]));
                System.out.println("added " + path.getConceptsInPositionOrder().size() + " length path " + path);
            }
        }

        System.out.println("Paths to root found=" + routesAsConceptIds.size());
        pathMatrix = routesAsConceptIds.toArray(new ONDEXConcept[routesAsConceptIds.size()][]);
    }

    /**
     * @param c
     * @param rs
     * @param directionIsToRoot
     */
    private void findAllConnectedRootsAndLeafs(ConceptClass c, Set<RelationType> rs,
                                               boolean directionIsToRoot) {

        roots = new HashSet<Integer>();
        leafs = new HashSet<Integer>();

        Set<ONDEXRelation> possibleInternalRelations = getRelationsOfTypes(rs);

        for (ONDEXConcept possibleConcept : graph.getConceptsOfConceptClass(c)) {
            Set<ONDEXRelation> relations = graph.getRelationsOfConcept(possibleConcept);


            boolean isRoot = true;
            boolean isLeaf = true;

            for (ONDEXRelation relation : BitSetFunctions.and(possibleInternalRelations, relations)) {
                if (relation.getKey().getFromID() == relation.getKey().getToID()) {
                    continue;
                    //ignore this it's meaningless and not a DAG btw
                }

                if (directionIsToRoot && relation.getKey().getFromID() == possibleConcept.getId()) {
                    //break for outgoing (not root)
                    isRoot = false;
                } else if (!directionIsToRoot && relation.getKey().getToID() == possibleConcept.getId()) {
                    //break for incoming (not root)
                    isRoot = false;
                } else if (directionIsToRoot && relation.getKey().getToID() == possibleConcept.getId()) {
                    //break for outgoing (not root)
                    isLeaf = false;
                } else if (!directionIsToRoot && relation.getKey().getFromID() == possibleConcept.getId()) {
                    //break for incoming (not root)
                    isLeaf = false;
                }

                if (!isLeaf && !isRoot) {
                    break;
                }
            }

            if (isRoot && isLeaf)
                throw new RuntimeException("Logic error node can not be leaf and root");

            if (isRoot) {
                roots.add(possibleConcept.getId());
            } else if (isLeaf) {
                leafs.add(possibleConcept.getId());
            }

        }
    }

    /**
     * returns a partialy computed distance reachability matrix based on a path matrix
     *
     * @param pathMatrix
     * @return reachability matrix
     */
    private void computePartialReachabilityMatrix(ONDEXConcept[][] pathMatrix) {
        int largestId = 0;
        for (ONDEXConcept[] path : pathMatrix) {
            for (ONDEXConcept concept : path) {
                if (concept.getId() > largestId) {
                    largestId = concept.getId();
                }
            }
        }

        reachabilityMatrix = new int[largestId + 1][largestId + 1];
        shortestPathRefMatrix = new int[largestId + 1][largestId + 1][0];
        for (int[] row : reachabilityMatrix)
            Arrays.fill(row, -1);


        for (int m = 0; m < pathMatrix.length; m++) {
            //for each path pathMatrix[m]

            for (int i = 0; i < pathMatrix[m].length; i++) { //all against
                for (int j = 0; j < pathMatrix[m].length; j++) { //all
                    //add distance as reachability

                    int distance = Math.abs(j - i);

                    int fromId = pathMatrix[m][i].getId();
                    int toId = pathMatrix[m][j].getId();

                    int oldDistance = reachabilityMatrix[fromId][toId];

                    if (oldDistance == -1 || oldDistance > distance) {
                        reachabilityMatrix[fromId][toId] = distance;
                        if (oldDistance == distance) {
                            int[] oldArray = shortestPathRefMatrix[fromId][toId];
                            int[] newArray = Arrays.copyOf(oldArray, oldArray.length + 1);
                            newArray[newArray.length - 1] = m;
                            shortestPathRefMatrix[fromId][toId] = newArray;
                        } else {
                            shortestPathRefMatrix[fromId][toId] = new int[]{m}; //the path with the shortest direct route
                        }
                    }
                }
            }
        }
    }

    /**
     * @param annotationRelationTypes
     * @return
     */
    private Set<ONDEXRelation> getRelationsOfTypes(
            Set<RelationType> annotationRelationTypes) {
        Set<ONDEXRelation> annotationRelations = null;
        for (RelationType annotationRelationType : annotationRelationTypes) {
            Set<ONDEXRelation> relations = graph.getRelationsOfRelationType(annotationRelationType);
            if (annotationRelations == null) {
                annotationRelations = relations;
            } else {
                BitSetFunctions.or(annotationRelations, relations);
            }
        }
        return annotationRelations;
    }

    public static void main(String[] args) {
        int[] arr = new int[]{1, 2, 3, 4, 5, 6, 7};

        int fromIndex = 2;
        int toIndex = 4;

        int lowestConcept = fromIndex > toIndex ? toIndex : fromIndex;
        int highestConcept = fromIndex < toIndex ? toIndex : fromIndex;

        int[] pathBetweenNodes = Arrays.copyOfRange(arr, lowestConcept, highestConcept + 1);

        for (int path : pathBetweenNodes) {
            System.out.println("path " + path);
        }
    }

    public Set<Integer> getRoots() {
        return roots;
    }

    public Set<Integer> getLeafs() {
        return leafs;
    }

    /**
     * Simple 2d co-ordinate class
     *
     * @author hindlem
     */
    protected class Coordinate {

        private final int from;
        private final int to;

        /**
         * @param from
         * @param to
         */
        public Coordinate(int from, int to) {
            this.from = from;
            this.to = to;
        }

        public int getFrom() {
            return from;
        }

        public int getTo() {
            return to;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Coordinate) {
                if (((Coordinate) o).from == from) {
                    if (((Coordinate) o).to == to) {
                        return true;
                    }
                }
            }
            return false;
        }

    }

}
