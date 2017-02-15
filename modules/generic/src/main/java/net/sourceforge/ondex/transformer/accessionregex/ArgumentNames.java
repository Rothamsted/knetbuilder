package net.sourceforge.ondex.transformer.accessionregex;

public interface ArgumentNames {

	public static final String CC_ARG = "ConceptClass";

	public static final String CC_ARG_DESC = "Concept class of concepts to modify accessions on.";

	public static final String DS_ARG = "DataSource";

	public static final String DS_ARG_DESC = "Data source of concepts to modify accessions on.";

	public static final String ACC_ARG = "AccessionType";

	public static final String ACC_ARG_DESC = "Accession type (data source of the accession rather than concept) to modify accessions on.";

	public static final String REGEX_ARG = "RegEx";

	public static final String REGEX_ARG_DESC = "Search pattern for these accessions. (Test your Java regular expressions at http://myregexp.com/.)";

	public static final String REPLACE_ARG = "ReplaceWith";

	public static final String REPLACE_ARG_DESC = "The string to replace the matched pattern with.";

}
