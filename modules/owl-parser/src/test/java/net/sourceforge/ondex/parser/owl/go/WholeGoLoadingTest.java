package net.sourceforge.ondex.parser.owl.go;

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
import net.sourceforge.ondex.parser.owl.OWLMapper;

/**
 * A scrap test to ensure the whole GO can be loaded.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>15 May 2017</dd></dl>
 *
 */
public class WholeGoLoadingTest
{
	@Test
	@Ignore ( "Not a real test, very time consuming" )
	public void testLoadAllBioProcess () throws Exception
	{
		ApplicationContext ctx = new ClassPathXmlApplicationContext ( "go_cfg.xml" );

		OntModel model = (OntModel) ctx.getBean ( "jenaOntModel" );
		model.read ( 
			new BufferedReader ( new FileReader ( "/Users/brandizi/Documents/Work/RRes/tasks/owl_parser/go.owl" ) ), 
			"RDF/XML" 
		);		
		
		OWLMapper owlMap = (OWLMapper) ctx.getBean ( "owlMapper" );
		ONDEXGraph graph = new MemoryONDEXGraph ( "default" );				
		owlMap.map2Graph ( model, graph );		
		((Closeable) ctx ).close ();
	}
}
