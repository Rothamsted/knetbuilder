package net.sourceforge.ondex.export.json;

import net.sourceforge.ondex.export.json.Export;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import static junit.framework.Assert.assertTrue;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.parser.oxl.Parser;
import org.junit.Test;

/**
 * To test the JSON Export code.
 * @author Ajit Singh
 * @version 14/10/15
 */
public class JsonExportTest {

    @Test
    public void testJsonExport() throws Throwable {

        // Using .oxl test file located under src/test/resources/.
        ClassLoader classLoader= getClass().getClassLoader();
        // example .oxl file to Test.
//	File oxlTestFile= new File(classLoader.getResource("oxlnetwork.oxl").getFile());
//	File oxlTestFile= new File(classLoader.getResource("result_wheat_161015_4.oxl").getFile());
//	File oxlTestFile= new File(classLoader.getResource("Ondex_Barley_Seed-size-QTL.oxl").getFile());
	File oxlTestFile= new File(classLoader.getResource("wheat_eq4_colaIssue.oxl").getFile());

        // output file (with timestamped filename) to get exported network graph data in JSON format.
        String outputFileName= "networkGraph_"+ new SimpleDateFormat("yyyyMMddhhmmss'.json'").format(new Date());
        File jsonOutputFile= new File(System.getProperty("java.io.tmpdir") + File.separator + outputFileName);

        // Creating a MemoryONDEXGraph object.
        ONDEXGraph graph= new MemoryONDEXGraph("test");

        // Import the OXL test file using the OXL Parser from the Ondex API.
        System.out.println("Test using example OXL file: "+ oxlTestFile.getName() +"\n Path: "+ oxlTestFile.getPath());

        Parser parser= new Parser(); // OXL Parser.

        ONDEXPluginArguments pa= new ONDEXPluginArguments(parser.getArgumentDefinitions());
        pa.setOption(FileArgumentDefinition.INPUT_FILE, oxlTestFile.getAbsolutePath());

        parser.setONDEXGraph(graph);
        parser.setArguments(pa);
        System.out.println("Running OXL Parser...");
        // Now, Parse the given input (.oxl) file to the 'graph' object.
        parser.start();

        System.out.println("Evaluating retrieved ONDEXGraph object...");

        // Check retrieved 'graph' contents.
        int conceptsCount= graph.getConcepts().size();
        int relationsCount= graph.getRelations().size();

        // tests
//        assertNotNull(graph);
        assertTrue(conceptsCount > 0);
        assertTrue(relationsCount > 0);
        System.out.println("conceptsCount= "+ conceptsCount +" , relationsCount= "+ relationsCount);
        System.out.println("Concepts: ");
        String conName;
        for(ONDEXConcept con : graph.getConcepts()) {
            int conId= con.getId(); // concept ID.
//            System.out.print("Concept ID: "+ conId);
            // test
            assertTrue(conId > 0);
            conName= " ";
            if(con.getConceptName() != null) {
               if(con.getConceptName().getName() != null) {
                  conName= con.getConceptName().getName(); // concept name.
                 }
              }
/*            System.out.print(" , Name: "+ conName);
            System.out.print(" , Type: "+ con.getOfType().getFullname()); // concept type.
            System.out.print("\n");*/
           }
//        System.out.print("\n");
//        System.out.println("Relations: ");
        for(ONDEXRelation rel : graph.getRelations()) {
            int relId= rel.getId(); // relation ID.
            // test
            assertTrue(relId > 0);
            int srcCon= rel.getFromConcept().getId(); // relation source ID.
            int targetCon= rel.getToConcept().getId(); // relation target ID.
            String edgeLbl= rel.getOfType().getFullname(); // relation type label.
/*            System.out.print("Relation ID: "+ relId);
            System.out.print(" , Source: "+ srcCon);
            System.out.print(" , Target: "+ targetCon);
            System.out.print(" , Edge Label: "+ edgeLbl);
            System.out.print("\n");*/
           }
//        System.out.print("\n");

        // Now, Export the graph as JSON using JSON Exporter plugin.
        Export jsonExp= new Export(); // Export.

        ONDEXPluginArguments ea= new ONDEXPluginArguments(jsonExp.getArgumentDefinitions());
        ea.setOption(FileArgumentDefinition.EXPORT_FILE, jsonOutputFile.getAbsolutePath());

        System.out.println("JSON Export file: "+ ea.getOptions().get(FileArgumentDefinition.EXPORT_FILE));
        // test
        assertTrue(ea.getOptions().get(FileArgumentDefinition.EXPORT_FILE).contains(jsonOutputFile.getAbsolutePath()));

        jsonExp.setArguments(ea);
        jsonExp.setONDEXGraph(graph);

        System.out.println("Running JSON Exporter plugin... \n");

        // Export the contents of the 'graph' object as multiple JSON objects to an output file ('jsonOutputFile').
        jsonExp.start();

        // delete on exit
//        jsonOutputFile.deleteOnExit();
    }

}
