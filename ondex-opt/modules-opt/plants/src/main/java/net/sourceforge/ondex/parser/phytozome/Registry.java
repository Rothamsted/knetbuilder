package net.sourceforge.ondex.parser.phytozome;

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
	
	//registers all parsed CDS IDs to Ondex IDs
	private HashMap<String, Integer> cdsRegistry;
	
	//registers all parsed chromosome IDs to Ondex IDs
	private HashMap<String, Integer> chromosomeRegistry;
	
	public Registry(){
		proteinRegistry = new HashMap<String, Integer>();
		geneRegistry = new HashMap<String, Integer>();
		cdsRegistry = new HashMap<String, Integer>();
		chromosomeRegistry = new HashMap<String, Integer>();
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
	 * gets internal ID for a CDS ID
	 * @param cdsId cdsId
	 * @return internal Ondex ID
	 */
	public Integer getCDS(String cdsId){
		return cdsRegistry.get(cdsId);
	}
	
	/**
	 * checks whether CDS is registered
	 * 
	 * @param cdsId cdsId
	 * @return true if CDS is registered
	 */
	public boolean containsCDS(String cdsId){
		return cdsRegistry.containsKey(cdsId);
	}
	
	/**
	 * registers a CDSId along with its internal ID
	 * @param cdsId cdsId
	 * @param internalId internal Ondex ID
	 */
	public void addCDS(String cdsId, Integer internalId){
		cdsRegistry.put(cdsId, internalId);
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
	public boolean containsChromosome(String chromId){
		return chromosomeRegistry.containsKey(chromId);
	}
	
	/**
	 * registers a chromosomeId along with its internal ID
	 * @param chromosomeId chromosomeId
	 * @param internalId internal Ondex ID
	 */
	public void addChromosome(String chromosomeId, Integer internalId){
		chromosomeRegistry.put(chromosomeId, internalId);
	}
	
	/**
	 * gets internal ID for a chromosome ID
	 * @param chromosomeId chromosomeId
	 * @return internal Ondex ID
	 */
	public Integer getChromosome(String chromosomeId){
		return chromosomeRegistry.get(chromosomeId);
	}

}
