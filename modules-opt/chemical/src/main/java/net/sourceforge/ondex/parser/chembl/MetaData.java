package net.sourceforge.ondex.parser.chembl;

import java.util.HashMap;
import java.util.Map;

public interface MetaData {

	// TODO: put attribute names into ondex_metadata.xml
	public static final String ATTR_CHARGE = "Charge";

	public static final String ATTR_CHEMICAL_STRUCTURE = "ChemicalStructure";

	public static final String ATTR_INCHI = "InChI";

	public static final String ATTR_INCHIKEY = "InChIKey";

	public static final String ATTR_LAST_MODIFIED = "LastModified";

	public static final String ATTR_MASS = "Mass";

	public static final String ATTR_STAR = "Star";

	public static final String CC_COMP = "Comp";

	public static final String DS_CHEMBL = "CHEMBL";

	public static final String DS_CHEBI = "CHEBI";

	public static final String ET_IMPD = "IMPD";

	@SuppressWarnings("serial")
	public static final Map<String, String> dataSourceMapping = new HashMap<String, String>() {
		{
			// TODO: add UC ones to ondex_metadata.xml
			put("DrugBank Database Links", "DRUGBANK");
			put("Beilstein Registry Numbers", "UC");
			put("LIPID MAPS instance Database Links", "LIPIDMAPS");
			put("Rhea Database Links", "RHEA");
			put("BioModels Database Links", "UC");
			put("Gmelin Registry Numbers", "UC");
			put("Reactome Database Links", "REAC");
			put("KEGG COMPOUND Database Links", "KEGG");
			put("UniProt Database Links", "UNIPROTKB");
			put("Patent Database Links", "UC");
			put("CAS Registry Numbers", "CAS");
			put("PubChem Database Links", "PUBCHEM");
			put("SABIO-RK Database Links", "UC");
			put("IntAct Database Links", "INTACT");
			put("IntEnz Database Links", "EC");
			put("KEGG DRUG Database Links", "KEGG");
		}
	};

}
