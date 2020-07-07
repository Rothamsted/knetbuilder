package net.sourceforge.ondex.parser.medline.args;

/**
 * 
 * @author hindlem
 *
 */
public final class ArgumentNames {

	public static final String LOWERXMLBOUNDARY_ARG = "LowerXmlBoundary";
	public static final String LOWERXMLBOUNDARY_DESC = "Only parses MEDLINE XMLs greater than this boundary";
	
	public static final String UPPERXMLBOUNDARY_ARG = "UpperXmlBoundary";
	public static final String UPPERXMLBOUNDARY_DESC = "Only parses MEDLINE XMLs lower than this boundary";
	
	public static final String PREFIX_ARG = "Prefix";
	public static final String PREFIX_DESC = "The Prefix letters that start the medline file in the form of \"medline07n\" where \"07\" is the date ";
	
	public static final String COMPRESSION_ARG = "Compression";
	public static final String COMPRESSION_DESC = "The Compression file type currently on gunzip denoted by gz is permitted";
	
	public static final String IDLIST_ARG = "PMIDInputList";
	public static final String IDLIST_DESC = "List of PUBMED IDs, deliminated by semicolon";
	
	public static final String IMP_ONLY_CITED_PUB_ARG = "ImportOnlyCitedPublications";
	public static final String IMP_ONLY_CITED_PUB_DESC = "Import only publications that are cited in the ondexgraph";
	
	public static final String XMLFILES_ARG = "XmlFiles";
	public static final String XMLFILES_DESC = "Medline XML files to include";
	
	public static final String PUBMEDFILE_ARG = "PubMedFile";
	public static final String PUBMEDFILE_DESC = "An XML file which contains the results of a PubMed search";
	
	public static final String USE_EFETCH_WS = "UseEfetchWebService";
	public static final String USE_EFETCH_WS_DESC = "True/False. If true uses NCBI's efetch to get publications. If false (default) prases local files.";
	
	
	
	

}
