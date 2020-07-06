package net.sourceforge.ondex.transformer.extendedcollapser;

public interface  ArgumentNames {
	public static final String RELATION_TYPES_ARG = "Retaltion_types";
	public static final String RELATION_TYPES_DESC = "Retaltion types to collapse, if more than one  ';' delimited";
	
	public static final String SOURCE_CCS_ARG = "Concept_classes_for_source";
	public static final String SOURCE_CCS_DESC = "Concept classes for source as a list";
	
	public static final String TARGET_CCS_ARG = "Concept_classes_for_target";
	public static final String TARGET_CCS_DESC = "Concept classes for target as a list";
	
	public static final String ONE_TO_ONE_MODE_ARG = "One_to_one";
	public static final String ONE_TO_ONE_MODE_DESC = "One to one mode. If ticked, one joined concept will be created per each relation" +
			"that joins the two instances of the specified concepts. If unticked the concpet of the type specified as" +
			"target of the collapse will get all of the attributes and relations of the valid source(s)";


}
