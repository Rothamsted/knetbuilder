package net.sourceforge.ondex.algorithm.graphquery.nodepath;

import net.sourceforge.ondex.algorithm.pathmodel.Path;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXRelation;

import java.util.*;

/**
 * A two-color singly linked list.
 * <p/>
 * This is intended for capturing branching, tree-shaped path clusters without duplication.
 *
 * @author Matthew Pocock
 * @param <EA> the current entity type for this EntityNode
 * @param <EB> the last entity type in the path
 */
public abstract class PathNode<EA extends ONDEXEntity, EB extends ONDEXEntity> implements Path {
    private final EA entity;
    private final PathNode<EB, EA> previous;

    abstract protected List<ONDEXEntity> buildEntityList(int size);

    abstract protected List<ONDEXConcept> buildConceptList(int size);

    abstract protected List<ONDEXRelation> buildRelationList(int size);

    abstract protected void collectConcepts(Set<ONDEXConcept> concepts);

    abstract protected void collectRelations(Set<ONDEXRelation> relations);

    final protected int length;

    public PathNode(EA entity, PathNode<EB, EA> previous) {
        this.entity = entity;
        this.previous = previous;
        if (previous != null)
            this.length = previous.getLength() + 1;
        else
            this.length = 1;
    }

    public int getLength() {
        return length;
    }

    public EA getEntity() {
        return entity;
    }

    public PathNode<EB, EA> getPrevious() {
        return previous;
    }

    public boolean equals(Object obj) {
        if (obj instanceof PathNode) {
            return ((PathNode) obj).length == length
                    && ((PathNode) obj).getConceptsInPositionOrder().equals(this.getConceptsInPositionOrder())
                    && ((PathNode) obj).getRelationsInPositionOrder().equals(this.getRelationsInPositionOrder());
        }
        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[CONCEPTS] ");
        for (ONDEXConcept concept : getConceptsInPositionOrder()) {
            sb.append(concept.getId() + " ");
            sb.append(' ');
        }
        sb.append("[RELATIONS] ");
        for (ONDEXRelation relation : getRelationsInPositionOrder()) {
            sb.append(relation.getId());
            sb.append(' ');
        }
        return sb.toString();
    }

    public boolean containsEntity(ONDEXEntity entityToCheck) {
        if (this.entity.equals(entityToCheck)) {
            return true;
        } else if (previous != null && previous.containsEntity(entityToCheck)) {
            return true;
        }
        return false;
    }

    public boolean containsEntityId(int id, Class<? extends ONDEXEntity> entityType) {
        if (entityType.isInstance(entity) && entity.getId() == id) {
            return true;
        } else if (previous != null && previous.containsEntityId(id, entityType)) {
            return true;
        }
        return false;
    }

    @Override
    public ONDEXEntity getEntityAtPosition(int i) {
        return buildEntityList((length + 1) / 2).get(i);
    }

    @Override
    public Set<ONDEXConcept> getAllConcepts() {
        Set<ONDEXConcept> concepts = new HashSet<ONDEXConcept>((length + 1) / 2);
        collectConcepts(concepts);
        return concepts;
    }

    @Override
    public Set<ONDEXRelation> getAllRelations() {
        Set<ONDEXRelation> relations = new HashSet<ONDEXRelation>((length + 1) / 2);
        collectRelations(relations);
        return relations;
    }

    @Override
    public List<ONDEXConcept> getConceptsInPositionOrder() {
        return buildConceptList((length + 1) / 2);
    }

    @Override
    public List<ONDEXRelation> getRelationsInPositionOrder() {
        return buildRelationList((length + 1) / 2);
    }

    @Override
    public Iterator<ONDEXEntity> iterator() {
        return buildEntityList(length).iterator();
    }

    public static class FirstConceptNode extends PathNode<ONDEXConcept, ONDEXRelation> {
        public FirstConceptNode(ONDEXConcept entity) {
            super(entity, null);
        }

        @Override
        public int getConceptLength() {
            return 1;
        }

        @Override
        public int getRelationLength() {
            return 0;
        }

        @Override
        public int getLength() {
            return 1;
        }

        @Override
        public void collectConcepts(Set<ONDEXConcept> concepts) {
            concepts.add(getEntity());
        }

        @Override
        public void collectRelations(Set<ONDEXRelation> relations) {
            // nothing to do
        }

        @Override
        public ONDEXEntity getStartingEntity() {
            return getEntity();
        }

        @Override
        protected List<ONDEXEntity> buildEntityList(int size) {
            List<ONDEXEntity> el = new ArrayList<ONDEXEntity>(size);
            el.add(getEntity());
            return el;
        }

        @Override
        protected List<ONDEXConcept> buildConceptList(int size) {
            List<ONDEXConcept> cl = new ArrayList<ONDEXConcept>(size);
            cl.add(getEntity());
            return cl;
        }

        @Override
        protected List<ONDEXRelation> buildRelationList(int size) {
            return new ArrayList<ONDEXRelation>(size);
        }
    }

    public static class ConceptNode extends PathNode<ONDEXConcept, ONDEXRelation> {
        public ConceptNode(ONDEXConcept entity, PathNode<ONDEXRelation, ONDEXConcept> previous) {
            super(entity, previous);
        }

        @Override
        public int getConceptLength() {
            return 1 + getPrevious().getConceptLength();
        }

        @Override
        public int getRelationLength() {
            return getPrevious().getConceptLength();
        }

        @Override
        public int getLength() {
            return 1 + getPrevious().getLength();
        }

        @Override
        public void collectConcepts(Set<ONDEXConcept> concepts) {
            concepts.add(getEntity());
            getPrevious().collectConcepts(concepts);
        }

        @Override
        public void collectRelations(Set<ONDEXRelation> relations) {
            getPrevious().collectRelations(relations);
        }

        @Override
        public ONDEXEntity getStartingEntity() {
            return getPrevious().getStartingEntity();
        }

        @Override
        protected List<ONDEXEntity> buildEntityList(int size) {
            List<ONDEXEntity> el = getPrevious().buildEntityList(size);
            el.add(getEntity());
            return el;
        }

        @Override
        protected List<ONDEXConcept> buildConceptList(int size) {
            List<ONDEXConcept> cl = getPrevious().buildConceptList(size);
            cl.add(getEntity());
            return cl;
        }

        @Override
        protected List<ONDEXRelation> buildRelationList(int size) {
            return getPrevious().buildRelationList(size);
        }
    }

    public static class RelationNode extends PathNode<ONDEXRelation, ONDEXConcept> {
        public RelationNode(ONDEXRelation entity, PathNode<ONDEXConcept, ONDEXRelation> previous) {
            super(entity, previous);
        }

        @Override
        public int getConceptLength() {
            return getPrevious().getConceptLength();
        }

        @Override
        public int getRelationLength() {
            return 1 + getPrevious().getRelationLength();
        }

        @Override
        public int getLength() {
            return 1 + getPrevious().getLength();
        }

        @Override
        public void collectConcepts(Set<ONDEXConcept> concepts) {
            getPrevious().collectConcepts(concepts);
        }

        @Override
        public void collectRelations(Set<ONDEXRelation> relations) {
            relations.add(getEntity());
            getPrevious().collectRelations(relations);
        }

        @Override
        public ONDEXEntity getStartingEntity() {
            return getPrevious().getStartingEntity();
        }

        @Override
        protected List<ONDEXEntity> buildEntityList(int size) {
            List<ONDEXEntity> el = getPrevious().buildEntityList(size);
            el.add(getEntity());
            return el;
        }

        @Override
        protected List<ONDEXConcept> buildConceptList(int size) {
            return getPrevious().buildConceptList(size);
        }

        @Override
        protected List<ONDEXRelation> buildRelationList(int size) {
            List<ONDEXRelation> rl = getPrevious().buildRelationList(size);
            rl.add(getEntity());
            return rl;
        }
    }
}
