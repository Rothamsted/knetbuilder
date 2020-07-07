package net.sourceforge.ondex.parser;

import java.util.stream.Stream;

/**
 * A marker for a stream pair mapper. Similarly to {@link StreamMapper}, this takes a data source and returns multiple 
 * outputs, possibly after having split it into multiple data source items, via {@link Scanner scanners}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Jul 2017</dd></dl>
 *
 */
public interface StreamPairMapper<S1, S2, OI> extends PairMapper<S1, S2, Stream<OI>>
{
}
