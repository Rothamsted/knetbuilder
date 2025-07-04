package net.sourceforge.ondex.parser.owl;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.export.oxl.Export;

/**
 * A scrap test to ensure the whole ontology can be loaded.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>15 May 2017</dd></dl>
 *
 */
public class LoadingTest
{
	@Test
	// @Ignore ( "Not a real test, very time consuming" )
	public void testLoad () throws Exception
	{
		String owlDir = "/tmp";
		ONDEXGraph g = load ( "go_cfg.xml", owlDir + "/go.owl" );
		
		Export.exportOXL ( g, "/tmp/loading_test.oxl" );
	}
	
	public static ONDEXGraph load ( String cfgPath, String owlPath ) throws Exception
	{
		try ( ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext ( cfgPath ) ) {
			return OWLMapper.mapFrom ( null, ctx, owlPath );
		}
	}
}
