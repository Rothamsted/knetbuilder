package net.sourceforge.ondex.core.test;

import junit.framework.TestCase;
import net.sourceforge.ondex.core.*;
import org.junit.*;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author hindlem
 */
public abstract class AbstractConceptTest extends TestCase {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private AttributeName att_bitscore;

	private AttributeName att_taxid;

	private AttributeName att_color;

	private AttributeName att_bool;

	private ConceptClass cc_protein;

	private DataSource dataSource_tair;

	private EvidenceType et_me;

	private ONDEXGraph og;

	/**
	 * Commit the aog if applicable
	 */
	public abstract void commit();

	/**
	 * Checks contract for two accession to be equal.
	 * 
	 * @param acc1
	 *            ConceptAccession
	 * @param acc2
	 *            ConceptAccession
	 * @return equal ?
	 */
	private boolean equalAccessions(ConceptAccession acc1, ConceptAccession acc2) {
		boolean equal;
		equal = acc1.getElementOf().equals(acc2.getElementOf());
		equal = equal && acc1.getAccession().equals(acc2.getAccession());
		equal = equal && (acc1.isAmbiguous() == acc2.isAmbiguous());
		return equal;
	}

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
		boolean equal;
		equal = attribute1.getOfType().getId()
				.equals(attribute2.getOfType().getId());
		equal = equal && attribute1.getValue().equals(attribute2.getValue());
		equal = equal && (attribute1.isDoIndex() == attribute2.isDoIndex());
		return equal;
	}

	/**
	 * Checks contract for two names to be equal.
	 * 
	 * @param name1
	 *            ConceptName
	 * @param name2
	 *            ConceptName
	 * @return equal ?
	 */
	private boolean equalNames(ConceptName name1, ConceptName name2) {
		boolean equal;
		equal = name1.getName().equals(name2.getName());
		equal = equal && (name1.isPreferred() == name2.isPreferred());
		return equal;
	}

	/**
	 * @param name
	 *            the name of the new graph
	 * @return the graph to be tested
	 * @throws Exception
	 *             an probs?
	 */
	public abstract ONDEXGraph initialize(String name) throws Exception;

	@Before
	public void setUp() throws Exception {
		// setup graph
		og = initialize(this.getName());

		// attribute names
		att_taxid = og.getMetaData().getFactory()
				.createAttributeName("TAXID", String.class);
		att_bitscore = og.getMetaData().getFactory()
				.createAttributeName("BITSCORE", Double.class);
		att_color = og.getMetaData().getFactory()
				.createAttributeName("COLOR", Color.class);
		att_bool = og.getMetaData().getFactory()
				.createAttributeName("BOOLEAN", Boolean.class);

		// other meta data
		cc_protein = og.getMetaData().getFactory()
				.createConceptClass("Protein");
		dataSource_tair = og.getMetaData().getFactory()
				.createDataSource("TAIR");
		et_me = og.getMetaData().getFactory().createEvidenceType("ME");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAddEvidenceType() {
		// create new evidence type to be added
		EvidenceType newEt = og.getMetaData().getFactory()
				.createEvidenceType("randomEt");

		// concept with standard evidence type
		ONDEXConcept concept2 = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);
		concept2.addEvidenceType(newEt);

		// check size of evidence
		Set<EvidenceType> ets2 = concept2.getEvidence();
		assertEquals(2, ets2.size());

		// iterate over evidence
		int count = 0;
		EvidenceType previousEt = null;
		for (EvidenceType et : ets2) {

			// repeating
			if (et != null) {
				assertTrue("EvidenceType is in twice", !et.equals(previousEt));
			}

			// something strange found
			if (!et.equals(et_me) && !et.equals(newEt)) {
				fail("unknown evidence type " + et);
			}

			previousEt = et;
			count++;
		}
		assertEquals(2, count);
	}

	@Test
	public void testAddTag() {
		// tested by testGetTag()
		// test on a larger set
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);
		for (int i = 0; i < 50; i++) {
			concept.addTag(og.getFactory().createConcept("a protein " + i,
					dataSource_tair, cc_protein, et_me));
		}
		assertEquals(50, concept.getTags().size());
	}

	@Test
	public void testAttributes() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);

		boolean raised = false;
		try {
			concept.getAttributes().clear();
		} catch (UnsupportedOperationException e) {
			raised = true;
		}
		assertTrue(
				"Attributes set must be immutable. UnsupportedOperationException must be raised.",
				raised);
	}

	@Test
	public void testConceptAccessions() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);

		boolean raised = false;
		try {
			concept.getConceptAccessions().clear();
		} catch (UnsupportedOperationException e) {
			raised = true;
		}
		assertTrue(
				"ConceptAccessions set must be immutable. UnsupportedOperationException must be raised.",
				raised);
	}

	@Test
	public void testConceptNames() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);

		boolean raised = false;
		try {
			concept.getConceptNames().clear();
		} catch (UnsupportedOperationException e) {
			raised = true;
		}
		assertTrue(
				"ConceptNames set must be immutable. UnsupportedOperationException must be raised.",
				raised);
	}

	@Test
	public void testCreateConceptAccessionStringDataSource() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);

		// create a new concept accession
		ConceptAccession accOrig = concept.createConceptAccession("AT3G234243",
				dataSource_tair, true);
		assertNotNull(accOrig);
		assertEquals("AT3G234243", accOrig.getAccession());
		assertEquals(dataSource_tair, accOrig.getElementOf());
		assertEquals(true, accOrig.isAmbiguous());

		// returned concept accession
		ConceptAccession acc = concept.getConceptAccession("AT3G234243",
				dataSource_tair);
		assertNotNull(acc);
		assertEquals("AT3G234243", acc.getAccession());
		assertEquals(dataSource_tair, acc.getElementOf());
		assertEquals(true, acc.isAmbiguous());

		// compare concept accessions
		assertEquals(accOrig, acc);
		assertTrue(equalAccessions(accOrig, acc));
	}

	@Test
	public void testCreateConceptAccessionStringDataSourceBoolean() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);

		// create new concept accession
		ConceptAccession accOrig = concept.createConceptAccession("AT3G234243",
				dataSource_tair, true);
		assertNotNull(accOrig);
		assertEquals("AT3G234243", accOrig.getAccession());
		assertEquals(dataSource_tair, accOrig.getElementOf());
		assertEquals(true, accOrig.isAmbiguous());

		// returned concept accession
		ConceptAccession acc = concept.getConceptAccession("AT3G234243",
				dataSource_tair);
		assertNotNull(acc);
		assertEquals("AT3G234243", acc.getAccession());
		assertEquals(dataSource_tair, acc.getElementOf());
		assertEquals(true, acc.isAmbiguous());

		// compare concept accessions
		assertEquals(accOrig, acc);
		assertTrue(equalAccessions(accOrig, acc));

		// different ambiguous flag
		ConceptAccession accOrig2 = concept.createConceptAccession(
				"AT3G234243.1", dataSource_tair, false);
		assertEquals("AT3G234243.1", accOrig2.getAccession());
		assertEquals(dataSource_tair, accOrig2.getElementOf());
		assertEquals(false, accOrig2.isAmbiguous());

		// different ambiguous flag
		ConceptAccession acc2 = concept.getConceptAccession("AT3G234243.1",
				dataSource_tair);
		assertNotNull(acc2);
		assertEquals("AT3G234243.1", acc2.getAccession());
		assertEquals(dataSource_tair, acc2.getElementOf());
		assertEquals(false, acc2.isAmbiguous());

		// compare concept accessions
		assertEquals(accOrig2, acc2);
		assertTrue(equalAccessions(accOrig2, acc2));
	}

	@Test
	public void testCreateConceptAttribute() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);

		// two Attribute, one with String other as Double
		Attribute taxIdOrig = concept.createAttribute(att_taxid, "3702", true);
		Attribute bitscoreOrig = concept.createAttribute(att_bitscore,
				3702.65645645645623452d, false);
		Attribute colorOrig = concept.createAttribute(att_color, Color.BLACK,
				false);
		Attribute boolOrig = concept.createAttribute(att_bool, Boolean.TRUE,
				false);

		// get String back
		Attribute taxId = concept.getAttribute(att_taxid);
		assertNotNull("String gds not submitted", taxId);
		assertEquals("Incorrect value for taxid", "3702", taxId.getValue());
		assertEquals(true, taxId.isDoIndex());
		assertTrue(equalAttribute(taxId, taxIdOrig));

		// get Double back
		Attribute bitscore = concept.getAttribute(att_bitscore);
		assertNotNull("Double gds not submitted", bitscore);
		assertEquals("Incorrect value for Double", 3702.65645645645623452d,
				bitscore.getValue());
		assertEquals(false, bitscore.isDoIndex());
		assertTrue(equalAttribute(bitscore, bitscoreOrig));

		// get color back
		Attribute color = concept.getAttribute(att_color);
		assertNotNull("Color gds not submitted", color);
		assertEquals("Incorrect value for Color", Color.BLACK, color.getValue());
		assertEquals(false, color.isDoIndex());
		assertTrue(equalAttribute(color, colorOrig));

		// get boolean back
		Attribute bool = concept.getAttribute(att_bool);
		assertNotNull("Boolean gds not submitted", bool);
		assertEquals("Incorrect value for Boolean", Boolean.TRUE,
				bool.getValue());
		assertEquals(false, bool.isDoIndex());
		assertTrue(equalAttribute(bool, boolOrig));
	}

	@Test
	public void testCreateConceptNameString() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);

		// create a concept name
		ConceptName cn = concept.createConceptName("Random Name", false);
		assertNotNull(cn);
		assertEquals("Random Name", cn.getName());
		assertEquals(false, cn.isPreferred());

		// get a concept name back
		ConceptName cn2 = concept.getConceptName("Random Name");
		assertNotNull(cn2);
		assertEquals("Random Name", cn2.getName());
		assertEquals(false, cn2.isPreferred());

		// compare both
		assertEquals(cn, cn2);
		assertTrue(equalNames(cn, cn2));
		assertNotNull(concept);
		assertNull(concept.getConceptName());
	}

	@Test
	public void testCreateConceptNameStringBoolean() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);

		// create a preferred concept name
		ConceptName cn = concept.createConceptName("Random Name", true);
		assertNotNull(cn);
		assertEquals("Random Name", cn.getName());
		assertEquals(true, cn.isPreferred());

		// get a preferred concept name back
		ConceptName cn2 = concept.getConceptName("Random Name");
		assertNotNull(cn2);
		assertEquals("Random Name", cn2.getName());
		assertEquals(true, cn2.isPreferred());

		// compare both
		assertEquals(cn, cn2);
		assertTrue(equalNames(cn, cn2));
		assertEquals(cn2, concept.getConceptName());
	}

	@Test
	public void testDeleteConceptAccession() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);

		// first create concept accession
		ConceptAccession accOrig = concept.createConceptAccession("AT3G234243",
				dataSource_tair, true);
		assertNotNull(accOrig);

		// then delete concept accession
		boolean delAcc = concept.deleteConceptAccession("AT3G234243",
				dataSource_tair);
		assertTrue(delAcc);
		// assertEquals("AT3G234243", delAcc.getAccession());
		// assertEquals(dataSource_tair, delAcc.getElementOf());
		// assertEquals(true, delAcc.isAmbiguous());

		// // assert right one got removed
		// assertEquals(accOrig, delAcc);
		// assertTrue(equalAccessions(accOrig, delAcc));
		// assertNull(concept.getConceptAccession("AT3G234243",
		// dataSource_tair));
	}

	@Test
	public void testDeleteConceptAttribute() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);

		// two Attribute, one with String other as Double
		Attribute taxidAttributeOrig = concept.createAttribute(att_taxid,
				"3702", false);
		Attribute bitscoreAttributeOrig = concept.createAttribute(att_bitscore,
				3702.65645645645623452d, false);

		// check its there
		Attribute taxidAttribute = concept.getAttribute(att_taxid);
		assertNotNull("String gds not submitted", taxidAttribute);
		assertTrue(equalAttribute(taxidAttributeOrig, taxidAttribute));

		// check its there
		Attribute bitscoreAttribute = concept.getAttribute(att_bitscore);
		assertNotNull("Double gds not submitted", bitscoreAttribute);
		assertTrue(equalAttribute(bitscoreAttributeOrig, bitscoreAttribute));

		// delete first one, check second still there
		assertTrue(concept.deleteAttribute(att_taxid));
		assertNull(concept.getAttribute(att_taxid));
		assertNotNull("Double gds not submitted",
				concept.getAttribute(att_bitscore));

		// delete second one
		assertTrue(concept.deleteAttribute(att_bitscore));
		assertNull(concept.getAttribute(att_bitscore));
	}

	@Test
	public void testDeleteConceptName() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);

		// create concept name
		ConceptName cn = concept.createConceptName("Random Name", true);
		assertNotNull(cn);

		// delete concept name
		boolean cn2 = concept.deleteConceptName("Random Name");
		assertTrue(cn2);
		// assertEquals(cn, cn2);
		// assertTrue(equalNames(cn, cn2));
		// assertNull(concept.getConceptName("Random Name"));
		// assertNull(concept.getConceptName());
	}

	@Test
	public void testEmptyTag() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);

		assertEquals("Concept should be created with an empty set of tags",
				Collections.emptySet(), concept.getTags());
		assertEquals("Tag size should be zero", 0, concept.getTags().size());
		assertTrue("Tag should be empty", concept.getTags().isEmpty());
	}

	@Test
	public void testEqualsObject() {
		ONDEXConcept concept = og.getFactory().createConcept("a proteins",
				dataSource_tair, cc_protein, et_me);
		ONDEXConcept concept2 = og.getFactory().createConcept("a proteins",
				dataSource_tair, cc_protein, et_me);

		// all combinations
		assertTrue(concept.equals(concept));
		assertTrue(concept2.equals(concept2));
		assertFalse(concept.equals(concept2));
		assertFalse(concept2.equals(concept));
	}

	@Test
	public void testEvidence() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);

		boolean raised = false;
		try {
			concept.getEvidence().clear();
		} catch (UnsupportedOperationException e) {
			raised = true;
		}
		assertTrue(
				"Evidence set must be immutable. UnsupportedOperationException must be raised.",
				raised);
	}

	@Test
	public void testGetAnnotation() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				"dummy annotation", dataSource_tair, cc_protein, et_me);

		// construction time annotation
		assertEquals("dummy annotation", concept.getAnnotation());

		// set annotation after creation
		concept.setAnnotation("my annotation");
		assertEquals("my annotation", concept.getAnnotation());
	}

	@Test
	public void testGetConceptAccession() {

		DataSource newDataSource = og.getMetaData().getFactory()
				.createDataSource("new_DataSource");

		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);

		// create three concept accessions
		ConceptAccession acc1 = concept.createConceptAccession("AT3G234243",
				dataSource_tair, true);
		assertNotNull(acc1);
		ConceptAccession acc2 = concept.createConceptAccession("AT3G234243.1",
				dataSource_tair, false);
		assertNotNull(acc2);
		ConceptAccession acc3 = concept.createConceptAccession("bla",
				newDataSource, false);
		assertNotNull(acc3);

		// test the same are returned
		assertEquals(acc1,
				concept.getConceptAccession("AT3G234243", dataSource_tair));
		assertEquals(acc2,
				concept.getConceptAccession("AT3G234243.1", dataSource_tair));
		assertEquals(acc3, concept.getConceptAccession("bla", newDataSource));
		assertTrue(equalAccessions(acc1,
				concept.getConceptAccession("AT3G234243", dataSource_tair)));
		assertTrue(equalAccessions(acc2,
				concept.getConceptAccession("AT3G234243.1", dataSource_tair)));
		assertTrue(equalAccessions(acc3,
				concept.getConceptAccession("bla", newDataSource)));
	}

	@Test
	public void testGetConceptAccessions() {
		testGetConceptAccession();

		// another DataSource
		DataSource newDataSource = og.getMetaData().getDataSource(
				"new_DataSource");
		assertNotNull(newDataSource);

		// get test concept
		ONDEXConcept concept = og.getConcept(1);
		assertNotNull(concept);

		List<ConceptAccession> previous = new ArrayList<ConceptAccession>(3);

		// there are three accessions
		Set<ConceptAccession> accessions = concept.getConceptAccessions();
		assertEquals(3, accessions.size());

		// check none is duplicated
		int count = 0;
		for (ConceptAccession accession : accessions) {
			assertFalse(previous.contains(accession));
			previous.add(accession);
			count++;
		}
		assertEquals(3, count);
	}

	@Test
	public void testGetConceptAttribute() {
		// this is essentially tested in testCreateConceptAttribute but we
		// expand the
		// test here to multiple Attribute attribubtes

		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);

		// lots of String attributes
		HashMap<AttributeName, String> string_values = new HashMap<AttributeName, String>(
				50);
		for (int i = 0; i < 50; i++) {
			AttributeName att = og.getMetaData().getFactory()
					.createAttributeName("ATTString" + i, String.class);
			Attribute attribute = concept.createAttribute(att, "String" + i,
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
			Attribute attribute = concept.createAttribute(att, (double) i,
					false);
			assertNotNull(attribute);
			double_values.put(att, (double) i);
		}

		// lots of Colour attributes
		HashMap<AttributeName, Color> color_values = new HashMap<AttributeName, Color>(
				50);
		for (int i = 0; i < 50; i++) {
			AttributeName att = og.getMetaData().getFactory()
					.createAttributeName("ATTColor" + i, Color.class);
			Attribute attribute = concept.createAttribute(att, new Color(i, 0,
					0), false);
			assertNotNull(attribute);
			color_values.put(att, new Color(i, 0, 0));
		}

		// compare original values for String
		for (AttributeName att : string_values.keySet()) {
			String value = string_values.get(att);
			Object o = concept.getAttribute(att).getValue();
			assertEquals("Attribute value class different", value.getClass(),
					o.getClass());
			assertEquals("Attribute value was not as submitted", value, o);
		}

		// compare original values for Double
		for (AttributeName att : double_values.keySet()) {
			Double value = double_values.get(att);
			Object o = concept.getAttribute(att).getValue();
			assertEquals("Attribute value class different", value.getClass(),
					o.getClass());
			assertEquals("Attribute value was not as submitted", value, o);
		}

		// compare original values for Colour
		for (AttributeName att : color_values.keySet()) {
			Color value = color_values.get(att);
			Object o = concept.getAttribute(att).getValue();
			assertEquals("Attribute value class different", value.getClass(),
					o.getClass());
			assertEquals("Attribute value was not as submitted", value, o);
		}
	}

	@Test
	public void testGetConceptAttributes() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);

		// same as above
		HashMap<AttributeName, Object> values = new HashMap<AttributeName, Object>(
				100);

		for (int i = 0; i < 50; i++) {
			AttributeName att = og.getMetaData().getFactory()
					.createAttributeName("ATTString" + i, String.class);
			concept.createAttribute(att, "String" + i, false);
			values.put(att, "String" + i);
		}

		for (int i = 0; i < 50; i++) {
			AttributeName att = og.getMetaData().getFactory()
					.createAttributeName("ATTDouble" + i, Double.class);
			concept.createAttribute(att, (double) i, false);
			values.put(att, (double) i);
		}

		for (int i = 0; i < 50; i++) {
			AttributeName att = og.getMetaData().getFactory()
					.createAttributeName("ATTColor" + i, Color.class);
			Attribute attribute = concept.createAttribute(att, new Color(i, 0,
					0), false);
			assertNotNull(attribute);
			values.put(att, new Color(i, 0, 0));
		}

		// here count numbers
		int count = 0;
		Set<Attribute> gdss = concept.getAttributes();
		assertEquals(150, gdss.size());
		for (Attribute attribute : gdss) {
			Object value = values.get(attribute.getOfType());
			Object o = attribute.getValue();
			assertEquals("Attribute value class different", value.getClass(),
					o.getClass());
			assertEquals("Value is not as created", value, o);
			count++;
		}
		assertEquals(150, count); // check size is working
	}

	@Test
	public void testGetConceptName() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);

		// two names, one preferred
		ConceptName cn = concept.createConceptName("Random Name", true);
		assertNotNull(cn);
		ConceptName cn2 = concept.createConceptName("Random Name 2", false);
		assertNotNull(cn2);

		// get preferred name
		ConceptName cnPref = concept.getConceptName();
		assertNotNull(cnPref);
		assertEquals(cn, cnPref);
		assertTrue(equalNames(cn, cnPref));
	}

	@Test
	public void testGetConceptNames() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);

		// more than one name
		ConceptName cn = concept.createConceptName("Random Name", true);
		assertNotNull(cn);
		ConceptName cn2 = concept.createConceptName("Random Name 2", false);
		assertNotNull(cn2);

		// check size
		Set<ConceptName> names = concept.getConceptNames();
		assertEquals(2, names.size());

		// check for duplicates
		int count = 0;
		ConceptName previousName = null;
		for (ConceptName name : names) {
			if (name.equals(cn) && name.equals(cn2)) {
				fail("unknown name :" + name);
			}

			if (previousName != null)
				assertFalse(previousName.equals(name));

			previousName = name;
			count++;
		}
		assertEquals(2, count);
	}

	@Test
	public void testGetConceptNameString() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);

		// particular concept names
		ConceptName cn = concept.createConceptName("Random Name", true);
		assertNotNull(cn);
		ConceptName cn2 = concept.createConceptName("Random Name 2", false);
		assertNotNull(cn2);

		// found the right one?
		ConceptName cnnamed = concept.getConceptName("Random Name 2");
		assertNotNull(cnnamed);
		assertEquals(cn2, cnnamed);
		assertTrue(equalNames(cn2, cnnamed));
	}

	@Test
	public void testGetDescription() {
		ONDEXConcept concept = og.getFactory()
				.createConcept("a protein", "annotation", "description",
						dataSource_tair, cc_protein, et_me);

		// construction time description
		assertEquals("description", concept.getDescription());

		// modified description
		concept.setDescription("my Description");
		assertEquals("my Description", concept.getDescription());
	}

	@Test
	public void testGetElementOf() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);
		assertEquals(dataSource_tair, concept.getElementOf());
	}

	@Test
	public void testGetEvidence() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);

		// collection of one evidence
		Set<EvidenceType> ets = concept.getEvidence();
		assertEquals(1, ets.size());
		int count = 0;
		for (EvidenceType et : ets) {
			assertEquals(et_me, et);
			count++;
		}
		assertEquals(1, count);

		// add more evidence
		EvidenceType newEt = og.getMetaData().getFactory()
				.createEvidenceType("randomEt");

		List<EvidenceType> al = new ArrayList<EvidenceType>(2);
		al.add(et_me);
		al.add(newEt);

		ONDEXConcept concept2 = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, al);

		// collection of two evidence
		Set<EvidenceType> ets2 = concept2.getEvidence();
		assertEquals(2, ets2.size());

		// check duplications
		count = 0;
		EvidenceType previousEt = null;
		for (EvidenceType et : ets2) {
			if (!et.equals(et_me) && !et.equals(newEt)) {
				fail("unknown evidence type " + et);
			}

			if (et != null) {
				assertTrue("EvidenceType is in twice", !et.equals(previousEt));
			}

			count++;
			previousEt = et;
		}
		assertEquals(2, count);
	}

	@Test
	public void testGetId() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);
		assertEquals(1, concept.getId());
	}

	@Test
	public void testGetInheritedFrom() {
		ONDEXGraphMetaData md = og.getMetaData();
		ConceptClass ccA = md.createConceptClass("A", "A", "A", null);
		ConceptClass ccB = md.createConceptClass("B", "B", "B", ccA);
		ConceptClass ccC = md.createConceptClass("C", "C", "C", ccB);
		ConceptClass ccX = md.createConceptClass("X", "X", "X", ccB);

		ONDEXConcept concept = og.getFactory().createConcept("testInheritance",
				dataSource_tair, ccC, et_me);
		assertTrue(
				"method inheritedFrom(ConceptClass) returned false for its own ofType",
				concept.inheritedFrom(ccC));
		assertTrue(
				"method inheritedFrom(ConceptClass) returned false for a superclass of its ofType",
				concept.inheritedFrom(ccA));
		assertFalse(
				"method inheritedFrom(ConceptClass) returned true for a branched ConceptClass",
				concept.inheritedFrom(ccX));
		assertFalse(
				"method inheritedFrom(ConceptClass) returned true for an unrelated ConceptClass",
				concept.inheritedFrom(cc_protein));
	}

	@Test
	public void testGetOfType() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);
		assertEquals(cc_protein, concept.getOfType());
	}

	@Test
	public void testGetPID() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);
		assertEquals("a protein", concept.getPID());
	}

	@Test
	public void testGetTag() {
		// self tag test
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);
		concept.addTag(concept);
		assertEquals(1, concept.getTags().size());

		// more tag
		ONDEXConcept concept2 = og.getFactory().createConcept("a protein 2",
				dataSource_tair, cc_protein, et_me);
		concept.addTag(concept2);

		// check size
		Set<ONDEXConcept> tags = concept.getTags();
		assertEquals(2, tags.size());

		// check duplicates
		int count = 0;
		ONDEXConcept previousTag = null;
		for (ONDEXConcept tag : tags) {
			if (!tag.equals(concept) && !tag.equals(concept2)) {
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
	public void testRemoveEvidenceType() {
		testAddEvidenceType();

		// not the best of practice but no point duplicating
		ONDEXConcept concept = og.getConcept(1);
		assertEquals(2, concept.getEvidence().size());
		concept.removeEvidenceType(et_me);

		// check size of iterator
		Set<EvidenceType> ets = concept.getEvidence();
		assertEquals(1, ets.size());
		for (EvidenceType et : ets) {
			assertEquals("randomEt", et.getId());
		}
	}

	@Test
	public void testRemoveTag() {
		testGetTag();

		// using previous results
		ONDEXConcept concept = og.getConcept(1);
		assertEquals(2, concept.getTags().size());

		ONDEXConcept concept2 = og.getConcept(2);
		assertTrue(concept.removeTag(concept));

		Set<ONDEXConcept> tags = concept.getTags();
		assertEquals(1, tags.size());

		// check size of iterator
		int count = 0;
		for (ONDEXConcept tag : tags) {
			assertEquals(tag, concept2);
			count++;
		}
		assertEquals(1, count);
	}

	@Test
	public void testSetAnnotation() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);

		// changing annotation
		concept.setAnnotation("my annotation2");
		assertEquals("my annotation2", concept.getAnnotation());
		concept.setAnnotation("my annotation3");
		assertEquals("my annotation3", concept.getAnnotation());
	}

	@Test
	public void testSetDescription() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);

		// changing description
		concept.setDescription("my Description");
		assertEquals("my Description", concept.getDescription());
		concept.setDescription("my Description2");
		assertEquals("my Description2", concept.getDescription());
	}

	@Test
	public void testSetPID() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);
		assertEquals("a protein", concept.getPID());

		// changing PID
		concept.setPID("a gene");
		assertEquals("a gene", concept.getPID());
		concept.setPID("an enzyme");
		assertEquals("an enzyme", concept.getPID());
	}

	@Test
	public void testTags() {
		ONDEXConcept concept = og.getFactory().createConcept("a protein",
				dataSource_tair, cc_protein, et_me);

		boolean raised = false;
		try {
			concept.getTags().clear();
		} catch (UnsupportedOperationException e) {
			raised = true;
		}
		assertTrue(
				"Tags set must be immutable. UnsupportedOperationException must be raised.",
				raised);
	}

}
