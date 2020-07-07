package net.sourceforge.ondex.parser.utils;

import java.util.stream.Stream;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.parser.AccessionMapper;
import net.sourceforge.ondex.parser.Scanner;
import net.sourceforge.ondex.parser.ScannerAccessionsMapper;

/**
 * A utility {@link Scanner} that takes the data source parameter and returns a singleton stream made of this
 * source only. This can be useful when working with Spring configurations, to adapt a single value achievable from 
 * a data source to a component that expects to decompose the source into multiple values. For instance, you might want 
 * to use this in a {@link ScannerAccessionsMapper}, so that a data source returns itself as the only one item and later
 * an {@link AccessionMapper} will map this onto a single {@link ConceptAccession}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Jul 2017</dd></dl>
 *
 */
public class IdentityScanner<S> implements Scanner<S, S>
{
	private boolean ignoreNull = true;
	
	@Override
	public Stream<S> scan ( S source ) {
		return source == null && ignoreNull ? Stream.empty () : Stream.of ( source );
	}

	/**
	 * When true (default), null values are translated to empty streams.
	 */
	public boolean isIgnoreNull ()
	{
		return ignoreNull;
	}

	public void setIgnoreNull ( boolean ignoreNull )
	{
		this.ignoreNull = ignoreNull;
	}
}
