package net.sourceforge.ondex.tools.auxfunctions;

import java.io.IOException;

/**
 * 
 * Auxiliary functions for parsers
 * Tab Delimited Parser
 * 
 * Parses individual tab delimited lines into TabArrayObjects
 * 
 * Usage:
 * 
 * TabDelimited td = new TabDelimited(String fn); 
 * // where fn is the filename of the tab delimited file
 * or
 * TabDelimited td = new TabDelimited(String fn, Class[] c); 
 * // where fn is as before, and c is an array of class types
 * // the Objects stored in the TabArrayObject can then be converted
 * // into specified types where transformers are available. Currently,
 * // only Strings, Character, Double, Integer, Float are supported, null class
 * // is converted into null values
 * 
 * TabArrayObject tdo = td.getNext();
 * // gets the next line and delivers it in TabArrayObject form.  
 * 
 * @author sckuo
 * 
 */

public class TabDelimited extends AuxTextBase {

	private Class<?>[] parserTypes;
	
	public TabDelimited(String fn, Class<?>[] pt) {
		super(fn);
		this.parserTypes = pt; 
	}
	
	public TabDelimited(String fn) {
		
		super(fn);
		this.parserTypes = null; 
		
	}
	
	public TabArrayObject getNext() {
		
			try {
				inputLine = input.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//return values;
			
			if (inputLine != null) {
				
				String[] values = inputLine.split("\t");
				
				if (parserTypes == null) {
					
					return(new TabArrayObject(values));
					
				} else {
				
					return(new TabArrayObject(values, parserTypes));
				}
				
			} else {
				return null;
			}
			
			
			
		
	}
	
}