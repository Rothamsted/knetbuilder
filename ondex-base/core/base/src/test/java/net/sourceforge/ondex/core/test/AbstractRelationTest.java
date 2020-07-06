package net.sourceforge.ondex.core.test;

import junit.framework.TestCase;
import net.sourceforge.ondex.core.*;

import org.junit.*;

import java.awt.Color;
import java.util.*;

/**
 * 
 * @author hindlem
 * 
 */
public abstract class AbstractRelationTest extends TestCase {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private ONDEXGraph og;
	private AttributeName bitscore_att;
	private AttributeName taxid_att;
	private AttributeName att_color;
	private AttributeName att_bool;
	private ConceptClass protein_cc;
	private DataSource tair_dataSource;
	private EvidenceType me_et;
	private RelationType hss_rt;
	private ONDEXConcept protein1_c;
	private ONDEXConcept protein2_c;

	/**
	 * Commit the aog if applicable
	 */
	public abstract void commit();

	/**
	 * Checks contract for two Attribute to be equal.
	 * 
	 * @param attribute1
	 *            Attribute
	 * @param attribute2
	 *            Attribute
	 * @return equal ?
	 */
	private boolean equalAttribute(Attribute attribute1, Attribute attribute2) {
		boolean equal = true;
		equal = attribute1.getOfType().getId()
				.equals(attribute2.getOfType().getId());
		equal = equal && attribute1.getValue().equals(attribute2.getValue());
		equal = equal && (attribute1.isDoIndex() == attribute2.isDoIndex());
		return equal;
	}

	/**
	 * 
	 * @param name
	 *            the name of the new graph
	 * @return the graph to be tested
	 * @throws Exception
	 *             an probs?
	 */
	public abstract ONDEXGraph initialize(String name) throws Exception;

	@Before
	public void setUp() throws Exception {
		og = initialize(this.getName());
		taxid_att = og.getMetaData().getFactory()
				.createAttributeName("TAXID", String.class);
		bitscore_att = og.getMetaData().getFactory()
				.createAttributeName("BITSCORE", Double.class);
		att_color = og.getMetaData().getFactory()
				.createAttributeName("COLOR", Color.class);
		att_bool = og.getMetaData().getFactory()
				.createAttributeName("BOOLEAN", Boolean.class);

		protein_cc = og.getMetaData().getFactory()
				.createConceptClass("Protein");
		tair_dataSource = og.getMetaData().getFactory()
				.createDataSource("TAIR");
		me_et = og.getMetaData().getFactory().createEvidenceType("ME");

		hss_rt = og.getMetaData().getFactory().createRelationType("h_s_s");

		protein1_c = og.getFactory().createConcept("a protien",
				tair_dataSource, protein_cc, me_et);
		protein2_c = og.getFactory().createConcept("a protien",
				tair_dataSource, protein_cc, me_et);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAddEvidenceType() {
		EvidenceType newEt = og.getMetaData().getFactory()
				.createEvidenceType("newEt");

		ONDEXRelation relation = og.getFactory().createRelation(protein1_c,
				protein2_c, hss_rt, me_et);
		assertEquals(1, relation.getEvidence().size());

		relation.addEvidenceType(newEt);
		Set<EvidenceType> ev = relation.getEvidence();
		assertEquals(2, ev.size());

		List<EvidenceType> ets = new ArrayList<EvidenceType>(2);
		int count = 0;
		for (EvidenceType evidence : ev) {
			assertFalse(ets.contains(evidence));
			ets.add(evidence);
			count++;
		}
		assertEquals(2, count);
	}

	@Test
	public void testAddTag() {
		ONDEXRelation relation = og.getFactory().createRelation(protein1_c,
				protein2_c, hss_rt, me_et);

		relation.addTag(protein1_c);
		assertEquals(1, relation.getTags().size());

		relation.addTag(protein2_c);

		Set<ONDEXConcept> tags = relation.getTags();
		assertEquals(2, tags.size());

		int count = 0;
		ONDEXConcept previousTag = null;
		for (ONDEXConcept tag : tags) {
			if (!tag.equals(protein1_c) && !tag.equals(protein2_c)) {
				fail("unknown concept in tag");
			}
			if (previousTag != null)
				assertTrue(!previousTag.equals(tag));
			previousTag = tag;
			count++;
		}
		assertEquals(2, count);
	}

	@Test
	public void testAttributes() {
		ONDEXRelation relation = og.getFactory().createRelation(protein1_c,
				protein2_c, hss_rt, me_et);

		boolean raised = false;
		try {
			relation.getAttributes().clear();
		} catch (UnsupportedOperationException e) {
			raised = true;
		}
		assertTrue(
				"Attributes set must be immutable. UnsupportedOperationException must be raised.",
				raised);
	}

	@Test
	public void testCreateRelationAttribute() {
		ONDEXRelation relation = og.getFactory().createRelation(protein1_c,
				protein2_c, hss_rt, me_et);

		Attribute taxIdOrig = relation.createAttribute(taxid_att, "3702", true);
		Attribute bitscoreOrig = relation.createAttribute(bitscore_att,
				3702.65645645645623452d, false);
		Attribute colorOrig = relation.createAttribute(att_color, Color.BLACK,
				false);
		Attribute boolOrig = relation.createAttribute(att_bool, Boolean.TRUE,
				false);

		Attribute taxId = relation.getAttribute(taxid_att);
		assertNotNull("String gds not submitted", taxId);
		assertEquals("Incorrect value for taxid", "3702", taxId.getValue());
		assertEquals(true, taxId.isDoIndex());
		assertTrue(equalAttribute(taxId, taxIdOrig));

		// get Double back
		Attribute bitscore = relation.getAttribute(bitscore_att);
		assertNotNull("Double gds not submitted", bitscore);
		assertEquals("Incorrect value for Double", 3702.65645645645623452d,
				bitscore.getValue());
		assertEquals(false, bitscore.isDoIndex());
		assertTrue(equalAttribute(bitscore, bitscoreOrig));

		// get color back
		Attribute color = relation.getAttribute(att_color);
		assertNotNull("Color gds not submitted", color);
		assertEquals("Incorrect value for Color", Color.BLACK, color.getValue());
		assertEquals(false, color.isDoIndex());
		assertTrue(equalAttribute(color, colorOrig));

		// get boolean back
		Attribute bool = relation.getAttribute(att_bool);
		assertNotNull("Boolean gds not submitted", bool);
		assertEquals("Incorrect value for Boolean", Boolean.TRUE,
				bool.getValue());
		assertEquals(false, bool.isDoIndex());
		assertTrue(equalAttribute(bool, boolOrig));
	}

	@Test
	public void testDeleteRelationAttribute() {
		ONDEXRelation relation = og.getFactory().createRelation(protein1_c,
				protein2_c, hss_rt, me_et);

		// two Attribute, one with String other as Double
		Attribute taxidAttributeOrig = relation.createAttribute(taxid_att,
				"3702", false);
		Attribute bitscoreAttributeOrig = relation.createAttribute(
				bitscore_att, 3702.65645645645623452d, false);

		// check its there
		Attribute taxidAttribute = relation.getAttribute(taxid_att);
		assertNotNull("String gds not submitted", taxidAttribute);
		assertTrue(equalAttribute(taxidAttributeOrig, taxidAttribute));

		// check its there
		Attribute bitscoreAttribute = relation.getAttribute(bitscore_att);
		assertNotNull("Double gds not submitted", bitscoreAttribute);
		assertTrue(equalAttribute(bitscoreAttributeOrig, bitscoreAttribute));

		// delete first one, check second still there
		assertTrue(relation.deleteAttribute(taxid_att));
		assertNull(relation.getAttribute(taxid_att));
		assertNotNull("Double gds not submitted",
				relation.getAttribute(bitscore_att));

		// delete second one
		assertTrue(relation.deleteAttribute(bitscore_att));
		assertNull(relation.getAttribute(bitscore_att));
	}

	@Test
	public void testEmptyTag() {
		ONDEXRelation relation = og.getFactory().createRelation(protein1_c,
				protein2_c, hss_rt, me_et);

		assertEquals("Relation should be created with an empty set of tags",
				Collections.emptySet(), relation.getTags());
		assertEquals("Tag size should be zero", 0, relation.getTags().size());
		assertTrue("Tag should be empty", relation.getTags().isEmpty());
	}

	@Test
	public void testEqualsObject() {
		ONDEXRelation relation = og.getFactory().createRelation(protein1_c,
				protein2_c, hss_rt, me_et);
		ONDEXRelation relation2 = og.getFactory().createRelation(protein1_c,
				protein2_c, hss_rt, me_et);
		assertEquals(relation, relation);
		assertTrue(relation.equals(relation2));
	}

	@Test
	public void testGetEvidence() {
		ONDEXRelation relation = og.getFactory().createRelation(protein1_c,
				protein2_c, hss_rt, me_et);
		Set<EvidenceType> ev = relation.getEvidence();
		assertEquals(1, ev.size());
		assertEquals(me_et, ev.iterator().next());
	}

	@Test
	public void testGetFromConcept() {
		ONDEXRelation relation = og.getFactory().createRelation(protein1_c,
				protein2_c, hss_rt, me_et);
		assertEquals(protein1_c, relation.getFromConcept());
	}

	@Test
	public void testGetId() {
		ONDEXRelation relation = og.getFactory().createRelation(protein1_c,
				protein2_c, hss_rt, me_et);
		assertEquals(1, relation.getId());
	}

	@Test
	public void testGetKey() {
		ONDEXRelation relation = og.getFactory().createRelation(protein1_c,
				protein2_c, hss_rt, me_et);
		RelationKey key = relation.getKey();
		assertNotNull(key);
		assertEquals(protein1_c.getId(), key.getFromID());
		assertEquals(protein2_c.getId(), key.getToID());

		ONDEXRelation relation2 = og.getFactory().createRelation(protein2_c,
				protein1_c, hss_rt, me_et);
		RelationKey key2 = relation2.getKey();
		assertNotNull(key2);
		assertEquals(protein2_c.getId(), key2.getFromID());
		assertEquals(protein1_c.getId(), key2.getToID());
	}

	@Test
	public void testGetOfType() {
		ONDEXRelation relation = og.getFactory().createRelation(protein1_c,
				protein2_c, hss_rt, me_et);
		assertEquals(hss_rt, relation.getOfType());
	}

	@Test
	public void testGetRelationAttribute() {
		ONDEXRelation relation = og.getFactory().createRelation(protein1_c,
				protein2_c, hss_rt, me_et);

		// lots of String attributes
		HashMap<AttributeName, String> string_values = new HashMap<AttributeName, String>(
				50);
		for (int i = 0; i < 50; i++) {
			AttributeName att = og.getMetaData().getFactory()
					.createAttributeName("ATTString" + i, String.class);
			Attribute attribute = relation.createAttribute(att, "String" + i,
					false);
			assertNotNull(attribute);
			string_values.put(att, "String" + i);
		}

		// lots of Double attributes
		HashMap<AttributeName, Double> double_values = new HashMap<AttributeName, Double>(
				50);
		for (int i = 0; i < 50; i++) {
			AttributeName att = og.getMetaData().getFactory()
					.createAttributeName("ATTDouble" + i, Double.class);
			Attribute attribute = relation.createAttribute(att, (double) i,
					false);
			assertNotNull(attribute);
			double_values.put(att, (double) i);
		}

		// compare original values for String
		for (AttributeName att : string_values.keySet()) {
			String value = string_values.get(att);
			Object o = relation.getAttribute(att).getValue();
			assertEquals("Attribute value class different", value.getClass(),
					o.getClass());
			assertEquals("Attribute value was not as submitted", value, o);
		}

		// compare original values for Double
		for (AttributeName att : double_values.keySet()) {
			Double value = double_values.get(att);
			Object o = relation.getAttribute(att).getValue();
			assertEquals("Attribute value class different", value.getClass(),
					o.getClass());
			assertEquals("Attribute value was not as submitted", value, o);
		}
	}

	@Test
	public void testGetRelationAttributes() {
		ONDEXRelation relation = og.getFactory().createRelation(protein1_c,
				protein2_c, hss_rt, me_et);

		// same as above
		HashMap<AttributeName, Object> values = new HashMap<AttributeName, Object>(
				100);

		for (int i = 0; i < 50; i++) {
			AttributeName att = og.getMetaData().getFactory()
					.createAttributeName("ATTString" + i, String.class);
			relation.createAttribute(att, "String" + i, false);
			values.put(att, "String" + i);
		}

		for (int i = 0; i < 50; i++) {
			AttributeName att = og.getMetaData().getFactory()
					.createAttributeName("ATTDouble" + i, Double.class);
			relation.createAttribute(att, (double) i, false);
			values.put(att, (double) i);
		}

		// here count numbers
		int count = 0;
		Set<Attribute> gdss = relation.getAttributes();
		assertEquals(100, gdss.size());
		for (Attribute attribute : gdss) {
			Object value = values.get(attribute.getOfType());
			Object o = attribute.getValue();
			assertEquals("Attribute value class different", value.getClass(),
					o.getClass());
			assertEquals("Value is not as created", value, o);
			count++;
		}
		assertEquals(100, count); // check size is working
	}

	@Test
	public void testGetTag() {
		// self tag test
		ONDEXRelation relation = og.getFactory().createRelation(protein1_c,
				protein2_c, hss_rt, me_et);

		relation.addTag(protein1_c);
		assertEquals(1, relation.getTags().size());

		// more tag
		relation.addTag(protein2_c);

		// check size
		Set<ONDEXConcept> tags = relation.getTags();
		assertEquals(2, tags.size());

		// check duplicates
		int count = 0;
		ONDEXConcept previousTag = null;
		for (ONDEXConcept tag : tags) {
			if (!tag.equals(protein1_c) && !tag.equals(protein2_c)) {
				fail("unknown concept in tag");
			}
			if (previousTag != null)
				assertTrue(!previousTag.equals(tag));
			previousTag = tag;
			count++;
		}
		assertEquals(2, count);
	}

	@Test
	public void testGetToConcept() {
		ONDEXRelation relation = og.getFactory().createRelation(protein1_c,
				protein2_c, hss_rt, me_et);
		assertEquals(protein2_c, relation.getToConcept());
	}

	@Test
	public void testHashCode() {
		ONDEXRelation relation = og.getFactory().createRelation(protein1_c,
				protein2_c, hss_rt, me_et);
		ONDEXRelation relation2 = og.getFactory().createRelation(protein1_c,
				protein1_c, hss_rt, me_et);
		assertFalse(relation.hashCode() == relation2.hashCode());
	}

	@Test
	public void testRemoveEvidenceType() {
		testAddEvidenceType();

		// not the best of practice but no point duplicating
		ONDEXRelation relation = og.getRelation(1);
		assertEquals(2, relation.getEvidence().size());
		relation.removeEvidenceType(me_et);

		// check size of iterator
		Set<EvidenceType> ets = relation.getEvidence();
		assertEquals(1, ets.size());
		for (EvidenceType et : ets) {
			assertEquals("newEt", et.getId());
		}
	}

	@Test
	public void testRemoveTag() {
		testGetTag();

		// using previous results
		ONDEXRelation relation = og.getRelation(1);
		assertEquals("There are initially two tags", 2, relation.getTags()
				.size());

		assertTrue("Removing a tag from a relation should return true",
				relation.removeTag(protein1_c));

		Set<ONDEXConcept> tags = relation.getTags();
		assertEquals("Removing one tag from two tags leaves behind 1 tag", 1,
				tags.size());

		// check size of iterator
		int count = 0;
		for (ONDEXConcept tag : tags) {
			assertEquals("The remaining tag is still there", tag, protein2_c);
			count++;
		}
		assertEquals(
				"There should be one tag in the iteration over remaining tags",
				1, count);
	}

	@Test
	public void testTag() {
		ONDEXRelation relation = og.getFactory().createRelation(protein1_c,
				protein2_c, hss_rt, me_et);

		boolean raised = false;
		try {
			relation.getTags().clear();
		} catch (UnsupportedOperationException e) {
			raised = true;
		}
		assertTrue(
				"Tag set must be immutable. UnsupportedOperationException must be raised.",
				raised);
	}

}
