package net.sourceforge.ondex.init;

import java.io.IOException;
import java.net.URISyntaxException;

import junit.framework.TestCase;
/**
 * 
 * @author lysenkoa
 *
 */
public class PluginRegistryTest extends TestCase{

	
	 public void testPluginLoadingPositive() throws IOException, URISyntaxException{
		 PluginRegistry.init(true);
		 PluginRegistry.getInstance().loadCls("net.sourceforge.ondex.init.AnONDEXPlugin");
	 }
	 
	 public void testPluginLoadingNegative() throws IOException, URISyntaxException, ClassNotFoundException{
		 PluginRegistry.init(true);
		 try{
			 PluginRegistry.getInstance().loadCls("net.sourceforge.ondex.init.NotAbstractONDEXPlugin");
		 }
		 catch(Exception e){
			 return;
		 }
		 fail("Only ONDEXPlugin are allowed to be loaded by PluginRegistry");
	 }
}
