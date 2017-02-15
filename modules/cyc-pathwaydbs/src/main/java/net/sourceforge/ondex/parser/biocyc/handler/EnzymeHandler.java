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

import org.biopax.paxtools.model.level2.catalysis;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.process;

/**
 * Translates catalysis entities into concepts.
 * 
 * @author taubertj
 * 
 */
public class EnzymeHandler extends DefaultHandler {

	/**
	 * Processes set of catalysis and creates concepts in given graph.
	 * 
	 * @param g
	 *            ONDEXGraph to create concepts in
	 * @param catalysis
	 *            Set of catalysis
	 */
	public EnzymeHandler(ONDEXGraph g, Set<catalysis> catalysis)
			throws Exception {
		this.graph = g;
		for (catalysis r : catalysis)
			processCatalysis(r);
	}

	/**
	 * Translate given catalysis into concept.
	 * 
	 * @param cat
	 *            catalysis to translate
	 */
	private void processCatalysis(catalysis cat) throws Exception {

		// check for evidence type
		EvidenceType evidence = graph.getMetaData().getEvidenceType(etIMPD);
		if (evidence == null)
			throw new EvidenceTypeMissingException(etIMPD + " is missing.");

		// check for concept class
		ConceptClass ofType = graph.getMetaData().getConceptClass(ccEnzyme);
		if (ofType == null)
			throw new ConceptClassMissingException(ccEnzyme + " is missing.");

		// check for relation types
		RelationType isA = graph.getMetaData().getRelationType(rtIsA);
		if (isA == null)
			throw new RelationTypeMissingException(rtIsA + " is missing.");

		RelationType caBy = graph.getMetaData().getRelationType(rtCatalysedBy);
		if (caBy == null)
			throw new RelationTypeMissingException(rtCatalysedBy
					+ " is missing.");

		// extract DataSource of entity
		DataSource elementOf = getDataSource(cat);

		// create concept
		ONDEXConcept c = graph.getFactory().createConcept(cat.getRDFId(),
				elementOf, ofType, evidence);
		rdf2Concept.put(cat.getRDFId(), c);

		// add synonyms
		addConceptNames(c, cat);

		// add references
		addConceptAccessions(c, cat);

		// add comments
		addDescription(c, cat);

		// enzymes catalysing reaction
		for (physicalEntityParticipant controller : cat.getCONTROLLER()) {

			// this is usually the enzyme
			physicalEntity entity = controller.getPHYSICAL_ENTITY();
			
			// is_a relation between enzyme and protein
			ONDEXConcept from = rdf2Concept.get(entity.getRDFId());
			if (from == null)
				System.err.println("Missing concept " + entity.getRDFId());
			else {
				graph.getFactory().createRelation(from, c, isA, evidence);
			}

			// ca_by relation between enzyme and reaction
			for (process process : cat.getCONTROLLED()) {
				ONDEXConcept reaction = rdf2Concept.get(process.getRDFId());
				if (reaction == null)
					System.err.println("Missing concept " + process.getRDFId());
				else {
					graph.getFactory().createRelation(reaction, c, caBy,
							evidence);
				}
			}
		}

	}

}
