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

import org.biopax.paxtools.model.level2.chemicalStructure;
import org.biopax.paxtools.model.level2.smallMolecule;

/**
 * Translates smallMolecules entities into concepts.
 * 
 * @author taubertj
 * 
 */
public class CompoundHandler extends DefaultHandler {

	/**
	 * Processes set of smallMolecules and creates concepts in given graph.
	 * 
	 * @param g
	 *            ONDEXGraph to create concepts in
	 * @param compounds
	 *            Set of smallMolecules
	 */
	public CompoundHandler(ONDEXGraph g, Set<smallMolecule> compounds)
			throws Exception {
		this.graph = g;
		for (smallMolecule s : compounds)
			processCompound(s);
	}

	/**
	 * Translate given smallMolecule into concept.
	 * 
	 * @param s
	 *            smallMolecule to translate
	 */
	private void processCompound(smallMolecule s) throws Exception {

		// check for evidence type
		EvidenceType evidence = graph.getMetaData().getEvidenceType(etIMPD);
		if (evidence == null)
			throw new EvidenceTypeMissingException(etIMPD + " is missing.");

		// check for concept class
		ConceptClass ofType = graph.getMetaData().getConceptClass(ccCompound);
		if (ofType == null)
			throw new ConceptClassMissingException(ccCompound + " is missing.");

		// extract DataSource of entity
		DataSource elementOf = getDataSource(s);

		// create concept
		ONDEXConcept c = graph.getFactory().createConcept(s.getRDFId(),
				elementOf, ofType, evidence);
		rdf2Concept.put(s.getRDFId(), c);

		// add synonyms
		addConceptNames(c, s);

		// add description
		addDescription(c, s);

		// add references
		addConceptAccessions(c, s);

		// possible structure present
		if (!s.getSTRUCTURE().isEmpty()) {
			// take only first one
			chemicalStructure struct = s.getSTRUCTURE().iterator().next();

			// until now only support CML
			if (struct.getSTRUCTURE_FORMAT().equals("CML")) {
				AttributeName an = graph.getMetaData().getAttributeName(anCML);
				if (an == null)
					throw new AttributeNameMissingException(anCML
							+ " is missing.");
				String text = struct.getSTRUCTURE_DATA();
				text = fixElementType(text);
				c.createAttribute(an, text, false);
			} else
				System.err.println("Unknown structure format: "
						+ struct.getSTRUCTURE_FORMAT());
		}

		// add molecular weight
		if (s.getMOLECULAR_WEIGHT() > 0) {
			AttributeName an = graph.getMetaData().getAttributeName(anWEIGHT);
			if (an == null)
				throw new AttributeNameMissingException(anWEIGHT
						+ " is missing.");
			c.createAttribute(an, Double.valueOf(s.getMOLECULAR_WEIGHT()),
					false);
		}
	}

	/**
	 * Fix wrong annotations in element type from BioPAX
	 * 
	 * @param text
	 * @return
	 */
	private String fixElementType(String text) {
		String result = "";

		String[] split = text.split("elementType=");
		for (int i = 0; i < split.length; i++) {
			if (i == 0)
				result = split[i];
			else {
				// parse out all the meta stuff here
				String sub = split[i].substring(3, split[i].indexOf(" "));
				result = result + "elementType=\"" + sub
						+ split[i].substring(split[i].indexOf("\" x"));
			}
		}

		return result;
	}

}
