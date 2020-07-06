package net.sourceforge.ondex.parser.medline;

public class MetaData {

	//cv
	public final static String CV_NLM = "NLM";
	
	//evidence
	public final static String ET_IMPD = "IMPD";
	
	//concept classes
	public static String CC_MESH = "MeSH";
	public final static String CC_PUBLICATION = "Publication";
	public final static String CC_CHEMICAL = "CHEM";
	
	//attributes
	public final static String ATT_NAME_ABSTRACT = "Abstract";
	public final static String ATT_NAME_ABSTRACT_HEADER = "AbstractHeader";
	public final static String ATT_NAME_AUTHORS = "AUTHORS";
	public final static String ATT_NAME_DOI = "DOI";
	public final static String ATT_NAME_MESH = "MeSH";
	public final static String ATT_NAME_CHEMICAL = "Chemical";
	public final static String ATT_NAME_YEAR = "YEAR";
	public final static String ATT_NAME_JOURNAL = "JOURNAL_REF";

	//relation types
	public final static String RT_HAS_MESH_TERM = "member_of";
	public static final String RT_HAS_CHEMICAL = "hs_ch";
	
}
