package net.sourceforge.ondex.algorithm.graphquery;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import net.sourceforge.ondex.algorithm.graphquery.exceptions.StateMachineInvalidException;
import net.sourceforge.ondex.algorithm.graphquery.nodepath.EvidencePathNode;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.core.util.BitSetFunctions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the Traversal of Graph using a state machine
 *
 * @author hindlem
 */
public class GraphTraverserTest extends TestCase {

    private MemoryONDEXGraph aog;

    private ConceptClass ccProtein;
//	private ConceptClass ccTF;
//	private ConceptClass ccGene;
    private ConceptClass ccEnzyme;
    private ConceptClass ccReaction;
//	private ConceptClass ccPathway;

//	private ONDEXRelationTypeSet rts_Encodes;
    private RelationType rts_is_a;
//	private ONDEXRelationTypeSet rts_m_isp;
    private RelationType rts_interacts;
//	private ONDEXRelationTypeSet rts_regulates;
    private RelationType rts_cats;

    private EvidenceType et;
    private DataSource dataSource;

    @Before
    public void setUp() {

        aog = new MemoryONDEXGraph("testGraph");


        ccProtein = aog.getMetaData().getFactory().createConceptClass("Protein");
        //ccTF = aog.getMemoryONDEXGraphData().createConceptClass("TF");
        //ccGene = aog.getMemoryONDEXGraphData().createConceptClass("Gene");
        ccEnzyme = aog.getMetaData().getFactory().createConceptClass("Enzyme");
        ccReaction = aog.getMetaData().getFactory().createConceptClass("Reaction");
        //ccPathway = aog.getMemoryONDEXGraphData().createConceptClass("Pathway");
        //
        //RelationType rtEncode = aog.getMemoryONDEXGraphData().createRelationType("Encodes");
        //rts_Encodes = aog.getMemoryONDEXGraphData().createRelationTypeSet("Encodes", rtEncode);

        rts_is_a = aog.getMetaData().getFactory().createRelationType("is_a");

        //RelationType rtm_isp = aog.getMemoryONDEXGraphData().createRelationType("m_isp");
        //rts_m_isp = aog.getMemoryONDEXGraphData().createRelationTypeSet("m_isp", rtm_isp);

        rts_interacts = aog.getMetaData().getFactory().createRelationType("interacts");

        //RelationType rtm_regulates = aog.getMemoryONDEXGraphData().createRelationType("regulates");
        //rts_regulates = aog.getMemoryONDEXGraphData().createRelationTypeSet("regulates", rtm_interacts);

        //RelationType rtm_cats = aog.getMemoryONDEXGraphData().createRelationType("cats");
        rts_cats = aog.getMetaData().getFactory().createRelationType("cats");

        et = aog.getMetaData().getFactory().createEvidenceType("I_made_it_up");
        dataSource = aog.getMetaData().getFactory().createDataSource("matts_db");

    }

    @After
    public void tearDown() {
        aog = null;
    }

    @Test
    public void testSimpleGraphTraversal() throws StateMachineInvalidException {

        StateMachine sm = new StateMachine();

        State p = new State(ccProtein);
        assertEquals(ccProtein.getId(), p.getValidConceptClass().getId());
        State e = new State(ccEnzyme);
        assertEquals(ccEnzyme.getId(), e.getValidConceptClass().getId());
        State r = new State(ccReaction);
        assertEquals(ccReaction.getId(), r.getValidConceptClass().getId());

        Transition t = new Transition(rts_is_a);
        assertEquals(rts_is_a.getId(), t.getValidRelationType().getId());
        Transition c = new Transition(rts_cats);
        assertEquals(rts_cats.getId(), c.getValidRelationType().getId());

        sm.setStartingState(p);
        assertEquals(p, sm.getStart());
        sm.addFinalState(r);
        assertEquals(r, sm.getFinishes().iterator().next());

        sm.addStep(p, t, e);
        assertEquals(p, sm.getTransitionSource(t));
        assertEquals(e, sm.getTransitionTarget(t));
        assertEquals(t, sm.getOutgoingTransitions(p).iterator().next());

        sm.addStep(e, c, r);
        assertEquals(e, sm.getTransitionSource(c));
        assertEquals(r, sm.getTransitionTarget(c));
        assertEquals(c, sm.getOutgoingTransitions(e).iterator().next());

        GraphTraverser gt = new GraphTraverser(sm);

        ONDEXConcept protein1 = aog.getFactory().createConcept("Protein 1", dataSource, ccProtein, et);

        System.out.println("ii" + aog.getConcept(protein1.getId()));
       
        ONDEXConcept protein2 = aog.getFactory().createConcept("Protein 2", dataSource, ccProtein, et);


        ONDEXConcept enzyme1 = aog.getFactory().createConcept("Enzyme 1", dataSource, ccEnzyme, et);

        ONDEXConcept reaction1 = aog.getFactory().createConcept("Reaction 1", dataSource, ccReaction, et);

        ONDEXRelation is_a1 = aog.getFactory().createRelation(protein1, enzyme1, rts_is_a, et);
        ONDEXRelation is_a2 = aog.getFactory().createRelation(protein2, enzyme1, rts_is_a, et);

        ONDEXRelation e_to_r = aog.getFactory().createRelation(enzyme1, reaction1, rts_cats, et);

        EvidencePathNode newPaths1 =
                new EvidencePathNode.FirstEvidenceConceptNode(protein1, p);
        EvidencePathNode newPaths2 =
                new EvidencePathNode.EvidenceRelationNode(is_a1, t, newPaths1);
        EvidencePathNode newPaths3 =
                new EvidencePathNode.EvidenceConceptNode(enzyme1, e, newPaths2);
        EvidencePathNode newPaths4 =
                new EvidencePathNode.EvidenceRelationNode(e_to_r, c, newPaths3);
        EvidencePathNode path1 =
                new EvidencePathNode.EvidenceConceptNode(reaction1, r, newPaths4);

        EvidencePathNode new2Paths1 =
                new EvidencePathNode.FirstEvidenceConceptNode(protein2, p);
        EvidencePathNode new2Paths2 =
                new EvidencePathNode.EvidenceRelationNode(is_a2, t, new2Paths1);
        EvidencePathNode new2Paths3 =
                new EvidencePathNode.EvidenceConceptNode(enzyme1, e, new2Paths2);
        EvidencePathNode new2Paths4 =
                new EvidencePathNode.EvidenceRelationNode(e_to_r, c, new2Paths3);
        EvidencePathNode path2 =
                new EvidencePathNode.EvidenceConceptNode(reaction1, r, new2Paths4);

        Map<ONDEXConcept, List<EvidencePathNode>> paths = gt.traverseGraph(aog, aog.getConcepts(), null);
        assertEquals(2, paths.size());

        for (ONDEXConcept concept : paths.keySet()) {
            List<EvidencePathNode> path = paths.get(concept);
            assertEquals(1, path.size());
            if (concept.equals(protein1)) {
                assertEquals(path1, path.get(0));
            } else if (concept.equals(protein2)) {
                assertEquals(path2, path.get(0));
            } else
                fail();
        }

    }

    @Test
    public void testNPathLengthGraph() throws StateMachineInvalidException {
        StateMachine sm = new StateMachine();

        State p = new State(ccProtein);
        State e = new State(ccEnzyme);
        State r = new State(ccReaction);

        Transition t = new Transition(rts_interacts);
        Transition i = new Transition(rts_is_a);
        Transition c = new Transition(rts_cats);

        sm.setStartingState(p);
        sm.addFinalState(r);

        sm.addStep(p, t, p);
        sm.addStep(p, i, e);
        sm.addStep(e, c, r);

        GraphTraverser gt = new GraphTraverser(sm);

        ONDEXConcept protein1 = aog.getFactory().createConcept("Protein 1a", dataSource, ccProtein, et);
        ONDEXConcept protein2 = aog.getFactory().createConcept("Protein 2a", dataSource, ccProtein, et);
        ONDEXConcept protein3 = aog.getFactory().createConcept("Protein 3a", dataSource, ccProtein, et);
        ONDEXConcept protein4 = aog.getFactory().createConcept("Protein 4a", dataSource, ccProtein, et);

        ONDEXConcept enzyme1 = aog.getFactory().createConcept("Enzyme 1", dataSource, ccEnzyme, et);

        ONDEXConcept reaction1 = aog.getFactory().createConcept("Reaction 1", dataSource, ccReaction, et);

        aog.getFactory().createRelation(protein1, protein2, rts_interacts, et);
        aog.getFactory().createRelation(protein2, protein3, rts_interacts, et);
        aog.getFactory().createRelation(protein3, protein4, rts_interacts, et);

        aog.getFactory().createRelation(protein4, enzyme1, rts_is_a, et);

        aog.getFactory().createRelation(enzyme1, reaction1, rts_cats, et);

        Map<ONDEXConcept, List<EvidencePathNode>> paths = gt.traverseGraph(aog, aog.getConcepts(), null);
        assertEquals(4, paths.size());

        assertTrue(paths.containsKey(protein1));
        assertTrue(paths.containsKey(protein2));
        assertTrue(paths.containsKey(protein3));
        assertTrue(paths.containsKey(protein4));

        for (List<EvidencePathNode> path : paths.values()) {
            assertEquals(1, path.size());
        }
    }

    @Test
    public void testLoopedGraphTraversal() throws StateMachineInvalidException {
        StateMachine sm = new StateMachine();

        State p = new State(ccProtein);
        State e = new State(ccEnzyme);
        State r = new State(ccReaction);

        Transition t = new Transition(rts_interacts);
        Transition i = new Transition(rts_is_a);
        Transition c = new Transition(rts_cats);

        sm.setStartingState(p);
        sm.addFinalState(r);

        sm.addStep(p, t, p);
        sm.addStep(p, i, e);
        sm.addStep(e, c, r);

        GraphTraverser gt = new GraphTraverser(sm);

        ONDEXConcept protein1 = aog.getFactory().createConcept("Protein 1a", dataSource, ccProtein, et);
        ONDEXConcept protein2 = aog.getFactory().createConcept("Protein 2a", dataSource, ccProtein, et);
        ONDEXConcept protein3 = aog.getFactory().createConcept("Protein 3a", dataSource, ccProtein, et);
        ONDEXConcept protein4 = aog.getFactory().createConcept("Protein 4a", dataSource, ccProtein, et);

        ONDEXConcept enzyme1 = aog.getFactory().createConcept("Enzyme 1", dataSource, ccEnzyme, et);

        ONDEXConcept reaction1 = aog.getFactory().createConcept("Reaction 1", dataSource, ccReaction, et);

        aog.getFactory().createRelation(protein1, protein1, rts_interacts, et);
        aog.getFactory().createRelation(protein1, protein2, rts_interacts, et);
        aog.getFactory().createRelation(protein2, protein3, rts_interacts, et);
        aog.getFactory().createRelation(protein3, protein4, rts_interacts, et);

        aog.getFactory().createRelation(protein4, protein1, rts_interacts, et);// a loop or backward

        aog.getFactory().createRelation(protein4, enzyme1, rts_is_a, et);

        aog.getFactory().createRelation(enzyme1, reaction1, rts_cats, et);

        BitSet sbs = new BitSet();
        sbs.set(protein1.getId());//start only from the first concept

        Map<ONDEXConcept, List<EvidencePathNode>> paths = gt.traverseGraph(aog, BitSetFunctions.create(aog, ONDEXConcept.class, sbs), null);
        assertEquals(1, paths.size());

        for (List<EvidencePathNode> path : paths.values()) {
            assertEquals(2, path.size());
        }
    }

    @Test
    public void testLengthRestrictedTraversal() throws StateMachineInvalidException {

        System.out.println("length");

        StateMachine sm = new StateMachine();

        State p = new State(ccProtein);
        State e = new State(ccEnzyme);
        State r = new State(ccReaction);

        Transition t = new Transition(rts_interacts, 1);
        Transition i = new Transition(rts_is_a);
        Transition c = new Transition(rts_cats);

        sm.setStartingState(p);
        sm.addFinalState(r);

        sm.addStep(p, t, p);
        sm.addStep(p, i, e);
        sm.addStep(e, c, r);

        GraphTraverser gt = new GraphTraverser(sm);

        ONDEXConcept protein1 = aog.getFactory().createConcept("Protein 1a", dataSource, ccProtein, et);
        ONDEXConcept protein2 = aog.getFactory().createConcept("Protein 2a", dataSource, ccProtein, et);
        ONDEXConcept protein3 = aog.getFactory().createConcept("Protein 3a", dataSource, ccProtein, et);
        ONDEXConcept protein4 = aog.getFactory().createConcept("Protein 4a", dataSource, ccProtein, et);

        ONDEXConcept enzyme1 = aog.getFactory().createConcept("Enzyme 1", dataSource, ccEnzyme, et);

        ONDEXConcept reaction1 = aog.getFactory().createConcept("Reaction 1", dataSource, ccReaction, et);

        aog.getFactory().createRelation(protein1, protein2, rts_interacts, et);
        aog.getFactory().createRelation(protein2, protein3, rts_interacts, et);
        aog.getFactory().createRelation(protein3, protein4, rts_interacts, et);
        aog.getFactory().createRelation(protein3, protein4, rts_interacts, et);

        aog.getFactory().createRelation(protein3, protein1, rts_interacts, et);

        aog.getFactory().createRelation(protein4, enzyme1, rts_is_a, et);// a loop
        aog.getFactory().createRelation(protein1, enzyme1, rts_is_a, et);// a loop

        aog.getFactory().createRelation(enzyme1, reaction1, rts_cats, et);

        BitSet sbs = new BitSet();
        sbs.set(protein1.getId());//start only from the first concept

        Map<ONDEXConcept, List<EvidencePathNode>> paths = gt.traverseGraph(aog, BitSetFunctions.create(aog, ONDEXConcept.class, sbs), null);
        assertEquals(1, paths.size());

        for (List<EvidencePathNode> path : paths.values()) {
            assertEquals(1, path.size());
        }
    }

    @Test
    public void testLargeGraph() throws StateMachineInvalidException {
        StateMachine sm = new StateMachine();

        State p = new State(ccProtein);
        State e = new State(ccEnzyme);
        State r = new State(ccReaction);

        Transition t = new Transition(rts_interacts);
        Transition i = new Transition(rts_is_a);
        Transition c = new Transition(rts_cats);

        sm.setStartingState(p);
        sm.addFinalState(r);

        sm.addStep(p, t, p);
        sm.addStep(p, i, e);
        sm.addStep(e, c, r);

        GraphTraverser gt = new GraphTraverser(sm);

        ArrayList<Integer> protiens = new ArrayList<Integer>();

        for (int j = 0; j < 5; j++) {
            ONDEXConcept protein = aog.getFactory().createConcept("Protein " + i, dataSource, ccProtein, et);
            ONDEXConcept enzyme = aog.getFactory().createConcept("Enzyme " + i, dataSource, ccEnzyme, et);
            ONDEXConcept reaction = aog.getFactory().createConcept("Reaction " + i, dataSource, ccReaction, et);
            aog.getFactory().createRelation(protein,
                    enzyme, rts_is_a, et);
            aog.getFactory().createRelation(enzyme,
                    reaction, rts_cats, et);
            protiens.add(protein.getId());
        }

        for (int j = 0; j < protiens.size(); j++) {
            for (int k = 0; k < protiens.size(); k++) {
                if (j != k)
                    aog.getFactory().createRelation(aog.getConcept(protiens.get(j)),
                            aog.getConcept(protiens.get(k)), rts_interacts, et);
            }
        }
        System.out.println("starting graph traversal");
        Map<ONDEXConcept, List<EvidencePathNode>> paths = gt.traverseGraph(aog, aog.getConceptsOfConceptClass(ccProtein), null);

        int pathsExp = 0;
        for (int j = 0; j < 4; j++) {
            pathsExp = pathsExp + factorial(5 - j);
        }

        // System.out.println(factorial(5)+" "+factorial(4)+" "+pathsExp);

        //check for uniqueness
        for (List<EvidencePathNode> path : paths.values()) {
            //System.out.println("start "+path.size());
            assertEquals(633, path.size());
            for (EvidencePathNode pathI : path) {
                //System.out.println(pathI.toString());

                Set<ONDEXConcept> concepts = pathI.getAllConcepts();
                //System.out.println(concepts.size()+" path");
                assertTrue(concepts.size() == pathI.getConceptLength());
                assertTrue(concepts.size() == pathI.getAllConcepts().size());

                Set<ONDEXRelation> relations = pathI.getAllRelations();

                assertTrue(relations.size() == pathI.getRelationLength());
                assertTrue(relations.size() == pathI.getAllRelations().size());
            }
        }
        assertEquals(5, paths.size());  //starting concepts

    }

    public static int factorial(int n) {
        if (n <= 1)     // base case
            return 1;
        else
            return n * factorial(n - 1);
    }
}
