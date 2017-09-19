package net.sourceforge.ondex.mapping.tmbased;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.mini.test.MiniInvoker;

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
	@Test
	public void testTextMiningWf ()
	{
		String wfPath = "target/test-classes/textmining_wf/";
		MiniInvoker invoker = new MiniInvoker ();
		invoker.invoke ( wfPath + "tm-workflow.xml" );
		ONDEXGraph graph = invoker.loadOXL ( wfPath + "out.oxl" );
				
		// Do we have the 
		ONDEXConcept testConcept = 
			graph.getConcepts ()
			.stream ()
			.filter ( c ->
				Optional.ofNullable ( c.getConceptAccessions () )
				.map ( accs -> accs.stream ().anyMatch ( acc -> "TO:0000599".equals ( acc.getAccession () ) )  )
				.orElse ( false )
			)
			.findFirst ()
			.orElse ( null );
		
		assertNotNull ( "Test concept not found!", testConcept );
		
		ONDEXRelation testRel = graph
		.getRelationsOfConcept ( testConcept )
		.stream ()
		.filter ( r -> 
			Optional.ofNullable ( r.getToConcept ().getConceptAccessions () )
			.map ( accs -> accs.stream ().anyMatch ( acc -> acc.getAccession ().contains ( "Q6ZAL2" ) ) )
			.orElse ( false )
		)
		.findFirst ()
		.orElse ( null );
		
		assertNotNull ( "Test relation not found!", testRel );

		Set<String> pmids = testRel
		.getAttributes ()
		.stream ()
		.filter ( attr -> "PMID".equals ( attr.getOfType ().getId () ) )
		.map ( attr -> (String) attr.getValue () )
		.collect ( Collectors.toSet () );
		
		assertEquals ( "Test PMIDs not found!", 1, pmids.size () );
		String pmid = pmids.iterator ().next ();
		
		assertTrue ( "Test PMID 1 not found!", pmid.contains ( "16240171" ) );
		assertTrue ( "Test PMID 2 not found!", pmid.contains ( "17257172" ) );
		
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
