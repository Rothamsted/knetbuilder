package net.sourceforge.ondex.mapping.accessionbased;

/**
 * ArgumentNames for the accession based mapping.
 * 
 * @author taubertj
 * 
 */
public interface ArgumentNames extends
		net.sourceforge.ondex.mapping.ArgumentNames {

	public final static String EQUIVALENT_CV_ARG = "EquivalentAccessionType";
	public final static String EQUIVALENT_CV_ARG_DESC = "This option should contain a pair of DataSources seperated by a komma (,); for example \"TIGR,TAIR\"\n"
			+ "The usage of this setting is to explicitly tell that two accessions with different DataSources are infact containing the same information.";

	public final static String IGNORE_AMBIGUOUS_ARG = "UseAmbiguousAccessions";
	public final static String IGNORE_AMBIGUOUS_ARG_DESC = "When true it allows ambiguous concept accessions to be mapped (use with care!).";

	public final static String RELATION_TYPE_ARG = "RelationType";
	public final static String RELATION_TYPE_ARG_DESC = "The relation type to create between matching concepts: default relation type is the equals relation type.";

}