
package net.sourceforge.ondex.args;

/**
 * Wrapper around the BooleanArguementDefintion for arguements which tell Plugin to compress the results.
 *
 * This wrapper helps the automatic webservice plugin creator, indentify these arugements.
 * When the user choose to compress the results these will be returned as a URL to the file rather than String.
 * This wrapper will help the WS offer within plugin compression to the user.
 * 
 * @author christian
 */
public class CompressResultsArguementDefinition extends BooleanArgumentDefinition{

    /**
     * Constructor which fills most internal fields and sets multiple instances
     * to false.
     *
     * @param name         String
     * @param description  String
     * @param required     boolean
     * @param defaultValue boolean
     */
    public  CompressResultsArguementDefinition(String name, String description,
                                     boolean required, boolean defaultValue) {
        super(name, description, required, defaultValue);
    }

}
