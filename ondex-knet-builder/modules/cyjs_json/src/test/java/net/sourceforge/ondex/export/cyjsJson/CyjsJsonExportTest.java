package net.sourceforge.ondex.export.cyjsJson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.util.GraphLabelsUtils;
import net.sourceforge.ondex.parser.oxl.Parser;
import net.sourceforge.ondex.utils.OndexPluginUtils;

/**
 * To test the cyjsJSON Export code.
 *  
 */
public class CyjsJsonExportTest
{
	private static ONDEXGraph humanGraph;
	private static Path humanJsPath = Path.of ( "target", "cy-export-basics-test.json" ).toAbsolutePath ();
	private static DocumentContext exportedJson;
	
	@BeforeClass
	public static void prepareExport() throws IOException
	{
		// human dataset 
		humanGraph = Parser.loadOXL (  CyjsJsonExportTest.class.getClassLoader ().getResource ( "MyNetwork_NeuroDisease_subset.oxl" ).getFile () );
		
		// Enable to see a graph dump
		// ONDEXGraphOperations.dumpAll ( humanGraph );

		OndexPluginUtils.runPlugin (
			Export.class, 
			humanGraph,
			Map.of ( FileArgumentDefinition.EXPORT_FILE, humanJsPath.toString() ,
					BooleanArgumentDefinition.EXPORT_PLAIN_JSON,true )
		);
		
		assertTrue ( "humanJsPath not created!", new File ( humanJsPath.toString() ).exists () );
		
		exportedJson =  JsonPath.parse ( Files.readString ( humanJsPath ) );
	}
	
	
	
	@Test
	public void testBasics () throws IOException
	{
		int conceptId = 48320;
		String idPath = idPath ( conceptId );
		var nodePath = nodePath ( conceptId );
		
		String metaPath = "$.allGraphData.ondexmetadata.." + idPath;
		String prefferedPath = "$.allGraphData.ondexmetadata.concepts." + idPath + ".conames.[?(@['name'] == 'PRNP')]";
		String attributePath = "$.allGraphData.ondexmetadata.concepts." + idPath + ".attributes.[?(@['attrname'] == 'TAXID')]";
				
		
		assertJson ( "Concept Type don't match", exportedJson, nodePath, "conceptType", "Gene" );
		assertJson ( "DisplayValue don't match", exportedJson, nodePath, "displayValue", "PRNP" );
		
		assertJson ( "OfType don't match", exportedJson, metaPath, "ofType", "Gene" );
		assertJson ( "Value don't match", exportedJson, metaPath, "value", "PRNP" );
		assertJson ( "ElementOf don't match", exportedJson, metaPath, "elementOf", "ENSEMBL" );
		
		assertJson ( "IsPreferred is false", exportedJson, prefferedPath, "isPreferred", "true" );
		assertJson ( "TaxId don't match", exportedJson, attributePath, "value", "9606" );	
	}
		
	
	@Test
	public void testBestLabel () throws IOException
	{
		String nonGenePath = "$.allGraphData.ondexmetadata.concepts.[?(@['ofType'] != 'Gene')].[?(@['id'] == 48391)]";
		String genePath = "$.allGraphData.ondexmetadata.concepts.[?(@['ofType'] == 'Gene')].[?(@['id'] == 48320)]";
		

		// Pick non-gene node from humanGraph and verify that the corresponding JSON label is the same as getBestConceptLabel()
		ONDEXConcept nonGeneConcept = humanGraph.getConcept ( 48391 );
		assertJson ( "Concept Name in Non Gene don't match", exportedJson, nonGenePath, "value", GraphLabelsUtils.getBestConceptLabelWithGeneSpeciePrefix ( nonGeneConcept, true ) );
		
		// Pick some gene node from JSON and verify the same
		ONDEXConcept geneConcept = humanGraph.getConcept ( 48320 );
		assertJson ( "Concept Name in Gene don't match", exportedJson, genePath, "value", GraphLabelsUtils.getBestConceptLabelWithGeneSpeciePrefix ( geneConcept, true ) );
		
		// TODO: similar test for $.nodes
	}
	
	/**
	 * Checks an array of exported objects. arrayJsonPath is expected to return an array of objects, the method
	 * takes the first and assert that its field jsonField is set to expectedValue.
	 */
	static <T> void assertJson (
		String errorMessage, DocumentContext json, String arrayJsonPath, String jsonField, T expectedValue 
	)
	{
		T jsValue = getJson ( json, arrayJsonPath, jsonField );
		assertEquals ( errorMessage, expectedValue, jsValue );
	}

	/**
	 * The same, but with negation.
	 */
	static <T> void negateJson (
		String errorMessage, DocumentContext json, String arrayJsonPath, String jsonField, T expectedValue 
	)
	{
		T jsValue = getJson ( json, arrayJsonPath, jsonField );
		assertNotEquals ( errorMessage, expectedValue, jsValue );
	}
	
	/**
	 * See {@link #assertJson(String, DocumentContext, String, String, Object)}. This gets the value to be tested
	 * using JSONPath.
	 *  
	 */
	@SuppressWarnings ( {"unchecked" } )
	static <T> T getJson ( DocumentContext json, String arrayJsonPath, String jsonField )
	{
		var jsArray = ( JSONArray ) json.read ( arrayJsonPath );
		var jsElem = ( Map<String, Object> ) jsArray.get ( 0 );
		T jsValue = (T) jsElem.get ( jsonField );

		return jsValue;
	}

	/**
	 * Can be used in other methods to fetch an exported concept from JSON by its ID. This returns 
	 * the JSONPath needed for various JSON object types about an Ondex concept with a given ID:
	 */
	static String idPath ( int id )
	{
		return String.format ( "[?(@['id'] == %s)]", id );
	}
	
	/**
	 * Can be used in other methods to fetch an exported concept from JSON (ie, a node) by its ID
	 */
	static String nodePath ( int id )
	{
		return "$.graphJSON.nodes.." + idPath ( id );
	}
}
