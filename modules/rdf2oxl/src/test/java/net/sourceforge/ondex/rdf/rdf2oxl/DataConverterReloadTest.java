package net.sourceforge.ondex.rdf.rdf2oxl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.Resources;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;

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
			"target/data_reload_test.xml", 
			"target/data_reload_test_tdb",
			Pair.of ( new FileInputStream ( "src/main/assembly/resources/data/bioknet.owl" ), "RDF/XML" ),
			Pair.of ( new FileInputStream ( "src/main/assembly/resources/data/bk_ondex.owl" ), "RDF/XML" ),
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
	public void testReloadedEvidence ()
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
		
		Set<EvidenceType> evs = concept.getEvidence ();
		assertNotNull ( "Test Evidence not found (null)", evs );
		assertEquals ( "Test Evidence cardinality wrong", 1, evs.size () );
		
		EvidenceType ev = evs.iterator ().next ();
		
		assertEquals ( "Test Evidence label is wrong!", "Imported from database", ev.getFullname () );
		assertEquals ( "Test Evidence description is wrong!", "imported from database", ev.getDescription () );
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
	
	@Test
	public void testIriAttribute ()
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

		AttributeName aname = meta.getAttributeName ( "iri" );

		assertNotNull ( "Test Attribute Name not found!", aname );

		Attribute attr = concept.getAttribute ( aname );
		assertNotNull ( "IRI Attribute not found!", attr );
		String iri = (String) attr.getValue ();
		assertTrue ( 
			"IRI value is wrong (" + iri + ")!", 
			iri.matches ( "^http://.+/resources/publication_26396590$" ) 
		);
	}	
	
	
	@Test
	public void testReloadedNumericAttributes ()
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

		AttributeName aname = meta.getAttributeName ( "PVALUE" );

		assertNotNull ( "Test Attribute Name not found!", aname );

		Attribute attr = concept.getAttribute ( aname );
		assertNotNull ( "Test Attribute not found!", attr );
		// type is in the metadata definition for this attribute.
		assertEquals ( "Test Attribute's value is wrong!", 0.001f, attr.getValue () );
		
		aname = meta.getAttributeName ( "YEAR" );
		assertNotNull ( "YEAR Attribute Type not found!", aname );
		attr = concept.getAttribute ( aname );
		assertNotNull ( "YEAR not found!", attr );
		// Unspecified attribute/value type, should fall into BigDecimal
		assertEquals ( "YEAR's value is wrong!", 2015, attr.getValue () );
	}	
	
	@Test
	public void testReloadedAttributeSet ()
	{
		ONDEXGraphMetaData meta = resultGraph.getMetaData ();
		
		ConceptClass cc = meta.getConceptClass ( "Annotation" );
		assertNotNull ( "Test Class not found!", cc );

		ONDEXConcept concept = resultGraph
		.getConceptsOfConceptClass ( cc )
		.stream ()
		.filter ( c -> "testAnnotation".equals ( c.getPID () ) )
		.findFirst ()
		.orElse ( null );
		
		assertNotNull ( "Test Concept not found!", concept );

		AttributeName aname = meta.getAttributeName ( "EVIDENCE" );

		assertNotNull ( "Test Attribute Name not found!", aname );

		Attribute attr = concept.getAttribute ( aname );
		assertNotNull ( "Test Attribute not found!", attr );
		
		Object aval = attr.getValue ();
		assertNotNull ( "Evidences Attribute not found!", aval);
		assertTrue ( "Evidences Attribute isn't a set!", aval instanceof Set );

		@SuppressWarnings ( "unchecked" )
		SortedSet<String> evs = new TreeSet<> ( (Set<String>) aval );
		
		assertEquals ( "Eveidences Attribute has wrong size!", 2, evs.size () );

		assertEquals ( "Evidence 1 is wrong!", "Foo Evidence 1", evs.first () );
		assertEquals ( "Evidence 2 is wrong!", "Foo Evidence 2", evs.last () );
	}	

	
	@Test
	public void testReloadedNumericAttributeSet ()
	{
		ONDEXGraphMetaData meta = resultGraph.getMetaData ();
		
		ConceptClass cc = meta.getConceptClass ( "Annotation" );
		assertNotNull ( "Test Class not found!", cc );

		ONDEXConcept concept = resultGraph
		.getConceptsOfConceptClass ( cc )
		.stream ()
		.filter ( c -> "testAnnotation".equals ( c.getPID () ) )
		.findFirst ()
		.orElse ( null );
		
		assertNotNull ( "Test Concept not found!", concept );

		AttributeName aname = meta.getAttributeName ( "Score" );

		assertNotNull ( "Test Attribute Name not found!", aname );

		Attribute attr = concept.getAttribute ( aname );
		assertNotNull ( "Test Attribute not found!", attr );
		
		Object aval = attr.getValue ();
		assertNotNull ( "Evidences Attribute not found!", aval);
		assertTrue ( "Evidences Attribute isn't a set!", aval instanceof Set );

		@SuppressWarnings ( "unchecked" )
		SortedSet<BigDecimal> evs = new TreeSet<> ( (Set<BigDecimal>) aval );
		
		assertEquals ( "Eveidences Attribute has wrong size!", 2, evs.size () );

		assertEquals ( "Evidence 1 is wrong!", 0.90, evs.first ().doubleValue (), 0.001 );
		assertEquals ( "Evidence 2 is wrong!", 0.95, evs.last ().doubleValue (), 0.001 );
	}	
		
	@Test
	public void testReloadedRelation ()
	{
		ONDEXGraphMetaData meta = resultGraph.getMetaData ();
		
		RelationType rt = meta.getRelationType ( "related" );
		assertNotNull ( "Test Relation Type not found!", rt );
						
		Set<ONDEXRelation> rels = resultGraph.getRelationsOfRelationType ( rt );
		assertNotNull ( "Test Relation not found (null result)!", rels );
		assertNotNull ( "Test Relation not found (wrong size result)!", rels.size () == 1 );
				
		ONDEXRelation rel = rels.iterator ().next ();

		assertEquals ( "From is wrong!", "26396590", rel.getFromConcept ().getPID () );
		assertEquals ( "To is wrong!", "17206375", rel.getToConcept ().getPID () );
		
		AttributeName aname = meta.getAttributeName ( "Score" );
		assertNotNull ( "Score Attribute Type not found!", aname );
		Attribute attr = rel.getAttribute ( aname );
		assertNotNull ( "Score not found!", attr );
		// Unspecified attribute/value type, should fallback to BigDecimal
		assertEquals ( "Score value is wrong!", BigDecimal.valueOf ( 0.95 ), attr.getValue () );
	}
	
	@Test
	public void testRelationIRIAttribute ()
	{
		ONDEXGraphMetaData meta = resultGraph.getMetaData ();
		
		RelationType rt = meta.getRelationType ( "related" );
		assertNotNull ( "Test Relation Type not found!", rt );
						
		Set<ONDEXRelation> rels = resultGraph.getRelationsOfRelationType ( rt );
		assertNotNull ( "Test Relation not found!", rels );
				
		ONDEXRelation rel = rels.iterator ().next ();
		
		AttributeName aname = meta.getAttributeName ( "iri" );
		Attribute attr = rel.getAttribute ( aname );
		assertNotNull ( "Relation's iri attribute not found!", attr );
		String iri = (String) attr.getValue ();
		assertTrue ( 
			"IRI value is wrong (" + iri + ")!", 
			iri.matches ( "^http://.+/resources/testRel1$" ) 
		);
	}	
	
}
