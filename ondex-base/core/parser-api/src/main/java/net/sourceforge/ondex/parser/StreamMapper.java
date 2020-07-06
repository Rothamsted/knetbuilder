package net.sourceforge.ondex.parser;

import java.util.stream.Stream;

/**
 * A stream mapper maps a data source to multiple output elements.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>7 Jul 2017</dd></dl>
 *
 */
public interface StreamMapper<S, OI> extends Mapper<S, Stream<OI>>
{
}
