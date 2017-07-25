package net.sourceforge.ondex.parser2;

import java.util.stream.Stream;

import net.sourceforge.ondex.core.ConceptAccession;

/**
 * A utility {@link Scanner} that takes the data source parameter and returns a singleton stream made of this
 * source only. This can be useful when working with Spring configurations, to adapt a single value achievable from 
 * a data source to a component that expects to decompose the source into multiple values. For instance, you might want 
 * to use this in a {@link AbstractAccessionsMapper}, so that a data source returns itself as the only one item and later
 * an {@link AccessionMapper} will map this onto a single {@link ConceptAccession}.
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
