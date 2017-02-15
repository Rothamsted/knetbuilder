package net.sourceforge.ondex.args;

import net.sourceforge.ondex.InvalidPluginArgumentException;

import java.io.File;

/**
 * ArgumentDefinition for a file/directory name.
 *
 * @author hindlem
 */
public class FileArgumentDefinition extends AbstractArgumentDefinition<String> {

    public static String INPUT_FILE = "InputFile";
    public static String INPUT_FILE_DESC = "Absolute path to input file";

    public static String INPUT_DIR = "InputDir";
    public static String INPUT_DIR_DESC = "Absolute path to input directory";

    public static String EXPORT_FILE = "ExportFile";
    public static String EXPORT_FILE_DESC = "Absolute path to export file";

    public static String EXPORT_DIR = "ExportDir";
    public static String EXPORT_DIR_DESC = "Absolute path to export directory";

    // file is directory
    private boolean isDirectory;

    // file exists
    private boolean preExisting;

    /**
     * Constructor which fills all internal fields.
     *
     * @param name                     String
     * @param description              String
     * @param required                 boolean
     * @param preExisting              should this file/dir exist
     * @param isDirectory              is a directory expected/else we assume a file is expected
     * @param canHaveMultipleInstances boolean
     */
    public FileArgumentDefinition(String name, String description,
                                  boolean required, boolean preExisting, boolean isDirectory,
                                  boolean canHaveMultipleInstances) {
        super(name, description, required, canHaveMultipleInstances);
        this.isDirectory = isDirectory;
        this.preExisting = preExisting;
    }

    /**
     * Constructor which fills most internal fields and sets multiple instances
     * to false.
     *
     * @param name        String
     * @param description String
     * @param required    boolean
     * @param preExisting should this file/dir exist
     * @param isDirectory is a directory expected/else we assume a file is expected
     */
    public FileArgumentDefinition(String name, String description,
                                  boolean required, boolean preExisting, boolean isDirectory) {
        super(name, description, required, false);
        this.isDirectory = isDirectory;
        this.preExisting = preExisting;
    }

    /**
     * Returns associated java class.
     *
     * @return Class
     */
    @Override
    public Class<String> getClassType() {
        return String.class;
    }

    /**
     * Returns default value.
     *
     * @return null
     */
    public String getDefaultValue() {
        return null;
    }

    /**
     * Checks for valid argument.
     *
     * @return boolean
     */
    public void isValidArgument(Object obj) throws InvalidPluginArgumentException {
        if (obj instanceof String) {
            File file = new File((String) obj);
            if (obj.toString().trim().length() == 0) {
                throw new InvalidPluginArgumentException("An empty argument is invalid for " + this.getName());
            }

            if (preExisting && !file.exists()) {
                throw new InvalidPluginArgumentException("The file " + file + " does not exist and is required to do so for " + this.getName() + ". Absolute path: " + file.getAbsolutePath() + " ");
            }
            if (isDirectory && !file.isDirectory()) {
                throw new InvalidPluginArgumentException("The dir " + file + " is not a valid dir for " + this.getName() + ". Absolute path: " + file.getAbsolutePath() + " ");
            }
            return;
        }
        throw new InvalidPluginArgumentException("A " + getName() + " argument is required to be specified as a String for " + this.getName() + " class " + obj.getClass().getName() + " was found ");
    }

    /**
     * Returns whether or not it is a directory.
     *
     * @return boolean
     */
    public boolean isDirectory() {
        return isDirectory;
    }

    /**
     * Sets whether or not it is a directory.
     *
     * @param isDirectory boolean
     */
    public void setDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }

    /**
     * Returns whether or not it is preexisting.
     *
     * @return boolean
     */
    public boolean isPreExisting() {
        return preExisting;
    }

    /**
     * Sets whether or not it is preexisting.
     *
     * @param preExisting boolean
     */
    public void setPreExisting(boolean preExisting) {
        this.preExisting = preExisting;
    }

    /**
     * Parses argument object from String.
     *
     * @param argument String
     * @return Boolean
     */
    public String parseString(String argument) {
        return argument;
    }

}
