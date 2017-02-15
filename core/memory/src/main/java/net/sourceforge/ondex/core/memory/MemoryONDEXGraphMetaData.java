package net.sourceforge.ondex.core.memory;

import java.util.Set;

import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.core.base.AbstractONDEXGraphMetaData;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.DuplicatedEntryEvent;
import net.sourceforge.ondex.event.type.EventType;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;

/**
 * This class represents a pure memory based implementation of the abstract
 * ONDEX graph meta data. It uses standard JAVA datatypes.
 * 
 * @author taubertj
 */
public class MemoryONDEXGraphMetaData extends AbstractONDEXGraphMetaData {

	// serial version id
	private static final long serialVersionUID = 1L;

	// Map for DataSources with id as index
	private BidiMap<String, DataSource> cvs;

	// Map for ConceptClasses with id as index
	private BidiMap<String, ConceptClass> ccs;

	// Map for AttributeNames with id as index
	private BidiMap<String, AttributeName> attribs;

	// Map for Units with id as index
	private BidiMap<String, Unit> units;

	// Map for EvidenceTypes with id as index
	private BidiMap<String, EvidenceType> evitypes;

	// Map for RelationTypes with id as index
	private BidiMap<String, RelationType> reltypes;

	/**
	 * Default constructor
	 */
	public MemoryONDEXGraphMetaData() {
		super();
		this.cvs = new DualHashBidiMap<String, DataSource>();
		this.ccs = new DualHashBidiMap<String, ConceptClass>();
		this.attribs = new DualHashBidiMap<String, AttributeName>();
		this.units = new DualHashBidiMap<String, Unit>();
		this.evitypes = new DualHashBidiMap<String, EvidenceType>();
		this.reltypes = new DualHashBidiMap<String, RelationType>();
	}

	/**
	 * Propagates events to registered handlers
	 * 
	 * @param e
	 *            EventType to propagate
	 */
	private void fireEventOccurred(EventType e) {
		ONDEXEventHandler.getEventHandlerForSID(getSID()).fireEventOccurred(e);
	}

	@Override
	protected boolean removeDataSource(String id) {
		return cvs.remove(id) != null;
	}

	@Override
	protected DataSource retrieveDataSource(String id) {
		return cvs.get(id);
	}

	@Override
	protected boolean existsDataSource(String id) {
		return cvs.containsKey(id);
	}

	@Override
	protected Set<DataSource> retrieveDataSourceAll() {
		return cvs.values();
	}

	@Override
	protected DataSource storeDataSource(DataSource dataSource) {

		DataSource existingDataSource = cvs.get(dataSource.getId());
		if (existingDataSource != null) {
			fireEventOccurred(new DuplicatedEntryEvent(
					Config.properties.getProperty("memory.ONDEXGraphMetaData.DuplicatedDataSource")
							+ dataSource.getId(),
					"[ONDEXGraphMetaData - storeDataSource]"));
			return existingDataSource;
		} else {
			cvs.put(dataSource.getId(), dataSource);
		}

		return dataSource;
	}

	@Override
	protected boolean removeConceptClass(String id) {
		return ccs.remove(id) != null;
	}

	@Override
	protected ConceptClass retrieveConceptClass(String id) {
		return ccs.get(id);
	}

	@Override
	protected boolean existsConceptClass(String id) {
		return ccs.containsKey(id);
	}

	@Override
	protected Set<ConceptClass> retrieveConceptClassAll() {
		return ccs.values();
	}

	@Override
	protected ConceptClass storeConceptClass(ConceptClass cc) {

		ConceptClass existingCc = ccs.get(cc.getId());
		if (existingCc != null) {
			fireEventOccurred(new DuplicatedEntryEvent(
					Config.properties.getProperty("memory.ONDEXGraphMetaData.DuplicatedConceptClass")
							+ cc.getId(),
					"[ONDEXGraphMetaData - storeConceptClass]"));
			return existingCc;
		} else {
			ccs.put(cc.getId(), cc);
		}

		return cc;
	}

	@Override
	protected boolean removeAttributeName(String id) {
		return attribs.remove(id) != null;
	}

	@Override
	protected AttributeName retrieveAttributeName(String id) {
		return attribs.get(id);
	}

	@Override
	protected boolean existsAttributeName(String id) {
		return attribs.containsKey(id);
	}

	@Override
	protected Set<AttributeName> retrieveAttributeNameAll() {
		return attribs.values();
	}

	@Override
	protected AttributeName storeAttributeName(AttributeName an) {

		AttributeName existingAn = attribs.get(an.getId());
		if (existingAn != null) {
			fireEventOccurred(new DuplicatedEntryEvent(
					Config.properties.getProperty("memory.ONDEXGraphMetaData.DuplicatedAttributeName")
							+ an.getId(),
					"[ONDEXGraphMetaData - storeAttributeName]"));
			return existingAn;
		} else {
			attribs.put(an.getId(), an);
		}

		return an;
	}

	@Override
	protected boolean removeUnit(String id) {
		return units.remove(id) != null;
	}

	@Override
	protected Unit retrieveUnit(String id) {
		return units.get(id);
	}

	@Override
	protected boolean existsUnit(String id) {
		return units.containsKey(id);
	}

	@Override
	protected Set<Unit> retrieveUnitAll() {
		return units.values();
	}

	@Override
	protected Unit storeUnit(Unit unit) {
		Unit existingUnit = units.get(unit.getId());
		if (existingUnit != null) {
			fireEventOccurred(new DuplicatedEntryEvent(
					Config.properties.getProperty("memory.ONDEXGraphMetaData.DuplicatedUnit")
							+ unit.getId(), "[ONDEXGraphMetaData - storeUnit"));
			return existingUnit;
		} else {
			units.put(unit.getId(), unit);
		}

		return unit;
	}

	@Override
	protected boolean removeEvidenceType(String id) {
		return evitypes.remove(id) != null;
	}

	@Override
	protected EvidenceType retrieveEvidenceType(String id) {
		return evitypes.get(id);
	}

	@Override
	protected boolean existsEvidenceType(String id) {
		return evitypes.containsKey(id);
	}

	@Override
	protected Set<EvidenceType> retrieveEvidenceTypeAll() {
		return evitypes.values();
	}

	@Override
	protected EvidenceType storeEvidenceType(EvidenceType evitype) {

		EvidenceType existingEt = evitypes.get(evitype.getId());
		if (existingEt != null) {
			fireEventOccurred(new DuplicatedEntryEvent(
					Config.properties.getProperty("memory.ONDEXGraphMetaData.DuplicatedEvidenceType")
							+ evitype.getId(),
					"[ONDEXGraphMetaData - storeEvidenceType]"));
			return existingEt;
		} else {
			evitypes.put(evitype.getId(), evitype);
		}

		return evitype;
	}

	@Override
	protected boolean removeRelationType(String id) {
		return reltypes.remove(id) != null;
	}

	@Override
	protected RelationType retrieveRelationType(String id) {
		return reltypes.get(id);
	}

	@Override
	protected boolean existsRelationType(String id) {
		return reltypes.containsKey(id);
	}

	@Override
	protected Set<RelationType> retrieveRelationTypeAll() {
		return reltypes.values();
	}

	@Override
	protected RelationType storeRelationType(RelationType rt) {

		RelationType existingRT = reltypes.get(rt.getId());
		if (existingRT != null) {
			fireEventOccurred(new DuplicatedEntryEvent(
					Config.properties.getProperty("memory.ONDEXGraphMetaData.DuplicatedRelationType")
							+ rt.getId(),
					"[ONDEXGraphMetaData - storeRelationType]"));
			return existingRT;
		} else {
			reltypes.put(rt.getId(), rt);
		}

		return rt;
	}
}
