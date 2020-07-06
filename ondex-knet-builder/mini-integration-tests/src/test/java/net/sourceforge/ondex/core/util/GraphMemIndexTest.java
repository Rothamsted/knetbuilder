package net.sourceforge.ondex.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;

/**
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>Oct 25, 2019</dd></dl>
 *
 */
public class GraphMemIndexTest
{
	private ONDEXGraph graph = new MemoryONDEXGraph ( "default" );
	private ONDEXConcept conceptA;
	private ONDEXConcept conceptB;
	private ONDEXRelation relAB;
	private AttributeName iriAttr;
	private GraphMemIndex memIdx;
	
	private static final String NS = "http://www.foo.net/ondex/";
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	
	@Before
	public void initGraph ()
	{
		CachedGraphWrapper gw = CachedGraphWrapper.getInstance ( graph );
		
		iriAttr = gw.getAttributeName ( "iri", "IRI", "The IRI/URI", String.class );
		
		ConceptClass cc = gw.getConceptClass ( "TestCC", "A Test CC", "A test concept class.", null );
		DataSource ds = gw.getDataSource ( "testDS", "Test Data Source", "A test data source." );
		EvidenceType ev = gw.getEvidenceType ( "testEvidence", "Test Evidence", "A test evidence type." );
		
		conceptA = gw.getConcept ( "A", "", "Test Concept A", ds, cc, ev );
		conceptA.createAttribute ( iriAttr, NS + conceptA.getPID (), true );

		conceptB = gw.getConcept ( "B", "", "Test Concept B", ds, cc, ev );
		conceptB.createAttribute ( iriAttr, NS + conceptB.getPID (), true );
		
		RelationType rtest = gw.getRelationType ( "testRelation", false, false, true, false );
		relAB = gw.getRelation ( conceptA, conceptB, rtest, ev );
		relAB.createAttribute ( iriAttr, NS + conceptA.getPID () + "_" + conceptB.getPID (), true );
		
		memIdx = GraphMemIndex.getInstance ( graph );
	}
	
	@Test
	public void testIRIs ()
	{
		// Concepts
		Stream.of ( conceptA, conceptB ).forEach ( concept -> 
		{
			String probedIri = NS + concept.getPID ();
			ONDEXConcept fetchedConcept = memIdx.get ( "iri", probedIri );
			
			assertNotNull ( "Entity " + probedIri + " not found!", fetchedConcept );
			
			assertEquals ( "Fetched entity != original!", fetchedConcept.getPID (), concept.getPID () );
			assertEquals ( "Fetched concept has an unexpected IRI!", probedIri, fetchedConcept.getAttribute ( iriAttr ).getValue () );
		});
		
		// Relations
		String probedIri = NS + conceptA.getPID () + "_" + conceptB.getPID ();
		ONDEXRelation fetchedRel = memIdx.get ( "iri", probedIri );
		
		assertNotNull ( "Entity " + probedIri + " not found!", fetchedRel );
		assertEquals ( "Fetched concept has an unexpected IRI!", probedIri, fetchedRel.getAttribute ( iriAttr ).getValue () );		
	}

}
