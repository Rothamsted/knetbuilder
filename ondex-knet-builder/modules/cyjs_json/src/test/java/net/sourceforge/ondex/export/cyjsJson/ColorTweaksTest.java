package net.sourceforge.ondex.export.cyjsJson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.Test;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.utils.OndexPluginUtils;

/**
 * To test the cyjsJSON Export code.
 *  
 */
@SuppressWarnings({"unchecked" })
public class ColorTweaksTest
{
	private static EvidenceType et;
	private static DataSource dataSource;
	
	private static Path jsPath = Path.of ( "target", "test.json" ).toAbsolutePath ();
	
	private static ONDEXGraph getTestGraph ( String name ) {
		
		ONDEXGraph graph = new MemoryONDEXGraph ( "testGraph" );

		ConceptClass ccProtein = graph.getMetaData ().getFactory ().createConceptClass ( "Protein" );
		et = graph.getMetaData ().getFactory ().createEvidenceType ( "I_made_it_up" );
		dataSource = graph.getMetaData ().getFactory ().createDataSource ( "matts_db" );
		
		graph.getFactory ().createConcept ( name , dataSource, ccProtein, et );
		
		return graph;
	}

	public static DocumentContext exportConcept ( String name ) throws IOException
	{
		ONDEXGraph testGraph = getTestGraph ( name );
		
		OndexPluginUtils.runPlugin (
			Export.class, 
			testGraph,
			Map.of ( FileArgumentDefinition.EXPORT_FILE, jsPath.toString () ,
					BooleanArgumentDefinition.EXPORT_PLAIN_JSON,true )
		);
		
		return  JsonPath.parse ( Files.readString ( jsPath ) );
		
	}
	
	@Test
	public void testNoExistingColor() throws IOException {
		
		DocumentContext testConcept = exportConcept ( "test" );
		String idPath = "[?(@['id'] == 1)]";
		String nodePath = "$.graphJSON.nodes.." + idPath;
		
		assertJsonExport ( "Concept Border Color don't match", testConcept, nodePath, "conceptBorderColor", "black" );
		
	}
	
	@Test
	public void testExistingColor() throws IOException {
		
		DocumentContext htmlTagsConcept = exportConcept ( "Hello, <span style = \"background-color: #000F12\"><b>World</b></span>" );
		
		String idPath = "[?(@['id'] == 1)]";
		String nodePath = "$.graphJSON.nodes.." + idPath;
		
		assertJsonExport ( "Concept Border Color don't match", htmlTagsConcept, nodePath, "conceptBorderColor", "black" );
		assertNotJsonExport ( "Concept Border Color match with the concept name", htmlTagsConcept, nodePath, "conceptBorderColor", "#000F12" );
	}
	
	@Test
	public void testHashedName() throws IOException {
		
		DocumentContext fancyNameConcept = exportConcept ( "#000F12 concept" );
		
		String idPath = "[?(@['id'] == 1)]";
		String nodePath = "$.graphJSON.nodes.." + idPath;
		
		assertJsonExport ( "Concept Border Color don't match", fancyNameConcept, nodePath, "conceptBorderColor", "black" );
		assertNotJsonExport ( "Concept Border Color match with the concept name", fancyNameConcept, nodePath, "conceptBorderColor", "#000F12" );
	}
	
	
	
	/**
	 * Checks an array of exported objects. arrayJsonPath is expected to return an array of objects, the method
	 * takes the first and assert that its field jsonField is set to expectedValue.
	 */
	private void assertJsonExport (
		String errorMessage, DocumentContext json, String arrayJsonPath, String jsonField, Object expectedValue )
	{
		var jsArray = ( JSONArray ) json.read ( arrayJsonPath );
		var jsElem = ( Map<String, Object> ) jsArray.get ( 0 );
		var jsValue = jsElem.get ( jsonField );
		
		assertEquals ( errorMessage, expectedValue, jsValue );
	}
	
	private void assertNotJsonExport (
			String errorMessage, DocumentContext json, String arrayJsonPath, String jsonField, Object expectedValue )
		{
			var jsArray = ( JSONArray ) json.read ( arrayJsonPath );
			var jsElem = ( Map<String, Object> ) jsArray.get ( 0 );
			var jsValue = jsElem.get ( jsonField );
			
			assertNotEquals( errorMessage, expectedValue, jsValue );
		}
}
