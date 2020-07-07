package net.sourceforge.ondex.parser.ecocyc;

/**
 * Metadata constants for the ecocyc parser
 * @author peschr
 *
 */
public interface MetaData {
	
	public final static String TAXID = "83333";
	
	//evidence: (relations)
	public final static String IMPD = "IMPD"; //concepts
	
	//concept classes:
	public final static String CC_Gene = "Gene";
	public final static String CC_Protein = "Protein";
	public final static String CC_Publication = "Publication";
	public final static String CC_Enzyme = "Enzyme";
	public final static String CC_PROTEIN_COMPLEX = "Protcmplx";
	public final static String CC_COMPOUND = "Comp";
	public final static String CC_REACTION = "Reaction";
	public final static String CC_PATHWAY = "Path";
	public final static String CC_EC = "EC";
	
	//cv
	public final static String CV_NCBIGene = "NC_GE";
	public final static String CV_REFSEQGene = "NC_NM";
	public final static String CV_DB		= "TAIR";
	public final static String CV_EcoC		= "ECOCYC";
	public final static String CV_PubMedID = "NLM";
	public final static String CV_ECOCYC = "ECOCYC";
	public final static String CV_CAS = "CAS";
	public final static String CV_REFSEQProtein ="NC_NP";
	public final static String CV_PID = "PROID";
	public final static String CV_PIR = "PIR";
	public final static String CV_UniProt = "UNIPROTKB";
	public final static String CV_SMILES = "PUBCHEM";
	public final static String CV_EC = "EC";
	public final static String CV_KEGG = "KEGG";
	
	//relations
	public final static String RT_COFACTORS_BY  =  "co_by";
	public final static String RT_PUBLISHED_IN = "pub_in";
	public final static String RT_IS_A = "is_a";
	public final static String RT_MEMBER_IS_PART_OF = "member_of";
	public final static String RT_IS_PART_OF = "part_of";
	public final static String RT_ENCODED_BY = "en_by";
	public final static String RT_CONSUMED_BY="cs_by";
	public final static String RT_PRODUCED_BY ="pd_by";
	public final static String RT_INHIBITED_BY =  "in_by";
	public final static String RT_ACTIVATED_BY = "ac_by";
	public final static String RT_PRECEDED_BY = "pr_by";
	public final static String RT_PART_OF_CATALYSING_CLASS = "cat_c";
	public final static String RT_CATALYSED_BY = "ca_by";
	
	//attributs
	public final static String ATR_PUBLICATION_TITLE = "AbstractHeader";
	public final static String ATR_TAXID = "TAXID";
	public final static String ATR_MOLWEIGHT = "Mr";
	public final static String ATR_DELTAGO = "DELTAGO";
}
