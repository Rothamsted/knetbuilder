package net.sourceforge.ondex.parser.utils;

import java.util.function.Function;

import uk.ac.ebi.utils.string.StringSearchUtils;

/**
 * An utility to convert a string like biological_process to Biological_Process.
 * 
 * All substrings preceded by ' ' or '_' are converted to upper case, in addition to the first character. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 May 2018</dd></dl>
 *
 */
public class WordCapitalizer implements Function<String, String>
{
	@Override
	public String apply ( String s )
	{
		if ( s == null ) return null;
		StringBuilder sb = new StringBuilder ( s );
		int len = s.length ();
		for ( int i = 0; i < len; )
		{
			// First char and the next after a separator
			sb.setCharAt ( i, Character.toUpperCase ( s.charAt ( i ) ) );

			// Go to the next separator, skipping contiguous ones
			for ( i++; ; )
			{
				i = StringSearchUtils.indexOfAny ( s, i, ' ', '_' );
				if ( i == -1 || ++i == len ) break;
				char c = s.charAt ( i );
				if ( !( c == ' ' || c == '_' ) ) break;
			}
			// And, if not last, to the char after the separator
			if ( i == -1 || i == len ) break;
		}
		return sb.toString ();
	}	
}
