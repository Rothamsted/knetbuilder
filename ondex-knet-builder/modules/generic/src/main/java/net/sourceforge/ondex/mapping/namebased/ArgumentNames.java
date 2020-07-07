package net.sourceforge.ondex.mapping.namebased;

/**
 * Defines argument names for the namebased mapping.
 * 
 * @author taubertj
 * 
 */
public interface ArgumentNames extends net.sourceforge.ondex.mapping.ArgumentNames {

	public final static String NAME_THRESHOLD_ARG = "NameThreshold";
	public final static String NAME_THRESHOLD_ARG_DESC = "Number of concept names that have to match.";

	public final static String EXACT_SYN_ARG = "ExactSynonyms";
	public final static String EXACT_SYN_ARG_DESC = "Force matching of only exact Synonyms (preferred concept names).";
	
	public final static String EXACT_NAME_MAPPING_ARG = "ExactNameMapping";
	public final static String EXACT_NAME_MAPPING_ARG_DESC = "Enforces when true that mappings between names should be case insensitive exact";
}
