package net.sourceforge.ondex.mapping.interolog;

/**
 * Defines MetaData used by this mapping method.
 * 
 * @author taubertj
 *
 */
public interface MetaData {

	// attributes to be copied
	public static final String anEvidence = "Evidence";
	
	// simple evidence type
	public static final String etInterolog = "INTEROLOG";
	
	// relation type names
	public static final String rtOrtholog = "ortho";
	public static final String rtParalog = "para";
	public static final String rtInteracts = "it_wi";
	public static final String rtInterolog = "interolog";

}
