package net.sourceforge.ondex.parser.brenda;

public interface MetaData {

	//evidence: (relations)
	public static String ET = "IMPD"; //concepts
	
	//concept classes:
	public final static String cc_Publication = "Publication";
	public final static String cc_PublicationList = "PublicationList";
	public final static String cc_Compound = "Comp";
	public final static String cc_CelComp = "CelComp";	
	public final static String cc_Tissue = "Tissue";
	public final static String cc_Gene = "Gene";
	public final static String cc_Protein = "Protein";
	public final static String cc_Enzyme = "Enzyme";
	public final static String cc_Ec = "EC";
	public final static String cc_Reaction = "Reaction";

	//cv
	public final static String CV_ec = "EC";
	public final static String CV_brenda = "BRENDA";
	public final static String CV_cas = "CAS";
	public final static String CV_NLM = "NLM";
	
	//relType
	public final static String rt_m_isp = "member_of";	
	public final static String rt_loc_in = "located_in";
	public final static String rt_is_a = "is_a";
	public final static String rt_cat_c = "cat_c";
	public final static String rt_co_by = "co_by";
	public final static String rt_in_by = "in_by";
	public final static String rt_cs_by = "cs_by";
	public final static String rt_pd_by = "pd_by";
	public final static String rt_ca_by = "ca_by";
	public final static String rt_ac_by = "ac_by";
	
	//Attribute
	public final static String at_taxID = "TAXID";
	public final static String at_YEAR = "YEAR";
	public final static String at_AUTH = "AUTHORS";
	public final static String at_TITLE_JOURNAL = "AbstractHeader";
	
	//AttributeName
	public final static String ATTR_NAME_KM = "KM";
	public final static String ATTR_NAME_KI = "KI";
}
