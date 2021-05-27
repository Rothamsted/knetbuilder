package net.sourceforge.ondex.core.searchable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.lucene.search.Query;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.logging.ONDEXLogger;
import net.sourceforge.ondex.tools.DirUtils;

public class LuceneEnvTest
{

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


	private Logger log = LoggerFactory.getLogger ( this.getClass () );

	
	public LuceneEnvTest() {

	}

	@Before
	public void setUp() throws IOException {
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
		
		// file = new File(File.createTempFile("lucene", "test").getParentFile().getAbsolutePath()+File.separator+"LuceneTest");
		String fpath = "target/lucene-env-test"; // + System.currentTimeMillis ();
		file = new File ( fpath );
		//System.out.println ( "Indexing on: " + fpath );
		if ( file.exists () ) DirUtils.deleteTree ( file );

		lenv = new LuceneEnv(file.getAbsolutePath(), true);
		lenv.addONDEXListener ( new ONDEXLogger () );
	}

	@After
	public void tearDown() throws IOException {
		lenv.closeAll();
		lenv = null;
		// NO! I might need this to debug!
		// DirUtils.deleteTree(file);
	}

	@Test
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
		assertTrue (ids.contains(concept1) );
		
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
	
	@Test
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
	
	@Test
	public void testRemoveRelationFromIndex() {
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
		assertFalse ("relation not in index", lenv.relationExistsInIndex(r2.getId()));
		assertTrue("relation not in index", lenv.relationExistsInIndex(r3.getId()));

	}

	@Test
	public void testRemoveRelationsFromIndex() {
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

	@Test
	public void testRemoveConceptFromIndex() {
		ONDEXConcept concept1 = og.getFactory().createConcept("A", dataSource, cc, et);
		concept1.createConceptAccession("ABC", dataSource, true);
		ONDEXConcept concept2 = og.getFactory().createConcept("B", dataSource, cc1, et);
		concept2.createConceptAccession("ABC", dataSource, true);
		ONDEXConcept concept3 = og.getFactory().createConcept("C", dataSource, cc1, et);
		concept3.createConceptAccession("ABC", dataSource, true);
		
		lenv.setONDEXGraph(og);
		
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept1.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept2.getId()));
		assertTrue("Concept not in index", lenv.conceptExistsInIndex(concept3.getId()));
		
		assertTrue("Unable to delete concept ", lenv.removeConceptFromIndex(concept2.getId()));
		
		assertTrue(lenv.conceptExistsInIndex(concept1.getId()));
		assertFalse(lenv.conceptExistsInIndex(concept2.getId()));
		assertTrue(lenv.conceptExistsInIndex(concept3.getId()));
	}
	
	@Test
	public void testRemoveConceptsFromIndex() {
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
	
	@Test
	public void testUpdateConceptToIndex ()
	{
		ONDEXConcept concept1 = og.getFactory ().createConcept ( "A", dataSource, cc, et );
		concept1.createConceptAccession ( "ABC", dataSource, true );
		ONDEXConcept concept2 = og.getFactory ().createConcept ( "B", dataSource, cc1, et );
		concept2.createConceptAccession ( "ABC", dataSource, true );
		ONDEXConcept concept3 = og.getFactory ().createConcept ( "C", dataSource, cc1, et );
		concept3.createConceptAccession ( "ABC", dataSource, true );

		System.out.println ( "index!!" );
		lenv.setONDEXGraph ( og );

		assertTrue ( "Concept not in index", lenv.conceptExistsInIndex ( concept1.getId () ) );
		assertTrue ( "Concept not in index", lenv.conceptExistsInIndex ( concept2.getId () ) );
		assertTrue ( "Concept not in index", lenv.conceptExistsInIndex ( concept3.getId () ) );

		ONDEXConcept concept4 = og.getFactory ().createConcept ( "D", dataSource, cc1, et );
		concept4.createConceptAccession ( "ABC", dataSource, true );

		lenv.updateConceptToIndex ( concept4 );

		assertTrue ( "Concept not in index", lenv.conceptExistsInIndex ( concept1.getId () ) );
		assertTrue ( "Concept not in index", lenv.conceptExistsInIndex ( concept2.getId () ) );
		assertTrue ( "Concept not in index", lenv.conceptExistsInIndex ( concept3.getId () ) );
		assertTrue ( "Concept not in index", lenv.conceptExistsInIndex ( concept4.getId () ) );
	}
	
	@Test
	public void testUpdateConceptsToIndex() {
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
	
	
	@Test
	public void testUpdateRelationToIndex() {
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
	
	@Test
	public void testUpdateRelationsToIndex() {
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
	
	@Test
	public void testIRISearch() 
	{
		String ns = "http://www.ondex.org/test/res/";
		
		AttributeName iria = og.getMetaData ().createAttributeName (
			"iri", "IRI", "IRI/URI of an entity", null, String.class, null
		);
		ONDEXConcept concept1 = og.getFactory().createConcept ( "A", dataSource, cc, et );
		concept1.createAttribute ( iria, ns + "concept:A", false );

		ONDEXConcept concept2 = og.getFactory().createConcept ( "B", dataSource, cc, et );
		concept2.createAttribute ( iria, ns + "concept:B", false );

		ONDEXRelation r1 = og.getFactory().createRelation(concept1, concept2, rts, et);
		r1.createAttribute ( iria, ns + "rel:AB", false );

		lenv.setONDEXGraph ( og );
		
		ONDEXConcept c1res = lenv.getConceptByIRI ( ns + "concept:A" );
		assertNotNull ( "Concept A not found!", c1res );
		assertEquals ( "Concept A different than expected!", concept1.getPID (), c1res.getPID () );
		
		ONDEXConcept c2res = lenv.getConceptByIRI ( ns + "concept:B" );
		assertNotNull ( "Concept B not found!", c2res );
		assertEquals ( "Concept B different than expected!", concept2.getPID (), c2res.getPID () );
		
		ONDEXRelation r1res = lenv.getRelationByIRI ( ns + "rel:AB" );
		assertNotNull ( "Relation not found!", c2res );
		assertEquals ( "Found Relation different than expected (type)!", r1.getOfType ().getId (), r1res.getOfType ().getId () );		
		assertEquals ( "Found Relation different than expected (from)!", r1.getFromConcept ().getPID (), r1.getFromConcept ().getPID () );		
		assertEquals ( "Found Relation different than expected (to)!", r1.getToConcept ().getPID (), r1.getToConcept ().getPID () );		
	}

	@Test
	public void testSearchByTypeAndAccession() throws Exception
	{
		var ccA = og.getMetaData ().createConceptClass ( "A", "Concept Type A", "", null );
		var ccB = og.getMetaData ().createConceptClass ( "B", "Concept Type B", "", null );
		
		ONDEXConcept concept1 = og.getFactory().createConcept ( "A1", dataSource, ccA, et );
		concept1.createConceptAccession ( "A:1", dataSource, false );
		concept1.createConceptAccession ( "A 2", dataSource, false );
		concept1.createConceptAccession ( "A 1", dataSource, false );
		concept1.createConceptAccession ( "A:3", dataSource1, false );
		concept1.createConceptAccession ( "3", dataSource1, false );
		concept1.createConceptAccession ( "A:42-Beta", dataSource1, false );

		
		ONDEXConcept concept2 = og.getFactory().createConcept ( "B1", dataSource, ccB, et );
		concept2.createConceptAccession ( "3", dataSource, false );
		concept2.createConceptAccession ( "X:5", dataSource, false );
		concept2.createConceptAccession ( "X 6", dataSource, false );
		concept2.createConceptAccession ( "cs1", dataSource1, false );

		ONDEXConcept concept3 = og.getFactory().createConcept ( "B2", dataSource, ccB, et );
		concept3.createConceptAccession ( "X:4", dataSource, false );
		concept3.createConceptAccession ( "X 7", dataSource, true );
		concept3.createConceptAccession ( "cS1", dataSource, false );

		
		lenv.setONDEXGraph ( og );
		
		var concepts = testSearchByTypeAndAccession ( "A", "A:1", 1, "Wrong no. of found concepts (basic)!" );
		assertEquals ( "Wrong fetched concept!", concept1.getPID (), concepts.iterator ().next ().getPID () );

		testSearchByTypeAndAccession ( "B", "X:*", 2, "Wrong no. of found concepts (wildcard)!" );
		testSearchByTypeAndAccession ( "A", "A 1", 1, "Wrong no. of found concepts (space)!" );
		testSearchByTypeAndAccession ( "B", "X ?", 2, "Wrong no. of found concepts (wildcard + space)!" );

		testSearchByTypeAndAccession ( "B", "X", 0, "Wrong no. of found concepts (exact matches)!" );
		
		testSearchByTypeAndAccession ( "B", "Cs1", false, 2, "Wrong no. of found concepts (case-insensitive)!" );
		testSearchByTypeAndAccession ( "B", "Cs*", false, 2, "Wrong no. of found concepts (case-insensitive + wildcard)!" );

		testSearchByTypeAndAccession ( "A", "42", 0, "Wrong no. of found concepts (partial match)!" );
		testSearchByTypeAndAccession ( "A", "foo-value", 0, "Wrong no. of found concepts (non-existing value)!" );
	}
	
	private Set<ONDEXConcept> testSearchByTypeAndAccession ( 
		String conceptClassId, String accessionTerm, boolean isCaseSensitive, int expectedResultSize,
		String errMsg
	)
	{
		Set<ONDEXConcept> concepts = lenv.searchByTypeAndAccession ( conceptClassId, accessionTerm, isCaseSensitive );
		assertEquals ( errMsg, expectedResultSize, concepts.size () );
		return concepts;
	}

	private Set<ONDEXConcept> testSearchByTypeAndAccession ( 
		String conceptClassId, String accessionTerm, int expectedResultSize, String errMsg
	)
	{
		return testSearchByTypeAndAccession ( conceptClassId, accessionTerm, true, expectedResultSize, errMsg );
	}
	
	
	
	@Test
	public void testSearchByTypeAndName() throws Exception
	{
		var ccA = og.getMetaData ().createConceptClass ( "A", "Concept Type A", "", null );
		var ccB = og.getMetaData ().createConceptClass ( "B", "Concept Type B", "", null );
		
		ONDEXConcept concept1 = og.getFactory().createConcept ( "A1", dataSource, ccA, et );
		concept1.createConceptName ( "A:1", true );
		concept1.createConceptName ( "A 2", false );
		concept1.createConceptName ( "A 1", false );
		concept1.createConceptName ( "A:3", false );
		concept1.createConceptName ( "3", false );
		
		ONDEXConcept concept2 = og.getFactory().createConcept ( "B1", dataSource, ccB, et );
		concept2.createConceptName ( "3", true );
		concept2.createConceptName ( "X:5", false );
		concept2.createConceptName ( "X 6", false );
		concept2.createConceptName ( "cs1", false );

		ONDEXConcept concept3 = og.getFactory().createConcept ( "B2", dataSource, ccB, et );
		concept3.createConceptName ( "X:4", true );
		concept3.createConceptName ( "X 7", false );
		concept3.createConceptName ( "cS1", false );

		
		lenv.setONDEXGraph ( og );
		
		var concepts = testSearchByTypeAndName ( "A", "A:1", 1, "Wrong no. of found concepts (basic)!" );
		assertEquals ( "Wrong fetched concept!", concept1.getPID (), concepts.iterator ().next ().getPID () );

		testSearchByTypeAndName ( "B", "X:*", 2, "Wrong no. of found concepts (wildcard)!" );
		testSearchByTypeAndName ( "A", "A 1", 1, "Wrong no. of found concepts (space)!" );
		testSearchByTypeAndName ( "B", "X ?", 2, "Wrong no. of found concepts (wildcard + space)!" );

		testSearchByTypeAndName ( "B", "X", 0, "Wrong no. of found concepts (exact matches)!" );
		
		testSearchByTypeAndName ( "B", "Cs1", false, 2, "Wrong no. of found concepts (case-insensitive)!" );
		testSearchByTypeAndName ( "B", "Cs*", false, 2, "Wrong no. of found concepts (case-insensitive + wildcard)!" );
	}
	
	private Set<ONDEXConcept> testSearchByTypeAndName ( 
		String conceptClassId, String accessionTerm, boolean isCaseSensitive, int expectedResultSize,
		String errMsg
	)
	{
		Set<ONDEXConcept> concepts = lenv.searchByTypeAndName ( conceptClassId, accessionTerm, isCaseSensitive );
		assertEquals ( errMsg, expectedResultSize, concepts.size () );
		return concepts;
	}

	private Set<ONDEXConcept> testSearchByTypeAndName ( 
		String conceptClassId, String accessionTerm, int expectedResultSize, String errMsg
	)
	{
		return testSearchByTypeAndName ( conceptClassId, accessionTerm, true, expectedResultSize, errMsg );
	}	
}
