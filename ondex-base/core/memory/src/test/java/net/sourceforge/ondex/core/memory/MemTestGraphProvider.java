package net.sourceforge.ondex.core.memory;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.test.TestGraphProvider;
import net.sourceforge.ondex.logging.ONDEXLogger;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>31 Mar 2022</dd></dl>
 *
 */
public class MemTestGraphProvider extends TestGraphProvider
{
	@Override
	public ONDEXGraph createGraph ( String name )
	{
		return new MemoryONDEXGraph ( name, new ONDEXLogger() );
	}

}
