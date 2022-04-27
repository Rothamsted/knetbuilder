package net.sourceforge.ondex.export.cyjsJson;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import JSON

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.parser.oxl.Parser;
import net.sourceforge.ondex.utils.OndexPluginUtils;

/**
 * To test the cyjsJSON Export code.
 * 
 * @author Ajit Singh
 */
public class CyjsJsonExportTest
{
	private static ONDEXGraph humanGraph;
	private static String humanJsPath = Path.of ( "target", "cy-export-basics-test.json" ).toAbsolutePath ().toString ();

	@BeforeClass
	public static void prepareExport()
	{
		// human dataset
		humanGraph = Parser.loadOXL ( CyjsJsonExportTest.class.getResource ( "MyNetwork_NeuroDisease_subset.oxl" ).getFile () );
		
		// Enable to see a graph dump
		// ONDEXGraphOperations.dumpAll ( humanGraph );
		
		OndexPluginUtils.runPlugin (
			Export.class, 
			humanGraph,
			Map.of ( FileArgumentDefinition.EXPORT_FILE, humanJsPath )
		);
		
	}
	
	
	@Test
	public void testBasics ()
	{
		// TODO: test humanJsPath was created
		// TODO: pick some concept from humanGraph (inspect it via dumpAll() above, or use the Ondex desktop application)
		// and verify it is in the resulting JSON. Possibly, use JSONPath (https://www.baeldung.com/guide-to-jayway-jsonpath)
		// if Map-based methods in JSONObject and JSONArray aren't enough		
	}
	
	
	@Test
	public void testBestLabel ()
	{
		// Pick non-gene node from humanGraph and verify that the corresponding JSON label is the same as getBestConceptLabel()
		// Pick some gene node from JSON and verify the same
	}
	
	
	/**
	 * Foo test. TODO: to be removed later
	 */
	@Test
	public void testJSONObjects ()
	{
		JSON
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
