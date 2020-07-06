package net.sourceforge.ondex.tools.tab.importer;

/**
 * A filter that removes blank lines from its base data reader
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>21 Feb 2017</dd></dl>
 *
 */
public class BlankLinesFilterDataReader extends FilterDataReader
{
	private String[] nextLine = null;

	/**
	 * @see #toNextLine()
	 */
	private boolean isBeforeFirst = true;
	
	public BlankLinesFilterDataReader ( DataReader base )
	{
		super ( base );
	}
	
	/**
	 * @see #toNextLine()
	 * 
	 * @return true if there is one more non-blank line
	 */
	@Override
	public boolean hasNext ()
	{
		if ( isBeforeFirst ) 
		{
			this.toNextLine ();
			isBeforeFirst = false;
		}
		return this.nextLine != null;
	}

	/**
	 * @see #toNextLine()
	 * 
	 * @return the next non-blank line or null, if none is available.
	 */
	@Override
	public String[] readLine ()
	{
		try {
			return this.nextLine;
		}
		finally {
			this.toNextLine ();
		}
	}

	/**
	 * Utility used above to move to the next non-blank line.
	 * 
 	 * The semantics initially implemented for {@link DataReader} is not so clear. So, let's assume, 
	 * {@link #hasNext()} is true when there is a following item and that only {@link #readLine()} move the cursor forward.
	 * 
	 * This way, the safest thing to do is to move forward with this method the very first time a cursor operation is 
	 * invoked. It's also safe to assume the creation or opening of {@link DataReader} doesn't touch the cursor, hence we 
	 * need {@link #isBeforeFirst}, which is updated by {@link #hasNext()} (or, indirectly, by {@link #readLine()}.
	 * 
	 */
	private void toNextLine () 
	{
		this.nextLine = null;
		while ( super.hasNext () && isEmptyRow ( this.nextLine = super.readLine () ) );
	}
	
	/**
	 * @return true if row is null, zero length, or has values which are all null or empty when trimmed
	 */
	public static boolean isEmptyRow ( String[] row )
	{
		if ( row == null || row.length == 0 ) return true;
		for ( String value: row )
			if ( value != null && !value.trim ().isEmpty () ) return false;
		return true;
	}	

}
