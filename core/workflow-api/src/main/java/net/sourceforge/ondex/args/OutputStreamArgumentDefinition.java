package net.sourceforge.ondex.args;

import java.io.FileNotFoundException;
import java.io.IOException;
import net.sourceforge.ondex.InvalidPluginArgumentException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;

/**
 * ArgumentDefinition for an output stream.
 * Can convert String into a valid outputStream
 * Can handle files uncompressed as well as Zipped and GZipped.
 *
 * Should only be used for OutputStreams that will NOT be wrapped with a Writer!
 * For Ouputs that handle only text/xml use WriterArgumentDefinition.
 *
 * @author hindlem, Christian Brenninkmeijer
 */
public class OutputStreamArgumentDefinition extends AbstractArgumentDefinition<OutputStream> {

    //Option FileName suffix which suggests the
    private String suffix;

    /**
     * Constructor which fills all internal fields.
     *
     * @param name                     String
     * @param description              String
     * @param suffix                   String (Optional Filename suffix to suggest the format)
     *                                 Example ".oxl","xml","txt","csv".
     *                                 Use bit before ".gz" or ".zip".
     * @param required                 boolean
     * @param canHaveMultipleInstances boolean
     */
    public OutputStreamArgumentDefinition(String name, String description,
                                  String suffix, boolean required,
                                  boolean canHaveMultipleInstances) {
        super(name, description, required, canHaveMultipleInstances);
        this.suffix = suffix;
    }

    /**
     * Constructor which fills most internal fields and sets multiple instances
     * to false.
     *
     * @param name        String
     * @param description String
     * @param suffix                   String (Optional Filename suffix to suggest the format)
     *                                 Example ".oxl","xml","txt","csv".
     *                                 Use bit before ".gz" or ".zip".
     * @param required    boolean
     */
    public OutputStreamArgumentDefinition(String name, String description,
                                  String suffix, boolean required) {
        this(name, description, suffix, required, false);
    }

    /**
     * Returns associated java class.
     *
     * @return Class
     */
    @Override
    public Class<OutputStream> getClassType() {
        return OutputStream.class;
    }

    /**
     * Returns default value.
     *
     * @return null
     */
    public OutputStream getDefaultValue() {
        return null;
    }

    /**
     * Checks for valid argument.
     *
     * @return boolean
     */
    public void isValidArgument(Object obj) throws InvalidPluginArgumentException {
        if (obj instanceof String) {
                if (obj.toString().trim().length() == 0) {
                throw new InvalidPluginArgumentException("An empty argument is invalid for " + this.getName());
            }
            return;
        }
        if (obj instanceof OutputStream){
            return;
        }
        throw new InvalidPluginArgumentException("A " + getName() + " argument is required to be specified as a OutputStream for "
                + this.getName() + " class " + obj.getClass().getName() + " was found ");
    }

    /**
     * Parser argument String into an OutputStream.
     *
     * @param argument The String name of the File to be used as the OutputStream
     *                  Legal values for the String are:
     *                  1) Path and Name of a File Stored on the machine running the code
     *                      a) File that end with ".zip" will be assumed to be in ZIP Format.
     *                      b) File that end with ".gz" will be assumed to be in GZ Format.
     *                      c) All other files are assumed to be in unzipped format.
     * @return          The OutputStream represented by this String
     */
    public static OutputStream StringToOutputStream(String argument) throws InvalidPluginArgumentException{
        if (argument == null){
            throw new InvalidPluginArgumentException("Illegal attempt to convert a null String into an outputStream");
        }
        if (argument.isEmpty()){
            throw new InvalidPluginArgumentException("Illegal attempt to convert an empty String into an outputStream");
        }
        if (argument.toLowerCase().endsWith(".gz")){
            String shorterName = argument.substring(0, argument.length()-3);
            OutputStream outputStream = StringToOutputStream(shorterName);
            try {
                return new GZIPOutputStream(outputStream);
            } catch (IOException ex) {
                throw new InvalidPluginArgumentException("Exception attempting to convert an InputStream from"
                        + shorterName + " to a GZIPOutputStream "+ex);
            }
        }
        if (argument.toLowerCase().endsWith(".zip")){
            String shorterName = argument.substring(0, argument.length()-3);
            OutputStream outputStream = StringToOutputStream(shorterName);
            return new ZipOutputStream(outputStream);
        }
        File file = new File(argument);
        if (file.exists()) {
            if (file.isDirectory()){
                throw new InvalidPluginArgumentException("Illegal attempt to convert a directory " + argument +
                        " to an OutputStream.");
            }
        }
        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException ex) {
                throw new InvalidPluginArgumentException("Exception attempting to open file "
                        + argument + " as an OutputStream."+ex);
        }
    }

    /**
     * Parses argument object from String.
     * Uses the Static method StringToReader and its submethods.
     * @see StringToReader
     *
     * @param argument String
     * @return InputStream
     * @throws InvalidPluginArgumentException
     * @throws IOException
     */
    @Override
    public OutputStream parseString(String argument)
            throws InvalidPluginArgumentException {
        return StringToOutputStream(argument);
    }

}
