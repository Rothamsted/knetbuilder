package net.sourceforge.ondex.core.persistent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;

import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXAssociable;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.base.AbstractRelation;
import net.sourceforge.ondex.core.base.RelationAttribute;
import net.sourceforge.ondex.core.util.UpdateListener;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.DuplicatedEntryEvent;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.StorageException;

/**
 * This class represents a persistent implementation of the AbstractRelation. It
 * uses the Berkeley Java Edition database.
 * 
 * @author taubertj
 */
public class BerkeleyRelation extends AbstractRelation implements
		UpdateListener, Updatable, BerkeleySerializable {

	/**
	 * Returns a new created object of this class from a byte array.
	 * 
	 * @param berkeley
	 *            BerkeleyEnv
	 * @param array
	 *            byte[]
	 * @return BerkeleyRelation
	 */
	public static BerkeleyRelation deserialise(BerkeleyEnv berkeley,
			byte[] array) {

		BerkeleyRelation relation = null;

		try {
			// byte input stream for byte array
			ByteArrayInputStream bais = new ByteArrayInputStream(array);

			// object input stream for deserialisation
			DataInputStream dis = new DataInputStream(new BufferedInputStream(
					bais));

			// get serialised class content
			long sid = dis.readLong();
			int id = dis.readInt();
			int fromConceptID = dis.readInt();
			int toConceptID = dis.readInt();
			String ofTypeId = dis.readUTF().intern();

			// retrieve meta data elements
			RelationType ofType = berkeley.getAbstractONDEXGraph()
					.getMetaData().getRelationType(ofTypeId);

			// retrieve concept elements
			ONDEXConcept fromConcept = berkeley.getAbstractONDEXGraph()
					.getConcept(fromConceptID);
			ONDEXConcept toConcept = berkeley.getAbstractONDEXGraph()
					.getConcept(toConceptID);

			// create new instance of this class
			relation = new BerkeleyRelation(sid, berkeley, id, fromConcept,
					toConcept, ofType);

			// close streams
			dis.close();
			bais.close();

			// try to prevent memory leaks
			dis = null;
			bais = null;

		} catch (IOException ioe) {
			throw new StorageException(ioe.getMessage());
		}

		// return new Berkeley relation or null
		return relation;
	}

	private UpdateListener l;

	// backing berkeley environment
	private transient BerkeleyEnv berkeley;

	// catches the byte[] of the relation id
	private byte[] idarray;

	/**
	 * Constructor which fills all fields of this class.
	 * 
	 * @param sid
	 *            unique id
	 * @param berkeley
	 *            BerkeleyEnv
	 * @param id
	 *            Integer
	 * @param fromConcept
	 *            startpoint
	 * @param toConcept
	 *            endpoint
	 * @param ofType
	 *            specifies Relation Type
	 * @throws AccessDeniedException
	 */
	protected BerkeleyRelation(long sid, BerkeleyEnv berkeley, int id,
			ONDEXConcept fromConcept, ONDEXConcept toConcept,
			RelationType ofType) {
		super(sid, id, fromConcept, toConcept, ofType);
		this.berkeley = berkeley;
		this.idarray = berkeley.convert(id).getData();
		// important for linking to serialisation
		this.key = new BerkeleyRelationKey(sid, fromConcept.getId(),
				toConcept.getId(), ofType.getId());
	}

	@Override
	public void fireUpdateEvent() {
		if (l != null)
			l.performUpdate(this);
	}

	/**
	 * Updates the persistent layer for a given property aspect of a
	 * BerkeleyRelation.
	 * 
	 * @param o
	 *            Object to update
	 */
	public void performUpdate(ONDEXAssociable o) {
		if (o instanceof BerkeleyRelationAttribute) {
			BerkeleyRelationAttribute attribute = (BerkeleyRelationAttribute) o;
			BerkeleyRelationKeyName keyname = new BerkeleyRelationKeyName(
					(BerkeleyRelationKey) key, attribute.getOfType().getId());
			berkeley.deleteFromDatabase(RelationAttribute.class, keyname);
			berkeley.insertIntoDatabase(RelationAttribute.class, keyname,
					attribute.serialise());
			keyname = null;
		} else if (o instanceof BerkeleyEvidenceType) {
			BerkeleyEvidenceType evidenceType = (BerkeleyEvidenceType) o;
			berkeley.deleteFromDupDatabase(EvidenceType.class, this.getKey(),
					evidenceType.serialise());
			berkeley.insertIntoDupDatabase(EvidenceType.class, this.getKey(),
					evidenceType.serialise());
		} else
			throw new UnsupportedOperationException(
					"Data could not be updated because of a "
							+ "an incompatible data type was provided :"
							+ o.getClass());
	}

	/**
	 * Returns a byte array serialisation of this class.
	 * 
	 * @return serialisation in the form of byte[]
	 */
	public byte[] serialise() {
		return serialise(this.sid);
	}

	/**
	 * Returns a byte array serialisation of this class.
	 * 
	 * @param sid
	 *            override current sid
	 * @return byte[]
	 */
	public byte[] serialise(long sid) {

		// byte return values
		byte[] retVal;

		try {

			// create a byte output stream
			ByteArrayOutputStream baos = new ByteArrayOutputStream(100);

			// object output stream for serialisation
			DataOutputStream dos = new DataOutputStream(
					new BufferedOutputStream(baos));

			// serialise class content
			dos.writeLong(sid);
			dos.writeInt(id);
			dos.writeInt(fromConcept.getId());
			dos.writeInt(toConcept.getId());
			dos.writeUTF(ofType.getId());
			dos.flush();

			retVal = baos.toByteArray();

			// make sure streams are closed
			dos.close();
			baos.close();

			// try to prevent memory leaks
			dos = null;
			baos = null;

		} catch (IOException ioe) {
			throw new StorageException(ioe.getMessage());
		}

		// return byte array
		return retVal;
	}

	@Override
	public void setUpdateListener(UpdateListener l) {
		this.l = l;
	}

	private void fireEventOccurred(EventType e) {
		ONDEXEventHandler.getEventHandlerForSID(getSID()).fireEventOccurred(e);
	}

	@Override
	protected boolean dropEvidenceType(EvidenceType evidencetype) {
		berkeley.deleteFromDupDatabase(BerkeleyIntegerName.class, evidencetype,
				idarray);
		return berkeley.deleteFromDupDatabase(EvidenceType.class,
				(BerkeleyRelationKey) key,
				((BerkeleyEvidenceType) evidencetype).serialise());
	}

	@Override
	protected boolean dropTag(ONDEXConcept concept) {

		if (concept instanceof BerkeleyConcept) {
			berkeley.deleteFromDupDatabase(BerkeleyIntegerName.class,
					concept.getId(), idarray);
			return berkeley.deleteFromDupDatabase(BerkeleyConcept.class,
					(BerkeleyRelationKey) key,
					((BerkeleyConcept) concept).serialise());
		} else
			throw new UnsupportedOperationException("Tag " + concept.getId()
					+ " of class " + concept.getClass()
					+ " could not be saved as it is not BerkeleyConcept");
	}

	@Override
	protected boolean removeRelationAttribute(AttributeName attributeName) {
		berkeley.deleteFromDupDatabase(BerkeleyIntegerName.class,
				attributeName, idarray);
		return berkeley.deleteFromDatabase(RelationAttribute.class,
				new BerkeleyRelationKeyName((BerkeleyRelationKey) key,
						attributeName.getId()));
	}

	@Override
	protected Set<EvidenceType> retrieveEvidenceTypeAll() {
		return new BerkeleyDupBrowser<EvidenceType>(berkeley, this,
				EvidenceType.class, (BerkeleyRelationKey) key);
	}

	@Override
	protected RelationAttribute retrieveRelationAttribute(
			AttributeName attributeName) {

		byte[] array = berkeley.getFromDatabase(RelationAttribute.class,
				new BerkeleyRelationKeyName((BerkeleyRelationKey) key,
						attributeName.getId()));

		if (array != null) {
			BerkeleyRelationAttribute attribute = BerkeleyRelationAttribute
					.deserialise(berkeley.getAbstractONDEXGraph(), array);
			array = null;
			attribute.setUpdateListener(this);
			return attribute;
		}

		return null;
	}

	@Override
	protected Set<Attribute> retrieveRelationAttributeAll() {
		return new BerkeleySecBrowser<Attribute>(berkeley, this,
				RelationAttribute.class, new BerkeleyRelationKeyName(
						(BerkeleyRelationKey) key, ""));
	}

	@Override
	protected Set<ONDEXConcept> retrieveTagAll() {
		return new BerkeleyDupBrowser<ONDEXConcept>(berkeley, this,
				BerkeleyConcept.class, (BerkeleyRelationKey) key);
	}

	@Override
	protected void saveEvidenceType(EvidenceType evidencetype) {
		berkeley.insertIntoDupDatabase(EvidenceType.class,
				(BerkeleyRelationKey) key,
				((BerkeleyEvidenceType) evidencetype).serialise());
		berkeley.insertIntoDupDatabase(BerkeleyIntegerName.class, evidencetype,
				idarray);
	}

	@Override
	protected void saveTag(ONDEXConcept concept) {

		if (concept instanceof BerkeleyConcept) {
			berkeley.insertIntoDupDatabase(BerkeleyConcept.class,
					(BerkeleyRelationKey) key,
					((BerkeleyConcept) concept).serialise());
			berkeley.insertIntoDupDatabase(BerkeleyIntegerName.class,
					concept.getId(), idarray);
		} else
			throw new UnsupportedOperationException("Tag " + concept.getId()
					+ " of class " + concept.getClass()
					+ " could not be saved as it is not BerkeleyConcept");
	}

	@Override
	protected Attribute storeRelationAttribute(Attribute attribute) {

		BerkeleyRelationKeyName keyname = new BerkeleyRelationKeyName(
				(BerkeleyRelationKey) key, attribute.getOfType().getId());
		BerkeleyRelationAttribute berkeleyAttribute = BerkeleyRelationAttribute
				.convert(attribute);

		if (berkeley.existsInDatabase(RelationAttribute.class, keyname)) {
			fireEventOccurred(new DuplicatedEntryEvent(
					Config.properties.getProperty("persistent.BerkeleyRelation.DuplicatedAttributeName")
							+ attribute.getOfType().getId(),
					"[BerkeleyRelation - storeRelationAttribute]"));
		} else {
			berkeley.insertIntoDatabase(RelationAttribute.class, keyname,
					berkeleyAttribute.serialise());
			berkeley.insertIntoDupDatabase(BerkeleyIntegerName.class,
					attribute.getOfType(), idarray);
		}

		keyname = null;
		berkeleyAttribute.setUpdateListener(this);
		return berkeleyAttribute;
	}
}
