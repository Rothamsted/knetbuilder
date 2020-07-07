package net.sourceforge.ondex.parser;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * A composite mapper joins the results of multiple {@link StreamMapper}s.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Jul 2017</dd></dl>
 *
 */
public class CompositeStreamMapper<S, OI> implements StreamMapper<S, OI>
{
	private Set<StreamMapper<S, OI>> mappers = Collections.emptySet ();
	
	@Override
	public Stream<OI> map ( S source, ONDEXGraph graph )
	{
		return this.getMappers ()
		.stream ()
		.flatMap ( mapper -> mapper.map ( source, graph ) );
	}

	public Set<StreamMapper<S, OI>> getMappers ()
	{
		return mappers;
	}

	public void setMappers ( Set<StreamMapper<S, OI>> mappers )
	{
		this.mappers = mappers;
	}
}
