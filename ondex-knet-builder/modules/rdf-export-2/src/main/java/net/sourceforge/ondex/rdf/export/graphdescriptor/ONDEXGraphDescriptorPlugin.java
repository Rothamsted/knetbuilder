package net.sourceforge.ondex.rdf.export.graphdescriptor;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
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

	public static final String 
		ARGDESCR_RDF_TEMPLATE_PATH = 
			"The RDF template to be used to create the descriptor. Keep in mind that further RDF triples are added automatically.",
		ARGDESCR_CTX_PATH =
			"The file defining the dataset variables used to populate the RDF template.",
		ARGDESCR_RDF_LANG = 
			"The RDF format that the RDF template uses (see Jena/RIOT docs)",
		ARGDFLT_RDF_LANG = "TURTLE",
		ARGDESCR_OXL_SRC_URL =
			"The OXL source URL. This is usually the same path where you have loaded the processed OXL from. "
			+ "If present, an MD5 checksum is added to the dataset's properties.",
		ARGDESCR_XPATH = 
			"File path where to export the descriptor in RDF format.",
		ARGDESCR_XPATH_LANG = 
				"RDF syntax to use for the descriptor export file.";

	
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
    		ARGDESCR_RDF_TEMPLATE_PATH,
    		true, // required
    		false, // preExists
    		false, // isDir
    		false // canBeMultiple
    	),
      new FileArgumentDefinition ( 
      	"configurationPath", 
    		ARGDESCR_CTX_PATH,
    		true, // required
    		false, // preExists
    		false, // isDir
    		false // canBeMultiple
    	),
	    new StringArgumentDefinition ( 
		    "rdfLanguage", 
				ARGDESCR_RDF_LANG,
				false, // required
				ARGDFLT_RDF_LANG, // default
				false // canBeMultiple
	    ),
	    new StringArgumentDefinition ( 
		    "exportPath", 
		    ARGDESCR_XPATH,
				false, // required
				"", // default
				false // canBeMultiple
	    ),
	    new StringArgumentDefinition ( 
		    "exportRdfLanguage", 
		    ARGDESCR_XPATH_LANG,
				false, // required
				ARGDFLT_RDF_LANG, // default
				false // canBeMultiple
	    ),
	    new StringArgumentDefinition ( 
		    "oxlSourceURL", 
		    ARGDESCR_OXL_SRC_URL,
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
		
		// RDF export
		//
		var exportPath = Optional.ofNullable ( (String) args.getUniqueValue ( "exportPath" ) )
			.filter ( p -> !p.isEmpty () )
			.orElse ( null );
		
		if ( exportPath == null ) return;
		
		var exportLang = (String) args.getUniqueValue ( "exportRdfLanguage" );
		descriptorTool.exportDescriptor ( exportPath, exportLang );
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
