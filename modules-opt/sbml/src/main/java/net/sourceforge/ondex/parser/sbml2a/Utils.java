/*
 * Utils class to be used by SML parsers
 */

package net.sourceforge.ondex.parser.sbml2a;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Christian Brenninkmeijer
 */
public class Utils {

    /**
     * Scrubs an XML file by replacing the "INF" values with numbers.
     * 
     * InputFile will be renamed, with a new file created with the old name.
     * value="INF" will be replaced with value="1000000"
     * value="-INF" will be replaced with value="-1000000"
     * @param inputName Name of the inpuit file
     * @return
     */
   public static String ScrubXML(String inputName) throws IOException {
       File inputFile = new File(inputName);
       File directory = inputFile.getParentFile();
       File newFile = File.createTempFile("Scrubbed", ".xml", directory);
       FileReader inputReader = new FileReader(inputFile);
       BufferedReader inputBuffer = new BufferedReader(inputReader);
       FileWriter outputWriter = new FileWriter(newFile);
       BufferedWriter outputBuffer = new BufferedWriter(outputWriter);
       while (inputBuffer.ready()) {
           String line = inputBuffer.readLine();
           line = line.replace("value=\"INF\"", "value=\"1000000\"");
           line = line.replace("value=\"-INF\"", "value=\"-1000000\"");
           outputBuffer.write(line);
           outputBuffer.write(System.getProperty("line.separator"));
       }
       outputBuffer.close();
       return newFile.getAbsolutePath();
    }

    /**
     * Testing method hard coded for my machine only.
     * @param args
     */
   	public static void main(String[] args) throws IOException {
        String name = ScrubXML ("c:/Users/Christian/Ondex/webservices/WS_Test_TESTER/data/sbml_big.xml");
        System.out.println(name);
    }
}
