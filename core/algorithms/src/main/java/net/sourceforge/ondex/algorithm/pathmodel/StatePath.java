package net.sourceforge.ondex.algorithm.pathmodel;

import net.sourceforge.ondex.algorithm.graphquery.State;
import net.sourceforge.ondex.algorithm.graphquery.StateMachineComponent;
import net.sourceforge.ondex.algorithm.graphquery.Transition;
import net.sourceforge.ondex.core.ONDEXEntity;

import java.util.Set;

/**
 * Defines a methods for accessing a State Path.
 *
 * @author hindlem
 */
public interface StatePath {

    /**
     * @return the last state that was responsible for the last added concept
     */
    public State getLastState();

    /**
     * @return the last transition that was responsible for the last added relation
     */
    public Transition getLastTransition();

    /**
     * @param component the state or transition
     * @param <C>       state or transition
     * @return the number of elements in the path this component is responsible for
     */
    public <C extends StateMachineComponent<?, ?>> int getComponentCountInPath(C component);

    /**
     * @param components the state or transition
     * @param <C>        state or transition
     * @return are these components all responsible for elements in this path
     */
    public <C extends StateMachineComponent<?, ?>> boolean containsEntitiesForAllComponents(Set<C> components);

    /**
     * @param components the state or transition
     * @param <C>        state or transition
     * @return are this components responsible for any elements in this path
     */
    public <C extends StateMachineComponent<?, ?>> boolean containsEntitiesForSomeComponents(Set<C> components);

    /**
     * @param component the state or transition
     * @param <C>       state or transition
     * @return is this component responsible for any elements in this path
     */
    public <C extends StateMachineComponent<?, ?>> boolean containsEntitiesForComponent(C component);

    /**
     * @param components the state or transition
     * @param <C>        state or transition
     * @return the path entities these components are responsible for
     */
    public <C extends StateMachineComponent<?, ?>> Set<ONDEXEntity> getONDEXEntities(Set<C> components);

    /**
     * @param component the state or transition
     * @param <C>       state or transition
     * @return the path entities this component is responsible for
     */
    public <C extends StateMachineComponent<?, ?>> Set<ONDEXEntity> getONDEXEntities(C component);

}
