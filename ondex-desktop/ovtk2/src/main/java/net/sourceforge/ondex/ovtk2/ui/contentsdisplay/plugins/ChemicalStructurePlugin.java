package net.sourceforge.ondex.ovtk2.ui.contentsdisplay.plugins;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AttributePlugin;
import net.sourceforge.ondex.tools.data.ChemicalStructure;

/**
 * Display SMILES string from chemical structure attributes in content info
 * 
 * @author taubertj
 * 
 */
public class ChemicalStructurePlugin extends AbstractContentDisplayPlugin implements AttributePlugin {

	public ChemicalStructurePlugin(ONDEXGraph aog) {
		super(aog);
	}

	@Override
	public String[] getAttributeNames() {
		return new String[] { "ChemicalStructure" };
	}

	@Override
	public String getName() {
		return "chemicalstructure";
	}

	@Override
	public String getVersion() {
		return "08.09.2011";
	}

	@Override
	public String compileContent(ONDEXEntity e) {
		StringBuffer b = new StringBuffer();

		// check for attribute only on concepts
		if (e instanceof ONDEXConcept) {
			AttributeName attrName = aog.getMetaData().getAttributeName("ChemicalStructure");
			if (attrName != null) {
				Attribute g = e.getAttribute(attrName);
				if (g != null) {
					ChemicalStructure cs = (ChemicalStructure) g.getValue();
					// extract SMILES from chemical structure
					if (cs.getSMILES() != null && cs.getSMILES().length() > 0) {
						b.append("<h2>SMILES</h2>");
						b.append(cs.getSMILES());
					}
					// In the future maybe include JChemPaint here
				}
			}
		}

		return b.toString();
	}

}
