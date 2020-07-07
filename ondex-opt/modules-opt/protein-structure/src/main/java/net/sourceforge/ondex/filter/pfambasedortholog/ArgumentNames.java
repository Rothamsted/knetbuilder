package net.sourceforge.ondex.filter.pfambasedortholog;

public interface ArgumentNames {
	
//	public final static String TAXID_ARG = "TaxID";
//	public final static String TAXID_ARG_DESC = "A taxonomy id for one of the organisms that orhologs are wanted for";
	
	public final static String CONF_ARG = "ConfidenceThreshold";
	public final static String CONF_ARG_DESC = "Threshold value for inparanoid confidence";
	
	public final static String THRESHOLD_ARG = "AnnotationScoreThreshold";
	public final static String THRESHOLD_ARG_DESC = "Threshold value for GO annotation score";
	
	public final static String INTERSECTION_ARG = "PfamSetIntersectionThreshold";
	public final static String INTERSECTION_ARG_DESC = "Minimal number of matching protein families for a valid orthology relation";
	
	public final static String TERM_DEPTH_CUTOFF = "TermDepthCutoff";
	public final static String TERM_DEPTH_CUTOFF_DESC = "Maximal distance between GO terms to be considered similar";
	
	public final static String GOFILE_ARG = "GoDataDirectory";
	public final static String GOFILE_ARG_DESC = "Data directory of go file";
	
}
