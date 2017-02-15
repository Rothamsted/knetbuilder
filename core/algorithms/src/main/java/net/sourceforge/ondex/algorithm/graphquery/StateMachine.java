package net.sourceforge.ondex.algorithm.graphquery;

import net.sourceforge.ondex.algorithm.graphquery.exceptions.StateMachineInvalidException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A object that defines a finite state machine with a start and multiple possible ends
 *
 * @author hindlem
 */
public class StateMachine {

    //Starting state
    private State start;
    //Final state
    private Set<State> finishes;

    private Map<State, Set<Transition>> transitionIndex = new HashMap<State, Set<Transition>>();
    private Map<Transition, State> targetStateIndex = new HashMap<Transition, State>();
    private Map<Transition, State> sourceStateIndex = new HashMap<Transition, State>();


    private static final Set<Transition> NO_TRANSITIONS = new HashSet<Transition>(0);
    private static final Set<State> NO_STATES = new HashSet<State>(0);

    /**
     * @param start the state the machine starts on
     */
    public void setStartingState(State start) {
        this.start = start;
    }

    /**
     * @param finish the state the machin finishes on
     * @throws StateMachineInvalidException if a finish state is specified for a State with outgoing transitions
     */
    public void addFinalState(State finish) throws StateMachineInvalidException {
        //if (transitionIndex.keySet().contains(finish)) {
        //	throw new StateMachineInvalidException("A finishing state can not have outgoing transitions");
        //}
        if (finishes == null)
            finishes = new HashSet<State>(1);
        finishes.add(finish);
    }

    /**
     * Adds a transition step from one state to another
     *
     * @param a the from in the transition
     * @param t the transition
     * @param b the to in the transition
     * @throws StateMachineInvalidException if a transition from a finish state is specified
     */
    public void addStep(State a, Transition t, State b) throws StateMachineInvalidException {
        //  if (finished != null && finishes.contains(a)) {
        //  	throw new StateMachineInvalidException("A finishing state can not have outgoing transitions");
        //  }

        Set<Transition> lista = transitionIndex.get(a);
        if (lista == null) {
            lista = new HashSet<Transition>(1);
            transitionIndex.put(a, lista);
        }
        lista.add(t);

        sourceStateIndex.put(t, a);
        targetStateIndex.put(t, b);
    }

    /**
     * @param s the state to fetch transitions on
     * @return the transitions
     * @throws StateMachineInvalidException is you haven't declared a finish
     */
    public Set<Transition> getOutgoingTransitions(State s) throws StateMachineInvalidException {
        checkValidStateMachine();
        Set<Transition> transitions = transitionIndex.get(s);
        if (transitions == null) {
            transitions = NO_TRANSITIONS;
        }
        return transitions;
    }

    /**
     * checks for machine definition errors
     *
     * @throws StateMachineInvalidException
     */
    private void checkValidStateMachine() throws StateMachineInvalidException {
        if (start == null) throw new StateMachineInvalidException("State machine has no start");
        if (finishes == null) throw new StateMachineInvalidException("State machine has no finish states");
    }

    /**
     * @param t the transition to get the target state of
     * @return the target for this transition
     */
    public State getTransitionTarget(Transition t) {
        return targetStateIndex.get(t);
    }

    /**
     * @param t the transition to get the source state of
     * @return the source for this transition
     */
    public State getTransitionSource(Transition t) {
        return sourceStateIndex.get(t);
    }

    /**
     * @param s the State to test
     * @return is this a finish state
     */
    public boolean isFinish(State s) throws StateMachineInvalidException {
        checkValidStateMachine();
        return finishes.contains(s);
    }

    public Object clone() throws CloneNotSupportedException {
        try {
            checkValidStateMachine();
        } catch (StateMachineInvalidException e) {
            e.printStackTrace();
        }
        StateMachine sm = (StateMachine) super.clone();
        sm.start = start;
        sm.finishes = finishes;
        sm.targetStateIndex = new HashMap<Transition, State>(targetStateIndex);
        sm.sourceStateIndex = new HashMap<Transition, State>(sourceStateIndex);
        sm.transitionIndex = new HashMap<State, Set<Transition>>(transitionIndex);
        return sm;
    }

    public State getStart() {
        return start;
    }

    public Set<State> getFinishes() throws StateMachineInvalidException {
        checkValidStateMachine();
        return finishes;
    }

    public Set<Transition> getAllTransitions() {
        return sourceStateIndex.keySet();
    }

    public Set<State> getAllStates() {
        HashSet<State> all = new HashSet<State>(targetStateIndex.values()); //set all from
        all.addAll(sourceStateIndex.values());
        all.add(start);
        return all;
    }

}
