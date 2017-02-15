package net.sourceforge.ondex.core.persistent;

import java.util.Set;

import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.MetaDataFactory;
import net.sourceforge.ondex.core.ONDEXAssociable;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.core.base.AbstractONDEXGraphMetaData;
import net.sourceforge.ondex.core.util.UpdateListener;
import net.sourceforge.ondex.event.type.DuplicatedEntryEvent;
import net.sourceforge.ondex.exception.type.DataLossException;

/**
 * This class represents a persistent implementation of the
 * AbstractONDEXGraphMetaData. It uses the Berkeley Java Edition database.
 * 
 * @author taubertj
 */
public class BerkeleyONDEXGraphMetaData extends AbstractONDEXGraphMetaData
		implements UpdateListener {

	// berkeley env
	private transient BerkeleyEnv berkeley;

	/**
	 * Default constructor initialises Berkeley
	 * 
	 * @param berkeley
	 *            Berkeley environment
	 */
	BerkeleyONDEXGraphMetaData(BerkeleyEnv berkeley) {
		super();
		this.berkeley = berkeley;
	}

	/**
	 * Sets backing BerkeleyEnv
	 * 
	 * @param berkeley
	 *            BerkeleyEnv
	 */
	public void setBerkeley(BerkeleyEnv berkeley) {
		this.berkeley = berkeley;
	}

	/**
	 * Updates the persistent layer for a given property aspect of meta data.
	 * 
	 * @param o
	 *            Object to update
	 */
	public void performUpdate(ONDEXAssociable o) {

		if (o instanceof BerkeleyDataSource) {
			BerkeleyDataSource cv = (BerkeleyDataSource) o;
			berkeley.deleteFromDatabase(DataSource.class, cv.getId());
			berkeley.insertIntoDatabase(DataSource.class, cv.getId(),
					cv.serialise());
		} else if (o instanceof BerkeleyConceptClass) {
			BerkeleyConceptClass cc = (BerkeleyConceptClass) o;
			berkeley.deleteFromDatabase(ConceptClass.class, cc.getId());
			berkeley.insertIntoDatabase(ConceptClass.class, cc.getId(),
					cc.serialise());
		} else if (o instanceof BerkeleyAttributeName) {
			BerkeleyAttributeName an = (BerkeleyAttributeName) o;
			berkeley.deleteFromDatabase(AttributeName.class, an.getId());
			berkeley.insertIntoDatabase(AttributeName.class, an.getId(),
					an.serialise());
		} else if (o instanceof BerkeleyUnit) {
			BerkeleyUnit unit = (BerkeleyUnit) o;
			berkeley.deleteFromDatabase(Unit.class, unit.getId());
			berkeley.insertIntoDatabase(Unit.class, unit.getId(),
					unit.serialise());
		} else if (o instanceof BerkeleyEvidenceType) {
			BerkeleyEvidenceType evitype = (BerkeleyEvidenceType) o;
			berkeley.deleteFromDatabase(EvidenceType.class, evitype.getId());
			berkeley.insertIntoDatabase(EvidenceType.class, evitype.getId(),
					evitype.serialise());
		} else if (o instanceof BerkeleyRelationType) {
			BerkeleyRelationType rt = (BerkeleyRelationType) o;
			berkeley.deleteFromDatabase(RelationType.class, rt.getId());
			berkeley.insertIntoDatabase(RelationType.class, rt.getId(),
					rt.serialise());
		} else {
			throw new DataLossException("Data could not be updated because of "
					+ "an incompatible data type was provided :" + o.getClass());

		}

	}

	@Override
	protected boolean removeDataSource(String id) {
		return berkeley.deleteFromDatabase(DataSource.class, id);
	}

	@Override
	protected DataSource retrieveDataSource(String id) {
		byte[] array = berkeley.getFromDatabase(DataSource.class, id);
		if (array != null) {
			BerkeleyDataSource cv = Persistence.deserialise(
					BerkeleyDataSource.FACTORY, array);
			array = null;
			cv.setUpdateListener(this);
			return cv;
		}
		return null;
	}

	@Override
	protected boolean existsDataSource(String id) {
		return berkeley.existsInDatabase(DataSource.class, id);
	}

	@Override
	protected Set<DataSource> retrieveDataSourceAll() {
		return new BerkeleyBrowser<DataSource>(berkeley, this, DataSource.class);
	}

	@Override
	protected DataSource storeDataSource(DataSource dataSource) {
		BerkeleyDataSource bcv = BerkeleyDataSource.FACTORY.convert(dataSource);
		if (berkeley.existsInDatabase(DataSource.class, dataSource.getId())) {
			berkeley.fireEventOccurred(new DuplicatedEntryEvent(
					Config.properties
							.getProperty("persistent.BerkeleyONDEXGraphMetaData.DuplicatedDataSource")
							+ dataSource.getId(),
					"[BerkeleyONDEXGraphMetaData - storeDataSource]"));
		} else {
			berkeley.insertIntoDatabase(DataSource.class, dataSource.getId(),
					bcv.serialise());
		}
		bcv.setUpdateListener(this);
		return bcv;

	}

	@Override
	protected boolean removeConceptClass(String id) {
		return berkeley.deleteFromDatabase(ConceptClass.class, id);
	}

	@Override
	protected ConceptClass retrieveConceptClass(String id) {
		byte[] array = berkeley.getFromDatabase(ConceptClass.class, id);
		if (array != null) {
			BerkeleyConceptClass cc = BerkeleyConceptClass.deserialise(
					berkeley.getAbstractONDEXGraph(), array);
			array = null;
			cc.setUpdateListener(this);
			return cc;
		}
		return null;
	}

	@Override
	protected boolean existsConceptClass(String id) {
		return berkeley.existsInDatabase(ConceptClass.class, id);
	}

	@Override
	protected Set<ConceptClass> retrieveConceptClassAll() {
		return new BerkeleyBrowser<ConceptClass>(berkeley, this,
				ConceptClass.class);
	}

	@Override
	protected ConceptClass storeConceptClass(ConceptClass cc) {
		BerkeleyConceptClass bcc = BerkeleyConceptClass.convert(cc);
		if (berkeley.existsInDatabase(ConceptClass.class, cc.getId())) {
			berkeley.fireEventOccurred(new DuplicatedEntryEvent(
					Config.properties
							.getProperty("persistent.BerkeleyONDEXGraphMetaData.DuplicatedConceptClass")
							+ cc.getId(),
					"[BerkeleyONDEXGraphMetaData - storeConceptClass]"));
		} else {
			berkeley.insertIntoDatabase(ConceptClass.class, cc.getId(),
					bcc.serialise());
		}
		bcc.setUpdateListener(this);
		return bcc;

	}

	@Override
	protected boolean removeAttributeName(String id) {
		return berkeley.deleteFromDatabase(AttributeName.class, id);
	}

	@Override
	protected AttributeName retrieveAttributeName(String id) {
		byte[] array = berkeley.getFromDatabase(AttributeName.class, id);
		if (array != null) {
			BerkeleyAttributeName an = BerkeleyAttributeName.deserialise(
					berkeley.getAbstractONDEXGraph(), array);
			array = null;
			an.setUpdateListener(this);
			return an;
		}
		return null;
	}

	@Override
	protected boolean existsAttributeName(String id) {
		return berkeley.existsInDatabase(AttributeName.class, id);
	}

	@Override
	protected Set<AttributeName> retrieveAttributeNameAll() {
		return new BerkeleyBrowser<AttributeName>(berkeley, this,
				AttributeName.class);
	}

	@Override
	protected AttributeName storeAttributeName(AttributeName an) {
		BerkeleyAttributeName ban = BerkeleyAttributeName.convert(an);
		if (berkeley.existsInDatabase(AttributeName.class, an.getId())) {
			berkeley.fireEventOccurred(new DuplicatedEntryEvent(
					Config.properties
							.getProperty("persistent.BerkeleyONDEXGraphMetaData.DuplicatedAttributeName")
							+ an.getId(),
					"[BerkeleyONDEXGraphMetaData - storeAttributeName]"));
		} else {
			berkeley.insertIntoDatabase(AttributeName.class, an.getId(),
					ban.serialise());
		}
		ban.setUpdateListener(this);
		return ban;

	}

	@Override
	protected boolean removeUnit(String id) {
		return berkeley.deleteFromDatabase(Unit.class, id);
	}

	@Override
	protected Unit retrieveUnit(String id) {
		byte[] array = berkeley.getFromDatabase(Unit.class, id);
		if (array != null) {
			BerkeleyUnit unit = Persistence.deserialise(BerkeleyUnit.FACTORY,
					array);
			array = null;
			unit.setUpdateListener(this);
			return unit;
		}
		return null;
	}

	@Override
	protected boolean existsUnit(String id) {
		return berkeley.existsInDatabase(Unit.class, id);
	}

	@Override
	protected Set<Unit> retrieveUnitAll() {
		return new BerkeleyBrowser<Unit>(berkeley, this, Unit.class);
	}

	@Override
	protected Unit storeUnit(Unit unit) {
		BerkeleyUnit bu = BerkeleyUnit.FACTORY.convert(unit);
		if (berkeley.existsInDatabase(Unit.class, unit.getId())) {
			berkeley.fireEventOccurred(new DuplicatedEntryEvent(
					Config.properties
							.getProperty("persistent.BerkeleyONDEXGraphMetaData.DuplicatedUnit")
							+ unit.getId(),
					"[BerkeleyONDEXGraphMetaData - storeUnit]"));
		} else {
			berkeley.insertIntoDatabase(Unit.class, unit.getId(),
					bu.serialise());
		}
		bu.setUpdateListener(this);
		return bu;

	}

	@Override
	protected boolean removeEvidenceType(String id) {
		return berkeley.deleteFromDatabase(EvidenceType.class, id);
	}

	@Override
	protected EvidenceType retrieveEvidenceType(String id) {
		byte[] array = berkeley.getFromDatabase(EvidenceType.class, id);
		if (array != null) {
			BerkeleyEvidenceType et = Persistence.deserialise(
					BerkeleyEvidenceType.FACTORY, array);
			array = null;
			et.setUpdateListener(this);
			return et;
		}
		return null;
	}

	@Override
	protected boolean existsEvidenceType(String id) {
		return berkeley.existsInDatabase(EvidenceType.class, id);
	}

	@Override
	protected Set<EvidenceType> retrieveEvidenceTypeAll() {
		return new BerkeleyBrowser<EvidenceType>(berkeley, this,
				EvidenceType.class);
	}

	@Override
	protected EvidenceType storeEvidenceType(EvidenceType evitype) {
		BerkeleyEvidenceType bet = BerkeleyEvidenceType.FACTORY
				.convert(evitype);
		if (berkeley.existsInDatabase(EvidenceType.class, evitype.getId())) {
			berkeley.fireEventOccurred(new DuplicatedEntryEvent(
					Config.properties
							.getProperty("persistent.BerkeleyONDEXGraphMetaData.DuplicatedEvidenceType")
							+ evitype.getId(),
					"[BerkeleyONDEXGraphMetaData - storeEvidenceType]"));
		} else {
			berkeley.insertIntoDatabase(EvidenceType.class, evitype.getId(),
					bet.serialise());
		}
		bet.setUpdateListener(this);
		return bet;

	}

	@Override
	protected boolean removeRelationType(String id) {
		return berkeley.deleteFromDatabase(RelationType.class, id);
	}

	@Override
	protected RelationType retrieveRelationType(String id) {
		byte[] array = berkeley.getFromDatabase(RelationType.class, id);
		if (array != null) {
			BerkeleyRelationType rt = BerkeleyRelationType.deserialise(
					berkeley.getAbstractONDEXGraph(), array);
			array = null;
			rt.setUpdateListener(this);
			return rt;
		}
		return null;
	}

	@Override
	protected boolean existsRelationType(String id) {
		return berkeley.existsInDatabase(RelationType.class, id);
	}

	@Override
	protected Set<RelationType> retrieveRelationTypeAll() {
		return new BerkeleyBrowser<RelationType>(berkeley, this,
				RelationType.class);
	}

	@Override
	protected RelationType storeRelationType(RelationType rt) {
		BerkeleyRelationType brt = BerkeleyRelationType.convert(rt);
		if (berkeley.existsInDatabase(RelationType.class, rt.getId())) {
			berkeley.fireEventOccurred(new DuplicatedEntryEvent(
					Config.properties
							.getProperty("persistent.BerkeleyONDEXGraphMetaData.DuplicatedRelationType")
							+ rt.getId(),
					"[BerkeleyONDEXGraphMetaData - storeRelationType]"));
		} else {
			berkeley.insertIntoDatabase(RelationType.class, rt.getId(),
					brt.serialise());
		}
		brt.setUpdateListener(this);
		return brt;

	}

	public byte[] serialise() {
		// do nothing
		return null;
	}

	@Override
	public MetaDataFactory getFactory() {
		return new MetaDataFactory(this);
	}
}
