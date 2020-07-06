package net.sourceforge.ondex.mapping.lowmemoryaccessionbased;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.mapping.lowmemoryaccessionbased.ArgumentNames;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.export.oxl.Export;
import net.sourceforge.ondex.logging.ONDEXLogger;
import net.sourceforge.ondex.parser.oxl.Parser;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>30 May 2020</dd></dl>
 *
 */
public class GoUniProtMappingTest
{
	@Test
	public void testGo0060333 () throws Exception
	{
		String termAcc = "GO:0060333", termSrc = "GO";
		String mapRelId = "collapse_me";

		String oxlPath =  
			"target/test-classes/net/sourceforge/ondex/mapping/lowmemoryaccessionbased/go-uniprot.xml";
		ONDEXGraph graph = Parser.loadOXL ( oxlPath );

		LowMemoryAccessionBasedTest.loadLuceneEnv ( graph );

		Mapping mapper = new Mapping ();
		mapper.setONDEXGraph ( graph );
		mapper.addONDEXListener ( new ONDEXLogger () );
		
		ONDEXPluginArguments args = new ONDEXPluginArguments ( mapper.getArgumentDefinitions () );
		args.setOption ( ArgumentNames.IGNORE_AMBIGUOUS_ARG, false );
		args.setOption ( ArgumentNames.RELATION_TYPE_ARG, mapRelId );
		args.setOption ( ArgumentNames.WITHIN_DATASOURCE_ARG, true );
		
		mapper.setArguments ( args );
		mapper.start ();
		
		Export.exportOXL ( graph, "target/go-uniprot-out.xml", false, true );
		
		ONDEXConcept testc = graph.getConcepts ()
		.stream ()
		.filter ( c -> c.getDescription () != null )
		.filter ( c -> c.getDescription ().contains ( "concept 384853" ) )
		.findAny ()
		.orElse ( null );
		 
		Assert.assertNotNull ( "Probe concept not found!", testc );
		
		Set<ONDEXRelation> relations =
			graph.getRelationsOfRelationType ( graph.getMetaData ().getRelationType ( mapRelId ) );
		
		Assert.assertEquals ( "Wrong mappings count!", 3, relations.stream ().count () );

		
		Set<ONDEXConcept> mappedConcepts = relations.stream ()
		.filter ( r -> 
				 testc.getId () == r.getFromConcept ().getId () 
			|| testc.getId () == r.getToConcept ().getId ()
		)
		.collect ( () -> 
			new HashSet<> (), 
			(s, r) -> { 
				s.add ( r.getFromConcept () );
				s.add ( r.getToConcept () );
			},
			(s1, s2) -> s1.addAll ( s2 ) 
		);
		
		Assert.assertTrue (
			"Mapped concept not found!", 
			mappedConcepts
			.stream ()
			.filter ( c -> c.getDescription () != null )
			.anyMatch ( c -> c.getDescription ().contains ( "concept 1518407" ) )
		);

		Assert.assertTrue (
			"Mapped concepts not found!", 
			mappedConcepts
			.stream ()
			.filter ( c -> c.getDescription () != null )
			.anyMatch ( c -> c.getDescription ().contains ( "concept 5015150" ) )
		);
		
	}
}
