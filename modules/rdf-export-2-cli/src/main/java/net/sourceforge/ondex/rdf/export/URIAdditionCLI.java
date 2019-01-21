package net.sourceforge.ondex.rdf.export;

import static java.lang.System.out;

import java.io.PrintWriter;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.export.oxl.Export;
import net.sourceforge.ondex.parser.oxl.Parser;

/**
 * Command line wrapper for the {@link URIAdditionPlugin}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>12 Nov 2018</dd></dl>
 *
 */
public class URIAdditionCLI
{
	private static int exitCode = 0;	
	private static Logger log = LoggerFactory.getLogger ( URIAdditionCLI.class );
	
	
	public static void main ( String... args )
	{
		try
		{
			exitCode = 0;
			CommandLineParser clparser = new DefaultParser ();
			CommandLine cli = clparser.parse ( getOptions(), args );
			args = cli.getArgs ();
			
			if ( cli.hasOption ( "help" ) || args.length < 2 ) {
				printUsage ();
				return;
			}

			out.println ();

			String inPath = args [ 0 ], outPath = args [ 1 ];
			
			URIAdditionPlugin plugin = new URIAdditionPlugin ();
			ArgumentDefinition<?>[] argDefs = plugin.getArgumentDefinitions ();
			for ( ArgumentDefinition<?> argDef: argDefs )
			{
				String argName = argDef.getName ();
				if ( !cli.hasOption ( argName ) ) continue;
				
				String argVal = cli.getOptionValue ( argName );
				if ( Boolean.class.isAssignableFrom ( argDef.getClassType () ) )
					plugin.setUriIndexingEnabled ( Boolean.valueOf ( argVal ) );
				else
					BeanUtils.setProperty ( plugin, argName, argVal );
			}

			log.info ( "Loading '{}'", inPath );
			ONDEXGraph graph = Parser.loadOXL ( inPath );
			log.info ( "Graph Loaded, now processing it", inPath );
			
			plugin.setONDEXGraph ( graph );
			plugin.run ();
			
			Export.exportOXL ( graph, outPath );
			
			out.println ();
			if ( exitCode == 0 ) log.info ( "URI addition finished." );
		}
		catch ( Throwable ex ) 
		{
			log.error ( "Execution failed with the error: " + ex.getMessage (), ex  );
			exitCode = 1; // TODO: proper exit codes
		}
		finally 
		{
			if ( !"true".equals ( System.getProperty ( RDFFileExporterCLI.NO_EXIT_PROP ) ) )
				System.exit ( exitCode );
		}
	}
	
	private static Options getOptions ()
	{
		Options opts = new Options ();

		opts.addOption ( Option.builder ( "h" )
			.desc ( "Prints out this message" )
			.longOpt ( "help" )
			.build ()
		);
				
		String pluginShortOpts = "nifdcrx";
		URIAdditionPlugin plugin = new URIAdditionPlugin ();
		ArgumentDefinition<?>[] argDefs = plugin.getArgumentDefinitions ();
		
		for ( int i = 0; i < pluginShortOpts.length (); i++ )
		{
			char letter = pluginShortOpts.charAt ( i );
			ArgumentDefinition<?> arg = argDefs [ i ];

			String descr = arg.getDescription ();
			if ( arg.getDefaultValue () != null ) descr += "\nDefault = " + arg.getDefaultValue () + ".";
			
			opts.addOption ( Option.builder ( "" + letter )
				.longOpt ( arg.getName () )
				.desc ( descr )
				.build ()
			);
		}
		
		return opts;		
	}
	
	private static void printUsage ()
	{
		out.println ();

		out.println ( "\n\n *** Ondex URI Adder ***" );
		out.println ( "\nAdds URIs to the concepts and instances of an Ondex graph" );
		
		out.println ( "\nSyntax:" );
		out.println ( "\n\tadd-uris.sh [options] <path/to/.oxl> <path/to/output.oxl>" );		
		
		out.println ( "\nOptions:" );
		HelpFormatter helpFormatter = new HelpFormatter ();
		PrintWriter pw = new PrintWriter ( out, true );
		helpFormatter.printOptions ( pw, 100, getOptions (), 2, 4 );
		
		out.println ( "\n" );
		
		exitCode = 1;
	}

	/**
	 * This can be used when {@link #NO_EXIT_PROP} is "true" and you're invoking the main() method from 
	 * a JUnit test. It tells you the OS exit code that the JVM would return upon exit.
	 */
	public static int getExitCode () {
		return exitCode;
	}	
}
