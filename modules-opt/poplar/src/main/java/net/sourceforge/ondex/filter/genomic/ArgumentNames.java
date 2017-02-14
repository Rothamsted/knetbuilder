package net.sourceforge.ondex.filter.genomic;

public interface ArgumentNames {
	
	public static final String CHROMOSOME_CONCEPT_ARG = "ChromosomeID";
	public static final String CHROMOSOME_CONCEPT_ARG_DESC = "The Concept ID of the Chromosome (Chromosome)";
	
	public static final String FROM_REGION_ARG = "From";
	public static final String FROM_REGION_ARG_DESC = "The start position on the chromosome, e.g. 100";

	public static final String TO_REGION_ARG = "To";
	public static final String TO_REGION_ARG_DESC = "The stop position on the chromosome, e.g. 30000";
	
	public static final String ALGORITHM_ARG = "Algorithm";
	public static final String ALGORITHM_ARG_DESC = "Choose a specific subnetwork of interest, choices at the moment are: \"NEIGHBOURHOOD\", \"GOA\", \"PATHWAY\"";

	public static final String KEYWORD_ARG = "Keyword";
	public static final String KEYWORD_ARG_DESC = "Concepts containing this string will get a size (Attribute) of 40";

}
