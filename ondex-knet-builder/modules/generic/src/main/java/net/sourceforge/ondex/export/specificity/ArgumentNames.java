package net.sourceforge.ondex.export.specificity;

/**
 * 
 * @author hindlem
 *
 */
public class ArgumentNames {

	public static String TARGET_CC_ARG = "TargetConceptClass";
	public static String TARGET_CC_ARG_DESC = "The terms being annotated to the subject";
	
	public static String SOURCE_CC_ARG = "SourceConceptClass";
	public static String SOURCE_CC_ARG_DESC = "The term that is the subject of the specificity";
	
	public static String THRESHOLD_ARG = "Threshold";
	public static String THRESHOLD_ARG_DESC = "The numerical threshold consistant with the ThresholdType that determins the minimum extent of annotations permitted before reaching 1 specificity";	
	
	public static String THRESHOLD_TYPE_ARG = "ThresholdType";
	public static String THRESHOLD_TYPE_ARG_DESC = "The type of specificity evaluation i.e. \"count\", \"percentage\" or \"percentage of mean\" measure. count is the number of annoations and percentage is the preportion of the maximum annotations present in this catagory";
	
	public static String RELATION_TYPE_ARG = "RelationType";
	public static String RELATION_TYPE_ARG_DESC = "Defines the RelationType to be the subject of the specificity analyis";

	public static String SPEC_ATT_TYPE_ARG = "SpecificityAttributeName";
	public static String SPEC_ATT_TYPE_ARG_DESC = "The AttributeName to stores specificity values";
	
	public static String DEGREE_ATT_TYPE_ARG = "DegreeAttributeName";
	public static String DEGREE_ATT_TYPE_ARG_DESC = "The AttributeName to stores degree (number of edges) values";

	public static String ADD_2_CONCEPT_ARG = "AddToConcepts";
	public static String ADD_2_CONCEPT_ARG_DESC = "Add Attribute values to the concept that is the subject of the specificity analysis";

	public static String ADD_2_RELATION_ARG = "AddToRelations";
	public static String ADD_2_RELATION_ARG_DESC = "Add Attribute values to the selected relations of the concept that is the subject of the specificity analysis";

}
