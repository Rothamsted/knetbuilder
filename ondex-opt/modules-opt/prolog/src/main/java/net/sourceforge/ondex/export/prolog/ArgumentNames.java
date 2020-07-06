package net.sourceforge.ondex.export.prolog;

public interface ArgumentNames {

	public static final String CONCEPTCLASS_ARG = "TargetConceptClass";
	
	public static final String CONCEPTCLASS_ARG_DESC = "The concept classes and their clause mapping to include in the prolog export.";
	
	public static final String RELATIONTYPE_ARG = "TargetRelationType";
	
	public static final String RELATIONTYPE_ARG_DESC = "The relation types and their clause mapping to include in the prolog export.";
	
	public static final String ATTRNAME_ARG = "TargetAttributeName";
	
	public static final String ATTRNAME_ARG_DESC = "The attribute names and their clause mapping to include in the prolog export.";
	
	public static final String CONCAT_ARG = "ConcatNames";
	
	public static final String CONCAT_ARG_DESC = "Concatenate the preferred name with the concept id.";
	
	public static final String RESTRICTLENGTH_ARG = "RestrictLength";
	
	public static final String RESTRICTLENGTH_ARG_DESC = "Restrict the length of any possible string to 250 characters.";
	
}
