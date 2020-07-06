package net.sourceforge.ondex.transformer.rankedpathways;

/**
 * @author hindlem
 */
public interface ArgumentNames {

    public static String STATE_MACHINE_DEF_ARG = "WeightedStateMachineFile";
    public static String STATE_MACHINE_DEF_ARG_DESC = "The flat file that defines the state machine and weightings for elements";

    public static String INCLUDE_UNRANKABLE_ARG = "IncludeUnrankable";
    public static String INCLUDE_UNRANKABLE_ARG_DESC = "Include all paths when no ranks within the routes for a given start point can be ranked?";

    public static String ADD_TAGS_ARG = "AddTags";
    public static String ADD_TAGS_ARG_DESC = "Add Tags of start and end node of path to all elements";

    public static String FILTER_REDUNDANT_ARG = "FilterOutRedundantPaths";
    public static String FILTER_REDUNDANT_ARG_DESC = "Filter out redundant paths that have the same concepts and relations but different state machine evidence";

    public static String MAKE_TAGS_VISIBLE_ARG = "TagsVisible";
    public static String MAKE_TAGS_VISIBLE_ARG_DESC = "Make visible Tags of elements in visible routes? else remove these Tags.";

    public static String MAX_PATHWAY_LENGTH_ARG = "MaxPathwayLength";
    public static String MAX_PATHWAY_LENGTH_ARG_DESC = "The Maximum length of any given pathway in states (concepts)";

    public static String INCLUDE_CONCEPT_CLASS_ARG = "ConceptClassInclusion";
    public static String INCLUDE_CONCEPT_CLASS_DESC = "Includes all concepts of the given ConceptClass and the there internal relations";

    //additional parameters for export
    public static final String FILE_ARG = "ExportFile";
    public static final String FILE_ARG_DESC = "Output file";

    public static final String ZIP_FILE_ARG = "Zip";
    public static final String ZIP_FILE_ARG_DESC = "GZip the output";

    public static final String ATTRIB_ARG = "Attributes";
    public static final String ATTRIB_ARG_DESC = "Attribute of the concept or relation to parse - valid format is position_in_path:type:subtype. Position numbering starts from 0 and * indicates all levels. Valid combinations are:" +
            " NN:name," +
            " NN:accession:DataSource," +
            " NN:cv," +
            " NN:gds:AttributeName," +
            " NN:evidence," +
            " NN:class," +
            " NN:pid," +
            " NN:description," +
            " NN:context:ConceptClass" +
            " Example: 0:accession:TAIR or *:pid";

    public static final String LINKS_ARG = "UseAccessionLinks";
    public static final String LINKS_ARG_DESC = "Write Accessions as Excel links";

    public static final String TRANSLATE_TAXID_ARG = "TranslateTaxId";
    public static final String TRANSLATE_TAXID_ARG_DESC = "Translates TAXID gds values to there scientific name";

}
