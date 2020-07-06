package net.sourceforge.ondex.parser.drastic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Stores all information for one ONDEX concept.
 * The data will be extracted from one DraEntity,
 * which can contain more than one concept (Gen and Treatment).
 * 
 * @author winnenbr
 *
 */
public class Concept {

	String key = "";
	String taxID = "";
	String description = "";
	String conceptClass = "";
	String prefName = "";
	HashSet<String> accs = new HashSet<String>();
	HashSet<String> names= new HashSet<String>();
	int id = 0;
	
	/**
	 * All accessions from DraEntity will be written to Concept.
	 * 
	 * @param c the collection of accession numbers
	 */
	public void addAccessions(Collection<String> c) {
		accs.addAll(c);
	}

	/**
	 * Annotaion from DraEntity will be written to Concept.
	 * If there is already an annotaion for this object,
	 * append this annotation with ;.
	 * 
	 * @param s the annotation
	 */
	public void addAnnotation(String s) {
		if (description.equals(""))
			description = s;
		else description = description + " ; " + s;
	}
	
	/**
	 * All names (except the preferred name)
	 * from DraEntity will be written to Concept.
	 * 
	 * @param set
	 */
	public void addNames(HashSet<String> set) {
		
		if (set.size()!=0){
			Iterator<String> sitd = set.iterator();
			
			while (sitd.hasNext()) {
				String s = sitd.next();
				if (!s.equals(""))
					names.add(s);
			}
		}
	}
	
	/**
	 * Get the description of this concept.
	 * 
	 * @return the decription as String.
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * If ID of this concept not yet set,
	 * set ID.
	 * 
	 * @param i ID
	 */
	public void setID(int i) {
		if (id==0) {
			id=i;
		}
	}
	
	/**
	 * Get the ID of this concept.
	 * 
	 * @return the ID as int.
	 */
	public int getID() {
		return id;
	}
	
	/**
	 * Get the Key of this concept, which
	 * identifies the object.
	 * (normally name or accession)
	 * 
	 * @return the ID as int.
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * Set the key of this concept only if not yet set.
	 * 
	 * @param s the key as String.
	 */
	public void setKey(String s) {
		if (key.equals("")) {
			key=s;
		}
	}
	
	/**
	 * Give the name of the concept's class:
	 * Gene or Treatment
	 * 
	 * @return coceot class as String
	 */
	public String getConceptClass() {
		return conceptClass;
	}
	
	/**
	 * Set the class of this concept.
	 * Gene or Treatment
	 * 
	 * @param s the class name as String.
	 */
	public void setConceptClass(String s) {
		conceptClass = s;
	}
	
	
	public void setPrefName(String n) {

		prefName=n.trim();
	}
	
	public String getPrefName() {
		//if (prefName.equals(""))
			//	return null;
		//else 
		return prefName;
	}
	
	public Iterator<String> getAccs() {
		return accs.iterator();
	}
	
	public Iterator<String> getNames() {
		return names.iterator();
	}
	
	public Iterator<String> getAllNames() {
		HashSet<String> newNames = new HashSet<String>();
		
		if (prefName.equals("")){
			newNames.add(prefName);
		}
		
		newNames.addAll(names);	
		return newNames.iterator();
	}
	
	public void setTaxID(String id) {
		taxID=id;
	}
	
	public String getTaxID() { 
		
		if (taxID!=null)
			return taxID.toLowerCase();
		else return null;
	}
}
