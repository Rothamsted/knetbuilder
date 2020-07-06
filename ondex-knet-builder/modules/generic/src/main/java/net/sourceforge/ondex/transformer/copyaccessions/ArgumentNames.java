package net.sourceforge.ondex.transformer.copyaccessions;

/**
 * PluginArgument names for copyaccessions transformer.
 * 
 * @author taubertj, hindlem
 * 
 */
public interface ArgumentNames extends net.sourceforge.ondex.transformer.ArgumentNames {

	public static final String RELATION_TYPE_SET_ARG = "RelationType";

	public static final String RELATION_TYPE_SET_ARG_DESC = "The relation type set to copy the accession across.";

	public static final String REVERSE_ARG = "Reverse";
	
	public static final String REVERSE_ARG_DESC = "Copy accessions in reverse order, i.e. 'to'->'from'";
	
}
