package uk.ac.rothamsted.knetminer.backend;

import org.apache.commons.lang3.StringUtils;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.algorithm.graphquery.GraphTraverser;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.args.StringMappingPairArgumentDefinition;
import net.sourceforge.ondex.export.ONDEXExport;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 Feb 2022</dd></dl>
 *
 */
public class KnetMinerInitializerPlugIn extends ONDEXExport
{

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
		return new ArgumentDefinition<?>[] {
      new FileArgumentDefinition ( 
    		"configXmlPath", 
    		"The KnetMiner XML configuration file where to get options like traverser semantic motifs file",
    		false, // required
    		true, // must exist
    		false, // isDir
    		false // canBeMultiple
    	),
      new FileArgumentDefinition ( 
    		"dataPath", 
    		"The data output path",
    		false, // required
    		true, // must exist
    		true, // isDir
    		false // canBeMultiple
    	),
			new StringArgumentDefinition ( 
			  "graphTraverserFQN", 
				"The FQN for the graph traverser class to be used, which has to be an instance of AbstractGraphTraverser (see documentation).", 
				true, // required
				GraphTraverser.class.getName (), // default
				false // canBeMultiple
	    ),
			new StringArgumentDefinition ( 
			  "SpeciesTaxIds", 
				"NCBITax's numerical identifiers for the specie to consider when fetching seed genes from the graph for the traversal."
				+ " If it's empty, the value is got from the SpeciesTaxId option (set separately or in the config file)", 
				false, // required
				GraphTraverser.class.getName (), // default
				true // canBeMultiple
	    ),
      new FileArgumentDefinition ( 
    		"seedGenesFile", 
    		"The seed genes file, containing a custom list of gene IDs (see documentation) to seed the traversal. This overrides"
    		+ "the TAX ID criterion for seeding. If not specified, the option with the same name is checked",
    		false, // required
    		true, // must exist
    		false, // isDir
    		false // canBeMultiple
    	),
			new StringMappingPairArgumentDefinition ( 
				"options", 
				"Options in the name:value format, which can be used provide further values to the explicit plug-in arguments and"
				+ "to override or extend the options in the KnetMiner config file.",
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

		// TODO: instantiate KnetMinerInitializer, use methods in args to extract the corresponding 
		// parameters, pass the params to the initializer and run it.
		// this.graph is available here to be passed down.
	}

	@Override
	public boolean requiresIndexedGraph ()
	{
		return false;
	}

	@Override
	public String[] requiresValidators ()
	{
		// It doesn't
		return null;
	}

}
