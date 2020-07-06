package net.sourceforge.ondex.mapping;

/**
 * Contains static String content for common mapping parameters.
 * 
 * @author taubertj
 * 
 */
public interface ArgumentNames {

	public static final String EQUIVALENT_CC_ARG = "EquivalentConceptClass";
	public static final String EQUIVALENT_CC_ARG_DESC = "This option should contain a pair of ConceptClasses seperated by a comma (,); for example \"Thing,EC\"\n"
			+ "The usage of this setting is to allow the mapping method to cross the ConceptClass boundary in some special cases.\n"
			+ "And thus be able to map for example similar GO and EC concepts to each other.";

	public static final String ATTRIBUTE_EQUALS_ARG = "AttributeRestriction";
	public static final String ATTRIBUTE_EQUALS_ARG_DESC = "This will limit the mapping method to only map concepts when the Attribute Value with the attribute name\n"
			+ "specified by this parameter is the same.";

	public static final String CONCEPTCLASS_RESTRICTION_ARG = "ConceptClassRestriction";
	public static final String CONCEPTCLASS_RESTRICTION_ARG_DESC = "A ConceptClass Restriction as an concept class that is used as the seed in the mapping";

	public static final String DATASOURCE_RESTRICTION_ARG = "DataSourceRestriction";
	public static final String DATASOURCE_RESTRICTION_ARG_DESC = "A DataSource Restriction as an ordered pair representing from and to Concepts in an evaluated Relation. (add the reverse compliment if direction is not important)";

	public final static String WITHIN_DATASOURCE_ARG = "WithinDataSourceMapping";
	public final static String WITHIN_DATASOURCE_ARG_DESC = "Map also within each DataSource.";

}