package net.sourceforge.ondex.parser.tair.genome;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.tair.MetaData;
import net.sourceforge.ondex.parser.tair.ParseLocusHistory;
import net.sourceforge.ondex.parser.tair.Parser;
import net.sourceforge.ondex.tools.oldfastafunctions.FastaBlock;
import net.sourceforge.ondex.tools.oldfastafunctions.ReadFastaFiles;
import net.sourceforge.ondex.tools.oldfastafunctions.WriteFastaFile;


//FIXME: Write properly using auxfunctions!
public class ParseProteinSequences {

	private Map<String, Set<String>> obsoletesMap;
	private Map<String, Set<String>> mergedMap;

	public Map<String,ONDEXConcept> parse(ONDEXGraph graph, String inFilesDir, ParseLocusHistory locusHistory) {
		this.obsoletesMap = locusHistory.getMergedObsoletes();
		this.mergedMap = locusHistory.getMerged();
		
		String sequenceDir = inFilesDir + Parser.BLASTSETSSUBDIR + File.separator;
		
		File dir = new File(sequenceDir);
		if (!dir.exists()) {
			ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new DataFileMissingEvent("Directory does not exits: "+sequenceDir, Parser.getCurrentMethodName()));
			return null;
		}
		String[] filenames = dir.list();

		String fileName = null; 
		for (String file:filenames) {
			if (file.startsWith(Parser.TAIRX+"_pep")) {
				// gets the genes + UTR
				fileName = sequenceDir + file; 
			}
		}

		if (fileName == null) {
			ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new DataFileMissingEvent("Protein sequence data missing: with prefix \"TAIR7_pep\" in directory " + sequenceDir, Parser.getCurrentMethodName()));
		}else {
			ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new GeneralOutputEvent("Protein sequence data version: "+fileName.substring("TAIR7_pep".length())+" detected", Parser.getCurrentMethodName()));
		}

		WriteFastaFile writeFastaFileTair = new WriteFastaFileTair();

		try {
			System.out.println("Parsing "+fileName);
			ReadFastaFiles.parseFastaFile(graph, fileName, writeFastaFileTair);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ((WriteFastaFileTair) writeFastaFileTair).getConceptList();
	}

	private class WriteFastaFileTair extends WriteFastaFile {

		private Map<String,ONDEXConcept> parsedConcepts = new HashMap<String,ONDEXConcept>();
		private ConceptClass cc;
		private DataSource elementOf;
		private EvidenceType etIMPD;
		private AttributeName taxIdAttr;
		private AttributeName aaAttr; 

		@Override
		public void parseFastaBlock(ONDEXGraph graph, FastaBlock fasta) {

			String header = fasta.getHeader();	    
			String[] fields = header.split(" \\| ");

			String firstAccession = fields[0].trim();
			String pid = fields[0].trim();
			
			String[] symbols = ParseGenome.getSymbols(fields[1]);

			//accession contains version number, remove:
			firstAccession = ParseGenome.chompVersion(firstAccession);

			String annot = fields[2].trim();

			if (cc == null) cc = graph.getMetaData().getConceptClass(MetaData.protein);
			if (elementOf == null) elementOf = graph.getMetaData().getDataSource(MetaData.tair);
			if (etIMPD == null) etIMPD = graph.getMetaData().getEvidenceType(MetaData.IMPD);
			if (taxIdAttr == null) taxIdAttr = graph.getMetaData().getAttributeName(MetaData.taxID);
			if (aaAttr == null) aaAttr = graph.getMetaData().getAttributeName(MetaData.aminoAcids);

			ONDEXConcept ac = graph.getFactory().createConcept(pid, annot, elementOf, cc, etIMPD);

			if (ac != null) {
				parsedConcepts.put(pid, ac);

				String locus = pid.substring(0, pid.indexOf("."));
				Set<String> obsoletes = obsoletesMap.get(locus);
				if (obsoletes != null) {
					Iterator<String> obIt = obsoletes.iterator();
					while (obIt.hasNext()) {
						String ob = obIt.next();
						if (ac.getConceptAccession(ob, elementOf) == null) 
							ac.createConceptAccession(ob, elementOf, true);
					}
				}
				
				Set<String> merged = mergedMap.get(locus);
				if (merged != null) {
					Iterator<String> merIt = merged.iterator();
					while (merIt.hasNext()) {
						String mer = merIt.next();
						if ((obsoletes == null || !obsoletes.contains(mer)) && ac.getConceptAccession(mer, elementOf) == null) {
							ac.createConceptAccession(mer, elementOf, true);
						}
					}
				}
				
				if ( ac.getConceptAccession(pid, elementOf) == null) 
					ac.createConceptAccession(pid, elementOf, false);
				
				if ( ac.getConceptAccession(locus, elementOf) == null) 
					ac.createConceptAccession(locus, elementOf, true);

				if(symbols != null){
					for(String symbol : symbols){
						if(ac.getConceptName(symbol) == null){
							ac.createConceptName(symbol, true);
						}
					}
				}
				String seq = fasta.getSequence();
				if(seq.endsWith("*")){
					seq = seq.substring(0, seq.length()-1);
				}	
				ac.createAttribute(taxIdAttr, MetaData.taxIDValue, true);
				ac.createAttribute(aaAttr, seq.trim(), false);
			} else {
				System.out.println("header not parsed succesfully: (" + header+ ")");
			}
		}

		public Map<String,ONDEXConcept> getConceptList(){
			return parsedConcepts;
		}
	}
}
