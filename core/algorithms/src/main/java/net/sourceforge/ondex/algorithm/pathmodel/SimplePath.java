package net.sourceforge.ondex.algorithm.pathmodel;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXRelation;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

/**
 * @author hindlem
 */
@XmlRootElement(name = "Path")
public abstract class SimplePath implements Path {

    @XmlElement(name = "ONDEXEntity", required = true)
    protected ArrayList<ONDEXEntity> entitiesInPositionOrder = new ArrayList<ONDEXEntity>(50);

    protected SimplePath(List<ONDEXEntity> entitiesInPositionOrder) {
        this.entitiesInPositionOrder = new ArrayList(entitiesInPositionOrder);
    }

    /**
     * @param c the concept which is the starting point of the path
     */
    protected SimplePath(ONDEXConcept c) {
        entitiesInPositionOrder.add(c);
    }

    /**
     * @param r the relation which is the starting point of the path
     */
    protected SimplePath(ONDEXRelation r) {
        entitiesInPositionOrder.add(r);
    }

    /**
     * @param r
     * @param c
     */
    protected void addPathStep(ONDEXRelation r, ONDEXConcept c) {
        entitiesInPositionOrder.add(r);
        entitiesInPositionOrder.add(c);
    }

    /**
     * @param r
     * @param c
     */
    protected void replacePathStep(ONDEXRelation r, ONDEXConcept c) {
        entitiesInPositionOrder.set(entitiesInPositionOrder.size() - 2, r);
        entitiesInPositionOrder.set(entitiesInPositionOrder.size() - 1, c);
    }

    /**
     * @param r
     */
    protected void addPathStep(ONDEXRelation r) {
        entitiesInPositionOrder.add(r);
    }

    /**
     * @param c
     */
    protected void addPathStep(ONDEXConcept c) {
        entitiesInPositionOrder.add(c);
    }


    public Object clone() throws CloneNotSupportedException {
        SimplePath cloned = (SimplePath) super.clone();
        cloned.entitiesInPositionOrder = (ArrayList) entitiesInPositionOrder.clone();
        return cloned;
    }

    @Override
    public int getLength() {
        return entitiesInPositionOrder.size();
    }

    @Override
    public ONDEXEntity getEntityAtPosition(int i) {
        return entitiesInPositionOrder.get(i);
    }

    @Override
    public Set<ONDEXConcept> getAllConcepts() {
        Set concepts = new HashSet<ONDEXConcept>(entitiesInPositionOrder.size() / 2 + 1);
        for (ONDEXEntity entity : entitiesInPositionOrder) {
            if (entity instanceof ONDEXConcept)
                concepts.add(entity);
        }

        return concepts;
    }

    @Override
    public Set<ONDEXRelation> getAllRelations() {
        Set relations = new HashSet<ONDEXConcept>(entitiesInPositionOrder.size() / 2 + 1);
        for (ONDEXEntity entity : entitiesInPositionOrder) {
            if (entity instanceof ONDEXRelation)
                relations.add(entity);
        }
        return relations;
    }

    @Override
    public ONDEXEntity getStartingEntity() {
        if (entitiesInPositionOrder.size() > 0) return entitiesInPositionOrder.get(0);
        return null;
    }

    public ONDEXConcept getLastConcept() {
        for (int i = entitiesInPositionOrder.size() - 1; i > -1; i--)
            if (entitiesInPositionOrder.get(i) instanceof ONDEXConcept)
                return (ONDEXConcept) entitiesInPositionOrder.get(i);
        throw new IndexOutOfBoundsException("This is an empty path, there is no last concept");
    }

    public ONDEXRelation getLastRelation() {
        for (int i = entitiesInPositionOrder.size() - 1; i > -1; i--)
            if (entitiesInPositionOrder.get(i) instanceof ONDEXRelation)
                return (ONDEXRelation) entitiesInPositionOrder.get(i);
        throw new IndexOutOfBoundsException("This is an empty path, there is no last relation");
    }

    public boolean containsRelationId(int relationId) {
        for (ONDEXEntity entity : entitiesInPositionOrder)
            if (entity instanceof ONDEXRelation && entity.getId() == relationId)
                return true;
        return false;
    }

    public boolean containsConceptId(int conceptId) {
        for (ONDEXEntity entity : entitiesInPositionOrder)
            if (entity instanceof ONDEXConcept && entity.getId() == conceptId)
                return true;
        return false;
    }

    public boolean containsEntity(ONDEXEntity entity) {
        return entitiesInPositionOrder.contains(entity);
    }

    public int getConceptLength() {
        if (entitiesInPositionOrder.size() == 1)
            return 1;
        return ((entitiesInPositionOrder.size() - 1) / 2) + 1;
    }

    public int getRelationLength() {
        if (entitiesInPositionOrder.size() == 1)
            return 0;
        return (entitiesInPositionOrder.size() - 1) / 2;
    }

    public List<ONDEXConcept> getConceptsInPositionOrder() {
        List<ONDEXConcept> concepts = new ArrayList<ONDEXConcept>(getConceptLength());
        for (ONDEXEntity entity : entitiesInPositionOrder) {
            if (entity instanceof ONDEXConcept)
                concepts.add((ONDEXConcept) entity);
        }
        return concepts;
    }

    @Override
    public List<ONDEXRelation> getRelationsInPositionOrder() {
        List<ONDEXRelation> relations = new ArrayList<ONDEXRelation>(getRelationLength());
        for (ONDEXEntity entity : entitiesInPositionOrder) {
            if (entity instanceof ONDEXRelation)
                relations.add((ONDEXRelation) entity);
        }
        return relations;
    }

    public boolean equals(Object o) {
        if (o instanceof StateMachineDerivedPath) {
            return ((StateMachineDerivedPath) o).entitiesInPositionOrder.equals(entitiesInPositionOrder);
        }
        return false;
    }

    @Override
    public Iterator<ONDEXEntity> iterator() {
        return entitiesInPositionOrder.iterator();
    }

    public SerializablePath toSerializablePath() {
        return new SerializablePath(this);
    }

}
