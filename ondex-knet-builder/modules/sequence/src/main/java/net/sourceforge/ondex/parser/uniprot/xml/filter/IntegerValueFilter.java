package net.sourceforge.ondex.parser.uniprot.xml.filter;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class IntegerValueFilter implements ValueFilter {
	
	private Set<Integer> values = new HashSet<Integer>();

	private boolean state = false;

	/**
	 * sets the state
	 * 
	 * @param state
	 */
	public void setState(boolean state) {
		this.state = state;
	}

	/**
	 * returns the internal state
	 * 
	 * @return
	 */
	public boolean getState() {
		return state;
	}
	/**
	 * Returns the amount of the internal accession
	 * @return int
	 */
	public int getSize(){
		return this.values.size();
	}
	/**
	 * resets the internal state to the default value
	 */
	public void resetState() {
		state = false;
	}

	
	public IntegerValueFilter(String text) {
		StringTokenizer st = new StringTokenizer(text, ",");
		while (st.hasMoreTokens()) {
			values.add(Integer.parseInt(st.nextToken().trim().toLowerCase()));
		}
	}
	
	public IntegerValueFilter(int[] valuesin) {
		for (int val: valuesin) { 
			values.add(val);
		}
	}
	
	public IntegerValueFilter(Object[] valuesin) {
		for (Object val: valuesin) { 
			values.add(Integer.valueOf((String)val));
		}
	}

	public void addFilterValues(int value) {
		values.add(value);
	}

	/**
	 * checks a value and sets the internal state. The internal state can be
	 * asked for by calling the <code>getState()<code> method
	 * 
	 * @param toCheck
	 *            the value which should be checked
	 */
	public void check(Object toCheck) {
		if (getState() == false) {
			if (toCheck == null) {
				return;
			}
			if (toCheck instanceof Number) {
				if (values.contains(((Number)toCheck).intValue())) {
					this.setState(true);
				}
			} else if (toCheck instanceof String){
				try {
					if (values.contains((Integer.parseInt((String)toCheck)))) {
						this.setState(true);
					}
				} catch (NumberFormatException e) {
					return;//not a number it can't be in there
				}
			} else {
				throw new RuntimeException("Uknown Integer type to check? "+toCheck);
			}
			
		} 
	}
}
