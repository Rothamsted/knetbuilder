package net.sourceforge.ondex.algorithm.annotationquality;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Parses Gene Ontology and represents it database structure in the memory
 * in the most minimalistic way: just ids, namespaces and their structure are stored, but no
 * actual content.
 * @author Jochen Weile, B.Sc.
 */
public class GOTreeParser {

	//####FIELDS####
	
	/**
	 * The GO OBO file.
	 */
	private File oboFile;
	
	/**
	 * maps each GO id to its memory representation.
	 */
	private HashMap<Integer,GoTerm> id2go;

	//####CONSTRUCTOR####
	
	/**
	 * the constructor.
	 * @param the location of the obo file.
	 * @throws IOException if the obo file is not doesn't exist.
	 */
	public GOTreeParser(String oboLocation) throws IOException{
		oboFile = new File(oboLocation);
		if (!oboFile.exists())
			throw new IOException("File "+oboLocation+" does not exist!");
		id2go = new HashMap<Integer,GoTerm>();
	}

	//####METHODS####
	
	/**
	 * parses the obo file and thereby creates the memory represention of GO.
	 * @throws IOException if the file is unparseable.
	 */
	public void parseOboFile() throws IOException {
		GoTerm term = null;
		int pId, altId;
		String domain;
		
		BufferedReader r = new BufferedReader(new FileReader(oboFile));
		String line = null;
		while ((line = r.readLine()) != null) {
			line = line.trim();
			if (line.equals("[Term]")) {
				term = null;
			}
			else if (line.startsWith("id:")) {
				pId =  parseId(line.substring(7, 14));
				term = getGoTerm(pId);
			}
			else if (line.startsWith("alt_id:")) {
				altId = parseId(line.substring(11, 18));
				id2go.put(altId,term);
			}
			else if (line.startsWith("namespace: ")) {
				domain = line.substring(11).trim();
				if (domain.equals("cellular_component"))
					term.setDomain(GoTerm.DOMAIN_CELLULAR_COMPONENT);
				else if (domain.equals("molecular_function"))
					term.setDomain(GoTerm.DOMAIN_MOLECULAR_FUNCTION);
				else if (domain.equals("biological_process"))
					term.setDomain(GoTerm.DOMAIN_BIOLOGICAL_PROCESS);
				else
					throw new IOException("Wrong file format!");
			}
			else if (line.startsWith("is_a")) {
				pId =  parseId(line.substring(9, 16));
				addParent(term, pId);
			}
			else if (line.startsWith("relationship: part_of")) {
				pId =  parseId(line.substring(25, 32));
				addParent(term, pId);
			}
			else if (line.equals("[Typedef]"))
				break;
		}
		r.close();
	}
	
	/**
	 * internal method used by the parser. gets the 
	 * existing go term if it was parsed already. 
	 * otherwise it creates a new one.
	 * @return a corresponding GoTerm object
	 */
	private GoTerm getGoTerm(int id) {
		GoTerm term = id2go.get(id);
		if (term == null) {
			term = new GoTerm(id);
			id2go.put(id,term);
		}
		return term;
	}
	
	/**
	 * parses a go id.
	 * @param idString the string id
	 * @return the int id
	 * @throws IOException if it doesn't work
	 */
	private int parseId(String idString) throws IOException {
		int pId = -1;
		try {
			pId = Integer.parseInt(idString);
		} catch (NumberFormatException nfe) {
			throw new IOException("Wrong file format!");
		}
		return pId;
	}
	
	/**
	 * adds a parent to a go term.
	 * @param term the term
	 * @param pId the id of the parent.
	 */
	private void addParent(GoTerm term, int pId) {
		GoTerm pTerm = id2go.get(pId);
		if (pTerm == null) {
			pTerm = new GoTerm(pId);
			id2go.put(pId, pTerm);
		}
		term.getParents().addElement(pTerm);
		
	}
		
	/**
	 * returns the number of entries.
	 * @return
	 */
	public int getNumEntries() {
		return id2go.size();
	}
	
	/**
	 * returns the corresponding go term for the given go id.
	 * @param id the query id.
	 * @return the go term.
	 */
	public GoTerm getEntry(int id){
		return id2go.get(id);
	}
	
	/**
	 * returns the shortest depth of the given go query id.
	 * @param id the query go id.
	 * @return the shortest depth of the term.
	 */
	public int getShortestDepthofEntry(int id) {
		GoTerm term = id2go.get(id);
		int d = term.getShortestDepth(id2go);
		return d;
	}
	
	/**
	 * returns the distance in the go database structure between the two given terms.
	 * the search is teminated when it exceeds the given search depth cutoff.
	 * @param id1 the id of the first term.
	 * @param id2 the id of the second term.
	 * @param cutoff the search depth cutoff.
	 * @return the distance. or -1 if there is no path.
	 */
	public int getDistanceBetweenTerms(int id1, int id2, int cutoff) {
		GoTerm term1 = id2go.get(id1);
		GoTerm term2 = id2go.get(id2);
		
		double dist_fwd = trace(term1, term2, 0.0d, cutoff);
		double dist_bck = trace(term2, term1, 0.0d, cutoff);
		
		double dist = Math.min(dist_fwd, dist_bck);
		if (dist == Double.POSITIVE_INFINITY)
			return -1;
		else
			return (int) dist;
	}
	
	/**
	 * gets the directed distance from first query term to the second query term.
	 * the search is terminated when it exceeds the given search depth cutoff.
	 * @param id1 the id of the first term.
	 * @param id2 the id of the second term.
	 * @param cutoff the search depth cutoff.
	 * @return the distance. or -1 if there is no path.
	 */
	public int getDirectedDistanceBetweenTerms(int id1, int id2, int cutoff) {
		GoTerm term1 = id2go.get(id1);
		GoTerm term2 = id2go.get(id2);
		
		if (term1 == null || term2 == null)
			return -1;
		
		double dist = trace(term1, term2, 0.0d, cutoff);
		
		if (dist == Double.POSITIVE_INFINITY)
			return -1;
		else
			return (int) dist;
	}
	
	/**
	 * returns the namespace of the term.
	 * @param id the id of the query term.
	 * @return the namespace, represented by an integer. (see constants of GoTerm)
	 */
	public int getNamespaceOfTerm(int id) {
		GoTerm term = id2go.get(id);
		if (term == null)
			System.err.println("ERROR: term #"+id+" does not exist!");
		return term.getDomain();
	}
	
	/**
	 * a recursion method for the distance search.
	 * @param term the iterated term of the current level
	 * @param search_term the constant query term.
	 * @param d the current depth.
	 * @param cutoff the search depth cutoff.
	 * @return the distance.
	 */
	private double trace(GoTerm term, GoTerm search_term, double d, int cutoff) {
		if (term.equals(search_term))
			return d;
		
		if (d == cutoff || term.getParents().size() == 0)
			return Double.POSITIVE_INFINITY;
		
		double min = Double.POSITIVE_INFINITY;
		for (GoTerm p : term.getParents()) {
			double tmp = trace(p, search_term, d + 1.0d, cutoff);
			if (tmp < min) min = tmp;
		}
		return min;
	}
	
	/**
	 * for testing purposes: a standalone query shell.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			GOTreeParser p = new GOTreeParser("D:\\work\\integration_data\\gene_ontology_edit.obo");
			p.parseOboFile();
			System.out.println(p.getNumEntries()+" entries parsed");
			
			BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
			String cmd;
			while (true) {
				System.out.print("> ");
				cmd = r.readLine();
				if (cmd.equals("exit") || cmd.equals("quit"))
					System.exit(0);
				else if (cmd.startsWith("depth")) {
					try {
						String[] parts = cmd.split(" ");
						int id = Integer.parseInt(parts[1]);
						int d = p.getShortestDepthofEntry(id);
						System.out.println("Depth of "+parts[1]+" is "+d);
					} catch (Exception e) {
						System.out.println("usage: \ndepth GOTERM");
					}
				}
				else if (cmd.startsWith("distance")) {
					try {
						String[] parts = cmd.split(" ");
						int id1 = Integer.parseInt(parts[1]);
						int id2 = Integer.parseInt(parts[2]);
						int d = p.getDistanceBetweenTerms(id1,id2, Integer.MAX_VALUE);
						System.out.println("Distance between "+parts[1]+" and "+parts[2]+" is "+d);
					} catch (Exception e) {
						System.out.println("usage: \ndistance GOTERM GOTERM");
					}
				}
				else if (cmd.startsWith("namespace")) {
					try {
						String[] parts = cmd.split(" ");
						int id1 = Integer.parseInt(parts[1]);
						int d = p.getNamespaceOfTerm(id1);
						String ns = null;
						if (d == GoTerm.DOMAIN_BIOLOGICAL_PROCESS)
							ns = "biological process";
						else if (d == GoTerm.DOMAIN_CELLULAR_COMPONENT)
							ns = "cellular component";
						else if (d == GoTerm.DOMAIN_MOLECULAR_FUNCTION)
							ns = "molecular function";
						System.out.println("Namespace of "+parts[1]+" is "+ns);
					} catch (Exception e) {
						System.out.println("namespace: \ndistance GOTERM");
					}
				}
				else
					System.out.println("unknown command!");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
