package net.sourceforge.ondex.transformer.copytag;

/**
 * PluginArgument names for copycontext transformer.
 * 
 * @author taubertj
 * 
 */
public interface ArgumentNames extends net.sourceforge.ondex.transformer.ArgumentNames {

	public static final String RELATION_TYPE_ARG = "RelationType";

	public static final String RELATION_TYPE_ARG_DESC = "The relation type to copy the context across.";

	public static final String REVERSE_ARG = "Reverse";
	
	public static final String REVERSE_ARG_DESC = "Copy context in reverse order, i.e. 'to'->'from'";
	
}
