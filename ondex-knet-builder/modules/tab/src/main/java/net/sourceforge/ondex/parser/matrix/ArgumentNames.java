package net.sourceforge.ondex.parser.matrix;

/**
 * Contains static Strings for ArgumentDefinitions.
 * 
 * @author taubertj
 * @version 31.03.2011
 */
public interface ArgumentNames {

	public final static String TAXID_TO_USE_ARG = "TaxId";

	public final static String TAXID_TO_USE_ARG_DESC = "Which taxonomy id should be assigned to the concepts.";

	public final static String CONCEPT_CLASS_ARG = "ConceptClass";

	public final static String CONCEPT_CLASS_ARG_DESC = "The type of the concepts (e.g. target, gene, protein)";

	public final static String DATA_SOURCE_ARG = "DataSource";

	public final static String DATA_SOURCE_ARG_DESC = "The source of the concepts (e.g. TAIR, KEGG, unknown)";

	public final static String RELATION_TYPE_ARG = "RelationType";

	public final static String RELATION_TYPE_ARG_DESC = "The relation type to create for every line in the tabular file.";

	public final static String INPUT_FILE_DESC = "Comma separated connectivity matrix to import.";
}
