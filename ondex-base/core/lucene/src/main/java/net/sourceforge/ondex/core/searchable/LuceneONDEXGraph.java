package net.sourceforge.ondex.core.searchable;

import java.util.Collection;
import java.util.Set;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.base.AbstractONDEXGraph;
import net.sourceforge.ondex.exception.type.NullValueException;

/**
 * This class represents a wrapper implementation of the AbstractONDEXGraph.
 * 
 * @author taubertj
 */
public class LuceneONDEXGraph extends AbstractONDEXGraph {

	// persistent ONDEXGraph
	private ONDEXGraph parent;

	/**
	 * @param og
	 *            ONDEXGraph to wrap
	 */
	LuceneONDEXGraph(ONDEXGraph og) {
		this(og.getName(), og.getMetaData());
		this.parent = og;
		super.readOnly = true;
	}

	/**
	 * Constructor overridden for superclass.
	 * 
	 * @param name
	 *            name of this graph
	 * @param data
	 *            meta data for this graph
	 */
	LuceneONDEXGraph(String name, ONDEXGraphMetaData data) {
		super(name, new LuceneONDEXGraphMetaData(data));
	}

	/**
	 * Returns the parent ONDEXGraph which is wrapped in this instance.
	 * 
	 * @return ONDEXGraph
	 */
	public ONDEXGraph getParent() {
		return this.parent;
	}

	@Override
	protected ONDEXConcept storeConcept(long sid, int id, String pid,
			String annotation, String description, DataSource elementOf,
			ConceptClass ofType, Collection<EvidenceType> evidence)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected ONDEXConcept removeConcept(int id)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected ONDEXConcept retrieveConcept(int id) {
		ONDEXConcept c = parent.getConcept(id);
		if (c != null) {
			return new LuceneConcept(sid, c);
		} else {
			return null;
		}
	}

	@Override
	protected Set<ONDEXConcept> retrieveConceptAll() {
		return parent.getConcepts();
	}

	@Override
	protected Set<ONDEXConcept> retrieveConceptAllDataSource(
			DataSource dataSource) {
		return parent.getConceptsOfDataSource(dataSource);
	}

	@Override
	protected Set<ONDEXConcept> retrieveConceptAllConceptClass(
			ConceptClass conceptClass) {
		return parent.getConceptsOfConceptClass(conceptClass);
	}

	@Override
	protected Set<ONDEXConcept> retrieveConceptAllAttributeName(
			AttributeName attributeName) {
		return parent.getConceptsOfAttributeName(attributeName);
	}

	@Override
	protected Set<ONDEXConcept> retrieveConceptAllEvidenceType(
			EvidenceType evidenceType) {
		return parent.getConceptsOfEvidenceType(evidenceType);
	}

	@Override
	protected Set<ONDEXConcept> retrieveConceptAllTag(ONDEXConcept concept) {
		return parent.getConceptsOfTag(concept);
	}

	@Override
	protected Set<ONDEXConcept> retrieveTags() {
		return parent.getAllTags();
	}

	@Override
	protected ONDEXRelation storeRelation(long sid, int id,
			ONDEXConcept fromConcept, ONDEXConcept toConcept,
			RelationType ofType, Collection<EvidenceType> evidence)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean removeRelation(ONDEXConcept fromConcept,
			ONDEXConcept toConcept, RelationType ofType)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean removeRelation(int id)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws NullValueException
	 *             if from- or to-concept are null
	 * @see net.sourceforge.ondex.core.base.AbstractONDEXGraph#retrieveRelation(net.sourceforge.ondex.core.ONDEXConcept,
	 *      net.sourceforge.ondex.core.ONDEXConcept,
	 *      net.sourceforge.ondex.core.ONDEXConcept,
	 *      net.sourceforge.ondex.core.RelationType)
	 */
	@Override
	protected ONDEXRelation retrieveRelation(ONDEXConcept fromConcept,
			ONDEXConcept toConcept, RelationType ofType)
			throws NullValueException {
		ONDEXRelation r = parent.getRelation(
				((LuceneConcept) fromConcept).getParent(),
				((LuceneConcept) toConcept).getParent(), ofType);
		if (r != null) {
			return new LuceneRelation(sid, this, r);
		} else {
			return null;
		}
	}

	@Override
	protected ONDEXRelation retrieveRelation(int id) {
		ONDEXRelation r = parent.getRelation(id);
		if (r != null) {
			return new LuceneRelation(sid, this, r);
		} else {
			return null;
		}
	}

	@Override
	protected Set<ONDEXRelation> retrieveRelationAll() {
		return parent.getRelations();
	}

	@Override
	protected Set<ONDEXRelation> retrieveRelationAllConcept(ONDEXConcept concept) {
		return parent.getRelationsOfConcept(concept);
	}

	@Override
	protected Set<ONDEXRelation> retrieveRelationAllDataSource(
			DataSource dataSource) {
		return parent.getRelationsOfDataSource(dataSource);
	}

	@Override
	protected Set<ONDEXRelation> retrieveRelationAllConceptClass(
			ConceptClass conceptClass) {
		return parent.getRelationsOfConceptClass(conceptClass);
	}

	@Override
	protected Set<ONDEXRelation> retrieveRelationAllAttributeName(
			AttributeName attributeName) {
		return parent.getRelationsOfAttributeName(attributeName);
	}

	@Override
	protected Set<ONDEXRelation> retrieveRelationAllEvidenceType(
			EvidenceType evidenceType) {
		return parent.getRelationsOfEvidenceType(evidenceType);
	}

	@Override
	protected Set<ONDEXRelation> retrieveRelationAllRelationType(
			RelationType relationType) {
		return parent.getRelationsOfRelationType(relationType);
	}

	@Override
	protected Set<ONDEXRelation> retrieveRelationAllTag(ONDEXConcept concept) {
		return parent.getRelationsOfTag(concept);
	}

}
