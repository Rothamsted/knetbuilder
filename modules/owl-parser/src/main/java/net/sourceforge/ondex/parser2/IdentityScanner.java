package net.sourceforge.ondex.parser2;

import java.util.stream.Stream;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Jul 2017</dd></dl>
 *
 */
public class IdentityScanner<S> implements Scanner<S, S>
{
	@Override
	public Stream<S> scan ( S source ) {
		return Stream.of ( source );
	}
}
