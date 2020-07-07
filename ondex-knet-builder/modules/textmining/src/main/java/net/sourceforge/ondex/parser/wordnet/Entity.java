package net.sourceforge.ondex.parser.wordnet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public abstract class Entity  {

	protected String id = "";
    protected String name = "";
    
    protected ArrayList<String> synonyms = new ArrayList<String>();
    protected ArrayList<Relation> relations = new ArrayList<Relation>();
    protected ArrayList<Accession> accessions = new ArrayList<Accession>();
    protected HashMap<String,String> GDS = new HashMap<String,String>();
    
    protected String description = "";
    protected String description_ca = "";
    protected String description_cf = "";
    protected String description_cc = "";
    protected String is_a_nr = "";
    protected String is_a_name = "";
    
    public static final String element_of="";
    
    public abstract String getElement_of();
    
    public static final String of_type="";
    
    public abstract String getOf_type();
    
    public static final String evidence="";
    
    public abstract String getEvidence();
    
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public ArrayList<String> getSynonyms() {
		return synonyms;
	}
	
	public void setSynonyms(ArrayList<String> synonyms) {
		this.synonyms = synonyms;
	}
	
	public void addSynonym(String s) {
		this.synonyms.add(s);
	}
	
	public ArrayList<Accession> getAccessions() {
		return accessions;
	}
	
	public void setAccessions(ArrayList<Accession> accs) {
		this.accessions = accs;
	}
	
	public void setRelations(ArrayList<Relation> relations) {
		this.relations = relations;
	}
	
	public void setRelations(Relation relation) {
		this.relations.add(relation);
	}
	
	public Iterator<Relation> getRelations() {
		return relations.iterator();
	}
	
	public int getRelationsSize() {
		return relations.size();
	}
	
	public void addAccession(Accession accession) {
		this.accessions.add(accession);
	}
	
	public void addGDS(String a, String b) {
		this.GDS.put(a,b);
	}
	
	public Iterator<ArrayList<String>> getGDSs() {
		Iterator<String> keys = GDS.keySet().iterator();
		ArrayList<ArrayList<String>> array = new ArrayList<ArrayList<String>>();
		while (keys.hasNext()) {
			ArrayList<String> tmp = new ArrayList<String>();
			String key = keys.next();
			String value = GDS.get(key);
			tmp.add(key);
			tmp.add(value);
			array.add(tmp);
		}
		return array.iterator();
	}
	
	public String getGDS(String k) {
		return GDS.get(k);
	}
	
	public Iterator<String> getGDSKeys() {
		return GDS.keySet().iterator();
	}
}
