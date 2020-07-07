package net.sourceforge.ondex.mapping.tmbased;

public class MetaData {

	//cv
	public final static String CV_NLM = "NLM";
	
	//evidence
	public final static String ET_IMPD = "IMPD";
	public final static String ET_TEXTMINING = "TM";
	
	//concept classes
	public static String CC_MESH = "MeSH";
	public final static String CC_PUBLICATION = "Publication";
	public final static String CC_CHEMICAL = "CHEM";
	
	//attributes
	public final static String ATT_NAME_ABSTRACT = "Abstract";
	public final static String ATT_NAME_ABSTRACT_HEADER = "AbstractHeader";
	public final static String ATT_NAME_FULLTEXT = "FullText";
	public static final String ATT_TMSCORE = "TFIDF";
	public static final String ATT_CITNUM = "CitNum";
	public static final String ATT_EVIDENCE = "EVIDENCE";

	//relation types
	public static final String RT_OCC_IN = "occ_in";
	public static final String is_a = "is_a";	
	public static final String is_p = "part_of";
	public static final String publishedIn = "pub_in";
	public final static String hasFunction = "has_function";
	public final static String hasParticipant = "has_participant";
	public final static String locatedIn = "located_in";


	
}
