package net.sourceforge.ondex.tools.data;

import net.sourceforge.ondex.config.OndexJAXBContextRegistry;

public class ChemicalStructure implements Comparable<ChemicalStructure> {

	static {
		// comes with its own marshaller etc
		OndexJAXBContextRegistry jaxbRegistry = OndexJAXBContextRegistry
				.instance();
		jaxbRegistry.addClassBindings(ChemicalStructureHolder.class);
		jaxbRegistry.addHolder(ChemicalStructure.class,
				ChemicalStructureHolder.class);
	}

	private String MOL = "";

	private String SMILES = "";

	public ChemicalStructure() {
		// empty for now
	}

	public String getMOL() {
		return MOL;
	}

	public String getSMILES() {
		return SMILES;
	}

	public void setMOL(String mOL) {
		MOL = mOL;
	}

	public void setSMILES(String sMILES) {
		SMILES = sMILES;
	}

	@Override
	public String toString() {
		if (SMILES != null && SMILES.length() > 0)
			return SMILES;
		else if (MOL != null && MOL.length() > 0) {
			String[] split = MOL.trim().split("\n");
			return split[0];
		}
		return "SMILES and MOL empty";
	}

	@Override
	public int compareTo(ChemicalStructure o) {
		if (o instanceof ChemicalStructure) {
			ChemicalStructure cs = (ChemicalStructure) o;
			if (SMILES != null && SMILES.length() > 0 && cs.SMILES != null
					&& cs.SMILES.length() > 0) {
				return cs.SMILES.compareTo(SMILES);
			} else if (MOL != null && MOL.length() > 0 && cs.MOL != null
					&& cs.MOL.length() > 0) {
				return cs.MOL.compareTo(MOL);
			} else
				return 0;
		} else
			return 0;
	}
}
