/**
 * 
 */
package net.sourceforge.ondex.core.test;

import java.awt.Color;
import java.io.IOException;
import java.util.Set;

import junit.framework.TestCase;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.MetaDataFactory;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.Unit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author taubertj
 * 
 */
public abstract class AbstractONDEXGraphMetaDataTest extends TestCase {

	// implementation of ONDEXGraph to test`
	private ONDEXGraph og;

	// associated ONDEXGraphMetaData
	private ONDEXGraphMetaData omd;

	private AttributeName an;

	private AttributeName an2;

	private AttributeName an3;

	private ConceptClass cc;

	private ConceptClass cc2;

	private ConceptClass cc3;

	private DataSource dataSource;

	private DataSource dataSource2;

	private DataSource dataSource3;

	private EvidenceType et;

	private EvidenceType et2;

	private EvidenceType et3;

	private RelationType rt;

	private RelationType rt2;

	private RelationType rt3;

	private Unit unit;

	private Unit unit2;

	private Unit unit3;

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
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#checkAttributeName(java.lang.String)}
	 * .
	 */
	@Test
	public void testCheckAttributeName() {

		assertFalse("AttributeName wrongly present",
				omd.checkAttributeName("an"));
		assertFalse("AttributeName wrongly present",
				omd.checkAttributeName("an2"));
		testCreateAttributeName();
		assertTrue("AttributeName not present", omd.checkAttributeName("an"));
		assertTrue("AttributeName not present", omd.checkAttributeName("an2"));
		assertTrue("AttributeName not present", omd.checkAttributeName("an3"));
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#checkConceptClass(java.lang.String)}
	 * .
	 */
	@Test
	public void testCheckConceptClass() {

		assertFalse("ConceptClass wrongly present", omd.checkConceptClass("cc"));
		assertFalse("ConceptClass wrongly present",
				omd.checkConceptClass("cc2"));
		testCreateConceptClass();
		assertTrue("ConceptClass not present", omd.checkConceptClass("cc"));
		assertTrue("ConceptClass not present", omd.checkConceptClass("cc2"));
		assertTrue("ConceptClass not present", omd.checkConceptClass("cc3"));
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#checkDataSource(java.lang.String)}
	 * .
	 */
	@Test
	public void testCheckDataSource() {

		assertFalse("DataSource wrongly present",
				omd.checkDataSource("dataSource"));
		assertFalse("DataSource wrongly present",
				omd.checkDataSource("dataSource2"));
		testCreateDataSource();
		assertTrue("DataSource not present", omd.checkDataSource("dataSource"));
		assertTrue("DataSource not present", omd.checkDataSource("dataSource2"));
		assertTrue("DataSource not present", omd.checkDataSource("dataSource3"));
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#checkEvidenceType(java.lang.String)}
	 * .
	 */
	@Test
	public void testCheckEvidenceType() {

		assertFalse("EvidenceType wrongly present", omd.checkEvidenceType("et"));
		assertFalse("EvidenceType wrongly present",
				omd.checkEvidenceType("et2"));
		testCreateEvidenceType();
		assertTrue("EvidenceType not present", omd.checkEvidenceType("et"));
		assertTrue("EvidenceType not present", omd.checkEvidenceType("et2"));
		assertTrue("EvidenceType not present", omd.checkEvidenceType("et3"));
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#checkRelationType(java.lang.String)}
	 * .
	 */
	@Test
	public void testCheckRelationType() {

		assertFalse("RelationType wrongly present", omd.checkRelationType("rt"));
		assertFalse("RelationType wrongly present",
				omd.checkRelationType("rt2"));
		testCreateRelationType();
		assertTrue("RelationType not present", omd.checkRelationType("rt"));
		assertTrue("RelationType not present", omd.checkRelationType("rt2"));
		assertTrue("RelationType not present", omd.checkRelationType("rt3"));
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#checkUnit(java.lang.String)}
	 * .
	 */
	@Test
	public void testCheckUnit() {

		assertFalse("Unit wrongly present", omd.checkUnit("unit"));
		assertFalse("Unit wrongly present", omd.checkUnit("unit2"));
		testCreateUnit();
		assertTrue("Unit not present", omd.checkUnit("unit"));
		assertTrue("Unit not present", omd.checkUnit("unit2"));
		assertTrue("Unit not present", omd.checkUnit("unit3"));
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#createAttributeName(java.lang.String, java.lang.String, java.lang.String, net.sourceforge.ondex.core.Unit, java.lang.Class, net.sourceforge.ondex.core.AttributeName)}
	 * .
	 */
	@Test
	public void testCreateAttributeName() {

		testCreateUnit();
		Unit unit = omd.getUnit("unit");
		assertNotNull("Unit null", unit);

		an = omd.createAttributeName("an", "attribute name",
				"an attribute name", null, String.class, null);
		assertNotNull("AttributeName null", an);
		assertEquals("wrong id", an.getId(), "an");
		assertEquals("wrong fullname", an.getFullname(), "attribute name");
		assertEquals("wrong description", an.getDescription(),
				"an attribute name");
		assertNull("wrong unit", an.getUnit());
		assertEquals("wrong datatype", an.getDataType(), String.class);
		assertNull("wrong specialisationOf", an.getSpecialisationOf());

		an2 = omd.createAttributeName("an2", "attribute name", "", null,
				Double.class, an);
		assertNotNull("AttributeName null", an2);
		assertEquals("wrong id", an2.getId(), "an2");
		assertEquals("wrong fullname", an2.getFullname(), "attribute name");
		assertEquals("wrong description", an2.getDescription(), "");
		assertNull("wrong unit", an2.getUnit());
		assertEquals("wrong datatype", an2.getDataType(), Double.class);
		assertEquals("wrong specialisationOf", an2.getSpecialisationOf(), an);

		an3 = omd.createAttributeName("an3", "", "", unit, Color.class, an2);
		assertNotNull("AttributeName null", an3);
		assertEquals("wrong id", an3.getId(), "an3");
		assertEquals("wrong fullname", an3.getFullname(), "");
		assertEquals("wrong description", an3.getDescription(), "");
		assertEquals("wrong unit", an3.getUnit(), unit);
		assertEquals("wrong datatype", an3.getDataType(), Color.class);
		assertEquals("wrong specialisationOf", an3.getSpecialisationOf(), an2);
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#createConceptClass(java.lang.String, java.lang.String, java.lang.String, net.sourceforge.ondex.core.ConceptClass)}
	 * .
	 */
	@Test
	public void testCreateConceptClass() {

		cc = omd.createConceptClass("cc", "concept class", "a concept class",
				null);
		assertNotNull("ConceptClass null", cc);
		assertEquals("wrong id", cc.getId(), "cc");
		assertEquals("wrong fullname", cc.getFullname(), "concept class");
		assertEquals("wrong description", cc.getDescription(),
				"a concept class");
		assertNull("wrong specialisationOf", cc.getSpecialisationOf());

		cc2 = omd.createConceptClass("cc2", "concept class", "", cc);
		assertNotNull("ConceptClass null", cc2);
		assertEquals("wrong id", cc2.getId(), "cc2");
		assertEquals("wrong fullname", cc2.getFullname(), "concept class");
		assertEquals("wrong description", cc2.getDescription(), "");
		assertEquals("wrong specialisationOf", cc2.getSpecialisationOf(), cc);

		cc3 = omd.createConceptClass("cc3", "", "", cc2);
		assertNotNull("ConceptClass null", cc3);
		assertEquals("wrong id", cc3.getId(), "cc3");
		assertEquals("wrong fullname", cc3.getFullname(), "");
		assertEquals("wrong description", cc3.getDescription(), "");
		assertEquals("wrong specialisationOf", cc3.getSpecialisationOf(), cc2);
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#createDataSource(java.lang.String, java.lang.String, java.lang.String)}
	 * .
	 */
	@Test
	public void testCreateDataSource() {

		dataSource = omd.createDataSource("dataSource",
				"controlled vocabulary", "a controlled vocabulary");
		assertNotNull("DataSource null", dataSource);
		assertEquals("wrong id", dataSource.getId(), "dataSource");
		assertEquals("wrong fullname", dataSource.getFullname(),
				"controlled vocabulary");
		assertEquals("wrong description", dataSource.getDescription(),
				"a controlled vocabulary");

		dataSource2 = omd.createDataSource("dataSource2",
				"controlled vocabulary", "");
		assertNotNull("DataSource null", dataSource2);
		assertEquals("wrong id", dataSource2.getId(), "dataSource2");
		assertEquals("wrong fullname", dataSource2.getFullname(),
				"controlled vocabulary");
		assertEquals("wrong description", dataSource2.getDescription(), "");

		dataSource3 = omd.createDataSource("dataSource3", "", "");
		assertNotNull("DataSource null", dataSource3);
		assertEquals("wrong id", dataSource3.getId(), "dataSource3");
		assertEquals("wrong fullname", dataSource3.getFullname(), "");
		assertEquals("wrong description", dataSource3.getDescription(), "");
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#createEvidenceType(java.lang.String, java.lang.String, java.lang.String)}
	 * .
	 */
	@Test
	public void testCreateEvidenceType() {

		et = omd.createEvidenceType("et", "controlled vocabulary",
				"a controlled vocabulary");
		assertNotNull("EvidenceType null", et);
		assertEquals("wrong id", et.getId(), "et");
		assertEquals("wrong fullname", et.getFullname(),
				"controlled vocabulary");
		assertEquals("wrong description", et.getDescription(),
				"a controlled vocabulary");

		et2 = omd.createEvidenceType("et2", "controlled vocabulary", "");
		assertNotNull("EvidenceType null", et2);
		assertEquals("wrong id", et2.getId(), "et2");
		assertEquals("wrong fullname", et2.getFullname(),
				"controlled vocabulary");
		assertEquals("wrong description", et2.getDescription(), "");

		et3 = omd.createEvidenceType("et3", "", "");
		assertNotNull("EvidenceType null", et3);
		assertEquals("wrong id", et3.getId(), "et3");
		assertEquals("wrong fullname", et3.getFullname(), "");
		assertEquals("wrong description", et3.getDescription(), "");
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#createRelationType(java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean, boolean, boolean, boolean, net.sourceforge.ondex.core.RelationType)}
	 * .
	 */
	@Test
	public void testCreateRelationType() {

		rt = omd.createRelationType("rt", "relation type", "a relation type",
				"inverse", true, true, true, true, null);
		assertNotNull("RelationType null", rt);
		assertEquals("wrong id", rt.getId(), "rt");
		assertEquals("wrong fullname", rt.getFullname(), "relation type");
		assertEquals("wrong description", rt.getDescription(),
				"a relation type");
		assertEquals("wrong inverse", rt.getInverseName(), "inverse");
		assertNull("wrong specialisationOf", rt.getSpecialisationOf());
		assertTrue("wrong isAntisymmetric", rt.isAntisymmetric());
		assertTrue("wrong isReflexive", rt.isReflexive());
		assertTrue("wrong isSymmetric", rt.isSymmetric());
		assertTrue("wrong isTransitiv", rt.isTransitiv());

		rt2 = omd.createRelationType("rt2", "relation type", "a relation type",
				"", false, false, false, false, rt);
		assertNotNull("RelationType null", rt2);
		assertEquals("wrong id", rt2.getId(), "rt2");
		assertEquals("wrong fullname", rt2.getFullname(), "relation type");
		assertEquals("wrong description", rt2.getDescription(),
				"a relation type");
		assertEquals("wrong inverse", rt2.getInverseName(), "");
		assertEquals("wrong specialisationOf", rt2.getSpecialisationOf(), rt);
		assertFalse("wrong isAntisymmetric", rt2.isAntisymmetric());
		assertFalse("wrong isReflexive", rt2.isReflexive());
		assertFalse("wrong isSymmetric", rt2.isSymmetric());
		assertFalse("wrong isTransitiv", rt2.isTransitiv());

		rt3 = omd.createRelationType("rt3", "relation type", "", "", false,
				false, true, true, rt2);
		assertNotNull("RelationType null", rt3);
		assertEquals("wrong id", rt3.getId(), "rt3");
		assertEquals("wrong fullname", rt3.getFullname(), "relation type");
		assertEquals("wrong description", rt3.getDescription(), "");
		assertEquals("wrong inverse", rt3.getInverseName(), "");
		assertEquals("wrong specialisationOf", rt3.getSpecialisationOf(), rt2);
		assertFalse("wrong isAntisymmetric", rt3.isAntisymmetric());
		assertFalse("wrong isReflexive", rt3.isReflexive());
		assertTrue("wrong isSymmetric", rt3.isSymmetric());
		assertTrue("wrong isTransitiv", rt3.isTransitiv());

		RelationType rt4 = omd.createRelationType("rt4", "", "", "", true,
				true, false, false, rt3);
		assertNotNull("RelationType null", rt4);
		assertEquals("wrong id", rt4.getId(), "rt4");
		assertEquals("wrong fullname", rt4.getFullname(), "");
		assertEquals("wrong description", rt4.getDescription(), "");
		assertEquals("wrong inverse", rt4.getInverseName(), "");
		assertEquals("wrong specialisationOf", rt4.getSpecialisationOf(), rt3);
		assertTrue("wrong isAntisymmetric", rt4.isAntisymmetric());
		assertTrue("wrong isReflexive", rt4.isReflexive());
		assertFalse("wrong isSymmetric", rt4.isSymmetric());
		assertFalse("wrong isTransitiv", rt4.isTransitiv());

		RelationType rt5 = omd.createRelationType("rt5", "", "", "", true,
				false, true, false, rt3);
		assertNotNull("RelationType null", rt5);
		assertEquals("wrong id", rt5.getId(), "rt5");
		assertEquals("wrong fullname", rt5.getFullname(), "");
		assertEquals("wrong description", rt5.getDescription(), "");
		assertEquals("wrong inverse", rt5.getInverseName(), "");
		assertEquals("wrong specialisationOf", rt5.getSpecialisationOf(), rt3);
		assertTrue("wrong isAntisymmetric", rt5.isAntisymmetric());
		assertFalse("wrong isReflexive", rt5.isReflexive());
		assertTrue("wrong isSymmetric", rt5.isSymmetric());
		assertFalse("wrong isTransitiv", rt5.isTransitiv());

		RelationType rt6 = omd.createRelationType("rt6", "", "", "", false,
				true, false, true, rt3);
		assertNotNull("RelationType null", rt6);
		assertEquals("wrong id", rt6.getId(), "rt6");
		assertEquals("wrong fullname", rt6.getFullname(), "");
		assertEquals("wrong description", rt6.getDescription(), "");
		assertEquals("wrong inverse", rt6.getInverseName(), "");
		assertEquals("wrong specialisationOf", rt6.getSpecialisationOf(), rt3);
		assertFalse("wrong isAntisymmetric", rt6.isAntisymmetric());
		assertTrue("wrong isReflexive", rt6.isReflexive());
		assertFalse("wrong isSymmetric", rt6.isSymmetric());
		assertTrue("wrong isTransitiv", rt6.isTransitiv());
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#createUnit(java.lang.String, java.lang.String, java.lang.String)}
	 * .
	 */
	@Test
	public void testCreateUnit() {

		unit = omd.createUnit("unit", "unit", "a unit");
		assertNotNull("Unit null", unit);
		assertEquals("wrong id", unit.getId(), "unit");
		assertEquals("wrong fullname", unit.getFullname(), "unit");
		assertEquals("wrong description", unit.getDescription(), "a unit");

		unit2 = omd.createUnit("unit2", "unit", "");
		assertNotNull("Unit null", unit2);
		assertEquals("wrong id", unit2.getId(), "unit2");
		assertEquals("wrong fullname", unit2.getFullname(), "unit");
		assertEquals("wrong description", unit2.getDescription(), "");

		unit3 = omd.createUnit("unit3", "", "");
		assertNotNull("Unit null", unit3);
		assertEquals("wrong id", unit3.getId(), "unit3");
		assertEquals("wrong fullname", unit3.getFullname(), "");
		assertEquals("wrong description", unit3.getDescription(), "");
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#deleteAttributeName(java.lang.String)}
	 * .
	 */
	@Test
	public void testDeleteAttributeName() {

		testCreateAttributeName();
		assertTrue(omd.checkAttributeName("an"));
		assertTrue(omd.deleteAttributeName("an"));
		assertFalse(omd.deleteAttributeName("an"));
		assertFalse(omd.checkAttributeName("an"));
		assertTrue(omd.checkAttributeName("an2"));
		assertTrue(omd.deleteAttributeName("an2"));
		assertFalse(omd.deleteAttributeName("an2"));
		assertFalse(omd.checkAttributeName("an2"));
		assertTrue(omd.checkAttributeName("an3"));
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#deleteConceptClass(java.lang.String)}
	 * .
	 */
	@Test
	public void testDeleteConceptClass() {

		testCreateConceptClass();
		assertTrue(omd.checkConceptClass("cc"));
		assertTrue(omd.deleteConceptClass("cc"));
		assertFalse(omd.deleteConceptClass("cc"));
		assertFalse(omd.checkConceptClass("cc"));
		assertTrue(omd.checkConceptClass("cc2"));
		assertTrue(omd.deleteConceptClass("cc2"));
		assertFalse(omd.deleteConceptClass("cc2"));
		assertFalse(omd.checkConceptClass("cc2"));
		assertTrue(omd.checkConceptClass("cc3"));
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#deleteDataSource(java.lang.String)}
	 * .
	 */
	@Test
	public void testDeleteDataSource() {

		testCreateDataSource();
		assertTrue(omd.checkDataSource("dataSource"));
		assertTrue(omd.deleteDataSource("dataSource"));
		assertFalse(omd.deleteDataSource("dataSource"));
		assertFalse(omd.checkDataSource("dataSource"));
		assertTrue(omd.checkDataSource("dataSource2"));
		assertTrue(omd.deleteDataSource("dataSource2"));
		assertFalse(omd.deleteDataSource("dataSource2"));
		assertFalse(omd.checkDataSource("dataSource2"));
		assertTrue(omd.checkDataSource("dataSource3"));
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#deleteEvidenceType(java.lang.String)}
	 * .
	 */
	@Test
	public void testDeleteEvidenceType() {

		testCreateEvidenceType();
		assertTrue(omd.checkEvidenceType("et"));
		assertTrue(omd.deleteEvidenceType("et"));
		assertFalse(omd.deleteEvidenceType("et"));
		assertFalse(omd.checkEvidenceType("et"));
		assertTrue(omd.checkEvidenceType("et2"));
		assertTrue(omd.deleteEvidenceType("et2"));
		assertFalse(omd.deleteEvidenceType("et2"));
		assertFalse(omd.checkEvidenceType("et2"));
		assertTrue(omd.checkEvidenceType("et3"));
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#deleteRelationType(java.lang.String)}
	 * .
	 */
	@Test
	public void testDeleteRelationType() {

		testCreateRelationType();
		assertTrue(omd.checkRelationType("rt"));
		assertTrue(omd.deleteRelationType("rt"));
		assertFalse(omd.deleteRelationType("rt"));
		assertFalse(omd.checkRelationType("rt"));
		assertTrue(omd.checkRelationType("rt2"));
		assertTrue(omd.deleteRelationType("rt2"));
		assertFalse(omd.deleteRelationType("rt2"));
		assertFalse(omd.checkRelationType("rt2"));
		assertTrue(omd.checkRelationType("rt3"));
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#deleteUnit(java.lang.String)}
	 * .
	 */
	@Test
	public void testDeleteUnit() {

		testCreateUnit();
		assertTrue(omd.checkUnit("unit"));
		assertTrue(omd.deleteUnit("unit"));
		assertFalse(omd.deleteUnit("unit"));
		assertFalse(omd.checkUnit("unit"));
		assertTrue(omd.checkUnit("unit2"));
		assertTrue(omd.deleteUnit("unit2"));
		assertFalse(omd.deleteUnit("unit2"));
		assertFalse(omd.checkUnit("unit2"));
		assertTrue(omd.checkUnit("unit3"));
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#getAttributeName(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetAttributeName() {

		testCreateAttributeName();

		AttributeName _an = omd.getAttributeName("an");
		assertEquals("not matching", _an, an);
		assertEquals("wrong id", _an.getId(), an.getId());
		assertEquals("wrong fullname", _an.getFullname(), an.getFullname());
		assertEquals("wrong description", _an.getDescription(),
				an.getDescription());
		assertEquals("wrong datatype", _an.getDataType(), an.getDataType());
		assertEquals("wrong unit", _an.getUnit(), an.getUnit());
		assertEquals("wrong specialisationOf", _an.getSpecialisationOf(),
				an.getSpecialisationOf());

		AttributeName _an2 = omd.getAttributeName("an2");
		assertEquals("not matching", _an2, an2);
		assertEquals("wrong id", _an2.getId(), an2.getId());
		assertEquals("wrong fullname", _an2.getFullname(), an2.getFullname());
		assertEquals("wrong description", _an2.getDescription(),
				an2.getDescription());
		assertEquals("wrong datatype", _an2.getDataType(), an2.getDataType());
		assertEquals("wrong unit", _an2.getUnit(), an2.getUnit());
		assertEquals("wrong specialisationOf", _an2.getSpecialisationOf(),
				an2.getSpecialisationOf());

		AttributeName _an3 = omd.getAttributeName("an3");
		assertEquals("not matching", _an3, an3);
		assertEquals("wrong id", _an3.getId(), an3.getId());
		assertEquals("wrong fullname", _an3.getFullname(), an3.getFullname());
		assertEquals("wrong description", _an3.getDescription(),
				an3.getDescription());
		assertEquals("wrong datatype", _an3.getDataType(), an3.getDataType());
		assertEquals("wrong unit", _an3.getUnit(), an3.getUnit());
		assertEquals("wrong specialisationOf", _an3.getSpecialisationOf(),
				an3.getSpecialisationOf());
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#getAttributeNames()}
	 * .
	 */
	@Test
	public void testGetAttributeNames() {

		testCreateAttributeName();

		Set<AttributeName> set = omd.getAttributeNames();
		assertEquals("different size", 3, set.size());
		assertTrue("not contained", set.contains(an));
		assertTrue("not contained", set.contains(an2));
		assertTrue("not contained", set.contains(an3));
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#getConceptClass(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetConceptClass() {

		testCreateConceptClass();

		ConceptClass _cc = omd.getConceptClass("cc");
		assertEquals("not matching", _cc, cc);
		assertEquals("wrong id", _cc.getId(), cc.getId());
		assertEquals("wrong fullname", _cc.getFullname(), cc.getFullname());
		assertEquals("wrong description", _cc.getDescription(),
				cc.getDescription());
		assertEquals("wrong specialisationOf", _cc.getSpecialisationOf(),
				cc.getSpecialisationOf());

		ConceptClass _cc2 = omd.getConceptClass("cc2");
		assertEquals("not matching", _cc2, cc2);
		assertEquals("wrong id", _cc2.getId(), cc2.getId());
		assertEquals("wrong fullname", _cc2.getFullname(), cc2.getFullname());
		assertEquals("wrong description", _cc2.getDescription(),
				cc2.getDescription());
		assertEquals("wrong specialisationOf", _cc2.getSpecialisationOf(),
				cc2.getSpecialisationOf());

		ConceptClass _cc3 = omd.getConceptClass("cc3");
		assertEquals("not matching", _cc3, cc3);
		assertEquals("wrong id", _cc3.getId(), cc3.getId());
		assertEquals("wrong fullname", _cc3.getFullname(), cc3.getFullname());
		assertEquals("wrong description", _cc3.getDescription(),
				cc3.getDescription());
		assertEquals("wrong specialisationOf", _cc3.getSpecialisationOf(),
				cc3.getSpecialisationOf());
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#getConceptClasses()}
	 * .
	 */
	@Test
	public void testGetConceptClasses() {

		testCreateConceptClass();

		Set<ConceptClass> set = omd.getConceptClasses();
		assertEquals("different size", 3, set.size());
		assertTrue("not contained", set.contains(cc));
		assertTrue("not contained", set.contains(cc2));
		assertTrue("not contained", set.contains(cc3));
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#getDataSource(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetDataSource() {

		testCreateDataSource();

		DataSource _dataSource = omd.getDataSource("dataSource");
		assertEquals("not matching", _dataSource, dataSource);
		assertEquals("wrong id", _dataSource.getId(), dataSource.getId());
		assertEquals("wrong fullname", _dataSource.getFullname(),
				dataSource.getFullname());
		assertEquals("wrong description", _dataSource.getDescription(),
				dataSource.getDescription());

		DataSource _dataSource2 = omd.getDataSource("dataSource2");
		assertEquals("not matching", _dataSource2, dataSource2);
		assertEquals("wrong id", _dataSource2.getId(), dataSource2.getId());
		assertEquals("wrong fullname", _dataSource2.getFullname(),
				dataSource2.getFullname());
		assertEquals("wrong description", _dataSource2.getDescription(),
				dataSource2.getDescription());

		DataSource _dataSource3 = omd.getDataSource("dataSource3");
		assertEquals("not matching", _dataSource3, dataSource3);
		assertEquals("wrong id", _dataSource3.getId(), dataSource3.getId());
		assertEquals("wrong fullname", _dataSource3.getFullname(),
				dataSource3.getFullname());
		assertEquals("wrong description", _dataSource3.getDescription(),
				dataSource3.getDescription());
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#getDataSources()}.
	 */
	@Test
	public void testGetDataSources() {

		testCreateDataSource();

		Set<DataSource> set = omd.getDataSources();
		assertEquals("different size", 3, set.size());
		assertTrue("not contained", set.contains(dataSource));
		assertTrue("not contained", set.contains(dataSource2));
		assertTrue("not contained", set.contains(dataSource3));
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#getEvidenceType(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetEvidenceType() {

		testCreateEvidenceType();

		EvidenceType _et = omd.getEvidenceType("et");
		assertEquals("not matching", _et, et);
		assertEquals("wrong id", _et.getId(), et.getId());
		assertEquals("wrong fullname", _et.getFullname(), et.getFullname());
		assertEquals("wrong description", _et.getDescription(),
				et.getDescription());

		EvidenceType _et2 = omd.getEvidenceType("et2");
		assertEquals("not matching", _et2, et2);
		assertEquals("wrong id", _et2.getId(), et2.getId());
		assertEquals("wrong fullname", _et2.getFullname(), et2.getFullname());
		assertEquals("wrong description", _et2.getDescription(),
				et2.getDescription());

		EvidenceType _et3 = omd.getEvidenceType("et3");
		assertEquals("not matching", _et3, et3);
		assertEquals("wrong id", _et3.getId(), et3.getId());
		assertEquals("wrong fullname", _et3.getFullname(), et3.getFullname());
		assertEquals("wrong description", _et3.getDescription(),
				et3.getDescription());
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#getEvidenceTypes()}.
	 */
	@Test
	public void testGetEvidenceTypes() {

		testCreateEvidenceType();

		Set<EvidenceType> set = omd.getEvidenceTypes();
		assertEquals("different size", 3, set.size());
		assertTrue("not contained", set.contains(et));
		assertTrue("not contained", set.contains(et2));
		assertTrue("not contained", set.contains(et3));
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#getFactory()}.
	 */
	@Test
	public void testGetFactory() {

		assertNotNull(new MetaDataFactory(omd));
		assertNotNull(omd.getFactory());
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#getRelationType(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetRelationType() {

		testCreateRelationType();

		RelationType _rt = omd.getRelationType("rt");
		assertEquals("not matching", _rt, rt);
		assertEquals("wrong id", _rt.getId(), rt.getId());
		assertEquals("wrong fullname", _rt.getFullname(), rt.getFullname());
		assertEquals("wrong description", _rt.getDescription(),
				rt.getDescription());
		assertEquals("wrong inverse", _rt.getInverseName(), rt.getInverseName());
		assertEquals("wrong specialisationOf", _rt.getSpecialisationOf(),
				rt.getSpecialisationOf());
		assertEquals("wrong isAntisymmetric", _rt.isAntisymmetric(),
				rt.isAntisymmetric());
		assertEquals("wrong isReflexive", _rt.isReflexive(), rt.isReflexive());
		assertEquals("wrong isSymmetric", _rt.isSymmetric(), rt.isSymmetric());
		assertEquals("wrong isTransitiv", _rt.isTransitiv(), rt.isTransitiv());

		RelationType _rt2 = omd.getRelationType("rt2");
		assertEquals("not matching", _rt2, rt2);
		assertEquals("wrong id", _rt2.getId(), rt2.getId());
		assertEquals("wrong fullname", _rt2.getFullname(), rt2.getFullname());
		assertEquals("wrong description", _rt2.getDescription(),
				rt2.getDescription());
		assertEquals("wrong inverse", _rt2.getInverseName(),
				rt2.getInverseName());
		assertEquals("wrong specialisationOf", _rt2.getSpecialisationOf(),
				rt2.getSpecialisationOf());
		assertEquals("wrong isAntisymmetric", _rt2.isAntisymmetric(),
				rt2.isAntisymmetric());
		assertEquals("wrong isReflexive", _rt2.isReflexive(), rt2.isReflexive());
		assertEquals("wrong isSymmetric", _rt2.isSymmetric(), rt2.isSymmetric());
		assertEquals("wrong isTransitiv", _rt2.isTransitiv(), rt2.isTransitiv());

		RelationType _rt3 = omd.getRelationType("rt3");
		assertEquals("not matching", _rt3, rt3);
		assertEquals("wrong id", _rt3.getId(), rt3.getId());
		assertEquals("wrong fullname", _rt3.getFullname(), rt3.getFullname());
		assertEquals("wrong description", _rt3.getDescription(),
				rt3.getDescription());
		assertEquals("wrong inverse", _rt3.getInverseName(),
				rt3.getInverseName());
		assertEquals("wrong specialisationOf", _rt3.getSpecialisationOf(),
				rt3.getSpecialisationOf());
		assertEquals("wrong isAntisymmetric", _rt3.isAntisymmetric(),
				rt3.isAntisymmetric());
		assertEquals("wrong isReflexive", _rt3.isReflexive(), rt3.isReflexive());
		assertEquals("wrong isSymmetric", _rt3.isSymmetric(), rt3.isSymmetric());
		assertEquals("wrong isTransitiv", _rt3.isTransitiv(), rt3.isTransitiv());
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#getRelationTypes()}.
	 */
	@Test
	public void testGetRelationTypes() {

		testCreateRelationType();

		Set<RelationType> set = omd.getRelationTypes();
		assertEquals("different size", 6, set.size());
		assertTrue("not contained", set.contains(rt));
		assertTrue("not contained", set.contains(rt2));
		assertTrue("not contained", set.contains(rt3));
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#getUnit(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetUnit() {

		testCreateUnit();

		Unit _unit = omd.getUnit("unit");
		assertEquals("not matching", _unit, unit);
		assertEquals("wrong id", _unit.getId(), unit.getId());
		assertEquals("wrong fullname", _unit.getFullname(), unit.getFullname());
		assertEquals("wrong description", _unit.getDescription(),
				unit.getDescription());

		Unit _unit2 = omd.getUnit("unit2");
		assertEquals("not matching", _unit2, unit2);
		assertEquals("wrong id", _unit2.getId(), unit2.getId());
		assertEquals("wrong fullname", _unit2.getFullname(),
				unit2.getFullname());
		assertEquals("wrong description", _unit2.getDescription(),
				unit2.getDescription());

		Unit _unit3 = omd.getUnit("unit3");
		assertEquals("not matching", _unit3, unit3);
		assertEquals("wrong id", _unit3.getId(), unit3.getId());
		assertEquals("wrong fullname", _unit3.getFullname(),
				unit3.getFullname());
		assertEquals("wrong description", _unit3.getDescription(),
				unit3.getDescription());
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXGraphMetaData#getUnits()}.
	 */
	@Test
	public void testGetUnits() {

		testCreateUnit();

		Set<Unit> set = omd.getUnits();
		assertEquals("different size", 3, set.size());
		assertTrue("not contained", set.contains(unit));
		assertTrue("not contained", set.contains(unit2));
		assertTrue("not contained", set.contains(unit3));
	}

	/**
	 * Test method for
	 * {@link net.sourceforge.ondex.core.ONDEXAssociable#getSID()}.
	 */
	@Test
	public void testGetSID() {

		assertNotNull("SID null", omd.getSID());
		assertTrue("SID negative or zero", omd.getSID() > 0);
	}

	@Test
	public void testAttributeNames() {
		boolean raised = false;
		try {
			omd.getAttributeNames().clear();
		} catch (UnsupportedOperationException e) {
			raised = true;
		}
		assertTrue(
				"AttributeNames set must be immutable. UnsupportedOperationException must be raised.",
				raised);
	}

	@Test
	public void testConceptClasses() {
		boolean raised = false;
		try {
			omd.getConceptClasses().clear();
		} catch (UnsupportedOperationException e) {
			raised = true;
		}
		assertTrue(
				"ConceptClasses set must be immutable. UnsupportedOperationException must be raised.",
				raised);
	}

	@Test
	public void testEvidenceTypes() {
		boolean raised = false;
		try {
			omd.getEvidenceTypes().clear();
		} catch (UnsupportedOperationException e) {
			raised = true;
		}
		assertTrue(
				"EvidenceType set must be immutable. UnsupportedOperationException must be raised.",
				raised);
	}

	@Test
	public void testRelationTypes() {
		boolean raised = false;
		try {
			omd.getRelationTypes().clear();
		} catch (UnsupportedOperationException e) {
			raised = true;
		}
		assertTrue(
				"RelationTypes set must be immutable. UnsupportedOperationException must be raised.",
				raised);
	}

	@Test
	public void testUnits() {
		boolean raised = false;
		try {
			omd.getUnits().clear();
		} catch (UnsupportedOperationException e) {
			raised = true;
		}
		assertTrue(
				"Units set must be immutable. UnsupportedOperationException must be raised.",
				raised);
	}
}
