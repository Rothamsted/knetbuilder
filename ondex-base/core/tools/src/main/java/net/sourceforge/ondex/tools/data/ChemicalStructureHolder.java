package net.sourceforge.ondex.tools.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import net.sourceforge.ondex.core.util.Holder;

/**
 * Utility bean to wrap up ChemicalStructure.
 * 
 * @author taubertj
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "chemicalStructure")
public class ChemicalStructureHolder implements Holder<ChemicalStructure> {

	/**
	 * Empty constructor for JAXB
	 */
	public ChemicalStructureHolder() {
	}

	@XmlAttribute
	private String smiles;
	
	@XmlAttribute
	private String mol;

	@Override
	public void setValue(ChemicalStructure cs) {
		this.smiles = cs.getSMILES();
		this.mol = cs.getMOL();
	}

	@Override
	public ChemicalStructure getValue() {
		ChemicalStructure cs = new ChemicalStructure();
		cs.setSMILES(smiles);
		cs.setMOL(mol);
		return cs;
	}
}
