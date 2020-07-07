package net.sourceforge.ondex.export.tagclusters;

/**
 * Static strings for argument names.
 * 
 * @author taubertj
 * @version 13.03.2009
 */
public interface ArgumentNames {

	public static String ATTRIBUTE_ARG = "Attribute";
	
	public static String ATTRIBUTE_ARG_DESC = "AttributeName id for which Attribute value should be included into output.";
	
	public static String TAG_ARG = "TagConceptClass";
	
	public static String TAG_ARG_DESC = "What concept class should be assumed to be the tag for the graph.";
	
	public static String EXCLUSIVE_ARG = "Exclusive";
	
	public static String EXCLUSIVE_ARG_DESC = "Only output concepts which have this particular Attribute associated.";
	
}
