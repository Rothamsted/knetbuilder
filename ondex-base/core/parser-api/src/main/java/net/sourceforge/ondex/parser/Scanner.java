package net.sourceforge.ondex.parser;

import java.util.stream.Stream;

/**
 * A scanner decomposes a data source S onto smaller items SI. For instance, a whole XML document might be decomposed
 * into the top XML elements, or a CSV into its rows. Items are supposed to be mapped by a {@link Mapper} or a 
 * {@link PairMapper}. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>30 May 2017</dd></dl>
 *
 */
@FunctionalInterface
public interface Scanner<S, SI> extends Visitable<S>
{
	public Stream<SI> scan ( S source ); 
}
