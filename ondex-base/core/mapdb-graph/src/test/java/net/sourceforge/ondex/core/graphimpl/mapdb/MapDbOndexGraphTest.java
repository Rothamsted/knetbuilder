package net.sourceforge.ondex.core.graphimpl.mapdb;

import static net.sourceforge.ondex.core.graphimpl.mapdb.MapOverMapDbSetTest.MAPDB_DIR;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.BeforeClass;

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
	private MapDbOndexGraph graph;
	
  private Logger log = Logger.getLogger ( this.getClass() );
	
	
	@BeforeClass
	public static void initMapDbDir ()
	{
		MapDbOndexGraph.setMapDbDir ( MAPDB_DIR );
	}
	
	/**
	 * Following the design of {@link AbstractONDEXGraphTest}, rather than using {@code @Before}.
	 * @throws  
	 */
	@Override
	protected AbstractONDEXGraph initialize ( String name ) throws IOException
	{
		var dir = new File ( MAPDB_DIR );
		dir.mkdir ();
		FileUtils.cleanDirectory ( dir );
		
		graph = new MapDbOndexGraph ( name );
		//log.info ( "mapdb graph initialised" );
		return graph;
	}
	
	@After
	public void closeGraph ()
	{
		//log.info ( "Closing mapdb graph" );
		this.graph.close ();
	}
}
