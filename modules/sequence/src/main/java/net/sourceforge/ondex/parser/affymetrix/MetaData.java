package net.sourceforge.ondex.parser.affymetrix;

public class MetaData 
{
	//evidence: (relations)
	public static String IMPD = "IMPD"; //concepts
	
	//concept classes:
	public static String gene = "Gene";
	public static String protein = "Protein";
	public static String ccTARGET = "TARGETSEQ";
	public static String ccCONSENSUS = "CONSENSUSSEQ";
	
	//relation_type
	public static String derives_from = "derives_from";
	
	//cvs
	public static String u_prot = "UNIPROTKB";
	public static String nc_gi = "NC_GI";
	public static String proid = "PROID";
	public static String ncnm = "NC_NM";
	public static String ncnp = "NC_NP";
	public static String genbank = "GENB";
	public static String cvdbEST = "dbEST";
	public static String cvAFFY = "AFFYMETRIX";
	public static String cvTAIR = "TAIR";
	
	//Attribute
	public static String taxID = "TAXID";
	public static String nucleicAcid = "NA";
	public static String aminoAcid = "AA";
	public static String affyChip = "AFFYCHIPSET";
	
	public static String PROBE_SET_TYPE_att = "PROBE_SET_TYPE";
	
}
