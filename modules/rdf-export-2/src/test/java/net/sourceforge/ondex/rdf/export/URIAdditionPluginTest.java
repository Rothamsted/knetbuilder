package net.sourceforge.ondex.rdf.export;

import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.ns;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.core.util.CachedGraphWrapper;

/**
 * A few tests for {@link URIAdditionPlugin}.
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Jan 2019</dd></dl>
 *
 */
public class URIAdditionPluginTest
{
	private ONDEXGraph graph = new MemoryONDEXGraph ( "default" );
	private ONDEXConcept conceptA;
	private ONDEXConcept conceptB;
	private ONDEXRelation relAB;
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	
	@Before
	public void initGraph ()
	{
		CachedGraphWrapper gw = CachedGraphWrapper.getInstance ( graph );
		
		ConceptClass cc = gw.getConceptClass ( "TestCC", "A Test CC", "A test concept class.", null );
		DataSource ds = gw.getDataSource ( "testDS", "Test Data Source", "A test data source." );
		EvidenceType ev = gw.getEvidenceType ( "testEvidence", "Test Evidence", "A test evidence type." );
		conceptA = gw.getConcept ( "A", "", "Test Concept A", ds, cc, ev );
		conceptB = gw.getConcept ( "B", "", "Test Concept B", ds, cc, ev );
		RelationType rtest = gw.getRelationType ( "testRelation", false, false, true, false );
		relAB = gw.getRelation ( conceptA, conceptB, rtest, ev );
	}
	
	@Test
	public void testBasics () {
		testTemplate ( null );
	}

	@Test
	public void testParams () {
		testTemplate ( "http://www.somewhere.net/examples/" );
	}
	
	
	private void testTemplate ( String instanceNamespace )
	{
		URIAdditionPlugin uriAdder = new URIAdditionPlugin ();
		uriAdder.setONDEXGraph ( this.graph );
		
		if ( instanceNamespace != null ) uriAdder.setInstanceNamespace ( instanceNamespace );
		else instanceNamespace = ns ( "bkr" ); 
		final String instanceNamespaceConst = instanceNamespace;	
		
		
		uriAdder.run ();
		
		AttributeName uriAttribType = graph.getMetaData ().getAttributeName ( "iri" );
		assertNotNull ( "URI Attribute Type not stored!", uriAttribType );

		Stream.of ( conceptA, conceptB )
		.forEach ( concept -> 
		{
			Attribute uriAttr = concept.getAttribute ( uriAttribType );
			assertNotNull ( "URI Attribute for " + concept.getPID () + " not stored!", uriAttr );
			
			String uri = (String) uriAttr.getValue ();			
			log.info ( "URI for {} is '{}'", concept.getPID (), uri );
			
			assertNotNull ( "URI value for " + concept.getPID () + " is null!", uri );
			assertTrue ( "URI format for " + concept.getPID () + " is wrong (namespace)", uri.startsWith ( instanceNamespaceConst ) );
			assertTrue ( 
				"URI format for " + concept.getPID () + " is wrong (ID)",
				uri.endsWith ( ( concept.getOfType ().getId () + "_" + concept.getPID () ).toLowerCase () ) 
			);
		});
		
		Attribute uriAttr = relAB.getAttribute ( uriAttribType );
		assertNotNull ( "URI Attribute for A->B not stored!", uriAttr );
		
		String uri = (String) uriAttr.getValue ();
		log.info ( "URI for A->B is '{}'", uri );
		
		assertNotNull ( "URI value for A->B is null!", uri );
		assertTrue ( "URI format for A->B is wrong (namespace)", uri.startsWith ( instanceNamespace ) );
		assertTrue ( 
			"URI format for A->B is wrong (ID)", 
			uri.contains ( ( 
				relAB.getOfType ().getId () + "_" 
			  + conceptA.getOfType ().getId () + "_" + conceptA.getPID () + "_" 
				+ conceptB.getOfType ().getId () + "_" + conceptB.getPID () 
			).toLowerCase () ) 
		);
	}
}
