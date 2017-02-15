package net.sourceforge.ondex.parser.uniprot;

/**
 * 
 * @author peschr
 *
 */
public interface ArgumentNames {
	public final static String TAXID_ARG = "TaxId";
	public final static String TAXID_ARG_DESC = "Only parse proteins with exactly this taxonomy id";

	public final static String REFERENCE_ACCESSION_ARG = "DbRefAcc";
	public final static String REFERENCE_ACCESSION_ARG_DESC = "True, false. Load accessions used in existing Ondex graph";

	public final static String ACCESSION_ARG = "Accessions";
	public final static String ACCESSION_ARG_DESC = "A list of comma separated accession numbers (any acc, does not need to be uniprot acc), which should be parsed from the given uniprot file";

	public final static String ACCESSION_FILE_ARG = "AccessionsFile";
	public final static String ACCESSION_FILE_ARG_DESC = "Path to the file containing a list of accession numbers (one acc per every line), which should be parsed from the given uniprot file";
	
	public final static String TAG_INFORMATION_ARG = "TagInformation";
	public final static String TAG_INFORMATION_ARG_DESC = "True,false. defines if tag information should be attachted to the concepts";

	public final static String HIDE_LARGE_SCALE_PUBLICATIONS_ARG = "HideLargeScaleRef";
	public final static String HIDE_LARGE_SCALE_PUBLICATIONS_ARG_DESC = "True,false. Hide large scale references (publications).";

	public final static String GO_OBO_FILE_ARG = "GoFile";
	public final static String GO_OBO_FILE_ARG_DESC = "GO OBO file. This will link proteins with proper GO concepts of ConcepClass MolFunc, BioProc and CelComp. If not specified GO accessions are part of proteins.";

}


