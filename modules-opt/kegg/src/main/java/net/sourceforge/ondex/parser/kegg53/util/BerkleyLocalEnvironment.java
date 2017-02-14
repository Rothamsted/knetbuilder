package net.sourceforge.ondex.parser.kegg53.util;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.tools.DirUtils;

import java.io.File;
import java.io.IOException;

public class BerkleyLocalEnvironment {

    private Environment envmnt;

    private EntityStore store;

    private File envHome;

    // 128MB default cache
    public static final int DEFAULT_CACHSIZE = 134217728;


    public BerkleyLocalEnvironment(ONDEXGraph og) {

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                store.close();
                envmnt.close();
                try {
                    DirUtils.deleteTree(envHome);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

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

}
