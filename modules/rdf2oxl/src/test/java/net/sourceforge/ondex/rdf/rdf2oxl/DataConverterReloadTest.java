package net.sourceforge.ondex.rdf.rdf2oxl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.Resources;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Oct 2018</dd></dl>
 *
 */
public class DataConverterReloadTest extends AbstractConverterReloadTest
{
	@BeforeClass
	public static void initData () throws IOException
	{
		resultGraph = loadOxl (
			"target/concepts_reload_test.xml", 
			"target/concepts_reload_test_tdb",
			Pair.of ( Resources.getResource ( "bioknet.owl" ).openStream (), "RDF/XML" ),
			Pair.of ( Resources.getResource ( "oxl_templates_test/bk_ondex.owl" ).openStream (), "RDF/XML" ),
			Pair.of ( Resources.getResource ( "support_test/publications.ttl" ).openStream (), "TURTLE" )
		);
	}
	
	
	@Test
	public void testConceptsReload ()
	{
		ConceptClass cc = resultGraph.getMetaData ().getConceptClass ( "Publication" );
		assertNotNull ( "Test Class not found!", cc );
		
		ONDEXConcept concept = resultGraph
		.getConceptsOfConceptClass ( cc )
		.stream ()
		.filter ( c -> "26396590".equals ( c.getPID () ) )
		.findFirst ()
		.orElse ( null );
		
		assertNotNull ( "Test Concept not found!", concept );
	}
	
	
	@Test
	public void testReloadedAccession ()
	{
		ConceptClass cc = resultGraph.getMetaData ().getConceptClass ( "Publication" );
		assertNotNull ( "Test Class not found!", cc );

		ONDEXConcept concept = resultGraph
		.getConceptsOfConceptClass ( cc )
		.stream ()
		.filter ( c -> "17206375".equals ( c.getPID () ) )
		.findFirst ()
		.orElse ( null );

		assertNotNull ( "Test Concept not found!", concept );
		
		ConceptAccession acc = concept.getConceptAccessions ()
		.stream ()
		.filter ( a -> "17206375".equals ( a.getAccession () ) )
		.findFirst ()
		.orElse ( null );
		
		assertNotNull ( "Test Accession not found!", acc );
		
		DataSource ds = acc.getElementOf ();
		assertNotNull ( "Test Data Source not found!", ds );
		
		assertTrue ( 
			"Test Data Source not found!", 
			ds.getDescription ().startsWith ( "PubMed comprises more than 19 million" ) 
		);		
	}
	
	@Test
	public void testReloadedAttribute ()
	{
		ONDEXGraphMetaData meta = resultGraph.getMetaData ();
		
		ConceptClass cc = meta.getConceptClass ( "Publication" );
		assertNotNull ( "Test Class not found!", cc );

		ONDEXConcept concept = resultGraph
		.getConceptsOfConceptClass ( cc )
		.stream ()
		.filter ( c -> "26396590".equals ( c.getPID () ) )
		.findFirst ()
		.orElse ( null );
		
		assertNotNull ( "Test Concept not found!", concept );

		AttributeName aname = meta.getAttributeName ( "JOURNAL_REF" );

		assertNotNull ( "Test Attribute Name not found!", aname );
		assertEquals ( "Test Attribute's label is wrong!", "Journal reference for a publication", aname.getFullname () );
		assertEquals ( "Test Attribute's description is wrong!", "The Journal reference for a publication", aname.getDescription () );
		assertEquals ( "Test Attribute's Java type is wrong!", "java.lang.String", aname.getDataTypeAsString () );

		Attribute attr = concept.getAttribute ( aname );
		assertNotNull ( "Test Attribute not found!", attr );
		assertEquals ( "Test Attribute's value is wrong!", "Biotechnology for biofuels", attr.getValue () );
	}
}
