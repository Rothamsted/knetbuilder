package net.sourceforge.ondex.parser.drastic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class Stoplist {
	
	static final HashSet<String> hs; 
	static final ArrayList<String> sp; 
	
	static {
		hs= new HashSet<String>();
		hs.add("none");
		hs.add("not known");
		hs.add("not available");
		hs.add("not specified");
		hs.add("no tknown");
		hs.add("no known");
		hs.add("not know");
		hs.add("not stated");
		hs.add("notknown");
		hs.add("unknown");
		hs.add("not relevant");
	}
	
	static {
		sp= new ArrayList<String>();
		sp.add(";");
		sp.add("/");
	}
	

	
	/**
	 * Checks whether a value name means "unknown" 
	 * @param s value as String
	 * @return true if term is OK, false if term is on the stopwordlist
	 */
	public static boolean check(String s) {
		
		if (hs.contains(s.trim()))return false;
		else return true;
		
	}
	
	
	/**
	 * Checks, whether an array of Strings contains at least one
	 * separator like ';' or '/' and gives back the separator
	 * 
	 * @param queryA: array of Strings
	 * @return returns the last separator found in the queries of the array
	 */
	public static String findSeparator(ArrayList<String> queryA) {
		
		Iterator<String> it = sp.iterator();
		
		String sep = "";
	
		while (it.hasNext()) {
			
			String p = it.next();
		
			Iterator<String> queryI = queryA.iterator();
			
			while (queryI.hasNext()) {
				String query = queryI.next();
				if (query.indexOf(p) > -1) {
					sep=p;
				}
			}
		}
		return sep;
	}
	
	
	/**
	 * analyzes a String, and returns a list of separators like ';' or '/'
	 * 
	 * @param query String which may contain separators like ';' or '/'
	 * @return the list of found separators
	 */
	public static ArrayList<String> findSeparatorList(String query) {
		
		Iterator<String> it = sp.iterator();
		ArrayList<String> arr = new ArrayList<String>();
		
		while (it.hasNext()) {
			
			String p = it.next();
			if (query.indexOf(p) > -1) {
				arr.add(p);
			}
			
		}
		return arr;
	}
	
	
	/**
	 * analyzes a list of Strings for brackets,
	 * extracts the content and adds the content to the list.
	 * 
	 * @param query String which may contain brackets ()
	 * @return the list enriched with extracted content
	 */
	public static ArrayList<String> handleBrackets(ArrayList<String> accA) {
		
		Iterator<String> itA = accA.iterator();
		
		ArrayList<String> accB = new ArrayList<String>();
		
		while (itA.hasNext()) {
			String toCheck = itA.next();
			
			if (toCheck.indexOf("(")>-1) {
				/*
				 * extract term in brackets and put it at the end of the array
				 */
				String oldS = toCheck;
				
				int start = toCheck.indexOf("(");
				if (toCheck.indexOf("(")<0
						|| toCheck.indexOf(")")<toCheck.indexOf("(")){
					start = -1;
				}
				
				int stop = toCheck.length();
				if (toCheck.indexOf(")")>-1)
					stop = toCheck.indexOf(")");
				
				
				String newS = oldS.substring(start+1,stop);
				if (stop!=toCheck.length()) stop++;
				if (start==-1) start++;
				oldS = toCheck.substring(0,start)+toCheck.substring(stop);
				accB.add(oldS.trim());
				accB.add(newS.trim());
			}
			else accB.add(toCheck);
			
		}
		return accB;
	}
	
	
}
