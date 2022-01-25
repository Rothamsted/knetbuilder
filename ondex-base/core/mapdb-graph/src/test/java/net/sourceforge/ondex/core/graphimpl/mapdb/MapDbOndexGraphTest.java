package net.sourceforge.ondex.core.graphimpl.mapdb;

import java.io.File;

import net.sourceforge.ondex.core.base.AbstractONDEXGraph;
import net.sourceforge.ondex.core.test.AbstractONDEXGraphTest;

/**
 * Tests memory implementation of AbstractONDEXGraph.
 * 
 * @author taubertj
 *
 */
public class MapDbOndexGraphTest extends AbstractONDEXGraphTest
{
	
	@Override
	protected AbstractONDEXGraph initialize ( String name )
	{
		new File ( "/tmp/" + name + "-mapdb.db" ).delete ();
		return new MapDbOndexGraph ( name );
	}
}
