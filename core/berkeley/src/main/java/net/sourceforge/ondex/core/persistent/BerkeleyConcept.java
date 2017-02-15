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
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXAssociable;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.base.AbstractConcept;
import net.sourceforge.ondex.core.base.ConceptAttribute;
import net.sourceforge.ondex.core.util.UpdateListener;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.DuplicatedEntryEvent;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.StorageException;

/**
 * This class represents a persistent implementation of the AbstractConcept. It
 * uses the Berkeley Java Edition database.
 * 
 * @author taubertj
 */
public class BerkeleyConcept extends AbstractConcept implements UpdateListener,
		Updatable, BerkeleySerializable {

	/**
	 * Returns a new created object of this class from a byte array.
	 * 
	 * @param berkeley
	 *            BerkeleyEnv
	 * @param array
	 *            byte[]
	 * @return BerkeleyConcept
	 */
	public static BerkeleyConcept deserialise(BerkeleyEnv berkeley, byte[] array) {

		BerkeleyConcept concept = null;

		try {

			// byte input stream for byte array
			ByteArrayInputStream bais = new ByteArrayInputStream(array);

			// object input stream for deserialisation
			DataInputStream dis = new DataInputStream(new BufferedInputStream(
					bais));

			// get serialised class content
			long sid = dis.readLong();
			int id = dis.readInt();
			String pid = dis.readUTF();
			String annotation = dis.readUTF();
			String description = dis.readUTF();
			String elementOfId = dis.readUTF();
			String ofTypeId = dis.readUTF();

			// retrieve meta data elements
			DataSource elementOf = berkeley.getAbstractONDEXGraph()
					.getMetaData().getDataSource(elementOfId);
			ConceptClass ofType = berkeley.getAbstractONDEXGraph()
					.getMetaData().getConceptClass(ofTypeId);

			// create new instance of this class
			concept = new BerkeleyConcept(sid, berkeley, id, pid, annotation,
					description, elementOf, ofType);

			// close streams
			dis.close();
			bais.close();

			// try to prevent memory leaks
			dis = null;
			bais = null;

		} catch (IOException ioe) {
			throw new StorageException(ioe.getMessage());
		}

		// return new Berkeley concept or null
		return concept;
	}

	private UpdateListener updateListener;

	// backing Berkeley environment
	private transient BerkeleyEnv berkeley;

	// catches the byte[] of the concept id
	private byte[] idarray;

	/**
	 * Constructor which fills all fields of Concept and initialises empty
	 * HashMaps for possible concept names and concept accessions
	 * 
	 * @param sid
	 *            unique id
	 * @param berkeley
	 *            BerkeleyEnv
	 * @param id
	 *            unique ID of this Concept
	 * @param pid
	 *            parser assigned ID
	 * @param annotation
	 *            relevant annotations of this Concept
	 * @param description
	 *            every associated description of this Concept
	 * @param elementOf
	 *            DataSource to which this Concept belongs to
	 * @param ofType
	 *            ConceptClass of this Concept
	 * @throws AccessDeniedException
	 */
	BerkeleyConcept(long sid, BerkeleyEnv berkeley, int id, String pid,
			String annotation, String description, DataSource elementOf,
			ConceptClass ofType) {
		super(sid, id, pid, annotation, description, elementOf, ofType);
		this.berkeley = berkeley;
		this.idarray = berkeley.convert(id).getData();
	}

	@Override
	public void fireUpdateEvent() {
		if (updateListener != null)
			updateListener.performUpdate(this);
	}

	/**
	 * Updates the persistent layer for a given property aspect of a
	 * BerkeleyConcept.
	 * 
	 * @param o
	 *            Object to update
	 */
	public void performUpdate(ONDEXAssociable o) {
		if (o instanceof BerkeleyConceptAttribute) {
			BerkeleyConceptAttribute gds = (BerkeleyConceptAttribute) o;
			BerkeleyIntegerName key = new BerkeleyIntegerName(this.getId(), gds
					.getOfType().getId());
			berkeley.deleteFromDatabase(ConceptAttribute.class, key);
			berkeley.insertIntoDatabase(ConceptAttribute.class, key,
					((BerkeleyConceptAttribute) gds).serialise());
			key = null;
		} else if (o instanceof BerkeleyConceptAccession) {
			BerkeleyConceptAccession ca = (BerkeleyConceptAccession) o;
			BerkeleyIntegerName key = new BerkeleyIntegerName(this.getId(),
					ca.getAccession() + ca.getElementOf().getId());
			berkeley.deleteFromDatabase(ConceptAccession.class, key);
			berkeley.insertIntoDatabase(ConceptAccession.class, key,
					ca.serialise());
			key = null;
		} else if (o instanceof BerkeleyConceptName) {
			BerkeleyConceptName cn = (BerkeleyConceptName) o;
			BerkeleyIntegerName key = new BerkeleyIntegerName(this.getId(),
					cn.getName());
			berkeley.deleteFromDatabase(ConceptName.class, key);
			berkeley.insertIntoDatabase(ConceptName.class, key, cn.serialise());
			key = null;
		} else if (o instanceof BerkeleyEvidenceType) {
			BerkeleyEvidenceType et = (BerkeleyEvidenceType) o;
			berkeley.deleteFromDupDatabase(EvidenceType.class, this.getId(),
					et.serialise());
			berkeley.insertIntoDupDatabase(EvidenceType.class, this.getId(),
					et.serialise());
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
	 * @return serialisation in the form of byte[]
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
			dos.writeUTF(pid);
			dos.writeUTF(annotation);
			dos.writeUTF(description);
			dos.writeUTF(elementOf.getId());
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
			throw new StorageException(ioe);
		}

		// return byte array
		return retVal;
	}

	@Override
	public void setAnnotation(String s) {
		super.setAnnotation(s);
		fireUpdateEvent();
	}

	@Override
	public void setDescription(String s) {
		super.setDescription(s);
		fireUpdateEvent();
	}

	@Override
	public void setPID(String pid) {
		super.setPID(pid);
		fireUpdateEvent();
	}

	@Override
	public void setUpdateListener(UpdateListener l) {
		updateListener = l;
	}

	private void fireEventOccurred(EventType e) {
		ONDEXEventHandler.getEventHandlerForSID(getSID()).fireEventOccurred(e);
	}

	@Override
	protected boolean dropEvidenceType(EvidenceType evidencetype) {
		berkeley.deleteFromDupDatabase(Integer.class, evidencetype, idarray);
		return berkeley.deleteFromDupDatabase(EvidenceType.class, id,
				((BerkeleyEvidenceType) evidencetype).serialise());
	}

	@Override
	protected boolean dropTag(ONDEXConcept concept) {

		if (concept instanceof BerkeleyConcept) {
			berkeley.deleteFromDupDatabase(Integer.class, concept.getId(),
					idarray);
			return berkeley.deleteFromDupDatabase(BerkeleyConcept.class,
					getId(), ((BerkeleyConcept) concept).serialise());
		} else
			throw new UnsupportedOperationException("Tag " + concept.getId()
					+ " of class " + concept.getClass()
					+ " could not be saved as it is not BerkeleyConcept");
	}

	@Override
	protected boolean removeConceptAccession(String accession,
			DataSource elementOf) {
		return berkeley.deleteFromDatabase(ConceptAccession.class,
				new BerkeleyIntegerName(id, accession + elementOf.getId()));
	}

	@Override
	protected boolean removeConceptAttribute(AttributeName attributeName) {
		berkeley.deleteFromDupDatabase(Integer.class, attributeName, idarray);
		return berkeley.deleteFromDatabase(ConceptAttribute.class,
				new BerkeleyIntegerName(id, attributeName.getId()));
	}

	@Override
	protected boolean removeConceptName(String name) {
		return berkeley.deleteFromDatabase(ConceptName.class,
				new BerkeleyIntegerName(id, name));
	}

	@Override
	protected ConceptAccession retrieveConceptAccession(String accession,
			DataSource elementOf) {

		byte[] array = berkeley.getFromDatabase(ConceptAccession.class,
				new BerkeleyIntegerName(id, accession + elementOf.getId()));

		if (array != null) {
			BerkeleyConceptAccession conceptAccession = BerkeleyConceptAccession
					.deserialise(berkeley.getAbstractONDEXGraph(), array);
			array = null;
			conceptAccession.setUpdateListener(this);
			return conceptAccession;
		}

		return null;
	}

	@Override
	protected Set<ConceptAccession> retrieveConceptAccessionAll() {
		return new BerkeleySecBrowser<ConceptAccession>(berkeley, this,
				ConceptAccession.class, new BerkeleyIntegerName(id, null));
	}

	@Override
	protected Attribute retrieveConceptAttribute(AttributeName attributeName) {

		byte[] array = berkeley.getFromDatabase(ConceptAttribute.class,
				new BerkeleyIntegerName(id, attributeName.getId()));

		if (array != null) {
			BerkeleyConceptAttribute attribute = BerkeleyConceptAttribute
					.deserialise(berkeley.getAbstractONDEXGraph(), array);
			array = null;
			attribute.setUpdateListener(this);
			return attribute;
		}

		return null;
	}

	@Override
	protected Set<Attribute> retrieveConceptAttributeAll() {
		return new BerkeleySecBrowser<Attribute>(berkeley, this,
				ConceptAttribute.class, new BerkeleyIntegerName(id, null));
	}

	@Override
	protected ConceptName retrieveConceptName(String name) {

		byte[] array = berkeley.getFromDatabase(ConceptName.class,
				new BerkeleyIntegerName(id, name));

		if (array != null) {
			BerkeleyConceptName conceptName = BerkeleyConceptName.deserialise(
					berkeley.getAbstractONDEXGraph(), array);
			array = null;
			conceptName.setUpdateListener(this);
			return conceptName;
		}

		return null;
	}

	@Override
	protected Set<ConceptName> retrieveConceptNameAll() {
		return new BerkeleySecBrowser<ConceptName>(berkeley, this,
				ConceptName.class, new BerkeleyIntegerName(id, null));
	}

	@Override
	protected Set<EvidenceType> retrieveEvidenceTypeAll() {
		return new BerkeleyDupBrowser<EvidenceType>(berkeley, this,
				EvidenceType.class, id);
	}

	@Override
	protected ConceptName retrievePreferredConceptName() {
		for (ConceptName cn : retrieveConceptNameAll()) {
			if (cn.isPreferred()) {
				((Updatable) cn).setUpdateListener(this);
				return cn;
			}
		}
		return null;
	}

	@Override
	protected Set<ONDEXConcept> retrieveTagAll() {
		return new BerkeleyDupBrowser<ONDEXConcept>(berkeley, this,
				BerkeleyConcept.class, id);
	}

	@Override
	protected void saveEvidenceType(EvidenceType evidencetype) {
		berkeley.insertIntoDupDatabase(EvidenceType.class, id,
				((BerkeleyEvidenceType) evidencetype).serialise());
		berkeley.insertIntoDupDatabase(Integer.class, evidencetype, idarray);
	}

	@Override
	protected void saveTag(ONDEXConcept concept) {

		if (concept instanceof BerkeleyConcept) {
			berkeley.insertIntoDupDatabase(BerkeleyConcept.class,
					Integer.valueOf(getId()),
					((BerkeleyConcept) concept).serialise());
			berkeley.insertIntoDupDatabase(Integer.class, concept.getId(),
					idarray);
		} else
			throw new UnsupportedOperationException("Tag " + concept.getId()
					+ " of class " + concept.getClass()
					+ " could not be saved as it is not BerkeleyConcept");
	}

	@Override
	protected ConceptAccession storeConceptAccession(
			ConceptAccession conceptAccession) {

		BerkeleyIntegerName key = new BerkeleyIntegerName(id,
				conceptAccession.getAccession()
						+ conceptAccession.getElementOf().getId());
		BerkeleyConceptAccession berkeleyConceptAccession = BerkeleyConceptAccession
				.convert(conceptAccession);

		if (berkeley.existsInDatabase(ConceptAccession.class, key)) {
			fireEventOccurred(new DuplicatedEntryEvent(
					Config.properties.getProperty("persistent.BerkeleyConcept.DuplicatedConceptAccession")
							+ conceptAccession.getAccession()
							+ " ("
							+ conceptAccession.getElementOf().getId() + ")",
					"[BerkeleyConcept - storeConceptAccession]"));
		} else {
			berkeley.insertIntoDatabase(ConceptAccession.class, key,
					berkeleyConceptAccession.serialise());
		}
		
		key = null;
		berkeleyConceptAccession.setUpdateListener(this);
		return berkeleyConceptAccession;
	}

	@Override
	protected Attribute storeConceptAttribute(Attribute attribute) {

		BerkeleyIntegerName key = new BerkeleyIntegerName(id, attribute
				.getOfType().getId());
		BerkeleyConceptAttribute berkeleyAttribute = BerkeleyConceptAttribute
				.convert(attribute);

		if (berkeley.existsInDatabase(ConceptAttribute.class, key)) {
			fireEventOccurred(new DuplicatedEntryEvent(
					Config.properties.getProperty("persistent.BerkeleyConcept.DuplicatedAttributeName")
							+ attribute.getOfType().getId(),
					"[BerkeleyConcept - storeConceptAttribute]"));
		} else {
			berkeley.insertIntoDatabase(ConceptAttribute.class, key,
					berkeleyAttribute.serialise());
			berkeley.insertIntoDupDatabase(Integer.class,
					attribute.getOfType(), idarray);
		}
		
		key = null;
		berkeleyAttribute.setUpdateListener(this);
		return berkeleyAttribute;
	}

	@Override
	protected ConceptName storeConceptName(ConceptName conceptName) {

		BerkeleyIntegerName key = new BerkeleyIntegerName(id,
				conceptName.getName());
		BerkeleyConceptName berkeleyConceptName = BerkeleyConceptName
				.convert(conceptName);

		if (berkeley.existsInDatabase(ConceptName.class, key)) {
			fireEventOccurred(new DuplicatedEntryEvent(
					Config.properties.getProperty("persistent.BerkeleyConcept.DuplicatedConceptName")
							+ conceptName.getName(),
					"[BerkeleyConcept - storeConceptName]"));
			conceptName = BerkeleyConceptName.deserialise(
					berkeley.getAbstractONDEXGraph(),
					berkeley.getFromDatabase(ConceptName.class, key));
		} else {
			berkeley.insertIntoDatabase(ConceptName.class, key,
					berkeleyConceptName.serialise());
		}
		
		key = null;
		berkeleyConceptName.setUpdateListener(this);
		return berkeleyConceptName;

	}
}
