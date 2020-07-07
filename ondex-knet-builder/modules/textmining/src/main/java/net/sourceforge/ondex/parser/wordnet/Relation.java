package net.sourceforge.ondex.parser.wordnet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Relation {

	private String id;
	private String type;
	private String r_from;
	private String r_to;
//	private String r_from_name;
//	private String r_to_name;
	private HashMap<String,String> GDS = new HashMap<String,String>();
    
	public Relation(String f, String t, String y) {

		this(f, t);
		type = y;
	}

	public Relation(int f, int t, String y) {

		this(new Integer(f).toString(), new Integer(t).toString());
		type = y;
	}
	
	public Relation(String f, String t) {

		r_from = f;
		r_to = t;	
	}
	
	public String[] getRelation() {

		String[] tmp = new String[2];
		tmp[0] = r_from;
		tmp[1] = r_to;

		return tmp;
	}

	public String getRelationType() {

		return type;
	}
	
	public void setRelationType(String t) {

		type=t;
	}
	
	public String getFrom() {

		return r_from;
	}

	public String getTo() {

		return r_to;
	}
	
//	public String getFromName() {
//
//		return r_from_name;
//	}

//	public String getToName() {
//
//		return r_to_name;
//	}
	
//	public void setFromName(String f) {
//
//		r_from_name = f;
//	}
	
//	public void setToName(String t) {
//
//		r_to_name=t;
//	}
	
	public void setFrom(String f) {

		r_from = f;
	}

	public void setTo(String t) {

		r_to = t;
	}
	
	public void setID(String i) {

		id = i;
	}
	
	public String getID() {

		return id;
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
