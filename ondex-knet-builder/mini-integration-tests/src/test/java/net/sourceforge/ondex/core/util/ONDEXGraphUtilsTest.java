package net.sourceforge.ondex.core.util;

import static net.sourceforge.ondex.core.util.ONDEXGraphUtils.getAttribute;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Assert;
import org.junit.Test;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;

/**
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 Jan 2020</dd></dl>
 *
 */
public class ONDEXGraphUtilsTest
{
	private ONDEXGraph graph = new MemoryONDEXGraph ( "testGraph" );
	private String attrNameId = "testType", attrValue = "test Value";
	private String intAttrNameId = "testIntType";
	int intAttrValue = 42; // You're not a true geek if you don't know what I'm citing...

	private ONDEXConcept concept;
	
	
	{
		CachedGraphWrapper gw = CachedGraphWrapper.getInstance ( graph );
				
		AttributeName attrName = gw.getAttributeName ( attrNameId, attrNameId, "Test Type", String.class );
		AttributeName intAttrName = gw.getAttributeName ( intAttrNameId, intAttrNameId, "Int Test Type", Integer.class );
		
		ConceptClass cc = gw.getConceptClass ( "TestCC", "A Test CC", "A test concept class.", null );
		DataSource ds = gw.getDataSource ( "testDS", "Test Data Source", "A test data source." );
		EvidenceType ev = gw.getEvidenceType ( "testEvidence", "Test Evidence", "A test evidence type." );
		
		concept = gw.getConcept ( "A", "", "Test Concept A", ds, cc, ev );
		concept.createAttribute ( attrName, attrValue, false );
		concept.createAttribute ( intAttrName, intAttrValue, false );
	}
	
	
	@Test
	public void testGetAttribute ()
	{
		assertNotNull ( "getAttribute() returns null!", getAttribute ( graph, concept, attrNameId ) );
	}

	@Test
	public void testGetAttributeMissingAttr ()
	{
		assertNull (
			"getAttribute() doesn't work for non exisiting attr!",
			getAttribute ( graph, concept, "fooAttrId", false, true ) 
		);
	}

	@Test
	public void testGetAttributeNullEntity ()
	{
		assertNull (
			"getAttribute() doesn't work for null concept!",
			getAttribute ( graph, null, attrNameId, false, false ) 
		);
	}
	

	@Test
	public void testGetAttrValueAsString ()
	{
		assertEquals ( 
			"getAttrValueAsString() wrong!",
			attrValue,
			ONDEXGraphUtils.getAttrValueAsString ( graph, concept, attrNameId ) 
		);
	}
	
	@Test
	public void testGetAttrValueAsStringMissingAttr ()
	{
		assertNull ( 
			"getAttrValueAsString() for missing attr wrong!",
			ONDEXGraphUtils.getAttrValueAsString ( graph, concept, "fooAttrId", false, true ) 
		);
	}
	
	@Test
	public void testGetAttrValueAsStringNullEntity ()
	{
		assertNull ( 
			"getAttrValueAsString() for null concept wrong!",
			ONDEXGraphUtils.getAttrValueAsString ( graph, null, attrNameId, false, false ) 
		);
	}


	@Test
	public void testGetIntAttrValue ()
	{
		assertEquals ( 
			"getAttrValue() wrong!",
			intAttrValue,
			(int) ONDEXGraphUtils.getAttrValue ( graph, concept, intAttrNameId ) 
		);
	}
	
	@Test
	public void testGetIntAttrValueMissingAttr ()
	{
		assertNull ( 
			"getAttrValue() for missing attr is wrong!",
			(Integer) ONDEXGraphUtils.getAttrValue ( graph, concept, "fooId", false, true ) 
		);
	}
	
	@Test
	public void testGetIntAttrValueNullEntity ()
	{
		assertNull ( 
			"getAttrValue() for null concept is wrong!",
			(Integer) ONDEXGraphUtils.getAttrValue ( graph, null, attrNameId, false, false ) 
		);
	}	

	
	
	@SuppressWarnings ( "unused" )
	@Test ( expected = IllegalArgumentException.class )
	public void testGetIntAttrValueMissingAttrRestriction ()
	{
		Integer v = ONDEXGraphUtils.getAttrValue ( graph, concept, "fooAttrId" );
	}	
	
	@SuppressWarnings ( "unused" )
	@Test ( expected = IllegalArgumentException.class )
	public void testGetIntAttrValueNullEntityRestriction ()
	{
		Integer v = ONDEXGraphUtils.getAttrValue ( graph, null, intAttrNameId );
	}	
	
}
