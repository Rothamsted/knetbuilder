package net.sourceforge.ondex.parser.biocyc.handler;

import java.util.Set;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.exception.type.AttributeNameMissingException;
import net.sourceforge.ondex.exception.type.ConceptClassMissingException;
import net.sourceforge.ondex.exception.type.EvidenceTypeMissingException;

import org.biopax.paxtools.model.level2.protein;

/**
 * Translates proteins entities into concepts.
 * 
 * @author taubertj
 * 
 */
public class ProteinHandler extends DefaultHandler {

	/**
	 * Processes set of proteins and creates concepts in given graph.
	 * 
	 * @param g
	 *            ONDEXGraph to create concepts in
	 * @param proteins
	 *            Set of proteins
	 */
	public ProteinHandler(ONDEXGraph g, Set<protein> proteins) throws Exception {
		this.graph = g;
		for (protein p : proteins)
			processProtein(p);
	}

	/**
	 * Translate given protein into concept.
	 * 
	 * @param p
	 *            protein to translate
	 */
	private void processProtein(protein p) throws Exception {

		// check for evidence type
		EvidenceType evidence = graph.getMetaData().getEvidenceType(etIMPD);
		if (evidence == null)
			throw new EvidenceTypeMissingException(etIMPD + " is missing.");

		// check for concept class
		ConceptClass ofType = graph.getMetaData().getConceptClass(ccProtein);
		if (ofType == null)
			throw new ConceptClassMissingException(ccProtein + " is missing.");

		// extract DataSource of entity
		DataSource elementOf = getDataSource(p);

		// create concept
		ONDEXConcept c = graph.getFactory().createConcept(p.getRDFId(),
				elementOf, ofType, evidence);
		rdf2Concept.put(p.getRDFId(), c);

		// add synonyms
		addConceptNames(c, p);

		// add description
		addDescription(c, p);

		// add references
		addConceptAccessions(c, p);

		// add organism TAXID
		addOrganism(c, p);
		
		// possible sequence on protein
		if (p.getSEQUENCE() != null) {
			AttributeName an = graph.getMetaData().getAttributeName(anAA);
			if (an == null)
				throw new AttributeNameMissingException(anAA + " is missing.");
			c.createAttribute(an, p.getSEQUENCE(), false);
		}
	}

}
