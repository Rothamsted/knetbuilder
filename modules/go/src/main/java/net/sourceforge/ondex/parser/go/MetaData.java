package net.sourceforge.ondex.parser.go;

public class MetaData {
	
	public static String IMPD = "IMPD"; 

	// concept classes:
	public static String BioProc = "BioProc";
	public static String MolFunc = "MolFunc";
	public static String CelComp = "CelComp";

	// data source
	public static String cvGO = "GO";
	public static String cvGOSLIM = "GOSLIM";
	public static String cvEC = "EC";
	public static String cvTC = "TC";
	public static String cvAC = "AC";
	public static String cvMC = "MC";
	public static String cvRESID = "RESID";
	public static String cvUME = "UM-E";
	public static String cvUMR = "UM-R";
	public static String cvUMP = "UM-P";
	public static String cvREAC = "REAC";
	public static String cvWIKI = "WIKI";
	public static String cvTX = "TX";
	public static String DATASOURCE_KEGG = "KEGG";
	public static String DATASOURCE_PO = "PO";
	public static String DATASOURCE_RHEA = "RHEA";

	// relType
	public static String is_a = "is_a";
	public static String is_p = "part_of";
	public static String has_part = "has_participant";
	public static String occurs_in = "occ_in";
	public static String results_in = "contrib";
	public static String rtRp_by = "rp_by";
	public static String rtRegulates = "regulates";
	public static String rtPos_reg = "pos_reg";
	public static String rtNeg_reg = "neg_reg";
}
