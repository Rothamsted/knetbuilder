package net.sourceforge.ondex.parser.tair;

public class MetaData 
{
	//evidence: (relations)
	public final static String IMPD = "IMPD"; //concepts
	
	//concept classes:
	public final static String gene = "Gene";
	public final static String protein = "Protein";
	public final static String DOMAIN = "ProtDomain";
	
	
	//cv
	public final static String tair		= "TAIR";
	public final static String ncNM		= "NC_NM";
	public final static String ncNP		= "NC_NP";
	public final static String UNIPROTKB	= "UNIPROTKB";
	public final static String IPRO 	= "IPRO"; //interpro
	
	//Attribute
	public final static String taxID = "TAXID";
	public final static String nucleicAcids = "NA";
	public final static String aminoAcids = "AA";
	
	//TaxID Value
	public final static String taxIDValue = "3702"; //arabidopsis NCBI taxID
	
	//Relation Types:
	public final static String encodedBy = "enc";
	public final static String isPartOf = "member_of";

	public static final String ATT_E_VALUE = "BLEV";

}
