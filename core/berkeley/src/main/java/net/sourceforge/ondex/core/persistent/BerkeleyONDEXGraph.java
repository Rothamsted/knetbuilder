package net.sourceforge.ondex.core.persistent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.BitSet;
import java.util.Collection;
import java.util.Set;

import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXAssociable;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.base.AbstractONDEXGraph;
import net.sourceforge.ondex.core.base.ConceptAttribute;
import net.sourceforge.ondex.core.base.RelationAttribute;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.core.util.UpdateListener;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.DatabaseErrorEvent;
import net.sourceforge.ondex.event.type.DuplicatedEntryEvent;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.DataLossException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.exception.type.RetrievalException;
import net.sourceforge.ondex.exception.type.StorageException;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * This class represents a persistent implementation of the AbstractONDEXGraph.
 * It uses the Berkeley Java Edition database.
 * 
 * @author taubertj
 */
public class BerkeleyONDEXGraph extends AbstractONDEXGraph implements
		UpdateListener, BerkeleySerializable {

	// backing berkeley environment
	private transient BerkeleyEnv berkeley;

	/**
	 * Constructor which sets the name of the graph to the given name.
	 * 
	 * @param sid
	 *            unique id
	 * @param name
	 *            name of graph
	 * @param berkeley
	 *            Berkeley environment
	 */
	BerkeleyONDEXGraph(long sid, String name, BerkeleyEnv berkeley) {
		super(sid, name, new BerkeleyONDEXGraphMetaData(berkeley));
		this.berkeley = berkeley;
		this.lastIdForConcept = berkeley.getLastConceptID();
		this.lastIdForRelation = berkeley.getLastRelationID();
	}

	public void deserialize(byte[] array) {
		try {
			// byte input stream for byte array
			ByteArrayInputStream bais = new ByteArrayInputStream(array);

			// object input stream for deserialisation
			DataInputStream dis = new DataInputStream(new BufferedInputStream(
					bais));

			// get serialised class content
			sid = dis.readLong();

			dis.close();
			bais.close();

		} catch (IOException ioe) {
			throw new RetrievalException(ioe.getMessage());
		}
	}

	/**
	 * Updates the persistent layer for a given property aspect of a
	 * BerkeleyConcept or a BerkeleyRelation.
	 * 
	 * @param o
	 *            Object to update
	 */
	public void performUpdate(ONDEXAssociable o) {
		if (o instanceof BerkeleyConcept) {
			BerkeleyConcept c = (BerkeleyConcept) o;

			// delete old serialisation of concept
			berkeley.deleteFromDatabase(BerkeleyConcept.class, c.getId());

			// insert new serialisation of concept
			berkeley.insertIntoDatabase(BerkeleyConcept.class, c.getId(),
					c.serialise());
		} else if (o instanceof BerkeleyRelation) {
			BerkeleyRelation r = (BerkeleyRelation) o;

			// delete old serialisation of relation
			berkeley.deleteFromDatabase(BerkeleyRelation.class, r.getKey());
			berkeley.deleteFromDatabase(Integer.class, r.getId());

			// insert new serialisation of relation
			byte[] array = r.serialise();
			berkeley.insertIntoDatabase(BerkeleyRelation.class, r.getKey(),
					array);
			berkeley.insertIntoDatabase(Integer.class, r.getId(), array);
		}
	}

	@Override
	protected ONDEXConcept removeConcept(int id) throws AccessDeniedException {
		byte[] array = berkeley.getFromDatabase(BerkeleyConcept.class, id);

		if (array != null) {
			BerkeleyConcept c = BerkeleyConcept.deserialise(berkeley, array);

			// remove references to concept
			byte[] idarray = berkeley.convert(id).getData();
			berkeley.deleteFromDupDatabase(Integer.class, c.getElementOf(),
					idarray);
			berkeley.deleteFromDupDatabase(Integer.class, c.getOfType(),
					idarray);
			for (Attribute cattribute : c.getAttributes()) {
				berkeley.deleteFromDupDatabase(Integer.class,
						cattribute.getOfType(), idarray);
				BerkeleyIntegerName key = new BerkeleyIntegerName(id,
						cattribute.getOfType().getId());
				berkeley.deleteFromDatabase(ConceptAttribute.class, key);
			}

			for (EvidenceType et : c.getEvidence()) {
				berkeley.deleteFromDupDatabase(Integer.class, et, idarray);
			}

			for (ONDEXConcept ac : c.getTags()) {
				berkeley.deleteFromDupDatabase(Integer.class, ac.getId(),
						idarray);
			}

			berkeley.deleteFromDatabase(BerkeleyConcept.class, id);
			return c;
		}
		return null;
	}

	@Override
	protected boolean removeRelation(int id)
			throws UnsupportedOperationException {
		byte[] array = berkeley.getFromDatabase(Integer.class, id);
		if (berkeley.deleteFromDatabase(Integer.class, id)) {
			BerkeleyRelation r = BerkeleyRelation.deserialise(berkeley, array);
			return this.removeRelation(r.getFromConcept(), r.getToConcept(),
					r.getOfType());
		} else {
			return false;
		}
	}

	@Override
	protected boolean removeRelation(ONDEXConcept fromConcept,
			ONDEXConcept toConcept, RelationType ofType)
			throws AccessDeniedException {
		BerkeleyRelationKey id = new BerkeleyRelationKey(sid,
				fromConcept.getId(), toConcept.getId(), ofType.getId());
		byte[] array = berkeley.getFromDatabase(BerkeleyRelation.class, id);
		if (berkeley.deleteFromDatabase(BerkeleyRelation.class, id)) {
			BerkeleyRelation r = BerkeleyRelation.deserialise(berkeley, array);
			berkeley.deleteFromDatabase(Integer.class, r.getId());
			byte[] idarray = berkeley.convert(
					new BerkeleyIntegerName(r.getId(), "")).getData();

			berkeley.deleteFromDupDatabase(BerkeleyIntegerName.class, ofType,
					idarray);
			berkeley.deleteFromDupDatabase(BerkeleyIntegerName.class,
					fromConcept.getId(), idarray);
			berkeley.deleteFromDupDatabase(BerkeleyIntegerName.class,
					fromConcept.getElementOf(), idarray);
			berkeley.deleteFromDupDatabase(BerkeleyIntegerName.class,
					fromConcept.getOfType(), idarray);
			berkeley.deleteFromDupDatabase(BerkeleyIntegerName.class,
					toConcept.getId(), idarray);
			berkeley.deleteFromDupDatabase(BerkeleyIntegerName.class,
					toConcept.getElementOf(), idarray);
			berkeley.deleteFromDupDatabase(BerkeleyIntegerName.class,
					toConcept.getOfType(), idarray);

			for (Attribute attribute : r.getAttributes()) {
				AttributeName an = attribute.getOfType();
				berkeley.deleteFromDupDatabase(BerkeleyIntegerName.class, an,
						idarray);
				BerkeleyRelationKeyName key = new BerkeleyRelationKeyName(id,
						an.getId());
				berkeley.deleteFromDatabase(RelationAttribute.class, key);
			}

			for (EvidenceType et : r.getEvidence()) {
				berkeley.deleteFromDupDatabase(BerkeleyIntegerName.class, et,
						idarray);
			}

			for (ONDEXConcept ac : r.getTags()) {
				berkeley.deleteFromDupDatabase(Integer.class, ac.getId(),
						idarray);
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	protected ONDEXConcept retrieveConcept(int id) {
		byte[] array = berkeley.getFromDatabase(BerkeleyConcept.class, id);
		if (array != null) {
			BerkeleyConcept c = BerkeleyConcept.deserialise(berkeley, array);
			c.setUpdateListener(this);
			return c;
		}
		return null;
	}

	@Override
	protected Set<ONDEXConcept> retrieveConceptAll() {
		BerkeleyBrowser<ONDEXConcept> browser = new BerkeleyBrowser<ONDEXConcept>(
				berkeley, this, BerkeleyConcept.class);
		if (browser.size() > 0) {
			BitSet set = new BitSet(browser.size());
			for (ONDEXConcept c : browser) {
				int id;
				try {
					id = c.getId();
					set.set(id);
				} catch (AccessDeniedException e) {

				}

			}
			return BitSetFunctions.create(this, ONDEXConcept.class, set);
		} else {
			return BitSetFunctions.create(this, ONDEXConcept.class,
					new BitSet());
		}
	}

	@Override
	protected Set<ONDEXConcept> retrieveConceptAllAttributeName(AttributeName an) {
		BerkeleyDupBrowser<Integer> browser = new BerkeleyDupBrowser<Integer>(
				berkeley, this, Integer.class, an);
		if (browser.size() > 0) {
			BitSet set = new BitSet(browser.size());
			for (Integer id : browser) {
				set.set(id);
			}
			return BitSetFunctions.create(this, ONDEXConcept.class, set);
		} else {
			return BitSetFunctions.create(this, ONDEXConcept.class,
					new BitSet());
		}
	}

	@Override
	protected Set<ONDEXConcept> retrieveConceptAllConceptClass(ConceptClass cc) {
		BerkeleyDupBrowser<Integer> browser = new BerkeleyDupBrowser<Integer>(
				berkeley, this, Integer.class, cc);
		if (browser.size() > 0) {
			BitSet set = new BitSet(browser.size());
			for (Integer id : browser) {
				set.set(id);
			}
			return BitSetFunctions.create(this, ONDEXConcept.class, set);
		} else {
			return BitSetFunctions.create(this, ONDEXConcept.class,
					new BitSet());
		}
	}

	@Override
	protected Set<ONDEXConcept> retrieveConceptAllDataSource(
			DataSource dataSource) {
		BerkeleyDupBrowser<Integer> browser = new BerkeleyDupBrowser<Integer>(
				berkeley, this, Integer.class, dataSource);
		if (browser.size() > 0) {
			BitSet set = new BitSet(browser.size());
			for (Integer id : browser) {
				set.set(id);
			}
			return BitSetFunctions.create(this, ONDEXConcept.class, set);
		} else {
			return BitSetFunctions.create(this, ONDEXConcept.class,
					new BitSet());
		}
	}

	@Override
	protected Set<ONDEXConcept> retrieveConceptAllEvidenceType(EvidenceType et) {
		BerkeleyDupBrowser<Integer> browser = new BerkeleyDupBrowser<Integer>(
				berkeley, this, Integer.class, et);
		if (browser.size() > 0) {
			BitSet set = new BitSet(browser.size());
			for (Integer id : browser) {
				set.set(id);
			}
			return BitSetFunctions.create(this, ONDEXConcept.class, set);
		} else {
			return BitSetFunctions.create(this, ONDEXConcept.class,
					new BitSet());
		}
	}

	@Override
	protected Set<ONDEXConcept> retrieveConceptAllTag(ONDEXConcept concept)
			throws AccessDeniedException {
		BerkeleyDupBrowser<Integer> browser = new BerkeleyDupBrowser<Integer>(
				berkeley, this, Integer.class, concept.getId());
		if (browser.size() > 0) {
			BitSet set = new BitSet();
			for (Integer id : browser) {
				set.set(id);
			}
			return BitSetFunctions.create(this, ONDEXConcept.class, set);
		} else {
			return BitSetFunctions.create(this, ONDEXConcept.class,
					new BitSet());
		}
	}

	@Override
	protected ONDEXRelation retrieveRelation(int id) {
		byte[] array = berkeley.getFromDatabase(Integer.class, id);
		if (array != null) {
			BerkeleyRelation r = BerkeleyRelation.deserialise(berkeley, array);
			r.setUpdateListener(this);
			return r;
		}
		return null;
	}

	@Override
	protected ONDEXRelation retrieveRelation(ONDEXConcept fromConcept,
			ONDEXConcept toConcept, RelationType ofType)
			throws AccessDeniedException {

		BerkeleyRelationKey id = new BerkeleyRelationKey(sid,
				fromConcept.getId(), toConcept.getId(), ofType.getId());
		byte[] array = berkeley.getFromDatabase(BerkeleyRelation.class, id);
		if (array != null) {
			BerkeleyRelation r = BerkeleyRelation.deserialise(berkeley, array);
			r.setUpdateListener(this);
			return r;
		}
		return null;
	}

	@Override
	protected Set<ONDEXRelation> retrieveRelationAll() {
		BerkeleyBrowser<ONDEXRelation> browser = new BerkeleyBrowser<ONDEXRelation>(
				berkeley, null, BerkeleyRelation.class);
		if (browser.size() > 0) {
			BitSet set = new BitSet(browser.size());
			for (ONDEXRelation r : browser) {
				try {
					int id = r.getId();
					set.set(id);
				} catch (AccessDeniedException e) {

				}
			}
			return BitSetFunctions.create(this, ONDEXRelation.class, set);
		} else {
			return BitSetFunctions.create(this, ONDEXRelation.class,
					new BitSet());
		}
	}

	@Override
	protected Set<ONDEXRelation> retrieveRelationAllAttributeName(
			AttributeName an) {
		BerkeleyDupBrowser<BerkeleyIntegerName> browser = new BerkeleyDupBrowser<BerkeleyIntegerName>(
				berkeley, this, BerkeleyIntegerName.class, an);
		if (browser.size() > 0) {
			BitSet set = new BitSet(browser.size());
			for (BerkeleyIntegerName id : browser) {
				set.set(id.getKey());
			}
			return BitSetFunctions.create(this, ONDEXRelation.class, set);
		} else {
			return BitSetFunctions.create(this, ONDEXRelation.class,
					new BitSet());
		}
	}

	@Override
	protected Set<ONDEXRelation> retrieveRelationAllConcept(ONDEXConcept c)
			throws AccessDeniedException {
		BerkeleyDupBrowser<BerkeleyIntegerName> browser = new BerkeleyDupBrowser<BerkeleyIntegerName>(
				berkeley, this, BerkeleyIntegerName.class, c.getId());
		if (browser.size() > 0) {
			BitSet set = new BitSet(browser.size());
			for (BerkeleyIntegerName id : browser) {
				set.set(id.getKey());
			}
			return BitSetFunctions.create(this, ONDEXRelation.class, set);
		} else {
			return BitSetFunctions.create(this, ONDEXRelation.class,
					new BitSet());
		}
	}

	@Override
	protected Set<ONDEXRelation> retrieveRelationAllConceptClass(ConceptClass cc) {
		BerkeleyDupBrowser<BerkeleyIntegerName> browser = new BerkeleyDupBrowser<BerkeleyIntegerName>(
				berkeley, this, BerkeleyIntegerName.class, cc);
		if (browser.size() > 0) {
			BitSet set = new BitSet(browser.size());
			for (BerkeleyIntegerName id : browser) {
				set.set(id.getKey());
			}
			return BitSetFunctions.create(this, ONDEXRelation.class, set);
		} else {
			return BitSetFunctions.create(this, ONDEXRelation.class,
					new BitSet());
		}
	}

	@Override
	protected Set<ONDEXRelation> retrieveRelationAllDataSource(
			DataSource dataSource) {
		BerkeleyDupBrowser<BerkeleyIntegerName> browser = new BerkeleyDupBrowser<BerkeleyIntegerName>(
				berkeley, this, BerkeleyIntegerName.class, dataSource);
		if (browser.size() > 0) {
			BitSet set = new BitSet(browser.size());
			for (BerkeleyIntegerName id : browser) {
				set.set(id.getKey());
			}
			return BitSetFunctions.create(this, ONDEXRelation.class, set);
		} else {
			return BitSetFunctions.create(this, ONDEXRelation.class,
					new BitSet());
		}
	}

	@Override
	protected Set<ONDEXRelation> retrieveRelationAllEvidenceType(EvidenceType et) {
		BerkeleyDupBrowser<BerkeleyIntegerName> browser = new BerkeleyDupBrowser<BerkeleyIntegerName>(
				berkeley, this, BerkeleyIntegerName.class, et);
		if (browser.size() > 0) {
			BitSet set = new BitSet(browser.size());
			for (BerkeleyIntegerName id : browser) {
				set.set(id.getKey());
			}
			return BitSetFunctions.create(this, ONDEXRelation.class, set);
		} else {
			return BitSetFunctions.create(this, ONDEXRelation.class,
					new BitSet());
		}
	}

	@Override
	protected Set<ONDEXRelation> retrieveRelationAllRelationType(RelationType rt) {
		BerkeleyDupBrowser<BerkeleyIntegerName> browser = new BerkeleyDupBrowser<BerkeleyIntegerName>(
				berkeley, this, BerkeleyIntegerName.class, rt);
		if (browser.size() > 0) {
			BitSet set = new BitSet(browser.size());
			for (BerkeleyIntegerName id : browser) {
				set.set(id.getKey());
			}
			return BitSetFunctions.create(this, ONDEXRelation.class, set);
		} else {
			return BitSetFunctions.create(this, ONDEXRelation.class,
					new BitSet());
		}
	}

	@Override
	protected Set<ONDEXRelation> retrieveRelationAllTag(ONDEXConcept concept)
			throws AccessDeniedException {
		BerkeleyDupBrowser<BerkeleyIntegerName> browser = new BerkeleyDupBrowser<BerkeleyIntegerName>(
				berkeley, this, BerkeleyIntegerName.class, concept.getId());
		if (browser.size() > 0) {
			BitSet set = new BitSet(browser.size());
			for (BerkeleyIntegerName id : browser) {
				set.set(id.getKey());
			}
			return BitSetFunctions.create(this, ONDEXRelation.class, set);
		} else {
			return BitSetFunctions.create(this, ONDEXRelation.class,
					new BitSet());
		}
	}

	@Override
	protected Set<ONDEXConcept> retrieveTags() {
		BitSet set = new BitSet();

		Database myDb = berkeley.getDupDatabases().get(BerkeleyConcept.class);
		if (myDb != null) {

			CursorConfig cc = new CursorConfig();
			cc.setReadUncommitted(true);

			Cursor cursor = myDb.openCursor(null, cc);

			try {
				// convert key
				DatabaseEntry theKey = new DatabaseEntry();

				// contains return data
				DatabaseEntry theData = new DatabaseEntry();

				// perform the get
				while (cursor.getNext(theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
					BerkeleyConcept o = BerkeleyConcept.deserialise(berkeley,
							theData.getData());
					set.set(o.getId());
					if (cursor.count() > 1) {
						while (cursor.getNextDup(theKey, theData,
								LockMode.DEFAULT) == OperationStatus.SUCCESS) {
							o = BerkeleyConcept.deserialise(berkeley,
									theData.getData());
							set.set(o.getId());
						}
					}
				}
			} catch (DatabaseException dbe) {
				DatabaseErrorEvent de = new DatabaseErrorEvent(
						dbe.getMessage(), "[BerkeleyEnv - getFromDatabase]");
				berkeley.fireEventOccurred(de);
			} finally {
				// do not forget to close the cursor
				cursor.close();
			}
		}

		return BitSetFunctions.create(this, ONDEXConcept.class, set);
	}

	@Override
	public byte[] serialise() {
		return serialise(getSID());
	}

	@Override
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
			dos.flush();

			retVal = baos.toByteArray();

			// make sure streams are closed
			dos.close();
			baos.close();
		} catch (IOException ioe) {
			// fireEventOccurred(new SerialisationFailedEvent(ioe.getMessage(),
			// "[AbstractRelation - serialise]"));
			throw new StorageException(ioe.getMessage());
		}

		// return byte array
		return retVal;
	}

	/**
	 * @throws NullValueException
	 *             if evidence set contains null values
	 * @see net.sourceforge.ondex.core.base.AbstractONDEXGraph#storeConcept(long,
	 *      int, java.lang.String, java.lang.String, java.lang.String,
	 *      net.sourceforge.ondex.core.DataSource,
	 *      net.sourceforge.ondex.core.ConceptClass, java.util.Collection)
	 */
	@Override
	protected ONDEXConcept storeConcept(long sid, int id, String pid,
			String annotation, String description, DataSource elementOf,
			ConceptClass ofType, Collection<EvidenceType> evidence)
			throws NullValueException {
		try {
			BerkeleyConcept c = new BerkeleyConcept(sid, berkeley, id, pid,
					annotation, description, elementOf, ofType);

			if (berkeley.existsInDatabase(BerkeleyConcept.class, c.getId())) {
				ONDEXEventHandler
						.getEventHandlerForSID(getSID())
						.fireEventOccurred(
								new DuplicatedEntryEvent(
										Config.properties
												.getProperty("persistent.BerkeleyONDEXGraph.DuplicatedConcept")
												+ c.getId(),
										"[BerkeleyONDEXGraph - storeConcept]"));
				c = BerkeleyConcept.deserialise(
						berkeley,
						berkeley.getFromDatabase(BerkeleyConcept.class,
								c.getId()));
			} else {
				// we do this here to avoid unnecessary evidence duplication
				// errors if there is a Concept duplication error
				for (EvidenceType anEvidence : evidence) {
					c.addEvidenceType(anEvidence);
				}
				byte[] array = c.serialise();
				berkeley.insertIntoDatabase(BerkeleyConcept.class, c.getId(),
						array);
				byte[] idarray = berkeley.convert(id).getData();
				berkeley.insertIntoDupDatabase(Integer.class, elementOf,
						idarray);
				berkeley.insertIntoDupDatabase(Integer.class, ofType, idarray);
			}
			c.setUpdateListener(this);
			return c;
		} catch (AccessDeniedException e) {
			throw new DataLossException(
					"Concept could not be created because of a "
							+ "paradox permission set.\n"
							+ "Please change your default permissions.");
		}
	}

	/**
	 * @throws AccessDeniedException
	 *             if given parameters are unreadable.
	 * @throws NullValueException
	 *             if evidence list contains null values.
	 * @see net.sourceforge.ondex.core.base.AbstractONDEXGraph#storeRelation(long,
	 *      int, net.sourceforge.ondex.core.ONDEXConcept,
	 *      net.sourceforge.ondex.core.ONDEXConcept,
	 *      net.sourceforge.ondex.core.ONDEXConcept,
	 *      net.sourceforge.ondex.core.RelationType, java.util.Collection)
	 */
	@Override
	protected ONDEXRelation storeRelation(long sid, int id,
			ONDEXConcept fromConcept, ONDEXConcept toConcept,
			RelationType ofType, Collection<EvidenceType> evidence)
			throws AccessDeniedException, NullValueException {
		BerkeleyRelation r = new BerkeleyRelation(sid, berkeley, id,
				fromConcept, toConcept, ofType);

		if (berkeley.existsInDatabase(BerkeleyRelation.class, r.getKey())) {
			ONDEXEventHandler
					.getEventHandlerForSID(getSID())
					.fireEventOccurred(
							new DuplicatedEntryEvent(
									Config.properties
											.getProperty("persistent.BerkeleyONDEXGraph.DuplicatedRelation")
											+ r.getKey(),
									"[BerkeleyONDEXGraph - storeRelation]"));
			return BerkeleyRelation.deserialise(berkeley, berkeley
					.getFromDatabase(BerkeleyRelation.class, r.getKey()));
		} else {
			// we do this here to avoid unnecessary evidence duplication errors
			// if there is a Relation duplication error
			for (EvidenceType anEvidence : evidence) {
				r.addEvidenceType(anEvidence);
			}

			byte[] array = r.serialise();
			berkeley.insertIntoDatabase(BerkeleyRelation.class, r.getKey(),
					array);
			berkeley.insertIntoDatabase(Integer.class, r.getId(), array);
			byte[] idarray = berkeley.convert(
					new BerkeleyIntegerName(r.getId(), "")).getData();

			// store references to relation type
			berkeley.insertIntoDupDatabase(BerkeleyIntegerName.class, ofType,
					idarray);

			// store references to fromConcept and DataSource
			berkeley.insertIntoDupDatabase(BerkeleyIntegerName.class,
					fromConcept.getId(), idarray);
			berkeley.insertIntoDupDatabase(BerkeleyIntegerName.class,
					fromConcept.getElementOf(), idarray);
			berkeley.insertIntoDupDatabase(BerkeleyIntegerName.class,
					fromConcept.getOfType(), idarray);

			// store references to toConcept and DataSource
			berkeley.insertIntoDupDatabase(BerkeleyIntegerName.class,
					toConcept.getId(), idarray);
			berkeley.insertIntoDupDatabase(BerkeleyIntegerName.class,
					toConcept.getElementOf(), idarray);
			berkeley.insertIntoDupDatabase(BerkeleyIntegerName.class,
					toConcept.getOfType(), idarray);

		}
		r.setUpdateListener(this);
		return r;
	}

}
