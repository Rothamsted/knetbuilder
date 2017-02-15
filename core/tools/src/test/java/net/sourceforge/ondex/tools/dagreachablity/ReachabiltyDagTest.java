/**
 *
 */
package net.sourceforge.ondex.tools.dagreachablity;


import junit.framework.TestCase;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;

import org.junit.After;
import org.junit.Before;

/**
 * @author hindlem
 */
public class ReachabiltyDagTest extends TestCase {

    private ONDEXGraph aog;
    private ConceptClass process;
    private ConceptClass protein;
    private RelationType is_a;
    private RelationType parts_in;
    private EvidenceType madeUp;
    private DataSource goDataSource;


    public int countTestCases() {
        return super.countTestCases();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
       
        aog = new MemoryONDEXGraph("testGraph");

        goDataSource = aog.getMetaData().getFactory().createDataSource("GO");
        process = aog.getMetaData().getFactory().createConceptClass("BioProcess");
        protein = aog.getMetaData().getFactory().createConceptClass("Protein");

        is_a = aog.getMetaData().getFactory().createRelationType("is_a");
        parts_in = aog.getMetaData().getFactory().createRelationType("participates_in");

        madeUp = aog.getMetaData().getFactory().createEvidenceType("MadeUP");
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {

    }

    public void test() {
        //all test cases removed as DAG test is incomplete
    }

//
//	/**
//	 *
//	 */
//	@Test
//	public void testGetShortestPathsToRoot() throws Exception {
//
//		ONDEXConcept rootConcept = aog.getFactory().createConcept("root 1", goDataSource, process, madeUp);
//
//		ONDEXConcept conceptA1 = aog.getFactory().createConcept("branchA 1", goDataSource, process, madeUp);
//		ONDEXConcept conceptA2 = aog.getFactory().createConcept("branchA 2", goDataSource, process, madeUp);
//		ONDEXConcept conceptA3 = aog.getFactory().createConcept("branchA 3", goDataSource, process, madeUp);
//		ONDEXConcept conceptAB4 = aog.getFactory().createConcept("branchAB 4", goDataSource, process, madeUp);
//
//		ONDEXRelation r1RootToA1 = aog.getFactory().createRelation(conceptA1, rootConcept, is_a, madeUp);
//		ONDEXRelation r1A1ToA2 = aog.getFactory().createRelation(conceptA2, conceptA1, is_a, madeUp);
//		ONDEXRelation r1A2ToA3 = aog.getFactory().createRelation(conceptA3, conceptA2, is_a, madeUp);
//		ONDEXRelation r1A3ToAB4 = aog.getFactory().createRelation(conceptAB4, conceptA3, is_a, madeUp);
//
//		ONDEXConcept conceptB1 = aog.getFactory().createConcept("branchB 1", goDataSource, process, madeUp);
//		ONDEXConcept conceptB2 = aog.getFactory().createConcept("branchB 2", goDataSource, process, madeUp);
//
//		ONDEXRelation r1RootToB1 = aog.getFactory().createRelation(conceptB1, rootConcept, is_a, madeUp);
//		ONDEXRelation r1B1ToB2 = aog.getFactory().createRelation(conceptB2, conceptB1, is_a, madeUp);
//		ONDEXRelation r1B2ToAB4 = aog.getFactory().createRelation(conceptAB4, conceptB2, is_a, madeUp);
//
//		Set<RelationType> rt = new HashSet<RelationType>(1);
//		rt.add(is_a);
//
//		ReachabiltyDag rdag = new ReachabiltyDag(aog, process, rt, true);
//
//		Assert.assertEquals(1, rdag.getRoots().size());
//		Assert.assertEquals(rootConcept.getId(), rdag.getRoots().iterator().next().intValue());
//
//		Assert.assertEquals(1, rdag.getLeafs().size());
//		Assert.assertEquals(conceptAB4.getId(), rdag.getLeafs().iterator().next().intValue());
//
//		ONDEXConcept[][] paths = rdag.getShortestPathsToRoot(conceptAB4);
//		Assert.assertEquals(1, paths.length);
//
//        for (ONDEXConcept id: paths[0]) {
//              System.out.println(id.getId());
//        }
//		Assert.assertEquals(4, paths[0].length);
//
//		for (int i = 0; i < paths[0].length; i++) {
//			ONDEXConcept id = paths[0][i];
//			switch (i) {
//				case 0: Assert.assertEquals(5, id.getId()); break;
//				case 1: Assert.assertEquals(7, id.getId()); break;
//				case 2: Assert.assertEquals(6, id.getId()); break;
//				case 3: Assert.assertEquals(1, id.getId()); break;
//			}
//		}
//	}
//
//	/**
//	 *
//	 */
//	@Test
//	public void testGetLeafs() throws Exception {
//
//		ONDEXConcept rootConcept = aog.getFactory().createConcept("root 1", goDataSource, process, madeUp);
//
//		ONDEXConcept conceptA1 = aog.getFactory().createConcept("branchA 1", goDataSource, process, madeUp);
//		ONDEXConcept conceptA2 = aog.getFactory().createConcept("branchA 2", goDataSource, process, madeUp);
//		ONDEXConcept conceptA3 = aog.getFactory().createConcept("branchA 3", goDataSource, process, madeUp);
//		ONDEXConcept conceptAB4 = aog.getFactory().createConcept("branchAB 4", goDataSource, process, madeUp);
//
//		ONDEXConcept conceptA5 = aog.getFactory().createConcept("branchA 5", goDataSource, process, madeUp);
//
//		ONDEXRelation r1RootToA1 = aog.getFactory().createRelation(conceptA1, rootConcept, is_a, madeUp);
//		ONDEXRelation r1A1ToA2 = aog.getFactory().createRelation(conceptA2, conceptA1, is_a, madeUp);
//		ONDEXRelation r1A2ToA3 = aog.getFactory().createRelation(conceptA3, conceptA2, is_a, madeUp);
//		ONDEXRelation r1A3ToAB4 = aog.getFactory().createRelation(conceptAB4, conceptA3, is_a, madeUp);
//		ONDEXRelation r1A3ToA5 = aog.getFactory().createRelation(conceptA5, conceptA3, is_a, madeUp);
//
//
//		ONDEXConcept conceptB1 = aog.getFactory().createConcept("branchB 1", goDataSource, process, madeUp);
//		ONDEXConcept conceptB2 = aog.getFactory().createConcept("branchB 2", goDataSource, process, madeUp);
//
//		ONDEXRelation r1RootToB1 = aog.getFactory().createRelation(conceptB1, rootConcept, is_a, madeUp);
//		ONDEXRelation r1B1ToB2 = aog.getFactory().createRelation(conceptB2, conceptB1, is_a, madeUp);
//		ONDEXRelation r1B2ToAB4 = aog.getFactory().createRelation(conceptAB4, conceptB2, is_a, madeUp);
//
//		Set<RelationType> rt = new HashSet<RelationType>(1);
//		rt.add(is_a);
//
//		ReachabiltyDag rdag = new ReachabiltyDag(aog, process, rt, true);
//
//		Assert.assertEquals(1, rdag.getRoots().size());
//		Assert.assertEquals(rootConcept.getId(), rdag.getRoots().iterator().next().intValue());
//
//		Assert.assertEquals(2, rdag.getLeafs().size());
//
//		Iterator<Integer> it = rdag.getLeafs().iterator();
//		Assert.assertEquals(conceptAB4.getId(), it.next().intValue());
//		Assert.assertEquals(conceptA5.getId(), it.next().intValue());
//	}
//
//	/**
//	 *
//	 */
//	@Test
//	public void testGetRoots() throws Exception {
//
//		ONDEXConcept rootConcept = aog.getFactory().createConcept("root 1", goDataSource, process, madeUp);
//		ONDEXConcept rootConcept2 = aog.getFactory().createConcept("root 2", goDataSource, process, madeUp);
//
//		ONDEXConcept conceptA1 = aog.getFactory().createConcept("branchA 1", goDataSource, process, madeUp);
//		ONDEXConcept conceptA2 = aog.getFactory().createConcept("branchA 2", goDataSource, process, madeUp);
//		ONDEXConcept conceptA3 = aog.getFactory().createConcept("branchA 3", goDataSource, process, madeUp);
//		ONDEXConcept conceptAB4 = aog.getFactory().createConcept("branchAB 4", goDataSource, process, madeUp);
//
//		ONDEXConcept conceptA5 = aog.getFactory().createConcept("branchA 5", goDataSource, process, madeUp);
//
//		ONDEXRelation r1RootToA1 = aog.getFactory().createRelation(conceptA1, rootConcept, is_a, madeUp);
//		ONDEXRelation r5RootToA1 = aog.getFactory().createRelation(conceptA5, rootConcept2, is_a, madeUp);
//		ONDEXRelation r1A1ToA2 = aog.getFactory().createRelation(conceptA2, conceptA1, is_a, madeUp);
//		ONDEXRelation r1A2ToA3 = aog.getFactory().createRelation(conceptA3, conceptA2, is_a, madeUp);
//		ONDEXRelation r1A3ToAB4 = aog.getFactory().createRelation(conceptAB4, conceptA3, is_a, madeUp);
//		ONDEXRelation r1A3ToA5 = aog.getFactory().createRelation(conceptA5, conceptA3, is_a, madeUp);
//
//
//		ONDEXConcept conceptB1 = aog.getFactory().createConcept("branchB 1", goDataSource, process, madeUp);
//		ONDEXConcept conceptB2 = aog.getFactory().createConcept("branchB 2", goDataSource, process, madeUp);
//
//		ONDEXRelation r1RootToB1 = aog.getFactory().createRelation(conceptB1, rootConcept, is_a, madeUp);
//		ONDEXRelation r1B1ToB2 = aog.getFactory().createRelation(conceptB2, conceptB1, is_a, madeUp);
//		ONDEXRelation r1B2ToAB4 = aog.getFactory().createRelation(conceptAB4, conceptB2, is_a, madeUp);
//
//		Set<RelationType> rt = new HashSet<RelationType>(1);
//		rt.add(is_a);
//
//		ReachabiltyDag rdag = new ReachabiltyDag(aog, process, rt, true);
//
//		Assert.assertEquals(2, rdag.getRoots().size());
//		Assert.assertEquals(rootConcept.getId(), rdag.getRoots().iterator().next().intValue());
//
//		Assert.assertEquals(2, rdag.getLeafs().size());
//
//		Iterator<Integer> it = rdag.getLeafs().iterator();
//		Assert.assertEquals(conceptAB4.getId(), it.next().intValue());
//		Assert.assertEquals(conceptA5.getId(), it.next().intValue());
//	}
//
//	/**
//	 *
//	 */
//	@Test
//	public void testGetClosestCommonAncestor() throws Exception {
//		ONDEXConcept rootConcept = aog.getFactory().createConcept("root 1", goDataSource, process, madeUp);
//		ONDEXConcept rootConcept2 = aog.getFactory().createConcept("root 2", goDataSource, process, madeUp);
//
//		ONDEXConcept conceptA1 = aog.getFactory().createConcept("branchA 1", goDataSource, process, madeUp);
//		ONDEXConcept conceptA2 = aog.getFactory().createConcept("branchA 2", goDataSource, process, madeUp);
//		ONDEXConcept conceptA3 = aog.getFactory().createConcept("branchA 3", goDataSource, process, madeUp);
//		ONDEXConcept conceptAB4 = aog.getFactory().createConcept("branchAB 4", goDataSource, process, madeUp);
//
//		ONDEXConcept conceptA5 = aog.getFactory().createConcept("branchA 5", goDataSource, process, madeUp);
//
//		ONDEXRelation r1RootToA1 = aog.getFactory().createRelation(conceptA1, rootConcept, is_a, madeUp);
//		ONDEXRelation r5RootToA1 = aog.getFactory().createRelation(conceptA5, rootConcept2, is_a, madeUp);
//		ONDEXRelation r1A1ToA2 = aog.getFactory().createRelation(conceptA2, conceptA1, is_a, madeUp);
//		ONDEXRelation r1A2ToA3 = aog.getFactory().createRelation(conceptA3, conceptA2, is_a, madeUp);
//		ONDEXRelation r1A3ToAB4 = aog.getFactory().createRelation(conceptAB4, conceptA3, is_a, madeUp);
//		ONDEXRelation r1A3ToA5 = aog.getFactory().createRelation(conceptA5, conceptA3, is_a, madeUp);
//
//		ONDEXConcept conceptB1 = aog.getFactory().createConcept("branchB 1", goDataSource, process, madeUp);
//		ONDEXConcept conceptB2 = aog.getFactory().createConcept("branchB 2", goDataSource, process, madeUp);
//
//		ONDEXRelation r1RootToB1 = aog.getFactory().createRelation(conceptB1, rootConcept, is_a, madeUp);
//		ONDEXRelation r1B1ToB2 = aog.getFactory().createRelation(conceptB2, conceptB1, is_a, madeUp);
//		ONDEXRelation r1B2ToAB4 = aog.getFactory().createRelation(conceptAB4, conceptB2, is_a, madeUp);
//
//		Set<RelationType> rt = new HashSet<RelationType>(1);
//		rt.add(is_a);
//
//		ReachabiltyDag rdag = new ReachabiltyDag(aog, process, rt, true);
//
//		//CommonAncestor ancestor = rdag.getClosestCommonAncestors(conceptA5.getId(), conceptAB4.getId());
//	}
//
//	
}
