package net.sourceforge.ondex.export.cyjsJson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.core.util.GraphLabelsUtils;
import net.sourceforge.ondex.parser.oxl.Parser;
import net.sourceforge.ondex.utils.OndexPluginUtils;

/**
 * To test the cyjsJSON Export code.
 * 
 * @author Ajit Singh
 */
@SuppressWarnings({"unchecked" })
public class CyjsJsonExportTest
{
	private static ONDEXGraph humanGraph;
	private static Path humanJsPath = Path.of ( "target", "cy-export-basics-test.json" ).toAbsolutePath ();

	@BeforeClass
	public static void prepareExport()
	{
		// human dataset 
		humanGraph = Parser.loadOXL (  CyjsJsonExportTest.class.getResource ( "MyNetwork_NeuroDisease_subset.oxl" ).getFile () );
		
		// Enable to see a graph dump
		//ONDEXGraphOperations.dumpAll ( humanGraph );
		
		OndexPluginUtils.runPlugin (
			Export.class, 
			humanGraph,
			Map.of ( FileArgumentDefinition.EXPORT_FILE, humanJsPath.toString() )
		);		
	}
	
	
	
	@Test
	public void testBasics () throws IOException
	{
		
		String idPath = "[?(@['id'] == 48320)]";
		String nodePath = "$.nodes.." + idPath;
		String metaPath = "$.ondexmetadata.." + idPath;
		String prefferedPath = "$.ondexmetadata.concepts." + idPath + ".conames.[?(@['name'] == 'PRNP')]";
		String attributePath = "$.ondexmetadata.concepts." + idPath + ".attributes.[?(@['attrname'] == 'TAXID')]";
		
		assertTrue ( "humanJsPath not created!", new File ( humanJsPath.toString() ).exists () );
		List<String> json = Files.readAllLines ( humanJsPath );

		String nodesData = JsonPath.parse ( json.get ( 0 ) ).json ().toString ();
		String metaData = JsonPath.parse ( json.get ( 2 ) ).json ().toString ();

		String nodeJson = nodesData.substring ( nodesData.indexOf ( "{" ), nodesData.length () );
		String metaJson = metaData.substring ( metaData.indexOf ( "{" ), metaData.length () );

		assertJsonExport ( "Concept Type don't match", nodeJson, nodePath, "conceptType", "Gene" );
		assertJsonExport ( "DisplayValue don't match", nodeJson, nodePath, "displayValue", "PRNP" );
		
		assertJsonExport ( "OfType don't match", metaJson, metaPath, "ofType", "Gene" );
		assertJsonExport ( "Value don't match", metaJson, metaPath, "value", "PRNP" );
		assertJsonExport ( "ElementOf don't match", metaJson, metaPath, "elementOf", "ENSEMBL" );
		
		assertJsonExport( "IsPreferred is false", metaJson, prefferedPath, "isPreferred", "true" );
		assertJsonExport( "TaxId don't match", metaJson, attributePath, "value", "9606" );
			
	}
	
	private void assertJsonExport (String errorMessage, String json, String jsonPath, String param,
			String expectedValue ) {
		assertEquals ( errorMessage, 
				( ( ( Map<String, Object> ) ( ( JSONArray ) JsonPath.parse ( json ).read ( jsonPath ) ).get ( 0 ) ).get ( param ) ),
				expectedValue );
	}
	
	
	@Test
	public void testBestLabel () throws IOException
	{
		
		String nonGenePath = "$.ondexmetadata.concepts.[?(@['ofType'] != 'Gene')].[?(@['id'] == 48391)]";
		String GenePath = "$.ondexmetadata.concepts.[?(@['ofType'] == 'Gene')].[?(@['id'] == 48320)]";
		
		
		List<String> json = Files.readAllLines ( humanJsPath );

		String metaData = JsonPath.parse ( json.get ( 2 ) ).json ().toString ();
		String metaJson = metaData.substring ( metaData.indexOf ( "{" ), metaData.length () );
		
		ONDEXConcept nonGeneConcept = humanGraph.getConcepts ().stream ().filter ( concept -> concept.getId ()== 48391 ).findAny ().get ();
		assertJsonExport ( "Concept Name in Non Gene don't match", metaJson, nonGenePath, "value", GraphLabelsUtils.getBestConceptLabel ( nonGeneConcept ) );
		
		ONDEXConcept geneConcept = humanGraph.getConcepts ().stream ().filter ( concept -> concept.getId () == 48320 ).findAny ().get ();
		assertJsonExport ( "Concept Name in Gene don't match", metaJson, GenePath, "value", GraphLabelsUtils.getBestConceptLabel ( geneConcept ) );
		
		// Pick non-gene node from humanGraph and verify that the corresponding JSON label is the same as getBestConceptLabel()
		// Pick some gene node from JSON and verify the same
	}
	
	
	@Test
	@Ignore ( "TODO: delete, old test, not very meaningful" )
	public void testJsonExport () throws Throwable
	{

		// Using .oxl test file located under src/test/resources/.
		ClassLoader classLoader = getClass ().getClassLoader ();
		// example .oxl file to Test.
		// File oxlTestFile= new File(classLoader.getResource("oxlnetwork.oxl").getFile());
		File oxlTestFile = new File ( classLoader.getResource ( "MyNetwork_NeuroDisease_subset.oxl" ).getFile () ); // human
																																																								// disease
																																																								// subset

		// output file (with timestamped filename) to get exported network graph data in JSON format.
		String outputFileName = "networkGraph_" + new SimpleDateFormat ( "yyyyMMddhhmmss'.json'" ).format ( new Date () );
		File jsonOutputFile = new File ( System.getProperty ( "java.io.tmpdir" ) + File.separator + outputFileName );

		// Creating a MemoryONDEXGraph object.
		ONDEXGraph graph = new MemoryONDEXGraph ( "test" );

		// Import the OXL test file using the OXL Parser from the Ondex API.
		System.out
				.println ( "Test using example OXL file: " + oxlTestFile.getName () + "\n path: " + oxlTestFile.getPath () );

		Parser parser = new Parser (); // OXL Parser.

		ONDEXPluginArguments pa = new ONDEXPluginArguments ( parser.getArgumentDefinitions () );
		pa.setOption ( FileArgumentDefinition.INPUT_FILE, oxlTestFile.getAbsolutePath () );

		parser.setONDEXGraph ( graph );
		parser.setArguments ( pa );
		System.out.println ( "Running OXL Parser..." );
		// Now, Parse the given input (.oxl) file to the 'graph' object.
		parser.start ();

		System.out.println ( "Evaluating retrieved ONDEXGraph object..." );

		// Check retrieved 'graph' contents.
		int conceptsCount = graph.getConcepts ().size ();
		int relationsCount = graph.getRelations ().size ();

		// tests
		// assertNotNull(graph);
		assertTrue ( conceptsCount > 0 );
		assertTrue ( relationsCount > 0 );
		System.out.println ( "conceptsCount= " + conceptsCount + " , relationsCount= " + relationsCount );
		// System.out.println("Concepts: ");
		String conName;
		for ( ONDEXConcept con : graph.getConcepts () )
		{
			int conId = con.getId (); // concept ID.
			// System.out.print("Concept ID: "+ conId);
			// test
			assertTrue ( conId > 0 );
			conName = " ";
			if ( con.getConceptName () != null )
			{
				if ( con.getConceptName ().getName () != null )
				{
					conName = con.getConceptName ().getName (); // concept name.
				}
			}
			/*
			 * System.out.print(" , Name: "+ conName); System.out.print(" , Type: "+ con.getOfType().getFullname()); //
			 * concept type. System.out.print("\n");
			 */
		}
		// System.out.print("\n");
		// System.out.println("Relations: ");
		for ( ONDEXRelation rel : graph.getRelations () )
		{
			int relId = rel.getId (); // relation ID.
			// test
			assertTrue ( relId > 0 );
			int srcCon = rel.getFromConcept ().getId (); // relation source ID.
			int targetCon = rel.getToConcept ().getId (); // relation target ID.
			String edgeLbl = rel.getOfType ().getFullname (); // relation type label.
			/*
			 * System.out.print("Relation ID: "+ relId); System.out.print(" , Source: "+ srcCon);
			 * System.out.print(" , Target: "+ targetCon); System.out.print(" , Edge Label: "+ edgeLbl);
			 * System.out.print("\n");
			 */
		}
		// System.out.print("\n");

		// Now, Export the graph as JSON using JSON Exporter plugin.
		Export jsonExp = new Export (); // Export.

		ONDEXPluginArguments ea = new ONDEXPluginArguments ( jsonExp.getArgumentDefinitions () );
		ea.setOption ( FileArgumentDefinition.EXPORT_FILE, jsonOutputFile.getAbsolutePath () );

		System.out.println ( "JSON Export file: " + ea.getOptions ().get ( FileArgumentDefinition.EXPORT_FILE ) );
		// test
		assertTrue (
				ea.getOptions ().get ( FileArgumentDefinition.EXPORT_FILE ).contains ( jsonOutputFile.getAbsolutePath () ) );

		jsonExp.setArguments ( ea );
		jsonExp.setONDEXGraph ( graph );

		System.out.println ( "Running JSON Exporter plugin... \n" );

		// Export the contents of the 'graph' object as multiple JSON objects to an output file ('jsonOutputFile').
		jsonExp.start ();

		// delete on exit
		// jsonOutputFile.deleteOnExit();
	}

}
