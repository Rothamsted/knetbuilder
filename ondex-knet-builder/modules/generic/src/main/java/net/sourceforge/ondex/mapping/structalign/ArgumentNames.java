package net.sourceforge.ondex.mapping.structalign;

/**
 * Defines argument names for the structalign mapping.
 * 
 * @author taubertj
 * 
 */
public interface ArgumentNames extends net.sourceforge.ondex.mapping.ArgumentNames {

	public final static String DEPTH_ARG = "Depth";

	public final static String DEPTH_ARG_DESC = "Depth of graph traversal to look for other matches.";

	public final static String EXACT_SYN_ARG = "ExactSynonyms";

	public final static String EXACT_SYN_ARG_DESC = "Force matching of only exact Synonyms (preferred concept names).";
	
}
