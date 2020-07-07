package net.sourceforge.ondex.export.delimitedfile;

/**
 * 
 * @author lysenkoa
 *
 */
public interface ArgumentNames {

	public static final String ORDER_ARG = "Order";
	public static final String ORDER_ARG_DESC = "Path to look for in the graph. Groups of concepts and relations separated by # between the groups and by , with the group e.g Protein,Gene#it_with#Protein";
	
	public static final String FILE_ARG = "ExportFile";
	public static final String FILE_ARG_DESC = "Output file";
	
	public static final String ZIP_FILE_ARG = "Zip";
	public static final String ZIP_FILE_ARG_DESC = "GZip the output";
	
	public static final String ATTRIB_ARG = "Attributes";
	public static final String ATTRIB_ARG_DESC = "Attribute of the concept or relation to parse - valid format is position_in_path:type:subtype. Position numbering starts from 0. Valid combinations are: NN:name, NN:accession:DataSource, NN:cv, NN:gds:AttributeName, NN:evidence, NN:class, NN:pid, NN:description, NN:context:ConceptClass and NN:synonyms Example: 0:accession:TAIR";
	
	public static final String COMPLETE_ONLY_ARG = "OnlyComplete";
	public static final String COMPLETE_ONLY_DESC = "Do not output partial path hits.";
	
	public static final String MIN_REPORT_DEPTH_ARG = "Min";
	public static final String MIN_REPORT_DEPTH_ARG_DESC = "Report incomplete entries over or equal to this depth.";
	
	public static final String REMOVE_DUPLICATES_ARG = "RemoveDuplicates";
	public static final String REMOVE_DUPLICATES_ARG_DESC = "Remove duplicate entries - on large graphs is slow and can cause the application to run out of memory";

	public static final String LINKS_ARG = "UseAccessionLinks";
	public static final String LINKS_ARG_DESC = "Write Accessions as Excel links";

	public static final String TRANSLATE_TAXID_ARG = "TranslateTaxId";
	public static final String TRANSLATE_TAXID_ARG_DESC = "Translates TAXID gds values to there scientific name";
}
