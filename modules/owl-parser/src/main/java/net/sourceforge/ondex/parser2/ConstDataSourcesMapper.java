package net.sourceforge.ondex.parser2;

import java.util.stream.Stream;

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.utils.DataSourcePrototype;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Jul 2017</dd></dl>
 *
 */
public class ConstDataSourcesMapper<S> extends ConstStreamMapper<S, DataSourcePrototype, DataSource>
{
	private ConstDataSourceMapper<S> helper = new ConstDataSourceMapper<> ();
	
	public ConstDataSourcesMapper () {
		super ();
	}

	public ConstDataSourcesMapper ( DataSourcePrototype value ) {
		super ( value );
	}

	@Override
	public Stream<DataSource> map ( S source, ONDEXGraph graph )
	{
		this.helper.setValue ( this.getValue () );
		return Stream.of ( helper.map ( source ) );
	}
}
