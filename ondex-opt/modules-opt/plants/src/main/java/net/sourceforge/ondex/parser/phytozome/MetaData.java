package net.sourceforge.ondex.parser.phytozome;

public class MetaData {

	//this two are used as both ConceptClass and AttributeName
	public static final String CHROMOSOME = "Chromosome";
	public static final String SCAFFOLD = "Scaffold";
	
	public static final String CC_PROTEIN = "Protein";
	public static final String CC_GENE = "Gene";
	public static final String CC_CDS = "CDS";
	
	public static final String RT_ENCODES = "enc"; //Gene-->Protein; Gene-->CDS; CDS-->Protein	

	public static final String DS_PHYTOZOME = "PHYTOZOME";
	public static final String DS_ENSEMBL = "ENSEMBL";

	public static final String AN_TAXID = "TAXID";
	public final static String AN_BEGIN = "BEGIN";
	public final static String AN_END = "END";
	public final static String AN_STR = "STR";
	public final static String AN_AA = "AA";
	public final static String AN_NA = "NA";
	
	public static final String ET_IMPD = "IMPD";
	
}
