package net.sourceforge.ondex.parser;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A composite scanner joins the results of multiple {@link Scanner}s.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>21 Aug 2017</dd></dl>
 *
 */
public class CompositeScanner<S, SI> implements Scanner<S, SI>
{
	private Set<Scanner<S, SI>> scanners = Collections.emptySet ();
	
	@Override
	public Stream<SI> scan ( S source )
	{
		return this.scanners
		.stream ()
		.flatMap ( scanner -> scanner.scan ( source ) );
	}

	public Set<Scanner<S, SI>> getScanners ()
	{
		return scanners;
	}

	public void setScanners ( Set<Scanner<S, SI>> scanners )
	{
		this.scanners = scanners;
	}
}
