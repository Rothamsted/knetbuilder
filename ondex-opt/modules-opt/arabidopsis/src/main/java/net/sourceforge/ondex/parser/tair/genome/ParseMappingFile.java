package net.sourceforge.ondex.parser.tair.genome;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.tair.Parser;

/**
 * Parses a TAIR8_NCBI_mapping_* file and creates NCBI accessions
 * 
 * @author keywan
 *
 */
public class ParseMappingFile {

	public void parseNCBI(String fileName, ONDEXGraph graph, Map<String,ONDEXConcept> knownAC, DataSource elementOf) throws FileNotFoundException, IOException {

		BufferedReader input = null;

		if (fileName.endsWith(".gz")) {
			GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(fileName));
			input = new BufferedReader(new InputStreamReader(gzip));
		} else {
			input = new BufferedReader(new FileReader(fileName));
		}

		String inputLine = input.readLine();
		
		//TAIR7 mapping file contains 2 columns whereas TAIR8 has three
		int refCol = (Parser.TAIRX.equals("TAIR7")) ? 0 : 1;
		int pidCol = (Parser.TAIRX.equals("TAIR7")) ? 1 : 2;
		
		// skip header line
		inputLine = input.readLine();

		while (inputLine != null) {

			String[] columns = inputLine.split("\t");

			String xRefAcc = ParseGenome.chompVersion(columns[refCol].trim());
			String pid = columns[pidCol].trim();
			if (pid.trim().endsWith("p")) {
				pid = pid.substring(0, pid.length()-1);
			}
			
			if(knownAC.containsKey(pid)) {
				knownAC.get(pid).createConceptAccession(xRefAcc, elementOf, false);
			} else {
//				System.out.println("Mapping file contains unknown accessions ("+pid+")");
			}

			inputLine = input.readLine();
		}
	}


}
