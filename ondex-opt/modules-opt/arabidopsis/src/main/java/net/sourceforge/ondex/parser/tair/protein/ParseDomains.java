package net.sourceforge.ondex.parser.tair.protein;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.parser.tair.MetaData;
import net.sourceforge.ondex.parser.tair.Parser;

/**
 * Parses the domain file as provided by tair. 
 * ftp://ftp.arabidopsis.org/home/tair/Proteins/Domains/TAIR8_all.domains
 * 
 * TODO: Currently it does not parse non Interpo domains.
 * TODO: This functionality should be added
 * 
 * @author hoekmanb
 *
 */
public class ParseDomains {
	
	private ConceptClass cc;
	private DataSource elementOf;
	private EvidenceType etIMPD;

	public void parseDomains(String fileName, ONDEXGraph graph, Map<String,ONDEXConcept> knownAC) throws FileNotFoundException, IOException {

		
		if (cc == null) cc = graph.getMetaData().getConceptClass(MetaData.DOMAIN);
		if (elementOf == null) elementOf = graph.getMetaData().getDataSource(MetaData.tair);
		if (etIMPD == null) etIMPD = graph.getMetaData().getEvidenceType(MetaData.IMPD);
		DataSource interPro = graph.getMetaData().getDataSource(MetaData.IPRO);
		
		if (interPro == null) {
			System.err.println("Could not find cv: "+MetaData.IPRO);
		} 
		if (cc == null) {
			System.err.println("Could not find cc: "+MetaData.DOMAIN);
		}
		
		
		BufferedReader input = null;

		if (fileName.endsWith(".gz")) {
			GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(
					fileName));
			input = new BufferedReader(new InputStreamReader(gzip));
		} else {
			input = new BufferedReader(new FileReader(fileName));
		}

		String inputLine = input.readLine();
		
		Map<String,ONDEXConcept> domains = new HashMap<String,ONDEXConcept>();

		while (inputLine != null) {
			
			// column 7 = Attribute evalue //TODO: add to Attribute, as the rest of the Tables.
			
			String[] columns = inputLine.split("\t+");

			String pid = columns[0].trim();
			if(knownAC.containsKey(pid)) {
				ONDEXConcept ac = knownAC.get(pid);				
				
				String domId = columns[3].trim();
				String conName = columns[4].trim();
				pid = columns[9].trim();
				String desc = columns[10].trim();
				
				ONDEXConcept acDomain = null;
				boolean writeRelation = true;
				
				if(pid.length() <= 1 || pid.equalsIgnoreCase("NULL") || pid.equalsIgnoreCase("0")) {
					if(Parser.DEBUG) {					
						System.out.println("PID: "+pid+" domID: "+domId+" conName: "+conName + " ("+inputLine+")");
					}
					writeRelation = false;
				}
				else if(domains.containsKey(pid)){
					acDomain = domains.get(pid);
					if(acDomain.getConceptAccession(domId, interPro) == null) {
						if(domId.length() > 1 && !domId.equalsIgnoreCase("NULL")) {
							acDomain.createConceptAccession(domId, interPro,false);
						}
					}
				} else {
					acDomain = graph.getFactory().createConcept(pid,desc,desc, elementOf, cc, etIMPD);
					acDomain.createConceptAccession(pid, interPro,false);
					domains.put(pid, acDomain);
					
					if(domId.length() > 1 && !domId.equalsIgnoreCase("NULL")) {
						acDomain.createConceptAccession(domId, interPro,false);
					}
					if(conName.length() > 1 && !conName.equalsIgnoreCase("NULL")) {
						acDomain.createConceptName(conName,false);
					}
				}
				
				if(writeRelation == true) {
					RelationType rtSetIsPartOf = graph.getMetaData().getRelationType(MetaData.isPartOf);					
					if(graph.getRelation(acDomain, ac, rtSetIsPartOf) == null){
						graph.getFactory().createRelation(ac, acDomain, rtSetIsPartOf, etIMPD);
					}
				}
				
			} else {
				System.out.println("Domains file contains unknown accessions ("+pid+")");
			}
			inputLine = input.readLine();
		}
	}

}
