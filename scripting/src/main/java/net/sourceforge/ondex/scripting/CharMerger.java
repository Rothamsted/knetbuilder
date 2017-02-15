package net.sourceforge.ondex.scripting;

import java.util.ArrayList;
import java.util.List;
/**
 * 
 * @author lysenkoa
 * Class for efficient concatenation of Strings
 */

public class CharMerger {
	private List<char[]> toCombine = new ArrayList<char[]>();
	private int toatal_length = 0;
	/**
	 * Constructor
	 */	
	public CharMerger(){}
	
	/**
	 * 
	 * @param args Any number of strings, integers and CharMergers to be concatenated
	 */
	public void addFragments(Object... args){
		for(Object arg : args){
			char [] chars;
			if(arg instanceof String){
				chars = ((String)arg).toCharArray();
			}
			else if(arg instanceof Integer){
				chars = arg.toString().toCharArray();
			}
			else if(arg instanceof CharMerger){
				toCombine.addAll(((CharMerger)arg).getFragments());
				toatal_length = toatal_length + ((CharMerger)arg).getTotal();	
				continue;
			}
			else{
				continue;
			}
			addChars(chars);
		}

	}

	private void addChars(char[] chars){
		toCombine.add(chars);
		toatal_length = toatal_length + chars.length;	
	}
	/**
	 * 
	 * @return the constructed string
	 */
	public String getString(){
		char [] complete = new char[toatal_length];
		int destPos = 0;
		for(char[] fragmet : toCombine){
			System.arraycopy(fragmet, 0, complete, destPos, fragmet.length);
			destPos = destPos + fragmet.length;
		}
		return String.valueOf(complete);
	}
	/**
	 * 
	 * @return list of fragments in the merger
	 */
	public List<char[]> getFragments(){
		return toCombine;
	}
	/**
	 * 
	 * @return total length of all fragments
	 */
	public int getTotal(){
		return toatal_length;
	}
	/**
	 * clear the content
	 * 
	 */
	public void reset(){
		toCombine.clear();
		toatal_length = 0;
	}
}
