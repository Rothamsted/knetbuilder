package net.sourceforge.ondex.plugins.tab_parser_2;

import java.io.File;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.google.common.base.Optional;
import com.machinezoo.noexception.Exceptions;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.plugins.tab_parser_2.config.ConfigParser;
import net.sourceforge.ondex.tools.subgraph.Subgraph;
import net.sourceforge.ondex.tools.tab.importer.PathParser;

/**
 * A plug-in for the Integrator, which uses a simple XML to map from a particular TSV/CSV file structure 
 * (i.e., list of headers) to ONDEX entities. See the [mapping XML Schema](src/main/resources/tab_parser.xsd) and
 * its [auto-generated documentation](doc/index.html) for details. 
 * the plug-in receives a parameter pointing to an XML instance of such schema, which defines how your particular tabular
 * file (or set of tabular files having the same structure) defines ONDEX entities such as concepts and relations.

 * @author brandizi
 * <dl><dt>Date:</dt><dd>30 Nov 2016</dd></dl>
 *
 */
public class TabParser2 extends ONDEXParser
{
  private Logger log = Logger.getLogger ( this.getClass() );

	public String getId ()
	{
		return "tabParser2";
	}

	public String getName ()
	{
		return "Tab Parser 2";
	}

	public String getVersion ()
	{
		return "1.0";
	}

	public ArgumentDefinition<?>[] getArgumentDefinitions ()
	{
		return new ArgumentDefinition<?>[] {
      new FileArgumentDefinition ( 
      	FileArgumentDefinition.INPUT_FILE, FileArgumentDefinition.INPUT_FILE_DESC, true, true, false, false
      ),
      new FileArgumentDefinition ( 
      	"configFile", 
      	"The XML file that maps the input tabular file to ONDEX. Must comply with tab_parser.xsd, see documentation", 
      	true, // required 
      	true, // preExisting 
      	false // isDirectory
      ),
      new BooleanArgumentDefinition ( PathParser.MERGE_ACC, "Accession-based concept merging in the parser", false, true ),
      new BooleanArgumentDefinition ( PathParser.MERGE_NAME, "Name-based concept merging in the parser", false, false ),
      new BooleanArgumentDefinition ( PathParser.MERGE_GDS, "Data source-based concept merging in the parser", false, false )
		};
	}

	public void start () throws Exception
	{		
		ONDEXPluginArguments args = getArguments ();
    String tabInputPath = (String) args.getUniqueValue( FileArgumentDefinition.INPUT_FILE );
    String tabConfigXmlPath = (String) args.getUniqueValue( "configFile" );
    
    // The mapping parser returns an ONDEX parser straight
    PathParser tabParser = ConfigParser.parseConfigXml ( tabConfigXmlPath, graph, tabInputPath );
    
    // Merging flags
    //
    String[] mergeFlags = (String[]) Stream.of ( PathParser.MERGE_ACC, PathParser.MERGE_NAME, PathParser.MERGE_GDS )
    .filter ( flag ->
    	// Keeps it if it's true, else this kind of merging won't happen, because it won't be in the final 
    	// processing options.
    	Boolean.TRUE.equals ( 
    		Exceptions
        // Wraps InvalidPluginArgumentException for getUniqueValue() into a RuntimeException   			
    		.wrap ( ex -> new IllegalArgumentException ( "Plug-in argument error: " + ex.getMessage (), ex ) )
    		.get ( () -> (Boolean) args.getUniqueValue ( flag ) )
    	)
    )
    .collect ( Collectors.toList () )    
    .toArray ( new String [ 0 ] );
    
    tabParser.setProcessingOptions ( mergeFlags );
    
    Subgraph newGraph = tabParser.parse ();
    int nconcepts = Optional.fromNullable ( newGraph.getConcepts () ).or ( Collections.<ONDEXConcept>emptySet () ).size ();
    int nrelations = Optional.fromNullable ( newGraph.getRelations () ).or ( Collections.<ONDEXRelation>emptySet () ).size ();
	
    log.info ( String.format ( 
    	"Got %d concepts and %d relations from '%s'", 
    	nconcepts, nrelations, new File ( tabInputPath ).getAbsolutePath () 
    ));
	}

	public String[] requiresValidators ()
	{
		return new String [ 0 ];
	}

}
