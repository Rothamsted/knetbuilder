package net.sourceforge.ondex.tools.dagreachablity;

import net.sourceforge.ondex.core.ONDEXConcept;

/**
 * Object for describing the common ancestor and its relation to the 2 query nodes
 *
 * @author hindlem
 */
public class CommonAncestor {

    private final ONDEXConcept fromId;
    private final ONDEXConcept toId;
    private final ONDEXConcept commonAncestorId;

    private final ONDEXConcept[][] conceptPath;
    private final ONDEXConcept[][] ancestorToRootShortestPaths;

    /**
     * @param fromId                      the from concept id
     * @param toId                        the to concept id
     * @param commonAncestorId            the common ancestor concept id
     * @param conceptPath                 the path from the from concept to the to concept
     * @param ancestorToRootShortestPaths the path from the ancestor to the root
     */
    public CommonAncestor(ONDEXConcept fromId,
                          ONDEXConcept toId,
                          ONDEXConcept commonAncestorId,
                          ONDEXConcept[][] conceptPath,
                          ONDEXConcept[][] ancestorToRootShortestPaths) {
        this.fromId = fromId;
        this.toId = toId;
        this.commonAncestorId = commonAncestorId;
        this.conceptPath = conceptPath;
        this.ancestorToRootShortestPaths = ancestorToRootShortestPaths;
    }

    /**
     * @return
     */
    public ONDEXConcept getFrom() {
        return fromId;
    }

    /**
     * @return
     */
    public ONDEXConcept getTo() {
        return toId;
    }

    /**
     * @return
     */
    public ONDEXConcept getCommonAncestor() {
        return commonAncestorId;
    }

    /**
     * @return the shortest paths between the ancestor and the nearest root/s
     */
    public ONDEXConcept[][] getConceptPathsFromAncestorToRoot() {
        return ancestorToRootShortestPaths;
    }

    /**
     * @return The path between the two query nodes
     */
    public ONDEXConcept[][] getConceptPathsBetweenFromAndTo() {
		return conceptPath;
	}

}
