/*
 * Created on 26-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg56.sink;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.exception.type.RelationTypeMissingException;
import net.sourceforge.ondex.parser.kegg56.MetaData;

/**
 * Turns a KEGG Relation into a binary or ternary ONDEXRelation.
 * 
 * @author taubertj
 */
public class RelationWriter {

	// previously written concepts
	private ConceptWriter cw;

	// standard relation evidence type IMPD
	private EvidenceType evidenceType;

	// wrapped ONDEX graph
	private ONDEXGraph graph;

	// meta data of ONDEX graph
	private ONDEXGraphMetaData omd;

	// cache for relation types
	private Map<String, RelationType> rts = new HashMap<String, RelationType>(
			50);

	/**
	 * Constructor for current ONDEXGraph and ConceptWriter to track written
	 * concepts.
	 * 
	 * @param og
	 *            current ONDEXGraph
	 * @param cw
	 *            ConceptWritter with history of concepts
	 */
	public RelationWriter(ONDEXGraph og, ConceptWriter cw) {
		this.graph = og;
		this.cw = cw;
		this.omd = og.getMetaData();
	}

	/**
	 * Create a ONDEXRelation on the ONDEXGraph from the temporary Relation
	 * 
	 * @param relation
	 *            KEGG Relation
	 */
	public void createRelation(Relation relation)
			throws MetaDataMissingException, InconsistencyException {

		// deal with from concept from ONDEXGraph
		int fromId = cw.getWrittenConceptId(relation.getFrom_concept().trim());

		ONDEXConcept fromConcept = graph.getConcept(fromId);

		// deal with to concept from ONDEXGraph
		int toId = cw.getWrittenConceptId(relation.getTo_concept().trim());

		ONDEXConcept toConcept = graph.getConcept(toId);

		// get standard evidence type
		if (evidenceType == null)
			evidenceType = omd.getEvidenceType(MetaData.EVIDENCE_IMPD);

		// get cached relation type
		RelationType rt = rts.get(relation.getOf_type());
		if (rt == null) {
			rt = omd.getRelationType(relation.getOf_type());
			if (rt != null) {
				rts.put(relation.getOf_type(), rt);
			} else {
				throw new RelationTypeMissingException(relation.getOf_type());
			}
		}

		ONDEXRelation r = null;

		// at least from, to, rt and eviType has to be present
		if (fromConcept != null && toConcept != null && rt != null
				&& evidenceType != null) {

			// binary relation
			if (graph.getRelation(fromConcept, toConcept, rt) == null) {

				r = graph.getFactory().createRelation(fromConcept, toConcept,
						rt, evidenceType);
				if (r == null) {
					throw new InconsistencyException(
							"Unable to create relation " + fromConcept.getId()
									+ " " + toConcept.getId());
				}
			}
		} else {
			throw new InconsistencyException(
					"Missing one or more values for Relation writing in Kegg: "
							+ relation.getOf_type());
		}

		if (r != null) {
			// add relation context
			for (String context : relation.getContext()) {
				Integer existingId = cw.getWrittenConceptId(context);
				if (existingId != null) {
					// when adding context, make sure context is on all concepts
					r.addTag(graph.getConcept(existingId));
					r.getFromConcept().addTag(graph.getConcept(existingId));
					r.getToConcept().addTag(graph.getConcept(existingId));
				} else {
					throw new InconsistencyException("|"
							+ context.toUpperCase() + "| Context not found");
				}
			}
		}
	}
}
