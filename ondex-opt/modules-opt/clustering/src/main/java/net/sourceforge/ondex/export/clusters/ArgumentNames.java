package net.sourceforge.ondex.export.clusters;

/**
 * Static strings for argument names.
 * 
 * @author taubertj
 * @version 16.08.2012
 */
public interface ArgumentNames {

	public static final String MINSIZE_ARG = "MinClusterSize";

	public static final String MINSIZE_ARG_DESC = "Minimum number of concepts in a cluster.";

	public static final String PREFIX_ARG = "Prefix";

	public static final String PREFIX_ARG_DESC = "Filename prefix for export files.";

	public static final String REMAINING_ARG = "ExportRemaining";

	public static final String REMAINING_ARG_DESC = "Export all remaining concepts (not in clusters of minimum size).";

}
