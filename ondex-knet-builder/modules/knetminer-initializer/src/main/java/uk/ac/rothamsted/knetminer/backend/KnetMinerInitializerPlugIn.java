//package uk.ac.rothamsted.knetminer.backend;
//
//import static org.apache.commons.lang3.StringUtils.trimToNull;
//
//import java.util.Optional;
//
//import org.apache.commons.lang3.StringUtils;
//
//import net.sourceforge.ondex.ONDEXPluginArguments;
//import net.sourceforge.ondex.args.ArgumentDefinition;
//import net.sourceforge.ondex.args.FileArgumentDefinition;
//import net.sourceforge.ondex.export.ONDEXExport;
//import rres.knetminer.datasource.ondexlocal.config.KnetminerConfiguration;
//
///**
// * An Ondex plug-in wrapper, which defines the available options for the traverser (arguments,
// * in the jargon of the Ondex plug-ins), and invokes the core component.This will be used in
// * Ondex Mini workflow, presumably with a graph from core core component.
// *
// * @author brandizi
// * @author jojicunnunni
// * <dl><dt>Date:</dt><dd>22 Feb 2022</dd></dl>
// *
// */
//public class KnetMinerInitializerPlugIn extends ONDEXExport
//{
//	public static final String
//	  OPT_DESCR_CONFIG_YML = "The KnetMiner YML configuration file where to get options like traverser semantic motifs file.",
//	  OPT_DESCR_DATA_PATH = "The data output path. Default is taken from configYmlPath.";
//	
//		@Override
//	public String getId ()
//	{
//		return StringUtils.uncapitalize ( this.getClass ().getSimpleName () );
//	}
//
//	@Override
//	public String getName ()
//	{
//		return "KnetMiner Initialiser";
//	}
//
//	@Override
//	public String getVersion ()
//	{
//		return "1.0-SNAPSHOT";
//	}
//
//	@Override
//	public ArgumentDefinition<?>[] getArgumentDefinitions ()
//	{
//		return new ArgumentDefinition<?>[]
//		{
//      new FileArgumentDefinition ( 
//      	// TODO:newConfig, change name and description to this, as explained in KnetMinerInitializer  
//      	// do the same in the CLI module
//      	"configYmlPath",
//    		OPT_DESCR_CONFIG_YML,
//    		true, // required
//    		true, // must exist
//    		false, // isDir
//    		false // canBeMultiple
//    	),
//      new FileArgumentDefinition ( 
//    		"dataPath", 
//    		OPT_DESCR_DATA_PATH,
//    		false, // required
//    		true, // must exist
//    		true, // isDir
//    		false // canBeMultiple
//    	)
//		};
//	}
//
//	@Override
//	public void start () throws Exception
//	{
//		ONDEXPluginArguments args = this.getArguments ();
//		
//		KnetMinerInitializer initializer = new KnetMinerInitializer ();
//		initializer.setGraph ( this.graph );
//		
//		KnetminerConfiguration conf = KnetminerConfiguration.load ( ( String ) args.getUniqueValue ( "configYmlPath" ) );
//		
//		
//		// They are like: set if not null
//		// 
//		Optional.ofNullable ( trimToNull ( conf.getGraphTraverserOptions ().getString ( "dataPath" ) ) )
//		.ifPresent ( initializer::setKnetminerConfiguration );
//
//		initializer.initKnetMinerData ( true );
//	}
//
//	/**
//	 * TODO: this returns false for the time being. There are plug-ins which trigger Ondex Mini indexing and 
//	 * the resulting files could be simply copied to the data output target for the initialiser. This is a possible
//	 * future improvement.
//	 *  
//	 */
//	@Override
//	public boolean requiresIndexedGraph ()
//	{
//		return false;
//	}
//
//	/**
//	 * @return null, we don't need any graph validation here.
//	 */
//	@Override
//	public String[] requiresValidators ()
//	{
//		return null;
//	}
//
//}
