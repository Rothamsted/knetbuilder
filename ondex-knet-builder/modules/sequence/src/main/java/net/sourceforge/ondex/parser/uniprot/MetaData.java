package net.sourceforge.ondex.parser.uniprot;


/**
 * 
 * @author peschr
 *
 */
public interface MetaData {
	
	public final static String IMPD_MANUALLY_CURATED = "IMPD_MANUALLY_CURATED"; //concepts
	public final static String IMPD_AUTOMATICALLY_CURATED = "IMPD";

	public final static String CC_Protein = "Protein";
	public final static String CC_Publication = "Publication";
	public static final String CC_EC = "EC";
	public static final String CC_MOLFUNC = "MolFunc";
	public static final String CC_BIOPROC = "BioProc";
	public static final String CC_CELCOMP = "CelComp";
	public static final String CC_DISEASE = "Disease";
	
	public final static String CV_ProtFam = "ProtDomain";
	public final static String CV_UniProt = "UNIPROTKB";
	public final static String CV_UniProt_SwissProt = "UNIPROTKB-SwissProt";
	public final static String CV_UniProt_TrEMBL = "UNIPROTKB-TrEMBL";
	public final static String CV_PubMed = "NLM";
	public final static String CV_InterPro = "IPRO";
	public final static String CV_EMBL = "EMBL";
	public final static String CV_Pfam = "PFAM";
	public final static String CV_ProDom = "PRODOM";
	public final static String CV_PROSITE = "PROSITE";
	public final static String CV_PIR = "PIR";
	public final static String CV_UniGene = "UNIGENE";
	public final static String CV_KEGG = "KEGG";
	public final static String CV_EC = "EC";
	public final static String CV_GO = "GO";
	public final static String CV_RefSeq = "NC_NP";
	public final static String CV_GeneId = "NC_GE";
	public final static String CV_PRINTS = "PRINTS";
	public final static String CV_TAIR = "TAIR";
	public final static String CV_TIGR = "TIGR";
	public final static String CV_OMIM = "OMIM";
	public final static String CV_DOI = "DOI";
	public final static String CV_SGD = "SGD";
	
	public final static String CV_PDB = "PDB";
	public final static String CV_ENSEMBL = "ENSEMBL";
	public final static String CV_GRAMENE = "GR";
	
	public final static String RT_PUBLISHED_IN = "pub_in";
	public final static String RT_CAT_CLASS = "cat_c";
	public final static String RT_INVOLVED_IN = "inv_in";
	public final static String RT_HAS_FUNCTION = "has_function";//molecular function "MolFunc"
	public final static String RT_PARTICIPATES_IN = "participates_in";//biological process "BioProc"
	public final static String RT_LOCATED_IN = "located_in";//cellular component "CelComp"


	public final static String ATR_TITLE = "AbstractHeader";
	public final static String ATR_TAXID = "TAXID";
	public final static String ATR_SEQUENCE = "AA";
	public final static String ATR_YEAR = "YEAR";
	public final static String ATR_JOURNAL = "JOURNAL_REF";
	public final static String ATR_PUBTYPE = "PUB_TYPE";
	public final static String ATR_PHENOTYPE = "Pheno";
	
}
