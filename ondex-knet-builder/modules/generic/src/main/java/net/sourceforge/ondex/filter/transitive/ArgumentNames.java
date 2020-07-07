package net.sourceforge.ondex.filter.transitive;

/**
 * Contains static String content for arguments.
 * 
 * @version 09.05.2008
 * @author lysenkoa
 * 
 */
public interface ArgumentNames extends
		net.sourceforge.ondex.filter.ArgumentNames {
	static final String CV_ARG = "DataSource";
	static final String CV_ARG_DESC = "Seed cv that will be used to extract the subgraph.";
	static final String ATT_ARG = "AttributeName";
	static final String ATT_ARG_DESC = "Seed attribute name that will be used to extract the subgraph.";
	static final String LEVEL_ARG = "level";
	static final String LEVEL_ARG_DESC = "Cutoff level.";
}

