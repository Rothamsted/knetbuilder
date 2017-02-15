/**
 * 
 */
package net.sourceforge.ondex.tools.ondex;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author taubertj
 * 
 */
public class ONDEXGraphClonerTest extends TestCase {

	ONDEXGraph source = null;

	ONDEXGraph target = null;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		source = new MemoryONDEXGraph("source");
		assertNotNull(source);

		target = new MemoryONDEXGraph("target");
		assertNotNull(target);

		DataSource elementOf = source.getMetaData().createDataSource("id",
				"fullname", "description");
		assertNotNull(elementOf);

		ConceptClass ofType = source.getMetaData().createConceptClass("id",
				"fullname", "description", null);
		assertNotNull(ofType);

		EvidenceType et = source.getMetaData().createEvidenceType("id",
				"fullname", "description");
		assertNotNull(et);

		Collection<EvidenceType> evidence = new ArrayList<EvidenceType>();
		evidence.add(et);

		AttributeName attrname = source.getMetaData().createAttributeName("id",
				"fullname", "description", null, String.class, null);
		assertNotNull(attrname);

		ONDEXConcept fromConcept = source.createConcept("id1", "annotation1",
				"description1", elementOf, ofType, evidence);
		assertNotNull(fromConcept);

		Attribute attribute = fromConcept.createAttribute(attrname, "value",
				false);
		assertNotNull(attribute);

		ONDEXConcept toConcept = source.createConcept("id2", "annotation2",
				"description2", elementOf, ofType, evidence);
		assertNotNull(toConcept);

		RelationType rt = source.getMetaData().createRelationType("id",
				"fullname", "description", "inverseName", false, false, false,
				false, null);
		assertNotNull(rt);

		ONDEXRelation r1 = source.createRelation(fromConcept, toConcept, rt,
				evidence);
		assertNotNull(r1);

		attribute = r1.createAttribute(attrname, "value", false);
		assertNotNull(attribute);

		ONDEXRelation r2 = source.createRelation(toConcept, fromConcept, rt,
				evidence);
		assertNotNull(r2);

		attribute = r2.createAttribute(attrname, "value", false);
		assertNotNull(attribute);

		ONDEXGraphCloner.clearIndex();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		source = null;
		target = null;
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner#ONDEXGraphCloner(net.sourceforge.ondex.core.ONDEXGraph, net.sourceforge.ondex.core.ONDEXGraph)}
	 * .
	 */
	@Test
	public void testONDEXGraphCloner() {
		ONDEXGraphCloner cloner = new ONDEXGraphCloner(source, target);
		assertNotNull(cloner);
		assertTrue(cloner.getOld2newConceptIds().isEmpty());
		assertTrue(cloner.getOld2newRelationIds().isEmpty());
		assertEquals(target, cloner.getNewGraph());
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner#clearIndex()}.
	 */
	@Test
	public void testClearIndex() {
		ONDEXGraphCloner cloner = new ONDEXGraphCloner(source, target);
		assertNotNull(cloner);
		cloner.cloneAll();
		assertFalse(cloner.getOld2newConceptIds().isEmpty());
		assertFalse(cloner.getOld2newRelationIds().isEmpty());
		ONDEXGraphCloner.clearIndex();
		cloner = new ONDEXGraphCloner(source, target);
		assertNotNull(cloner);
		assertTrue(cloner.getOld2newConceptIds().isEmpty());
		assertTrue(cloner.getOld2newRelationIds().isEmpty());
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner#cloneMetaData()}
	 * .
	 */
	@Test
	public void testCloneMetaData() {
		ONDEXGraphCloner cloner = new ONDEXGraphCloner(source, target);
		assertNotNull(cloner);
		cloner.cloneMetaData();
		assertEquals(1, target.getMetaData().getAttributeNames().size());
		assertEquals(1, target.getMetaData().getConceptClasses().size());
		assertEquals(1, target.getMetaData().getDataSources().size());
		assertEquals(1, target.getMetaData().getEvidenceTypes().size());
		assertEquals(1, target.getMetaData().getRelationTypes().size());
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner#cloneAllConcepts()}
	 * .
	 */
	@Test
	public void testCloneAllConcepts() {
		ONDEXGraphCloner cloner = new ONDEXGraphCloner(source, target);
		assertNotNull(cloner);
		cloner.cloneAllConcepts();
		assertEquals(source.getConcepts().size(), target.getConcepts().size());
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner#cloneAllRelations()}
	 * .
	 */
	@Test
	public void testCloneAllRelations() {
		ONDEXGraphCloner cloner = new ONDEXGraphCloner(source, target);
		assertNotNull(cloner);
		cloner.cloneAllRelations();
		assertEquals(source.getRelations().size(), target.getRelations().size());
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner#cloneAll()}.
	 */
	@Test
	public void testCloneAll() {
		ONDEXGraphCloner cloner = new ONDEXGraphCloner(source, target);
		assertNotNull(cloner);
		cloner.cloneAll();
		assertEquals(1, target.getMetaData().getAttributeNames().size());
		assertEquals(1, target.getMetaData().getConceptClasses().size());
		assertEquals(1, target.getMetaData().getDataSources().size());
		assertEquals(1, target.getMetaData().getEvidenceTypes().size());
		assertEquals(1, target.getMetaData().getRelationTypes().size());
		assertEquals(source.getConcepts().size(), target.getConcepts().size());
		assertEquals(source.getRelations().size(), target.getRelations().size());
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner#getNewGraph()}.
	 */
	@Test
	public void testGetNewGraph() {
		ONDEXGraphCloner cloner = new ONDEXGraphCloner(source, target);
		assertNotNull(cloner);
		assertEquals(target, cloner.getNewGraph());
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner#cloneConcept(int)}
	 * .
	 */
	@Test
	public void testCloneConceptInt() {
		ONDEXGraphCloner cloner = new ONDEXGraphCloner(source, target);
		assertNotNull(cloner);
		cloner.cloneConcept(1);
		assertEquals(1, target.getConcepts().size());
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner#cloneConcept(net.sourceforge.ondex.core.ONDEXConcept)}
	 * .
	 */
	@Test
	public void testCloneConceptONDEXConcept() {
		ONDEXGraphCloner cloner = new ONDEXGraphCloner(source, target);
		assertNotNull(cloner);
		cloner.cloneConcept(source.getConcept(1));
		assertEquals(1, target.getConcepts().size());
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner#cloneRelation(int)}
	 * .
	 */
	@Test
	public void testCloneRelationInt() {
		ONDEXGraphCloner cloner = new ONDEXGraphCloner(source, target);
		assertNotNull(cloner);
		assertNotNull(source.getRelation(1));
		cloner.cloneRelation(1);
		assertEquals(1, target.getRelations().size());
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner#cloneRelation(net.sourceforge.ondex.core.ONDEXRelation)}
	 * .
	 */
	@Test
	public void testCloneRelationONDEXRelation() {
		ONDEXGraphCloner cloner = new ONDEXGraphCloner(source, target);
		assertNotNull(cloner);
		assertNotNull(source.getRelation(1));
		cloner.cloneRelation(source.getRelation(1));
		assertEquals(1, target.getRelations().size());
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner#getOld2newConceptIds()}
	 * .
	 */
	@Test
	public void testGetOld2newConceptIds() {
		ONDEXGraphCloner cloner = new ONDEXGraphCloner(source, target);
		assertNotNull(cloner);
		cloner.cloneAll();
		assertEquals(2, cloner.getOld2newConceptIds().size());
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner#getOld2newRelationIds()}
	 * .
	 */
	@Test
	public void testGetOld2newRelationIds() {
		ONDEXGraphCloner cloner = new ONDEXGraphCloner(source, target);
		assertNotNull(cloner);
		cloner.cloneAll();
		assertEquals(2, cloner.getOld2newRelationIds().size());
	}

}
