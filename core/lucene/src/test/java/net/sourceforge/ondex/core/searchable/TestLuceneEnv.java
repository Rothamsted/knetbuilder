package net.sourceforge.ondex.core.searchable;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.Set;

import junit.framework.TestCase;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.tools.DirUtils;

import org.apache.lucene.search.Query;

public class TestLuceneEnv extends TestCase {

	private MemoryONDEXGraph og;

	private DataSource dataSource;

	private ConceptClass cc;

	private EvidenceType et;

	private DataSource dataSource1;

	private ConceptClass cc1;

	// private EvidenceType et1;
	private File file;

	private LuceneEnv lenv;

	private RelationType rts;

	private AttributeName at;

	// private DataSource cv2;

	public TestLuceneEnv() {

	}

	@Override
	protected void setUp() {
		og = new MemoryONDEXGraph(this.getClass().getName());

		dataSource = og.getMetaData().getFactory().createDataSource("dataSource");
		dataSource1 = og.getMetaData().getFactory().createDataSource("dataSource1");
		// cv2 = og.getMetaData().createDataSource( "cv2");

		cc = og.getMetaData().getFactory().createConceptClass("cc");
		cc1 = og.getMetaData().getFactory().createConceptClass("cc1");

		et = og.getMetaData().getFactory().createEvidenceType("et");
		// et1 = og.getMetaData().createEvidenceType( "et1");

		rts = og.getMetaData().getFactory().createRelationType("bla");
		
		at = og.getMetaData().getFactory().createAttributeName("att", String.class);
		
		try {
			file = new File(File.createTempFile("lucene", "test").getParentFile().getAbsolutePath()+File.separator+"LuceneTest");
			System.out.println(file.getAbsolutePath());
		}catch (IOException e) {
			e.printStackTrace();
		}
		lenv = new LuceneEnv(file.getAbsolutePath(), true);
	}

	@Override
	protected void tearDown() {
		lenv.cleanup();
		lenv = null;
		try {
			DirUtils.deleteTree(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void testSearchConceptByConceptAccessionExact() {

		ONDEXConcept concept1 = og.getFactory().createConcept("A", dataSource, cc, et);
		concept1.createConceptAccession("ABC", dataSource, true);
		concept1.createConceptAccession("ABC", dataSource1, true); // we will try to match this
		concept1.createConceptAccession("BLA", dataSource1, false); // we will try to match this
		// one only

		ONDEXConcept concept2 = og.getFactory().createConcept("B", dataSource, cc1, et);
		concept2.createConceptAccession("ABC", dataSource1, true); // we will try to match this
		concept2.createConceptAccession("BLA", dataSource1, true); // we will try to match this
		// one only
		
		ONDEXConcept concept3 = og.getFactory().createConcept("C", dataSource, cc1, et);
		concept3.createConceptAccession("OTHER", dataSource, true); // we will try to match this
		concept3.createConceptAccession("OTHER2", dataSource1, true); // we will try to match this
		// one only

		lenv.setONDEXGraph(og);

		Query query = LuceneQueryBuilder.searchConceptByConceptAccessionExact(dataSource, "ABC", null, cc, true);
		Set<ONDEXConcept> results = lenv.searchInConcepts(query);
		assertEquals(1, results.size());
		Set<ONDEXConcept> ids = results;
		assertTrue(ids.contains(concept1));
		
		query = LuceneQueryBuilder.searchConceptByConceptAccessionExact(dataSource, "ABC", dataSource, cc, true);
		results = lenv.searchInConcepts(query);
		assertEquals(0, results.size());

		// match both this time
		query = LuceneQueryBuilder.searchConceptByConceptAccessionExact(dataSource1, "ABC", null, cc1, true);
		results = lenv.searchInConcepts(query);

		assertEquals(1, results.size());
		ids = results;
		assertTrue(ids.contains(concept2));
		
		// match both this time
		query = LuceneQueryBuilder.searchConceptByConceptAccessionExact(dataSource1, "ABC", null, null, true);
		results = lenv.searchInConcepts(query);
		assertEquals(2, results.size());
		
		// match one
		query = LuceneQueryBuilder.searchConceptByConceptAccessionExact(dataSource1, "BLA", null, null, false);
		results = lenv.searchInConcepts(query);
		assertEquals(1, results.size());
		ids = results;
		assertTrue(ids.contains(concept1));
		
		// match both this time
		query = LuceneQueryBuilder.searchConceptByConceptAccessionExact(dataSource1, "BLA", null, null, true);
		results = lenv.searchInConcepts(query);
		assertEquals(2, results.size());
	}
	
	public void testSearchConceptByConceptAttributeExact() {
		
		ONDEXConcept concept1 = og.getFactory().createConcept("A", dataSource, cc1, et);
		//concept1.createConceptName("AA", true);
		concept1.createAttribute(at, "mutant show an increased branching phenotype", true); // we will try to match this
		// one only

		lenv.setONDEXGraph(og);

		Query query = LuceneQueryBuilder.searchConceptByConceptAttributeExact(at, "branching");
		Set<ONDEXConcept> results = lenv.searchInConcepts(query);
		assertEquals(1, results.size());
		Set<ONDEXConcept> ids = results;
		assertTrue(ids.contains(concept1));
		
		query = LuceneQueryBuilder.searchConceptByConceptAttributeExact(at, "shoot");
		results = lenv.searchInConcepts(query);
		assertEquals(0, results.size());
		
	}
	
	public void test_removeRelationFromIndex() {
		ONDEXConcept concept1 = og.getFactory().createConcept("A", dataSource, cc, et);
		concept1.createConceptAccession("ABC", dataSource, true);
		ONDEXConcept concept2 = og.getFactory().createConcept("B", dataSource, cc1, et);
		concept2.createConceptAccession("ABC", dataSource, true);
		ONDEXConcept concept3 = og.getFactory().createConcept("C", dataSource, cc1, et);
		concept3.createConceptAccession("ABC", dataSource, true);
		
		ONDEXRelation r1 = og.getFactory().createRelation(concept1, concept2, rts, et);
		r1.createAttribute(at, "bla", true);
		ONDEXRelation r2 = og.getFactory().createRelation(concept2, concept3, rts, et);
		r2.createAttribute(at, "bla", true);
		ONDEXRelation r3 = og.getFactory().createRelation(concept1, concept3, rts, et);
		r3.createAttribute(at, "bla", true);
		
		lenv.setONDEXGraph(og);
		
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept1.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept2.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept3.getId()));
		
		assertTrue("relation not in index", lenv.relationExistsInIndex(r1.getId()));
		assertTrue("relation not in index", lenv.relationExistsInIndex(r2.getId()));
		assertTrue("relation not in index", lenv.relationExistsInIndex(r3.getId()));

		assertTrue("Unable to delete concept ", lenv.removeRelationFromIndex(r2.getId()));
		
		assertTrue(lenv.conceptExistsInIndex(concept1.getId()));
		assertTrue(lenv.conceptExistsInIndex(concept2.getId()));
		assertTrue(lenv.conceptExistsInIndex(concept3.getId()));
		
		assertTrue("relation not in index", lenv.relationExistsInIndex(r1.getId()));
		assertFalse("relation not in index", lenv.relationExistsInIndex(r2.getId()));
		assertTrue("relation not in index", lenv.relationExistsInIndex(r3.getId()));

	}

	public void test_removeRelationsFromIndex() {
		ONDEXConcept concept1 = og.getFactory().createConcept("A", dataSource, cc, et);
		concept1.createConceptAccession("ABC", dataSource, true);
		ONDEXConcept concept2 = og.getFactory().createConcept("B", dataSource, cc1, et);
		concept2.createConceptAccession("ABC", dataSource, true);
		ONDEXConcept concept3 = og.getFactory().createConcept("C", dataSource, cc1, et);
		concept3.createConceptAccession("ABC", dataSource, true);
		
		ONDEXRelation r1 = og.getFactory().createRelation(concept1, concept2, rts, et);
		r1.createAttribute(at, "bla", true);
		ONDEXRelation r2 = og.getFactory().createRelation(concept2, concept3, rts, et);
		r2.createAttribute(at, "bla", true);
		ONDEXRelation r3 = og.getFactory().createRelation(concept1, concept3, rts, et);
		r3.createAttribute(at, "bla", true);
		
		lenv.setONDEXGraph(og);
		
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept1.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept2.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept3.getId()));
		
		assertTrue("relation not in index", lenv.relationExistsInIndex(r1.getId()));
		assertTrue("relation not in index", lenv.relationExistsInIndex(r2.getId()));
		assertTrue("relation not in index", lenv.relationExistsInIndex(r3.getId()));

		assertTrue("Unable to delete concept ", lenv.removeRelationsFromIndex(new int []{r1.getId(), r2.getId()}));
		
		assertTrue(lenv.conceptExistsInIndex(concept1.getId()));
		assertTrue(lenv.conceptExistsInIndex(concept2.getId()));
		assertTrue(lenv.conceptExistsInIndex(concept3.getId()));
		
		assertFalse("relation not in index", lenv.relationExistsInIndex(r1.getId()));
		assertFalse("relation not in index", lenv.relationExistsInIndex(r2.getId()));
		assertTrue("relation not in index", lenv.relationExistsInIndex(r3.getId()));

	}

	public void test_removeConceptFromIndex() {
		ONDEXConcept concept1 = og.getFactory().createConcept("A", dataSource, cc, et);
		concept1.createConceptAccession("ABC", dataSource, true);
		ONDEXConcept concept2 = og.getFactory().createConcept("B", dataSource, cc1, et);
		concept2.createConceptAccession("ABC", dataSource, true);
		ONDEXConcept concept3 = og.getFactory().createConcept("C", dataSource, cc1, et);
		concept3.createConceptAccession("ABC", dataSource, true);
		
		System.out.println("index!!");
		lenv.setONDEXGraph(og);
		
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept1.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept2.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept3.getId()));
		
		assertTrue("Unable to delete concept ", lenv.removeConceptFromIndex(concept2.getId()));
		
		assertTrue(lenv.conceptExistsInIndex(concept1.getId()));
		assertFalse(lenv.conceptExistsInIndex(concept2.getId()));
		assertTrue(lenv.conceptExistsInIndex(concept3.getId()));
	}
	
	public void test_removeConceptsFromIndex() {
		ONDEXConcept concept1 = og.getFactory().createConcept("A", dataSource, cc, et);
		concept1.createConceptAccession("ABC", dataSource, true);
		ONDEXConcept concept2 = og.getFactory().createConcept("B", dataSource, cc1, et);
		concept2.createConceptAccession("ABC", dataSource, true);
		ONDEXConcept concept3 = og.getFactory().createConcept("C", dataSource, cc1, et);
		concept3.createConceptAccession("ABC", dataSource, true);
		
		System.out.println("index!!");
		lenv.setONDEXGraph(og);
		
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept1.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept2.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept3.getId()));
		
		assertTrue("Unable to delete concept ", lenv.removeConceptsFromIndex(new int []{concept1.getId(), concept2.getId()}));
		
		assertFalse(lenv.conceptExistsInIndex(concept1.getId()));
		assertFalse(lenv.conceptExistsInIndex(concept2.getId()));
		assertTrue(lenv.conceptExistsInIndex(concept3.getId()));
	}
	
	public void test_updateConceptToIndex() {
		ONDEXConcept concept1 = og.getFactory().createConcept("A", dataSource, cc, et);
		concept1.createConceptAccession("ABC", dataSource, true);
		ONDEXConcept concept2 = og.getFactory().createConcept("B", dataSource, cc1, et);
		concept2.createConceptAccession("ABC", dataSource, true);
		ONDEXConcept concept3 = og.getFactory().createConcept("C", dataSource, cc1, et);
		concept3.createConceptAccession("ABC", dataSource, true);
		
		System.out.println("index!!");
		lenv.setONDEXGraph(og);
		
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept1.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept2.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept3.getId()));
		
		ONDEXConcept concept4 = og.getFactory().createConcept("D", dataSource, cc1, et);
		concept4.createConceptAccession("ABC", dataSource, true);
		
		lenv.updateConceptToIndex(concept4);
		
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept1.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept2.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept3.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept4.getId()));
	}
	
	public void test_updateConceptsToIndex() {
		ONDEXConcept concept1 = og.getFactory().createConcept("A", dataSource, cc, et);
		concept1.createConceptAccession("ABC", dataSource, true);
		ONDEXConcept concept2 = og.getFactory().createConcept("B", dataSource, cc1, et);
		concept2.createConceptAccession("ABC", dataSource, true);
		ONDEXConcept concept3 = og.getFactory().createConcept("C", dataSource, cc1, et);
		concept3.createConceptAccession("ABC", dataSource, true);
		
		System.out.println("index!!");
		lenv.setONDEXGraph(og);
		
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept1.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept2.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept3.getId()));
		
		ONDEXConcept concept4 = og.getFactory().createConcept("D", dataSource, cc1, et);
		concept4.createConceptAccession("ABC", dataSource, true);
		ONDEXConcept concept5 = og.getFactory().createConcept("E", dataSource, cc1, et);
		concept5.createConceptAccession("ABC", dataSource, true);
		ONDEXConcept concept6 = og.getFactory().createConcept("F", dataSource, cc1, et);
		concept6.createConceptAccession("ABC", dataSource, true);
		
		BitSet bs = new BitSet();
		bs.set(concept4.getId());
		bs.set(concept5.getId());
		bs.set(concept6.getId());
		
		lenv.updateConceptsToIndex(BitSetFunctions.create(og, ONDEXConcept.class, bs));
		
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept1.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept2.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept3.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept4.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept5.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept6.getId()));
	}
	
	public void test_updateRelationToIndex() {
		ONDEXConcept concept1 = og.getFactory().createConcept("A", dataSource, cc, et);
		concept1.createConceptAccession("ABC", dataSource, true);
		ONDEXConcept concept2 = og.getFactory().createConcept("B", dataSource, cc1, et);
		concept2.createConceptAccession("ABC", dataSource, true);
		ONDEXConcept concept3 = og.getFactory().createConcept("C", dataSource, cc1, et);
		concept3.createConceptAccession("ABC", dataSource, true);
		
		ONDEXRelation r1 = og.getFactory().createRelation(concept1, concept2, rts, et);
		r1.createAttribute(at, "bla", true);
		ONDEXRelation r2 = og.getFactory().createRelation(concept2, concept3, rts, et);
		r2.createAttribute(at, "bla", true);
		ONDEXRelation r3 = og.getFactory().createRelation(concept1, concept3, rts, et);
		r3.createAttribute(at, "bla", true);
		
		lenv.setONDEXGraph(og);
		
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept1.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept2.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept3.getId()));
		
		assertTrue("relation not in index", lenv.relationExistsInIndex(r1.getId()));
		assertTrue("relation not in index", lenv.relationExistsInIndex(r2.getId()));
		assertTrue("relation not in index", lenv.relationExistsInIndex(r3.getId()));

		ONDEXRelation r4 = og.getFactory().createRelation(concept1, concept1, rts, et);
		r4.createAttribute(at, "bla", true);

		lenv.updateRelationToIndex(r4);
		
		assertTrue("relation not in index", lenv.relationExistsInIndex(r1.getId()));
		assertTrue("relation not in index", lenv.relationExistsInIndex(r2.getId()));
		assertTrue("relation not in index", lenv.relationExistsInIndex(r3.getId()));
		assertTrue("relation not in index", lenv.relationExistsInIndex(r4.getId()));
	}
	
	public void test_updateRelationsToIndex() {
		ONDEXConcept concept1 = og.getFactory().createConcept("A", dataSource, cc, et);
		concept1.createConceptAccession("ABC", dataSource, true);
		ONDEXConcept concept2 = og.getFactory().createConcept("B", dataSource, cc1, et);
		concept2.createConceptAccession("ABC", dataSource, true);
		ONDEXConcept concept3 = og.getFactory().createConcept("C", dataSource, cc1, et);
		concept3.createConceptAccession("ABC", dataSource, true);
		
		ONDEXRelation r1 = og.getFactory().createRelation(concept1, concept2, rts, et);
		r1.createAttribute(at, "bla", true);
		ONDEXRelation r2 = og.getFactory().createRelation(concept2, concept3, rts, et);
		r2.createAttribute(at, "bla", true);
		ONDEXRelation r3 = og.getFactory().createRelation(concept1, concept3, rts, et);
		r3.createAttribute(at, "bla", true);
		
		lenv.setONDEXGraph(og);
		
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept1.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept2.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept3.getId()));
		
		assertTrue("relation not in index", lenv.relationExistsInIndex(r1.getId()));
		assertTrue("relation not in index", lenv.relationExistsInIndex(r2.getId()));
		assertTrue("relation not in index", lenv.relationExistsInIndex(r3.getId()));

		ONDEXRelation r4 = og.getFactory().createRelation(concept1, concept1, rts, et);
		r4.createAttribute(at, "bla", true);
		ONDEXRelation r5 = og.getFactory().createRelation(concept2, concept2, rts, et);
		r5.createAttribute(at, "bla", true);
		ONDEXRelation r6 = og.getFactory().createRelation(concept3, concept3, rts, et);
		r6.createAttribute(at, "bla", true);

		BitSet bs = new BitSet();
		bs.set(r4.getId());
		bs.set(r5.getId());
		bs.set(r6.getId());
		
		lenv.updateRelationsToIndex(BitSetFunctions.create(og, ONDEXRelation.class, bs));
		
		assertTrue("relation not in index", lenv.relationExistsInIndex(r1.getId()));
		assertTrue("relation not in index", lenv.relationExistsInIndex(r2.getId()));
		assertTrue("relation not in index", lenv.relationExistsInIndex(r3.getId()));
		assertTrue("relation not in index", lenv.relationExistsInIndex(r4.getId()));
		assertTrue("relation not in index", lenv.relationExistsInIndex(r5.getId()));
		assertTrue("relation not in index", lenv.relationExistsInIndex(r6.getId()));
	}
}
