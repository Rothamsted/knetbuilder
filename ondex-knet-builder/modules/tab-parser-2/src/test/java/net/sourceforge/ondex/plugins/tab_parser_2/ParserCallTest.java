package net.sourceforge.ondex.plugins.tab_parser_2;

import static net.sourceforge.ondex.tools.subgraph.DefConst.defAccession;
import static net.sourceforge.ondex.tools.subgraph.DefConst.defAttribute;
import static net.sourceforge.ondex.tools.subgraph.DefConst.defCC;
import static net.sourceforge.ondex.tools.subgraph.DefConst.defDataSource;
import static net.sourceforge.ondex.tools.subgraph.DefConst.defName;
import static net.sourceforge.ondex.tools.subgraph.DefConst.defRT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.tools.tab.importer.ConceptPrototype;
import net.sourceforge.ondex.tools.tab.importer.DelimitedReader;
import net.sourceforge.ondex.tools.tab.importer.PathParser;

/**
 * Tests the existing ONDEX TSV importer.
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>2 Feb 2017</dd></dl>
 *
 */
public class ParserCallTest
{
	@Test
	@Ignore ( "Dirty test code, not a real Unit test" )
	public void testParserCall () throws Exception
	{
		String inpath = "target/test-classes/multi_attr_test/protDomain.tsv";
		ONDEXGraph graph = new MemoryONDEXGraph ( "default" );

		PathParser pp = new PathParser ( graph, new DelimitedReader ( inpath, "\t" ) );
		ConceptPrototype prot = pp.newConceptPrototype ( defAccession ( 1, "ENSEMBL" ), defCC ( "Protein" ), defDataSource ( "ENSEMBL" ) );
		ConceptPrototype protDomain = pp.newConceptPrototype ( 
			defName ( 2, "false" ), 
			defName ( 4, "true" ),
			defAccession ( 3, "IPRO" ),
			defAttribute ( 5, "Description", "TEXT", "false" ),
			defCC ( "ProtDomain" ),
			defDataSource ( "ENSEMBL" ) 
		);
		pp.newRelationPrototype ( prot, protDomain, defRT ( "has_domain" ) );
		pp.parse ();
		
		Set<ONDEXConcept> concepts = graph.getConcepts ();
		assertFalse ( "No concepts found in the test file!", concepts.isEmpty () );

		assertTrue ( 
			"Test names not found!", 
			concepts
			.stream ()
			.anyMatch ( concept -> {
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
			  }
			})
		);		
	}	
}
