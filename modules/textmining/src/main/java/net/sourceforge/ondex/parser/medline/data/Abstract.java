package net.sourceforge.ondex.parser.medline.data;

import java.util.HashSet;

/**
 * This class holds all information which could be extracted from a
 * MEDLINE XML file for one single MEDLINE abstract.
 * 
 * 
 * @author keywan
 * 
 */
public class Abstract {
	
	private int id = -1;
	private String title;
	private String body;
	private String doi;
	private boolean delete = false;
	private int year;
	private String authors;
	private String journal;
	
	private HashSet<String> chemicals = new HashSet<String>();
	private HashSet<String> meshs = new HashSet<String>();
	
	/**
	 * Sets the IDs for a MEDLINE abstract (PubMed ID).
	 * 
	 * @param i - the Pubmed ID (e.g. 11345678)
	 */
	public void setID(int i) {
		this.id = i;
	}
	
	/**
	 * Returns the PubMed ID of a MEDLINE Abstract.
	 * 
	 * @return integer ID of the abstract
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Sets the delete status for the document.
	 * if true, an abstract will be deleted rather than added to the back end
	 * 
	 * @param status
	 */
	public void setDelete(boolean bo) {
		this.delete=bo;
	}
	
	/**
	 * Gives back the delete status for the document.
	 * 
	 * @param status
	 */
	public boolean isDeleted() {
		return this.delete;
	}
	
	/**
	 * Returns the text body of a MEDLINE abstract
	 * 
	 * @return
	 */
	public String getBody() {
		return body;
	}
		
	/**
	 * Sets the abstract's body.
	 * 
	 * @param body - the abstract of a MEDLINE publication
	 */
	public void setBody(String body) {
		this.body = body;
	}
	
	/**
	 * Returns the title (header) of a MEDLINE abstract.
	 * 
	 * @return the header as a String
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Sets the title (header) of a MEDLINE abstract.
	 * 
	 * @param title - the header as a String
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Returns both the header and the body of a MEDLINE abstract as one String.
	 * 
	 * @return one String containing header and abstract.
	 */
	public String getConcatBody() {
		return title+" "+body;
	}
	
	
	/**
	 * 
	 * Returns all MEDLINE annotated chemicals for an abstract.
	 * 
	 * @return an ArrayList of Chemical objects
	 */
	public HashSet<String> getChemicals() {
		return chemicals;
	}
	
	public HashSet<String> getMeSHs() {
		return meshs;
	}
	
	/**
	 * Adds one Chemical object to a MEDLINE abstract.
	 * 
	 * @param chem - the Chemical object
	 */
	public void addChemical(String chem) {
		this.chemicals.add(chem);
	}
	
	public void addMeSH(String mesh) {
		this.meshs.add(mesh);
	}
	
	public int hashCode() {
		return id;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof Abstract) {
			return ((Abstract)obj).getId() == id;
		}
		return false;
	}
	
	public void finalize() {
		chemicals = null;
		meshs = null;
		doi = null;
		title = null;
		body = null;
	}

	public String getDoi() {
		return doi;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getAuthors() {
		return authors;
	}

	public void setAuthors(String authors) {
		this.authors = authors;
	}

	public String getJournal() {
		return journal;
	}

	public void setJournal(String journal) {
		this.journal = journal;
	}
}
