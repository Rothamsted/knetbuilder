package net.sourceforge.ondex.parser.genericobo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * 
 * @author hoekmanb
 *
 */
public class OboConcept {

	private String id = "";

	private List<String> alt_ids = new ArrayList<String>();

	private String name = "";

	private String namespace = "";

	private HashMap<String, HashSet<String>> synonyms = new HashMap<String, HashSet<String>>();

	private String definition = "";

	private List<List<String>> relations = new ArrayList<List<String>>();

	private List<String> refs = new ArrayList<String>();

	private String[] definitionRefs;

	/**
	 * Defines that this synonym is exact, see OBO/GO documentation
	 */
	public static String exactSynonym = "exact_synonym";

	/**
	 * Normal synonym.
	 */
	public static String normalSynonym = "synonym";

	// few more classe see GO/OBO documentation
	public static String broadSynonym = "broad_synonym";
	public static String narrowlSynonym = "narrow_synonym";
	public static String relatedSynonym = "related_synonym";

	public List<String> getAlt_ids() {
		return alt_ids;
	}

	public void addAlt_ids(String alt_id) {
		this.alt_ids.add(alt_id);
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public List<String> getRefs() {
		return refs;
	}

	public void addRef(String ref) {
		this.refs.add(ref);
	}

	public List<List<String>> getRelations() {
		return relations;
	}

	public void addRelation(List<String> relations) {
		this.relations.add(relations);
	}

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

	public String[] getDefinitionRefs() {
		return definitionRefs;
	}

	public void setDefinitionRefs(String[] definitionRefs) {
		this.definitionRefs = definitionRefs;
	}

	public HashMap<String, HashSet<String>> getSynonyms() {
		return synonyms;
	}

	public HashSet<String> getSynonymsOfType(String type) {
		return synonyms.get(type);
	}

}
