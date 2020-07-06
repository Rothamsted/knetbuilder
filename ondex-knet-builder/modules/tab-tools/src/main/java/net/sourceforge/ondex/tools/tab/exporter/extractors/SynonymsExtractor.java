package net.sourceforge.ondex.tools.tab.exporter.extractors;

import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.base.AbstractConcept;
import net.sourceforge.ondex.tools.tab.exporter.InvalidOndexEntityException;

public class SynonymsExtractor implements AttributeExtractor {
	public String getValue(ONDEXEntity cOrr) throws InvalidOndexEntityException {
		if (AbstractConcept.class.isAssignableFrom(cOrr.getClass())) {
			StringBuffer sb = new StringBuffer();
			boolean hasOneEntry = false;
			// first line up all preferred names
			for (ConceptName cn : ((ONDEXConcept) cOrr).getConceptNames()) {
				if (cn != null) {
					String tmp = cn.getName().trim();
					if (tmp.length() > 0 && cn.isPreferred()) {
						if (hasOneEntry) {
							sb.append("|");
						} else {
							hasOneEntry = true;
						}
						sb.append(cn.getName());
					}
				}
			}
			// all non-preferred names at the end
			for (ConceptName cn : ((ONDEXConcept) cOrr).getConceptNames()) {
				if (cn != null) {
					String tmp = cn.getName().trim();
					if (tmp.length() > 0 && !cn.isPreferred()) {
						if (hasOneEntry) {
							sb.append("|");
						} else {
							hasOneEntry = true;
						}
						sb.append(cn.getName());
					}
				}
			}
			// just use PID as a fall-back
			String preferedName = sb.toString();
			if (preferedName.length() == 0) {
				preferedName = ((ONDEXConcept) cOrr).getPID();
			}
			return preferedName;
		}
		throw new InvalidOndexEntityException(cOrr.getClass()
				+ ": is not an Ondex concept class for which a name is known");

	}

	@Override
	public String getHeaderName() {
		return "Synonyms";
	}

}
