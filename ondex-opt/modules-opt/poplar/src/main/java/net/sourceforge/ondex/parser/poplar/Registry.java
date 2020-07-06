package net.sourceforge.ondex.parser.poplar;

import java.util.HashMap;

/**
 * This class keeps track off all gene, protein and
 * chromosome ids parsed into Ondex.
 * 
 * @author Keywan
 *
 */
public class Registry {
	
	//registers all parsed protein IDs to Ondex IDs
	private HashMap<String, Integer> proteinRegistry;
	
	//registers all parsed gene IDs to Ondex IDs
	private HashMap<String, Integer> geneRegistry;
	
	//registers all parsed chromosome IDs to Ondex IDs
	private HashMap<Integer, Integer> chromosomeRegistry;
	
	public Registry(){
		proteinRegistry = new HashMap<String, Integer>();
		geneRegistry = new HashMap<String, Integer>();
		chromosomeRegistry = new HashMap<Integer, Integer>();
	}
	
	/**
	 * checks whether protein is registered
	 * 
	 * @param proteinId ProteinId
	 * @return true if protein is registered
	 */
	public boolean containsProtein(String proteinId){
		return proteinRegistry.containsKey(proteinId);
	}
	
	/**
	 * registers a proteinId along with its internal ID
	 * @param proteinId proteinId
	 * @param internalId internal Ondex ID
	 */
	public void addProtein(String proteinId, Integer internalId){
		proteinRegistry.put(proteinId, internalId);
	}
	
	/**
	 * gets internal ID for a protein ID
	 * @param proteinId proteinId
	 * @return internal Ondex ID
	 */
	public Integer getProtein(String proteinId){
		return proteinRegistry.get(proteinId);
	}
	
	/**
	 * checks whether gene is registered
	 * 
	 * @param geneId geneId
	 * @return true if gene is registered
	 */
	public boolean containsGene(String geneId){
		return geneRegistry.containsKey(geneId);
	}
	
	/**
	 * registers a geneId along with its internal ID
	 * @param geneId geneId
	 * @param internalId internal Ondex ID
	 */
	public void addGene(String geneId, Integer internalId){
		geneRegistry.put(geneId, internalId);
	}
	
	/**
	 * gets internal ID for a gene ID
	 * @param geneId geneId
	 * @return internal Ondex ID
	 */
	public Integer getGene(String geneId){
		return geneRegistry.get(geneId);
	}
	
	/**
	 * checks whether chromosome is registered
	 * 
	 * @param chromId chromId
	 * @return true if chromosome is registered
	 */
	public boolean containsChromosome(Integer chromId){
		return chromosomeRegistry.containsKey(chromId);
	}
	
	/**
	 * registers a chromosomeId along with its internal ID
	 * @param chromosomeId chromosomeId
	 * @param internalId internal Ondex ID
	 */
	public void addChromosome(Integer chromosomeId, Integer internalId){
		chromosomeRegistry.put(chromosomeId, internalId);
	}
	
	/**
	 * gets internal ID for a chromosome ID
	 * @param chromosomeId chromosomeId
	 * @return internal Ondex ID
	 */
	public Integer getChromosome(Integer chromosomeId){
		return chromosomeRegistry.get(chromosomeId);
	}

}
