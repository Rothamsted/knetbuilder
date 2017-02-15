package net.sourceforge.ondex.core.persistent;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.base.ConceptAttribute;
import net.sourceforge.ondex.core.base.RelationAttribute;
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
public class BerkeleySecBrowser<AnyType> extends AbstractSet<AnyType> {

	// Berkeley env for events and dbs
	private final BerkeleyEnv berkeley;

	// global cursor config for this browser
	private CursorConfig cc;

	// current class used
	private final Class<? extends AnyType> clazz;

	// key object
	private final Object key;

	// listener for updates of returned entities
	private UpdateListener listener;

	// unique to this browser used as an id cursor index
	private final Object proxyObject = new Object();

	// current key
	private final DatabaseEntry theKey;

	/**
	 * Constructor for this database browser.
	 * 
	 * @param berkeley
	 *            BerkeleyEnv
	 * @param l
	 *            UpdateListener
	 * @param clazz
	 *            Class<AnyType>
	 * @param key
	 *            Object
	 */
	public BerkeleySecBrowser(BerkeleyEnv berkeley, UpdateListener l,
			Class<? extends AnyType> clazz, Object key) {

		// Berkeley env for event propagation
		this.berkeley = berkeley;

		// persistent update listener
		this.listener = l;

		// class for objects
		this.clazz = clazz;

		// store key object
		this.key = key;

		if (key instanceof BerkeleyIntegerName) {

			BerkeleyIntegerName bin = (BerkeleyIntegerName) key;

			theKey = berkeley.convert(bin.getKey());

		} else if (key instanceof BerkeleyRelationKeyName) {

			BerkeleyRelationKeyName brkn = (BerkeleyRelationKeyName) key;

			theKey = berkeley.convert(brkn.getKey());

		} else {

			theKey = null;

		}

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

		// get name for which we are searching
		String name = null;

		if (o instanceof ConceptName) {
			name = ((ConceptName) o).getName();
		} else if (o instanceof ConceptAccession) {
			ConceptAccession ca = (ConceptAccession) o;
			name = ca.getAccession() + ca.getElementOf().getId();
		} else if (o instanceof Attribute) {
			name = ((Attribute) o).getOfType().getId();
		} else {
			return false;
		}

		// System.out.println("Search for: "+name);

		// get the concept or relation id as pKey
		DatabaseEntry myKey = null;

		if (key instanceof BerkeleyIntegerName) {

			BerkeleyIntegerName bin = (BerkeleyIntegerName) key;

			myKey = berkeley
					.convert(new BerkeleyIntegerName(bin.getKey(), name));

		} else if (key instanceof BerkeleyRelationKeyName) {

			BerkeleyRelationKeyName brkn = (BerkeleyRelationKeyName) key;

			myKey = berkeley.convert(new BerkeleyRelationKeyName(brkn.getKey(),
					name));

		} else {
			return false;
		}

		// get database for class
		Database myDb = berkeley.getDatabases().get(clazz);
		if (myDb != null) {
			try {

				// contains return data
				DatabaseEntry myData = new DatabaseEntry();

				// perform the get
				if (myDb.get(null, myKey, myData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
					name = null;
					myKey = null;
					myData = null;
					return true;
				}

				name = null;
				myKey = null;
				myData = null;
				return false;

			} catch (DatabaseException dbe) {
				DatabaseErrorEvent de = new DatabaseErrorEvent(
						dbe.getMessage(), "[BerkeleySecBrowser - contains]");
				berkeley.fireEventOccurred(de);
			}
		}

		return false;
	}

	@Override
	public Iterator<AnyType> iterator() {
		try {

			return new Iterator<AnyType>() {

				final Cursor cursor = berkeley.openSecondaryCursor(proxyObject,
						null, clazz, cc);

				DatabaseEntry theData = new DatabaseEntry();

				OperationStatus retVal = cursor.getSearchKey(theKey, theData,
						LockMode.DEFAULT);

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
						if (clazz.equals(ConceptAccession.class)) {
							BerkeleyConceptAccession o = BerkeleyConceptAccession
									.deserialise(
											berkeley.getAbstractONDEXGraph(),
											theData.getData());
							o.setUpdateListener(listener);
							val = clazz.cast(o);

						} else if (clazz.equals(ConceptAttribute.class)) {
							BerkeleyConceptAttribute o = BerkeleyConceptAttribute
									.deserialise(
											berkeley.getAbstractONDEXGraph(),
											theData.getData());
							o.setUpdateListener(listener);
							val = clazz.cast(o);

						} else if (clazz.equals(ConceptName.class)) {
							BerkeleyConceptName o = BerkeleyConceptName
									.deserialise(
											berkeley.getAbstractONDEXGraph(),
											theData.getData());
							o.setUpdateListener(listener);
							val = clazz.cast(o);

						} else if (clazz.equals(RelationAttribute.class)) {
							BerkeleyRelationAttribute o = BerkeleyRelationAttribute
									.deserialise(
											berkeley.getAbstractONDEXGraph(),
											theData.getData());
							o.setUpdateListener(listener);
							val = clazz.cast(o);

						}
					} else {
						throw new NoSuchElementException();
					}

					try {
						// This is important!
						if (retVal == OperationStatus.SUCCESS
								&& cursor.count() > 0) {
							retVal = cursor.getNextDup(theKey, theData,
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
								dbe.getMessage(), "[BerkeleySecBrowser - next]");
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
					"[BerkeleySecBrowser - initCursor]");
			berkeley.fireEventOccurred(de);
			throw new Error(dbe);
		}
	}

	@Override
	public int size() {
		Cursor c = berkeley.openSecondaryCursor(proxyObject, null, clazz, cc);
		DatabaseEntry theData = new DatabaseEntry();

		// TODO: this is a very slow but robust way to get the size
		int size = 0;
		OperationStatus retVal = c.getSearchKey(theKey, theData,
				LockMode.DEFAULT);
		if (retVal == OperationStatus.SUCCESS && c.count() > 0) {
			while (retVal == OperationStatus.SUCCESS) {
				retVal = c.getNextDup(theKey, theData, LockMode.DEFAULT);
				size++;
			}
		}
		berkeley.releaseCursor(proxyObject);

		return size;
	}

}
