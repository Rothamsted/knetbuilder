package net.sourceforge.ondex.core.persistent;

import com.sleepycat.je.*;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.base.AbstractMetaData;
import net.sourceforge.ondex.core.base.AbstractONDEXEntity;
import net.sourceforge.ondex.core.util.UpdateListener;
import net.sourceforge.ondex.event.type.DatabaseErrorEvent;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Berkeley implementation of the Set.
 * 
 * @author taubertj
 * @param <AnyType>
 */
public class BerkeleyDupBrowser<AnyType> extends AbstractSet<AnyType> {

	private final Object proxyObject = new Object(); // unique to this browser
														// used as an id cursor
														// index

	// berkeley env for events and dbs
	private final BerkeleyEnv berkeley;

	// current class used
	private final Class<? extends AnyType> clazz;

	// for contains enquires
	private Cursor containsCursor = null;

	// current search key
	private final DatabaseEntry theKey;

	// contains size of results
	private Integer size = null;

	// SID of contained objects
	private long srcSID = 0L;

	// listener for updates of returned entities
	private final UpdateListener listener;

	/**
	 * Constructor for this duplicated database browser.
	 * 
	 * @param berkeley
	 *            BerkeleyEnv
	 * @param clazz
	 *            Class<AnyType>
	 * @param key
	 *            Object
	 */
	public BerkeleyDupBrowser(BerkeleyEnv berkeley, UpdateListener l,
			Class<? extends AnyType> clazz, Object key) {

		// berkeley env for event propagation
		this.berkeley = berkeley;

		// persistent update listener
		this.listener = l;

		// class for objects
		this.clazz = clazz;

		// the search key
		this.theKey = berkeley.convert(key);
	}

	@Override
	public boolean contains(Object o) {

		if (containsCursor == null) {
			CursorConfig config = new CursorConfig();
			config.setReadUncommitted(true);
			containsCursor = berkeley.openDupCursor(proxyObject, null, clazz,
					config);
			containsCursor.getSearchKey(theKey, new DatabaseEntry(),
					LockMode.DEFAULT);

			if (containsCursor.count() > 0) {
				// #TODO this is dirty hack fix!! create somthing more elegant
				Object firstObject = iterator().next();
				if (firstObject instanceof AbstractONDEXEntity) {
					srcSID = ((AbstractONDEXEntity) firstObject).getSID();
				} else if (firstObject instanceof AbstractMetaData) {
					srcSID = ((AbstractMetaData) firstObject).getSID();
				}
			}
		}

		if (containsCursor.count() == 0) {
			return false;
		}

		// contains return data
		final DatabaseEntry myData;

		if (o instanceof BerkeleyEvidenceType) {
			BerkeleyEvidenceType query = (BerkeleyEvidenceType) o;
			myData = new DatabaseEntry(query.serialise(srcSID));
		} else if (o instanceof BerkeleyRelationType) {
			BerkeleyRelationType query = (BerkeleyRelationType) o;
			myData = new DatabaseEntry(query.serialise(srcSID));
		} else if (o instanceof BerkeleyConcept) {
			BerkeleyConcept query = (BerkeleyConcept) o;
			myData = new DatabaseEntry(query.serialise(srcSID));
		} else if (o instanceof BerkeleyRelation) {
			BerkeleyRelation query = (BerkeleyRelation) o;
			myData = new DatabaseEntry(query.serialise(srcSID));
		} else if (o instanceof Integer) {
			Integer query = (Integer) o;
			myData = berkeley.convert(query);
		} else if (o instanceof BerkeleyIntegerName) {
			BerkeleyIntegerName query = (BerkeleyIntegerName) o;
			myData = berkeley.convert(query);
		} else {
			return false;
		}

		try {
			// Position the cursor
			final OperationStatus myRetVal = containsCursor.getSearchBoth(
					theKey, myData, LockMode.DEFAULT);
			// check for empty queries
			if (myRetVal == OperationStatus.SUCCESS) {
				return true;
			}
			return false;

		} catch (DatabaseException dbe) {
			DatabaseErrorEvent de = new DatabaseErrorEvent(dbe.getMessage(),
					"[BerkeleyDupBrowser - contains]");
			berkeley.fireEventOccurred(de);
			return false;
		}
	}

	@Override
	public Iterator<AnyType> iterator() {
		try {

			// set cursor to read uncommitted data
			final CursorConfig config = new CursorConfig();
			config.setReadUncommitted(true);

			return new Iterator<AnyType>() {

				// current database cursor
				final private Cursor cursor;

				// last return value
				private OperationStatus retVal;

				// current data entry
				private final DatabaseEntry theData;

				// already finished
				private boolean finished;

				{
					cursor = berkeley.openDupCursor(proxyObject, null, clazz,
							config);
					theData = new DatabaseEntry();
					retVal = cursor.getSearchKey(theKey, theData,
							LockMode.DEFAULT);
					finished = retVal != OperationStatus.SUCCESS;
				}

				@Override
				public boolean hasNext() {
					return !finished;
				}

				@Override
				public AnyType next() {
					AnyType val = null;

					if (!finished) {
						if (clazz.equals(RelationType.class)) {
							BerkeleyRelationType o = BerkeleyRelationType
									.deserialise(
											berkeley.getAbstractONDEXGraph(),
											theData.getData());
							o.setUpdateListener(listener);
							val = clazz.cast(o);
						} else if (clazz.equals(EvidenceType.class)) {
							BerkeleyEvidenceType o = Persistence.deserialise(
									BerkeleyEvidenceType.FACTORY,
									theData.getData());
							o.setUpdateListener(listener);
							val = clazz.cast(o);
						} else if (clazz.equals(BerkeleyConcept.class)) {
							BerkeleyConcept o = BerkeleyConcept.deserialise(
									berkeley, theData.getData());
							o.setUpdateListener(listener);
							val = clazz.cast(o);
						} else if (clazz.equals(BerkeleyRelation.class)) {
							BerkeleyRelation o = BerkeleyRelation.deserialise(
									berkeley, theData.getData());
							o.setUpdateListener(listener);
							val = clazz.cast(o);
						} else if (clazz.equals(Integer.class)) {
							Integer o = (Integer) berkeley.convert(theData,
									Integer.class);
							val = clazz.cast(o);
						} else if (clazz.equals(BerkeleyIntegerName.class)) {
							BerkeleyIntegerName o = (BerkeleyIntegerName) berkeley
									.convert(theData, BerkeleyIntegerName.class);
							val = clazz.cast(o);
						}
					} else {
						throw new NoSuchElementException();
					}

					try {
						// Count the number of duplicates. If the count is
						// greater than 1,
						// process the duplicates. This is important!
						if (retVal == OperationStatus.SUCCESS
								&& cursor.count() > 1) {
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
								dbe.getMessage(), "[BerkeleyDupBrowser - next]");
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
					"[BerkeleyDupBrowser - initCursor]");
			berkeley.fireEventOccurred(de);
			throw new Error(dbe);
		}
	}

	@Override
	public int size() {
		if (size == null) {
			CursorConfig cc = new CursorConfig();
			cc.setReadUncommitted(true);
			Cursor c = berkeley.openDupCursor(proxyObject, null, clazz, cc);
			if (c.getSearchKey(theKey, new DatabaseEntry(), LockMode.DEFAULT) != OperationStatus.SUCCESS) {
				size = 0;
			} else {
				size = c.count();
			}
			berkeley.releaseCursor(c);
		}
		return size;
	}

}
