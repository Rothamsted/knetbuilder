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

import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.transport;

/**
 * Translates transport entities into concepts.
 * 
 * @author taubertj
 * 
 */
public class TransportHandler extends DefaultHandler {

	/**
	 * Processes set of transport and creates concepts in given graph.
	 * 
	 * @param g
	 *            ONDEXGraph to create concepts in
	 * @param transports
	 *            Set of transport
	 */
	public TransportHandler(ONDEXGraph g, Set<transport> transports)
			throws Exception {
		this.graph = g;
		for (transport t : transports)
			processTransport(t);
	}

	/**
	 * Translate given transport into concept.
	 * 
	 * @param t
	 *            transport to translate
	 */
	private void processTransport(transport t) throws Exception {

		// check for evidence type
		EvidenceType evidence = graph.getMetaData().getEvidenceType(etIMPD);
		if (evidence == null)
			throw new EvidenceTypeMissingException(etIMPD + " is missing.");

		// check for concept class
		ConceptClass ofType = graph.getMetaData().getConceptClass(ccTransport);
		if (ofType == null)
			throw new ConceptClassMissingException(ccTransport + " is missing.");

		// extract DataSource of entity
		DataSource elementOf = getDataSource(t);

		// create concept
		ONDEXConcept c = graph.getFactory().createConcept(t.getRDFId(),
				elementOf, ofType, evidence);
		rdf2Concept.put(t.getRDFId(), c);

		// add synonyms
		addConceptNames(c, t);

		// add description
		addDescription(c, t);

		// add accessions
		addConceptAccessions(c, t);

		// what is consumed
		for (physicalEntityParticipant left : t.getLEFT()) {
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
		for (physicalEntityParticipant right : t.getRIGHT()) {
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
