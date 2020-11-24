package net.sourceforge.ondex.tools.tab.importer;

import java.util.Iterator;

/**
 * 
 * @author lysenkoa
 * @uthor Marco Brandizi (reviewed in 2020)
 * 
 * Note that the comments below are based on educated guesses and code fixes on
 * {@link DelimitedReader}. Initially, no comment was available and the implementations
 * were inconsistent.
 *
 */
public abstract class DataReader implements Iterator<String[]>, AutoCloseable 
{
	/**
	 * An alias of {@link #next()}, ie, reads the next line and should be called if {@link #hasNext()} is true.
	 */
	public abstract String [] readLine();
	
	@Override
	public abstract boolean hasNext();
	
	@Override
	public String[] next () {
		return readLine ();
	}
	
	/**
	 * Position the reader to the index lineNumber (starting from 0). This operation might not be 
	 * possible or might be possible only in a forward direction, or only before start reading.
	 * 
	 */
	public abstract void setLine ( int lineNumber );
	
	/**
	 * Reads up to lineNumber lines (eg, from 0 to thi value, excluded).
	 * Similarly to {@link #setLine(int)}, this operation might be not supported.
	 * 
	 */
	public abstract void setLastLine ( int lastLineExcluded );
	
	@Override
	public abstract void close();

}
