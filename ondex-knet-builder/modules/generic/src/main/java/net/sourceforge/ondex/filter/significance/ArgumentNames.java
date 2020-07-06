package net.sourceforge.ondex.filter.significance;

/**
 * Contains static String content for arguments.
 * 
 * @version 19.03.2008
 * @author taubertj
 * 
 */
public interface ArgumentNames extends
		net.sourceforge.ondex.filter.ArgumentNames {

	static final String CONCEPTMODE_ARG = "ConceptMode";
	
	static final String CONCEPTMODE_ARG_DESC = "If true filters concepts, if false filters realtions";
	
	static final String TARGETAN_ARG = "TargetAttributeName";

	static final String TARGETAN_ARG_DESC = "Target AttributeName to filter for significance.";
	
	static final String SIG_ARG = "Significance";
	
	static final String SIG_ARG_DESC = "A significance value to filter relations with.";
	
	static final String INVERSE_ARG = "Inverse";
	
	static final String INVERSE_ARG_DESC = "If set to true only relation smaller than Significance will be kept.";

	static final String NO_ATT_ARG = "Remove_no_att";
	
	static final String NO_ATT_ARG_DESC = "Remove elements without the attribute, if set to true.";
	
	static final String ABSOLUTE_ARG = "AbsoluteValues";
	
	static final String ABSOLUTE_ARG_DESC = "Absolute Attribute values when testing for significance";
	
}
