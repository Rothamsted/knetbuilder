package net.sourceforge.ondex.plugins.tab_parser_2;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Set;

import org.junit.Test;

import com.google.common.io.Resources;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
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
}
