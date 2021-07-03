package net.sourceforge.ondex.mini.test;

import java.util.Arrays;

import org.apache.log4j.Logger;

import com.google.common.collect.ObjectArrays;

import net.sourceforge.ondex.OndexMiniMain;

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
	private String ondexDirPath = null;
	private String plugInDirPath = null;
	private String libDirPath = null;

  private Logger log = Logger.getLogger ( this.getClass() );
	
	public void invoke ( String workFlowPath, String ... optArgs )
	{
		System.setProperty ( "ondex.dir", getOndexDirPath () );
		System.setProperty ( "ondex.plugin.dir", getPlugInDirPath () );
		System.setProperty ( "ondex.lib.dir", getLibDirPath () );
		
		String[] args = new String[] { "-u", "fooTestUser", "-p", "foo-pass", "-w", workFlowPath }; 
		args = ObjectArrays.concat ( args, optArgs, String.class );
		
		log.info ( "Invoking Ondex-Mini with: " + Arrays.toString ( args ) );
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
		return getAbsoluteDir ( ondexDirPath, "data" );
	}


	public void setOndexDirPath ( String ondexDirPath )
	{
		this.ondexDirPath = ondexDirPath;
	}


	public String getPlugInDirPath ()
	{
		return getAbsoluteDir ( plugInDirPath, "plugins" );
	}


	public void setPlugInDirPath ( String plugInDirPath )
	{
		this.plugInDirPath = plugInDirPath;
	}


	public String getLibDirPath ()
	{
		return getAbsoluteDir ( libDirPath, "lib" );
	}


	public void setLibDirPath ( String libDirPath )
	{
		this.libDirPath = libDirPath;
	}

	private String getAbsoluteDir ( String localMiniDir, String defaultDir )
	{
		String result = this.miniStartDirPath;
		if ( result == null ) result = "";
		if ( !( result.isEmpty () || result.endsWith ( "/" ) ) ) result += "/";
		return result + (localMiniDir != null ? localMiniDir : defaultDir);
	}
}
