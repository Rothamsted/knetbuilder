package net.sourceforge.ondex.data;

import java.io.File;
import java.net.URI;

/**
 * The interface for classes that download data files necessary for running a producer (usualt a parser)
 * @author hindlem
 *
 */
public interface DataRetrieval {

	/**
	 * 
	 * @return a list of URI's retrieved by this parser
	 */
	public URI[] getURIs();
	
	/**
	 * 
	 * @param directory
	 * @return success?
	 */
	public boolean fetchFiles(File directory);

	/**
	 * 
	 * @return progress in downloading this file 0-1
	 */
	public double getProgressOnFile();
	
	/**
	 * 
	 * @return progress overall for for all operations 0-1
	 */
	public double getProgressOnOverall();
	
	/**
	 * 
	 * @return the current textual status of progress
	 */
	public String getCurrentStatus();
}
