package net.sourceforge.ondex.parser.biocyc;

import static org.junit.Assert.assertNotNull;

import java.nio.file.Path;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import net.sourceforge.ondex.ONDEXPlugin;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.utils.OndexPluginUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 May 2021</dd></dl>
 *
 */
public class BioCycParserTest
{
	private Logger log = Logger.getLogger ( this.getClass () );
	
	private ONDEXGraph loadFromBioPax ( String bpaxFileName )
	{
		log.info ( "Pre-loading graph with metadata" );
		var graph = net.sourceforge.ondex.parser.oxl.Parser.loadOXL (
			Path.of ( "target/test-classes/ondex_metadata.xml" ).toAbsolutePath ().toString () 
		);
		
		log.info ( "Loading '" + bpaxFileName + "'" );
		String bpaxPath = Path.of ( "target/test-classes/" + bpaxFileName ).toAbsolutePath ().toString ();
		
		OndexPluginUtils.runPlugin ( 
			Parser.class, 
			graph, 
			Map.of (
				FileArgumentDefinition.INPUT_FILE, bpaxPath,
				ArgumentNames.TAXID_TO_USE_ARG, "372",
				ArgumentNames.CV_ARG, "AC"
			)
		);
		
		return graph;
	}
	
	
	@Test
	public void testIAAPathway ()
	{
		var graph = loadFromBioPax ( "indole-3-acetate-i-bpax2.owl" );
		
		var compCC = graph.getMetaData ().getConceptClass ( "Comp" );
		var probeComp = graph.getConceptsOfConceptClass ( compCC )
			.stream ()
			.filter ( 
				c -> c.getConceptAccessions ().stream ().anyMatch ( acc -> 
					"CHEBI".equals ( acc.getElementOf ().getId () ) && "30854".equals ( acc.getAccession () ) 
				) 
			)
			.findAny ()
			.orElse ( null );
		assertNotNull ( "Expected Comp concept not found!", probeComp );

		var reactCC = graph.getMetaData ().getConceptClass ( "Reaction" );
		var probeReact = graph.getConceptsOfConceptClass ( reactCC )
			.stream ()
			.filter ( 
				c -> c.getConceptAccessions ().stream ().anyMatch ( acc -> 
					"KEGG".equals ( acc.getElementOf ().getId () ) && "R10181".equals ( acc.getAccession () ) 
				) 
			)
			.findAny ()
			.orElse ( null );
		assertNotNull ( "Expected Reaction concept not found!", probeReact );
		
		
		var prodBy = graph.getRelationsOfConcept ( probeComp )
		.stream ()
		.filter ( rel -> probeComp.equals ( rel.getFromConcept () ) )
		.filter ( rel -> "pd_by".equals ( rel.getOfType ().getId () ) )
		.filter ( rel -> probeReact.equals ( rel.getToConcept () ) )
		.findFirst ()
		.orElse ( null );
		
		assertNotNull ( "Expected pd_by relation not found!", prodBy );
		
		// ONDEXGraphOperations.dumpAll ( graph );
	}
}
