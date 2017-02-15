package net.sourceforge.ondex.algorithm.pathmodel;

import net.sourceforge.ondex.algorithm.graphquery.State;
import net.sourceforge.ondex.algorithm.graphquery.StateMachineComponent;
import net.sourceforge.ondex.algorithm.graphquery.Transition;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXRelation;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * A more defined and enriched Route model than SimpleRoute
 * Defines a non cyclical route through a graph where the start and the end are both concepts
 *
 * @author hindlem
 */
@XmlRootElement(name = "StateMachineDerivedPath")
public class StateMachineDerivedPath extends SimplePath implements StatePath {

    /**
     *
     */
    @XmlElement(name = "StateMachineComponents", required = true)
    protected ArrayList<StateMachineComponent<?, ?>> smcInPositionOrder = new ArrayList<StateMachineComponent<?, ?>>(50);

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ONDEXEntity entity : entitiesInPositionOrder) {
            sb.append(entity.getId() + "(" + entity.getClass().getSimpleName() + ")");
        }

        return sb.toString() + smcInPositionOrder.toString();
    }

    /**
     * @param c
     * @param state
     */
    public StateMachineDerivedPath(ONDEXConcept c, State state) {
        super(c);
        smcInPositionOrder.add(state);
    }

    /**
     * @return
     * @throws CloneNotSupportedException
     */
    public Object clone() throws CloneNotSupportedException {
        StateMachineDerivedPath cloned = (StateMachineDerivedPath) super.clone();
        cloned.smcInPositionOrder = (ArrayList) smcInPositionOrder.clone();
        return cloned;
    }

    public void addPathStep(ONDEXRelation r, Transition transition, ONDEXConcept c, State state) {
        super.addPathStep(r, c);
        smcInPositionOrder.add(transition);
        smcInPositionOrder.add(state);
    }

    public void replacePathStep(ONDEXRelation r, Transition transition, ONDEXConcept c, State state) {
        super.replacePathStep(r, c);
        smcInPositionOrder.set(smcInPositionOrder.size() - 2, transition);
        smcInPositionOrder.set(smcInPositionOrder.size() - 1, state);
    }

    public State getLastState() {
        for (int i = smcInPositionOrder.size() - 1; i > -1; i--)
            if (smcInPositionOrder.get(i) instanceof State)
                return (State) smcInPositionOrder.get(i);
        throw new IndexOutOfBoundsException("This is an empty path, there is no last state");
    }

    public Transition getLastTransition() {
        for (int i = smcInPositionOrder.size() - 1; i > -1; i--)
            if (smcInPositionOrder.get(i) instanceof Transition)
                return (Transition) smcInPositionOrder.get(i);
        throw new IndexOutOfBoundsException("This is an empty path, there is no last state");
    }

    /**
     * @param component
     * @param <C>
     * @return
     */
    public <C extends StateMachineComponent<?, ?>> int getComponentCountInPath(C component) {
        int count = 0;
        for (StateMachineComponent<?, ?> componentInPath : smcInPositionOrder) {
            if (componentInPath.equals(component))
                count++;
        }
        return count;  //To change body of created methods use File | Settings | File Templates.
    }

    /**
     * @param components
     * @param <C>
     * @return
     */
    public <C extends StateMachineComponent<?, ?>> boolean containsEntitiesForAllComponents(Set<C> components) {
        return smcInPositionOrder.containsAll(components);
    }

    /**
     * @param components
     * @param <C>
     * @return
     */
    public <C extends StateMachineComponent<?, ?>> boolean containsEntitiesForSomeComponents(Set<C> components) {
        for (StateMachineComponent<?, ?> component : components) {
            if (smcInPositionOrder.contains(component))
                return true;
        }
        return false;
    }

    /**
     * @param component
     * @param <C>
     * @return
     */
    public <C extends StateMachineComponent<?, ?>> boolean containsEntitiesForComponent(C component) {
        return smcInPositionOrder.contains(component);
    }

    /**
     * @param components
     * @param <C>
     * @return
     */
    public <C extends StateMachineComponent<?, ?>> Set<ONDEXEntity> getONDEXEntities(Set<C> components) {
        Set<ONDEXEntity> entities = new HashSet<ONDEXEntity>();
        int index = 0;
        for (StateMachineComponent<?, ?> component : smcInPositionOrder) {
            if (components.contains(component)) {
                entities.add(entitiesInPositionOrder.get(index));
            }
            index++;
        }
        return entities;
    }

    /**
     * @param component
     * @param <C>
     * @return
     */
    public <C extends StateMachineComponent<?, ?>> Set<ONDEXEntity> getONDEXEntities(C component) {
        Set<ONDEXEntity> entities = new HashSet<ONDEXEntity>();
        int index = 0;
        for (StateMachineComponent<?, ?> componentUsed : smcInPositionOrder) {
            if (componentUsed.equals(component)) {
                entities.add(entitiesInPositionOrder.get(index));
            }
            index++;
        }
        return entities;
    }

    /**
     * @param o
     * @return
     */
    public boolean equals(Object o) {
        if (o instanceof StateMachineDerivedPath) {
            System.out.println(((StateMachineDerivedPath) o).entitiesInPositionOrder.equals(entitiesInPositionOrder));
            System.out.println(((StateMachineDerivedPath) o).smcInPositionOrder.equals(smcInPositionOrder));

            return (((StateMachineDerivedPath) o).smcInPositionOrder.equals(smcInPositionOrder) &&
                    ((StateMachineDerivedPath) o).entitiesInPositionOrder.equals(entitiesInPositionOrder));
        }
        return false;
    }

    /**
     * @return
     */
    public ONDEXEntityPath toONDEXEntityPath() {
        return new ONDEXEntityPath(entitiesInPositionOrder);
    }

}