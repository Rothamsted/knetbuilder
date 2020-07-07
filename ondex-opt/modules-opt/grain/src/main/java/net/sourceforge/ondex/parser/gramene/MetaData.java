package net.sourceforge.ondex.parser.gramene;

import java.util.HashMap;

/**
 * 
 * @author hindlem, hoekmanb
 *
 */
public class MetaData {

	private static HashMap<String, String> dbmap;
	
	/**
	 * 
	 * @param db the query database
	 * @return the dbname in the metadata
	 */
	public static String getMapping(String db){
		if (dbmap == null) {
			dbmap = new HashMap<String, String>(20);
			dbmap.put("INTERPRO", "IPRO");
			dbmap.put("SWISSPROT", uniprot);
			dbmap.put("TREMBL", uniprot);
			dbmap.put("UNIPROT", uniprot);
			dbmap.put("UPROT", uniprot);
			dbmap.put("UPROTKB", uniprot);
			dbmap.put(uniprot, uniprot);
			dbmap.put("EMBL", "EMBL");
			dbmap.put("PRODOM", "PRODOM");
			dbmap.put("PROSITE", "PROSITE");
			dbmap.put("PRINTS", "PRINTS");
			dbmap.put("PFAM", "PFAM");
			dbmap.put("MENDEL", "MENDEL");
			dbmap.put("PIR", "PIR");
			dbmap.put("PDB", "PDB");
			dbmap.put("HSSP", "HSSP");
			dbmap.put("EC", "EC");
			dbmap.put("SMART", "SMART");
			dbmap.put("MAIZEDB", "maizeGDB");
			dbmap.put("MEROPS", "MEROPS");
			dbmap.put("ENZYME", "EC");
			dbmap.put("TRANSFAC", "TF");
			dbmap.put("TRANSPATH", "TP");
			dbmap.put("EC", ec);
			dbmap.put("TIGR", tigr);
			
			dbmap.put("IRGSP Gene".toUpperCase(), "IRGSP");
			dbmap.put("GrainGenes Gene".toUpperCase(), "GrainGenes");
			dbmap.put("Wheat Gene Catalogue Number [GrainGenes]".toUpperCase(), "CGSW");
			dbmap.put("TIGR rice gene model".toUpperCase(), tigr);
			dbmap.put("miRBase".toUpperCase(), "MIRBASE");
			dbmap.put("MaizeGDB".toUpperCase(), "maizeGDB");
			dbmap.put("Rice Ensembl Gene".toUpperCase(), "ENSEMBL");
			dbmap.put("E.C. Number(s)".toUpperCase(), ec);
			dbmap.put("Oryzabase.2".toUpperCase(), "Oryzabase");
			dbmap.put("Oryzabase".toUpperCase(), "Oryzabase");
			dbmap.put("Gramene Protein".toUpperCase(), gramene);
			dbmap.put("GenBank Nucleotide".toUpperCase(), "GENB");
			
			dbmap.put("Pubmed".toUpperCase(), "NLM");
		}
		return dbmap.get(db.toUpperCase());
	}
	
	//evidence: (relations)
	public static String IMPD = "IMPD"; //concepts
	
	//concept classes:
	public static String BioProc = "BioProc";
	public static String MolFunc = "MolFunc";
	public static String CelComp = "CelComp";	
	public static String POSTRUC  = "POSTRUC";
	public static String PODevStag = "PODevStage";
	public static String Tissue = "Tissue";
	
	public static String GRO = "GRO";
	public static String TraitOnt = "TO";
	public static String gene = "Gene";
	public static String protein = "Protein";
	public static String enzyme = "Enzyme";
	public static final String Environment = "Environment";
	public static final String PUBLICATION = "Publication";
	public static final String Cultivar = "Cultivar";
	//cv
	public static String gramene = "GR";
	public static String ec = "EC";
	public static String ensembl = "ENSEMBL";
	public static String maizeGDB = "maizeGDB";
	public static String mirbase = "MIRBASE";
	public static String tigr = "TIGR";
	public static String uniprot = "UNIPROTKB";
	public static String goCv = "GO";
	public static String cv_enzyme = "EC";
	public static String plant_onto = "PO";
	public static String env_onto = "EO";
	
	//relType
	public static String m_isp = "member_of";	
	public static String is_a = "is_a";
	public static String cat_c = "cat_c";
	public static final String en_by = "en_by";
	public static final String pub_in = "pub_in";

	//Relation Types
	public final static String hasFunction = "has_function";//molecular function "MolFunc"
	public final static String hasParticipant = "has_participant";//biological process "BioProc"
	public final static String locatedIn = "located_in";//cellular component "CelComp"
	
	//Attribute
	public static String taxID = "TAXID";


}
