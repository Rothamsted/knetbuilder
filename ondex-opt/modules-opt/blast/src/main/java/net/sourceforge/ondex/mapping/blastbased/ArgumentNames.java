package net.sourceforge.ondex.mapping.blastbased;

/**
 * ArgumentNames for the blast based mapping.
 * 
 * @author taubertj
 *
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

	public final static String WITHIN_DATASOURCE_ARG = "WithinSameDataSource";
	
	public final static String WITHIN_DATASOURCE_ARG_DESC = "Allow mapping within the same DataSource.";
	
}