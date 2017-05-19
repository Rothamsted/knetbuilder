package net.sourceforge.ondex.parser.owl.plugin;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.owl.go.OWLMapper;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 May 2017</dd></dl>
 *
 */
public class OWLParser extends ONDEXParser
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );


	@Override
	public String getId ()
	{
		return "owlParser";
	}

	@Override
	public String getName ()
	{
		return "The OWL Parser";
	}

	@Override
	public String getVersion ()
	{
		return "1.0";
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions ()
	{
		return new ArgumentDefinition<?>[] {
      new FileArgumentDefinition ( 
      	FileArgumentDefinition.INPUT_FILE, 
      	FileArgumentDefinition.INPUT_FILE_DESC, 
      	true, // required 
      	true, // preExisting 
      	false, // isDirectory
      	false // canHaveMultipleInstances
      ),
      new FileArgumentDefinition ( 
      	"configFile", 
      	"The Spring Beans XML file that maps the input to ONDEX. See the documentation for details.", 
      	true, // required 
      	true, // preExisting 
      	false, // isDirectory
      	false // canHaveMultipleInstances
      )
		};	
	}

	@Override
	public void start () throws Exception
	{
    String owlInputPath = (String) getArguments().getUniqueValue( FileArgumentDefinition.INPUT_FILE );
    String springXmlPath = (String) getArguments().getUniqueValue( "configFile" );

		ApplicationContext ctx = new FileSystemXmlApplicationContext ( springXmlPath );

		OntModel model = (OntModel) ctx.getBean ( "jenaOntModel" );
		model.read ( 
			new BufferedReader ( new FileReader ( owlInputPath ) ), 
			"RDF/XML" 
		);		
		
		OWLMapper owlMap = (OWLMapper) ctx.getBean ( "owlMapper" );
		
		ScheduledExecutorService timerService = Executors.newScheduledThreadPool ( 1 );
		timerService.scheduleAtFixedRate (
			() -> log.info ( "Mapped {} GO classes", graph.getConcepts ().size () ), 
			30, 30, TimeUnit.SECONDS 
		);
		
		owlMap.map ( model, graph );
		
		((Closeable) ctx ).close ();
    
    
	}

	@Override
	public String[] requiresValidators ()
	{
		return new String [ 0 ];
	}

}
