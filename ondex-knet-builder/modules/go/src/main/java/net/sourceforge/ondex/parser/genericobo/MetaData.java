package net.sourceforge.ondex.parser.genericobo;

/**
 * 
 * @author hoekmanb
 *
 */
public class MetaData {
	// Evidence Type:
	public static final String IMPD = "IMPD"; // Generic

	// concept classes:
	public static final String Thing = "Thing"; // Generic
	public static final String BioProc = "BioProc"; // GO
	public static final String MolFunc = "MolFunc"; // GO
	public static final String CelComp = "CelComp"; // GO
	public static final String Taxon = "Taxon"; // GO
	public static final String Comp = "Comp"; // Chebi
	public static final String POStructure = "PO_PlantStructure"; // PO
	public static final String POGrowthStage = "PO_GrowthStage"; // PO

	// cv Concepts:
	public static final String cvGO = "GO"; // GO
	public static final String cvChebi = "CHEBI"; // Chebi
	public static final String cvPO = "PO"; // PO
	public static final String cvGramene = "GR"; // Gramene

	// Cv Accessions:
	public static final String cvEC = "EC"; // GO
	public static final String cvTC = "TC"; // GO
	public static final String cvMC = "MC"; // GO
	public static final String cvRESID = "RESID"; // GO
	public static final String cvUME = "UM-E"; // GO
	public static final String cvUMP = "UM-P"; // GO
	public static final String cvREAC = "REAC"; // GO
	public static final String cvTX = "TX"; // GO
	public static final String casCV = "CAS"; // Chebi
	public static final String chemPDBCV = "MSDchem"; // Chebi
	public static final String PDB = "PDB"; // Chebi
	public static final String KEGG = "KEGG"; // Chebi
	public static final String cvCL = "CL"; // PO
	public static final String msdChem = "MSDchem";
	
	// relType
	public static final String is_a = "is_a"; // Generic
	public static final String is_p = "part_of"; // Generic
	public static final String dev = "dev"; // Generic
	public static final String rtRp_by = "rp_by";
	public static final String rtRegulates = "regulates";
	public static final String rtPos_reg = "pos_reg";
	public static final String rtNeg_reg = "neg_reg";
	public static final String sensu = "sensu"; // GO
	public static final String isChemSubGrFrom = "is_chem_sub_gr_from"; // Chebi
	public static final String isConjugate = "is_conjugate"; // Chebi
	public static final String isEnantiomer = "is_enantiomer_of"; // Chebi
	public static final String isTautomerOf = "is_tautomer_of"; // Chebi
	public static final String hasChemFunctionalParent = "has_chem_f_p"; // Chebi
	public static final String hasChemParentHybride = "h_chem_p_hybride"; // Chebi
	public static final String hasFunction = "has_function"; // Chebi

	public final static String rtAdjacentTo = "adjacent_to"; // PO
	public final static String rtDevelopsFrom = "develops_from"; // PO
	public final static String rtHasPart = "has_part"; // PO
	public final static String rtDerivesFrom = "derives_from"; // PO
	public final static String rtParticipatesIn = "participates_in"; // PO
	public final static String rtHasParticipant = "has_participant"; // PO
}
