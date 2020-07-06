package net.sourceforge.ondex.parser.transpath;

import java.util.HashSet;
import java.util.Iterator;

/**
 * Superclass for Gene, Molecule and Reaction.
 * 
 * @author taubertj
 *
 */
abstract class AbstractTPObject {
	
	// unique accession
	private final String accession; // AC
	
	// human readable name of entry
	private String name;
	
	// maybe long description
	private String description; // CC
		
	// alternative accessions within TP
	private HashSet<String> accessionAlternatives = new HashSet<String>(); //AS
	
	// accessions from other databases
	private HashSet<DBlink> databaseLinks = new HashSet<DBlink>();
	
	// list of other possible names
	private HashSet<String> synonyms = new HashSet<String>();
	
	// associated publications
	private HashSet<Publication> publications = new HashSet<Publication>();

	/**
	 * Constructor for a given unique TP ID.
	 * 
	 * @param accession - unique TP ID
	 */
	protected AbstractTPObject(String accession) {
		this.accession = accession;
	}
	
	/**
	 * Returns unique TP ID.
	 * 
	 * @return String
	 */
	public String getAccession() {
		return accession;
	}
	
	/**
	 * Sets primary name.
	 * 
	 * @param name - String
	 */
	protected void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns primary name.
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets maybe long description.
	 * 
	 * @param description - String
	 */
	protected void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Returns maybe long description
	 * 
	 * @return String
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Adds a DBlink to current list.
	 * 
	 * @param dbentry - DBlink
	 */
	protected void addDatabaseLink(DBlink dbentry) {
		databaseLinks.add(dbentry);
	}
	
	/**
	 * Returns Iterator over contained links.
	 * 
	 * @return Iterator<DBlink>
	 */
	public Iterator<DBlink> getDatabaseLinks() {
		return databaseLinks.iterator();
	}
	
	/**
	 * Adds a alternative accession to current list.
	 * 
	 * @param acc - String
	 */
	protected void addAccessionAlternative(String acc) {
		this.accessionAlternatives.add(acc);
	}
	
	/**
	 * Returns Iterator over contained alternative accessions.
	 * 
	 * @return Iterator<String>
	 */
	public Iterator<String> getAccessionAlternatives() {
		return accessionAlternatives.iterator();
	}
		
	/**
	 * Adds a synonym to current list.
	 * 
	 * @param synonym - String
	 */
	protected void addSynonym(String synonym) {
		synonyms.add(synonym);
	}

	/**
	 * Returns Iterator over contained synonyms.
	 * 
	 * @return Iterator<String>
	 */
	public Iterator<String> getSynonyms() {
		return synonyms.iterator();
	}
	
	/**
	 * Adds a publication to current list.
	 * 
	 * @param publication - Publication
	 */
	protected void addPublication(Publication publication) {
		publications.add(publication);
	}

	/**
	 * Returns Iterator over contained publications.
	 * 
	 * @return Iterator<Publication>
	 */
	public Iterator<Publication> getPublications() {
		return publications.iterator();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o instanceof AbstractTPObject) {
			return ((AbstractTPObject) o).accession.equals(this.accession);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return accession.hashCode();
	}

	@Override
	public String toString() {
		return accession;
	}
	
}
