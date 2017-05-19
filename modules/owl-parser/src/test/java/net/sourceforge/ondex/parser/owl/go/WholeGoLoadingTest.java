package net.sourceforge.ondex.parser.owl.go;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.jena.ontology.OntModel;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.parser.owl.OWLMapper;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>15 May 2017</dd></dl>
 *
 */
public class WholeGoLoadingTest
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	@Test
	@Ignore ( "Not a real test, very time consuming" )
	public void testLoadAllBioProcess () throws Exception
	{
		ApplicationContext ctx = new ClassPathXmlApplicationContext ( "go_all_test_cfg.xml" );

		OntModel model = (OntModel) ctx.getBean ( "jenaOntModel" );
		model.read ( 
			new BufferedReader ( new FileReader ( "/Users/brandizi/Documents/Work/RRes/tasks/owl_parser/go.owl" ) ), 
			"RDF/XML" 
		);		
		
		OWLMapper owlMap = (OWLMapper) ctx.getBean ( "owlMapper" );

		final ONDEXGraph graph = new MemoryONDEXGraph ( "default" );
		
		ScheduledExecutorService timerService = Executors.newScheduledThreadPool ( 1 );
		timerService.scheduleAtFixedRate (
			() -> log.info ( "Mapped {} GO classes", graph.getConcepts ().size () ), 
			30, 30, TimeUnit.SECONDS 
		);
		
		owlMap.map ( model, graph );
		
		((Closeable) ctx ).close ();
	}
}
