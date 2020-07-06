package net.sourceforge.ondex.mapping.crossspecies;

/**
 * Defines the MetaData used in this Mapping method
 * @author hindlem
 *
 */
public interface MetaData {
	
	static final String ATT_E_VALUE = "BLEV";
	static final String ATT_BITSCORE = "BITSCORE";
	
	static final String EVIDENCE_BLAST = "BLAST";
	
	static final String RT_HAS_SIMILER_SEQUENCE = "h_s_s";

	static final String ATT_NUCL_ACID_SEQ = "NA";
	static final String ATT_AMINO_ACID_SEQ = "AA";
	
	static final String ATT_TAXID = "TAXID";
	
	static final String ATT_COVERAGE = "COVERAGE";
	
	static final String ATT_OVERLAP = "OVERLAP";
	static final String ATT_FRAME = "TRANSLATION_FRAME";
	static final String ATT_QUERY_LENGTH = "QUERY_LENGTH";
	static final String ATT_TARGET_LENGTH = "TARGET_LENGTH";
	
}
