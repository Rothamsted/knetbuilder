package net.sourceforge.ondex.parser.atregnet2;

public interface MetaData {

	public static String evidence = "IMPD";
	public static String at_cv = "ATREGNET";
	public static String tair_cv = "TAIR";
	public static String proID_cv = "PROID";
	public static String genb_cv = "GENB";
	
	public final static String CC_GENE = "Gene";
	public final static String CC_PROTEIN = "Protein";
	public final static String CC_ENZYME = "Enzyme";
	public final static String CC_TF = "TF";
	public final static String CC_ProteinFamily = "TFProteinFamily";
	
	public static String repressed_by = "re_by";
	public static String regulated_by = "rg_by";
	public static String activated_by = "ac_by";
	public String M_ISP_RT = "member_of";
	public static String is_a = "is_a";
	public static String en_by = "en_by";
	
	public static String taxidAttr = "TAXID";
	public static String ATT_PMID = "PMID";
	public static String ATT_BEV = "BEV";
	
	public static String ATT_NA = "NA";
	public static String ATT_AA = "AA";
}
