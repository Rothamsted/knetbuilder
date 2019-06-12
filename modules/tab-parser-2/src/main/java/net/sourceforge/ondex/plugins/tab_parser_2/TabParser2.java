package net.sourceforge.ondex.plugins.tab_parser_2;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.plugins.tab_parser_2.config.ConfigParser;
import net.sourceforge.ondex.tools.subgraph.Subgraph;
import net.sourceforge.ondex.tools.tab.importer.PathParser;

/**
 * A plug-in for the workflow/integrator, which uses a simple XML to map from a particular TSV/CSV file structure 
 * (i.e., list of headers) to ONDEX entities. See the [mapping XML Schema](src/main/resources/tab_parser.xsd) and
 * its [auto-generated documentation](doc/index.html) for details. 
 * the plug-in receives a parameter pointing to an XML instance of such schema, which defines how your particular tabular
 * file (or set of tabular files having the same structure) defines ONDEX entities such as concepts and relations.

 * @author brandizi
 * <dl><dt>Date:</dt><dd>30 Nov 2016</dd></dl>
 *
 */
@Status( status = StatusType.STABLE )
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
		return new ArgumentDefinition[] {
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
      new StringArgumentDefinition ( 
      	"mergeOptions", 
      	"Merging options passed to PathParser, valid values are: " 
      	+ Arrays.toString ( PathParser.validFlags.toArray ( new String[ 0 ] ) )
      	+ ", default is " + PathParser.MERGE_ACC
      	+ ", use '-' to specify none", 
      	false, // required
      	PathParser.MERGE_ACC, // default 
      	true  // multiple vals
      )
		};
	}

	public void start () throws Exception
	{		
		ONDEXPluginArguments args = getArguments ();
    String tabInputPath = (String) args.getUniqueValue( FileArgumentDefinition.INPUT_FILE );
    String tabConfigXmlPath = (String) args.getUniqueValue( "configFile" );
    
    // The mapping parser returns an ONDEX parser straight
    PathParser tabParser = ConfigParser.parseConfigXml ( tabConfigXmlPath, graph, tabInputPath );
    
    String[] mergeFlags = (String[]) args.getObjectValueArray ( "mergeOptions" );
    if ( ! ( mergeFlags == null || mergeFlags.length == 1 && "-".equals ( mergeFlags [ 0 ] ) ) )
    	// Consider them if non null and non default
    	tabParser.setProcessingOptions ( mergeFlags );
    
    Subgraph newGraph = tabParser.parse ();
    int nconcepts = Optional.ofNullable ( newGraph.getConcepts () ).map ( Set::size ).orElse ( 0 );
    int nrelations = Optional.ofNullable ( newGraph.getRelations () ).map ( Set::size ).orElse ( 0 );
	
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
