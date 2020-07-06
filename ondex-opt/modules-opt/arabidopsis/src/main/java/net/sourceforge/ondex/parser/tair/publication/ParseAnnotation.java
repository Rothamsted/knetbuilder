package net.sourceforge.ondex.parser.tair.publication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;

/**
 * 
 * This class needs the annotation folder.
 * Contains functional annotation data offered by TAIR curators 
 * 
 * @author keywan
 * 
 */
public class ParseAnnotation {

	private Map<String,Integer> conIDs = new HashMap<String,Integer>();
	private Map<String, Set<ONDEXConcept>> nameToConcepts;
	
	private ConceptClass ccPub;
	private DataSource dataSourceTAIR;
	private DataSource dataSourceNLM;
	private EvidenceType etIMPD;
	private RelationType pub_in;

	public void parse(String tairReleaseDir, ONDEXGraph graph, Map<String,ONDEXConcept> knownAC) {


		ccPub = graph.getMetaData().getConceptClass(MetaData.PUBLICATION);
		dataSourceTAIR = graph.getMetaData().getDataSource( MetaData.DB);
		dataSourceNLM = graph.getMetaData().getDataSource( MetaData.NLM);
		etIMPD = graph.getMetaData().getEvidenceType(MetaData.IMPD);
		pub_in = graph.getMetaData().getRelationType( MetaData.publishedIn);
		
		nameToConcepts = new HashMap<String, Set<ONDEXConcept>>(knownAC.size()*7);
		Set<ONDEXConcept> concepts;

        for (ONDEXConcept ondexConcept : knownAC.values()) {
            for (ConceptName cn : ondexConcept.getConceptNames()) {
                String name = cn.getName();
                concepts = nameToConcepts.get(name);
                if (concepts == null) {
                    concepts = new HashSet<ONDEXConcept>();
                    nameToConcepts.put(name, concepts);
                }
                concepts.add(ondexConcept);
            }
            for (ConceptAccession acc : ondexConcept.getConceptAccessions()){
            	if(acc.getElementOf().equals(dataSourceTAIR)){
                    concepts = nameToConcepts.get(acc.getAccession());
                    if (concepts == null) {
                        concepts = new HashSet<ONDEXConcept>();
                        nameToConcepts.put(acc.getAccession(), concepts);
                    }
                    concepts.add(ondexConcept);
            	}
            }
        }
		
		try {
			parsePublications(tairReleaseDir, graph, knownAC, nameToConcepts);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	

	}
	
	
	/*
	 * find additional PMID for genes within a list created for us by a TAIR curator
	 * Downloaded from ftp://ftp.arabidopsis.org/home/tair/User_Requests/
	 */
	private void parsePublications(String tairReleaseDir, ONDEXGraph graph,
			Map<String,ONDEXConcept> knownAC,
			Map<String,Set<ONDEXConcept>> names)
				throws FileNotFoundException, IOException {


		String fileName = tairReleaseDir  + "LocusPublished.02262008.txt";
		
		String[] files = new File(tairReleaseDir).list();
		for (String file: files) {
			if (file.startsWith("LocusPublished")) {
				fileName = tairReleaseDir + file;
			}
		}
		
		File f = new File(fileName);
		if (!f.exists()) {
			ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new DataFileMissingEvent("File does not exit: "+fileName, "parsePublications()"));
		}
		
		BufferedReader input = null;

		if (fileName.endsWith(".gz")) {
			GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(fileName));
			input = new BufferedReader(new InputStreamReader(gzip));
		} else {
			input = new BufferedReader(new FileReader(fileName));
		}
		System.out.println("Parsing "+fileName);
		String inputLine = input.readLine();
		inputLine = input.readLine(); //skip the first header line
		int acc = 0;
		int n = 0;
		int u = 0;
		while (inputLine != null) {

			String[] columns = inputLine.split("\t");
			if(columns.length != 4 || columns[3].equals("")){
				inputLine = input.readLine();
				continue;
			}

			String locus = columns[0];
			String pid = columns[1];
			String tairInternalPublication = columns[2];
			String pubmedID = columns[3].replaceAll("^0+(?!$)", "");
		
			if(!conIDs.containsKey(pubmedID)){
				ONDEXConcept c = graph.getFactory().createConcept( pubmedID, dataSourceTAIR, ccPub, etIMPD);
				c.createConceptAccession( pubmedID, dataSourceNLM, false);
				c.createConceptAccession( tairInternalPublication, dataSourceTAIR, false);
				conIDs.put(pubmedID, c.getId());
			}
			
			ONDEXConcept pub = graph.getConcept( conIDs.get(pubmedID));
			
			if(knownAC.containsKey(pid)) {
				
				ONDEXConcept gene = knownAC.get(pid);
				if(graph.getRelation( gene, pub, pub_in) == null){
					acc++;
					graph.getFactory().createRelation( gene, pub, pub_in, etIMPD);
				}	
			}
			//pid not found, search for it in concept names...
			else if(names.containsKey(locus)){
				
				Set<ONDEXConcept> concepts = names.get(locus);
                for (ONDEXConcept c : concepts) {
                    if (graph.getRelation(c, pub, pub_in) == null) {
                    	n++;
                        graph.getFactory().createRelation(c, pub, pub_in, etIMPD);
                    }
                }
			}else{
				//Unknown accession or locus
				u++;
			}
			
			inputLine = input.readLine();
		}
		
		System.out.println("Publication stats - acc: "+acc+" name: "+n+" unknown: "+u);
	}
	
}
