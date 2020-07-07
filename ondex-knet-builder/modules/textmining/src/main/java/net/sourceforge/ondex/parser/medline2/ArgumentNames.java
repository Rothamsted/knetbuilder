package net.sourceforge.ondex.parser.medline2;

/**
 * 
 * @author keywan 
 *
 */
public final class ArgumentNames {

	public static final String PMID_FILE_ARG = "PubMedIdsFile";
	public static final String PMID_FILE_ARG_DESC = "File which contains PubMed IDs to be parsed into Ondex (separated by line break). Efetch web-service will be used to retrieve and create publication concepts.";
	
	public static final String IMP_CITED_PUB_ARG = "ImportCitedPMIDs";
	public static final String IMP_CITED_PUB_DESC = "Import publications that are cited in the Ondex graph. Efetch web-service will be used to retrieve all information and new publication concepts will be created.";
	
}
