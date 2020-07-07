package net.sourceforge.ondex.transformer.copyattribute;

/**
 * PluginArgument names for Attribute transformer.
 * 
 * @author taubertj, hindlem
 * 
 */
public interface ArgumentNames extends net.sourceforge.ondex.transformer.ArgumentNames {

	public static final String RELATION_TYPE_SET_ARG = "RelationType";

	public static final String RELATION_TYPE_SET_ARG_DESC = "The relation type set to copy the Attribute across.";

	public static final String REVERSE_ARG = "Reverse";
	
	public static final String REVERSE_ARG_DESC = "Copy Attribute in reverse order, i.e. 'to'->'from'";
	
}
