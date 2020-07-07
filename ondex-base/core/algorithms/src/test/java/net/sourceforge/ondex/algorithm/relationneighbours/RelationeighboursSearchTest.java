package net.sourceforge.ondex.algorithm.relationneighbours;

import java.util.Random;
import java.util.Set;

import junit.framework.TestCase;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.base.AbstractONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.ONDEXListener;
import net.sourceforge.ondex.logging.ONDEXLogger;

/**
 * @author hindlem
 */
public class RelationeighboursSearchTest extends TestCase {

    private AbstractONDEXGraph og;
    private ConceptClass cc1;
    private DataSource dataSource1;
    private EvidenceType ev;
    private RelationType r;
    private RelationType r1;
    private RelationType r3;
    private RelationType r2;

    public RelationeighboursSearchTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
       
        og = new MemoryONDEXGraph(RelationeighboursSearchTest.class.getName());
        assertNotNull(og);

        ONDEXListener logger = new ONDEXLogger();
        ONDEXEventHandler.getEventHandlerForSID(og.getSID()).addONDEXONDEXListener(logger);

        cc1 = og.getMetaData().getFactory().createConceptClass("cc1");
        dataSource1 = og.getMetaData().getFactory().createDataSource("dataSource1");
        ev = og.getMetaData().getFactory().createEvidenceType("ev");

        r = og.getMetaData().getFactory().createRelationType("r");
        r1 = og.getMetaData().getFactory().createRelationType("r1");
        r2 = og.getMetaData().getFactory().createRelationType("r2");
        r3 = og.getMetaData().getFactory().createRelationType("r3");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        cc1 = null;
        dataSource1 = null;
        ev = null;
        r = null;
        r1 = null;
        r2 = null;
        r3 = null;
    }

    public void testDoNotRetainSingleConceptClustersConcept() {

        for (int i = 1; i < 100; i++) {
            og.getFactory().createConcept("c:" + i, dataSource1, cc1, ev);
        }

        RelationNeighboursSearch nrns = new RelationNeighboursSearch(og);
        nrns.search(og.getConcept(1), 5);

        assertEquals(0, nrns.getFoundConcepts().size());
        assertEquals(0, nrns.getFoundRelations().size());
    }


    public void testChainOfRelatedConcept() {
        //forms long chain from 1 to end of relations
        for (int i = 1; i < 100; i++) {
            og.getFactory().createConcept("c:" + i, dataSource1, cc1, ev);
            if (i > 1) {
                og.getFactory().createRelation(
                        og.getConcept(i),
                        og.getConcept(i - 1),
                        r, ev);
            }
        }

        RelationNeighboursSearch nrns = new RelationNeighboursSearch(og);
        nrns.search(og.getConcept(1), 5);

        assertEquals(6, nrns.getFoundConcepts().size());
        assertEquals(5, nrns.getFoundRelations().size());

        System.out.println("OK");

        nrns = new RelationNeighboursSearch(og);
        nrns.search(og.getConcept(1), Integer.MAX_VALUE);

        assertEquals(99, nrns.getFoundConcepts().size());
        assertEquals(98, nrns.getFoundRelations().size());

        nrns.search(og.getConcept(50), Integer.MAX_VALUE);

        assertEquals(99, nrns.getFoundConcepts().size());
        assertEquals(98, nrns.getFoundRelations().size());

        nrns.search(og.getConcept(50), 50);

        assertEquals(99, nrns.getFoundConcepts().size());
        assertEquals(98, nrns.getFoundRelations().size());

        nrns.search(og.getConcept(1), 50);

        assertEquals(51, nrns.getFoundConcepts().size());
        assertEquals(50, nrns.getFoundRelations().size());
    }


    public void testDeterminism() {

        ONDEXConcept concept = og.getFactory().createConcept("c", dataSource1, cc1, ev);
        //forms long chain from 1 to end of relations
        for (int i = 1; i < 30; i++) {
            ONDEXConcept concept1 = og.getFactory().createConcept("c:" + i, dataSource1, cc1, ev);
            if (i > 1) {
                og.getFactory().createRelation(
                        concept1,
                        og.getConcept(i - 1),
                        r, ev);
            } else {
                og.getFactory().createRelation(
                        concept1,
                        concept,
                        r, ev);
            }
            for (int j = 1; j < i * 2; j++) {
                ONDEXConcept concept2 = og.getFactory().createConcept("cb:" + j, dataSource1, cc1, ev);
                if (j > 1) {
                    og.getFactory().createRelation(
                            concept2,
                            og.getConcept((i + j) - 1),
                            r, ev);
                } else {
                    og.getFactory().createRelation(
                            concept2,
                            concept1,
                            r, ev);
                }
            }
        }

        Set<ONDEXConcept> previousResultC = null;
        Set<ONDEXRelation> previousResultR = null;

        RelationNeighboursSearch nrns = new RelationNeighboursSearch(og);
        for (int i = 0; i < 90; i++) {
            nrns.search(concept, Integer.MAX_VALUE);
            Set<ONDEXConcept> foundC = BitSetFunctions.copy(nrns.getFoundConcepts());
            Set<ONDEXRelation> foundR = BitSetFunctions.copy(nrns.getFoundRelations());
            assertEquals("Failed on try " + i, og.getConcepts().size(), foundC.size());
            assertEquals("Failed on try " + i, og.getRelations().size(), foundR.size());

            if (previousResultC != null) {

                previousResultC.removeAll(foundC);
                assertEquals(0, previousResultC.size());

                previousResultR.removeAll(foundR);
                assertEquals(0, previousResultR.size());
            }

            previousResultC = foundC;
            previousResultR = foundR;
        }

    }

    public void testBiRelatedConcept() {

        //forms long chain from 1 to end of relations
        ONDEXConcept previousConcept = null;
        for (int i = 1; i < 100; i++) {
            ONDEXConcept concept = og.getFactory().createConcept("c:" + i, dataSource1, cc1, ev);
            if (previousConcept != null) {
                og.getFactory().createRelation(
                        previousConcept,
                        concept,
                        r, ev);
                og.getFactory().createRelation(
                        concept,
                        previousConcept,
                        r, ev);
            }
            previousConcept = concept;
        }

        RelationNeighboursSearch nrns = new RelationNeighboursSearch(og);
        nrns.search(og.getConcept(1), 50);

        assertEquals(51, nrns.getFoundConcepts().size());
        assertEquals(100, nrns.getFoundRelations().size());
    }

    public void testTrueBranchedRelatedConcept() {
        RelationNeighboursSearch nrns = new RelationNeighboursSearch(og);

        for (int branches = 2; branches < 3; branches++) {
            for (int depth = 1; depth < 7; depth++) {
                System.out.println(branches + ":" + depth);
                ONDEXConcept root = og.getFactory().createConcept("root", dataSource1, cc1, ev);
                createRecursiveNodesOf(root, depth - 1, branches);

                nrns.search(root, Integer.MAX_VALUE);

                int sum = 0;
                for (int d = 0; d <= depth; d++) {
                    sum = (int) (sum + Math.pow(branches, d));
                }

                assertEquals(sum, nrns.getFoundConcepts().size());
                assertEquals(sum - 1, nrns.getFoundRelations().size());
            }
        }
    }

    private int createRecursiveNodesOf(ONDEXConcept c, int depth, int branches) {

        int nodes = 0;
        for (int i = 1; i <= branches; i++) {
            ONDEXConcept branch = og.getFactory().createConcept("branch" + i + ":" + depth, dataSource1, cc1, ev);
            nodes++;
            //System.out.println("branch"+i+":"+depth);
            og.getFactory().createRelation(c, branch, r, ev);
            if (depth > 0) {
                nodes = nodes + createRecursiveNodesOf(branch, depth - 1, branches);
            }
        }
        return nodes;
    }

    public void testSelfRelations() {

        //forms long chain from 1 to end of relations
        ONDEXConcept concept = og.getFactory().createConcept("root", dataSource1, cc1, ev);
        og.getFactory().createRelation(
                concept,
                concept,
                r, ev);
        og.getFactory().createRelation(
                concept,
                concept,
                r2, ev);
        og.getFactory().createRelation(
                concept,
                concept,
                r3, ev);

        RelationNeighboursSearch nrns = new RelationNeighboursSearch(og);
        nrns.search(og.getConcept(1), 50);

        assertEquals(1, nrns.getFoundConcepts().size());
        assertEquals(3, nrns.getFoundRelations().size());
    }

    public void testCycle() {
        ONDEXConcept concept = og.getFactory().createConcept("1", dataSource1, cc1, ev);
        ONDEXConcept concept2 = og.getFactory().createConcept("2", dataSource1, cc1, ev);
        ONDEXConcept concept3 = og.getFactory().createConcept("3", dataSource1, cc1, ev);
        og.getFactory().createRelation(
                concept,
                concept2,
                r, ev);
        og.getFactory().createRelation(
                concept2,
                concept3,
                r, ev);
        og.getFactory().createRelation(
                concept3,
                concept,
                r, ev);
        RelationNeighboursSearch nrns = new RelationNeighboursSearch(og);
        nrns.search(og.getConcept(1), 50);

        assertEquals(3, nrns.getFoundConcepts().size());
        assertEquals(3, nrns.getFoundRelations().size());
    }

    public void testRandomGraphs() {

        Random randomNumberGenerator = new Random(System.currentTimeMillis() * System.identityHashCode(this));
        Random randomNumberGenerator2 = new Random(System.currentTimeMillis() * System.nanoTime());

        ONDEXConcept[] concepts = new ONDEXConcept[500];
        for (int j = 0; j < concepts.length; j++) {
            concepts[j] = og.getFactory().createConcept("concept:" + j, dataSource1, cc1, ev);
        }

        for (ONDEXConcept concept : concepts) {
            int num = Math.round(randomNumberGenerator.nextInt(concepts.length));
            int num2 = Math.round(randomNumberGenerator2.nextInt(concepts.length));
            og.getFactory().createRelation(
                    concepts[num],
                    concepts[num2],
                    r, ev);
            num = Math.round(randomNumberGenerator.nextInt(concepts.length));
            num2 = Math.round(randomNumberGenerator2.nextInt(concepts.length));
            og.getFactory().createRelation(
                    concepts[num],
                    concepts[num2],
                    r2, ev);
            num = Math.round(randomNumberGenerator.nextInt(concepts.length));
            num2 = Math.round(randomNumberGenerator2.nextInt(concepts.length));
            og.getFactory().createRelation(
                    concepts[num2],
                    concepts[num],
                    r3, ev);
        }
        for (ONDEXConcept concept : concepts) {
            int num = Math.round(randomNumberGenerator.nextInt(concepts.length));
            int num2 = Math.round(randomNumberGenerator2.nextInt(concepts.length));
            og.getFactory().createRelation(
                    concepts[num],
                    concepts[num2],
                    r, ev);
            og.getFactory().createRelation(
                    concepts[num2],
                    concepts[num],
                    r, ev);
        }

        RelationNeighboursSearch nrns = new RelationNeighboursSearch(og);

        for (ONDEXConcept concept : concepts) {
            nrns.search(concept, Integer.MAX_VALUE);

            if (nrns.getFoundConcepts().size() == 1 && nrns.getFoundRelations().size() > 0) {
                Set<ONDEXRelation> rels = og.getRelationsOfConcept(nrns.getFoundConcepts().iterator().next());
                for (ONDEXRelation rel : rels) {
                    assertEquals(rel.getKey().getFromID(), rel.getKey().getToID());
                }
            }
        }
    }

    public void testBidirectionalRelations() {
        ONDEXConcept root = og.getFactory().createConcept("root", dataSource1, cc1, ev);
        int nodes = createRecursiveNodesOf(root, 3, 5);
        ONDEXConcept root2 = og.getFactory().createConcept("root2", dataSource1, cc1, ev);
        nodes = nodes + createRecursiveNodesOf(root2, 5, 4);
        ONDEXConcept root3 = og.getFactory().createConcept("root3", dataSource1, cc1, ev);
        nodes = nodes + createRecursiveNodesOf(root3, 6, 2);
        ONDEXConcept root4 = og.getFactory().createConcept("root4", dataSource1, cc1, ev);
        nodes = nodes + createRecursiveNodesOf(root4, 6, 2);
        ONDEXConcept root5 = og.getFactory().createConcept("root5", dataSource1, cc1, ev);
        nodes = nodes + createRecursiveNodesOf(root5, 6, 2);
        ONDEXConcept root6 = og.getFactory().createConcept("root6", dataSource1, cc1, ev);
        nodes = nodes + createRecursiveNodesOf(root6, 6, 2);

        og.getFactory().createRelation(root, root2, r1, ev);
        og.getFactory().createRelation(root3, root2, r1, ev);
        og.getFactory().createRelation(root3, root5, r1, ev);
        og.getFactory().createRelation(root3, root4, r1, ev);
        og.getFactory().createRelation(root4, root5, r1, ev);
        og.getFactory().createRelation(root4, root2, r1, ev);
        og.getFactory().createRelation(root6, root2, r1, ev);
        og.getFactory().createRelation(root6, root3, r1, ev);
        og.getFactory().createRelation(root6, root4, r1, ev);
        og.getFactory().createRelation(root6, root5, r1, ev);

        og.getFactory().createRelation(root, root, r1, ev);
        og.getFactory().createRelation(root6, root6, r1, ev);

        RelationNeighboursSearch ns = new RelationNeighboursSearch(og);

        DepthInsensitiveRTValidator validator = new DepthInsensitiveRTValidator();
        validator.addIncomingRelationType(r1);
        validator.addOutgoingRelationType(r1);
        ns.search(root);

        assertEquals(ns.getFoundConcepts().size(), nodes + 6);
    }
}
