package net.sourceforge.ondex.parser2;

import java.util.stream.Stream;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>30 May 2017</dd></dl>
 *
 */
@FunctionalInterface
public interface Scanner<S, SI>
{
	public Stream<SI> scan ( S source ); 
}
