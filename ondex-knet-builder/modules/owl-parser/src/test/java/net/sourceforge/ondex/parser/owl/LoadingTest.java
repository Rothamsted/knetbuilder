package net.sourceforge.ondex.parser.owl;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.export.oxl.Export;

/**
 * A scrap test to ensure the whole ontology can be loaded.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>15 May 2017</dd></dl>
 *
 */
public class LoadingTest
{
	@Test
	@Ignore ( "Not a real test, very time consuming" )
	public void testLoad () throws Exception
	{
		// String owlDir = "/Users/brandizi/Documents/Work/RRes/ondex_data/knet_miner_data/knet-pipelines/ontologies/";
		
		//ONDEXGraph g = load ( "go_cfg.xml", owlDir + "go.owl" );
		//load ( "doid_cfg.xml", owlDir + "doid.owl" );
		//load ( "to_cfg.xml", owlDir + "trait_ontology.owl" );
		//load ( "fypo_cfg.xml", owlDir + "fypo.owl" );
		
		//ONDEXGraph g = load ( "co_cfg.xml", owlDir + "co_321.owl" );
		
		//owlDir = "/Users/brandizi/Documents/Work/RRes/ondex_data/owl-parser_test_data/";
		//load ( "po_cfg.xml", owlDir + "po.owl" );

		String owlDir = "/Users/brandizi/Documents/Work/RRes/ondex_data/knet_miner_data/knet-pipelines/ontologies/";
		ONDEXGraph g = load ( "efo_cfg.xml", owlDir + "efo.owl" );
		
		Export.exportOXL ( g, "target/loading_test.oxl" );
	}
	
	public static ONDEXGraph load ( String cfgPath, String owlPath ) throws Exception
	{
		try ( ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext ( cfgPath ) ) {
			return OWLMapper.mapFrom ( null, ctx, owlPath );
		}
	}
}
