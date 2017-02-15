package net.sourceforge.ondex.core.persistent;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.core.base.AbstractONDEXGraph;
import net.sourceforge.ondex.core.base.ConceptAttribute;
import net.sourceforge.ondex.core.base.RelationAttribute;
import net.sourceforge.ondex.core.persistent.binding.BerkeleyAttributeNameBinding;
import net.sourceforge.ondex.core.persistent.binding.BerkeleyConceptClassBinding;
import net.sourceforge.ondex.core.persistent.binding.BerkeleyDataSourceBinding;
import net.sourceforge.ondex.core.persistent.binding.BerkeleyEvidenceTypeBinding;
import net.sourceforge.ondex.core.persistent.binding.BerkeleyIntegerBinding;
import net.sourceforge.ondex.core.persistent.binding.BerkeleyIntegerNameBinding;
import net.sourceforge.ondex.core.persistent.binding.BerkeleyIntegerNameKeyCreator;
import net.sourceforge.ondex.core.persistent.binding.BerkeleyRelationKeyBinding;
import net.sourceforge.ondex.core.persistent.binding.BerkeleyRelationKeyNameBinding;
import net.sourceforge.ondex.core.persistent.binding.BerkeleyRelationKeyNameKeyCreator;
import net.sourceforge.ondex.core.persistent.binding.BerkeleyRelationTypeBinding;
import net.sourceforge.ondex.core.persistent.binding.BerkeleyStringBinding;
import net.sourceforge.ondex.core.persistent.binding.CursorManager;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.ONDEXListener;
import net.sourceforge.ondex.event.type.DatabaseErrorEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.exception.type.RetrievalException;
import net.sourceforge.ondex.exception.type.StorageException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.je.CheckpointConfig;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Durability;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentMutableConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryCursor;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.Transaction;

/**
 * BerkeleyEnv wraps the Berkeley DB Java Edition and provides methods for
 * inserting and deleting data in the tables.
 * 
 * @author taubertj
 */
public class BerkeleyEnv extends AbstractONDEXPersistent {
	private static final Logger LOG = Logger.getLogger(BerkeleyEnv.class);

	// 128MB default cache
	public static long DEFAULT_CACHSIZE;

	static {
		DEFAULT_CACHSIZE = 134217728;
		if (DEFAULT_CACHSIZE > getRuntimeMaxMemory()) {
			DEFAULT_CACHSIZE = getRuntimeMaxMemory();
		}
	}

	// stored ONDEXGraph
	private BerkeleyONDEXGraph root;

	// Berkeley db environment
	private Environment myDbEnvironment = null;

	// contains primary databases indexed by class
	private Map<Class<?>, Database> databases = new HashMap<Class<?>, Database>();

	// contains first secondary databases indexed by class
	private Map<Class<?>, SecondaryDatabase> secDatabases = new HashMap<Class<?>, SecondaryDatabase>();

	// contains databases with sorted duplicates
	private Map<Class<?>, Database> dupDatabases = new HashMap<Class<?>, Database>();

	// contains entry bindings indexed by class
	private final Map<Class<?>, EntryBinding> bindings = new HashMap<Class<?>, EntryBinding>();

	private final File dir;

	private final CursorManager cursorManager = new CursorManager();

	/**
	 * Constructor which preloads a Berkeley DB in the given directory. Does not
	 * init the ONDEX graph.
	 * 
	 * @param path
	 *            Berkeley database path
	 * @param listener
	 *            ONDEXLIstener
	 */
	private BerkeleyEnv(String path, ONDEXListener listener) {

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				cleanup();
			}
		});

		addONDEXListener(listener);

		dir = new File(path);
		if (!dir.exists())
			fireEventOccurred(new GeneralOutputEvent(
					Config.properties
							.getProperty("persistent.BerkeleyEnv.PreparingDirectory"),
					"[BerkeleyEnv - constructor]"));
		if (dir.mkdirs())
			fireEventOccurred(new GeneralOutputEvent(
					Config.properties.getProperty("persistent.BerkeleyEnv.CreatedDirectory")
							+ path, "[BerkeleyEnv - constructor]"));

		DatabaseErrorEvent error = new DatabaseErrorEvent(
				Config.properties.getProperty("persistent.BerkeleyEnv.PermissionError")
						+ dir.getPath(), "[BerkeleyEnv - constructor]");
		error.setLog4jLevel(Level.FATAL);
		if (!dir.canRead()) {
			fireEventOccurred(error);
			System.exit(1);
		}
		if (!dir.canWrite()) {
			fireEventOccurred(error);
			System.exit(1);
		}

		GeneralOutputEvent so = new GeneralOutputEvent(
				Config.properties.getProperty("persistent.BerkeleyEnv.UsingBerkeley")
						+ path, "[BerkeleyEnv - constructor]");
		so.setLog4jLevel(Level.INFO);
		fireEventOccurred(so);

		// Open the environment. Allow it to be created if it does not already
		// exist.
		try {

			// create new environment
			EnvironmentConfig envConfig = new EnvironmentConfig();
			envConfig.setAllowCreate(true);

			myDbEnvironment = new Environment(new File(path), envConfig);

			// set performance parameter
			EnvironmentMutableConfig envMutableConfig = new EnvironmentMutableConfig();
			envMutableConfig.setDurability(Durability.COMMIT_NO_SYNC);
			envMutableConfig.setCacheSize(DEFAULT_CACHSIZE); // 128MB
			myDbEnvironment.setMutableConfig(envMutableConfig);

			setupDatabases();

			setupDupDatabases();

		} catch (DatabaseException dbe) {
			DatabaseErrorEvent de = new DatabaseErrorEvent(dbe.getMessage(),
					"[BerkeleyEnv - constructor]");
			fireEventOccurred(de);
		}
	}

	/**
	 * Create or loads Berkeley storage for the given filename.
	 * 
	 * @param path
	 *            Berkeley database path
	 * @param name
	 *            name of graph
	 * @param listener
	 *            ONDEXListener
	 */
	public BerkeleyEnv(String path, String name, ONDEXListener listener) {

		this(path, listener);

		// Setup database (de)serialisation bindings for keys
		try {
			setupBindings();
		} catch (DatabaseException dbe) {
			DatabaseErrorEvent de = new DatabaseErrorEvent(dbe.getMessage(),
					"[BerkeleyEnv - constructor]");
			fireEventOccurred(de);
		}

		// init new ondex graph
		root = new BerkeleyONDEXGraph(getSID(), name, this);
		ONDEXEventHandler.getEventHandlerForSID(root.getSID())
				.addONDEXONDEXListener((ONDEXListener) listener);
	}

	/**
	 * Create or loads Berkeley storage for the given filename.
	 * 
	 * @param path
	 *            Berkeley database path
	 * @param name
	 *            name of graph
	 * @param sid
	 *            SID to be used for this graph.
	 * @param listener
	 *            ONDEXListener
	 */
	public BerkeleyEnv(String path, String name, long sid,
			ONDEXListener listener) {

		this(path, listener);

		// Setup database (de)serialisation bindings for keys
		try {
			setupBindings();
		} catch (DatabaseException dbe) {
			DatabaseErrorEvent de = new DatabaseErrorEvent(dbe.getMessage(),
					"[BerkeleyEnv - constructor]");
			fireEventOccurred(de);
		}

		// init new ondex graph
		root = new BerkeleyONDEXGraph(sid, name, this);
		ONDEXEventHandler.getEventHandlerForSID(root.getSID())
				.addONDEXONDEXListener((ONDEXListener) listener);
	}

	public Cursor openCursor(Object peer, Transaction t, Class<?> clazz,
			CursorConfig config) {
		Cursor c = databases.get(clazz).openCursor(t, config);
		cursorManager.registerCursor(peer, c);
		return c;
	}

	public SecondaryCursor openSecondaryCursor(Object peer, Transaction t,
			Class<?> clazz, CursorConfig config) {
		SecondaryCursor c = secDatabases.get(clazz).openCursor(t, config);
		cursorManager.registerCursor(peer, c);
		return c;
	}

	public Cursor openDupCursor(Object peer, Transaction t, Class<?> clazz,
			CursorConfig config) {
		Cursor c = dupDatabases.get(clazz).openCursor(t, config);
		cursorManager.registerCursor(peer, c);
		return c;
	}

	@Override
	public void cleanup() {
		if (root != null) {
			fireEventOccurred(new GeneralOutputEvent(
					"close and cleanup berkley database", ""));
			storeGraphPermissions();
			try {
				cursorManager.close();

				// close all secondary databases
				for (SecondaryDatabase secDB : secDatabases.values()) {
					try {
						secDB.sync();
					} finally {
						secDB.close();
					}
				}
				secDatabases.clear();
				secDatabases = null;

				// close all primary databases
				for (Database db : databases.values()) {
					try {
						db.sync();
					} finally {
						db.close();
					}
				}
				databases.clear();
				databases = null;

				// close all dup databases
				for (Database db : dupDatabases.values()) {
					try {
						db.sync();
					} finally {
						db.close();
					}
				}
				dupDatabases.clear();
				dupDatabases = null;

				if (myDbEnvironment != null) {
					try {
						myDbEnvironment.sync();
						// Clean the log before closing
						myDbEnvironment.getConfig().setConfigParam(
								EnvironmentConfig.ENV_RUN_CLEANER, "false");
						// taken from
						// http://www.oracle.com/technology/documentation/berkeley-db/je/java/com/sleepycat/je/Environment.html#cleanLog()
						boolean anyCleaned = false;
						while (myDbEnvironment.cleanLog() > 0) {
							anyCleaned = true;
						}
						if (anyCleaned) {
							CheckpointConfig force = new CheckpointConfig();
							force.setForce(true);
							myDbEnvironment.checkpoint(force);
						}
					} finally {
						myDbEnvironment.close();
					}
					myDbEnvironment = null;
				}

				bindings.clear();
				root = null;

			} catch (DatabaseException dbe) {
				DatabaseErrorEvent de = new DatabaseErrorEvent(
						dbe.getMessage(), "[BerkeleyEnv - cleanup]");
				fireEventOccurred(de);
			}
		} else {
			fireEventOccurred(new GeneralOutputEvent(
					"Berkley database is already cleaned up: cleanup call ignored",
					""));
		}
	}

	@Override
	public void commit() {
		try {

			// sync all secondary databases
			for (SecondaryDatabase d : secDatabases.values()) {
				d.sync();
			}

			// sync all primary databases
			for (Database d : databases.values()) {
				d.sync();
			}

			// sync all dup databases
			for (Database d : dupDatabases.values()) {
				d.sync();
			}

			if (myDbEnvironment != null) {
				myDbEnvironment.sync();
				myDbEnvironment.compress();
				myDbEnvironment.evictMemory();
			}
		} catch (DatabaseException dbe) {
			DatabaseErrorEvent de = new DatabaseErrorEvent(dbe.getMessage(),
					"[BerkeleyEnv - commit]");
			fireEventOccurred(de);
		}
	}

	/**
	 * Returns a Object of given class derived from DatabaseEntry.
	 * 
	 * @param theData
	 *            DatabaseEntry
	 * @param c
	 *            Class
	 * @return Object
	 */
	@SuppressWarnings("unchecked")
	public <E> E convert(DatabaseEntry theData, Class<E> c) {
		return (E) bindings.get(c).entryToObject(theData);
	}

	/**
	 * Returns a DatabaseEntry representation for a given Object.
	 * 
	 * @param object
	 *            Object
	 * @return DatabaseEntry
	 */
	@SuppressWarnings("unchecked")
	public DatabaseEntry convert(Object object) {
		DatabaseEntry theData = new DatabaseEntry();
		bindings.get(object.getClass()).objectToEntry(object, theData);
		return theData;
	}

	/**
	 * Deletes a value from a primary database for a given key and class.
	 * 
	 * @param c
	 *            Class
	 * @param key
	 *            Object
	 * @return byte[]
	 */
	public boolean deleteFromDatabase(Class<?> c, Object key) {

		byte[] old = getFromDatabase(c, key);

		// get database for class
		Database myDb = databases.get(c);
		if (old != null && myDb != null) {

			try {
				// convert key
				DatabaseEntry theKey = convert(key);

				// perform delete
				if (myDb.delete(null, theKey) == OperationStatus.SUCCESS) {
					return true;
				}
			} catch (DatabaseException dbe) {
				DatabaseErrorEvent de = new DatabaseErrorEvent(
						dbe.getMessage(), "[BerkeleyEnv - deleteFromDatabase]");
				fireEventOccurred(de);
			}
		}

		return false;
	}

	/**
	 * Deletes a value from a duplicated database for a given key, value and
	 * class.
	 * 
	 * @param c
	 *            Class
	 * @param key
	 *            Object
	 * @param value
	 *            byte[]
	 * @return boolean
	 */
	public boolean deleteFromDupDatabase(Class<?> c, Object key, byte[] value) {
		Cursor cursor = null;

		// get database for class
		Database myDb = dupDatabases.get(c);
		if (myDb != null) {

			try {

				DatabaseEntry theKey = convert(key);
				DatabaseEntry theData = new DatabaseEntry(value);

				// Open a cursor using a database handle
				cursor = myDb.openCursor(null, null);

				// Perform the search
				OperationStatus retVal = cursor.getSearchBoth(theKey, theData,
						LockMode.DEFAULT);

				// NOTFOUND is returned if a record cannot be found whose key
				// matches the search key AND whose data begins with the search
				// data.
				if (retVal == OperationStatus.NOTFOUND) {
					return false;
				}
				// Upon completing a search, the key and data DatabaseEntry
				// parameters for getSearchBothRange() are populated with the
				// key/data values of the found record.
				retVal = cursor.delete();

				return retVal == OperationStatus.SUCCESS;
			} catch (DatabaseException dbe) {
				DatabaseErrorEvent de = new DatabaseErrorEvent(
						dbe.getMessage(),
						"[BerkeleyEnv - deleteFromDupDatabase]");
				fireEventOccurred(de);
			} finally {
				if (cursor != null) {
					try {
						cursor.close();
					} catch (DatabaseException dbe) {
						DatabaseErrorEvent de = new DatabaseErrorEvent(
								dbe.getMessage(),
								"[BerkeleyEnv - deleteFromDupDatabase]");
						fireEventOccurred(de);
					}
				}
			}
		}
		return false;
	}

	/**
	 * Returns true if there is a value associated with a key in a database for
	 * a given class.
	 * 
	 * @param c
	 *            Class
	 * @param key
	 *            Object
	 * @return boolean
	 */
	public boolean existsInDatabase(Class<?> c, Object key) {

		// get database for class
		Database myDb = databases.get(c);
		if (myDb != null) {

			try {

				// convert key
				DatabaseEntry theKey = convert(key);

				// contains return data
				DatabaseEntry theData = new DatabaseEntry();

				// perform the get
				if (myDb.get(null, theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
					return true;
				}

			} catch (DatabaseException dbe) {
				DatabaseErrorEvent de = new DatabaseErrorEvent(
						dbe.getMessage(), "[BerkeleyEnv - existsInDatabase]");
				fireEventOccurred(de);
			}
		}

		return false;
	}

	@Override
	public AbstractONDEXGraph getAbstractONDEXGraph() {
		if (root != null) {
			String s = System.getProperty("file.separator");
			String fn = dir.getAbsolutePath() + s + "gp" + root.getSID();
			File gpFile = new File(fn);
			if (gpFile.exists()) {
				try {
					FileInputStream in = new FileInputStream(gpFile);
					byte[] array = root.serialise();
					int read = in.read(array);
					in.close();
					if (read == array.length) {
						((BerkeleyONDEXGraph) root).deserialize(array);
					} else {
						throw new RetrievalException(
								"graph permissions corrupted!");
					}
				} catch (IOException e) {
					throw new RetrievalException(
							"unable to retrieve graph permissions");
				}
			} else {
				try {
					if (!gpFile.createNewFile()) {
						LOG.info("File claimed not to exist, but createNewFile thought that it did: "
								+ gpFile);
					}
					FileOutputStream out = new FileOutputStream(gpFile);
					out.write(root.serialise());
					out.flush();
					out.close();
				} catch (IOException ioe) {
					throw new StorageException(
							"unable to store graph permissions");
				}
			}
			return root;
		} else {
			return null;
		}
	}

	/**
	 * Returns index of databases.
	 * 
	 * @return Map<Class,Database>
	 */
	public Map<Class<?>, Database> getDatabases() {
		return databases;
	}

	/**
	 * Returns index of dup databases.
	 * 
	 * @return Map<Class,Database>
	 */
	public Map<Class<?>, Database> getDupDatabases() {
		return dupDatabases;
	}

	/**
	 * Returns value associated with a key in a database for a given class.
	 * 
	 * @param c
	 *            Class
	 * @param key
	 *            Object
	 * @return byte[]
	 */
	public byte[] getFromDatabase(Class<?> c, Object key) {

		// get database for class
		Database myDb = databases.get(c);
		if (myDb != null) {

			try {

				// convert key
				DatabaseEntry theKey = convert(key);

				// contains return data
				DatabaseEntry theData = new DatabaseEntry();

				// perform the get
				if (myDb.get(null, theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
					return theData.getData();
				}

			} catch (DatabaseException dbe) {
				DatabaseErrorEvent de = new DatabaseErrorEvent(
						dbe.getMessage(), "[BerkeleyEnv - getFromDatabase]");
				fireEventOccurred(de);
			}
		}

		return null;
	}

	/**
	 * Parses the last valid concept ID out of the Berkeley persistent database,
	 * so that new created entities will have the right incremented concept ID.
	 * 
	 * @return int
	 */
	protected int getLastConceptID() {
		return extractLastID(BerkeleyConcept.class,
				"[BerkeleyEnv - getLastConceptID]");
	}

	/**
	 * Parses the last valid relation ID out of the Berkeley persistent
	 * databaseo that new created entities will have the right incremented
	 * relation ID.
	 * 
	 * @return int
	 */
	protected int getLastRelationID() {
		return extractLastID(BerkeleyRelation.class,
				"[BerkeleyEnv - getLastRelationID]");
	}

	private int extractLastID(Class<?> clazz, String topic) {
		Database db = this.getDatabases().get(clazz);
		int last_id = 0;
		// set cursor to read uncommitted data
		CursorConfig config = new CursorConfig();
		config.setReadUncommitted(true);

		Cursor cursor = null;

		if (db != null) {
			try {

				// Open the cursor.
				cursor = db.openCursor(null, config);

				// empty search key
				DatabaseEntry theKey = new DatabaseEntry();

				// contains return data
				DatabaseEntry theData = new DatabaseEntry();

				// Position the cursor
				OperationStatus retVal = cursor.getLast(theKey, theData,
						LockMode.DEFAULT);

				// check for empty queries
				if (retVal == OperationStatus.SUCCESS) {
					ByteArrayInputStream bais = new ByteArrayInputStream(
							theData.getData());

					// object input stream for deserialisation
					DataInputStream dis = new DataInputStream(
							new BufferedInputStream(bais));

					try {
						// get serialised class content
						int length = dis.readInt();
						byte[] parray = new byte[length];
						dis.read(parray);

						// skip sid
						dis.readLong();
						last_id = dis.readInt();

						dis.close();
						bais.close();
					} catch (IOException ioe) {
						DatabaseErrorEvent de = new DatabaseErrorEvent(
								ioe.getMessage(), topic);
						fireEventOccurred(de);
					}
				}

			} catch (DatabaseException dbe) {
				DatabaseErrorEvent de = new DatabaseErrorEvent(
						dbe.getMessage(), topic);
				fireEventOccurred(de);
			} finally {
				if (cursor != null) {
					try {
						cursor.close();
					} catch (DatabaseException dbe) {
						DatabaseErrorEvent de = new DatabaseErrorEvent(
								dbe.getMessage(), topic);
						fireEventOccurred(de);
					}
				}
			}
		}

		return last_id;
	}

	/**
	 * Returns current Database Environment.
	 * 
	 * @return Environment
	 */
	public Environment getMyDbEnvironment() {
		return myDbEnvironment;
	}

	/**
	 * Returns index of secondary databases.
	 * 
	 * @return Map<Class,SecondaryDatabase>
	 */
	public Map<Class<?>, SecondaryDatabase> getSecDatabases() {
		return secDatabases;
	}

	/**
	 * Parses the first valid SID out of the Berkeley persistent database that
	 * new created entities will share have the original SID.
	 * 
	 * @return long
	 */
	private long getSID() {

		long sid = System.nanoTime();

		Cursor cursor = null;

		Database db = this.getDatabases().get(BerkeleyConcept.class);
		if (db != null) {
			try {
				// Open the cursor to read uncommitted data
				CursorConfig config = new CursorConfig();
				config.setReadUncommitted(true);
				cursor = db.openCursor(null, config);

				// empty search key
				DatabaseEntry theKey = new DatabaseEntry();

				// contains return data
				DatabaseEntry theData = new DatabaseEntry();

				// Position the cursor
				OperationStatus retVal = cursor.getNext(theKey, theData,
						LockMode.DEFAULT);

				// check for empty queries
				if (retVal == OperationStatus.SUCCESS) {
					ByteArrayInputStream bais = new ByteArrayInputStream(
							theData.getData());

					// object input stream for deserialisation
					DataInputStream dis = new DataInputStream(
							new BufferedInputStream(bais));

					try {
						// get serialised class content
						int length = dis.readInt();
						byte[] parray = new byte[length];
						dis.read(parray);

						sid = dis.readLong();

						dis.close();
						bais.close();
					} catch (IOException ioe) {
						DatabaseErrorEvent de = new DatabaseErrorEvent(
								ioe.getMessage(), "[BerkeleyEnv - getSID]");
						fireEventOccurred(de);
					}
				}

			} catch (DatabaseException dbe) {
				DatabaseErrorEvent de = new DatabaseErrorEvent(
						dbe.getMessage(), "[BerkeleyEnv - getSID]");
				fireEventOccurred(de);
			} finally {
				if (cursor != null) {
					try {
						cursor.close();
					} catch (DatabaseException dbe) {
						DatabaseErrorEvent de = new DatabaseErrorEvent(
								dbe.getMessage(), "[BerkeleyEnv - getSID]");
						fireEventOccurred(de);
					}
				}
			}
		}

		return sid;
	}

	/**
	 * Inserts a key/data combination into corresponding database.
	 * 
	 * @param c
	 *            Class
	 * @param key
	 *            Object
	 * @param data
	 *            byte[]
	 */
	public void insertIntoDatabase(Class<?> c, Object key, byte[] data) {

		// get database for data
		Database myDb = databases.get(c);
		if (myDb != null) {
			try {
				// convert key and data
				DatabaseEntry theKey = convert(key);
				DatabaseEntry theData = new DatabaseEntry(data);

				// insert into database
				myDb.put(null, theKey, theData);
			} catch (DatabaseException dbe) {
				DatabaseErrorEvent de = new DatabaseErrorEvent(
						dbe.getMessage(), "[BerkeleyEnv - insertIntoDatabase]");
				fireEventOccurred(de);
			}
		}
	}

	/**
	 * Inserts a key/data combination into corresponding database.
	 * 
	 * @param c
	 *            Class
	 * @param key
	 *            Object
	 * @param data
	 *            byte[]
	 */
	public void insertIntoDupDatabase(Class<?> c, Object key, byte[] data) {

		// get database for data
		Database myDb = dupDatabases.get(c);
		if (myDb != null) {
			try {
				// convert key and data
				DatabaseEntry theKey = convert(key);
				DatabaseEntry theData = new DatabaseEntry(data);

				// insert into database
				myDb.put(null, theKey, theData);
			} catch (DatabaseException dbe) {
				DatabaseErrorEvent de = new DatabaseErrorEvent(
						dbe.getMessage(),
						"[BerkeleyEnv - insertIntoDupDatabase]");
				fireEventOccurred(de);
			}
		}
	}

	/**
	 * Sets the runtime cache size of the Berkeley persistent layer as a
	 * percentage of Java Heap.
	 * 
	 * @param percent
	 *            int
	 */
	// fixme: can find no uses of this
	public void setCachePercentage(int percent) {
		try {
			myDbEnvironment.getMutableConfig().setCachePercent(percent);
			myDbEnvironment.evictMemory();
		} catch (IllegalArgumentException iae) {
			DatabaseErrorEvent de = new DatabaseErrorEvent(iae.getMessage(),
					"[BerkeleyEnv - setCache]");
			fireEventOccurred(de);
		} catch (DatabaseException dbe) {
			DatabaseErrorEvent de = new DatabaseErrorEvent(dbe.getMessage(),
					"[BerkeleyEnv - setCache]");
			fireEventOccurred(de);
		}
	}

	/**
	 * Sets the runtime cache size of the Berkeley persistent layer as a
	 * absolute figure.
	 * 
	 * @param size
	 *            long
	 */
	// fixme: can find no uses of this
	public void setCacheSize(long size) {
		try {
			myDbEnvironment.getMutableConfig().setCacheSize(size);
		} catch (IllegalArgumentException iae) {
			DatabaseErrorEvent de = new DatabaseErrorEvent(iae.getMessage(),
					"[BerkeleyEnv - setCacheSize]");
			fireEventOccurred(de);
		} catch (DatabaseException dbe) {
			DatabaseErrorEvent de = new DatabaseErrorEvent(dbe.getMessage(),
					"[BerkeleyEnv - setCacheSize]");
			fireEventOccurred(de);
		}
	}

	/**
	 * Setup data bindings.
	 * 
	 * @throws DatabaseException
	 */
	private void setupBindings() throws DatabaseException {

		// primative bindings
		bindings.put(Integer.class, new BerkeleyIntegerBinding());
		bindings.put(String.class, new BerkeleyStringBinding());
		bindings.put(BerkeleyRelationKeyName.class,
				new BerkeleyRelationKeyNameBinding(this));
		bindings.put(BerkeleyIntegerName.class,
				new BerkeleyIntegerNameBinding());
		bindings.put(BerkeleyRelationKey.class, new BerkeleyRelationKeyBinding(
				this));
		bindings.put(BerkeleyDataSource.class, new BerkeleyDataSourceBinding(
				this));
		bindings.put(BerkeleyConceptClass.class,
				new BerkeleyConceptClassBinding(this));
		bindings.put(BerkeleyAttributeName.class,
				new BerkeleyAttributeNameBinding(this));
		bindings.put(BerkeleyEvidenceType.class,
				new BerkeleyEvidenceTypeBinding(this));
		bindings.put(BerkeleyRelationType.class,
				new BerkeleyRelationTypeBinding(this));
	}

	/**
	 * Creates databases to hold ondex data.
	 * 
	 * @throws DatabaseException
	 */
	private void setupDatabases() throws DatabaseException {

		// primary database config
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setDeferredWrite(true);
		dbConfig.setAllowCreate(true);
		dbConfig.setSortedDuplicates(false);

		// config and keycreator for secondary databases
		SecondaryConfig mySecConfig = new SecondaryConfig();
		mySecConfig.setDeferredWrite(true);
		mySecConfig.setAllowCreate(true);
		mySecConfig.setSortedDuplicates(true);
		mySecConfig.setKeyCreator(new BerkeleyIntegerNameKeyCreator(this));

		SecondaryConfig mySecConfigRel = new SecondaryConfig();
		mySecConfigRel.setDeferredWrite(true);
		mySecConfigRel.setAllowCreate(true);
		mySecConfigRel.setSortedDuplicates(true);
		mySecConfigRel
				.setKeyCreator(new BerkeleyRelationKeyNameKeyCreator(this));

		// create database for concepts
		databases.put(
				BerkeleyConcept.class,
				myDbEnvironment.openDatabase(null,
						BerkeleyConcept.class.getName(), dbConfig));

		// create database for relations
		databases.put(
				BerkeleyRelation.class,
				myDbEnvironment.openDatabase(null,
						BerkeleyRelation.class.getName(), dbConfig));

		// create database for relations with Integer Id
		databases.put(
				Integer.class,
				myDbEnvironment.openDatabase(null, "id"
						+ BerkeleyRelation.class.getName(), dbConfig));

		// create database for attributename
		databases.put(
				AttributeName.class,
				myDbEnvironment.openDatabase(null,
						AttributeName.class.getName(), dbConfig));

		// create database for conceptaccession
		databases.put(
				ConceptAccession.class,
				myDbEnvironment.openDatabase(null,
						ConceptAccession.class.getName(), dbConfig));

		// create secondary database for conceptaccession
		secDatabases.put(
				ConceptAccession.class,
				myDbEnvironment.openSecondaryDatabase(null, "sec"
						+ ConceptAccession.class.getName(),
						databases.get(ConceptAccession.class), mySecConfig));

		// create database for conceptclass
		databases.put(ConceptClass.class, myDbEnvironment.openDatabase(null,
				ConceptClass.class.getName(), dbConfig));

		// create database for conceptgds
		databases.put(
				ConceptAttribute.class,
				myDbEnvironment.openDatabase(null,
						ConceptAttribute.class.getName(), dbConfig));

		// create secondary database for conceptgds
		secDatabases.put(
				ConceptAttribute.class,
				myDbEnvironment.openSecondaryDatabase(null, "sec"
						+ ConceptAttribute.class.getName(),
						databases.get(ConceptAttribute.class), mySecConfig));

		// create database for conceptname
		databases.put(ConceptName.class, myDbEnvironment.openDatabase(null,
				ConceptName.class.getName(), dbConfig));

		// create secondary database for conceptname
		secDatabases.put(
				ConceptName.class,
				myDbEnvironment.openSecondaryDatabase(null, "sec"
						+ ConceptName.class.getName(),
						databases.get(ConceptName.class), mySecConfig));

		// create database for cv
		databases.put(DataSource.class, myDbEnvironment.openDatabase(null,
				DataSource.class.getName(), dbConfig));

		// create database for evidencetype
		databases.put(EvidenceType.class, myDbEnvironment.openDatabase(null,
				EvidenceType.class.getName(), dbConfig));

		// create database for relationgds
		databases.put(
				RelationAttribute.class,
				myDbEnvironment.openDatabase(null,
						RelationAttribute.class.getName(), dbConfig));

		// create secondary database for relationgds
		secDatabases
				.put(RelationAttribute.class, myDbEnvironment
						.openSecondaryDatabase(null, "sec"
								+ RelationAttribute.class.getName(),
								databases.get(RelationAttribute.class),
								mySecConfigRel));

		// create database for relationtype
		databases.put(RelationType.class, myDbEnvironment.openDatabase(null,
				RelationType.class.getName(), dbConfig));

		// create database for unit
		databases.put(Unit.class, myDbEnvironment.openDatabase(null,
				Unit.class.getName(), dbConfig));
	}

	/**
	 * Creates databases containing duplicates.
	 * 
	 * @throws DatabaseException
	 */
	private void setupDupDatabases() throws DatabaseException {

		// Open the database. Create it if it does not already exist.
		Database myDb = null;
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setDeferredWrite(true);
		dbConfig.setAllowCreate(true);
		dbConfig.setSortedDuplicates(true);

		// create database for relationtypeset
		myDb = myDbEnvironment.openDatabase(null,
				"dup" + RelationType.class.getName(), dbConfig);
		dupDatabases.put(RelationType.class, myDb);

		// create database for evidences
		myDb = myDbEnvironment.openDatabase(null,
				"dup" + EvidenceType.class.getName(), dbConfig);
		dupDatabases.put(EvidenceType.class, myDb);

		// create database for tag
		myDb = myDbEnvironment.openDatabase(null,
				"dup" + BerkeleyConcept.class.getName(), dbConfig);
		dupDatabases.put(BerkeleyConcept.class, myDb);

		// create database for cc2concepts, an2concepts, cv2concepts,
		// et2concepts and tag2concepts using Integer ids as values
		myDb = myDbEnvironment.openDatabase(null,
				"dup" + Integer.class.getName(), dbConfig);
		dupDatabases.put(Integer.class, myDb);

		// also for an2relations, rtset2relations, rt2relations,
		// cc2relations, cv2relations, et2relations, concept2relations and
		// tag2relations
		myDb = myDbEnvironment.openDatabase(null, "dup"
				+ BerkeleyIntegerName.class.getName(), dbConfig);
		dupDatabases.put(BerkeleyIntegerName.class, myDb);
	}

	public void storeGraphPermissions() {
		if (root != null) {
			String s = System.getProperty("file.separator");
			String fn = dir.getAbsolutePath() + s + "gp" + root.getSID();
			File gpFile = new File(fn);
			try {
				if (!gpFile.exists()) {
					if (!gpFile.createNewFile()) {
						LOG.info("File claimed not to exist, but createNewFile thought that it did: "
								+ gpFile);
					}
				}
				FileOutputStream out = new FileOutputStream(gpFile);
				out.write(root.serialise());
				out.flush();
				out.close();
			} catch (IOException ioe) {
				throw new StorageException("unable to store graph permissions");
			}
		} else {
			// Graph already cleaned up or not initialized
		}
	}

	/**
	 * Returns Runtime.maxMemory(), accounting for a MacOS bug. May return
	 * Long.MAX_VALUE if there is no inherent limit. Used by unit tests as well
	 * as by this class.
	 * 
	 * @return the availabel memory
	 */
	private static long getRuntimeMaxMemory() {

		/* Runtime.maxMemory is unreliable on MacOS Java 1.4.2. */
		if ("Mac OS X".equals(System.getProperty("os.name"))) {
			String jvmVersion = System.getProperty("java.version");
			if (jvmVersion != null && jvmVersion.startsWith("1.4.2")) {
				return Long.MAX_VALUE; /* Undetermined heap size. */
			}
		}

		return Runtime.getRuntime().maxMemory();
	}

	@SuppressWarnings({ "unchecked" })
	public <E> EntryBinding<E> getBinding(Class<E> clazz) {
		return (EntryBinding<E>) bindings.get(clazz);
	}

	public void releaseCursor(Object peer) {
		cursorManager.releaseCursor(peer);
	}
}
