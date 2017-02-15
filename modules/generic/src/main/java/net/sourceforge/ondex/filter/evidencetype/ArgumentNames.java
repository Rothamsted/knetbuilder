package net.sourceforge.ondex.filter.evidencetype;

/**
 * Arguments for evidence type filter.
 * 
 * @author taubertj
 *
 */
public interface ArgumentNames {

	public static final String ET_ARG = "EvidenceType";
	public static final String ET_ARG_DESC = "EvidenceType to be taken into consideration.";

	public static final String CONCEPTS_ARG = "OnConcepts";
	public static final String CONCEPTS_ARG_DESC = "If true filter on concepts, else on relations.";
	
	public static final String EXCLUDE_ARG = "Exclude";
	public static final String EXCLUDE_ARG_DESC = "If true exclude concepts and/or relations from graph, else exclude all non-matching concepts and/or relations.";
	
}
