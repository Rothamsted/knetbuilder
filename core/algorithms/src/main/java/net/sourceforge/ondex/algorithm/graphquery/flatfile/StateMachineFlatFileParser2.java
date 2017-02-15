package net.sourceforge.ondex.algorithm.graphquery.flatfile;

import net.sourceforge.ondex.algorithm.graphquery.DirectedEdgeTransition;
import net.sourceforge.ondex.algorithm.graphquery.DirectedEdgeTransition.EdgeTreatment;
import net.sourceforge.ondex.algorithm.graphquery.State;
import net.sourceforge.ondex.algorithm.graphquery.StateMachine;
import net.sourceforge.ondex.algorithm.graphquery.Transition;
import net.sourceforge.ondex.algorithm.graphquery.exceptions.InvalidFileException;
import net.sourceforge.ondex.algorithm.graphquery.exceptions.StateMachineInvalidException;
import net.sourceforge.ondex.algorithm.graphquery.pathrank.NumericalRank;
import net.sourceforge.ondex.algorithm.graphquery.pathrank.NumericalRank.ComparisonMethod;
import net.sourceforge.ondex.algorithm.graphquery.pathrank.StateNumericalRank;
import net.sourceforge.ondex.algorithm.graphquery.pathrank.TransitionNumericalRank;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;

import java.io.*;
import java.util.*;

/**
 * @author hindlem
 */
public class StateMachineFlatFileParser2 {

    /**
     * Represents which block of information in the flat file we are in
     *
     * @author hindlem
     */
    static enum DataBlock {
        NONE, STATES, TRANSITIONS, STATE_RANK, TRANSITION_RANK;

        public static DataBlock translateLine(String line) {
            if (line.toLowerCase().startsWith("#finite states")) {
                return STATES;
            } else if (line.toLowerCase().startsWith("#transitions")) {
                return TRANSITIONS;
            } else if (line.toLowerCase().startsWith("#weightings on finite states")) {
                return STATE_RANK;
            } else if (line.toLowerCase().startsWith("#weightings on transitions")) {
                return TRANSITION_RANK;
            }
            return NONE;
        }
    }

    private StateMachine sm;
    private List<NumericalRank> ranks;

    public void parseString(String s, ONDEXGraph og) throws InvalidFileException, StateMachineInvalidException, IOException {
        BufferedReader br = new BufferedReader(new StringReader(s));
        parseReader(br, og);
    }

    /**
     * @param f  the file representing the flat file
     * @param og an OndexGraph with the MetaData referenced in the flatfile
     * @throws InvalidFileException
     * @throws StateMachineInvalidException
     */
    public void parseFile(File f, ONDEXGraph og) throws InvalidFileException, StateMachineInvalidException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        parseReader(br, og);
    }

    public void parseReader(BufferedReader br, ONDEXGraph og) throws InvalidFileException, IOException, StateMachineInvalidException {
        sm = null;
        ranks = new ArrayList<NumericalRank>();

        DataBlock block = DataBlock.NONE;

        Map<Integer, State> statesConceptClass = new HashMap<Integer, State>();
        Map<String, Transition> transitionRts = new HashMap<String, Transition>();

        State startingState = null;
        HashSet<State> finishingStates = new HashSet<State>();

        boolean transitionsDeclared = false;
        boolean statesDeclared = false;

        while (br.ready()) {
            String line = br.readLine();
            if (line.trim().length() == 0) {
                continue;
            }

            if (line.startsWith("#")) {
                block = DataBlock.translateLine(line);
            } else {

                switch (block) {
                    case STATES:

                        String[] valuess = line.split("\t");

                        if (valuess.length < 2) {
                            throw new InvalidFileException("Finite States must at least include a Integer Id and a ConceptClass name");
                        }
                        int state;
                        boolean isStartingState = false;
                        boolean isFinishingState = false;

                        try {
                            if (valuess[0].contains("*")) {
                                state = Integer.valueOf(valuess[0].trim().replace("*", ""));
                                if (startingState != null) {
                                    throw new InvalidFileException("Error: Multiple start states defined");
                                }
                                isStartingState = true;
                            } else if (valuess[0].contains("^")) {
                                state = Integer.valueOf(valuess[0].trim().replace("^", ""));
                                isFinishingState = true;
                            } else {
                                state = Integer.valueOf(valuess[0].trim());
                            }
                        } catch (NumberFormatException e1) {
                            throw new InvalidFileException("Invalid state id (must be an integer value)");
                        }

                        ConceptClass conceptClass = og.getMetaData().getConceptClass(valuess[1].trim());

                        if (conceptClass == null) {
                            System.err.println(
                                    "Warning: ConceptClass " + valuess[1].trim() + " is not found on meta data");
                            continue;
                        }

                        State statep = new State(conceptClass);

                        if (isStartingState) {
                            startingState = statep;
                        } else if (isFinishingState) {
                            finishingStates.add(statep);
                        }

                        statesConceptClass.put(state, statep);
                        statesDeclared = true;
                        break;

                    case TRANSITIONS:
                        String[] valuest = line.split("\t");

                        if (valuest.length < 2) {
                            throw new InvalidFileException("Transitions must at least include a state id pair and a RelationType name: " + line);
                        }
                        String pair = valuest[0].trim();

                        String[] pairValues = pair.split("-");
                        if (pairValues.length != 2) {
                            throw new InvalidFileException("Transitions must be represented by a stateId-stateId pair (e.g 1-2)");
                        }
                        try {
                            Integer.parseInt(pairValues[0]);
                            Integer.parseInt(pairValues[1]);
                        } catch (NumberFormatException e) {
                            throw new InvalidFileException("Invalid stateId-stateId pair for Transition");
                        }

                        RelationType relationType = og.getMetaData().getRelationType(valuest[1].trim());
                        if (relationType == null) {
                            System.err.println(
                                    "Warning: RelationType " + valuest[1].trim() + " is not found in MetaData");
                            continue;
                        }

                        int maxLength = Integer.MAX_VALUE;
                        if (valuest.length > 2 && valuest[2].trim().length() > 0) {
                            if (valuest[2].trim().equalsIgnoreCase("u")) { //for unlimited
                                maxLength = Integer.MAX_VALUE;
                            } else {
                                maxLength = Integer.parseInt(valuest[2]);
                            }
                        }
                        
                        boolean isDirection = false;
                        if (valuest.length > 3 && valuest[3].trim().length() > 0) {
                            if (valuest[3].trim().equalsIgnoreCase("d")) { //for unlimited
                            	isDirection = true;
                            }
                        }
                        
                        //unique key ID-ID-RT
                        String key = pair+"-"+valuest[1].trim();

                        Transition t = new Transition(relationType, maxLength);
                        
                        if(isDirection)
                        	t = new DirectedEdgeTransition(relationType, maxLength, EdgeTreatment.FORWARD);

                        transitionRts.put(key, t);
                        transitionsDeclared = true;
                        break;

                    case STATE_RANK:

                        if (!transitionsDeclared || !statesDeclared) {
                            throw new InvalidFileException("The state machine must be defined before weightings");
                        }

                        String[] valuessr = line.split("\t");
                        int rank = 0;

                        if (valuessr.length < 5) {
                            throw new InvalidFileException("Weighting on state inadequately defined");
                        }

                        try {
                            if (valuessr.length > 3)
                                rank = Integer.parseInt(valuessr[3]);
                        } catch (NumberFormatException e) {
                            throw new InvalidFileException("Relative ranking of ranks should be an integer value");
                        }

                        String attributeName = valuessr[1];
                        AttributeName att = og.getMetaData().getAttributeName(attributeName);
                        if (att == null) {
                            throw new InvalidFileException("Attribute is not found in metadata " + attributeName);
                        }

                        State states = statesConceptClass.get(Integer.valueOf(valuessr[0].trim()));

                        boolean invertedOrder = valuessr[2].contains("i");
                        boolean modulusValues = valuessr[2].contains("m");

                        ComparisonMethod method = getMethodType(valuessr[4].trim().toLowerCase());

                        StateNumericalRank snr = new StateNumericalRank(rank, att, states, sm, og, method, invertedOrder, modulusValues);
                        ranks.add(snr);

                        break;
                    case TRANSITION_RANK:
                        if (!transitionsDeclared || !statesDeclared) {
                            throw new InvalidFileException("The state machine must be defined before weightings: t=" + transitionsDeclared + " s=" + statesDeclared);
                        }
                        String[] valuestr = line.split("\t");

                        if (valuestr.length < 5) {
                            throw new InvalidFileException("Weighting on transitions inadequately defined :" + line);
                        }

                        rank = 0;

                        try {
                            if (valuestr.length > 3)
                                rank = Integer.parseInt(valuestr[3]);
                        } catch (NumberFormatException e) {
                            throw new InvalidFileException("Relative ranking of ranks should be an integer value");
                        }

                        String attributeName2 = valuestr[1];
                        AttributeName att2 = og.getMetaData().getAttributeName(attributeName2);
                        if (att2 == null) {
                            throw new InvalidFileException("Attribute is not found in metadata " + attributeName2);
                        }

                        Set<Transition> transitions = new HashSet<Transition>();

                        for (String transitionId : valuestr[0].trim().split(",")) {
                            Transition transition = transitionRts.get(transitionId.trim());
                            transitions.add(transition);
                        }

                        invertedOrder = valuestr[2].contains("i");
                        modulusValues = valuestr[2].contains("m");

                        method = getMethodType(valuestr[4].trim().toLowerCase());

                        TransitionNumericalRank tnr = new TransitionNumericalRank(rank, att2,
                                transitions, sm, og, method, invertedOrder, modulusValues);
                        ranks.add(tnr);
                        Collections.sort(ranks);
                        break;
                }

                if (transitionsDeclared && statesDeclared) {
                    initalizeStateMachine(transitionRts,
                            statesConceptClass,
                            startingState,
                            finishingStates);
                }

            }
        }
    }

    /**
     * @param transitionRts
     * @param statesConceptClass
     * @param startingState
     * @param finishingStates
     * @throws InvalidFileException
     * @throws StateMachineInvalidException
     */
    public void initalizeStateMachine(
            Map<String, Transition> transitionRts,
            Map<Integer, State> statesConceptClass,
            State startingState,
            Set<State> finishingStates
    ) throws InvalidFileException, StateMachineInvalidException {

        sm = new StateMachine();

        for (String tid : transitionRts.keySet()) {
            String[] values = tid.split("-");
            int statefrom = Integer.parseInt(values[0]);
            int stateto = Integer.parseInt(values[1]);

            Transition transition = transitionRts.get(tid);

            State fromState = statesConceptClass.get(statefrom);
            State toState = statesConceptClass.get(stateto);

            if (fromState == null || toState == null) {
                System.err.println("Undefined state referenced in transition: " + tid + ": this may be due to missing metadata and an uninitialized state");
                continue;
            }
            sm.addStep(fromState, transition, toState);

        }

        if (startingState == null) {
            throw new InvalidFileException("No starting state defined");
        }

        sm.setStartingState(startingState);

        if (finishingStates.size() == 0) {
            throw new InvalidFileException("No finishing states defined");
        }

        for (State state : finishingStates) {
            sm.addFinalState(state);
        }
    }

    private ComparisonMethod getMethodType(String name) throws InvalidFileException {
        ComparisonMethod method = null;

        if (name.equals("probability") || name.replaceAll(" ", "").equals("probability(independent)")) {
            method = ComparisonMethod.INTEPENENTPROBABLITIES;
        } else if (name.equals("mean")) {
            method = ComparisonMethod.MEAN;
        } else if (name.equals("sum")) {
            method = ComparisonMethod.SUM;
        } else {
            throw new InvalidFileException("unrecognised comparison type " + method);
        }
        return method;
    }

    public StateMachine getStateMachine() {
        return sm;
    }

    public List<NumericalRank> getRanks() {
        return ranks;
    }

}
