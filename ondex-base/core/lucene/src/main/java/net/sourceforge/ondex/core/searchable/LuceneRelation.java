package net.sourceforge.ondex.core.searchable;

import java.util.Set;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.base.AbstractRelation;

/**
 * This class represents a wrapper implementation of the ONDEXRelation.
 * 
 * @author taubertj
 * 
 */
public class LuceneRelation extends AbstractRelation {

	// relation to wrap
	private ONDEXRelation parent;

	/**
	 * Constructor wraps a given ONDEXRelation.
	 * 
	 * @param sid
	 *            unique id
	 * @param aog
	 *            LuceneONDEXGraph
	 * @param r
	 *            ONDEXRelation
	 */
	LuceneRelation(long sid, LuceneONDEXGraph aog, ONDEXRelation r) {
		this(sid, aog, r.getId(), r.getFromConcept(), r.getToConcept(), r
				.getOfType());
		this.parent = r;
	}

	/**
	 * Overridden constructor for super class.
	 * 
	 * @param sid
	 *            unique id
	 * @param aog
	 *            LuceneONDEXGraph
	 * @param id
	 *            Integer
	 * @param fromConcept
	 *            from Concept
	 * @param toConcept
	 *            to Concept
	 * @param ofType
	 *            RelationType
	 */
	LuceneRelation(long sid, LuceneONDEXGraph aog, int id,
			ONDEXConcept fromConcept, ONDEXConcept toConcept,
			RelationType ofType) {
		// TODO: optimise similar to LuceneConcept
		super(sid, id, fromConcept, toConcept, ofType);
	}

	@Override
	protected void saveEvidenceType(EvidenceType evidenceType)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean dropEvidenceType(EvidenceType evidenceType)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Set<EvidenceType> retrieveEvidenceTypeAll() {
		return parent.getEvidence();
	}

	@Override
	protected Attribute storeRelationAttribute(Attribute attribute)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean removeRelationAttribute(AttributeName attributeName)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Attribute retrieveRelationAttribute(AttributeName attributeName) {
		return parent.getAttribute(attributeName);
	}

	@Override
	protected Set<Attribute> retrieveRelationAttributeAll() {
		return parent.getAttributes();
	}

	@Override
	protected void saveTag(ONDEXConcept concept)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean dropTag(ONDEXConcept concept)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Set<ONDEXConcept> retrieveTagAll() {
		return parent.getTags();
	}
}
