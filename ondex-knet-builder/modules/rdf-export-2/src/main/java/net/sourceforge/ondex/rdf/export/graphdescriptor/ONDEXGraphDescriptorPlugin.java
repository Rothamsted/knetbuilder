package net.sourceforge.ondex.rdf.export.graphdescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>26 Nov 2021</dd></dl>
 *
 */
@Status ( status = StatusType.EXPERIMENTAL )
public class ONDEXGraphDescriptorPlugin extends ONDEXTransformer
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () ); 


	@Override
	public String getId ()
	{
		return "graphDescriptor";
	}

	@Override
	public String getName ()
	{
		return "Plug-in to add dataset description metadata to a graph itself";
	}

	@Override
	public String getVersion ()
	{
		return "1.0";
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions ()
	{
		return new ArgumentDefinition[]
		{
      new FileArgumentDefinition ( 
      	"rdfTemplatePath", 
    		"The RDF template to be used to create the descriptor. Keep in mind that further RDF triples are added automatically.",
    		true, // required
    		false, // preExists
    		false, // isDir
    		false // canBeMultiple
    	),
      new FileArgumentDefinition ( 
      	"configurationPath", 
    		"The file defining the dataset variables used to populate the RDF template",
    		true, // required
    		false, // preExists
    		false, // isDir
    		false // canBeMultiple
    	),
	    new StringArgumentDefinition ( 
		    "rdfLanguage", 
				"The RDF format that the RDF template uses",
				false, // required
				"TURTLE", // default
				false // canBeMultiple
	    ),
	    new StringArgumentDefinition ( 
		    "oxlSourceURL", 
				"The OXL source URL. This is usually the same path where you have loaded the processed OXL from. "
		    + "If present, an MD5 checksum is added to the dataset's properties.",
				false, // required
				null, // default
				false // canBeMultiple
	    )
		};
	}

	@Override
	public void start () throws Exception
	{
		log.info ( "Running the graph descriptor plug-in" );
		
		ONDEXPluginArguments args = this.getArguments ();
		
		var descriptorTool = new OndexGraphDescriptorTool.Builder ()
			.setGraph ( graph )
			.setRdfTemplatePath ( (String) args.getUniqueValue ( "rdfTemplatePath" ) )
			.setContextPath ( (String) args.getUniqueValue ( "configurationPath" ) )
			.setRdfLang ( (String) args.getUniqueValue ( "rdfLanguage" ) )
			.setOxlSourceURL ( (String) args.getUniqueValue ( "oxlSourceURL" ) )
			.build ();
		
		descriptorTool.createDescriptor ();

		log.info ( "graph description metadata added to the graph itself" );
	}

	@Override
	public boolean requiresIndexedGraph ()
	{
		return false;
	}

	@Override
	public String[] requiresValidators ()
	{
		return null;
	}

}
