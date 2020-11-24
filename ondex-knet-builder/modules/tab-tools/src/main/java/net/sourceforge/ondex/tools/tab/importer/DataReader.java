package net.sourceforge.ondex.tools.tab.importer;

import java.util.Iterator;

/**
 * 
 * @author lysenkoa
 *
 */
public abstract class DataReader implements Iterator<String[]>, AutoCloseable 
{
	public abstract String [] readLine();
	
	@Override
	public abstract boolean hasNext();
	
	@Override
	public String[] next () {
		return readLine ();
	}
	
	public abstract boolean isOpen();
	public abstract void setLine(int lineNumber);
	public abstract void setLastLine(int lineNumber);
	
	@Override
	public abstract void close();
	public abstract void reset();
}
