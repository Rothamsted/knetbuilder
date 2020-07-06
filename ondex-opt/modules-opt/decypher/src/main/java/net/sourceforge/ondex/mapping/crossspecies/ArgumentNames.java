package net.sourceforge.ondex.mapping.crossspecies;

/**
 * @author hindlem
 */
public interface ArgumentNames {

    final static String TARGET_CC_ARG = "TargetConceptClass";
    final static String TARGET_CC_DESC = "Filter by a Target Concept Class [default is all]";

    final static String QUERY_CC_ARG = "QueryConceptClass";
    final static String QUERY_CC_DESC = "Filter by a target Query Concept Class [default is all]";

    final static String ALIGNMENT_THRESHOLD_ON_ENTRY_POINT_ARG = "AlignmentThreshold";
    final static String ALIGNMENT_THRESHOLD_ON_ENTRY_POINT_DESC = "Indicates the number (The integer paramiter) of alignments that should be taken per entrypoint, based on the top ranking coverage/alignment length of the longer/shorter(default) sequence in the match";

    final static String ENTRY_POINT_IS_TAXID_ARG = "EntryPointIsTAXID";
    final static String ENTRY_POINT_IS_TAXID_DESC = "Indicates that species should be considered a unique entry point for blasting";

    final static String ENTRY_POINT_IS_CV_ARG = "EntryPointIsCV";
    final static String ENTRY_POINT_IS_CV_DESC = "Indicates that DataSource (database) should be considered a unique entry point for blasting, this turns this method into a cross datbase mapping method";

    final static String E_VALUE_ARG = "EValue";
    final static String E_VALUE_DESC = "Cutoff for the BLAST E-Value";

    final static String TARGET_SEQ_TYPE_ARG = "TargetSequenceType";
    final static String QUERY_SEQ_TYPE_ARG = "QuerySequenceType";

    final static String PROGRAM_DIR_ARG = "ProgramDir";
    final static String PROGRAM_DIR_DESC = "The directory where the sequence alignment program is located";

    final static String OVERLAP_ARG = "Overlap";
    final static String OVERLAP_DESC = "The minimum base pair alignment overlap to tolerate";

    final static String BITSCORE_ARG = "Bitscore";
    final static String BITSCORE_DESC = "The minimum bitscore to tolerate";

    final static String ALINGMENTS_PER_QUERY_ARG = "AlignmentsPerQuery";
    final static String ALINGMENTS_PER_QUERY_DESC = "Maximum alignments decypher will return per query";

    final static String ALIGNMENTS_CUTOFF_ARG = "AlignmentsCutoff";
    final static String ALIGNMENTS_CUTOFF_DESC = "The minimum length of sequence to allow as a valid alignment";

    final static String QUERY_TAXID_ARG = "QueryTAXID";
    final static String QUERY_TAXID_ARG_DESC = "The TAXID to do cross species mapping from";

    final static String ALIGNMENT_TYPE_ARG = "ComparisonType";
    final static String ALIGNMENT_TYPE_DESC = "The comparison type that defines the better alignment: \"bitscore\", \"coverage_target\" (fraction of target sequnce aligned), \"coverage_query\" (fraction of query sequnce aligned), \"coverage_shortest\" (fraction of shortest sequnce aligned), \"coverage_longest\" (fraction of longest sequnce aligned), \"overlap\" (number of matching pairs), and \"evalue\"";

    final static String SPECIES_SEQUENCE_SIZE_ARG = "SeqDBSize";
    final static String SPECIES_SEQUENCE_SIZE_ARG_DESC = "The number of sequences requires in a species to qualify for blasting against (Useful if PerSpeciesBlast is off)";

    final static String PER_SPECIES_BLAST_ARG = "PerSpeciesBlast";
    final static String PER_SPECIES_BLAST_ARG_DESC = "Set to false (default is true) to create for each all qualifying species in the graph only blast DB along with an one decypher blast job. (beware e-values may be higher given the increase in abundence of sequence hits for a given gene family)" +
            " This is very slow, however it will reduce the DB size for each blast job and improve the E-value of the hits";

}
