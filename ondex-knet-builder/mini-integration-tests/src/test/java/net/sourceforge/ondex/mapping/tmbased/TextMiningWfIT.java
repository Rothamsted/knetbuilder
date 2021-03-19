package net.sourceforge.ondex.mapping.tmbased;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.mini.test.MiniInvoker;
import net.sourceforge.ondex.parser.oxl.Parser;

/**
 * Some integration tests based on test text mining workflows.
 *
 * TODO: use the Graph Query language, instead of the current code.
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Sep 2017</dd></dl>
 *
 */
public class TextMiningWfIT
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	@Test
	public void testTextMiningWf ()
	{
		String mavenBuildPath = System.getProperty ( "maven.buildDirectory", "target" ) + "/";
		String wfPath = mavenBuildPath + "test-classes/textmining_wf/";
		MiniInvoker invoker = new MiniInvoker ();
		invoker.invoke ( wfPath + "tm-workflow.xml" );
		ONDEXGraph graph = Parser.loadOXL ( wfPath + "out.oxl" );
				
		String termAcc = "TO:0000599";
		String protAcc = "Q6ZAL2";
		
		
		// Do we have this concept?
		//
		ONDEXConcept testConcept = 
			graph.getConcepts ()
			.stream ()
			.filter ( c ->
				Optional.ofNullable ( c.getConceptAccessions () )
				.map ( accs -> accs.stream ().anyMatch ( acc -> termAcc.equals ( acc.getAccession () ) )  )
				.orElse ( false )
			)
			.findFirst ()
			.orElse ( null );
		
		assertNotNull ( "Test concept not found!", testConcept );
		
		
		// And does it have a relation having this other concept at the other end?
		//
		ONDEXRelation testRel = graph
		.getRelationsOfConcept ( testConcept )
		.stream ()
		.filter ( r -> 
			Optional.ofNullable ( r.getToConcept ().getConceptAccessions () )
			.map ( accs -> accs.stream ().anyMatch ( acc -> acc.getAccession ().contains ( protAcc ) ) )
			.orElse ( false )
		)
		.findFirst ()
		.orElse ( null );
		
		assertNotNull ( "Test relation not found!", testRel );

		
		// And how does the relation look like?
		//
		Set<String> pmids = testRel
		.getAttributes ()
		.stream ()
		.filter ( attr -> "PMID".equals ( attr.getOfType ().getId () ) )
		.map ( attr -> (String) attr.getValue () )
		.collect ( Collectors.toSet () );
		
		// All PMIDs are joined into one string only.
		assertEquals ( "Test PMIDs attribute value not found!", 1, pmids.size () );
		String pmidsStr = pmids.iterator ().next ();
		
		//Stream.of ( "16240171", "17257172" )
		Stream.of ( "17257172" )
		.forEach ( pmid -> 
			assertTrue (  
				format ( "No PMID attribute %s found in %s -> %s!", pmid, termAcc, protAcc ), 
				pmidsStr.contains ( pmid ) )
		);
		
		assertTrue ( "Bad test score!",
			testRel
			.getAttributes ()
			.stream ()
			.filter ( attr -> "Inner product of TFIDF".equals ( attr.getOfType ().getFullname () ) )
			.map ( attr -> (double) attr.getValue () )
			.anyMatch ( score -> score > 0 )
		);
	}
}
