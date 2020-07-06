package net.sourceforge.ondex.tools.tab.importer;
/**
 * An interface that defines a custom parser for prototype-based parsing
 * @author lysenkoa
 *
 */
public interface TaggedInputReader {
	/**
	 * 
	 * @return next resource in the stream or queue.
	 */
	public TaggedInput next();
	
	/**
	 * Re-starts the processing form the beginning
	 */
	public void reset();
	
	/**
	 * Runs the clean-up methods associated with this parser
	 */
	public void close();
}
