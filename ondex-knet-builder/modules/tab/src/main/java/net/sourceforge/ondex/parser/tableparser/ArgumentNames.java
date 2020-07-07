package net.sourceforge.ondex.parser.tableparser;

/**
 * 
 * @author lysenkoa
 *
 *
 */
public interface ArgumentNames {

	public static final String SHEET_ARG = "sheet";
	public static final String SHEET_ARG_DESC = "If parsing data from Excel spreadsheet this is a required argument";

	public static final String FIRST_DATA_ROW_ARG = "firstRow";
	public static final String FIRST_DATA_ROW_ARG_DESC = " (Optional)First row";

	public static final String LAST_ROW_ARG = "lastRow";
	public static final String LAST_ROW_ARG_DESC = "(Optional for tab delimited files, strongly recomended for MS-EXCEL files) Last row";

	public static final String COLUMN_SEPARATOR_REGEX = "col_separator";
	public static final String COLUMN_SEPARATOR_REGEX_DESC = "Java regex. If parsing from a delimited file this is a required argument, not required otherwise";
	
	public static final String CONCEPT_ATT = "attribute";
	public static final String CONCEPT_ATT_DESC = "Convert value in the column into the specified attribute (gds, name or accession), the format is concept_id,col,type,[type specific options],"+
	"where the first three argumetns are required and the rest are optional. Type identifiers are (case insensitive): ATTRIBUTE, NAME or ACC. Concept_id can be anything and is just used to group the attributes to appropritate concepts. Examples:"+
	"c1,1,NAME  c1,2,ACC,TAIR  c2,3,ATTRIBUTE,p-value,NUMBER  c2,4,ATTRIBUTE,description,TEXT  c3,5,ATTRIBUTE,count,INTEGER  c3,5,ATTRIBUTE,ChemicalStructure,SMILES "+
	"Optionally, you may append a regular expression string. If it matches, the first group of such expression will be read as value. Example:  c1,1,NAME,(.{2}).*  will read the first two characters as name.";
	
	public static final String CONCEPT_CLASS = "concept_class";
	public static final String CONCEPT_CLASS_DESC = "Tuple of concept_id and corresponding concept class it should have e.g.: c1,Protein";

	public final static String DATASOURCE_ARG = "data_source";
	public final static String DATASOURCE_ARG_DESC = "The source of the concepts (e.g. TAIR, KEGG, unknown)";

	public static final String INPUT_FILE_DESC = "table-like data representations file";

}