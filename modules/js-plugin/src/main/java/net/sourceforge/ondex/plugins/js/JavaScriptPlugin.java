package net.sourceforge.ondex.plugins.js;

import java.io.File;
import java.io.FileReader;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.console.OVTKScriptingInitialiser;
import net.sourceforge.ondex.scripting.BasicInterpretationController;
import net.sourceforge.ondex.scripting.CommandInterpreter;
import net.sourceforge.ondex.scripting.InterpretationController;
import net.sourceforge.ondex.scripting.OutputPrinter;
import net.sourceforge.ondex.scripting.ProxyTemplate;
import net.sourceforge.ondex.scripting.TemplateBuilder;
import net.sourceforge.ondex.scripting.base.JavaProxyTemplate;
import net.sourceforge.ondex.scripting.javascript.JSInterpreter;
import net.sourceforge.ondex.scripting.ui.CommandEvent;
import net.sourceforge.ondex.scripting.ui.ScriptingShell;
import net.sourceforge.ondex.scripting.wrappers.ContextualReferenceResolver;
import net.sourceforge.ondex.scripting.wrappers.OndexScriptingInitialiser;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

/**
 * An Integrator plug-in that allows for invoking a JavaScript file from an Integrator workflow.
 * Comes with ONDEX entities available inside the scripting engine, so that you can run the same scripts
 * that are available from the ONDEX Console component.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Jan 2017</dd></dl>
 *
 */
@Status ( status = StatusType.EXPERIMENTAL )
public class JavaScriptPlugin extends ONDEXTransformer
{
  private Logger log = Logger.getLogger ( this.getClass() );
  
  private static final String SCRIPT_PATH_ARG_NAME = "Script Path"; 
  
  private static JavaScriptPlugin instance;

  
  public JavaScriptPlugin () {
  	instance = this;
  }
  
  /**
   * Runs a script located in a file
   * @param scriptPath the script file
   * @param graph it's passed to the scripting engine
   */
  private void runScriptFromPath ( String scriptPath, ONDEXGraph graph ) throws Exception
  {
		runScript ( 
			IOUtils.toString ( new FileReader ( new File ( scriptPath ) ) ),
			graph
		);
  }
  
  /**
   * Runs a script
   * @param script the script code
   * @param graph it's passed to the scripting engine
   */
	private void runScript ( String script, final ONDEXGraph graph ) throws Exception 
	{
		OutputPrinter out = new ScriptingShell ( "ONDEX>", "Integrator Js Plug-In" );
		JSInterpreter jsi = new JSInterpreter ();
		
		new OVTKScriptingInitialiser () 
		{{
			reset (); // I cannot make the scripting engine to work twice in the same run without this
			initialiseAspectBuilder(); // Doesn't work without
		}};

		// Makes the current graph available to the scripting environment as getActiveGraph()
		OndexScriptingInitialiser.setGraphResolver ( new ContextualReferenceResolver<ONDEXGraph>() 
		{
			public ONDEXGraph resolveRef ( Object arg ) throws Exception {
				return arg == null ? graph : null;
			}
		});;
		
		JavaProxyTemplate proxyTemplate = OndexScriptingInitialiser.getProxyTemplateWithDoc();
		InterpretationController ic = new BasicInterpretationController(
			new TemplateBuilder[] {}, new ProxyTemplate[] { proxyTemplate }, new CommandInterpreter[] { jsi }
		);
		ic.setInterpreterOrder ( JSInterpreter.class );
		CommandEvent cmdEv = new CommandEvent ( "ONDEX Integrator", script, out );
		// We cannot make the template mechanism to work, so I've written this wrapper.
		jsi.injectDirectly ( instance.getArguments().getObjectValueArray ( "Script Argument(s)" ), "scriptArgs" );
		ic.newCommand ( cmdEv );
	}  
  
	public String getId ()
	{
		return "javaScriptPlugin";
	}

	public String getName ()
	{
		return "JavaScript Plug-in";
	}

	public String getVersion ()
	{
		return "1.0-SNAPSHOT";
	}

	public ArgumentDefinition<?>[] getArgumentDefinitions ()
	{
		return new ArgumentDefinition<?>[]
		{
      new FileArgumentDefinition ( 
      	SCRIPT_PATH_ARG_NAME, 
      	"The path to the script to be run", 
      	true, // required  
      	true, // pre-existing
      	false, // is-directory
      	false // multi-instance
      ),
      new StringArgumentDefinition ( 
      	"Script Argument(s)", 
      	"Argument for the script. Multiple values can be given and the script code will see them via scriptArgs", 
      	false, // required
      	null, // default 
      	true // multi-instance
      )
		};
	}

	public void start () throws Exception
	{	
		String scriptPath = (String) getArguments().getUniqueValue( SCRIPT_PATH_ARG_NAME );
		runScriptFromPath ( scriptPath, this.graph );
	}
	
	
	@Override
	public String[] requiresValidators ()
	{
		return new String [ 0 ];
	}

	@Override
	public boolean requiresIndexedGraph ()
	{
		return false;
	}

}
