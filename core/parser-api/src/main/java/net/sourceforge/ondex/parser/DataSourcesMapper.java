package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.DataSource;

/**
 * Maps a source of information of type S to multiple ONDEX {@link DataSource}s.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>3 Aug 2017</dd></dl>
 *
 */
public interface DataSourcesMapper<S> extends StreamMapper<S, DataSource>
{
}
