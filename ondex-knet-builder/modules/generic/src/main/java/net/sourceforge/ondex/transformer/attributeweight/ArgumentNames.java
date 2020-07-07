package net.sourceforge.ondex.transformer.attributeweight;

/**
 * PluginArgument names for gdsweight transformer.
 * 
 * @author taubertj
 * 
 */
public interface ArgumentNames extends net.sourceforge.ondex.transformer.ArgumentNames {

	public static final String RELATION_TYPE_ARG = "RelationType";

	public static final String RELATION_TYPE_ARG_DESC = "The relation type to calculate weighted Attribute on.";

	public static final String ATTRIBUTE_ARG = "Attribute";

	public static final String ATTRIBUTE_ARG_DESC = "The Attributes to take into account.";
	
	public static final String WEIGHTS_ARG = "Weights";
	
	public static final String WEIGHTS_ARG_DESC = "What weights to use, e.g. 0.1,0.4,0.5 for 3 Attributess.";

	public static final String INVERSE_ARG = "Inverse";
	
	public static final String INVERSE_ARG_DESC = "Which weights should be treated inverse (like BLAST e-value), e.g. true,false,false";
	
}
