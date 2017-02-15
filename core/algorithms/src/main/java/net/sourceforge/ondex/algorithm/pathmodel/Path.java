package net.sourceforge.ondex.algorithm.pathmodel;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXRelation;

import java.util.List;
import java.util.Set;

/**
 * An interface for defining routes through a graph, useful for many algorithms
 *
 * @author hindlem
 */
public interface Path extends Iterable<ONDEXEntity>, Cloneable {

    /**
     * Get the number of ONDEXEntitys in the path
     *
     * @return ONDEXEntitys in the path
     */
    public int getLength();

    /**
     * Get the number of ONDEXConcepts in the path
     *
     * @return ONDEXConcepts in the path
     */
    public int getConceptLength();

    /**
     * Get the number of ONDEXRelations in the path
     *
     * @return ONDEXRelations in the path
     */
    public int getRelationLength();

    /**
     * returns the concept at the specified position in the route (e.g. starting concept = 0, outgoing relation = 0, target concept = 1)
     *
     * @param i the position in the route (starting from 0)
     * @return the id for the concept
     */
    public ONDEXEntity getEntityAtPosition(int i);

    /**
     * @return all the concepts that are traversed in the route
     */
    public Set<ONDEXConcept> getAllConcepts();

    /**
     * @return all the concepts that are traversed in the route
     */
    public Set<ONDEXRelation> getAllRelations();


    /**
     * @return all concepts in an ordered list with the start element being the first concept in the path
     */
    public List<ONDEXConcept> getConceptsInPositionOrder();

    /**
     * @return all relations in an ordered list with the start element being the first relation in the path
     */
    public List<ONDEXRelation> getRelationsInPositionOrder();

    /**
     * @return the ONDEXEntity that is the start of this path
     */
    public ONDEXEntity getStartingEntity();
}
