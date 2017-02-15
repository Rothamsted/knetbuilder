package net.sourceforge.ondex.algorithm.graphquery;

import java.util.Set;

import junit.framework.TestCase;
import net.sourceforge.ondex.algorithm.graphquery.exceptions.StateMachineInvalidException;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;

/**
 * Check that the StateMachine object is working as expected
 *
 * @author hindlem
 */
public class StateMachineTest extends TestCase {

    private ConceptClass cc;
    private RelationType rt;

    public void setUp() {
        MemoryONDEXGraph aog = new MemoryONDEXGraph("testGraph");
        cc = aog.getMetaData().getFactory().createConceptClass("testCC");
        rt = aog.getMetaData().getFactory().createRelationType("testRT");
    }

    public void testNoStartState() throws StateMachineInvalidException {
        StateMachine sm = new StateMachine();

        State st = new State(cc);
        Transition t = new Transition(rt);

        sm.addStep(st, t, new State(cc));

        try {
            assertEquals(sm.getOutgoingTransitions(st).iterator().next(), t);
            fail("Expected Exception StateMachineInvalidException not thrown as start not specified");
        } catch (StateMachineInvalidException e) {
            //intentional
        }
    }

    public void testNoFinishState() throws StateMachineInvalidException {
        StateMachine sm = new StateMachine();

        State st = new State(cc);
        Transition t = new Transition(rt);

        sm.addStep(st, t, new State(cc));
        sm.setStartingState(new State(cc));
        try {
            assertEquals(sm.getOutgoingTransitions(st), t);
            fail("Expected Exception StateMachineInvalidException not thrown as finish not specified");
        } catch (StateMachineInvalidException e) {
            //intentional
        }
    }
    /*
     public void testAddingTransitionsToFinish() throws StateMachineInvalidException {
         StateMachine sm = new StateMachine();

         State ss = new State(cc);
         State fs = new State(cc);

         sm.addStep(ss, new Transition(rt), fs);
         sm.setStartingState(ss);

         sm.addFinalState(fs);


         try {
             sm.addStep(fs, new Transition(rt), ss);
             fail("Expected Exception StateMachineInvalidException not thrown when transitions on finish specifed");
         } catch (StateMachineInvalidException e) {
             //intentional
         }

         try {
             sm.addFinalState(ss);
             fail("Expected Exception StateMachineInvalidException not thrown when finish specified on state with outgoing transitions");
         } catch (StateMachineInvalidException e) {
             //intentional
         }
     }
     *///feature removed

    public void testTransitionsAddedOk() throws StateMachineInvalidException {
        StateMachine sm = new StateMachine();

        State ss = new State(cc);
        State fs = new State(cc);

        Transition t = new Transition(rt);

        sm.setStartingState(ss);
        sm.addFinalState(fs);

        sm.addStep(ss, t, fs);

        assertEquals(fs, sm.getTransitionTarget(t));

        Set<Transition> nt = sm.getOutgoingTransitions(ss);

        assertEquals(1, nt.size());
        assertEquals(t, nt.iterator().next());
    }

}
