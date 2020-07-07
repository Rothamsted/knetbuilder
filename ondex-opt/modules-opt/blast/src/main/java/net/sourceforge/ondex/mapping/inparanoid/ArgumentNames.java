package net.sourceforge.ondex.mapping.inparanoid;

/**
 * ArgumentNames for the inparanoid based mapping.
 * 
 * @author taubertj
 * @version 28.02.2008
 */
public interface ArgumentNames extends net.sourceforge.ondex.mapping.ArgumentNames {

	public final static String PATH_TO_BLAST_ARG = "PathToBlast";

	public final static String PATH_TO_BLAST_ARG_DESC = "Path to BLAST executable.";
	
	public final static String EVALUE_ARG = "Evalue";

	public final static String EVALUE_ARG_DESC = "Evalue cutoff BLAST argument.";

	public final static String SEQUENCE_ATTRIBUTE_ARG = "SeqAttribute";

	public final static String SEQUENCE_ATTRIBUTE_ARG_DESC = "Specifies the Attribute attribute containing the sequence data.";

	public final static String SEQUENCE_TYPE_ARG = "SeqType";

	public final static String SEQUENCE_TYPE_ARG_DESC = "Specifies what sequence type is contained in the Attribute. [NA,AA]";
	
	public final static String CUTOFF_ARG = "cutoff";
	
	public final static String CUTOFF_ARG_DESC = "Bit-score cutoff (default 30).";

	public final static String OVERLAP_ARG = "overlap";
	
	public final static String OVERLAP_ARG_DESC = "Sequence overlap of match length compared to longest sequences (default 0.5).";
	
}