package net.sourceforge.ondex.parser.kegg52.util;

import java.io.File;
import java.io.IOException;
import java.lang.ref.Cleaner;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.tools.DirUtils;

public class BerkleyLocalEnvironment implements AutoCloseable {

    private Environment envmnt;

    private EntityStore store;

    private File envHome;

    // 128MB default cache
    public static final int DEFAULT_CACHSIZE = 134217728;

    {
    	// This is how to implement finalisation in J9+
    	Cleaner.create ().register ( this, this::close );
    }
    
    
    public BerkleyLocalEnvironment(ONDEXGraph og) {
        String ondexDir = System.getProperties().getProperty("ondex.dir");

        envHome = new File(ondexDir + File.separator + "dbs" + File.separator
                + "KEGG_TEMP_STORE_" + og.getName()
                + System.currentTimeMillis());

        try {
            DirUtils.deleteTree(envHome);
            if (!envHome.mkdirs()) {
                System.err.println("No permission to write to directory "
                        + envHome.getAbsolutePath());
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        envConfig.setTransactional(false);
        envConfig.setCacheSize(DEFAULT_CACHSIZE);

        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(true);
        storeConfig.setTransactional(false);
        storeConfig.setDeferredWrite(true);

        try {
            envmnt = new Environment(envHome, envConfig);
            store = new EntityStore(envmnt, "keggStore", storeConfig);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    public Environment getEnv() {
        return envmnt;
    }

    public EntityStore getStore() {
        return store;
    }

    @Override
    public void close () {
        try {
            store.close();
            envmnt.close();
            try {
                DirUtils.deleteTree(envHome);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

}
