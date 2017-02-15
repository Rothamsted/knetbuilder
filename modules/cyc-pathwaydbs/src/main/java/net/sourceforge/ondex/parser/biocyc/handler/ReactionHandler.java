package net.sourceforge.ondex.parser.biocyc.handler;

import java.util.Set;

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.exception.type.DataSourceMissingException;
import net.sourceforge.ondex.exception.type.ConceptClassMissingException;
import net.sourceforge.ondex.exception.type.EvidenceTypeMissingException;
import net.sourceforge.ondex.exception.type.RelationTypeMissingException;

import org.biopax.paxtools.model.level2.biochemicalReaction;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;

/**
 * Translates biochemicalReaction entities into concepts.
 * 
 * @author taubertj
 * 
 */
public class ReactionHandler extends DefaultHandler {

	/**
	 * Processes set of biochemicalReaction and creates concepts in given graph.
	 * 
	 * @param g
	 *            ONDEXGraph to create concepts in
	 * @param reactions
	 *            Set of biochemicalReaction
	 */
	public ReactionHandler(ONDEXGraph g, Set<biochemicalReaction> reactions)
			throws Exception {
		this.graph = g;
		for (biochemicalReaction r : reactions)
			processReaction(r);
	}

	/**
	 * Translate given biochemicalReaction into concept.
	 * 
	 * @param r
	 *            biochemicalReaction to translate
	 */
	private void processReaction(biochemicalReaction r) throws Exception {

		// check for evidence type
		EvidenceType evidence = graph.getMetaData().getEvidenceType(etIMPD);
		if (evidence == null)
			throw new EvidenceTypeMissingException(etIMPD + " is missing.");

		// check for concept class
		ConceptClass ofType = graph.getMetaData().getConceptClass(ccReaction);
		if (ofType == null)
			throw new ConceptClassMissingException(ccReaction + " is missing.");

		// extract DataSource of entity
		DataSource elementOf = getDataSource(r);

		// create concept
		ONDEXConcept c = graph.getFactory().createConcept(r.getRDFId(),
				elementOf, ofType, evidence);
		rdf2Concept.put(r.getRDFId(), c);

		// add synonyms
		addConceptNames(c, r);

		// add description
		addDescription(c, r);

		// add accessions
		addConceptAccessions(c, r);

		// a reaction can have an EC number
		if (!r.getEC_NUMBER().isEmpty()) {

			for (String ec : r.getEC_NUMBER()) {

				if (!rdf2Concept.containsKey(ec)) {
					// concept class for EC number
					ofType = graph.getMetaData().getConceptClass(ccEC);
					if (ofType == null)
						throw new ConceptClassMissingException(ccEC
								+ " is missing.");

					// DataSource for EC number accession
					DataSource ecElementOf = graph.getMetaData().getDataSource(cvEC);
					if (ecElementOf == null)
						throw new DataSourceMissingException(cvEC + " is missing.");

					// create concept for EC entry with accession and name
					ONDEXConcept ecC = graph.getFactory().createConcept(ec,
							elementOf, ofType, evidence);
					ecC.createConceptAccession(ec, ecElementOf, false);
					ecC.createConceptName(ec, true);
					rdf2Concept.put(ec, ecC);
				}
				ONDEXConcept ecC = rdf2Concept.get(ec);

				// new relation between reaction and EC class
				RelationType rt = graph.getMetaData().getRelationType(
						rtCatalysingClass);
				if (rt == null)
					throw new RelationTypeMissingException(rtCatalysingClass
							+ " is missing.");
				graph.getFactory().createRelation(c, ecC, rt, evidence);
			}
		}

		// what is consumed
		for (physicalEntityParticipant left : r.getLEFT()) {
			String rdfID = left.getPHYSICAL_ENTITY().getRDFId();
			ONDEXConcept from = rdf2Concept.get(rdfID);
			if (from == null)
				System.err.println("Missing concept " + rdfID);
			else {
				RelationType rt = graph.getMetaData().getRelationType(
						rtConsumedBy);
				if (rt == null)
					throw new RelationTypeMissingException(rtConsumedBy
							+ " is missing.");
				graph.getFactory().createRelation(from, c, rt, evidence);
			}
		}

		// what is produced
		for (physicalEntityParticipant right : r.getRIGHT()) {
			String rdfID = right.getPHYSICAL_ENTITY().getRDFId();
			ONDEXConcept from = rdf2Concept.get(rdfID);
			if (from == null)
				System.err.println("Missing concept " + rdfID);
			else {
				RelationType rt = graph.getMetaData().getRelationType(
						rtProducedBy);
				if (rt == null)
					throw new RelationTypeMissingException(rtProducedBy
							+ " is missing.");
				graph.getFactory().createRelation(from, c, rt, evidence);
			}
		}
	}

}
