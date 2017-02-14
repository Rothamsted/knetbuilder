package net.sourceforge.ondex.parser.transfac;

import java.util.HashSet;
import java.util.Iterator;

import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.parser.transfac.sink.Publication;



/**
 * Superclass for Factor, Gene and Matrix.
 * 
 * @author taubertj
 *
 */
public abstract class AbstractTFObject {

	private static boolean DEBUG = false;
	
	// unique accession
	private final String accession; // AC
	
	// human readable name of entry
	private String name;
	
	// maybe long description
	private String description; // CC
	
	// species string
	private String species; // OS
	
	// alternative accessions within TF
	private HashSet<String> accessionAlternatives = new HashSet<String>(); //AS
	
	// accessions from other databases
	private HashSet<DBlink> databaseLinks = new HashSet<DBlink>();
	
	// list of other possible names
	private HashSet<String> synonyms = new HashSet<String>();
	
	// associated publications
	private HashSet<Publication> publications = new HashSet<Publication>();
	
	/**
	 * Constructor for a given unique TF ID.
	 * 
	 * @param accession - unique TF ID
	 */
	public AbstractTFObject(String accession) {
		this.accession = accession;
	}
	
	/**
	 * Returns unique TF ID.
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
	public void setName(String name) {
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
	public void setDescription(String description) {
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
	 * Sets species for this Gene.
	 * 
	 * @param species - String
	 */
	public void setSpecies(String species) {
		this.species = species;
	}
	
	/**
	 * Returns associated species.
	 * 
	 * @return String
	 */
	public String getSpecies() {
		return species;
	}
	
	/**
	 * Adds a alternative accession to current list.
	 * 
	 * @param acc - String
	 */
	public void addAccessionAlternative(String acc) {
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
	 * Adds a DBlink to current list.
	 * 
	 * @param dbentry - DBlink
	 */
	public void addDatabaseLink(DBlink dbentry) {
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
	 * Adds a synonym to current list.
	 * 
	 * @param synonym - String
	 */
	public void addSynonym(String synonym) {
		synonyms.add(synonym);
	}

	public HashSet<String> getSynonyms() {
		return synonyms;
	}
	
	/**
	 * Adds a publication to current list.
	 * 
	 * @param publication - Publication
	 */
	public void addPublication(Publication publication) {
		if (DEBUG && publications.contains(publication)) {
			Parser.propagateEventOccurred(new DataFileErrorEvent(
					"Publication id already exists: " 
					+publication.getPmid()+" in "+getAccession(), "addPublication(Publication publication)"));
		}
		publications.add(publication);
	}

	/**
	 * get publications on this Object
	 * @return publications
	 */
	public HashSet<Publication> getPublications() {
		return publications;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o instanceof AbstractTFObject) {
			return ((AbstractTFObject) o).accession.equals(this.accession);
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
