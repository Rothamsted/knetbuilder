package net.sourceforge.ondex.transformer.removeattribute;

/**
 * 
 * @author hindlem
 *
 */
public interface ArgumentNames {

	public final static String DATASOURCE_ARG = "DataSource";
	public final static String DATASOURCE_ARG_DESC = "DataSource of concepts to remove in";
	
	public final static String CONCEPTCLASS_ARG = "ConceptClass";
	public final static String CONCEPTCLASS_ARG_DESC = "ConceptClass of concepts to remove in";
	
	public final static String ATTRIBUTE_NAME_ARG = "AttributeName";
	public final static String ATTRIBUTE_NAME_ARG_DESC = "AttributeName of Attributes to remove";
	
	public final static String ACCESSION_DATASOURCE_ARG = "AccessionDataSource";
	public final static String ACCESSION_DATASOURCE_ARG_DESC = "DataSource of Accessions to remove";
	
	public final static String RELATIONTYPE_ARG = "RelationType";
	public final static String RELATIONTYPE_ARG_DESC = "RelationType of relations to remove in";
	
	public final static String EXCLUDE_ARG = "RemoveMatches";
	public final static String EXCLUDE_ARG_DESC = "If false remove non-matching attributes / accessions from concepts or relations.";
}
