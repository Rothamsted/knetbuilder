package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.util.CachedGraphWrapper;
import net.sourceforge.ondex.core.util.prototypes.DataSourcePrototype;

/**
 * Maps anything to constant {@link DataSource}, which can be defined in a Spring configuration file, by means of
 * {@link DataSourcePrototype}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Jul 2017</dd></dl>
 *
 */
public class ConstDataSourceMapper<S> extends ConstMapper<S, DataSourcePrototype, DataSource>
  implements DataSourceMapper<S>
{
	public ConstDataSourceMapper () {
		this ( null );
	}

	public ConstDataSourceMapper ( DataSourcePrototype value ) {
		super ( value );
	}

	
	@Override
	public DataSource map ( S source, ONDEXGraph graph )
	{
		return CachedGraphWrapper
		.getInstance ( graph )
		.getDataSource ( this.getValue () );
	}
}
