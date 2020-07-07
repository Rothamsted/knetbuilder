/*
 * Created on 26-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg56.sink;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.exception.type.AttributeNameMissingException;
import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;

/**
 * Class for writing sequences to Attribute
 * 
 * @author taubertj
 */
public class SequenceWriter {

	public Map<String, AttributeName> attNames = new HashMap<String, AttributeName>();

	// previously written concepts
	private ConceptWriter cw;

	// wrapped ONDEX graph
	private ONDEXGraph graph;

	/**
	 * Constructor to set internal fields.
	 * 
	 * @param og
	 *            ONDEXGraph
	 * @param cw
	 *            ConceptWriter
	 */
	public SequenceWriter(ONDEXGraph og, ConceptWriter cw) {
		this.cw = cw;
		this.graph = og;
	}

	/**
	 * Adds a sequence Attribute to a concept
	 * 
	 * @param seq
	 *            sequence prototype to use
	 * @throws MetaDataMissingException
	 * @throws InconsistencyException
	 */
	public void createSequence(Sequence seq) throws MetaDataMissingException,
			InconsistencyException {

		// get concept from graph first
		Integer cid = cw.getWrittenConceptId(seq.getConcept_fk());
		ONDEXConcept ac = graph.getConcept(cid);
		if (ac == null)
			throw new NullPointerException("Concept is null ");

		// check attribute name exists
		AttributeName an = attNames.get(seq.getSequence_type_fk());
		if (an == null) {
			an = graph.getMetaData()
					.getAttributeName(seq.getSequence_type_fk());
			if (an == null)
				throw new AttributeNameMissingException(seq
						.getSequence_type_fk());
			attNames.put(seq.getSequence_type_fk(), an);
		}

		// create Attribute on concept
		ac.createAttribute(an, seq.getSeq(), false);
	}
}
