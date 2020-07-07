package net.sourceforge.ondex.rdf.export;

import static java.lang.System.out;
import static net.sourceforge.ondex.rdf.export.RDFFileExporter.DEFAULT_X_LANG;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.oxl.Parser;

/**
 * Tests for the {@link RDFFileExporterCLI}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>12 Nov 2018</dd></dl>
 *
 */
public class RDFFileExporterCLI
{
	/**
	 * If you set this to true, main() will not invoke {@link System#exit(int)}. This is useful in unit tests.
	 */
	public static final String NO_EXIT_PROP = "net.sourceforge.ondex.debug.no_jvm_exit"; 
			
	private static int exitCode = 0;
	
	private static Logger log = LoggerFactory.getLogger ( RDFFileExporterCLI.class );

	
	
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
			String outLang = cli.getOptionValue ( "lang", DEFAULT_X_LANG );

			log.info ( "Loading '{}'", inPath );
			ONDEXGraph graph = Parser.loadOXL ( inPath );
			
			log.info ( "Graph Loaded, now exporting to RDF", inPath );
			RDFFileExporter xporter = new RDFFileExporter ();
			xporter.export ( graph, outPath, outLang );
			
			out.println ();
			if ( exitCode == 0 ) log.info ( "Conversion finished." );
		}
		catch ( Throwable ex ) 
		{
			log.error ( "Execution failed with the error: " + ex.getMessage (), ex  );
			exitCode = 1; // TODO: proper exit codes
		}
		finally 
		{
			if ( !"true".equals ( System.getProperty ( NO_EXIT_PROP ) ) )
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
		
		opts.addOption ( Option.builder ( "l" )
			.longOpt ( "lang" )
			.desc ( "The RDF format to produce. Accepts values from either Jena's RDFFormat (https://goo.gl/XVQBHi) " +
				"or Jena's Lang (https://goo.gl/gbp6bL). The default " + DEFAULT_X_LANG + " writes Turtle in an efficient way." )
			.hasArg ()
			.argName ( "RDF Language" )
			.build ()
		);
		
		return opts;		
	}
	
	private static void printUsage ()
	{
		out.println ();

		out.println ( "\n\n *** Ondex RDF Exporter ***" );
		out.println ( "\nExports Ondex's OXL files to RDF" );
		
		out.println ( "\nSyntax:" );
		out.println ( "\n\todx2rdf.sh [options] <path/to/.oxl> <path/to/rdf>" );		
		
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
