package net.sourceforge.ondex.test.utils;

import java.io.FileNotFoundException;
import net.sourceforge.ondex.emolecules.graph.Configuration;

/**
 *
 * @author grzebyta
 */
public class DefaultConfiguration {
    public static final String TEST_DATA_FILE = "target/test-classes/version.smi.gz";
    public static final String INDEX_GRAPH_DIR = "target/";
    
    public static Configuration instantiate() throws FileNotFoundException {
        Configuration conf = new Configuration()
                .setIndexDirectoryPath(INDEX_GRAPH_DIR, true)
                .setInputFilePath(TEST_DATA_FILE);
        
        return conf;
    }
}
