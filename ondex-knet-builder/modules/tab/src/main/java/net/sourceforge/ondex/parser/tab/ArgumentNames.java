package net.sourceforge.ondex.parser.tab;

/**
 * Contains static Strings for ArgumentDefinitions.
 * 
 * @author taubertj
 * @version 10.03.2008
 */
public interface ArgumentNames {

	public static final String SKIP_ARG = "skip";

	public static final String SKIP_ARG_DESC = "How many rows to skip at begin of document.";

	public static final String FROM_COL_ARG = "fromCol";

	public static final String FROM_COL_ARG_DESC = "Index of concept parser id for from concept.";

	public static final String TO_COL_ARG = "toCol";

	public static final String TO_COL_ARG_DESC = "Index of concept parser id for to concept.";

	public static final String CONF_COL_ARG = "confCol";

	public static final String CONF_COL_ARG_DESC = "Index of confidence score of relation.";

	public static final String FROM_NAME_COL_ARG = "fromNameCol";

	public static final String FROM_NAME_COL_ARG_DESC = "ConceptName index for use with from concept.";

	public static final String TO_NAME_COL_ARG = "toNameCol";

	public static final String TO_NAME_COL_ARG_DESC = "ConceptName index for use with to concept.";

	public static final String FROM_PHENO_COL_ARG = "fromPhenoCol";

	public static final String FROM_PHENO_COL_ARG_DESC = "Attribute Pheno index for use with from concept";

	public static final String TO_PHENO_COL_ARG = "toPhenoCol";

	public static final String TO_PHENO_COL_ARG_DESC = "Attribute Pheno index for use with to concept.";

	public static final String FROM_TAXID_COL_ARG = "fromTaxId";
	
	public static final String FROM_TAXID_COL_ARG_DESC = "TaxID index for use with from concept.";
	
	public static final String TO_TAXID_COL_ARG = "toTaxId";
	
	public static final String TO_TAXID_COL_ARG_DESC = "TaxID index for use with to concept.";
	
	public final static String TAXID_TO_USE_ARG = "TaxId";

	public final static String TAXID_TO_USE_ARG_DESC = "Which taxonomy id should be assigned to the sequences.";

	public final static String CC_ARG = "CC";

	public final static String CC_ARG_DESC = "The type of the concepts (e.g. target, gene, protein)";

	public final static String CV_ARG = "DataSource";

	public final static String CV_ARG_DESC = "The source of the concepts (e.g. TAIR, KEGG, unknown)";

	public final static String RELATION_TYPE_ARG = "RelationType";

	public final static String RELATION_TYPE_ARG_DESC = "The relation type to create for every line in the tabular file.";
	
	public final static String CONF_THRESHOLD_ARG = "Threshold";
	
	public final static String CONF_THRESHOLD_ARG_DESC = "Import threshold for confidence value.";
	
	public final static String INPUT_FILE_DESC = "delimited file to import";
}
