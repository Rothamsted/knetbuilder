package net.sourceforge.ondex.parser.plntfdb;

public interface MetaData {

	//cc
	public String CC_TRANS_FACTOR = "TF";
	public String CC_PROTEIN = "Protein";
	public String CC_ProteinFamily = "TFProteinFamily";
	
	
	//cv
	public static String CV_PLNTFDB = "PlnTFDB";
	public static String CV_TAIR = "TAIR";
	public static String CV_TIGR = "TIGR";
	public static String MAIZE_CV = "MAIZE_GI";
	
	public static String SUGERCANE_CV = "SUGERCANE_GI";
	public static String SORGUM_CV = "Sorghum-JGI";
	public static String POPLAR_CV = "Poplar-JGI";
	public static String OS_TAU_ALGAE_CV = "Ostreococcus_tauri-JGI";
	
	public static String CHLAM_CV = "Chlamydomonas_reinhardtii-JGI";
	public static String MOSS_CV = "Physcomitrella_patens-JGI";
	public static String CY_MER_CV = "Cyanidioschyzon_merolae_GP";
	//et
	public String ET_IMPD = "IMPD";
	
	//att
	public String ATT_TAXID = "TAXID";
	public String ATT_AA = "AA";
	
	public String M_ISP_RT = "member_of";
	public static String EN_BY_RT = "en_by";
	public static String IS_A_RT = "is_a";
	
	public static String tfAttr = "Transcription_factor";

	
}
