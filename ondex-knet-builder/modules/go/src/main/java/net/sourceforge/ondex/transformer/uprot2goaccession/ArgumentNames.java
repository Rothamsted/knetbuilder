package net.sourceforge.ondex.transformer.uprot2goaccession;

public interface ArgumentNames {
	public static final String TAXID_ARG = "TaxID";
	public static final String TAXID_ARG_DESC = "Taxonomy ID of the desired organism.";
	
	public static final String ANALYZE_QUALITY_ARG = "AnalyzeQuality";
	public static final String ANALYZE_QUALITY_ARG_DESC = "Analyze Quality of annotation.";
	
	public static final String GO_FILE_ARG = "GoFile";
	public static final String GO_FILE_ARG_DESC = "Location of GO OBO file.";
	
	public static final String GOA_FILE_ARG = "GoaFile";
	public static final String GOA_FILE_ARG_DESC = "Location of the UniProt GOA file.";
	
	public static final String DBID_ARG = "DBID";
	public static final String DBID_ARG_DESC = "Database identifier to parse from GOA file";
}
