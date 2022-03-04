package uk.ac.rothamsted.knetminer.backend;

import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.algorithm.graphquery.GraphTraverser;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.args.StringMappingPairArgumentDefinition;
import net.sourceforge.ondex.export.ONDEXExport;

/**
 * An Ondex plug-in wrapper, which defines the available options for the traverser (arguments,
 * in the jargon of the Ondex plug-ins), and invokes the core component.This will be used in
 * Ondex Mini workflow, presumably with a graph from core core component.
 *
 * @author brandizi
 * @author jojicunnunni
 * <dl><dt>Date:</dt><dd>22 Feb 2022</dd></dl>
 *
 */
public class KnetMinerInitializerPlugIn extends ONDEXExport
{
	public static final String
	  OPT_DESCR_CONFIG_XML = "The KnetMiner XML configuration file where to get options like traverser semantic motifs file.",
	  OPT_DESCR_DATA_PATH = "The data output path.",
	  OPT_DESCR_TRAVERSER = "The FQN for the graph traverser class to be used, which has to be an instance of "
	  	+ "AbstractGraphTraverser (see documentation).",
		OPT_DESCR_TAXIDS = "NCBITax's numerical identifiers for the specie to consider when fetching seed genes from the graph for the traversal."
			+ " If it's empty, the value is got from the SpeciesTaxId option (set separately or in the config file)", 
		OPT_DESCR_OPTS = "Options in the name:value format, which can be used provide further values to the explicit plug-in arguments and"
			+ "to override or extend the options in the KnetMiner config file.";
	
		@Override
	public String getId ()
	{
		return StringUtils.uncapitalize ( this.getClass ().getSimpleName () );
	}

	@Override
	public String getName ()
	{
		return "KnetMiner Initialiser";
	}

	@Override
	public String getVersion ()
	{
		return "1.0-SNAPSHOT";
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions ()
	{
		return new ArgumentDefinition<?>[]
		{
      new FileArgumentDefinition ( 
    		"configXmlPath", 
    		OPT_DESCR_CONFIG_XML,
    		false, // required
    		true, // must exist
    		false, // isDir
    		false // canBeMultiple
    	),
      new FileArgumentDefinition ( 
    		"dataPath", 
    		OPT_DESCR_DATA_PATH,
    		false, // required
    		true, // must exist
    		true, // isDir
    		false // canBeMultiple
    	),
			new StringArgumentDefinition ( 
			  "graphTraverserFQN", 
				OPT_DESCR_TRAVERSER, 
				true, // required
				GraphTraverser.class.getName (), // default
				false // canBeMultiple
	    ),
			new StringArgumentDefinition ( 
			  "SpeciesTaxIds", 
			  OPT_DESCR_TAXIDS, 
				false, // required
				null, // default
				true // canBeMultiple
	    ),
			new StringMappingPairArgumentDefinition ( 
				"options", 
				OPT_DESCR_OPTS,
        false, // required 
        "", // default
        true, // multiple
        ":" // separator
			)
		};
	}

	@Override
	public void start () throws Exception
	{
		ONDEXPluginArguments args = this.getArguments ();
		
		KnetMinerInitializer initializer = new KnetMinerInitializer ();
		initializer.setGraph ( this.graph );
		
		// They are like: set if not null
		// 
		Optional.ofNullable ( trimToNull ( (String) args.getUniqueValue ( "configXmlPath" ) ) )
		.ifPresent ( initializer::setConfigXmlPath );
		
		Optional.ofNullable ( trimToNull ( (String) args.getUniqueValue ( "dataPath" ) ) )
		.ifPresent ( initializer::setDataPath );
				
		Optional.ofNullable ( trimToNull ( (String) args.getUniqueValue ( "graphTraverserFQN" ) ) )
		.ifPresent ( initializer::setGraphTraverserFQN );
		
		// Check not null and not empty, possibly translate to set and pass it to the initializer
		Optional.ofNullable ( args.getObjectValueList ( "SpeciesTaxIds", String.class ) )
		.filter ( ids -> !ids.isEmpty () )
		.map ( HashSet::new )
		.ifPresent ( initializer::setTaxIds );
				
		// Translate "key:value" strings into a map
		Map<String, Object> opts = args.getObjectValueList ( "options", String.class )
		.stream ()
		.filter ( optStr -> optStr != null && !optStr.isEmpty () )
		.map ( optStr -> optStr.split ( ":" ) )
		.filter ( optArray -> optArray != null && optArray.length >= 2 )
		.collect ( Collectors.toMap ( optArray -> optArray [ 0 ], optArray -> optArray [ 1 ] ) );
		
		// opts here will override keys taken from configXmlPath. In turn, initializer's setters will 
		// have priority on everything else
		//
		initializer.initKnetMinerData ( opts );
	}

	/**
	 * TODO: this returns false for the time being. There are plug-ins which trigger Ondex Mini indexing and 
	 * the resulting files could be simply copied to the data output target for the initialiser. This is a possible
	 * future improvement.
	 *  
	 */
	@Override
	public boolean requiresIndexedGraph ()
	{
		return false;
	}

	/**
	 * @return null, we don't need any graph validation here.
	 */
	@Override
	public String[] requiresValidators ()
	{
		return null;
	}

}
