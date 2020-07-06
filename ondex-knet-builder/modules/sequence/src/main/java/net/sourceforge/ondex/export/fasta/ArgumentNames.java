package net.sourceforge.ondex.export.fasta;

/**
 * 
 * @author hindlem
 *
 */
public interface ArgumentNames {

	public final static String CONCEPTS_ARG = "ConceptId";
	public final static String CONCEPTS_ARG_DESC = "Concept ids of concepts containing sequences to export";
	
	public final static String SEQUENCE_TYPE_ARG = "AttributeName";
	public final static String SEQUENCE_TYPE_ARG_DESC = "The AttributeName for the sequence type to export";
	
	public final static String HEADER_FIELDS_ARG = "HeaderFields";
	public final static String HEADER_FIELDS_ARG_DESC = "The HeaderFields to include in the fasta header";
	
	public static final String TRANSLATE_TAXID_ARG = "TranslateTaxId";
	public static final String TRANSLATE_TAXID_ARG_DESC = "Translates TAXID gds values to there scientific name";
	
	public static final String INCLUDE_VARIENTS_ARG = "IncludeSequenceVarients";
	public static final String INCLUDE_VARIENTS_ARG_DESC = "Include Varients on a AttributeName Sequence that have been produced by collapsing e.g. AA:1, AA:2";
	
	public static final String ZIP_FILE_ARG = "Zip";
	public static final String ZIP_FILE_ARG_DESC = "GZip the output";
	
}
