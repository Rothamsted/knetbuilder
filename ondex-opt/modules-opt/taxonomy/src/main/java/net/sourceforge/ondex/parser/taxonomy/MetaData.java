package net.sourceforge.ondex.parser.taxonomy;

/**
 * Interface states meta data usage of Parser.
 * 
 * @author taubertj
 * @version 28.05.2008
 */
public interface MetaData {

	/**
	 * The evidence type
	 */
	public static final String ET = "IMPD";
	
	/**
	 * The concept class
	 */
	public static final String CC = "Taxon";
	
	/**
	 * The controlled vocabulary
	 */
	public static final String CV = "TX";
	
	/**
	 * The taxonomy id itself
	 */
	public static final String TAXID = "TAXID";
	
	/**
	 * The taxonomy rank attribute
	 */
	public static final String RANK = "RANK";
	
	/**
	 * The relation between entries
	 */
	public static final String RTSET = "is_a";
}
