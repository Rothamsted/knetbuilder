package net.sourceforge.ondex.parser2;

import java.util.stream.Stream;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * This is like {@link ConstMapper}, but adapts to a {@link StreamMapper}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Jul 2017</dd></dl>
 *
 */
public class ConstStreamMapper<S, C, OI> extends ConstMapper<S, C, Stream<OI>> implements StreamMapper<S, OI>
{
	public ConstStreamMapper () {
		this ( null );
	}

	public ConstStreamMapper ( C value ) {
		super ( value );
	}

	
	@Override
	@SuppressWarnings ( "unchecked" )
	public Stream<OI> map ( S source, ONDEXGraph graph )
	{
		return Stream.of ( (OI) this.getValue () );
	}	
}
