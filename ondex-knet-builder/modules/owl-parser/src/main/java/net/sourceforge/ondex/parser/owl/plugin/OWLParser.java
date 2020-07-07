package net.sourceforge.ondex.parser.owl.plugin;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.owl.OWLMapper;

/**
 * The {@link ONDEXParser} wrapper for the OWL parser. As usually, this makes it an ONDEX plug-in, which can be used
 * in a workflow for the integrator or ONDEX mini.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 May 2017</dd></dl>
 *
 */
@Status(
	description = "An ONDEX parser module and plug-in that parses OWL ontology files.\n" +
								"See https://github.com/Rothamsted/ondex-knet-builder/tree/master/modules/owl-parser for details.",
  status = StatusType.STABLE
)
@Authors( authors = { "Marco Brandizi" }, emails = "marco.brandizi@rothamsted.ac.uk" )
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
		return "OWL Parser";
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
      	"The OWL Files to import. See https://jena.apache.org/documentation/ontology/#the-ontology-document-manager to set up a Jena policy manager.", 
      	true, // required 
      	true, // preExisting 
      	false, // isDirectory
      	true // canHaveMultipleInstances
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
    List<String> owlInputPaths = getArguments().getObjectValueList ( FileArgumentDefinition.INPUT_FILE, String.class );
    String springXmlPath = (String) getArguments().getUniqueValue( "configFile" );
    
    OWLMapper.mapFrom ( graph, springXmlPath, owlInputPaths.toArray ( new String [ 0 ] ) );
	}

	@Override
	public String[] requiresValidators ()
	{
		return new String [ 0 ];
	}

}
