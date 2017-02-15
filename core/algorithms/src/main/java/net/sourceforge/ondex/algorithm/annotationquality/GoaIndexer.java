package net.sourceforge.ondex.algorithm.annotationquality;

import net.sourceforge.ondex.core.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * This class builds a complete go annotation index for a given mapping file
 * or a given ondex graph. It then provides several methods to evaluate the go annotation.
 * @author Jochen Weile, B.Sc.
 *
 */
public class GoaIndexer {

	//####FIELDS####
	
	/**
	 * a go tree parser
	 */
	private GOTreeParser gtp;
	
	/**
	 * maps each protein id to its set of go terms.
	 */
	private HashMap<String,HashSet<Integer>> protId2goSet;
	
	/**
	 * maps each go term to its number of occurrences in the annotation.
	 */
	private HashMap<Integer,Integer> goId2Count;
	
	/**
	 * the set of proteins with an annotation.
	 */
	private HashSet<String> annotatedProteins;
	
	/**
	 * a helper mapping. prevents duplicated counts.
	 */
	private HashMap<String,HashSet<Integer>> anti_dup;
	
	public final static String hasFunction = "has_function";//molecular function
	public final static String participatesIn = "participates_in";//biological process
	public final static String locatedIn = "located_in";//cellular component
	public final static String BioProc = "BioProc";
	public final static String MolFunc = "MolFunc";
	public final static String CelComp = "CelComp";

	//####CONSTRUCTOR####
	
	/**
	 * the constructor.
	 */
	public GoaIndexer(GOTreeParser gtp){
		
		this.gtp = gtp;

	}

	//####METHODS####
	
	/**
	 * (re-)initializes the complete index.
	 */
	private void init() {
		
		protId2goSet = new HashMap<String,HashSet<Integer>>();
		
		goId2Count = new HashMap<Integer,Integer>();
		
		annotatedProteins = new HashSet<String>();
		
		anti_dup = new HashMap<String,HashSet<Integer>>();
		
	}
	
	/**
	 * parses a goa mapping file to create a new index.
	 * @param goaLocation the location of the goa mapping file.
	 * @param taxid the taxid to be scanned.
	 * @throws IOException if the file is not parseable.
	 */
	public void parseFileByTaxon(String goaLocation, int taxid, String datasource) throws IOException{
		
		init();
		
		File goaFile = new File(goaLocation);
		if (!goaFile.exists())
			throw new IOException("File does not exist!");
		
		BufferedReader r = new BufferedReader(new FileReader(goaFile));
		
		for (String line = r.readLine(); line != null; line = r.readLine()) {
			if (line.startsWith("!")) {
				continue;
			}
			String[] entries = line.split("\t");
			String taxStr = entries[12].split(":")[1].trim();
			int currTaxId;
			try {
				currTaxId = Integer.parseInt(taxStr);
				if (currTaxId == taxid) {
					if (entries[0].trim().equals(datasource)) {
						String uniprotID = entries[1].trim();
						String goID = entries[4].trim();
						int goID_int = Integer.parseInt(goID.split(":")[1]);

						register(uniprotID, goID_int);
					}
				}
			} catch (NumberFormatException nfe) {
				r.close();
				throw new IOException("Wrong file format!");
			}
		}
		
		r.close();
	}
	
	/**
	 * creates a new index of go annotations in concept accessions for a given graph.
	 * @param aog the graph.
	 * @param taxid the taxid for which the annotation should be indexed.
	 */
	public void analyzeGraphAccByTaxon(ONDEXGraph aog, int taxid) {

		init();
		
		AttributeName an_taxid = aog.getMetaData().getAttributeName("TAXID");
		DataSource dataSource_go = aog.getMetaData().getDataSource("GO");
		for(ONDEXConcept c : aog.getConceptsOfAttributeName(an_taxid)) {
			String curr_taxidStr = (String) c.getAttribute(an_taxid).getValue();
			int curr_taxid = Integer.parseInt(curr_taxidStr);
			if (curr_taxid == taxid) {
				for (ConceptAccession acc : c.getConceptAccessions()) {
					if (acc.getElementOf().equals(dataSource_go)) {
						String cid = c.getId()+"";
						String goID = acc.getAccession();
						int goID_int = Integer.parseInt(goID.split(":")[1]);
						register(cid, goID_int);
					}
				}
			}
		}
	}
	
	
	/**
	 * creates a new index of go annotations for a given graph.
	 * @param aog the graph.
	 * @param taxid the taxid for which the annotation should be indexed.
	 * 			if taxid -1 then use all taxids
	 */
	public void analyzeGraphByTaxon(ONDEXGraph aog, int taxid) {

		init();
		
		boolean useAllTaxa = false;
		if(taxid == -1) 
			useAllTaxa = true;
		
		AttributeName an_taxid = aog.getMetaData().getAttributeName("TAXID");
		DataSource dataSource_go = aog.getMetaData().getDataSource("GO");
        //iterate over all concepts that have taxids assigned
		for(ONDEXConcept c : aog.getConceptsOfAttributeName(an_taxid)) {
			String curr_taxidStr = (String) c.getAttribute(an_taxid).getValue();
			int curr_taxid = Integer.parseInt(curr_taxidStr);
			//if taxid correct
			if (curr_taxid == taxid || useAllTaxa) {
				//iterate over all connected relations
				for (ONDEXRelation r : aog.getRelationsOfConcept(c)) {
					String rt = r.getOfType().getId();
					//if they are of the correct type
					if (rt.equals(hasFunction) || rt.equals(participatesIn) || rt.equals(locatedIn)) {
						//get the GO concept on the other end
						ONDEXConcept c2 = (r.getFromConcept().equals(c))? r.getToConcept() : r.getFromConcept();
						String cc = c2.getOfType().getId();
						//if that is also of the correct type
						if (cc.equals(BioProc) || cc.equals(MolFunc) || cc.equals(CelComp)) {
							//iterate over its accessions
							for (ConceptAccession acc : c2.getConceptAccessions()) {
								//if the accession is of DataSource GO
								if (acc.getElementOf().equals(dataSource_go)) {
									String cid = c.getId()+"";
									String goID = acc.getAccession();
									int goID_int = Integer.parseInt(goID.split(":")[1]);
									//store it
									register(cid, goID_int);
									//and cancel the iteration, because there should only be one.
									break;
								}
							}
						}
					}
				}
			}
		}
	}
	
	
	
	/**
	 * registers a go term (and recursively all its subterms) for a given protein id.
	 * (don't worry, it doesn't count anything twice. dupliates cannot occurr, because
	 * every subterm is also registered in the anti_dup map).
	 * @param protID
	 * @param goID
	 */
	private void register(String protID, int goID) {
		
		HashSet<Integer> goSet = protId2goSet.get(protID);
		if (goSet == null) {
			goSet = new HashSet<Integer>();
			protId2goSet.put(protID, goSet);
		}
		goSet.add(goID);
		
		incrementTrace(protID, goID);
		
		annotatedProteins.add(protID);
	}
	
	/**
	 * a recursion method to help the registration.
	 * @param protID
	 * @param goID
	 */
	private void incrementTrace(String protID, int goID) {
		Integer c = goId2Count.get(goID);
		if (c == null)
			goId2Count.put(goID, 1);
		else
			goId2Count.put(goID, c + 1);
		
		HashSet<Integer> anti_dup_set = anti_dup.get(protID);
		if (anti_dup_set == null) {
			anti_dup_set = new HashSet<Integer>();
			anti_dup.put(protID,anti_dup_set);
		}
		anti_dup_set.add(goID);
		
		GoTerm term = gtp.getEntry(goID);
		if (term != null) {
			Vector<GoTerm> parents = term.getParents();
			for (GoTerm p : parents) {
				if (!anti_dup_set.contains(p.getId()))
					incrementTrace(protID, p.getId());
			}
		}
	}
	
	/**
	 * returns the information content bitscore for a given go term.
	 * @param goID the id of the query go term. 
	 * @return
	 */
	public double getInformationContent(int goID) {
		if (goId2Count.get(goID) != null) {
			double k_n = (double) goId2Count.get(goID);
			double k_all = (double) annotatedProteins.size();
			double p = k_n / k_all;
			double ic = -(Math.log10(p) / Math.log10(2));
			return ic;
		}
		else return Double.NaN;
	}
	
	/**
	 * returns a ready iterator with all go annotations for a given protein.
	 * @param protID the query protein id.
	 * @return an iterator with the annotations.
	 */
	public Iterator<Integer> getGoaIteratorForProtID(String protID) {
		HashSet<Integer> out = protId2goSet.get(protID);
		if (out == null)
			out = new HashSet<Integer>();
		return out.iterator();
	}
	
	/**
	 * returns the set of all go annotations for a given protein.
	 * @param protID the query protein id.
	 * @return the set of annotations for that protein.
	 */
	public Collection<Integer> getGoaForProtID(String protID) {
		Vector<Integer> out = new Vector<Integer>();
		HashSet<Integer> set = protId2goSet.get(protID);
		for (Integer i : set)
			out.add(i);
		return out;
	}
	
	/**
	 * returns a set of nonredundant GO annotations for a given concept.
	 * nonredundant means that none of the yielded terms are subterms of another.
	 * @param protID the query concept
	 * @return a set of goterm ids.
	 */
	public Collection<Integer> getNonRedundantGoaForProtID(String protID) {
		Iterator<Integer> goa_it = getGoaIteratorForProtID(protID);
		int[] goas = new int[protId2goSet.get(protID).size()];
		for (int i = 0; i < goas.length; i++)
			goas[i] = goa_it.next();
		
		Vector<Integer> out = new Vector<Integer>();
		
		for (int i = 1; i < goas.length; i++) {
			for (int j = 0; j < i; j++) {
				int dist_fwd = gtp.getDirectedDistanceBetweenTerms(goas[i], goas[j], Integer.MAX_VALUE);
				int dist_bck = gtp.getDirectedDistanceBetweenTerms(goas[j], goas[i], Integer.MAX_VALUE);
				if (dist_fwd > -1) {
//					System.out.println(goas[j]+" is subterm of "+goas[i]+" with distance "+dist_fwd);
					out.add(goas[j]);
				}
				else if (dist_bck > -1) {
//					System.out.println(goas[i]+" is subterm of "+goas[j]+" with distance "+dist_bck);
					out.add(goas[i]);
				}
			}
		}
		
		Collection<Integer> result = getGoaForProtID(protID);
		result.removeAll(out);
		
		return result;
	}
	
	/**
	 * returns the complete set of all used go terms in this annotation.
	 * @return
	 */
	public Set<Integer> getAllUsedGoTerms() {
		return goId2Count.keySet();
	}
	
	/**
	 * returns the complete set of all used protein ids in this annotation.
	 * @return
	 */
	public Set<String> getAllUsedProtIDs() {
		return protId2goSet.keySet();
	}
	
	/**
	 * for testing purposes.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			GOTreeParser p = new GOTreeParser("D:\\OndexNew\\ondex-mini-0.0.2-20090929\\ondex-mini-0.0.2-SNAPSHOT\\data\\importdata\\go\\gene_ontology.1_2.obo");
			p.parseOboFile();
			GoaIndexer g = new GoaIndexer(p);
			g.parseFileByTaxon("D:\\OndexNew\\ondex-mini-0.0.2-20090929\\ondex-mini-0.0.2-SNAPSHOT\\data\\importdata\\goa\\gene_association.goa_arabidopsis", 3702, "UniProtKB/TrEMBL");
			
//			System.out.println("GOAs for Q8W4J3: ");
//			Iterator<Integer> it = g.getGoaIteratorForProtID("Q8W4J3");
//			while (it.hasNext()) {
//				int id = it.next();
//				double ic = g.getInformationContent(id);
//				double d = p.getShortestDepthofEntry(id);
//				System.out.println(id+"\t"+d+"\t"+ic);
//			}
//			System.out.println();
//			
//			it = g.getNonRedundantGoaForProtID("Q8W4J3").iterator();
//			
//			System.out.println("\nnon redundant:");
//			while (it.hasNext()) {
//				int id = it.next();
//				double ic = g.getInformationContent(id);
//				double d = p.getShortestDepthofEntry(id);
//				System.out.println(id+"\t"+d+"\t"+ic);
//			}
//			
//			System.out.println();
//			
//			int query = 5634;
//			double ic = g.getInformationContent(query);
//			System.out.println("IC for "+query+": " +ic);
//			
//			System.out.println();
			
			Iterator<Integer> go_it = g.getAllUsedGoTerms().iterator();
			double averageIC1 = 0;
			int[] histoGO = new int[15]; 
			for (int i = 0; i < histoGO.length; i++)
				histoGO[i] = 0;
			while(go_it.hasNext()){
				Integer goID = go_it.next();
				double ic = g.getInformationContent(goID);
				averageIC1 += ic;
				int i = (int) ic;
				histoGO[i]++;
			}
			
			System.out.println("IC via GO");
			int goNum = g.getAllUsedGoTerms().size();
			System.out.println("#Used GO terms: "+goNum);
			System.out.println("Average IC: "+averageIC1/goNum);
			for (int i = 0; i < histoGO.length; i++)
				System.out.println(i+"\t"+histoGO[i]);
			
			
			double averageIC = 0;
			int[] histo = new int[15]; 
			for (int i = 0; i < histo.length; i++)
				histo[i] = 0;
			Iterator<String> uni_it = g.getAllUsedProtIDs().iterator();
			while (uni_it.hasNext()) {
				String uni = uni_it.next();
				Iterator<Integer> it = g.getGoaIteratorForProtID(uni);
				double max = Double.NEGATIVE_INFINITY;
				while (it.hasNext()) {
					double d = g.getInformationContent(it.next());
					if (d > max) max = d;
				}
				averageIC += max;
				int i = (int) max;
				histo[i]++;
			}
				
//			BufferedWriter w = new BufferedWriter(new FileWriter(ondexdir+"\\stats.txt"));
			System.out.println("IC via Proteins");
			int protNum = g.getAllUsedProtIDs().size();
			System.out.println("#Proteins: "+protNum);
			System.out.println("Average IC: "+averageIC/protNum);
			for (int i = 0; i < histo.length; i++)
				System.out.println(i+"\t"+histo[i]);
//				w.write(i+"\t"+histo[i]+"\n");
//			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
