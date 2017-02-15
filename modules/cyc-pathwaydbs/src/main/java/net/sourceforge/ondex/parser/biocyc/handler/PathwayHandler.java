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

import org.biopax.paxtools.model.level2.control;
import org.biopax.paxtools.model.level2.modulation;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.pathwayComponent;
import org.biopax.paxtools.model.level2.pathwayStep;
import org.biopax.paxtools.model.level2.process;

/**
 * Translates pathway entities into concepts.
 * 
 * @author taubertj
 * 
 */
public class PathwayHandler extends DefaultHandler {

	/**
	 * Processes set of pathways and creates concepts in given graph.
	 * 
	 * @param g
	 *            ONDEXGraph to create concepts in
	 * @param pathways
	 *            Set of pathways
	 */
	public PathwayHandler(ONDEXGraph g, Set<pathway> pathways) throws Exception {
		this.graph = g;
		for (pathway p : pathways)
			processPathway(p);
	}

	/**
	 * Translate given pathway into concept.
	 * 
	 * @param p
	 *            pathway to translate
	 */
	private void processPathway(pathway p) throws Exception {

		// check for evidence type
		EvidenceType evidence = graph.getMetaData().getEvidenceType(etIMPD);
		if (evidence == null)
			throw new EvidenceTypeMissingException(etIMPD + " is missing.");

		// check for concept class
		ConceptClass ofType = graph.getMetaData().getConceptClass(ccPath);
		if (ofType == null)
			throw new ConceptClassMissingException(ccPath + " is missing.");

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

		// create relation between pathway and reactions etc
		for (pathwayComponent component : p.getPATHWAY_COMPONENTS()) {
			if (component instanceof pathwayStep) {
				pathwayStep step = (pathwayStep) component;
				for (process process : step.getSTEP_INTERACTIONS()) {
					String rdfID = process.getRDFId();
					// ignore modulation and control here, as expressed via
					// relation
					if (!(process instanceof modulation)
							&& !(process instanceof control)) {
						ONDEXConcept from = rdf2Concept.get(rdfID);
						if (from == null) {
							// nested pathways
							if (process instanceof pathway) {
								processPathway((pathway) process);
								from = rdf2Concept.get(rdfID);
							} else
								System.err.println("Missing concept " + rdfID);
						} else {
							RelationType rt = graph.getMetaData()
									.getRelationType(rtPartOf);
							if (rt == null)
								throw new RelationTypeMissingException(rtPartOf
										+ " is missing.");
							graph.getFactory().createRelation(from, c, rt,
									evidence);
						}
					}
				}
			} else
				System.err.println("Unsupported pathwayComponent: "
						+ component.getRDFId());
		}

	}

}
