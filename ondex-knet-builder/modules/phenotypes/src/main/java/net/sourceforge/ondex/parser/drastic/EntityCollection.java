package net.sourceforge.ondex.parser.drastic;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Collection of all Concepts (Genes and Treatments)
 * extracted from the DraEntities
 * 
 * @author winnenbr
 *
 */
public class EntityCollection {
	
	int idNumber = 0;
	
	ArrayList<Concept> concepts = new ArrayList<Concept>();
	ArrayList<Relation> relations = new ArrayList<Relation>();
	
	HashMap<String,Integer> uniqueGenes = new HashMap<String,Integer>();
	HashMap<String,Integer> uniqueTreatments = new HashMap<String,Integer>();
	HashMap<String,Integer> uniqueRelations = new HashMap<String,Integer>();
	
	/**
	 * Checks whether there is already one concept with the 
	 * ID given as parameter, and will link to that object in this case
	 * or create a new index for a new Concept instance.
	 * 
	 * @param s key of the DraEntity
	 * @param g a new Concept instance
	 * 
	 * @return the index for this Concept in the concepts ArrayList
	 */
	private int assignGeneID(String s, Concept g) {
	
		if (uniqueGenes.containsKey(s)) {
			
			int index = uniqueGenes.get(s);
			g = concepts.get(index);
			return index;
		
		}else {
			
			int index = idNumber++;
			uniqueGenes.put(s, index);
			concepts.add(g);
			return index;
		
		}
	}
	
	/**
	 * Checks whether there is already one concept with the 
	 * ID given as parameter, and will link to that object in this case
	 * or create a new index for a new Concept instance.
	 * 
	 * @param s key of the DraEntity
	 * @param t a new Concept instance
	 * 
	 * @return the index for this Concept in the concepts ArrayList
	 */
	private int assignTreatmentID(String s, Concept t) {
		
		if (uniqueTreatments.containsKey(s)) {
			
			int index = uniqueTreatments.get(s);
			t = concepts.get(index);
			return index;
			
		}else {
			
			int index = idNumber++;
			uniqueTreatments.put(s, index);
			concepts.add(t);
			return index;
			
		}
	}
	
	/**
	 * Creates from one DraEntity object given as parameter
	 * two Concepts (Gene and Treatment). The DraEntity's keys will
	 * be looked up and new Concepts will only be created if there
	 * is not already one with the same key in the EntityCollection.
	 * The already existent Concept would then be extended.
	 * 
	 * @param d the DraEntity
	 */
	private void addToCollection(DraEntity d) {
		
		Concept treatment = new Concept();
		Concept gene = new Concept();
		
		int id_g = this.assignGeneID(d.getKey(), gene);
		int id_t = this.assignTreatmentID(d.getTreatment(), treatment);
		
		gene.setID(id_g);
		gene.addAccessions(d.getAccs());
		gene.addAnnotation(d.annotation);
		gene.setKey(d.keyID);
		gene.setPrefName(d.prefName);
		gene.addNames(d.getGeneNames());
		gene.setConceptClass("Gene");
		gene.setTaxID(d.taxID);
		
		treatment.setID(id_t);
		treatment.setKey(d.getTreatment());
		treatment.setPrefName(d.getTreatment());
		treatment.addNames(d.getTreatments());
		treatment.addAnnotation(d.getTreatmentDescription());
		treatment.setConceptClass("Treatment");
		
		relations.add(new Relation(id_g, id_t, d.getRegulation()));
		
	}
	
	
	/**
	 * Checks if DraEntity object has a key. 
	 * Possible reason why an object has no key is
	 * the lack of both a valid accession number and
	 * a gene name.
	 * 
	 * @param d the DraEntity object to be checked.
	 * @return true if instance has a key, false if not.
	 */
	private boolean checkKey(DraEntity d) {
		return d.hasKey();
	}
	
	/**
	 * Return all concepts of this collection
	 * 
	 * @return all concepts
	 */
	public ArrayList<Concept> getConcepts() {
		return concepts;
	}
	
	/**
	 * Return all relations of this collection
	 * 
	 * @return
	 */
	public ArrayList<Relation> getRelations() {
		return relations;
	}
	
	/**
	 * Extract Concepts from DraEntity instance
	 * and add to collection.
	 * 
	 * @param d the DraEntity entity
	 */
	public void add(DraEntity d) {
		
		if (this.checkKey(d))
		this.addToCollection(d);
		
	}
	
}
