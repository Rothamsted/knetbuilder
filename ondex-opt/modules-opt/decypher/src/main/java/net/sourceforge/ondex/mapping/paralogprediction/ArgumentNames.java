package net.sourceforge.ondex.mapping.paralogprediction;

/**
 * The argument names that can be passed to this Mapping method
 * @author hindlem
 *
 */
public interface ArgumentNames {
	
	final static String E_VALUE_ARG = "EValue";
	final static String E_VALUE_DESC = "Cutoff for the BLAST E-Value";
	
	final static String BITSCORE_ARG = "ScoreCutoff";
	final static String BITSCORE_DESC = "Score above which to register similar sequences";
	
	final static String SEQ_TYPE_ARG = "SequenceType";
	final static String SEQ_TYPE_ARG_DESC = "The Sequence Type to conduct paralog search on";
	
	final static String PROGRAM_DIR_ARG = "ProgramDir";
	final static String PROGRAM_DIR_DESC = "The directory where the sequence alignment program is located";
		
	final static String OVERLAP_ARG = "Overlap";
	final static String OVERLAP_DESC = "The minimum overlap to tolerate: as a percentage";
	
	final static String CUTOFF_ARG = "Cutoff";
	final static String CUTOFF_DESC = "The minimum length of sequence to allow as a valid alignment: the number of bases";
	
	final static String TAX_ID_ARG = "TaxId";
	final static String TAX_ID_ARG_DESC = "TaxIDs to include in the paralog prediction";
	
	final static String SPECIES_SEQUENCE_SIZE_ARG = "SeqDBSize";
	final static String SPECIES_SEQUENCE_SIZE_ARG_DESC = "The number of sequences requires in a species to qualify for paralog prediction";
	
	
}
