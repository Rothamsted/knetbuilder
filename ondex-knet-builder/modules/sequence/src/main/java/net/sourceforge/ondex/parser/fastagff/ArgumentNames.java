package net.sourceforge.ondex.parser.fastagff;

public class ArgumentNames {
	
	public final static String TAXID_ARG = "TaxId";
	public final static String TAXID_ARG_DESC = "Set the TAXID";
	public final static String XREF_ARG = "Accession";
	public final static String XREF_ARG_DESC = "Set the Accession";
	public final static String DATASOURCE_ARG = "DataSource";
	public final static String DATASOURCE_ARG_DESC = "Set the data source of the input file";
	
	public static final String GFF_ARG = "GFF3 File";
	public static final String GFF_ARG_DESC = "A file containing gene information on GFF3 format";
	public static final String FASTA_ARG = "Fasta File";
	public static final String FASTA_ARG_DESC = "A file containing protein information on FASTA format";
	public static final String MAPPING_ARG = "Mapping File";
	public static final String MAPPING_ARG_DESC = "A file containing the relationship between genes and proteins";
	public static final String MAPPING_GENE = "Column of the genes";
	public static final String MAPPING_GENE_DESC = "The number of the column (starting from 0) where the gene IDs are in the mapping file";
	public static final String MAPPING_PROTEIN = "Column of the proteins";
	public static final String MAPPING_PROTEIN_DESC = "The number of the column (starting from 0) where the protein IDs are in the mapping file";

}
