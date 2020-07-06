package net.sourceforge.ondex.parser.tair.protein;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.zip.GZIPInputStream;

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.tair.genome.ParseGenome;

/**
 * Parser for ftp://ftp.arabidopsis.org/home/tair/Proteins/Id_conversions/AGI2Uniprot.20080418
 * 
 * @author keywan
 *
 */
public class ParseMappingFile {

	public void parseMappingFile(String fileName, ONDEXGraph graph, Map<String,ONDEXConcept> knownAC, DataSource elementOf) throws FileNotFoundException, IOException {

		BufferedReader input = null;
		
		if (fileName.endsWith(".gz")) {
			GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(
					fileName));
			input = new BufferedReader(new InputStreamReader(gzip));
		} else {
			input = new BufferedReader(new FileReader(fileName));
		}

		String inputLine = input.readLine();
		int accCount = 0;
		while (inputLine != null) {

			String[] columns = inputLine.split("\t");
			if(!inputLine.startsWith("##") && columns.length >= 2) { // ## = comment

				String uniprotAcc = columns[0];
				String[] tairAccs  = columns[1].split(";");

				for(String tairAcc : tairAccs) {

					if (tairAcc.trim().endsWith("p")) {
						tairAcc = tairAcc.substring(0, tairAcc.length()-1);
					}
					String pid = tairAcc.trim();
					if(knownAC.containsKey(pid)) {				
						String accession = uniprotAcc.trim();
						if(accession.length() > 1) {
							ONDEXConcept concept = knownAC.get(pid);
							
							concept.createConceptAccession(accession, elementOf, false);
							accCount++;
							Matcher matcher = ParseGenome.tairSpliter.matcher(accession);
							if (matcher.find()) {
								concept.createConceptAccession(accession.substring(0, matcher.start()), elementOf, false);
							}
						}
						
					} else {
						System.out.println("Protein Mapping file contains unknown accessions ("+pid+")");
					}
				}
			}
			inputLine = input.readLine();
		}
		System.out.println("TAIR with UniProt acc: "+accCount);
	}

}
