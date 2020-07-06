package net.sourceforge.ondex.transformer.relationcollapser;

/**
 * PluginArgument names for this transformer
 * 
 * @author hindlem
 */
public interface ArgumentNames {

	public static final String RELATION_TYPE_ARG = "RelationType";
	public static final String RELATION_TYPE_ARG_DESC = "The relation type to collapse (!SPECIFING MORE THAN ONE RT IS NOT EQUIVILENT TO MULTIPLE COLLAPSES!! be careful specifying more than one relation type; concepts will be collapsed across RELATION TYPES and concept based restrictions will apply equaly to all)";

	public static final String CONCEPTCLASS_RESTRICTION_ARG = "ConceptClassRestriction";
	public static final String CONCEPTCLASS_RESTRICTION_ARG_DESC = "A Concept Class Restriction as an ordered pair representing from and to Concepts in an evaluated Relation (reverse compliment is included by default)";

	public static final String DATASOURCE_RESTRICTION_ARG = "DataSourceRestriction";
	public static final String DATASOURCE_RESTRICTION_ARG_DESC = "A DataSource Restriction as an ordered pair representing from and to Concepts in an evaluated Relation (reverse compliment is included by default)";

	public static final String CLONE_ATTRIBUTES_ARG = "CloneAttributes";
	public static final String CLONE_ATTRIBUTES_ARG_DESC = "Add inherited Attribute properties to the new collapsed concepts (set false to speed up significantly)";

	public static final String COPY_TAG_REFERENCES_ARG = "CopyTagReferences";
	public static final String COPY_TAG_REFERENCES_ARG_DESC = "Creates a tag list entry pointing the new concept on all concepts and relations which had any of the collapsed concepts in their tag list.";

	public static final String DATASOURCE_PREFNAMES_ARG = "DataSourcePreferredNames";
	public static final String DATASOURCE_PREFNAMES_ARG_DESC = "A DataSource Restriction for adding preferred names only from that data source.";

}
