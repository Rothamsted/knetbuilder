package net.sourceforge.ondex.mapping.sequence2pfam;

/**
 * Arguments for the sequence2pfam mapping
 * 
 * @author peschr
 * 
 */
public interface ArgumentNames {
	public final static String PROGRAM_DIR_ARG = "ProgramDir";
	public final static String PROGRAM_DIR_ARG_DESC = "Has to be set in order to execute blast/hmmer e.g. /home/apps/hmmer3/binaries";

	public final static String PFAM_PATH_ARG = "PfamPath";
	public final static String PFAM_PATH_ARG_DESC = "Location of the Pfam database. If using Hmmer this needs to point to a .hmm file e.g. Pfam-A.hmm. You need to do first a: hmmpress Pfam-A.hmm";

	public final static String TMP_DIR_ARG = "TmpDir";
	public final static String TMP_DIR_ARG_DESC = "Temporary dir";

	public final static String EVALUE_ARG = "Evalue";
	public final static String EVALUE_ARG_DESC = "Evalue cutoff argument.";

	public final static String BIT_SCORE_ARG = "BitScore";
	public final static String BIT_SCORE_ARG_DESC = "bitscore cutoff argument. (NB will only work with decypher)";

	public final static String METHOD_ARG = "Method";
	public final static String METHOD_ARG_DESC = "Blast, Hmmer or Decypher. BLAST by default";

	public final static String CONCEPT_CLASS_ARG = "ConceptClass";
	public final static String CONCEPT_CLASS_ARG_DESC = "The ConceptClass to align to pfam domains";
	
	public final static String ATTRIBUTE_ARG = "AttributeName";
	public final static String ATTRIBUTE_ARG_DESC = "The AttributeName containing sequences to align to pfam domains";
	
	public final static String IGNORE_PFAM_ANNOTATION_ARG = "IgnorePfamAccessions";
	public final static String IGNORE_PFAM_ANNOTATION_ARG_DESC = "If true, the mapping method tries to identify the protein family even if there are already protein family annotations added to the protein";

	public static final String HMM_THRESHOLDS_ARG = "HMMThresholds";
	public static final String HMM_THRESHOLDS_DESC = "The HMM THRESHOLDS (can be null) specifies the use of one or more of the threshold values specified in a hidden Markov model as a minimum criteria that search results must meet to be presented in the output file. Pfam and other curated databases of hidden Markov models may contain within them annotation lines specifying per-model threshold values for scoring alignments that use the model. i.e. GA, NC or TC";

}
