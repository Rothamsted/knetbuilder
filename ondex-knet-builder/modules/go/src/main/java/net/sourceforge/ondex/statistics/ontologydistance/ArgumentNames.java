package net.sourceforge.ondex.statistics.ontologydistance;
/**
 * Statistics class for text mining results
 * 
 * @author keywan
 * @modified lysenkoa
 */
public interface ArgumentNames extends net.sourceforge.ondex.mapping.ArgumentNames {
	
	public final static String PR_RT_ARG = "PedictedRT";
	public static final String PR_RT_ARG_DESC = "Relation typset of the realtion evaluated.";
	
	public final static String STATISTICS_DIR_ARG = "StatsDir";
	public static final String STATISTICS_DIR_ARG_DESC = "The output directory for statistics: precision, recall, F-score...";

	public final static String CONC_CLASS_ARG = "CC";
	public static final String CONC_CLASS_ARG_DESC = "Concept class of the test set to be evaluated";
}
