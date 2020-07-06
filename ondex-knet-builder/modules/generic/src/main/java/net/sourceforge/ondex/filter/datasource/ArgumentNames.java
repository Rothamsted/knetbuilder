package net.sourceforge.ondex.filter.datasource;

/**
 * Arguments for DataSource Filter
 * @author hindlem
 *
 */
public interface ArgumentNames {

	public static final String DATASOURCE_ARG = "DataSource";
	public static final String DATASOURCE_ARG_DESC = "A DataSource to include/exclude Concepts and Relations of (see Exclude param)";

	public static final String EXCLUDE_ARG = "Exclude";
	public static final String EXCLUDE_ARG_DESC = "Exclude concepts and relations that meet the given crieria, else if false then exclusivly include?";

}
