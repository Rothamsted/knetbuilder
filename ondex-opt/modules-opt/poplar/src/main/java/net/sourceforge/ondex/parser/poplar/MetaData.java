package net.sourceforge.ondex.parser.poplar;

public class MetaData {

	//this two are used as both ConceptClass and AttributeName
	public static final String CHROMOSOME = "Chromosome";
	public static final String SCAFFOLD = "Scaffold";
	
	public static final String CC_PROTEIN = "Protein";
	public static final String CC_GENE = "Gene";
	public static final String CC_TF = "TF";
	public static final String CC_MolFunc = "MolFunc";
	public static final String CC_BioProc = "BioProc";
	public static final String CC_CelComp = "CelComp";
	public static final String CC_PATHWAY = "Path";
	public static final String CC_ENZYME = "EC";
	public static final String CC_QTL = "QTL";
	public static final String CC_PUBLICATION = "Publication";
	
	public static final String RT_ENCODEDBY = "en_by"; //Gene-->Protein	
	public static final String RT_hasFunction = "has_function";
	public static final String RT_hasParticipant = "has_participant";
	public static final String RT_locatedIn = "located_in";
	public static final String RT_CATC = "cat_c";      //Protein-->EC
	public static final String RT_MISP = "member_of";      //Enzyme-->Pathway
	public static final String RT_PUBIN = "pub_in";
	public static final String RT_ISA = "is_a";	
	
	public static final String CV_GO = "GO";
	public static final String CV_JGI = "Poplar-JGI";
	public static final String CV_PHYTOZOME = "PHYTOZOME-POPLAR";
	public static final String CV_EC = "EC";
	public static final String CV_NLM = "NLM";
	public static final String CV_DPTF = "DPTF";

	public static final String AN_TAXID = "TAXID";
	public final static String AN_BEGIN = "BEGIN";
	public final static String AN_END = "END";
	public final static String AN_STR = "STR";
	public final static String AN_AA = "AA";
	public final static String AN_NA = "NA";
	public final static String AN_YEAR = "YEAR";
	
	public static final String ET_IMPD = "IMPD";
	
}
