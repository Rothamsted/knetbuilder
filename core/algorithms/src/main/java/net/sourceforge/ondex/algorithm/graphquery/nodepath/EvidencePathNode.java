package net.sourceforge.ondex.algorithm.graphquery.nodepath;

import net.sourceforge.ondex.algorithm.graphquery.State;
import net.sourceforge.ondex.algorithm.graphquery.StateMachineComponent;
import net.sourceforge.ondex.algorithm.graphquery.Transition;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXRelation;

import java.util.*;

/**
 * @author hindlem
 * @param <EA> the current entity type for this EntityNode
 * @param <EB> the last entity type in the path
 * @param <SC> the state component that evidences this entity (EA)
 */
public abstract class EvidencePathNode<EA extends ONDEXEntity, EB extends ONDEXEntity, SC extends StateMachineComponent> extends PathNode<EA, EB> {

    private SC evidence;

    public <SB extends StateMachineComponent> EvidencePathNode(EA entity, SC evidence, EvidencePathNode<EB, EA, SB> previous) {
        super(entity, previous);
        this.evidence = evidence;
    }

    public SC getStateMachineComponent() {
        return evidence;
    }

    public List<StateMachineComponent> getEvidencesInPositionOrder() {
        List<StateMachineComponent> evidences = new ArrayList<StateMachineComponent>(length);
        collectEvidencesInPositionOrder(evidences);
        Collections.reverse(evidences);
        return evidences;
    }

    private void collectEvidencesInPositionOrder(List<StateMachineComponent> evidences) {
        evidences.add(evidence);
        if (getPrevious() != null) {
            getPrevious().collectEvidencesInPositionOrder(evidences);
        }
    }

    public boolean containsStateMachineComponent(StateMachineComponent component) {
        if (this.evidence.equals(component)) {
            return true;
        } else if (getPrevious() != null && getPrevious().containsStateMachineComponent(component)) {
            return true;
        }
        return false;
    }

    public boolean containsStateMachineComponents(Set<? extends StateMachineComponent> components) {
        if (components.contains(evidence)) {
            return true;
        } else if (getPrevious() != null && getPrevious().containsStateMachineComponents(components)) {
            return true;
        }
        return false;
    }

    public Set<ONDEXEntity> getEntities(StateMachineComponent component) {
        Set<ONDEXEntity> list = new HashSet<ONDEXEntity>();
        collectEntities(component, list);
        return list;
    }

    private void collectEntities(StateMachineComponent component, Set<ONDEXEntity> list) {
        if (evidence.equals(component))
            list.add(getEntity());
        EvidencePathNode previous = getPrevious();
        if (previous != null)
            previous.collectEntities(component, list);
    }

    public Set<ONDEXEntity> getEntities(Set<? extends StateMachineComponent> components) {
        Set<ONDEXEntity> list = new HashSet<ONDEXEntity>();
        collectEntities(components, list);
        return list;
    }

    private void collectEntities(Set<? extends StateMachineComponent> component, Set<ONDEXEntity> list) {
        if (component.contains(evidence))
            list.add(getEntity());
        EvidencePathNode previous = getPrevious();
        if (previous != null)
            previous.collectEntities(component, list);
    }

    public boolean equals(Object obj) {
        if (obj instanceof EvidencePathNode) {
            return super.equals(obj) && ((EvidencePathNode) obj).getEvidencesInPositionOrder().equals(getEvidencesInPositionOrder());
        }
        return false;
    }

    public String toString() {
        return super.toString();
    }

    @Override
    public EvidencePathNode getPrevious() {
        return (EvidencePathNode) super.getPrevious();
    }


    public static class FirstEvidenceConceptNode extends EvidencePathNode<ONDEXConcept, ONDEXRelation, State> {
        public FirstEvidenceConceptNode(ONDEXConcept entity, State evidence) {
            super(entity, evidence, null);
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

    public static class EvidenceConceptNode extends EvidencePathNode<ONDEXConcept, ONDEXRelation, State> {
        public EvidenceConceptNode(ONDEXConcept entity, State evidence, EvidencePathNode<ONDEXRelation, ONDEXConcept, Transition> previous) {
            super(entity, evidence, previous);
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

    public static class EvidenceRelationNode extends EvidencePathNode<ONDEXRelation, ONDEXConcept, Transition> {
        public EvidenceRelationNode(ONDEXRelation entity, Transition evidence, EvidencePathNode<ONDEXConcept, ONDEXRelation, State> previous) {
            super(entity, evidence, previous);
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
