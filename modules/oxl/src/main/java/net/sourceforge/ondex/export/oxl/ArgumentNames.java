package net.sourceforge.ondex.export.oxl;

/**
 * Static String constants.
 * 
 */
public final class ArgumentNames {

	public final static String EXCLUDE_ATTRIBUTE_ARG = "ExcludeAttributeWithName";

	public final static String EXCLUDE_ATTRIBUTE_ARG_DESC = "This parameter can be used to exclude a number of attributes from being written in the ondex.xml file.\n"
			+ "When **//ALL//** is used all attributes are excluded and thus no Attribute values will be written.";

	public final static String EXCLUDE_C_WITH_CC_ARG = "ExcludeConceptsOfConceptClass";

	public final static String EXCLUDE_C_WITH_CC_ARG_DESC = "This parameter can be used to do some basic filtering on ConceptClass in the export method.\nThis is especially useful if graphs become to large.";

	public final static String EXCLUDE_R_WITH_RT_ARG = "ExcludeRelationsOfRelationType";

	public final static String EXCLUDE_R_WITH_RT_ARG_DESC = "This parameter can be used to do some basic filtering on RelationType in the export method.\nThis is especially useful if graphs become to large.";

	public final static String EXCLUSIVE_ATTRIBUTE_INCLUSION = "IncludeAttributesOfName";

	public final static String EXCLUSIVE_ATTRIBUTE_INCLUSION_DESC = "This parameter works by setting exclusive inclusions for a set of Attribute Attributes. All other GDSs Attributes not specified will be excluded.";

	public final static String EXCLUSIVE_C_WITH_CC_INCLUSION = "IncludeOnlyConceptClass";

	public final static String EXCLUSIVE_C_WITH_CC_INCLUSION_DESC = "This parameter works by setting exclusive inclusions for a set of Concept Classes. All other Concept Classes not specified will be excluded.";

	public final static String EXCLUSIVE_R_WITH_RT_INCLUSION = "IncludeOnlyRelationType";

	public final static String EXCLUSIVE_R_WITH_RT_INCLUSION_DESC = "This parameter works by setting exclusive inclusions for a set of Relation Types. All other Relation Types not specified will be excluded.";

	public final static String EXPORT_AS_ZIP_FILE = "GZip";

	public final static String EXPORT_AS_ZIP_FILE_DESC = "When this option is set the file wil be exported as a zip file, this is the preffered option as it reduces disc space requirements by a lot."
			+ "\nFor example a file of 1.4 GigaByte is often compressed to a file of 50 MegaByte or less!!";

	public final static String PRETTY_PRINTING = "pretty";

	public final static String PRETTY_PRINTING_DESC = "When this option is set the output XML is kind of pretty printed. This makes the output larger.";

	public final static String EXPORT_ISOLATED_CONCEPTS = "ExportIsolatedConcepts";

	public final static String EXPORT_ISOLATED_CONCEPTS_DESC = "When this is option is set, it will export also concepts without any relations.";
}
