package net.sourceforge.ondex.filter.conceptclass;

/**
 * Contains static String content for arguments.
 * 
 * @version 31.01.2008
 * @author taubertj
 * 
 */
public interface ArgumentNames extends
		net.sourceforge.ondex.filter.ArgumentNames {

	public static final String TARGETCC_ARG = "TargetConceptClass";
	public static final String TARGETCC_ARG_DESC = "Target Concept Class to filter out.";

	public static final String FILTER_CV_ARG = "cv_to_filter";
	public static final String FILTER_CV_ARG_DESC = "Filter the concepts of specified DataSource.";
	
	public static final String ACC_FILE_ARG = "acc_file";
	public static final String ACC_FILE_ARG_DESC = "A file of accessions of type cv_to_filter; one per line.";
		
	public static final String EXCLUDE_ARG = "Exclude";
	public static final String EXCLUDE_ARG_DESC = "Exclude concepts and relations that meet the given crieria, else if false then exclusivly include?";
}
