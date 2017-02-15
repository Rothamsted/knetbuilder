package net.sourceforge.ondex.core.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.ONDEXEvent;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.ONDEXListener;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Common methods and variables for graph implementation tests.
 * 
 * @author taubertj
 */
public abstract class AbstractONDEXGraphTest extends TestCase {

	public class FailOnErrorLogger implements ONDEXListener {

		@Override
		public void eventOccurred(ONDEXEvent e) {
			Assert.fail(e.getEventType().getCompleteMessage());
		}
	}

	// constant for annotation
	private static String ANNO = "anno";

	// constant for concept class id
	private static String CCID = "cc_id";

	// constant for dataSource id
	private static String DataSourceID = "dataSource_id";

	// constant for description
	private static String DESC = "desc";

	// constant for evidence type id
	private static String ETID = "et_id";

	// constant for parser id
	private static String PID = "id";

	// ConceptClass used for testing
	private ConceptClass cc;

	// DataSource used for testing
	private DataSource dataSource;

	// EvidenceType used for testing
	private EvidenceType et;

	// Set of EvidenceTypes used for testing
	private Collection<EvidenceType> ets;

	// implementation of ONDEXGraph to test`
	private ONDEXGraph og;

	// associated ONDEXGraphMetaData
	private ONDEXGraphMetaData omd;

	// RelationType used for testing
	private RelationType rt;

	// AttributeName used for testing
	private AttributeName attrname;

	private void assertEvidenceTypeSetEquals(String message,
			Set<EvidenceType> ondexiterator, Collection<EvidenceType> ets) {
		if (ondexiterator.size() != ets.size()) {
			throw new AssertionError(message
					+ ": wrong evidences number in returned array expected "
					+ ets.size() + " found " + ondexiterator.size());
		}
		for (EvidenceType evidence : ondexiterator) {
			if (!ets.contains(evidence))
				throw new AssertionError(
						"incorrect evidences in returned array ");
		}
	}

	private void assertEvidenceTypeSetEquals(String message,
			Set<EvidenceType> ets, EvidenceType et) {
		if (ets.size() > 1) {
			throw new AssertionError("too many evidences in returned array");
		}
		if (ets.size() != 1) {
			throw new AssertionError("too few evidences in returned array");
		}
		if (!ets.contains(et)) {
			throw new AssertionError(
					"incorrect evidences in returned array found "
							+ ets.iterator().next().getId() + " expected "
							+ et.getId());
		}
	}

	/**
	 * Make sure that ONDEXGraph instance is teared down well.
	 */
	protected abstract void finalize() throws IOException;

	private ONDEXConcept getFromConcept(int i) {
		ONDEXConcept from = og.getFactory().createConcept("from" + i,
				dataSource, cc, et);
		Assert.assertNotNull(from);
		return from;
	}

	private ONDEXConcept getToConcept(int i) {
		ONDEXConcept to = og.getFactory().createConcept("to" + i, dataSource,
				cc, et);
		Assert.assertNotNull(to);
		return to;
	}

	/**
	 * Returns the ONDEXGraph implementation to test.
	 * 
	 * @param name
	 *            String
	 * @return ONDEXGraph
	 */
	protected abstract ONDEXGraph initialize(String name) throws IOException;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

		og = initialize("test_graph");
		Assert.assertNotNull(og);
		omd = og.getMetaData();
		Assert.assertNotNull(omd);
		et = omd.getFactory().createEvidenceType(ETID);
		Assert.assertNotNull(et);
		cc = omd.getFactory().createConceptClass(CCID);
		Assert.assertNotNull(cc);
		dataSource = omd.getFactory().createDataSource(DataSourceID);
		Assert.assertNotNull(dataSource);
		rt = omd.getFactory().createRelationType("rt");
		Assert.assertNotNull(rt);
		attrname = omd.getFactory().createAttributeName("double", Double.class);
		Assert.assertNotNull(attrname);

		ets = new ArrayList<EvidenceType>();
		ets.add(et);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		finalize();
		og = null;
	}

	@Test
	public void testConcepts() {
		boolean raised = false;
		try {
			og.getConcepts().clear();
		} catch (UnsupportedOperationException e) {
			raised = true;
		}
		assertTrue(
				"Concepts set must be immutable. UnsupportedOperationException must be raised.",
				raised);
	}

	@Test
	public void testCreateConceptStringDataSourceConceptClassCollectionOfEvidenceType() {

		ONDEXEventHandler.getEventHandlerForSID(og.getSID())
				.addONDEXONDEXListener(new FailOnErrorLogger());

		ONDEXConcept concept = og.getFactory().createConcept(PID, dataSource,
				cc, ets);
		Assert.assertNotNull("no concept created", concept);
		Assert.assertEquals("concept class wrong", CCID, concept.getOfType()
				.getId());
		Assert.assertEquals("dataSource wrong", DataSourceID, concept
				.getElementOf().getId());
		assertEvidenceTypeSetEquals("evidence type wrong",
				concept.getEvidence(), et);
	}

	@Test
	public void testCreateConceptStringDataSourceConceptClassEvidenceType() {

		ONDEXEventHandler.getEventHandlerForSID(og.getSID())
				.addONDEXONDEXListener(new FailOnErrorLogger());

		ONDEXConcept concept = og.getFactory().createConcept(PID, dataSource,
				cc, et);
		Assert.assertNotNull("no concept created", concept);
		Assert.assertEquals("concept class wrong", CCID, concept.getOfType()
				.getId());
		Assert.assertEquals("dataSource wrong", DataSourceID, concept
				.getElementOf().getId());
		assertEvidenceTypeSetEquals("evidence type wrong",
				concept.getEvidence(), et);
	}

	@Test
	public void testCreateConceptStringStringDataSourceConceptClassCollectionOfEvidenceType() {

		ONDEXEventHandler.getEventHandlerForSID(og.getSID())
				.addONDEXONDEXListener(new FailOnErrorLogger());

		ONDEXConcept concept = og.getFactory().createConcept(PID, ANNO,
				dataSource, cc, ets);
		Assert.assertNotNull("no concept created", concept);
		Assert.assertEquals("pid wrong", PID, concept.getPID());
		Assert.assertEquals("anno wrong", ANNO, concept.getAnnotation());
		Assert.assertEquals("concept class wrong", CCID, concept.getOfType()
				.getId());
		Assert.assertEquals("dataSource wrong", DataSourceID, concept
				.getElementOf().getId());
		assertEvidenceTypeSetEquals("evidence type wrong",
				concept.getEvidence(), et);
	}

	@Test
	public void testCreateConceptStringStringDataSourceConceptClassEvidenceType() {

		ONDEXEventHandler.getEventHandlerForSID(og.getSID())
				.addONDEXONDEXListener(new FailOnErrorLogger());

		ONDEXConcept concept = og.getFactory().createConcept(PID, ANNO,
				dataSource, cc, et);
		Assert.assertNotNull("no concept created", concept);
		Assert.assertEquals("pid wrong", PID, concept.getPID());
		Assert.assertEquals("anno wrong", ANNO, concept.getAnnotation());
		Assert.assertEquals("concept class wrong", CCID, concept.getOfType()
				.getId());
		Assert.assertEquals("dataSource wrong", DataSourceID, concept
				.getElementOf().getId());
		assertEvidenceTypeSetEquals("evidence type wrong",
				concept.getEvidence(), et);
	}

	@Test
	public void testCreateConceptStringStringStringDataSourceConceptClassCollectionOfEvidenceType() {

		ONDEXEventHandler.getEventHandlerForSID(og.getSID())
				.addONDEXONDEXListener(new FailOnErrorLogger());

		ONDEXConcept concept = og.createConcept(PID, ANNO, DESC, dataSource,
				cc, ets);
		Assert.assertNotNull("no concept created", concept);
		Assert.assertEquals("pid wrong", PID, concept.getPID());
		Assert.assertEquals("desc wrong", DESC, concept.getDescription());
		Assert.assertEquals("annp wrong", ANNO, concept.getAnnotation());
		Assert.assertEquals("concept class wrong", CCID, concept.getOfType()
				.getId());
		Assert.assertEquals("dataSource wrong", DataSourceID, concept
				.getElementOf().getId());
		assertEvidenceTypeSetEquals("evidence type wrong",
				concept.getEvidence(), et);
	}

	@Test
	public void testCreateConceptStringStringStringDataSourceConceptClassEvidenceType() {

		ONDEXEventHandler.getEventHandlerForSID(og.getSID())
				.addONDEXONDEXListener(new FailOnErrorLogger());

		ONDEXConcept concept = og.getFactory().createConcept(PID, ANNO, DESC,
				dataSource, cc, et);
		Assert.assertNotNull("no concept created", concept);
		Assert.assertEquals("pid wrong", PID, concept.getPID());
		Assert.assertEquals("desc wrong", DESC, concept.getDescription());
		Assert.assertEquals("anno wrong", ANNO, concept.getAnnotation());
		Assert.assertEquals("concept class wrong", CCID, concept.getOfType()
				.getId());
		Assert.assertEquals("dataSource wrong", DataSourceID, concept
				.getElementOf().getId());
		assertEvidenceTypeSetEquals("evidence type wrong",
				concept.getEvidence(), et);
	}

	@Test
	public void testCreateRelationONDEXConceptONDEXConceptRelationTypeCollectionOfEvidenceType() {

		ONDEXConcept from = getFromConcept(0);
		ONDEXConcept to = getToConcept(0);

		ONDEXRelation r = og.createRelation(from, to, rt, ets);
		Assert.assertNotNull(r);
		Assert.assertEquals("from concept wrong", from, r.getFromConcept());
		Assert.assertEquals("to concept wrong", to, r.getToConcept());
		Assert.assertEquals("from concept wrong in key", from.getId(), r
				.getKey().getFromID());
		Assert.assertEquals("to concept wrong in key", to.getId(), r.getKey()
				.getToID());
		assertEvidenceTypeSetEquals("evidence type wrong", r.getEvidence(), ets);
	}

	@Test
	public void testCreateRelationONDEXConceptONDEXConceptRelationTypeEvidenceType() {

		ONDEXConcept from = getFromConcept(0);
		ONDEXConcept to = getToConcept(0);

		ONDEXRelation r = og.getFactory().createRelation(from, to, rt, et);
		Assert.assertNotNull(r);
		Assert.assertEquals("from concept wrong", from, r.getFromConcept());
		Assert.assertEquals("to concept wrong", to, r.getToConcept());
		Assert.assertEquals("from concept wrong in key", from.getId(), r
				.getKey().getFromID());
		Assert.assertEquals("to concept wrong in key", to.getId(), r.getKey()
				.getToID());
		assertEvidenceTypeSetEquals("evidence type wrong", r.getEvidence(), et);
	}

	@Test
	public void testDeleteConcept() {

		testCreateConceptStringStringStringDataSourceConceptClassCollectionOfEvidenceType();
		Set<ONDEXConcept> conceptsAll = og.getConcepts();
		for (ONDEXConcept concept : conceptsAll) {
			Assert.assertNotNull(concept);
			Assert.assertNotNull("no concept deleted",
					og.deleteConcept(concept.getId()));
		}
		conceptsAll = og.getConcepts();
		Assert.assertEquals("concept/s where not deleted", 0,
				conceptsAll.size());
	}

	@Test
	public void testDeleteConceptWithAttributes() {

		testCreateConceptStringStringStringDataSourceConceptClassCollectionOfEvidenceType();
		Set<ONDEXConcept> conceptsAll = og.getConcepts();
		for (ONDEXConcept concept : conceptsAll) {
			Assert.assertNotNull(concept);
			concept.createAttribute(attrname, Math.random(), false);
			Assert.assertNotNull("no concept deleted",
					og.deleteConcept(concept.getId()));
		}
		conceptsAll = og.getConcepts();
		Assert.assertEquals("concept/s where not deleted", 0,
				conceptsAll.size());
	}

	@Test
	public void testDeleteRelationInt() {

		testCreateRelationONDEXConceptONDEXConceptRelationTypeEvidenceType();
		assertEquals("wrong number of relations", og.getRelations().size(), 1);

		testCreateRelationONDEXConceptONDEXConceptRelationTypeEvidenceType();
		assertEquals("wrong number of relations", og.getRelations().size(), 2);

		assertTrue("no relation deleted", og.deleteRelation(1));
		assertEquals("wrong number of relations", og.getRelations().size(), 1);

		assertTrue("no relation deleted", og.deleteRelation(2));
		assertEquals("wrong number of relations", og.getRelations().size(), 0);
	}

	@Test
	public void testDeleteRelationIntWithAttributes() {

		testCreateRelationONDEXConceptONDEXConceptRelationTypeEvidenceType();
		assertEquals("wrong number of relations", og.getRelations().size(), 1);

		testCreateRelationONDEXConceptONDEXConceptRelationTypeEvidenceType();
		assertEquals("wrong number of relations", og.getRelations().size(), 2);

		// add test attributes
		og.getRelation(1).createAttribute(attrname, Math.random(), false);
		og.getRelation(2).createAttribute(attrname, Math.random(), true);

		assertTrue("no relation deleted", og.deleteRelation(1));
		assertEquals("wrong number of relations", og.getRelations().size(), 1);

		assertTrue("no relation deleted", og.deleteRelation(2));
		assertEquals("wrong number of relations", og.getRelations().size(), 0);
	}

	@Test
	public void testDeleteRelationONDEXConceptONDEXConceptRelationType() {

		testCreateRelationONDEXConceptONDEXConceptRelationTypeEvidenceType();
		assertEquals("wrong number of relations", og.getRelations().size(), 1);

		ONDEXRelation r = og.getRelation(1);
		assertNotNull("null relation", r);
		assertNotNull("null from concept", r.getFromConcept());
		assertNotNull("null to concept", r.getToConcept());
		assertNotNull("null relation type", r.getOfType());

		assertTrue(
				"no relation deleted",
				og.deleteRelation(r.getFromConcept(), r.getToConcept(),
						r.getOfType()));
		assertEquals("wrong number of relations", og.getRelations().size(), 0);
	}

	@Test
	public void testGetConcept() {

		testCreateConceptStringStringStringDataSourceConceptClassCollectionOfEvidenceType();
		Integer cid = null;
		Set<ONDEXConcept> conceptsAll = og.getConcepts();
		for (ONDEXConcept concept : conceptsAll) {
			cid = concept.getId();
			break;
		}
		Assert.assertNotNull("no concept created", cid);

		ONDEXConcept concept = og.getConcept(cid);
		Assert.assertNotNull("no concept created", concept);
		Assert.assertEquals("pid wrong", PID, concept.getPID());
		Assert.assertEquals("desc wrong", DESC, concept.getDescription());
		Assert.assertEquals("annp wrong", ANNO, concept.getAnnotation());
		Assert.assertEquals("concept class wrong", CCID, concept.getOfType()
				.getId());
		Assert.assertEquals("dataSource wrong", DataSourceID, concept
				.getElementOf().getId());
		assertEvidenceTypeSetEquals("evidence type wrong",
				concept.getEvidence(), et);
	}

	@Test
	public void testGetConcepts() {

		ONDEXEventHandler.getEventHandlerForSID(og.getSID())
				.addONDEXONDEXListener(new FailOnErrorLogger());

		Map<Integer, Integer> ids = new HashMap<Integer, Integer>();
		for (int i = 1; i <= 50; i++) {
			ConceptClass cc = omd.getFactory().createConceptClass(CCID + i);
			DataSource dataSource = omd.getFactory().createDataSource(
					DataSourceID + i);
			EvidenceType et = omd.getFactory().createEvidenceType(ETID + i);

			ArrayList<EvidenceType> al = new ArrayList<EvidenceType>();
			al.add(et);

			ONDEXConcept concept = og.createConcept(PID + i, ANNO + i,
					DESC + i, dataSource, cc, al);
			ids.put(concept.getId(), i);
			Assert.assertNotNull("no concept created", concept);
			Assert.assertEquals("pid wrong", PID + i, concept.getPID());
			Assert.assertEquals("desc wrong", DESC + i,
					concept.getDescription());
			Assert.assertEquals("anno wrong", ANNO + i, concept.getAnnotation());
			Assert.assertEquals("concept class wrong", CCID + i, concept
					.getOfType().getId());
			Assert.assertEquals("dataSource wrong", DataSourceID + i, concept
					.getElementOf().getId());
			assertEvidenceTypeSetEquals("evidence type wrong",
					concept.getEvidence(), et);
			assertEvidenceTypeSetEquals("evidence type wrong",
					concept.getEvidence(), et);
		}
		Set<ONDEXConcept> concepts = og.getConcepts();
		Assert.assertEquals("not enough concepts returned", 50, concepts.size());

		int count = 0;
		for (ONDEXConcept concept : concepts) {
			int i = ids.get(concept.getId());
			count++;
			Assert.assertNotNull("no concept found", concept);
			Assert.assertEquals("pid wrong", PID + i, concept.getPID());
			Assert.assertEquals("desc wrong", DESC + i,
					concept.getDescription());
			Assert.assertEquals("anno wrong", ANNO + i, concept.getAnnotation());
			Assert.assertEquals("concept class wrong", CCID + i, concept
					.getOfType().getId());
			Assert.assertEquals("dataSource wrong", DataSourceID + i, concept
					.getElementOf().getId());
			assertEvidenceTypeSetEquals("evidence type wrong",
					concept.getEvidence(), omd.getEvidenceType(ETID + i));
		}
		Assert.assertEquals("not enough concepts interated through", 50, count);

	}

	@Test
	public void testGetConceptsOfAttributeName() {

		AttributeName attA = omd.getFactory().createAttributeName("attA",
				String.class);
		AttributeName attB = omd.getFactory().createAttributeName("attB",
				Long.class);

		for (int i = 1; i <= 50; i++) {
			ONDEXConcept concept = og.createConcept(PID + i, ANNO + i,
					DESC + i, dataSource, cc, ets);
			Assert.assertNotNull("no concept created", concept);
			Assert.assertEquals("pid wrong", PID + i, concept.getPID());
			Assert.assertEquals("desc wrong", DESC + i,
					concept.getDescription());
			Assert.assertEquals("anno wrong", ANNO + i, concept.getAnnotation());
			Assert.assertEquals("concept class wrong", cc.getId(), concept
					.getOfType().getId());
			Assert.assertEquals("dataSource wrong", dataSource.getId(), concept
					.getElementOf().getId());
			assertEvidenceTypeSetEquals("evidence type wrong",
					concept.getEvidence(), et);
			Assert.assertNotNull(concept.createAttribute(attA, "MySTRING",
					false));
		}
		for (int i = 1; i <= 40; i++) {
			ONDEXConcept concept = og.createConcept(PID + i + "B", ANNO + i
					+ "B", DESC + i + "B", dataSource, cc, ets);
			Assert.assertNotNull("no concept created", concept);
			Assert.assertEquals("pid wrong", PID + i + "B", concept.getPID());
			Assert.assertEquals("desc wrong", DESC + i + "B",
					concept.getDescription());
			Assert.assertEquals("anno wrong", ANNO + i + "B",
					concept.getAnnotation());
			Assert.assertEquals("concept class wrong", cc.getId(), concept
					.getOfType().getId());
			Assert.assertEquals("dataSource wrong", dataSource.getId(), concept
					.getElementOf().getId());
			assertEvidenceTypeSetEquals("evidence type wrong",
					concept.getEvidence(), et);
			Assert.assertNotNull(concept.createAttribute(attB,
					Long.valueOf(1000), false));
		}

		Set<ONDEXConcept> concepts = og.getConceptsOfAttributeName(attA);
		Assert.assertEquals("not enough concepts returned", 50, concepts.size());

		int count = 0;
		for (ONDEXConcept concept : concepts) {
			count++;
			Assert.assertNotNull(concept.getAttribute(attA));
		}
		Assert.assertEquals("not enough concepts interated through", 50, count);

		concepts = og.getConceptsOfAttributeName(attB);
		Assert.assertEquals("not enough concepts returned", 40, concepts.size());

		count = 0;
		for (ONDEXConcept concept : concepts) {
			count++;
			Assert.assertNotNull(concept.getAttribute(attB));
		}
		Assert.assertEquals("not enough concepts interated through", 40, count);
	}

	@Test
	public void testGetConceptsOfConceptClass() {

		ConceptClass cca = omd.getFactory().createConceptClass(CCID + "A");
		ConceptClass ccb = omd.getFactory().createConceptClass(CCID + "B");

		for (int i = 1; i <= 50; i++) {

			ONDEXConcept concept = og.createConcept(PID + i, ANNO + i,
					DESC + i, dataSource, cca, ets);
			Assert.assertNotNull("no concept created", concept);
			Assert.assertEquals("pid wrong", PID + i, concept.getPID());
			Assert.assertEquals("desc wrong", DESC + i,
					concept.getDescription());
			Assert.assertEquals("anno wrong", ANNO + i, concept.getAnnotation());
			Assert.assertEquals("concept class wrong", cca.getId(), concept
					.getOfType().getId());
			Assert.assertEquals("dataSource wrong", dataSource.getId(), concept
					.getElementOf().getId());
			assertEvidenceTypeSetEquals("evidence type wrong",
					concept.getEvidence(), et);
			assertEvidenceTypeSetEquals("evidence type wrong",
					concept.getEvidence(), et);
		}
		for (int i = 1; i <= 40; i++) {

			ONDEXConcept concept = og.createConcept(PID + i + "B", ANNO + i
					+ "B", DESC + i + "B", dataSource, ccb, ets);
			Assert.assertNotNull("no concept created", concept);
			Assert.assertEquals("pid wrong", PID + i + "B", concept.getPID());
			Assert.assertEquals("desc wrong", DESC + i + "B",
					concept.getDescription());
			Assert.assertEquals("anno wrong", ANNO + i + "B",
					concept.getAnnotation());
			Assert.assertEquals("concept class wrong", ccb.getId(), concept
					.getOfType().getId());
			Assert.assertEquals("dataSource wrong", dataSource.getId(), concept
					.getElementOf().getId());
			assertEvidenceTypeSetEquals("evidence type wrong",
					concept.getEvidence(), et);
			assertEvidenceTypeSetEquals("evidence type wrong",
					concept.getEvidence(), et);
		}

		Set<ONDEXConcept> concepts = og.getConceptsOfConceptClass(cca);
		Assert.assertEquals("not enough concepts returned", 50, concepts.size());

		int count = 0;
		for (ONDEXConcept concept : concepts) {
			count++;
			Assert.assertEquals("concept class wrong", cca.getId(), concept
					.getOfType().getId());
		}
		Assert.assertEquals("not enough concepts interated through", 50, count);

		concepts = og.getConceptsOfConceptClass(ccb);
		Assert.assertEquals("not enough concepts returned", 40, concepts.size());

		count = 0;
		for (ONDEXConcept concept : concepts) {
			count++;
			Assert.assertNotNull("no concept found", concept);
			Assert.assertEquals("concept class wrong", ccb.getId(), concept
					.getOfType().getId());
		}
		Assert.assertEquals("not enough concepts interated through", 40, count);
	}

	@Test
	public void testGetConceptsOfDataSource() {

		HashSet<ONDEXConcept> dataSourcea_set = new HashSet<ONDEXConcept>();
		HashSet<ONDEXConcept> dataSourceb_set = new HashSet<ONDEXConcept>();

		DataSource dataSourcea = omd.getFactory().createDataSource(
				DataSourceID + "A");
		DataSource dataSourceb = omd.getFactory().createDataSource(
				DataSourceID + "B");

		// create concepts for DataSource A
		for (int i = 1; i <= 50; i++) {
			ConceptClass cc = omd.getFactory().createConceptClass(CCID + i);
			EvidenceType et = omd.getFactory().createEvidenceType(ETID + i);

			ArrayList<EvidenceType> al = new ArrayList<EvidenceType>();
			al.add(et);

			ONDEXConcept concept = og.createConcept(PID + i, ANNO + i,
					DESC + i, dataSourcea, cc, al);
			Assert.assertNotNull("no concept created", concept);
			Assert.assertEquals("pid wrong", PID + i, concept.getPID());
			Assert.assertEquals("desc wrong", DESC + i,
					concept.getDescription());
			Assert.assertEquals("anno wrong", ANNO + i, concept.getAnnotation());
			Assert.assertEquals("concept class wrong", CCID + i, concept
					.getOfType().getId());
			Assert.assertEquals("dataSource wrong", DataSourceID + "A", concept
					.getElementOf().getId());
			assertEvidenceTypeSetEquals("evidence type wrong",
					concept.getEvidence(), et);
			assertEvidenceTypeSetEquals("evidence type wrong",
					concept.getEvidence(), et);
			dataSourcea_set.add(concept);
		}

		// create concepts for DataSource B
		for (int i = 1; i <= 40; i++) {
			ConceptClass cc = omd.getFactory().createConceptClass(
					CCID + i + "B");
			EvidenceType et = omd.getFactory().createEvidenceType(
					ETID + i + "B");

			ArrayList<EvidenceType> al = new ArrayList<EvidenceType>();
			al.add(et);

			ONDEXConcept concept = og.createConcept(PID + i + "B", ANNO + i
					+ "B", DESC + i + "B", dataSourceb, cc, al);
			Assert.assertNotNull("no concept created", concept);
			Assert.assertEquals("pid wrong", PID + i + "B", concept.getPID());
			Assert.assertEquals("desc wrong", DESC + i + "B",
					concept.getDescription());
			Assert.assertEquals("anno wrong", ANNO + i + "B",
					concept.getAnnotation());
			Assert.assertEquals("concept class wrong", CCID + i + "B", concept
					.getOfType().getId());
			Assert.assertEquals("dataSource wrong", dataSourceb.getId(),
					concept.getElementOf().getId());
			assertEvidenceTypeSetEquals("evidence type wrong",
					concept.getEvidence(), et);
			assertEvidenceTypeSetEquals("evidence type wrong",
					concept.getEvidence(), et);
			dataSourceb_set.add(concept);
		}

		// check concepts of DataSource A
		Set<ONDEXConcept> concepts = og.getConceptsOfDataSource(dataSourcea);
		Assert.assertEquals("not enough concepts returned", 50, concepts.size());

		Assert.assertEquals("concepts lost", dataSourcea_set.size(),
				concepts.size());

		int count = 0;
		for (ONDEXConcept concept : concepts) {
			count++;
			Assert.assertNotNull("no concept found", concept);
			Assert.assertEquals("concept is part of wrong DataSource",
					dataSourcea, concept.getElementOf());
			Assert.assertTrue("concept is not part of correct set",
					dataSourcea_set.contains(concept));
			// Assert.assertEquals("pid wrong", PID + count, concept.getPID());
			// Assert.assertEquals("desc wrong", DESC + count, concept
			// .getDescription());
			// Assert.assertEquals("anno wrong", ANNO + count, concept
			// .getAnnotation());
			// Assert.assertEquals("concept class wrong", CCID + count, concept
			// .getOfType().getId());
			// Assert.assertEquals("dataSource wrong", dataSourcea.getId(),
			// concept.getElementOf()
			// .getId());
			// assertEvidenceTypeSetEquals("evidence type wrong", concept
			// .getEvidence(), omd.getFactory().getEvidenceType(ETID + count));
		}
		Assert.assertEquals("not enough concepts interated through", 50, count);

		// check concepts for DataSource B
		concepts = og.getConceptsOfDataSource(dataSourceb);
		Assert.assertEquals("not enough concepts returned", 40, concepts.size());

		Assert.assertEquals("concepts lost", dataSourceb_set.size(),
				concepts.size());

		count = 0;
		for (ONDEXConcept concept : concepts) {
			count++;
			Assert.assertNotNull("no concept found", concept);
			Assert.assertEquals("concept is part of wrong DataSource",
					dataSourceb, concept.getElementOf());
			Assert.assertTrue("concept is not part of correct set",
					dataSourceb_set.contains(concept));

			// Assert.assertEquals("pid wrong", PID + count + "B", concept
			// .getPID());
			// Assert.assertEquals("desc wrong", DESC + count + "B", concept
			// .getDescription());
			// Assert.assertEquals("anno wrong", ANNO + count + "B", concept
			// .getAnnotation());
			// Assert.assertEquals("concept class wrong", CCID + count + "B",
			// concept.getOfType().getId());
			// Assert.assertEquals("dataSource wrong", dataSourceb.getId(),
			// concept.getElementOf()
			// .getId());
			// assertEvidenceTypeSetEquals("evidence type wrong", concept
			// .getEvidence(), omd.getFactory().getEvidenceType(ETID + count));
		}
		Assert.assertEquals("not enough concepts interated through", 40, count);
	}

	@Test
	public void testGetConceptsOfEvidenceType() {

		EvidenceType etA = omd.getFactory().createEvidenceType(ETID + "A");
		EvidenceType etB = omd.getFactory().createEvidenceType(ETID + "B");

		ArrayList<EvidenceType> alA = new ArrayList<EvidenceType>();
		alA.add(etA);

		ArrayList<EvidenceType> alB = new ArrayList<EvidenceType>();
		alB.add(etB);

		ArrayList<EvidenceType> alMix = new ArrayList<EvidenceType>();
		alMix.add(etA);
		alMix.add(etB);

		for (int i = 1; i <= 50; i++) {

			ONDEXConcept concept = og.createConcept(PID + i, ANNO + i,
					DESC + i, dataSource, cc, alA);
			Assert.assertNotNull("no concept created", concept);
			Assert.assertEquals("pid wrong", PID + i, concept.getPID());
			Assert.assertEquals("desc wrong", DESC + i,
					concept.getDescription());
			Assert.assertEquals("anno wrong", ANNO + i, concept.getAnnotation());
			Assert.assertEquals("concept class wrong", cc.getId(), concept
					.getOfType().getId());
			Assert.assertEquals("dataSource wrong", dataSource.getId(), concept
					.getElementOf().getId());
			assertEvidenceTypeSetEquals("evidence type wrong",
					concept.getEvidence(), alA);
		}
		for (int i = 1; i <= 40; i++) {

			ONDEXConcept concept = og.createConcept(PID + i + "B", ANNO + i
					+ "B", DESC + i + "B", dataSource, cc, alB);
			Assert.assertNotNull("no concept created", concept);
			Assert.assertEquals("pid wrong", PID + i + "B", concept.getPID());
			Assert.assertEquals("desc wrong", DESC + i + "B",
					concept.getDescription());
			Assert.assertEquals("anno wrong", ANNO + i + "B",
					concept.getAnnotation());
			Assert.assertEquals("concept class wrong", cc.getId(), concept
					.getOfType().getId());
			Assert.assertEquals("dataSource wrong", dataSource.getId(), concept
					.getElementOf().getId());
			assertEvidenceTypeSetEquals("evidence type wrong",
					concept.getEvidence(), alB);
		}
		for (int i = 1; i <= 10; i++) {
			ONDEXConcept concept = og.createConcept(PID + i + "B", ANNO + i
					+ "B", DESC + i + "B", dataSource, cc, alMix);
			Assert.assertNotNull("no concept created", concept);
			Assert.assertEquals("pid wrong", PID + i + "B", concept.getPID());
			Assert.assertEquals("desc wrong", DESC + i + "B",
					concept.getDescription());
			Assert.assertEquals("anno wrong", ANNO + i + "B",
					concept.getAnnotation());
			Assert.assertEquals("concept class wrong", cc.getId(), concept
					.getOfType().getId());
			Assert.assertEquals("dataSource wrong", dataSource.getId(), concept
					.getElementOf().getId());
			assertEvidenceTypeSetEquals("evidence type wrong",
					concept.getEvidence(), alMix);
		}

		Set<ONDEXConcept> conceptsA = og.getConceptsOfEvidenceType(etA);
		Assert.assertEquals("not enough concepts returned", 60,
				conceptsA.size());

		int count = 0;
		for (ONDEXConcept concept : conceptsA) {
			count++;
			Assert.assertTrue("evidence type wrong", concept.getEvidence()
					.contains(etA));
		}
		Assert.assertEquals("not enough concepts interated through", 60, count);

		Set<ONDEXConcept> conceptsB = og.getConceptsOfEvidenceType(etB);
		Assert.assertEquals("not enough concepts returned", 50,
				conceptsB.size());

		count = 0;
		for (ONDEXConcept concept : conceptsB) {
			count++;
			Assert.assertTrue("evidence type wrong", concept.getEvidence()
					.contains(etB));
		}
		Assert.assertEquals("not enough concepts interated through", 50, count);

		Set<ONDEXConcept> conceptsTogether = BitSetFunctions.and(conceptsA,
				conceptsB);
		Assert.assertEquals("wrong number of concepts", 10,
				conceptsTogether.size());

		count = 0;
		for (ONDEXConcept concept : conceptsTogether) {
			count++;
			assertEvidenceTypeSetEquals("evidence type wrong",
					concept.getEvidence(), alMix);
		}
		Assert.assertEquals("not enough concepts interated through", 10, count);

	}

	@Test
	public void testGetConceptsOfTag() {
		ONDEXConcept c = getFromConcept(0);
		for (int i = 1; i <= 50; i++) {
			ONDEXConcept concept = getFromConcept(i);
			concept.addTag(c);
		}
		assertEquals("wrong number of concepts for tag", 50, og
				.getConceptsOfTag(c).size());
	}

	@Test
	public void testGetFactory() {
		assertNotNull("Factory not initialized", og.getFactory());
	}

	@Test
	public void testGetMetaData() {

		Assert.assertNotNull("AbstractONDEXGraphMetaData not created",
				og.getMetaData());
	}

	@Test
	public void testGetName() {

		Assert.assertEquals("name not set", "test_graph", og.getName());
	}

	@Test
	public void testGetRelationInt() {

		testCreateRelationONDEXConceptONDEXConceptRelationTypeEvidenceType();
		assertEquals("wrong number of relations", og.getRelations().size(), 1);

		assertNotNull("no relation found for id", og.getRelation(1));
	}

	@Test
	public void testGetRelationONDEXConceptONDEXConceptRelationType() {

		testCreateRelationONDEXConceptONDEXConceptRelationTypeEvidenceType();
		assertEquals("wrong number of relations", og.getRelations().size(), 1);

		ONDEXRelation r = og.getRelation(1);
		assertNotNull("null relation", r);
		assertNotNull("null from concept", r.getFromConcept());
		assertNotNull("null to concept", r.getToConcept());
		assertNotNull("null relation type", r.getOfType());

		ONDEXRelation r2 = og.getRelation(r.getFromConcept(), r.getToConcept(),
				r.getOfType());
		assertNotNull("null relation", r2);
		assertEquals("not same relations returned", r, r2);
	}

	@Test
	public void testGetRelations() {

		assertEquals("wrong number of relations", og.getRelations().size(), 0);

		for (int i = 0; i < 50; i++) {
			ONDEXConcept from = getFromConcept(i);
			ONDEXConcept to = getToConcept(i);

			ONDEXRelation r = og.getFactory().createRelation(from, to, rt, et);
			Assert.assertNotNull(r);
		}

		assertEquals("wrong number of relations", og.getRelations().size(), 50);
	}

	@Test
	public void testGetRelationsOfAttributeName() {

		assertEquals("wrong number of relations", og.getRelations().size(), 0);

		AttributeName doubleAn = og.getMetaData().getFactory()
				.createAttributeName("double", Double.class);
		assertNotNull("null attribute name", doubleAn);

		AttributeName stringAn = og.getMetaData().getFactory()
				.createAttributeName("string", String.class);
		assertNotNull("null attribute name", stringAn);

		for (int i = 0; i < 50; i++) {
			ONDEXConcept from = getFromConcept(i);
			ONDEXConcept to = getToConcept(i);

			ONDEXRelation r = og.getFactory().createRelation(from, to, rt, et);
			Assert.assertNotNull(r);

			if (i < 20)
				assertNotNull("null relation gds",
						r.createAttribute(doubleAn, Double.valueOf(i), false));
			else
				assertNotNull("null relation gds",
						r.createAttribute(stringAn, String.valueOf(i), false));
		}

		assertEquals("wrong number of relations", og
				.getRelationsOfAttributeName(doubleAn).size(), 20);
		assertEquals("wrong number of relations", og
				.getRelationsOfAttributeName(stringAn).size(), 30);
	}

	@Test
	public void testGetRelationsOfConcept() {

		assertEquals("wrong number of relations", og.getRelations().size(), 0);

		Map<ONDEXConcept, ONDEXRelation> map = new HashMap<ONDEXConcept, ONDEXRelation>();

		for (int i = 0; i < 50; i++) {
			ONDEXConcept from = getFromConcept(i);
			ONDEXConcept to = getToConcept(i);

			ONDEXRelation r = og.getFactory().createRelation(from, to, rt, et);
			Assert.assertNotNull(r);
			map.put(from, r);
			map.put(to, r);
		}

		assertEquals("wrong number of relations", og.getRelations().size(), 50);

		for (ONDEXConcept c : map.keySet()) {
			assertEquals("wrong number of relations",
					og.getRelationsOfConcept(c).size(), 1);
			assertEquals("not equal relation", og.getRelationsOfConcept(c)
					.iterator().next(), map.get(c));
		}
	}

	@Test
	public void testGetRelationsOfConceptClass() {

		assertEquals("wrong number of relations", og.getRelations().size(), 0);

		for (int i = 0; i < 50; i++) {
			ONDEXConcept from = getFromConcept(i);
			ONDEXConcept to = getToConcept(i);

			ONDEXRelation r = og.getFactory().createRelation(from, to, rt, et);
			Assert.assertNotNull(r);
		}

		assertEquals("wrong number of relations", og.getRelations().size(), 50);
		assertEquals("wrong number of relations", og
				.getRelationsOfConceptClass(cc).size(), 50);
	}

	@Test
	public void testGetRelationsOfDataSource() {

		assertEquals("wrong number of relations", og.getRelations().size(), 0);

		for (int i = 0; i < 50; i++) {
			ONDEXConcept from = getFromConcept(i);
			ONDEXConcept to = getToConcept(i);

			ONDEXRelation r = og.getFactory().createRelation(from, to, rt, et);
			Assert.assertNotNull(r);
		}

		assertEquals("wrong number of relations", og.getRelations().size(), 50);
		assertEquals("wrong number of relations",
				og.getRelationsOfDataSource(dataSource).size(), 50);
	}

	@Test
	public void testGetRelationsOfEvidenceType() {

		assertEquals("wrong number of relations", og.getRelations().size(), 0);

		for (int i = 0; i < 50; i++) {
			ONDEXConcept from = getFromConcept(i);
			ONDEXConcept to = getToConcept(i);

			ONDEXRelation r = og.getFactory().createRelation(from, to, rt, et);
			Assert.assertNotNull(r);
		}

		assertEquals("wrong number of relations", og.getRelations().size(), 50);
		assertEquals("wrong number of relations", og
				.getRelationsOfEvidenceType(et).size(), 50);
	}

	@Test
	public void testGetRelationsOfRelationType() {

		assertEquals("wrong number of relations", og.getRelations().size(), 0);

		for (int i = 0; i < 50; i++) {
			ONDEXConcept from = getFromConcept(i);
			ONDEXConcept to = getToConcept(i);

			ONDEXRelation r = og.getFactory().createRelation(from, to, rt, et);
			Assert.assertNotNull(r);
		}

		assertEquals("wrong number of relations", og.getRelations().size(), 50);
		assertEquals("wrong number of relations", og
				.getRelationsOfRelationType(rt).size(), 50);
	}

	@Test
	public void testGetRelationsOfTag() {
		ONDEXConcept c = getFromConcept(0);
		for (int i = 1; i <= 50; i++) {
			ONDEXConcept from = getFromConcept(i);
			ONDEXConcept to = getToConcept(i);
			ONDEXRelation r = og.getFactory().createRelation(from, to, rt, et);
			Assert.assertNotNull(r);

			r.addTag(c);
		}

		assertEquals("wrong number of relations for tag", 50, og
				.getRelationsOfTag(c).size());
	}

	@Test
	public void testGetSID() {

		Assert.assertNotNull(og.getSID());
	}

	@Test
	public void testGetTag() {
		ONDEXConcept c1 = getFromConcept(0);
		for (int i = 1; i <= 5; i++) {
			ONDEXConcept concept = getFromConcept(i);
			concept.addTag(c1);
		}
		ONDEXConcept c2 = getToConcept(0);
		for (int i = 1; i <= 5; i++) {
			ONDEXConcept concept = getToConcept(i);
			concept.addTag(c2);
		}
		for (int i = 1; i <= 5; i++) {
			ONDEXConcept concept = getToConcept(i);
			concept.addTag(c1);
			concept.addTag(c2);
		}
		ONDEXConcept c3 = getFromConcept(0);
		for (int i = 0; i < 5; i++) {
			ONDEXConcept from = getFromConcept(i);
			ONDEXConcept to = getToConcept(i);

			ONDEXRelation r = og.getFactory().createRelation(from, to, rt, et);
			Assert.assertNotNull(r);
			r.addTag(c3);
		}
		for (int i = 0; i < 5; i++) {
			ONDEXConcept from = getFromConcept(i);
			ONDEXConcept to = getToConcept(i);

			ONDEXRelation r = og.getFactory().createRelation(from, to, rt, et);
			Assert.assertNotNull(r);
			r.addTag(c1);
			r.addTag(c3);
		}

		assertEquals("wrong number of tag concepts", 3, og.getAllTags().size());
		assertTrue("wrong tag concepts", og.getAllTags().contains(c1));
		assertTrue("wrong tag concepts", og.getAllTags().contains(c2));
		assertTrue("wrong tag concepts", og.getAllTags().contains(c3));
	}

	@Test
	public void testIsReadOnly() {
		assertFalse("Graph is read only", og.isReadOnly());
	}

	@Test
	public void testRelations() {
		boolean raised = false;
		try {
			og.getRelations().clear();
		} catch (UnsupportedOperationException e) {
			raised = true;
		}
		assertTrue(
				"Relations set must be immutable. UnsupportedOperationException must be raised.",
				raised);
	}
}
