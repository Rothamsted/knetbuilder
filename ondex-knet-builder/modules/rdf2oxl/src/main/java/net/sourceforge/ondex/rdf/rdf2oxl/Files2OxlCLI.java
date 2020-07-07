package net.sourceforge.ondex.rdf.rdf2oxl;

import static java.lang.System.out;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.ArrayUtils.subarray;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This variant is a simple wrapper that first loads files into a TDB database and then invokes {@link Rdf2OxlCLI}.  
 * 
 * For the moment, it relies on the Jena's distribution and its `tdbloader` command. This class is used by invocation 
 * scripts in the final distribution, see `src/main/assembly/resources`.  
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>12 Nov 2018</dd></dl>
 *
 */
public class Files2OxlCLI
{
	private static Logger log = LoggerFactory.getLogger ( Files2OxlCLI.class );

		
	public static void main ( String... args )
	{
		try
		{
			Rdf2OxlCLI.exitCode = 0;
			
			String originalArgs[] = args; 
			
			CommandLineParser clparser = new DefaultParser ();
			CommandLine cli = clparser.parse ( Rdf2OxlCLI.getOptions(), args );
			args = cli.getArgs ();
			
			if ( cli.hasOption ( "help" ) || args.length < 2 ) {
				printUsage ();
				return;
			}
			
			out.println ();

			String oxlPath = args [ 0 ];
			String tdbPath = cli.getOptionValue ( "tdb", null );

			if ( tdbPath == null ) {
				tdbPath = "/tmp/rdf2oxl-tdb";
				out.format ( "\tCreating new TDB at '%s'\n", tdbPath );
				FileUtils.deleteDirectory ( new File ( tdbPath  ) );
			}
			
			
			
			out.println ( "\n\tInvoking tdbloader" );
			String JENA_HOME = System.getenv ( "JENA_HOME" );
			
			if ( JENA_HOME == null ) throw new IllegalArgumentException (
				"Please set JENA_HOME to use rdf2oxl"
			);
			
			org.apache.commons.exec.CommandLine tdbCmd = new org.apache.commons.exec.CommandLine ( 
				JENA_HOME + "/bin/tdbloader"
			);

			tdbCmd.addArgument ( "--loc" );
			tdbCmd.addArgument ( tdbPath );
			for ( int i = 1; i < args.length; i++ ) tdbCmd.addArgument ( args [ i ] );
			DefaultExecutor tdbExec = new DefaultExecutor();
			int tdbExitCode = tdbExec.execute ( tdbCmd );
			System.out.flush ();
			System.err.flush ();
						
			if ( tdbExitCode != 0 ) {
				// Not clear what the hell Jena returns, so just a warning
				// NOTE: this is unreacheable until you set tdbExec.setExitValues () (cause it fails with != 0 by default) 
				log.warn ( "tdbloader exited with the {} code", tdbExitCode );
				Rdf2OxlCLI.exitCode = 1;
			}

			
			
			out.println ( "\n\tInvoking rdf2oxl" );

			// New args are the same as before minus the files passed to tdbload
			
			List<String> rdf2OxlArgs = 
				new LinkedList<> ( asList ( subarray ( originalArgs, 0, originalArgs.length - args.length ) ) );
						
			// Plus --tdb, if it wasn't specified
			if ( !cli.hasOption ( "tdb" ) ) {
				rdf2OxlArgs.add ( "--tdb" );
				rdf2OxlArgs.add ( tdbPath );
			}
			
			rdf2OxlArgs.add ( oxlPath );
			
			// Must not exit, let's override the corresponding flag
			String exitFlag = System.getProperty ( Rdf2OxlCLI.NO_EXIT_PROP );
			System.setProperty ( Rdf2OxlCLI.NO_EXIT_PROP, "false" );
			
			Rdf2OxlCLI.main ( rdf2OxlArgs.toArray ( new String [ 0 ] ) );
			
			out.println ();
			if ( Rdf2OxlCLI.exitCode == 0 ) log.info ( "Files2Oxl finished." );
			
			System.setProperty ( Rdf2OxlCLI.NO_EXIT_PROP, exitFlag );
		}
		catch ( Throwable ex ) 
		{
			log.error ( "Execution failed with the error: " + ex.getMessage (), ex  );
			Rdf2OxlCLI.exitCode = 1; // TODO: proper exit codes
		}
		finally 
		{
			if ( !"true".equals ( System.getProperty ( Rdf2OxlCLI.NO_EXIT_PROP ) ) )
				System.exit ( Rdf2OxlCLI.exitCode );
		}
	}

	
	private static void printUsage ()
	{
		out.println ();

		out.println ( "\n\n *** Ondex RDF/OXL Converter ***" );
		out.println ( "\nImports BioKNO-based RDF files into Ondex OXL files" );
		
		out.println ( "\nSyntax:" );
		out.println ( "\n\tfilef2odx.sh [options] <oxl file> <rdf file>..." );		
				
		out.println ( 
			"\nLoads files into a TDB triple store set by --tdb (uses a default in /tmp if none is set),\n" + 
			"then invokes rdf2oxl.sh, passing it the same invocation options.\n" + 
			"\n" + 
			"Requires JENA_HOME to be set.\n" 
		);
								
		Rdf2OxlCLI.exitCode = 1;
	}
	
}
