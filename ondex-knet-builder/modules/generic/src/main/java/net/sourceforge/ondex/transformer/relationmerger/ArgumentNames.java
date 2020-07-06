package net.sourceforge.ondex.transformer.relationmerger;

/**
 * PluginArgument names for relationmerger transformer.
 * 
 * @author taubertj
 * 
 */
public interface ArgumentNames extends net.sourceforge.ondex.transformer.ArgumentNames {

	public static final String FIRST_RELATION_TYPE_ARG = "FirstRelationType";

	public static final String FIRST_RELATION_TYPE_ARG_DESC = "The first relation type to merge.";

	public static final String SECOND_RELATION_TYPE_ARG = "SecondRelationType";

	public static final String SECOND_RELATION_TYPE_ARG_DESC = "The second relation type to merge.";
	
	public static final String CONCEPT_CLASS_ARG = "ConceptClass";
	
	public static final String CONCEPT_CLASS_ARG_DESC = "ConceptClass for from and to concept.";
	
	public static final String EXCLUSIVE_ARG = "Exclusive";
	
	public static final String EXCLUSIVE_ARG_DESC = "Keep only the merged relations and remove all other of the specified kinds.";
	
}
