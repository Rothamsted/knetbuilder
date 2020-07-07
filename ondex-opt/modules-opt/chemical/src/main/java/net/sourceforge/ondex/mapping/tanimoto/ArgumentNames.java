package net.sourceforge.ondex.mapping.tanimoto;

/**
 * ArgumentNames for the blast based mapping.
 * 
 * @author taubertj
 * 
 */
public interface ArgumentNames extends
		net.sourceforge.ondex.mapping.ArgumentNames {

	public final static String PATH_TO_EXEC_ARG = "PathToExecutable";

	public final static String PATH_TO_EXEC_ARG_DESC = "Path to OpenBable executable.";

	public final static String CUTOFF_ARG = "Cutoff";

	public final static String CUTOFF_ARG_DESC = "Tanimoto cutoff value.";

	public final static String WITHIN_DATASOURCE_ARG = "WithinSameDataSource";

	public final static String WITHIN_DATASOURCE_ARG_DESC = "Allow mapping within the same DataSource.";

	public final static String EXEC_OPTIONS_ARG = "Options";

	public final static String EXEC_OPTIONS_ARG_DESC = "Additional command line options for OpenBable.";

}