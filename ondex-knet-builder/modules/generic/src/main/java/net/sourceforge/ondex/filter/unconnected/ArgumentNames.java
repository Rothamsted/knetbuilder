package net.sourceforge.ondex.filter.unconnected;

/**
 * Contains static String content for arguments.
 * 
 * @author keywan
 * 
 */
public interface ArgumentNames extends
		net.sourceforge.ondex.filter.ArgumentNames {

	static final String REMOVE_TAG_ARG = "RemoveTagDependencies";

	static final String REMOVE_TAG_ARG_DESC = "Set true to remove tag dependencies, " +
						"otherwise unconnected concepts will still remain in the graph.";
	
}
