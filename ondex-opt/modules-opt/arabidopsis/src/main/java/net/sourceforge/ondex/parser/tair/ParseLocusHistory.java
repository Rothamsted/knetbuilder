package net.sourceforge.ondex.parser.tair;

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

/**
 * 
 * @author hindlem
 *
 */
public class ParseLocusHistory {

	private Map<String, Set<String>> mergedObsoletes;
	private Map<String, Set<String>> merged;
	private Map<String, Set<String>> split;
	private Set<String> obsoleted;
	
	/**
	 * 
	 * @param localHistoryFile  file to parse
	 */
	public ParseLocusHistory(String localHistoryFile) {
		mergedObsoletes = new HashMap<String, Set<String>>();
		merged = new HashMap<String, Set<String>>();
		split = new HashMap<String, Set<String>>();
		obsoleted = new HashSet<String>();
		parse(localHistoryFile);
	}
	
	public void parse(String localHistoryFile) {
		
		if (!new File(localHistoryFile).exists()) {
			System.err.println("missing file \""+localHistoryFile+"\" locus histories are not included in this parse!");
			return;
		} 
		
		System.out.println("Parsing "+localHistoryFile);
		
		
		try {
			BufferedReader input = null;

			if (localHistoryFile.endsWith(".gz")) {
				GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(
						localHistoryFile));
				input = new BufferedReader(new InputStreamReader(gzip));
			} else {
				input = new BufferedReader(new FileReader(localHistoryFile));
			}

			while (input.ready()) {
				String line = input.readLine();
				String[] values = line.split("\t");
				
				if (values[2].equalsIgnoreCase("mergeobsolete") && values.length==5) {
					String oldLocus = values[0].trim().toUpperCase();
					String newLocus = values[4].trim().toUpperCase();
					Set<String> obsoletes = mergedObsoletes.get(newLocus);
					if (obsoletes == null) {
						obsoletes = new HashSet<String>(1);
						mergedObsoletes.put(newLocus, obsoletes);
					}
					obsoletes.add(oldLocus);
				} else if (values[2].equalsIgnoreCase("merge")) {
					String locusA = values[0].trim().toUpperCase();
					String locusB = values[4].trim().toUpperCase();
					//forward complement
					Set<String> obsoletes = merged.get(locusA);
					if (obsoletes == null) {
						obsoletes = new HashSet<String>(1);
						merged.put(locusA, obsoletes);
					}
					obsoletes.add(locusB);
					
					//reverse complement
					obsoletes = merged.get(locusB);
					if (obsoletes == null) {
						obsoletes = new HashSet<String>(1);
						merged.put(locusB, obsoletes);
					}
					obsoletes.add(locusA);
				} else if ((values[2].equalsIgnoreCase("split") 
						|| values[2].equalsIgnoreCase("splitinsert"))
						&& values.length==5) {
					String locusA = values[0].trim().toUpperCase();
					String locusB = values[4].trim().toUpperCase();
					//forward complement
					Set<String> obsoletes = split.get(locusA);
					if (obsoletes == null) {
						obsoletes = new HashSet<String>(1);
						split.put(locusA, obsoletes);
					}
					obsoletes.add(locusB);
					
					//reverse complement
					obsoletes = split.get(locusB);
					if (obsoletes == null) {
						obsoletes = new HashSet<String>(1);
						split.put(locusB, obsoletes);
					}
					obsoletes.add(locusA);
				} else if (values[2].equalsIgnoreCase("obsolete")){
					obsoleted.add(values[0].trim().toUpperCase());
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.err.println("Merged size: "+merged.size());
		System.err.println("Split size: "+split.size());
		System.err.println("Obsolete size: "+obsoleted.size());
		System.out.println("Parsed locus history");
	}

	/**
	 * 
	 * @return new locus to obsolete locus 
	 */
	public Map<String, Set<String>> getMergedObsoletes() {
		return mergedObsoletes;
	}

	/**
	 * 
	 * @return merged locuss <Back and forward equiv>
	 */
	public Map<String, Set<String>> getMerged() {
		return merged;
	}

	/**
	 * 
	 * @return locuss that have been split
	 */
	public Map<String, Set<String>> getSplit() {
		return split;
	}

	/**
	 * 
	 * @return locuss that have been made obsolete
	 */
	public Set<String> getObsoleted() {
		return obsoleted;
	}
	
}
