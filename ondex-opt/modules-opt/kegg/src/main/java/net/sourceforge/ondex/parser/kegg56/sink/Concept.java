/*
 * Created on 25-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg56.sink;

import java.util.HashSet;
import java.util.Set;

/**
 * Prototype of a concept
 * 
 * @author taubertj
 */
public class Concept implements Comparable<Concept> {

	private Set<ConceptAcc> conceptAccs = new HashSet<ConceptAcc>(4);

	private Set<ConceptName> conceptNames = new HashSet<ConceptName>(4);

	private String description;

	private String element_of; // required

	private float graphical_x;

	private float graphical_y;

	private String id; // required

	private boolean isSelfContext = false;

	private Set<String> keggConceptIdContext = new HashSet<String>(4);

	private String of_type_fk; // required

	private String taxid;

	private String url;

	private String mol;

	private Set<String> structures;

	/**
	 * Create a new concept for KEGG internal use.
	 * 
	 * @param id
	 *            concept parser id
	 * @param element_of
	 *            data source id
	 * @param of_type_fk
	 *            concept class id
	 */
	public Concept(String id, String element_of, String of_type_fk) {
		if (id == null || id.length() == 0)
			throw new NullPointerException("ID is null");
		if (element_of == null || element_of.length() == 0)
			throw new NullPointerException("ElementOf is null");
		if (of_type_fk == null || of_type_fk.length() == 0)
			throw new NullPointerException("OfTypeFK is null");
		this.id = id.toUpperCase();
		this.element_of = element_of.intern();
		this.of_type_fk = of_type_fk.intern();
	}

	/**
	 * Adds a concept id to the list of context
	 * 
	 * @param ondexConceptId
	 */
	public void addContext(String keggConceptId) {
		keggConceptIdContext.add(keggConceptId);
	}

	/**
	 * Clones a KEGG internal Concept for a new id, element_of and of_type_fk.
	 * 
	 * @param new_id
	 *            String
	 * @param new_element_of
	 *            String
	 * @param new_of_type_fk
	 *            String
	 * @return Concept
	 */
	public Concept clone(String new_id, String new_element_of,
			String new_of_type_fk) {

		// create new concept
		Concept newConcept = new Concept(new_id, new_element_of, new_of_type_fk);
		if (this.description != null)
			newConcept.setDescription(this.getDescription());
		newConcept.setGraphical_x(this.getGraphical_x());
		newConcept.setGraphical_y(this.getGraphical_y());
		if (this.getTaxid() != null)
			newConcept.setTaxid(this.getTaxid());
		if (this.url != null)
			newConcept.setUrl(this.getUrl());

		// copy concept accessions
		if (this.getConceptAccs().size() > 0) {
			for (ConceptAcc ca : conceptAccs) {
				ConceptAcc newCA = new ConceptAcc(ca.getConcept_accession(), ca
						.getElement_of());
				newCA.setAmbiguous(ca.isAmbiguous());
				newConcept.getConceptAccs().add(newCA);
			}
		}

		// copy concept names
		if (this.getConceptNames().size() > 0) {
			for (ConceptName cn : getConceptNames()) {
				ConceptName newCN = new ConceptName(cn.getName(), cn
						.isPreferred());
				newConcept.getConceptNames().add(newCN);
			}
		}
		return newConcept;
	}

	@Override
	public int compareTo(Concept c) {
		return this.id.compareTo(c.getId());
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Concept)
			return this.id.equals(((Concept) o).getId());
		return false;
	}

	/**
	 * Returns set of Concept Accession.
	 * 
	 * @return Set<ConceptAcc>
	 */
	public Set<ConceptAcc> getConceptAccs() {
		return conceptAccs;
	}

	/**
	 * Returns set of Concept Names.
	 * 
	 * @return Set<ConceptName>
	 */
	public Set<ConceptName> getConceptNames() {
		return conceptNames;
	}

	/**
	 * Returns set of associated context
	 * 
	 * @return Set<String>
	 */
	public Set<String> getContext() {
		return keggConceptIdContext;
	}

	/**
	 * Returns description.
	 * 
	 * @return String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns element_of.
	 * 
	 * @return String
	 */
	public String getElement_of() {
		return element_of;
	}

	/**
	 * Returns graphical_x.
	 * 
	 * @return float
	 */
	public float getGraphical_x() {
		return graphical_x;
	}

	/**
	 * Returns graphical_y.
	 * 
	 * @return float
	 */
	public float getGraphical_y() {
		return graphical_y;
	}

	/**
	 * Returns Concept ID.
	 * 
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns of_type_fk.
	 * 
	 * @return String
	 */
	public String getOf_type_fk() {
		return of_type_fk;
	}

	/**
	 * Returns taxid.
	 * 
	 * @return String
	 */
	public String getTaxid() {
		return taxid;
	}

	/**
	 * Returns mol string.
	 * 
	 * @return String
	 */
	public String getMol() {
		return mol;
	}

	/**
	 * Returns the set of PDB structures
	 * 
	 * @return Set<String>
	 */
	public Set<String> getStructures() {
		return structures;
	}

	/**
	 * Returns URL.
	 * 
	 * @return String
	 */
	public String getUrl() {
		return url;
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	/**
	 * If this concept is context of itself.
	 * 
	 * @return the isSelfContext
	 */
	public boolean isSelfContext() {
		return isSelfContext;
	}

	/**
	 * Sets description.
	 * 
	 * @param description
	 *            String
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets graphical_x.
	 * 
	 * @param graphical_x
	 *            - float
	 */
	public void setGraphical_x(float graphical_x) {
		this.graphical_x = graphical_x;
	}

	/**
	 * Set graphical_y.
	 * 
	 * @param graphical_y
	 *            - float
	 */
	public void setGraphical_y(float graphical_y) {
		this.graphical_y = graphical_y;
	}

	/**
	 * Changes KEGG parser id.
	 * 
	 * @param id
	 *            String
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param isSelfContext
	 *            the isSelfContext to set
	 */
	public void setSelfContext(boolean isSelfContext) {
		this.isSelfContext = isSelfContext;
	}

	/**
	 * Sets taxid.
	 * 
	 * @param taxid
	 *            - String
	 */
	public void setTaxid(String taxid) {
		this.taxid = taxid.intern();
	}

	/**
	 * Sets mol String.
	 * 
	 * @param mol
	 *            String
	 */
	public void setMol(String mol) {
		this.mol = mol;
	}

	/**
	 * Sets the set of PDB structures.
	 * 
	 * @param s
	 *            Set<String>
	 */
	public void setStructures(Set<String> s) {
		this.structures = s;
	}

	/**
	 * Sets URL.
	 * 
	 * @param url
	 *            - String
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		String s = "Concept\n" + "Id: " + id + "\n" + "Of_type_fk: "
				+ of_type_fk + "\n" + "Element_of: " + element_of;
		return s;
	}
}
