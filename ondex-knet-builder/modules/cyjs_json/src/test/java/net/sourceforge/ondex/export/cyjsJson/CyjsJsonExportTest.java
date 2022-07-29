package net.sourceforge.ondex.export.cyjsJson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

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
@SuppressWarnings({"unchecked" })
public class CyjsJsonExportTest
{
	private static ONDEXGraph humanGraph;
	private static Path humanJsPath = Path.of ( "target", "cy-export-basics-test.json" ).toAbsolutePath ();
	private static String exportedJson;
	
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
		
		exportedJson = Files.readString ( humanJsPath );
	}
	
	
	
	@Test
	public void testBasics () throws IOException
	{
		String idPath = "[?(@['id'] == 48320)]";
		String nodePath = "$.graphJSON.nodes.." + idPath;
		String metaPath = "$.allGraphData.ondexmetadata.." + idPath;
		String prefferedPath = "$.allGraphData.ondexmetadata.concepts." + idPath + ".conames.[?(@['name'] == 'PRNP')]";
		String attributePath = "$.allGraphData.ondexmetadata.concepts." + idPath + ".attributes.[?(@['attrname'] == 'TAXID')]";
				
		
		assertJsonExport ( "Concept Type don't match", exportedJson, nodePath, "conceptType", "Gene" );
		assertJsonExport ( "DisplayValue don't match", exportedJson, nodePath, "displayValue", "PRNP" );
		
		assertJsonExport ( "OfType don't match", exportedJson, metaPath, "ofType", "Gene" );
		assertJsonExport ( "Value don't match", exportedJson, metaPath, "value", "PRNP" );
		assertJsonExport ( "ElementOf don't match", exportedJson, metaPath, "elementOf", "ENSEMBL" );
		
		assertJsonExport( "IsPreferred is false", exportedJson, prefferedPath, "isPreferred", "true" );
		assertJsonExport( "TaxId don't match", exportedJson, attributePath, "value", "9606" );
			
	}
		
	
	@Test
	public void testBestLabel () throws IOException
	{
		
		String nonGenePath = "$.allGraphData.ondexmetadata.concepts.[?(@['ofType'] != 'Gene')].[?(@['id'] == 48391)]";
		String genePath = "$.allGraphData.ondexmetadata.concepts.[?(@['ofType'] == 'Gene')].[?(@['id'] == 48320)]";
		

		// Pick non-gene node from humanGraph and verify that the corresponding JSON label is the same as getBestConceptLabel()
		ONDEXConcept nonGeneConcept = humanGraph.getConcept ( 48391 );
		assertJsonExport ( "Concept Name in Non Gene don't match", exportedJson, nonGenePath, "value", GraphLabelsUtils.getBestConceptLabel ( nonGeneConcept, true ) );
		
		// Pick some gene node from JSON and verify the same
		ONDEXConcept geneConcept = humanGraph.getConcept ( 48320 );
		assertJsonExport ( "Concept Name in Gene don't match", exportedJson, genePath, "value", GraphLabelsUtils.getBestConceptLabel ( geneConcept, true ) );
		
		// TODO: similar test for $.nodes
	}
	
	
	private void assertJsonExport (String errorMessage, String json, String jsonPath, String param,
			String expectedValue ) {
		assertEquals ( errorMessage, 
			expectedValue,
			( ( ( Map<String, Object> ) ( ( JSONArray ) JsonPath.parse ( json ).read ( jsonPath ) ).get ( 0 ) ).get ( param ) )
		);
	}
}
