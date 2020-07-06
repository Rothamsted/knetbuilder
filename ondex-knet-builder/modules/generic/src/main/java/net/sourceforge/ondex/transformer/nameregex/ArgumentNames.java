package net.sourceforge.ondex.transformer.nameregex;

public interface ArgumentNames {

	public static final String CC_ARG = "ConceptClass";

	public static final String CC_ARG_DESC = "Concept class of concepts to modify names on.";

	public static final String DS_ARG = "DataSource";

	public static final String DS_ARG_DESC = "Data source of concepts to modify names on.";
	
	public static final String REGEX_ARG = "RegEx";

	public static final String REGEX_ARG_DESC = "Search pattern for these names. (Test your Java regular expressions at http://myregexp.com/.)";

	public static final String REPLACE_ARG = "ReplaceWith";

	public static final String REPLACE_ARG_DESC = "The string to replace the matched pattern with.";

	public static final String COPYAS_ARG = "CopyAsAccessionWithDataSource";
	
	public static final String COPYAS_ARG_DESC = "Copy the result of the matched pattern as a accession of this data source to the concept.";
	
}
