package net.sourceforge.ondex.parser;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * A composite pair mapper joins the results of multiple {@link StreamPairMapper}s.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Aug 2017</dd></dl>
 *
 */
public class CompositeStreamPairMapper<S1, S2, OI> implements StreamPairMapper<S1, S2, OI>
{
	private Set<StreamPairMapper<S1, S2, OI>> mappers = Collections.emptySet ();
	
	@Override
	public Stream<OI> map ( S1 src1, S2 src2, ONDEXGraph graph )
	{
		return mappers
		.stream ()
		.flatMap ( mapper -> mapper.map ( src1, src2, graph ) );
	}

	public Set<StreamPairMapper<S1, S2, OI>> getMappers ()
	{
		return mappers;
	}

	public void setMappers ( Set<StreamPairMapper<S1, S2, OI>> mappers )
	{
		this.mappers = mappers;
	}
}
