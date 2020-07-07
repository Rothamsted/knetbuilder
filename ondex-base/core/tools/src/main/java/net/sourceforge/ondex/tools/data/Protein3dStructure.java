package net.sourceforge.ondex.tools.data;

import net.sourceforge.ondex.config.OndexJAXBContextRegistry;

/**
 * 
 * @author peschr, taubertj
 */
public class Protein3dStructure implements Comparable<Protein3dStructure> {

	static {
		// comes with its own marshaller etc
		OndexJAXBContextRegistry jaxbRegistry = OndexJAXBContextRegistry
				.instance();
		jaxbRegistry.addClassBindings(Protein3dStructureHolder.class);
		jaxbRegistry.addHolder(Protein3dStructure.class,
				Protein3dStructureHolder.class);
	}

	private String accessionNr;

	public Protein3dStructure() {
		// empty for now
	}

	public String getAccessionNr() {
		return accessionNr;
	}

	public void setAccessionNr(String accessionNr) {
		this.accessionNr = accessionNr;
	}

	public String toString() {
		return getAccessionNr();
	}

	@Override
	public int compareTo(Protein3dStructure o) {
		if (o instanceof Protein3dStructure) {
			Protein3dStructure ps = (Protein3dStructure) o;
			if (accessionNr != null && accessionNr.length() > 0
					&& ps.accessionNr != null && ps.accessionNr.length() > 0) {
				return ps.accessionNr.compareTo(accessionNr);
			} else
				return 0;
		} else
			return 0;
	}
}
