package net.sourceforge.ondex.tools.tab.importer;

/**
 * The usual <a href ="https://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a> applied to 
 * {@link DataReader}, so that the functionality of a reader can be enriched by pre/post processing. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>21 Feb 2017</dd></dl>
 *
 */
public class FilterDataReader extends DataReader
{
	protected DataReader base;

	public FilterDataReader ( DataReader base )
	{
		super ();
		this.base = base;
	}

	public boolean hasNext ()
	{
		return base.hasNext ();
	}

	public void setLine ( int lineNumber )
	{
		base.setLine ( lineNumber );
	}

	public void setLastLine ( int lineNumber )
	{
		base.setLastLine ( lineNumber );
	}

	public void close ()
	{
		base.close ();
	}

	@Override
	public String[] readLine () 
	{
		return base.readLine ();
	}

}
