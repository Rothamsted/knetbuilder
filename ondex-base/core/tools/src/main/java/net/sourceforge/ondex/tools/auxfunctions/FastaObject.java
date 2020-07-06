package net.sourceforge.ondex.tools.auxfunctions;

/**
 * 
 * Auxiliary functions for parsers
 * Fasta container
 * 
 * Wrapper object for Fasta entries
 * 
 * Usage:
 * 
 * FastaObject fo = new FastaObject(String h, String s); // where h is the header and s is the sequence
 * 
 * String h = fo.getHeader(); // returns header, including initial '>'
 * 
 * String s = fo.getSeq(); // returns sequence
 * 
 *  @author sckuo
 * 
 */

public class FastaObject {

	private String seq;
	private String header;
	
	public FastaObject (String f_header, String f_seq) {
	
		this.seq = f_seq;
		this.header = f_header;
		
		// TODO: Validation of input sequence may be a good idea
		
	}
	
	public String getSeq() {
		return this.seq;
	}
	
	public String getHeader() {
		return this.header;
	}
	
	
}
