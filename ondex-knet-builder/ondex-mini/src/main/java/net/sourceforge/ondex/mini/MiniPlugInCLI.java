package net.sourceforge.ondex.mini;

import static java.lang.System.out;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

import net.sourceforge.ondex.ONDEXPlugin;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.export.oxl.Export;
import net.sourceforge.ondex.init.PluginRegistry;
import net.sourceforge.ondex.parser.oxl.Parser;
import net.sourceforge.ondex.utils.OndexPluginUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * A simple command line interface to run a single plugin, possibly preceded by the load of an OXL
 * and possibly followed by the export of the resulting graph.
 * 
 * This is wrapped by plugin.sh
 * 
 * Everything here is based on the picocli library.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>12 Jul 2021</dd></dl>
 *
 */
@Command ( 
	name = "plugin", 
		description = "\n\n  *** Ondex Plug-In invoker ***\n" +
			"\nInvokes a single plugin via command-line, after having loaded an OXL.\n",
	exitCodeOnVersionHelp = ExitCode.USAGE, // else, it's 0 and you can't know about this event
	exitCodeOnUsageHelp = ExitCode.USAGE, // ditto
	mixinStandardHelpOptions = true,
	usageHelpAutoWidth = true,
	usageHelpWidth = 120
)
public class MiniPlugInCLI implements Callable<Integer>
{
	@Option (
		names = { "-i", "--input", "--in" },
		paramLabel = "<path/to/oxl>",		
		description = "The path of the OXL to start from (default = starts with empty OXL)"
	)
	private String oxlInputPath = null; 

	@Option (
		names = { "-o", "--output", "--out" },
		paramLabel = "<path/to/oxl>",		
		description = "The path of the OXL where to save the results (default = doesn't export)"
	)
	private String oxlOutputPath = null; 
	
	@Option (
		names = { "-c", "--compress" },
		description = "Compress the output OXL (ignored without -o)",
		showDefaultValue = Visibility.ALWAYS
	)
	private boolean zip = true;
	
	@Option (
		names = { "-b", "--pretty-print", "--pretty" },
		description = "Pretty-print the OXL output (ignored without -o)",
		showDefaultValue = Visibility.ALWAYS
	)
	private boolean prettyPrint = true;
	
	@Option (
		names = { "-a", "--arguments" },
		description = "List the plugin arguments (requires plugin class, ignores other options)"
	)
	private boolean listArguments = false;
	
	@Option (
		names = { "-r", "--plugin" },
	  paramLabel = "<plugin class>",
		description = "The FQN for the plug-in class (default = possibly loads/exports without any other operation)"
	)
	private String pluginFQN = null;

	@Option (
		names = { "-p", "--plugins-dir", "--plugins" },
		paramLabel = "<path>",
		description = "Plug-ins directory",
		showDefaultValue = Visibility.ALWAYS
	)
	private String pluginsDir = "plugins";
	
	@Option (
		names = { "-l", "--lib-dir", "--lib" },
		paramLabel = "<path>",
		description = "Lib directory",
		showDefaultValue = Visibility.ALWAYS
	)
	private String libDir = "lib";
	
	@Parameters (
		paramLabel = "<name>=<value>",
		description = "The plugin arguments (use '|' if <name> has multiple arguments)" 
	)
	private Map<String, Object> arguments = new HashMap<> ();
	
	/**
	 * The core, in the form required by picocli
	 */
	@Override
	public Integer call () throws Exception
	{
		PluginRegistry.init ( true, this.pluginsDir, this.libDir );
		
		if ( listArguments ) {
			doListArguments ( pluginFQN );
			return 0;
		}
		
		ONDEXGraph graph = Optional.ofNullable ( oxlInputPath )
			.map ( Parser::loadOXL )
			.orElse ( new MemoryONDEXGraph ( "graph" ) );
		
		if ( pluginFQN != null )
		{
			splitMultiValueArgs ( arguments );
			OndexPluginUtils.runPlugin ( pluginFQN, graph, arguments );
		}
		
		if ( oxlOutputPath != null )
			Export.exportOXL ( graph, oxlOutputPath, zip, prettyPrint );
		
		return 0;
	}
	
	/**
	 * Does all the job of {@link #main(String...)}, except exiting, useful for 
	 * testing.
	 * 
	 * This uses {@link CommandLine}, as prescribed by the picocli library.
	 */
	public static int invoke ( String... args )
	{
    int exitCode = new CommandLine ( new MiniPlugInCLI () ).execute ( args );
    return exitCode; 
	}
	
	/**
	 * The usual wrapper for the external invocation. This just invokes {@link #invoke(String...)}
	 * and exits with its result.
	 * 
	 */
	public static void main ( String... args )
	{
		System.exit ( invoke ( args ) );
	}
	
	
	private void doListArguments ( String pluginFQN )
	{
		if ( pluginFQN == null ) throw new IllegalArgumentException (
			"Cannot list arguments for a null plug-in"
		);
		ONDEXPlugin plugin = OndexPluginUtils.createPlugin ( pluginFQN );
		var args = plugin.getArgumentDefinitions ();

		out.println ();

		if ( args == null || args.length == 0 ) 
		{
			out.println ( "<no argument>" );
			return;
		}
		
		for ( var arg: plugin.getArgumentDefinitions () )
		{
			out.format ( "name: %s, type: %s, description: %s, default: %s, required: %s, multiple: %s\n", 
				arg.getName (),
				arg.getClassType ().getTypeName (),
				arg.getDescription (),
				arg.getDefaultValue (),
				arg.isRequiredArgument () ? "Y" : "N",
				arg.isAllowedMultipleInstances () ? "Y" : "N"
			);
		}
	}
	
	/** 
	 * Little utility to split string arguments based on '|'. When such argument values are present
	 * (of type string), the args map is changed with a list corresponding to the multiple values in the string.
	 * 
	 * TODO: write a test for a plug-in with this case. 
	 */
	private static void splitMultiValueArgs ( Map<String, Object> args )
	{
		if ( args == null || args.isEmpty () ) return;
		for ( String aname: args.keySet () )
		{
			var val = args.get ( aname );
			if ( val == null ) continue;
			if ( ! ( val instanceof String ) ) continue;
			String sval = (String) val;
			if ( sval.isEmpty () ) continue;
			if ( !sval.contains ( "|" ) ) continue;
			var svalues = sval.split ( "\\|" );
			args.put ( aname, List.of ( svalues ) );
		}
	}
}
