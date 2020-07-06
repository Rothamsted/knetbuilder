package net.sourceforge.ondex.parser.fasta.args;

/**
 * @author hindlem
 */
public final class ArgumentNames {

    public final static String FASTA_FILE_TYPE_ARG = "FastaFileType";
    public final static String FASTA_FILE_TYPE_ARG_DESC = "This setting must be used in order to parse the fasta header in a sensible way."
            + "\nIf your file type is not in this list, please try \"simple\" as this parser is the most generic";

    public final static String TAXID_TO_USE_ARG = "TaxId";
    public final static String TAXID_TO_USE_ARG_DESC = "Which taxonomy id should be assigned to the sequences.";

    public final static String CC_OF_SEQ_ARG = "CC";
    public final static String CC_OF_SEQ_ARG_DESC = "The type of the sequences (e.g. target, gene, protein)";

    public final static String CV_OF_SEQ_ARG = "DataSource";
    public final static String CV_OF_SEQ_ARG_DESC = "The source of the sequences (e.g. AFFY, TAIR, dbEST, unknown)";

    public final static String TYPE_OF_SEQ_ARG = "SeqType";
    public final static String TYPE_OF_SEQ_ARG_DESC = "The type (attribute name) of the sequences (e.g. NA, AA)";

    public final static String TO_ACC_ARG = "POS_TO_ACCESSION";
    public final static String TO_ACC_ARG_DESC = "";

    public final static String SEPARATOR_ARG = "Separator";
    public final static String SEPARATOR_ARG_DESC = "RegEx to split header in simple FASTA parser.";

    public final static String ACCESSION_ARG = "AccessionRegEx";
    public final static String ACCESSION_ARG_DESC = "RegEx to be used as an additional accession.";

}
