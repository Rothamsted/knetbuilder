package net.sourceforge.ondex.parser.correlationtab;

/**
 * Contains static Strings for ArgumentDefinitions.
 * 
 * @author hindlem
 * @version 02.07.2008
 */
public interface ArgumentNames {

	public static String HEADER_VALUES_ARG = "HeaderValues";
	public static String HEADER_VALUES_ARG_DESC = "Header Values representing concepts in a list form (concept values will be added as unambiguous accessions)";
	
	public static String INCLUSION_LIST_ARG = "InclusionList";
	public static String INCLUSION_LIST_ARG_DESC = "Genes from the matrices to include (all other genes will be ignored)";
	
	public static String HEADER_CONCEPTCLASS_ARG = "HeaderConceptClass";
	public static String HEADER_CONCEPTCLASS_ARG_DESC = "The ConceptClass to assign to concepts";
	
	public static String ACCESSION_CV_ARG = "AccessionCV";
	public static String ACCESSION_CV_ARG_DESC = "The DataSource for the accession idetifying the headers";
	
	public static String HEADER_ATTRIBUTENAME_ARG = "HeaderAttributeName";
	public static String HEADER_ATTRIBUTENAME_ARG_DESC = "An AttributeName and value to create as a Attribute on each Concept";
	
	public static String CONTEXT_ARG = "Context";
	public static String CONTEXT_ARG_DESC = "Context defined as {Name, ConceptClass} name must be unique for this instance and will be added as the pid and name, cv is taken from the general cv";
	
	public static String CORRELATION_ATTRIBUTENAME_ARG = "CorrelationAttributeName";
	public static String CORRELATION_ATTRIBUTENAME_ARG_DESC = "Correlation AttributeName";

	public static String CORRELATION_TABLE_ARG = "CorrelationFile";
	public static String CORRELATION_TABLE_ARG_DESC = "CorrelationFile";
		
	public static String CV_ARG  = "DataSource";
	public static String CV_ARG_DESC  = "DataSource to assign to these concepts and relations";
	
	public static String EVIDENCE_TYPE_ARG  = "EvidenceType";
	public static String EVIDENCE_TYPE_ARG_DESC  = "EvidenceType to assign to these concepts and relations";

	public static String PVALUE_CUT_ARG = "PValueCutOff";
	public static String PVALUE_CUT_ARG_DESC = "The highest PValue to allow";
	
	public static String MIN_CORR_ARG = "MinCorrelation";
	public static String MIN_CORR_ARG_DESC = "The Minimum correlation allowed, defined as values greater that the absoluted correlation value {parsed when the MinCorrelation > abs(correlation)}";
	
	public static String TAXID_ARG = "TAXID";
	public static String TAXID_ARG_DESC = "NCBI TAXID for all concepts";
}
