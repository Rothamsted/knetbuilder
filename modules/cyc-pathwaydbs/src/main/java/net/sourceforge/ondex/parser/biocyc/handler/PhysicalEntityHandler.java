package net.sourceforge.ondex.parser.biocyc.handler;

import java.util.Set;

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.exception.type.ConceptClassMissingException;
import net.sourceforge.ondex.exception.type.EvidenceTypeMissingException;

import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.protein;
import org.biopax.paxtools.model.level2.smallMolecule;

/**
 * Translates physicalEntitys entities into concepts.
 * 
 * @author taubertj
 * 
 */
public class PhysicalEntityHandler extends DefaultHandler {

	/**
	 * Processes set of physicalEntitys and creates concepts in given graph.
	 * 
	 * @param g
	 *            ONDEXGraph to create concepts in
	 * @param entities
	 *            Set of physicalEntitys
	 */
	public PhysicalEntityHandler(ONDEXGraph g, Set<physicalEntity> entities)
			throws Exception {
		this.graph = g;
		for (physicalEntity s : entities)
			processPhysicalEntity(s);
	}

	/**
	 * Translate given physicalEntity into concept.
	 * 
	 * @param e
	 *            physicalEntity to translate
	 */
	private void processPhysicalEntity(physicalEntity e) throws Exception {

		// check for evidence type
		EvidenceType evidence = graph.getMetaData().getEvidenceType(etIMPD);
		if (evidence == null)
			throw new EvidenceTypeMissingException(etIMPD + " is missing.");

		// check for concept classes
		ConceptClass ofType = graph.getMetaData().getConceptClass(ccThing);
		if (ofType == null)
			throw new ConceptClassMissingException(ccThing + " is missing.");

		// decide on type of physicalEntity
		if (!e.getModelInterface().equals(protein.class)
				&& !e.getModelInterface().equals(smallMolecule.class)) {

			// extract DataSource of entity
			DataSource elementOf = getDataSource(e);

			// create concept
			ONDEXConcept c = graph.getFactory().createConcept(e.getRDFId(),
					elementOf, ofType, evidence);
			rdf2Concept.put(e.getRDFId(), c);

			// add synonyms
			addConceptNames(c, e);

			// add description
			addDescription(c, e);

			// add references
			addConceptAccessions(c, e);
		}
	}

}
