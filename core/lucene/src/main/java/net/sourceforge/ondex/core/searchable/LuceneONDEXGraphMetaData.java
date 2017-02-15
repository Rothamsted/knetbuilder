package net.sourceforge.ondex.core.searchable;

import java.util.Set;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.core.base.AbstractONDEXGraphMetaData;

/**
 * Wrapper implementation which is read only for Lucene environment.
 * 
 * @author taubertj
 * 
 */
public class LuceneONDEXGraphMetaData extends AbstractONDEXGraphMetaData {

	/**
	 * Wrapped meta data
	 */
	private ONDEXGraphMetaData parent;

	/**
	 * Wraps given meta data into a read-only format.
	 * 
	 * @param data
	 *            ONDEXGraphMetaData
	 */
	protected LuceneONDEXGraphMetaData(ONDEXGraphMetaData data) {
		this.parent = data;
	}

	/**
	 * Returns the parent ONDEXGraphMetaData which is wrapped in this instance.
	 * 
	 * @return ONDEXGraphMetaData
	 */
	public ONDEXGraphMetaData getParent() {
		return this.parent;
	}

	@Override
	protected boolean existsAttributeName(String id) {
		return parent.checkAttributeName(id);
	}

	@Override
	protected boolean existsConceptClass(String id) {
		return parent.checkConceptClass(id);
	}

	@Override
	protected boolean existsDataSource(String id) {
		return parent.checkDataSource(id);
	}

	@Override
	protected boolean existsEvidenceType(String id) {
		return parent.checkEvidenceType(id);
	}

	@Override
	protected boolean existsRelationType(String id) {
		return parent.checkRelationType(id);
	}

	@Override
	protected boolean existsUnit(String id) {
		return parent.checkUnit(id);
	}

	@Override
	protected boolean removeAttributeName(String id)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean removeConceptClass(String id)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean removeDataSource(String id)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean removeEvidenceType(String id)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean removeRelationType(String id)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean removeUnit(String id)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected AttributeName retrieveAttributeName(String id) {
		return parent.getAttributeName(id);
	}

	@Override
	protected Set<AttributeName> retrieveAttributeNameAll() {
		return parent.getAttributeNames();
	}

	@Override
	protected ConceptClass retrieveConceptClass(String id) {
		return parent.getConceptClass(id);
	}

	@Override
	protected Set<ConceptClass> retrieveConceptClassAll() {
		return parent.getConceptClasses();
	}

	@Override
	protected DataSource retrieveDataSource(String id) {
		return parent.getDataSource(id);
	}

	@Override
	protected Set<DataSource> retrieveDataSourceAll() {
		return parent.getDataSources();
	}

	@Override
	protected EvidenceType retrieveEvidenceType(String id) {
		return parent.getEvidenceType(id);
	}

	@Override
	protected Set<EvidenceType> retrieveEvidenceTypeAll() {
		return parent.getEvidenceTypes();
	}

	@Override
	protected RelationType retrieveRelationType(String id) {
		return parent.getRelationType(id);
	}

	@Override
	protected Set<RelationType> retrieveRelationTypeAll() {
		return parent.getRelationTypes();
	}

	@Override
	protected Unit retrieveUnit(String id) {
		return parent.getUnit(id);
	}

	@Override
	protected Set<Unit> retrieveUnitAll() {
		return parent.getUnits();
	}

	@Override
	protected AttributeName storeAttributeName(AttributeName an)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected ConceptClass storeConceptClass(ConceptClass cc)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected DataSource storeDataSource(DataSource dataSource)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected EvidenceType storeEvidenceType(EvidenceType evitype)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected RelationType storeRelationType(RelationType rt)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Unit storeUnit(Unit unit) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

}
