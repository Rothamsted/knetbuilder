package net.sourceforge.ondex.mapping.graphpattern;

/**
 * Arguments for mapping method and their description.
 * 
 * @author taubertj
 *
 */
public interface ArgumentNames extends net.sourceforge.ondex.mapping.ArgumentNames {

	public static final String PATTERN_ARG = "pattern";

	public static final String PATTERN_ARG_DESC = "The graph pattern starting at concept class to map entries up to concept class to consider as condition, separated by comma. (e.g. Enzyme,Protein,Gene will map Enzymes based on equ relations between Genes)";

	public static final String RELATIONTYPE_ARG = "relationType";

	public static final String RELATIONTYPE_ARG_DESC = "The relation type to be considered at sufficient between concepts of the criteria concept class. (default: equ)";

}
