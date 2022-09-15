package net.sourceforge.ondex.export.cyjsJson;

import static net.sourceforge.ondex.export.cyjsJson.CyjsJsonExportTest.assertJson;
import static net.sourceforge.ondex.export.cyjsJson.CyjsJsonExportTest.negateJson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.core.util.ONDEXGraphUtils;
import net.sourceforge.ondex.utils.OndexPluginUtils;

/**
 * To test the cyjsJSON Export code.
 *  
 */
public class ColorTweaksTest
{		
	@SuppressWarnings ( "unused" )
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	private static ONDEXGraph createTestGraph ( String conceptName )
	{
		ONDEXGraph graph = new MemoryONDEXGraph ( "testGraph" );

		ConceptClass ccProtein = ONDEXGraphUtils.getOrCreateConceptClass ( graph, "Protein" );
		EvidenceType et = ONDEXGraphUtils.getOrCreateEvidenceType ( graph, "I_made_it_up" );
		DataSource dataSource = ONDEXGraphUtils.getOrCreateDataSource ( graph, "matts_db" );
				
		graph.getFactory ().createConcept ( conceptName , dataSource, ccProtein, et );
		
		return graph;
	}

	private static DocumentContext createAndExportTestGraph ( String conceptName ) throws IOException
	{
		ONDEXGraph testGraph = createTestGraph ( conceptName );
		
		var jsPath = Path.of ( "target", "test.json" ).toAbsolutePath ();
		
		OndexPluginUtils.runPlugin (
			Export.class, 
			testGraph,
			Map.of ( FileArgumentDefinition.EXPORT_FILE, jsPath.toString () ,
					BooleanArgumentDefinition.EXPORT_PLAIN_JSON,true )
		);
		
		return  JsonPath.parse ( Files.readString ( jsPath ) );
		
	}
	
	@Test
	public void testNoExistingColor() throws IOException
	{
		DocumentContext jsExport = createAndExportTestGraph ( "test" );
		
		String nodePath = CyjsJsonExportTest.nodePath ( 1 );
		
		assertJson ( "Concept Border Color don't match", jsExport, nodePath, "conceptTextBGcolor", "black" );
	}
	
	@Test
	public void testExistingColor() throws IOException
	{
		final var testColor = "#000F12";
		
		DocumentContext jsExport = createAndExportTestGraph ( 
			String.format ( "Hello, <span style = \"background-color: %s\"><b>World</b></span>", testColor )
		);
		
		String nodePath = CyjsJsonExportTest.nodePath ( 1 );
				
		assertJson ( "conceptBorderColor not reused!", jsExport, nodePath, "conceptTextBGcolor", testColor );
		negateJson ( "There is still conceptBorderColor: black?!", jsExport, nodePath, "conceptTextBGcolor", "black" );
	}
	
	@Test
	public void testHashedName() throws IOException
	{
		final var testColor = "#000F12";

		DocumentContext jsExport = createAndExportTestGraph ( testColor + " concept" );
				
		var nodePath = CyjsJsonExportTest.nodePath ( 1 );
		
		assertJson ( "Concept Border Color don't match", jsExport, nodePath, "conceptTextBGcolor", "black" );
		negateJson ( "Concept Border Color match with the concept name", jsExport, nodePath, "conceptTextBGcolor", testColor );
	}
	
}
