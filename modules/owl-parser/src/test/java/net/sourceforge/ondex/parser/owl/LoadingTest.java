package net.sourceforge.ondex.parser.owl;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;

import org.apache.jena.ontology.OntModel;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
		String owlDir = "/Users/brandizi/Documents/Work/RRes/tasks/owl_parser/";
		//load ( "go_cfg.xml", owlDir + "go.owl" );
		//load ( "doid_cfg.xml", owlDir + "doid.owl" );
		//load ( "to_cfg.xml", owlDir + "trait_ontology.owl" );
		load ( "fypo_cfg.xml", owlDir + "fypo.owl" );
		
		owlDir = "/Users/brandizi/Documents/Work/RRes/ondex_data/owl-parser_test_data/";
		load ( "po_cfg.xml", owlDir + "po.owl" );
	}
	
	public static void load ( String cfgPath, String owlPath ) throws Exception
	{
		ApplicationContext ctx = new ClassPathXmlApplicationContext ( cfgPath );

		OntModel model = (OntModel) ctx.getBean ( "jenaOntModel" );
		model.read ( 
			new BufferedReader ( new FileReader ( owlPath ) ), 
			"RDF/XML" 
		);		
		
		OWLMapper owlMap = (OWLMapper) ctx.getBean ( "owlMapper" );
		owlMap.map2Graph ( model );		
		((Closeable) ctx ).close ();
	}
}
