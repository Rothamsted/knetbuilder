package net.sourceforge.ondex.plugins.tab_parser_2;

import java.io.File;
import java.util.Collections;

import org.apache.log4j.Logger;

import com.google.common.base.Optional;

import net.sourceforge.ondex.args.ArgumentDefinition;
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
		return "1.0-SNAPSHOT";
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
      )
		};
	}

	public void start () throws Exception
	{		
    String tabInputPath = (String) getArguments().getUniqueValue( FileArgumentDefinition.INPUT_FILE );
    String tabConfigXmlPath = (String) getArguments().getUniqueValue( "configFile" );
    
    // The mapping parser returns an ONDEX parser straight
    PathParser tabParser = ConfigParser.parseConfigXml ( tabConfigXmlPath, graph, tabInputPath );
    
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
