/*
 * Created on 12-May-2005
 *
 */
package net.sourceforge.ondex.parser.kegg56.util;

import com.sleepycat.persist.EntityCursor;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.parser.kegg56.GenomeParser;
import net.sourceforge.ondex.parser.kegg56.sink.*;

import java.util.*;

/**
 * @author taubertj
 */
public class Util {

	private final ConceptWriter cw;
	private final RelationWriter rw;
	private final SequenceWriter sw;

	private final Set<String> element_of;
	private final GenomeParser genomeParser;

	public Util(ONDEXGraph og, GenomeParser genomeParser) {
		this.genomeParser = genomeParser;
		element_of = Collections.synchronizedSet(new HashSet<String>());
		cw = new ConceptWriter(og);
		rw = new RelationWriter(og, cw);
		sw = new SequenceWriter(og, cw);
	}

	/**
	 * @param concept
	 *            kegg concept to write
	 * @param speciesSpecific
	 *            contains a species in the id
	 * @throws MetaDataMissingException
	 * @throws InconsistencyException
	 */
	public void writeConcept(Concept concept, boolean speciesSpecific)
			throws MetaDataMissingException, InconsistencyException {
		cw.createConcept(concept, genomeParser, speciesSpecific);
	}

	/**
	 * @param concept
	 *            kegg concept to write
	 * @throws MetaDataMissingException
	 * @throws InconsistencyException
	 */
	public void writeConcept(Concept concept) throws MetaDataMissingException,
			InconsistencyException {
		cw.createConcept(concept, genomeParser, true);
	}

	public Set<String> getClashedElementOf() {
		return element_of;
	}

	/**
	 * @param conceptsToWrite
	 *            kegg concepts to write
	 * @param speciesSpecific
	 *            contains a species in the id
	 * @throws MetaDataMissingException
	 * @throws InconsistencyException
	 */
	public void writeConcepts(Collection<Concept> conceptsToWrite,
			boolean speciesSpecific) throws MetaDataMissingException,
			InconsistencyException {
		for (Concept concept : conceptsToWrite) {
			writeConcept(concept, speciesSpecific);
		}
	}

	/**
	 * @param conceptsToWrite
	 *            kegg concepts to write
	 * @throws MetaDataMissingException
	 * @throws InconsistencyException
	 */
	public void writeConcepts(Collection<Concept> conceptsToWrite)
			throws MetaDataMissingException, InconsistencyException {
		writeConcepts(conceptsToWrite, true);
	}

	/**
	 * @param seqs
	 * @return jobID
	 * @throws InconsistencyException
	 */
	public void writeSequences(DPLPersistantSet<Sequence> seqs)
			throws MetaDataMissingException, InconsistencyException {
		EntityCursor<Sequence> cursor = seqs.getCursor();
		try {
			for (Sequence seq : cursor) {
				sw.createSequence(seq);
			}
		} finally {
			seqs.closeCursor(cursor);
		}
	}

	/**
	 * @param relationsToWrite
	 * @throws InconsistencyException
	 */
	public void writeRelations(DPLPersistantSet<Relation> relationsToWrite)
			throws MetaDataMissingException, InconsistencyException {
		EntityCursor<Relation> cursor = relationsToWrite.getCursor();
		try {
			for (Relation relation : cursor) {
				writeRelation(relation);
			}
		} finally {
			relationsToWrite.closeCursor(cursor);
		}
	}

	/**
	 * @param relationsToWrite
	 * @throws InconsistencyException
	 */
	public void writeRelations(ArrayList<Relation> relationsToWrite)
			throws MetaDataMissingException, InconsistencyException {
		for (Relation relation : relationsToWrite) {
			writeRelation(relation);
		}
	}

	public void writeRelation(Relation relation)
			throws MetaDataMissingException, InconsistencyException {
		rw.createRelation(relation);
	}

	public ConceptWriter getCw() {
		return cw;
	}

	public RelationWriter getRw() {
		return rw;
	}

	public SequenceWriter getSw() {
		return sw;
	}

}
