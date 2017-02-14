package net.sourceforge.ondex.parser.phytozome;

public interface ArgumentNames {
	
    public static String TAXID_ARG = "TaxID";
    public static String TAXI_ARG_DESC = "Specify the NCBI/UniProt taxid of this species.";
    
    public static String NUM_CHROMOSOMES_ARG = "ChromosomeNumber";
    public static String NUM_CHROMOSOMES_ARG_DESC = "Enter the number of chromosomes for this species. This is used to distinguish between main chromosomes and scaffolds";
    
	public static String ACC_DATASOURCE_ARG = "AccDataSource";
	public static String ACC_DATASOURCE_DESC = "Choose a datasource for accessions e.g. TAIR (default: ENSEMBL)";
	
	public static String SYNONYMS_PREF_ARG = "PreferredSynonyms";
	public static String SYNONYMS_PREF_DESC = "True if synonyms from synonym.txt files should be made preferred names, otherwise set to false.";

}
