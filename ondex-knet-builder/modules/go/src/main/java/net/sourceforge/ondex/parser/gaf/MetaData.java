package net.sourceforge.ondex.parser.gaf;

public class MetaData {
	
	// Evidence Type of concepts
    public static String IMPD = "IMPD"; 

    //Relation Types
    public final static String hasFunction = "has_function";//molecular function "MolFunc"
    public final static String hasParticipant = "participates_in";//biological process "BioProc"
    public final static String locatedIn = "located_in";//cellular component "CelComp"

    public final static String hasFunctionNOT = "not_function";
    public final static String hasParticipantNOT = "participates_not";
    public final static String locatedInNOT = "not_located_in";
    
    public final static String rtExpressedIn = "ex_in"; //PO Anatomy
    public final static String rtExpressedDuring = "ex_du"; //PO Growth Stage
    public final static String rtHasPhenotype = "has_observ_pheno"; // TO
    
    public static final String rtIS_RELATED_TO = "r"; // if relation type not recognised

    //Concept Classes
    public static String BioProc = net.sourceforge.ondex.parser.go.MetaData.BioProc;
    public static String MolFunc = net.sourceforge.ondex.parser.go.MetaData.MolFunc;
    public static String CelComp = net.sourceforge.ondex.parser.go.MetaData.CelComp;
    public static String ccGrowthDev = net.sourceforge.ondex.parser.genericobo.MetaData.POGrowthStage;
    public static String ccStructure = net.sourceforge.ondex.parser.genericobo.MetaData.POStructure;
    public static String ccTraitOnt = "TO";
    public static String PUBLICATION = "Publication";

    
    public static String CC_ONTOLOGY_TERMS = "OntologyTerms";
    public static String gene = "Gene";
    public static String rna = "RNA";
    public static String protein = "Protein";
    public static String proteinComplex = "Protcmplx";

    //data source 
    public static String CVGO = "GO";
    public static String CVPO = "PO";
    public static String CVTO = "TO";
    public static String UNIPROTKB = "UNIPROTKB";
    public static String Ensembl = "ENSEMBL";
    public static String GOAEBI = "GOAEBI";
    public static String VEGA = "VEGA";
    public static String HINVDB = "HINVDB";
    public static String NLM = "NLM";
    public static String TAIR = "TAIR";
    public static String TIGR = "TIGR";
    public static final String GRAMENE = "GR";
    public static final String UC = "UC";
    public static final String SGN = "SGN";

	public static final String RT_PUB_IN = "pub_in";
	

    //Attribute
    public static String taxID = "TAXID";
    public static String attEVIDENCE = "EVIDENCE";

    public static String ncNM = "NC_NM";
    public static String ncNP = "NC_NP";


}
