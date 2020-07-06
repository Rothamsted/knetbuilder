package net.sourceforge.ondex.mapping.external2go;

import net.sourceforge.ondex.args.FileArgumentDefinition;

/**
 * Defines argument names for the external2go mapping.
 *
 * @author peschr
 */
public interface ArgumentNames extends net.sourceforge.ondex.mapping.ArgumentNames {

    public final static String INPUT_FILE_ARG = FileArgumentDefinition.INPUT_FILE;
    public final static String INPUT_FILE_ARG_DESC = "This option gives the path to the *2go mapping file.";

    public final static String FROM_CONCEPT_CLASS_ARG = "fromConceptClass";
    public final static String FROM_CONCEPT_CLASS_ARG_DESC = "This option defines the from ConceptClass type eg. Pfam or EC or IPRO";

    public final static String FROM_CV_ARG = "fromCV";
    public final static String FROM_CV_ARG_DESC = "This option defines the from DataSource type of the external accession identifier";

}
