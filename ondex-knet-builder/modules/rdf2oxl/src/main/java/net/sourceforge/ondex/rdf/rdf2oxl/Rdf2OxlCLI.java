package net.sourceforge.ondex.rdf.rdf2oxl;

import static java.lang.System.out;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * # The command-line version of the RDF/OXL converter
 * 
 * This variant accepts the path of a TDB triple store as input, where the graph to be converted is fetched from.  
 * 
 * This class is used by invocation scripts in the final distribution, see `src/main/assembly/resources`.     
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>12 Nov 2018</dd></dl>
 *
 */
public class Rdf2OxlCLI
{
	/**
	 * If you set this to true, main() will not invoke {@link System#exit(int)}. This is useful in unit tests.
	 */
	public static final String NO_EXIT_PROP = "net.sourceforge.ondex.debug.no_jvm_exit"; 
			
	static int exitCode = 0;
	
	private static Logger log = LoggerFactory.getLogger ( Rdf2OxlCLI.class );

	
	
	public static void main ( String... args )
	{
		try
		{
			exitCode = 0;
			CommandLineParser clparser = new DefaultParser ();
			CommandLine cli = clparser.parse ( getOptions(), args );
			args = cli.getArgs ();
			
			if ( cli.hasOption ( "help" ) || !cli.hasOption ( "tdb" ) || args.length < 1 ) {
				printUsage ();
				return;
			}
			
			out.println ();

			String oxlPath = args [ 0 ];
			String tdbPath = cli.getOptionValue ( "tdb", null );
			String springFile = cli.getOptionValue ( "config", null );
			boolean zipFlag = !cli.hasOption ( "plain" );
			
			// if springFile is null, this will pick the Spring config from the classpath
			Rdf2OxlConverter.convert ( springFile, tdbPath, oxlPath, zipFlag );
			
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
	
	static Options getOptions ()
	{
		Options opts = new Options ();

		opts.addOption ( Option.builder ( "h" )
			.desc ( "Prints out this message" )
			.longOpt ( "help" )
			.build ()
		);
		
		opts.addOption ( Option.builder ( "c" ) 
			.desc ( "Configuration file (see examples/sample-cfg.xml). Uses an embedded default otherwise. "
					+   "WARNING! use 'file:///...' to specify absolute paths (Spring requirement)." )
			.longOpt ( "config" )
			.argName ( "bean configuration file.xml" )
			.numberOfArgs ( 1 )
			.build ()
		);
		
		opts.addOption ( Option.builder ( "t" ) 
			.desc ( 
				"Uses a TDB databases at this location as input. For the moment this parameter is mandatory, " + 
		    "we will add more SPARQL endpoint types in future."
			)
			.longOpt ( "tdb" )
			.argName ( "TDB path" )
			.numberOfArgs ( 1 )
			.build ()
		);

		opts.addOption ( Option.builder ( "p" ) 
			.desc ( 
				"Doesn't compress (GZIP) the output"
			)
			.longOpt ( "plain" )
			.build ()
		);
		
		return opts;		
	}
	
	static void printUsage ()
	{
		out.println ( "\n\n *** Ondex RDF/OXL Converter ***" );
		out.println ( "\nImports BioKNO-based RDF files into Ondex OXL files" );
		
		out.println ( "\nSyntax:" );
		out.println ( "\n\trdf2odx.sh [options] <path/to/*.oxl>" );		
		
		out.println ( "\nOptions:" );
		HelpFormatter helpFormatter = new HelpFormatter ();
		PrintWriter pw = new PrintWriter ( out, true );
		helpFormatter.printOptions ( pw, 100, getOptions (), 2, 4 );
		
		out.println ();
		
		exitCode = 1;
	}

	/**
	 * This can be used when {@link #NO_EXIT_PROP} is "true" and you're invoking the main() method from 
	 * a JUnit test. It tells you the OS exit code that the JVM would return upon exit.
	 */
	static int getExitCode () {
		return exitCode;
	}	
}
