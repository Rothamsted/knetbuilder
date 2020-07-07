package net.sourceforge.ondex.filter.accessionregex;

/**
 * Contains static String content for arguments.
 * 
 * @version 31.01.2008
 * @author hindlem
 * 
 */
public interface ArgumentNames extends
		net.sourceforge.ondex.filter.ArgumentNames {

	public static final String TARGETCC_ARG = "TargetConceptClass";
	public static final String TARGETCC_ARG_DESC = "Target Concept Class to filter out.";

	public static final String ACC_CV_ARG = "AccessionCV";
	public static final String ACC_CV_ARG_DESC = "The Accession of DataSource? to apply the regex to";
	
	public static final String REGEX_ARG = "Regex";
	public static final String REGEX_ARG_DESC = "The Regex which matches accessions";
}
