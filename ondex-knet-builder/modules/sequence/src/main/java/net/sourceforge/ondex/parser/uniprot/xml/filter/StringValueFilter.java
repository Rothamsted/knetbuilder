package net.sourceforge.ondex.parser.uniprot.xml.filter;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Filter for all kind of values. The filter check is not case-sensitiv!
 * 
 * @author peschr
 */
public class StringValueFilter implements ValueFilter {
	
	private Set<String> accession = new HashSet<String>();

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
		return this.accession.size();
	}
	/**
	 * resets the internal state to the default value
	 */
	public void resetState() {
		state = false;
	}

	public StringValueFilter() {
	}
	
	public StringValueFilter(String text) {
		StringTokenizer st = new StringTokenizer(text, ",");
		while (st.hasMoreTokens()) {
			accession.add(st.nextToken().trim().toLowerCase());
		}
	}
	
	public StringValueFilter(Object[] text) {
		for (Object val: text) { 
			accession.add(((String)val).trim().toLowerCase());
		}
	}

	public void addFilterValues(String text) {
		accession.add(text.toLowerCase());
	}

	/**
	 * checks a value and sets the internal state. The internal state can be
	 * asked for by calling the <code>getState()<code> method
	 * 
	 * @param toCheck
	 *            the value which should be checked
	 */
	public void check(Object toCheck) {
		if (getState() == false && accession.contains(((String)toCheck).toLowerCase())) {
			this.setState(true);
		} 
	}
}
