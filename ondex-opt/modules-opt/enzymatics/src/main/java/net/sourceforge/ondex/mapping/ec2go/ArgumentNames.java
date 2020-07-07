package net.sourceforge.ondex.mapping.ec2go;

import net.sourceforge.ondex.args.FileArgumentDefinition;

/**
 * Defines argument names for the ec2go mapping.
 *
 * @author taubertj
 */
public interface ArgumentNames extends net.sourceforge.ondex.mapping.ArgumentNames {

    public final static String INPUT_FILE_ARG = FileArgumentDefinition.INPUT_FILE;

    public final static String INPUT_FILE_ARG_DESC = "This option gives the path to the EC2GO mapping file.";

}
