package net.sourceforge.ondex.parser.biocyc.handler;

import java.util.Set;

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.exception.type.ConceptClassMissingException;
import net.sourceforge.ondex.exception.type.EvidenceTypeMissingException;
import net.sourceforge.ondex.exception.type.RelationTypeMissingException;

import org.biopax.paxtools.model.level2.complex;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;

/**
 * Translates complexes entities into concepts.
 * 
 * @author taubertj
 * 
 */
public class ComplexHandler extends DefaultHandler {

	/**
	 * Processes set of complexes and creates concepts in given graph.
	 * 
	 * @param g
	 *            ONDEXGraph to create concepts in
	 * @param complexes
	 *            Set of complexes
	 */
	public ComplexHandler(ONDEXGraph g, Set<complex> complexes)
			throws Exception {
		this.graph = g;
		for (complex p : complexes)
			processComplex(p);
	}

	/**
	 * Translate given complex into concept.
	 * 
	 * @param cmplx
	 *            complex to translate
	 */
	private void processComplex(complex cmplx) throws Exception {

		// check for evidence type
		EvidenceType evidence = graph.getMetaData().getEvidenceType(etIMPD);
		if (evidence == null)
			throw new EvidenceTypeMissingException(etIMPD + " is missing.");

		// check for concept class
		ConceptClass ofType = graph.getMetaData().getConceptClass(ccComplex);
		if (ofType == null)
			throw new ConceptClassMissingException(ccComplex + " is missing.");

		// extract DataSource of entity
		DataSource elementOf = getDataSource(cmplx);

		// create concept
		ONDEXConcept c = graph.getFactory().createConcept(cmplx.getRDFId(),
				elementOf, ofType, evidence);
		rdf2Concept.put(cmplx.getRDFId(), c);

		// add synonyms
		addConceptNames(c, cmplx);

		// add description
		addDescription(c, cmplx);

		// add references
		addConceptAccessions(c, cmplx);

		// add organism TAXID
		addOrganism(c, cmplx);

		// member relationships to complex
		for (physicalEntityParticipant part : cmplx.getCOMPONENTS()) {
			String rdfID = part.getPHYSICAL_ENTITY().getRDFId();
			ONDEXConcept from = rdf2Concept.get(rdfID);
			if (from == null)
				System.err.println("Missing concept " + rdfID);
			else {
				RelationType rt = graph.getMetaData().getRelationType(
						rtIsPartOf);
				if (rt == null)
					throw new RelationTypeMissingException(rtIsPartOf
							+ " is missing.");
				graph.getFactory().createRelation(from, c, rt, evidence);
			}
		}
	}

}
