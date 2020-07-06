package net.sourceforge.ondex.parser.tair.genome;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.parser.tair.MetaData;
import net.sourceforge.ondex.parser.tair.ParseLocusHistory;
import net.sourceforge.ondex.parser.tair.Parser;

public class ParseGenome {

	public final static Pattern tairSpliter = Pattern.compile("[.][0-9]+$");
	
	private Map<String,ONDEXConcept> genesMap;
	private Map<String,ONDEXConcept> proteinsMap;

	public void parseGenome(String tairReleaseDir, ONDEXGraph graph) {

		// prefer large locus history file
		String locusFile = tairReleaseDir+"locushistory.txt";
		
		ParseLocusHistory locusHistory = new ParseLocusHistory(locusFile);

		//	Parses the Gene sequences:
		ParseGeneSequences parseGeneSeqs = new ParseGeneSequences();
		genesMap = parseGeneSeqs.parse(graph, tairReleaseDir, locusHistory);

		//	Parses the Proteins sequences:
		ParseProteinSequences parseProteinSeqs = new ParseProteinSequences();
		proteinsMap = parseProteinSeqs.parse(graph, tairReleaseDir, locusHistory);	

		// Genes and proteins should contain the same accession if they do then map them:
		Set<String> keys = new HashSet<String>();
		if (genesMap != null) {
			keys.addAll(genesMap.keySet());
		}
		if (proteinsMap != null) {
			keys.addAll(proteinsMap.keySet());
		}

		RelationType rtSetEncodeBy = graph.getMetaData().getRelationType(MetaData.encodedBy);
		EvidenceType etIMPD = graph.getMetaData().getEvidenceType(MetaData.IMPD);

		Iterator<String> keysIt = keys.iterator();

		while(keysIt.hasNext()) {
			String accession = keysIt.next();
			if(genesMap.containsKey(accession) && proteinsMap.containsKey(accession)) {
				//map the gene to protein:
				graph.getFactory().createRelation(genesMap.get(accession), proteinsMap.get(accession), rtSetEncodeBy, etIMPD);
			}
		}

		boolean parseNCBImapping = true;
		
		if(parseNCBImapping){
			ParseMappingFile mapper = new ParseMappingFile();
			
			//NCBI DataSource's:
			DataSource refSeqG = graph.getMetaData().getDataSource(MetaData.ncNM);
			if(refSeqG == null)
				graph.getMetaData().getFactory().createDataSource(MetaData.ncNM);
			DataSource refSeqP = graph.getMetaData().getDataSource(MetaData.ncNP);
			if(refSeqP == null)
				graph.getMetaData().getFactory().createDataSource(MetaData.ncNP);
	
			//Get the NCBI accessions for the genes	
			try {
				File dir = new File(tairReleaseDir);
				if (!dir.exists()) {
					ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new DataFileMissingEvent("Directory does not exits: "+tairReleaseDir, Parser.getCurrentMethodName()));
				}
				// different filenames in TAIR7 and TAIR8
				String rna = (Parser.TAIRX.equals("TAIR7")) ? "mRNA" : "RNA";
				String ncbi = (Parser.TAIRX.equals("TAIR9")) ? "_NCBI_REFSEQ" : "_NCBI";
				
				String[] files = dir.list();
				boolean found = false;
				for (String file: files) {
					if (file.startsWith(Parser.TAIRX+ncbi+"_mapping_"+rna)) {
						System.out.println("Parsing "+file);
						mapper.parseNCBI(tairReleaseDir+file, graph, genesMap, refSeqG);
						found = true;
					}
				}
				if (!found) {
					ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new DataFileMissingEvent(Parser.TAIRX+"_NCBI_mapping_mRNA is missing from dir: "+tairReleaseDir, Parser.getCurrentMethodName()));
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
			//Get the NCBI accessions for the proteins
			try {
				String prot = (Parser.TAIRX.equals("TAIR9")) ? "PROT" : "prot";
				String ncbi = (Parser.TAIRX.equals("TAIR9")) ? "_NCBI_REFSEQ" : "_NCBI";
				
				String[] files = new File(tairReleaseDir).list();
				boolean found = false;
				for (String file: files) {
					if (file.startsWith(Parser.TAIRX+ncbi+"_mapping_"+prot)) {
						System.out.println("Parsing "+file);
						mapper.parseNCBI(tairReleaseDir+file, graph, proteinsMap, refSeqP);
						found = true;
					}
				}
				if (!found) {
					ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new DataFileMissingEvent(Parser.TAIRX+"_NCBI_mapping_prot is missing from dir: "+tairReleaseDir, Parser.getCurrentMethodName()));
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}

	public static String chompVersion(String acc){
		//return acc.substring(0,acc.indexOf("."));
		return acc;
	}
	
	public static String[] getSymbols(String symbolsLine){
		String symbols = symbolsLine.replaceFirst("Symbols: ", "");
		if(symbols.equals("")){
			return null;
		}else{
			return symbols.split(", ");
		}
	}

	public Map<String, ONDEXConcept> getGenesMap() {
		return genesMap;
	}

	public Map<String, ONDEXConcept> getProteinsMap() {
		return proteinsMap;
	}

}
