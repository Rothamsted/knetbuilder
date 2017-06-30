package net.sourceforge.ondex.parser.owl;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;

import org.apache.jena.ontology.OntModel;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;

/**
 * A scrap test to ensure the whole GO can be loaded.
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
		ONDEXGraph graph = new MemoryONDEXGraph ( "default" );				
		owlMap.map ( model, graph );		
		((Closeable) ctx ).close ();
	}
}
