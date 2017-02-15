package net.sourceforge.ondex.core.persistent;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.core.util.UpdateListener;
import net.sourceforge.ondex.event.type.DatabaseErrorEvent;
import net.sourceforge.ondex.exception.type.AccessDeniedException;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * Berkeley implementation of the Set.
 * 
 * @author taubertj
 * @param <AnyType>
 */
public class BerkeleyBrowser<AnyType> extends AbstractSet<AnyType> {

	// berkeley env for events and dbs
	private final BerkeleyEnv berkeley;

	// global cursor config for this browser
	private CursorConfig cc;

	// current class used
	private final Class<? extends AnyType> clazz;

	// listener for updates of returned entities
	private UpdateListener listener;

	// unique to this browser used as an id cursor index
	private final Object proxyObject = new Object();

	/**
	 * Constructor for this database browser.
	 * 
	 * @param berkeley
	 *            BerkeleyEnv
	 * @param l
	 *            UpdateListener
	 * @param clazz
	 *            Class<AnyType>
	 */
	public BerkeleyBrowser(BerkeleyEnv berkeley, UpdateListener l,
			Class<? extends AnyType> clazz) {

		// berkeley env for event propagation
		this.berkeley = berkeley;

		// persistent update listener
		this.listener = l;

		// class for objects
		this.clazz = clazz;

		// set cursor to read uncommitted data
		cc = new CursorConfig();
		cc.setReadUncommitted(true);
	}

	@Override
	public boolean contains(Object o) throws AccessDeniedException {

		// obvious case
		if (isEmpty()) {
			return false;
		}

		DatabaseEntry myKey;

		if (o instanceof ONDEXRelation) {
			ONDEXRelation r = (ONDEXRelation) o;
			myKey = berkeley.convert(r.getKey());
		} else if (o instanceof ONDEXConcept) {
			ONDEXConcept c = (ONDEXConcept) o;
			myKey = berkeley.convert(c.getId());
		} else if (o instanceof DataSource) {
			DataSource dataSource = (DataSource) o;
			myKey = berkeley.convert(dataSource.getId());
		} else if (o instanceof ConceptClass) {
			ConceptClass cc = (ConceptClass) o;
			myKey = berkeley.convert(cc.getId());
		} else if (o instanceof AttributeName) {
			AttributeName an = (AttributeName) o;
			myKey = berkeley.convert(an.getId());
		} else if (o instanceof Unit) {
			Unit u = (Unit) o;
			myKey = berkeley.convert(u.getId());
		} else if (o instanceof EvidenceType) {
			EvidenceType et = (EvidenceType) o;
			myKey = berkeley.convert(et.getId());
		} else if (o instanceof RelationType) {
			RelationType rt = (RelationType) o;
			myKey = berkeley.convert(rt.getId());
		} else {
			return false;
		}

		// get database for class
		Database myDb = berkeley.getDatabases().get(clazz);
		if (myDb != null) {

			try {

				DatabaseEntry myData = new DatabaseEntry();

				// perform the get
				return myDb.get(null, myKey, myData, LockMode.DEFAULT) == OperationStatus.SUCCESS;

			} catch (DatabaseException dbe) {
				DatabaseErrorEvent de = new DatabaseErrorEvent(
						dbe.getMessage(), "[BerkeleyBrowser - contains]");
				berkeley.fireEventOccurred(de);
				return false;
			}
		}

		return false;
	}

	@Override
	public Iterator<AnyType> iterator() {
		try {

			return new Iterator<AnyType>() {
				// Open the cursor.
				final Cursor cursor = berkeley.openCursor(proxyObject, null,
						clazz, cc);

				// contains return data
				DatabaseEntry theData = new DatabaseEntry();

				// empty search key
				DatabaseEntry theKey = new DatabaseEntry();

				// Position the cursor
				OperationStatus retVal = cursor.getNext(theKey, theData,
						LockMode.DEFAULT);

				// check for at least one element is present
				boolean finished = retVal != OperationStatus.SUCCESS
						|| size() == 0;

				@Override
				public boolean hasNext() {
					return !finished;
				}

				@Override
				public AnyType next() {

					AnyType val = null;

					if (!finished) {
						if (clazz.equals(BerkeleyConcept.class)) {
							BerkeleyConcept o = BerkeleyConcept.deserialise(
									berkeley, theData.getData());
							o.setUpdateListener(listener);
							val = clazz.cast(o);
						} else if (clazz.equals(BerkeleyRelation.class)) {
							BerkeleyRelation o = BerkeleyRelation.deserialise(
									berkeley, theData.getData());
							o.setUpdateListener(listener);
							val = clazz.cast(o);
						} else if (clazz.equals(AttributeName.class)) {
							BerkeleyAttributeName o = BerkeleyAttributeName
									.deserialise(
											berkeley.getAbstractONDEXGraph(),
											theData.getData());
							o.setUpdateListener(listener);
							val = clazz.cast(o);
						} else if (clazz.equals(ConceptClass.class)) {
							BerkeleyConceptClass o = BerkeleyConceptClass
									.deserialise(
											berkeley.getAbstractONDEXGraph(),
											theData.getData());
							o.setUpdateListener(listener);
							val = clazz.cast(o);
						} else if (clazz.equals(DataSource.class)) {
							BerkeleyDataSource o = Persistence.deserialise(
									BerkeleyDataSource.FACTORY,
									theData.getData());
							o.setUpdateListener(listener);
							val = clazz.cast(o);
						} else if (clazz.equals(EvidenceType.class)) {
							BerkeleyEvidenceType o = Persistence.deserialise(
									BerkeleyEvidenceType.FACTORY,
									theData.getData());
							o.setUpdateListener(listener);
							val = clazz.cast(o);
						} else if (clazz.equals(RelationType.class)) {
							BerkeleyRelationType o = BerkeleyRelationType
									.deserialise(
											berkeley.getAbstractONDEXGraph(),
											theData.getData());
							o.setUpdateListener(listener);
							val = clazz.cast(o);
						} else if (clazz.equals(Unit.class)) {
							BerkeleyUnit o = Persistence.deserialise(
									BerkeleyUnit.FACTORY, theData.getData());
							o.setUpdateListener(listener);
							val = clazz.cast(o);
						}

					} else {
						throw new NoSuchElementException();
					}

					// try to retrieve next value in database
					try {
						if (retVal == OperationStatus.SUCCESS) {
							retVal = cursor.getNext(theKey, theData,
									LockMode.DEFAULT);
							if (retVal != OperationStatus.SUCCESS) {
								finished = true;
							}
						} else if (!finished) {
							finished = true;
							berkeley.releaseCursor(proxyObject);
						}
					} catch (DatabaseException dbe) {
						DatabaseErrorEvent de = new DatabaseErrorEvent(
								dbe.getMessage(), "[BerkeleyBrowser - next]");
						berkeley.fireEventOccurred(de);
					}

					return val;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		} catch (DatabaseException dbe) {
			DatabaseErrorEvent de = new DatabaseErrorEvent(dbe.getMessage(),
					"[BerkeleyBrowser - initCursor]");
			berkeley.fireEventOccurred(de);
			throw new Error(dbe);
		}
	}

	@Override
	public int size() {
		Cursor c = berkeley.openCursor(proxyObject, null, clazz, cc);
		DatabaseEntry foundKey = new DatabaseEntry();
		DatabaseEntry foundData = new DatabaseEntry();

		// TODO: this is a very slow but robust way to get the size
		int size = 0;
		while (c.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			size++;
		}
		berkeley.releaseCursor(proxyObject);

		return size;
	}
}
