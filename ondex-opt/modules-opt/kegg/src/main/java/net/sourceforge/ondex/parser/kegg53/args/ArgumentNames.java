package net.sourceforge.ondex.parser.kegg53.args;

public interface ArgumentNames {

    public static String CLEANUP_ARG = "OnlyReferenced";
    public static String CLEANUP_ARG_DESC = "Import only pathway entries which are referenced in relation or reaction section.";

    public static String SPECIES_ARG = "Species";
    public static String SPECIES_ARG_DESC = "KEGG species code or NCBI taxid; specifies species to parse pathways for";

    public static String IMPORT_SEQS_4_SPECIES_ARG = "ParseAllSequencesForSpecies";
    public static String IMPORT_SEQS_4_SPECIES_ARG_DESC = "Specifies to parse all genes for a species regardless of membership of pathway";

    public static String SPECIES_OTHO_ARG = "SpeciesOrthologs";
    public static String SPECIES_OTHO_ARG_DESC = "Parse othologs to specified species";


}
