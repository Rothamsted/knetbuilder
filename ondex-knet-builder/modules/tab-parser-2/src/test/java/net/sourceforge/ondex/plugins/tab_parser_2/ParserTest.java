package net.sourceforge.ondex.plugins.tab_parser_2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.io.Resources;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.core.util.ONDEXGraphOperations;
import net.sourceforge.ondex.core.util.ONDEXGraphUtils;
import net.sourceforge.ondex.plugins.tab_parser_2.config.ConfigParser;
import net.sourceforge.ondex.tools.tab.importer.PathParser;

/**
 * Tests the parser with real use cases
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>6 Jan 2017</dd></dl>
 *
 */
public class ParserTest
{
	private Logger log = Logger.getLogger ( this.getClass () );
	
	@Test
	public void testTutorialGeneEx () throws Exception
	{
		Reader schemaReader = new InputStreamReader ( 
			Resources.getResource ( this.getClass (), "/ondex_tutorial_2016/gene_example_parser_cfg.xml" ).openStream (),
			"UTF-8"
		);
		ONDEXGraph graph = new MemoryONDEXGraph ( "default" );
	

		PathParser pp = ConfigParser.parseConfigXml ( 
			schemaReader, graph, "target/test-classes/ondex_tutorial_2016/gene_example.tsv" 
		);
		pp.parse ();
		
		
		Set<ONDEXConcept> concepts = graph.getConcepts ();
		final ONDEXConcept testConcept[] = new ONDEXConcept [ 1 ];
		assertFalse ( "No concepts found in the test file!", concepts.isEmpty () );
		assertTrue ( 
			"Expected protein not found in the test file!",
			concepts
			.stream()
			.anyMatch ( concept ->
		  {
		  	for ( ConceptAccession acc: concept.getConceptAccessions () )
		  		if ( "ENSG00000115317".equals ( acc.getAccession () ) )
		  		{
		  			testConcept [ 0 ] = concept;
		  			return true;
		  		}
		  	return false;
		  })
		);

		
		assertTrue ( 
			"Expected Chromosome attribute not found!",
			testConcept [ 0 ].getAttributes ()
			.stream ()
			.anyMatch ( attr -> 
		  {
		  	return 
		  		"Chromosome".equals ( attr.getOfType ().getId () ) 
		  		&&  Integer.valueOf ( 2 ).equals ( attr.getValue () ); 
		  })
		);
		
		
		Set<ONDEXRelation> relations = graph.getRelations ();
		assertFalse ( "No relations found in the test file!", relations.isEmpty () );
		
		assertTrue ( 
			"Expected interaction not found in the test file!",
			relations
			.stream ()
			.anyMatch ( relation -> 
			{
		  	ONDEXConcept from = relation.getFromConcept ();
		  	ONDEXConcept to = relation.getToConcept ();
		  	if ( !"ENSG00000143801".equals ( from.getConceptAccessions ().iterator ().next ().getAccession () ) )
		  		return false;
		  	if ( !"P49810".equals ( to.getConceptAccessions ().iterator ().next ().getAccession () ) )
		  		return false;
		  	return true;
			})
		);
	}

	
	/**
	 * Tests that multiple attributes like accession or name are taken 
	 */
	@Test
	public void testMultipleAttributes () throws Exception
	{
		Reader schemaReader = new InputStreamReader ( 
			Resources.getResource ( this.getClass (), "/multi_attr_test/Arabidopsis_protDomain_config.xml" ).openStream (),
			"UTF-8"
		);
		ONDEXGraph graph = new MemoryONDEXGraph ( "default" );
	

		PathParser pp = ConfigParser.parseConfigXml ( 
			schemaReader, graph, "target/test-classes//multi_attr_test/protDomain.tsv" 
		);
		pp.parse ();
		
		Set<ONDEXConcept> concepts = graph.getConcepts ();
		assertFalse ( "No concepts found in the test file!", concepts.isEmpty () );

		assertTrue ( 
			"Test names not found!", 
			concepts
			.stream ()
			.anyMatch ( concept ->
		  {
		  	for ( ConceptAccession acc: concept.getConceptAccessions () )
		  	{
		  		if ( !"IPR004815".equals ( acc.getAccession () ) ) continue;
		  		boolean isName1 = false, isName2 = false;
		  		for ( ConceptName name: concept.getConceptNames () )
		  		{
		  			if ( "PF02190".equals ( name.getName () ) ) isName1 = true;
		  			else if ( "Pept_S16_lon".equals ( name.getName () ) ) isName2 = true;
		  			
		  			if ( isName1 && isName2 ) return true;
		  		}
		  	}
		  	return false;
		  })
		);			
	}
	
	
	
	/**
	 * Tests blank lines.
	 */
	@Test
	public void testBlankLinesFilter () throws Exception
	{
		Reader schemaReader = new InputStreamReader ( 
			Resources.getResource ( this.getClass (), "/multi_attr_test/Arabidopsis_protDomain_config.xml" ).openStream (),
			"UTF-8"
		);
		ONDEXGraph graph = new MemoryONDEXGraph ( "default" );

		PathParser pp = ConfigParser.parseConfigXml ( 
			schemaReader, graph, "target/test-classes//multi_attr_test/protDomain_empty_rows.tsv" 
		);
		pp.parse ();
		
		assertEquals ( "Wrong no of retrieved relations!", 7, graph.getRelations ().size () );
	}
	
	/**
	 * Tests cases where concept-defining cells are all empty (id/accessions/names/attributes)
	 * This is about #31
	 */
	@Test
	public void testEmptyValues () throws Exception
	{
		Reader schemaReader = new InputStreamReader ( 
			Resources.getResource ( this.getClass (), "/ondex_tutorial_2016/gene_example_parser_cfg.xml" ).openStream (),
			"UTF-8"
		);
		ONDEXGraph graph = new MemoryONDEXGraph ( "default" );

		PathParser pp = ConfigParser.parseConfigXml ( 
			schemaReader, graph, "target/test-classes/ondex_tutorial_2016/gene_example_holes.tsv" 
		);
		pp.parse ();
		
		ONDEXGraphOperations.dumpAll ( graph );
		
		Function<String, ONDEXConcept> conceptFinder = 
			pid -> graph.getConcepts ()
			.stream ()
			.filter ( c -> pid.equals ( c.getPID () ) )
			.findAny ()
			.orElse ( null );
		
		ONDEXConcept unlinkedProto = conceptFinder.apply ( "O43426" );		
		assertNotNull ( "Probe protein not found!", unlinkedProto );
		assertTrue ( "Probe protein is linked!", graph.getRelationsOfConcept ( unlinkedProto ).isEmpty () );
		
		ONDEXConcept unlinkedGene = conceptFinder.apply ( "ENSG00000185345" );
		assertNotNull ( "Probe gene not found!", unlinkedGene );
		assertTrue ( "Probe gene is linked!", graph.getRelationsOfConcept ( unlinkedGene ).isEmpty () );

		ONDEXConcept noAttrGene = conceptFinder.apply ( "ENSG00000184381" );
		assertNotNull ( "No-attrib gene not found!", noAttrGene );
		assertNotNull ( "No-attrib gene is linked!", graph.getRelationsOfConcept ( noAttrGene ).isEmpty () );
		assertEquals ( "Shouldn't have any (variable) attribute!", 1, noAttrGene.getAttributes ().size () );		
	}
	
	/**
	 * Shows that relations are duplicated when occurring in multiple rows
	 */
	@Test
	public void testDupedRelation () throws Exception
	{
		Reader schemaReader = new InputStreamReader ( 
			Resources.getResource ( this.getClass (), "/duped_relations/duped_rels_cfg.xml" ).openStream (),
			"UTF-8"
		);
		ONDEXGraph graph = new MemoryONDEXGraph ( "default" );

		PathParser pp = ConfigParser.parseConfigXml ( 
			schemaReader, graph, "target/test-classes/duped_relations/duped_rels.tsv" 
		);
		//pp.setProcessingOptions ( PathParser.MERGE_ACC, PathParser.MERGE_NAME, PathParser.MERGE_GDS );
		// Resets MERGE_ACC
		pp.setProcessingOptions ( new String [ 0 ] );
		pp.parse ();
		
		ONDEXGraphOperations.dumpAll ( graph );
		
		assertEquals ( "Relation count is wrong!", 4, graph.getRelations ().size () );
	}


	/**
	 * Tests types defined in the input
	 */
	@Test
	public void testDynamicConcepts () throws Exception
	{
		Reader schemaReader = new InputStreamReader ( 
			Resources.getResource ( this.getClass (), "/dynamic-types/dynamic-concepts-cfg.xml" ).openStream (),
			"UTF-8"
		);
		ONDEXGraph graph = new MemoryONDEXGraph ( "default" );

		PathParser pp = ConfigParser.parseConfigXml ( 
			schemaReader, graph, "target/test-classes/dynamic-types/dynamic-concepts.tsv" 
		);
		pp.setProcessingOptions ( new String [ 0 ] );
		pp.parse ();
		
		ONDEXGraphOperations.dumpAll ( graph );
		
		var concepts = graph.getConceptsOfConceptClass ( ONDEXGraphUtils.getConceptClass ( graph, "Gene" ) );
		assertEquals ( "Wrong count for gene concepts!", 2, concepts.size () );
		
		concepts = graph.getConceptsOfConceptClass ( ONDEXGraphUtils.getConceptClass ( graph, "GOTerm" ) );
		assertEquals ( "Wrong count for gene concepts!", 1, concepts.size () );
		var concept = concepts.iterator ().next ();
		assertEquals ( "Wrong PID for the test concept!", "Apoptosis", concept.getPID () );
		assertEquals ( "Wrong concept test attribute", 
			"Foo term",
			ONDEXGraphUtils.getAttribute ( graph, concept, "Notes" ).getValue ().toString ()
		);

	}
	
	/**
	 * Test types defined in the input
	 */
	@Test
	public void testDynamicRelations () throws Exception
	{
		Reader schemaReader = new InputStreamReader ( 
			Resources.getResource ( this.getClass (), "/dynamic-types/dynamic-rels-cfg.xml" ).openStream (),
			"UTF-8"
		);
		ONDEXGraph graph = new MemoryONDEXGraph ( "default" );

		PathParser pp = ConfigParser.parseConfigXml ( 
			schemaReader, graph, "target/test-classes/dynamic-types/dynamic-rels.tsv" 
		);
		pp.setProcessingOptions ( new String [ 0 ] );
		pp.parse ();
		
		ONDEXGraphOperations.dumpAll ( graph );
		
		var rels = graph.getRelationsOfRelationType ( ONDEXGraphUtils.getRelationType ( graph, "ortho" ) );
		assertEquals ( "Wrong count for ortho relations!", 1, rels.size () );
		
		var rel = rels.iterator ().next ();
		assertEquals ( "Wrong relation test attribute", 
			"Note 1",
			ONDEXGraphUtils.getAttribute ( graph, rel, "TestRelationAttribute" ).getValue ().toString ()
		);
		
		rels = graph.getRelationsOfRelationType ( ONDEXGraphUtils.getRelationType ( graph, "para" ) );
		assertEquals ( "Wrong count for para relations!", 2, rels.size () );
	}	
	
	@Test
	public void testAccessionMerge () throws Exception
	{
		Reader schemaReader = new InputStreamReader ( 
			Resources.getResource ( this.getClass (), "/acc-merge/acc-merge-cfg.xml" ).openStream (),
			"UTF-8"
		);
		ONDEXGraph graph = new MemoryONDEXGraph ( "default" );

		PathParser pp = ConfigParser.parseConfigXml ( 
			schemaReader, graph, "target/test-classes//acc-merge/acc-merge.tsv" 
		);
		//pp.setProcessingOptions ( new String [ 0 ] );
		pp.parse ();
		
		ONDEXGraphOperations.dumpAll ( graph );
				
		var phenos = graph.getConceptsOfConceptClass ( ONDEXGraphUtils.getConceptClass ( graph, "Phenotype" ) );
		
		ONDEXConcept floweringPheno = phenos.stream ()
		.filter ( 
			c -> c.getConceptAccessions ()
			  .stream ()
			  .anyMatch ( acc ->
			  	"Delayed flowering.".toUpperCase ().equals ( acc.getAccession () )
			  	&& "TAIR-Pheno".equals ( acc.getElementOf ().getId () ) 
			  )
		)
		.findAny ()
		.orElse ( null );
		
		assertNotNull ( "No flowering pheno found!", floweringPheno );
		assertEquals ( "No flowering pheno found!", 1, floweringPheno.getConceptAccessions ().size () );
		
		assertEquals ( "Phenotypes count doesn't match!", 9, phenos.size () );
	}
}
