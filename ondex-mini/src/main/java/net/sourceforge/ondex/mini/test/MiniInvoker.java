package net.sourceforge.ondex.mini.test;

import java.text.MessageFormat;

import com.google.common.collect.ObjectArrays;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.OndexMiniMain;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import net.sourceforge.ondex.parser.oxl.Parser;

/**
 * Invokes ONDEX-Mini to run a workflow file.
 * 
 * <p>This is here mainly to help you with integration tests. It brings up the whole MINI framework, pretty much like the
 * command line invokation does.</p> 
 * 
 * <p><b>WARNING</b>: Very likely you will need to 1) declare the dependency on the oxl module 2) Use the maven-dependency
 * plug-in to unpack the whole ONDEX mini package and make everything it needs (config files, lib/ and plugins/ 
 * directories, etc) in place before using this class.</p>
 * 
 * <p>See the textmining module for an example on how to setup things right and run this invoker.</p>
 * 
 * TODO: I'm not sure it works across multiple calls within the same JVM process.  
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>18 Sep 2017</dd></dl>
 *
 */
public class MiniInvoker
{
	private String miniStartDirPath = "target/ondex-mini/";
	private String ondexDirPath = miniStartDirPath + "data";
	private String plugInDirPath = miniStartDirPath + "plugins";
	private String libDirPath = miniStartDirPath + "lib";

	
	public void invoke ( String workFlowPath, String ... optArgs )
	{
		System.setProperty ( "ondex.dir", ondexDirPath );
		System.setProperty ( "ondex.plugin.dir", plugInDirPath );
		System.setProperty ( "ondex.lib.dir", libDirPath );
		
		String[] args = new String[] { "-u", "fooTestUser", "-p", "foo-pass", "-w", workFlowPath }; 
		args = ObjectArrays.concat ( args, optArgs, String.class );
		
		OndexMiniMain.main ( args );
	}


	public String getMiniStartDirPath ()
	{
		return miniStartDirPath;
	}


	public void setMiniStartDirPath ( String miniStartDirPath )
	{
		this.miniStartDirPath = miniStartDirPath;
	}


	public String getOndexDirPath ()
	{
		return ondexDirPath;
	}


	public void setOndexDirPath ( String ondexDirPath )
	{
		this.ondexDirPath = ondexDirPath;
	}


	public String getPlugInDirPath ()
	{
		return plugInDirPath;
	}


	public void setPlugInDirPath ( String plugInDirPath )
	{
		this.plugInDirPath = plugInDirPath;
	}


	public String getLibDirPath ()
	{
		return libDirPath;
	}


	public void setLibDirPath ( String libDirPath )
	{
		this.libDirPath = libDirPath;
	}
	
	public ONDEXGraph loadOXL ( String filePath, ONDEXGraph graph )
	{
		try
		{
			if ( graph == null ) graph = new MemoryONDEXGraph ( "default" );
			
			Parser parser = new Parser ();
			parser.setONDEXGraph ( graph );
			ONDEXPluginArguments args = new ONDEXPluginArguments ( parser.getArgumentDefinitions () );
			args.setOption ( FileArgumentDefinition.INPUT_FILE, filePath );
			parser.setArguments ( args );
			parser.start ();
			return graph;
		}
		catch ( PluginConfigurationException ex )
		{
			throw new RuntimeException ( MessageFormat.format ( 
				"Internal error while loading '{}': {}", filePath, ex.getMessage () 
			), ex);
		}
	}
	
	public ONDEXGraph loadOXL ( String filePath )
	{
		return loadOXL ( filePath, null );
	}

}
