package uk.ac.rothamsted.knetminer.backend;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.utils.OndexPluginUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * @author jojicunnunni
 * 
 * <dl><dt>Date:</dt><dd>23 Feb 2022</dd></dl>
 *
 */
public class KnetMinerInitializerPlugInTest
{
	private static ONDEXGraph graph;
	
	@BeforeClass
	public static void initGraph ()
	{
		// TODO: Load the graph, as before
	}
	
	@Test
	public void testBasics ()
	{
		Map<String, Object> pluginArgs = new HashMap<> ();
		
		// TODO: populate the args with test params (see KnetMinerInitializerTest)
		
		// TODO: check this runs and do basic tests on output directories existence
		// (we don't need detailed verifications here, they already occurs in KnetMinerInitializerTest)		
		OndexPluginUtils.runPlugin ( KnetMinerInitializerPlugIn.class, graph, pluginArgs );
	}
}
