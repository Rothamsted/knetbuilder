package net.sourceforge.ondex.parser.kegg52.args;

public interface ArgumentNames {

    public static String CLEANUP_ARG = "OnlyReferenced";
    public static String CLEANUP_ARG_DESC = "Import only pathway entries which are referenced in relation or reaction section.";

    public static String SPECIES_ARG = "Species";
    public static String SPECIES_ARG_DESC = "KEGG species code";

    public static String IMPORTSEQS_ARG = "ParseSequences";
    public static String IMPORTSEQS_ARG_DESC = "This options specifies wether or not for every enzyme/protein/gene the sequence will be parsed and added as a Attribute value.\nPlease note that parsing all the sequences can take up a considerable ammount of time.";

    public static String PATHWAY_FILLERS_ARG = "ImportOrthologFillers";
    public static String PATHWAY_FILLERS_ARG_DESC = "Import Ortholog Pathway Fillers";
}
