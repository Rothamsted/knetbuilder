package net.sourceforge.ondex.tools.tab.importer;

/**
 * 
 * @author lysenkoa
 *
 */
public abstract class DataReader {
	protected abstract String [] readLine();
	public abstract boolean hasNext();
	public abstract boolean isOpen();
	public abstract void setLine(int lineNumber);
	public abstract void setLastLine(int lineNumber);
	public abstract void close();
	public abstract void reset();
}
