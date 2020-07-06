package net.sourceforge.ondex.tools.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import net.sourceforge.ondex.core.util.Holder;

/**
 * Utility bean to wrap up protein3dStructure.
 * 
 * @author taubertj
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "protein3dStructure")
public class Protein3dStructureHolder implements Holder<Protein3dStructure> {

	/**
	 * Empty constructor for JAXB
	 */
	public Protein3dStructureHolder() {
	}

	@XmlAttribute
	private String accessionNr;

	@Override
	public void setValue(Protein3dStructure p) {
		this.accessionNr = p.getAccessionNr();
	}

	@Override
	public Protein3dStructure getValue() {
		Protein3dStructure p = new Protein3dStructure();
		p.setAccessionNr(accessionNr);
		return p;
	}

}
