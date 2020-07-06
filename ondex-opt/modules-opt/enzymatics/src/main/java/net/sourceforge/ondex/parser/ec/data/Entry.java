package net.sourceforge.ondex.parser.ec.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to cache existing entries.
 * 
 * @author taubertj
 * 
 */
public class Entry {

	/**
	 * Defines the type of Entry
	 * 
	 * @author hindlem
	 * 
	 */
	public enum Type {
		EC, PROTEIN, DOMAIN;
	}

	private final Type type;

	private final String id;

	private String name = "";

	private String description = "";

	private String taxid = null;

	private final Map<String, String[]> accessions = new HashMap<String, String[]>();

	private final List<String> synonyms = new ArrayList<String>();

	private final List<Relation> relations = new ArrayList<Relation>();

	/**
	 * 
	 * @param type
	 *            the type of object represented by this class
	 * @param id
	 *            the unique id for this object
	 */
	public Entry(Type type, String id) {
		this.type = type;
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Adds new accessions to Entry
	 * 
	 * @param dataSource
	 *            db source for these accessions
	 * @param accessions
	 *            accessions for this DataSource
	 */
	public void addAccession(String dataSource, String[] accessions) {
		this.accessions.put(dataSource, accessions);
	}

	public Map<String, String[]> getAccessions() {
		return accessions;
	}

	public void addRelation(Relation relation) {
		relations.add(relation);
	}

	public List<Relation> getRelations() {
		return relations;
	}

	public List<String> getSynonyms() {
		return synonyms;
	}

	public Type getType() {
		return type;
	}

	public String getTaxid() {
		return taxid;
	}

	public void setTaxid(String taxid) {
		this.taxid = taxid;
	}

}
