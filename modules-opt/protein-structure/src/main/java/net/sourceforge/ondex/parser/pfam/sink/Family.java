package net.sourceforge.ondex.parser.pfam.sink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * stores a pfam database entry
 * 
 * @author peschr
 * 
 */
public class Family {
	
	private String id;

	private String accession;

	// a list of referenced articles
	private List<Publication> publications = new ArrayList<Publication>();

	// a list of referenced database links
	private Map<String, DbLink> dblinks = new HashMap<String, DbLink>();

	private String description = "";

	public String getAccession() {
		return accession;
	}

	public void setAccession(String accession) {
		this.accession = accession;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Map<String, DbLink> getDblinks() {
		return dblinks;
	}

	public List<Publication> getPublications() {
		return publications;
	}

	/**
	 * adds a publication reference to the family entry
	 * 
	 * @param dbLink
	 */
	public void addPublicatio(Publication pub) {
		publications.add(pub);
	}

	/**
	 * adds a database reference to the family entry
	 * 
	 * @param dbLink
	 */
	public void addDbLink(DbLink dbLink) {
		this.dblinks.put(dbLink.getAccession(), dbLink);
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
}
