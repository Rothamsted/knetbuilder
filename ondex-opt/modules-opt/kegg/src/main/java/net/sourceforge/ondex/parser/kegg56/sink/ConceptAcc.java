/*
 * Created on 26-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg56.sink;

/**
 * Prototype for a concept accession
 * 
 * @author taubertj
 */
public class ConceptAcc {

	private boolean ambiguous = false; // default

	private String concept_accession; // required

	private String element_of; // required

	private String s = ""; // internal

	/**
	 * Create a new concept accession for KEGG internal use.
	 * 
	 * @param concept_accession
	 *            actual accession
	 * @param element_of
	 *            data source of accession
	 */
	public ConceptAcc(String concept_accession, String element_of) {
		if (concept_accession == null)
			throw new NullPointerException("concept_accession is null");
		if (element_of == null)
			throw new NullPointerException("element_of is null");
		this.concept_accession = concept_accession;
		this.element_of = element_of.intern();
		this.s = this.concept_accession + this.element_of;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ConceptAcc) {
			ConceptAcc acc = (ConceptAcc) o;
			return this.concept_accession.equals(acc.getConcept_accession())
					&& this.element_of.equals(acc.getElement_of());
		}
		return false;
	}

	/**
	 * Returns actual accession
	 * 
	 * @return String
	 */
	public String getConcept_accession() {
		return concept_accession.replace("_", ".");
	}

	/**
	 * Returns data source of accession
	 * 
	 * @return String
	 */
	public String getElement_of() {
		return element_of;
	}

	@Override
	public int hashCode() {
		return s.hashCode();
	}

	/**
	 * Returns ambiguity of accession
	 * 
	 * @return boolean
	 */
	public boolean isAmbiguous() {
		return ambiguous;
	}

	/**
	 * Sets ambiguity of accession
	 * 
	 * @param ambiguous
	 */
	public void setAmbiguous(boolean ambiguous) {
		this.ambiguous = ambiguous;
	}

	/**
	 * Changes the data source for this accession.
	 * 
	 * @param element_of
	 *            String
	 */
	public void setElement_of(String element_of) {
		this.element_of = element_of;
	}

	@Override
	public String toString() {
		return "ConceptAcc: " + concept_accession + " (" + element_of + ")";
	}
}
