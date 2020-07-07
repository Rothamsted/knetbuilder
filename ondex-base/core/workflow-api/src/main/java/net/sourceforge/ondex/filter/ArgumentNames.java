package net.sourceforge.ondex.filter;

/**
 * Contains static String content for common filter parameters.
 * 
 * @author taubertj
 * 
 */
public interface ArgumentNames {

	public static final String CONCEPTCLASS_RESTRICTION_ARG = "ConceptClassRestriction";

	public static final String CONCEPTCLASS_RESTRICTION_ARG_DESC = "A Concept Class Restriction as an ordered pair representing from and to Concepts in an evaluated Relation. (add the reverse compliment if direction is not important)";

	public static final String DATASOURCE_RESTRICTION_ARG = "DataSourceRestriction";

	public static final String DATASOURCE_RESTRICTION_ARG_DESC = "A DataSource Restriction as an ordered pair representing from and to Concepts in an evaluated Relation. (add the reverse compliment if direction is not important)";

}
