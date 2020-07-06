package net.sourceforge.ondex.ovtk2.util;

import java.util.Set;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;

public class ConceptListUtils {

	/**
	 * create a name for concepts
	 * 
	 * @param c
	 * @return
	 */
	public static String getDefaultNameForConcept(ONDEXConcept c) {
		String name = null;
		ConceptName cn = c.getConceptName();

		Set<ConceptAccession> accs = c.getConceptAccessions();

		if (cn != null && cn.getName().trim().length() > 0)
			name = cn.getName().trim();
		else if (accs.size() > 0)
			for (ConceptAccession acc : accs) {
				if (acc.getAccession().trim().length() > 0) {
					if (acc.getElementOf().equals(c.getElementOf())) {
						// prefer native accession
						name = acc.getAccession().trim();
						break;
					}
					name = acc.getAccession().trim();
				}

			}
		else if (c.getAnnotation().length() > 0)
			name = c.getAnnotation().trim();
		else if (c.getDescription().length() > 0)
			name = c.getDescription().trim();
		else
			name = c.getId() + " - n/a";

		return name;
	}

}
