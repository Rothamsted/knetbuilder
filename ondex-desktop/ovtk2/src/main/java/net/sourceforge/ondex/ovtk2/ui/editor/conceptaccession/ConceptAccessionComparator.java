package net.sourceforge.ondex.ovtk2.ui.editor.conceptaccession;

import java.util.Comparator;

import net.sourceforge.ondex.core.ConceptAccession;

/**
 * Comparator for concept accessions. First sorting according to DataSource id,
 * than by ambiguity and last by accession.
 * 
 * @author taubertj
 * 
 */
public class ConceptAccessionComparator implements Comparator<ConceptAccession> {

	@Override
	public int compare(ConceptAccession o1, ConceptAccession o2) {
		// within same DataSource sort first by ambiguity, than by accession
		if (o1.getElementOf().equals(o2.getElementOf())) {
			if (!o1.isAmbiguous() && o2.isAmbiguous())
				return -1;
			else if (o1.isAmbiguous() && !o2.isAmbiguous())
				return 1;
			else
				return o1.getAccession().compareTo(o2.getAccession());
		} else {
			// sort according to DataSource
			return o1.getElementOf().getId().compareTo(o2.getElementOf().getId());
		}
	}

}
