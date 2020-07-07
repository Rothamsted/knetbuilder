package net.sourceforge.ondex.init;

import java.io.IOException;
import java.net.URISyntaxException;

import junit.framework.TestCase;
/**
 * 
 * @author lysenkoa
 *
 */
public class PluginDocumentationFactoryTest extends TestCase{
	
	

	public void testLoadValidPlugin() throws IOException, URISyntaxException{
		PluginRegistry.init(true);
		PluginDescription pb = new PluginDescription();
		pb.setCls("net.sourceforge.ondex.init.AnONDEXPlugin");
		pb.setDescription("desc");
		pb.setName("name");
		String result = PluginDocumentationFactory.getDocumentation(pb);
		//System.out.println(result);
	}
}
