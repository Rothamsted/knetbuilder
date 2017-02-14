package net.sourceforge.ondex.parser.kegg52.util;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.*;
import net.sourceforge.ondex.event.type.DatabaseErrorEvent;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.tools.DirUtils;
import org.apache.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Temp berkley store for storage of large object arrays in kegg
 *
 * @author hindlem
 */
public class TempBerkleyStore {

    private Environment myDbEnvironment;

    private final Map<Class<?>, Database> databases = new HashMap<Class<?>, Database>();

    private DatabaseConfig dbConfig;

    private ONDEXParser plugin;

    public TempBerkleyStore(String path, ONDEXParser plugin) throws IOException {
        this.plugin = plugin;
        path = path + File.separator + "TmpBerkStore";
        File dir = new File(path);
        dir.deleteOnExit();
        try {
            DirUtils.deleteTree(dir);
            if (!dir.mkdirs()) System.err.println("No permission to write to tmp directory");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!dir.canRead()) throw new IOException("Permissions error! on " + dir.getPath());
        if (!dir.canWrite()) throw new IOException("Permissions error! on " + dir.getPath());

        GeneralOutputEvent so = new GeneralOutputEvent("Using Berkeley for KEGG storte with path " + path, "");
        so.setLog4jLevel(Level.INFO);

        // Open the environment. Allow it to be created if it does not already exist.
        try {

            // create new environment
            EnvironmentConfig envConfig = new EnvironmentConfig();
            envConfig.setAllowCreate(true);

            myDbEnvironment = new Environment(new File(path), envConfig);

            // set performance parameter
            EnvironmentMutableConfig envMutableConfig = new EnvironmentMutableConfig();
            envMutableConfig.setDurability(Durability.COMMIT_NO_SYNC);
            envMutableConfig.setCachePercent(40);
            myDbEnvironment.setMutableConfig(envMutableConfig);

            dbConfig = new DatabaseConfig();
            dbConfig.setDeferredWrite(true);
            dbConfig.setAllowCreate(true);
            dbConfig.setSortedDuplicates(false);

            setupBindings();
        } catch (DatabaseException dbe) {
            DatabaseErrorEvent de = new DatabaseErrorEvent(
                    "[BerkeleyEnv - BerkeleyEnv] " +
                            dbe.getMessage(), "");
            plugin.fireEventOccurred(de);
        }
    }

    public void insertIntoDatabase(Class<?> c, Object key, byte[] data) {

        Database myDb = databases.get(c);
        // get database for data
        if (myDb != null) {
            try {

                // convert key and data
                DatabaseEntry theKey = convert(key);
                DatabaseEntry theData = new DatabaseEntry(data);

                if (theKey == null || theData == null)
                    throw new NullPointerException("coverted Key or DatabaseEntry is null");
                if (theData.getSize() == 1) throw new NullPointerException("data is empty");

                // insert into database
                myDb.put(null, theKey, theData);

            } catch (DatabaseException dbe) {
                DatabaseErrorEvent de = new DatabaseErrorEvent(
                        "[BerkeleyEnv - insertIntoDatabase] " +
                                dbe.getMessage(), "");
                plugin.fireEventOccurred(de);
            }
        }
    }


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
                if (myDb.get(null, theKey, theData, LockMode.DEFAULT) ==
                        OperationStatus.SUCCESS) {
                    return theData.getData();
                }

            } catch (DatabaseException dbe) {
                DatabaseErrorEvent de = new DatabaseErrorEvent(
                        "[BerkeleyEnv - getFromDatabase] " +
                                dbe.getMessage(), "");
                plugin.fireEventOccurred(de);
            }
        }

        return null;
    }

    public byte[] deleteFromDatabase(Class<?> c, Object key) {

        byte[] old = getFromDatabase(c, key);

        // get database for class
        Database myDb = databases.get(c);
        if (old != null && myDb != null) {

            try {
                // convert key
                DatabaseEntry theKey = convert(key);

                // perform delete
                if (myDb.delete(null, theKey) == OperationStatus.SUCCESS)
                    return old;

            } catch (DatabaseException dbe) {
                DatabaseErrorEvent de = new DatabaseErrorEvent(
                        "[BerkeleyEnv - deleteFromDatabase] " +
                                dbe.getMessage(), "");
                plugin.fireEventOccurred(de);
            }
        }

        return null;
    }

    private final Map<Class<?>, EntryBinding> bindings = new HashMap<Class<?>, EntryBinding>();

    public DatabaseEntry convert(Object o) {
        DatabaseEntry theData = new DatabaseEntry();
        bindings.get(o.getClass()).objectToEntry(o, theData);
        return theData;
    }

    public void createNonDuplicateStoreForObject(Class<?> classType, TupleBinding<Object> binding) {
        bindings.put(BerkeleyRelationKeyName.class, binding);
        Database myDb;
        try {
            myDb = myDbEnvironment.openDatabase(null, classType.getName(),
                    dbConfig);
            databases.put(classType, myDb);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    private void setupBindings() throws DatabaseException {

        // Primitive bindings
        EntryBinding<Integer> dataBinding1 = TupleBinding
                .getPrimitiveBinding(Integer.class);
        bindings.put(Integer.class, dataBinding1);

        EntryBinding<String> dataBinding2 = TupleBinding
                .getPrimitiveBinding(String.class);
        bindings.put(String.class, dataBinding2);
    }

    public void commit() {
        try {

            // sync all primary databases
            Iterator<Database> it = databases.values().iterator();
            while (it.hasNext()) {
                it.next().sync();
            }

            if (myDbEnvironment != null) {
                myDbEnvironment.sync();
            }
        } catch (DatabaseException dbe) {
            DatabaseErrorEvent de = new DatabaseErrorEvent(
                    "[BerkeleyEnv - commit] " +
                            dbe.getMessage(), "");
            plugin.fireEventOccurred(de);
        }
    }

    public void cleanup() {

        try {

            // close all primary databases
            Iterator<Database> it = databases.values().iterator();
            Database db = null;
            while (it.hasNext()) {
                db = it.next();
                db.sync();
                db.close();
            }

            if (myDbEnvironment != null) {
                myDbEnvironment.sync();
                myDbEnvironment.cleanLog(); // Clean the log before closing
                myDbEnvironment.close();
            }
        } catch (DatabaseException dbe) {
            DatabaseErrorEvent de = new DatabaseErrorEvent(
                    "[BerkeleyEnv - cleanup] " +
                            dbe.getMessage(), "");
            plugin.fireEventOccurred(de);
        }
    }

    //   private final Set<BerkeleySet> berkeleyIterators =
//	new HashSet<BerkeleySet>();

    public void propagateEventOccurred(EventType et) {
        plugin.fireEventOccurred(et);
    }

    public Map<Class<?>, Database> getDatabases() {
        return databases;
    }

    public void closeDatabase(Class<?> classDBToDelete) {
        Database myDb = databases.get(classDBToDelete);
        databases.remove(classDBToDelete);
        try {
            myDb.close();
        } catch (DatabaseException e) {
            //e.printStackTrace(); ignore if already closed
        }
    }


    /**
     * Implements a tuple of RelationKey and String.
     *
     * @author taubertj
     */
    public class BerkeleyRelationKeyName {

        // key of type BerkeleyRelationKey
        private BerkeleyRelationKey key;

        // string name
        private String name = null;

        /**
         * Constructor fills all variables.
         *
         * @param key  BerkeleyRelationKey
         * @param name String
         */
        public BerkeleyRelationKeyName(BerkeleyRelationKey key, String name) {
            this.key = key;
            if (name != null)
                this.name = name.intern();
        }

        /**
         * Returns key from type BerkeleyRelationKey.
         *
         * @return BerkeleyRelationKey
         */
        public BerkeleyRelationKey getKey() {
            return key;
        }

        /**
         * Returns associated name.
         *
         * @return String
         */
		public String getName() {
			return name;
		}
	}

}
