package net.sourceforge.ondex.emolecules.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;

/**
 *
 * @author grzebyta
 */
public class Configuration implements Serializable {

    private static final long serialVersionUID = -1557015549876148582L;
    public static final String INDEX_DIR = "graphDb";
    private File inputFilePath;
    private File indexDirectoryPath;

    public Configuration setInputFilePath(String path) throws FileNotFoundException {
        if (path == null) {
            throw new FileNotFoundException("data file path can't be null");
        }
        
        this.inputFilePath = new File(path);

        if (!inputFilePath.exists()) {
            throw new FileNotFoundException("missing data file at: " + path);
        }

        if (!inputFilePath.isFile()) {
            throw new FileNotFoundException("required file not directory");
        }

        return this;
    }

    /**
     * Set index directory path. Is argument create is true than the directory
     * is created.
     *
     * @param path
     * @param create
     * @return
     * @throws FileNotFoundException
     */
    public Configuration setIndexDirectoryPath(String path, boolean create) throws FileNotFoundException {
        if (path == null) {
            throw new FileNotFoundException("index path can't be null");
        }
        this.indexDirectoryPath = new File(path, INDEX_DIR);

        if (!indexDirectoryPath.exists()) {
            if (create) {
                this.indexDirectoryPath.mkdirs();
            } else {
                throw new FileNotFoundException("missing directory: " + path);
            }
        }

        if (indexDirectoryPath.isFile()) {
            throw new FileNotFoundException("requred path should be a directory");
        }

        return this;
    }

    public File getInputFilePath() {
        return inputFilePath;
    }

    public File getIndexDirectoryPath() {
        return indexDirectoryPath;
    }

    /**
     * Check if all required configuration attributes are valid.
     *
     * @return
     */
    public boolean isValid() {
        return (indexDirectoryPath != null || inputFilePath != null);
    }
}
