package net.sourceforge.ondex.rdf.export.graphdescriptor;

import static net.sourceforge.ondex.rdf.export.graphdescriptor.ONDEXGraphDescriptorPlugin.ARGDESCR_CTX_PATH;
import static net.sourceforge.ondex.rdf.export.graphdescriptor.ONDEXGraphDescriptorPlugin.ARGDESCR_RDF_TEMPLATE_PATH;
import static net.sourceforge.ondex.rdf.export.graphdescriptor.ONDEXGraphDescriptorPlugin.ARGDFLT_RDF_LANG;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.export.oxl.Export;
import net.sourceforge.ondex.parser.oxl.Parser;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Help.Visibility;

/**
 * A command-line wrapper for the {@link OndexGraphDescriptorTool}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>7 Dec 2021</dd></dl>
 *
 */
@Command ( 
	name = "oxldescriptor", 
		description = "\n\n  *** OXL metadata descriptor tool ***\n" +
			"\nAnnotates an OXL file with a data set descriptor, based on interoperable standards.\n",
	exitCodeOnVersionHelp = ExitCode.USAGE, // else, it's 0 and you can't know about this event
	exitCodeOnUsageHelp = ExitCode.USAGE, // ditto
	mixinStandardHelpOptions = true,
	usageHelpAutoWidth = true,
	usageHelpWidth = 120
)
public class OndexGraphDescriptorCLI implements Callable<Integer>
{
	@Option (
		names = { "-t", "--template" },
		description = ARGDESCR_RDF_TEMPLATE_PATH,
		paramLabel = "<file path>",
		showDefaultValue = Visibility.ALWAYS,
		required = true
	)
	private String rdfTemplatePath = null;

	@Option (
		names = { "-p", "--config" },
		description = ARGDESCR_CTX_PATH,
		paramLabel = "<file path>",
		showDefaultValue = Visibility.ALWAYS,
		required = false
	)
	private String contextPath = null;
	
	@Option (
		names = { "-l", "--rdf-language" },
		description = ONDEXGraphDescriptorPlugin.ARGDESCR_RDF_LANG,
		paramLabel = "<RDF language>",
		showDefaultValue = Visibility.ALWAYS,
		required = false
	)
	private String rdfLang = ARGDFLT_RDF_LANG;

	@Option (
		names = { "-x", "--export" },
		description = ONDEXGraphDescriptorPlugin.ARGDESCR_XPATH,
		paramLabel = "<file path>",
		showDefaultValue = Visibility.ALWAYS,
		required = false
	)
	private String exportPath = null;
	
	@Option (
		names = { "-m", "--x-rdf-language" },
		description = ONDEXGraphDescriptorPlugin.ARGDESCR_XPATH_LANG,
		paramLabel = "<RDF language>",
		showDefaultValue = Visibility.ALWAYS,
		required = false
	)
	private String exportRdfLang = ARGDFLT_RDF_LANG;
	
	
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
	
	@Parameters (
		paramLabel = "<path/to/input/oxl>",		
		description = "The path of the OXL to process",
		arity = "1",
		index = "0" 
	)
	private String oxlInputPath = null; 

	@Parameters (
		paramLabel = "<path/to/output/oxl>",		
		description = "The output OXL",
		arity = "0..1",
		index = "1" 
	)
	private String oxlOutputPath = null; 
		
	private Logger log = LoggerFactory.getLogger ( this.getClass () ); 
	
	
	@Override
	public Integer call ()
	{
		log.info ( "Loading the OXL: '{}'", this.oxlInputPath );
		var graph = Parser.loadOXL ( oxlInputPath );
		
		var descriptorTool = new OndexGraphDescriptorTool.Builder ()
			.setGraph ( graph )
			.setRdfTemplatePath ( this.rdfTemplatePath )
			.setContextPath ( this.contextPath )
			.setRdfLang ( this.rdfLang )
			.setOxlSourceURL ( Path.of ( this.oxlInputPath ).toAbsolutePath ().toUri ().toString () )
			.build ();
		
		descriptorTool.createDescriptor ();

		log.info ( "graph description metadata added to the graph itself" );
		
		// RDF export
		//
		exportPath = Optional.ofNullable ( this.exportPath )
			.filter ( p -> !p.isEmpty () )
			.orElse ( null );
		
		if ( exportPath != null )
		{
			var exportLang = this.exportRdfLang;
			descriptorTool.exportDescriptor ( exportPath, exportLang );
		}
		
		if ( this.oxlOutputPath != null ) {
			log.info ( "Exporting the new OXL to '{}'", this.oxlOutputPath );
			Export.exportOXL ( graph, oxlOutputPath, this.zip, this.prettyPrint );
		}
		
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
    int exitCode = new CommandLine ( new OndexGraphDescriptorCLI () ).execute ( args );
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
	
}
