package net.sourceforge.ondex.mapping.attributeequality;

/**
 * The argument names that can be passed to this Mapping method
 * 
 * @author hindlem
 * 
 */
public interface ArgumentNames extends
		net.sourceforge.ondex.mapping.ArgumentNames {

	public final static String CONCEPT_CLASS_ARG = "ConceptClass";
	public final static String CONCEPT_CLASS_ARG_DESC = "ConceptClass to restrict mapping to";

	public final static String ATTRIBUTE_ARG = "AttributeNames";
	public final static String ATTRIBUTE_DESC = "AttributeNames that must be present and equal for concept to map";

	public final static String RELATION_ARG = "RelationType";
	public final static String RELATION_DESC = "RelationType to create when conditions are met";

	public final static String REPLACE_PATTERN_ARG = "ReplacePattern";
	public final static String REPLACE_PATTERN_ARG_DESC = "An optional preprocessing pattern if Attribute is on Strings";

	public final static String IGNORE_CASE_ARG = "IgnoreCase";
	public final static String IGNORE_CASE_ARG_DESC = "Ignore case if comparing Strings";

}
