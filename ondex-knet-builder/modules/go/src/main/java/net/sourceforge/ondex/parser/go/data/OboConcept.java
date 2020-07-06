package net.sourceforge.ondex.parser.go.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Object to contain extract data from OBO file.
 * 
 * @author taubertj
 * 
 */
public class OboConcept {

	// few more classe see GO/OBO documentation
	public static String broadSynonym = "broad_synonym";

	// Defines that this synonym is exact, see OBO/GO documentation
	public static String exactSynonym = "exact_synonym";

	// few more classe see GO/OBO documentation
	public static String narrowSynonym = "narrow_synonym";

	// few more classe see GO/OBO documentation
	public static String relatedSynonym = "related_synonym";

	// alternative ids will lead to duplication of concept
	private List<String> alt_ids = new ArrayList<String>();

	// becomes annotation tag
	private String definition = "";

	// not yet used, concept accessions
	private String[] definitionRefs;

	// pid of concept
	private String id = "";

	// preferred name
	private String name = "";

	// biological process, function, location
	private String namespace = "";

	// whether Obo concept is obsolete
	private boolean obsolete = false;

	// concept accessions from xref tag
	private List<String> refs = new ArrayList<String>();

	// list of relations from relationship tag
	private List<String[]> relations = new ArrayList<String[]>();

	// term was replace by another
	private String replacement = null;

	// more concept names
	private HashMap<String, HashSet<String>> synonyms = new HashMap<String, HashSet<String>>();

	/**
	 * Adds a alternative id to this OBO concept.
	 * 
	 * @param alt_id
	 *            String
	 */
	public void addAlt_id(String alt_id) {
		this.alt_ids.add(alt_id);
	}

	/**
	 * Adds a reference, e.g. EC:2.4.1.- to this OBO concept.
	 * 
	 * @param ref
	 *            String
	 */
	public void addRef(String ref) {
		this.refs.add(ref);
	}

	/**
	 * Adds a String pair in the form of ("part_of", "GO:0006310") to this OBO
	 * concept.
	 * 
	 * @param relation
	 *            String[]
	 */
	public void addRelation(String[] relation) {
		this.relations.add(relation);
	}

	/**
	 * Adds another synonym for this OBO concepts. Checks for equivalence with
	 * preferred name.
	 * 
	 * @param type
	 *            String
	 * @param synonym
	 *            String
	 */
	public void addSynonym(String type, String synonym) {

		HashSet<String> existingSet = this.synonyms.get(type);
		if (existingSet == null) {
			HashSet<String> set = new HashSet<String>();
			set.add(synonym);
			this.synonyms.put(type, set);
		} else {
			existingSet.add(synonym);
		}
	}

	/**
	 * Returns list of alternative ids.
	 * 
	 * @return List<String>
	 */
	public List<String> getAlt_ids() {
		return alt_ids;
	}

	/**
	 * Returns parsed definition line.
	 * 
	 * @return String
	 */
	public String getDefinition() {
		return definition;
	}

	/**
	 * Returns references extracted from definition line.
	 * 
	 * @return String[]
	 */
	public String[] getDefinitionRefs() {
		return definitionRefs;
	}

	/**
	 * Returns the id of this OBO concept.
	 * 
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns preferred name of this OBO concept.
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns GO category, e.g. biological function which becomes concept
	 * class.
	 * 
	 * @return String
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * Returns list of references from xref tag to become concept accessions.
	 * 
	 * @return List<String>
	 */
	public List<String> getRefs() {
		return refs;
	}

	/**
	 * Returns a list of pairs consisting of relation type and to concept.
	 * 
	 * @return List<String[]>
	 */
	public List<String[]> getRelations() {
		return relations;
	}

	/**
	 * Returns replacement term.
	 * 
	 * @return String
	 */
	public String getReplacement() {
		return replacement;
	}

	/**
	 * Returns a map of synonym type to synonym for concept names.
	 * 
	 * @return HashMap<String, HashSet<String>>
	 */
	public HashMap<String, HashSet<String>> getSynonyms() {
		return synonyms;
	}

	/**
	 * Returns only synonyms of a given type, e.g. OboConcept.exactSynonym.
	 * 
	 * @param type
	 *            String
	 * @return HashSet<String>
	 */
	public HashSet<String> getSynonymsOfType(String type) {
		return synonyms.get(type);
	}

	/**
	 * Returns whether or not this OBO concept is obsolete.
	 * 
	 * @return boolean
	 */
	public boolean isObsolete() {
		return obsolete;
	}

	/**
	 * Sets def line for this OBO concept.
	 * 
	 * @param definition
	 *            String
	 */
	public void setDefinition(String definition) {
		this.definition = definition;
	}

	/**
	 * Sets references parsed from def line for this OBO concept.
	 * 
	 * @param definitionRefs
	 *            String[]
	 */
	public void setDefinitionRefs(String[] definitionRefs) {
		this.definitionRefs = definitionRefs;
	}

	/**
	 * Sets pid for this OBO concept.
	 * 
	 * @param id
	 *            String
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Sets preferred name for this OBO concept.
	 * 
	 * @param name
	 *            String
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets namespace, e.g. biological function for this OBO concept.
	 * 
	 * @param namespace
	 *            String
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * Sets whether or not this OBO concept is obsolete.
	 * 
	 * @param obsolete
	 *            boolean
	 */
	public void setObsolete(boolean obsolete) {
		this.obsolete = obsolete;
	}

	/**
	 * Sets replacement term for this OBO concept.
	 * 
	 * @param replacement
	 *            String
	 */
	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}

}
