package net.sourceforge.ondex.scripting.javascript;

import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.sourceforge.ondex.scripting.CommandInterpreter;
import net.sourceforge.ondex.scripting.FunctionException;
import net.sourceforge.ondex.scripting.OutputPrinter;
import net.sourceforge.ondex.scripting.ProcessingCheckpoint;
import net.sourceforge.ondex.scripting.ProxyTemplate;
import net.sourceforge.ondex.scripting.base.BasicProxyTemplate;
import net.sourceforge.ondex.scripting.base.ScriptingWrapper;
import net.sourceforge.ondex.scripting.base.UniversalConstants;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrapFactory;
import org.mozilla.javascript.WrappedException;

/**
 * 
 * @author lysenkoa
 * JavaScipt implementation of CommandInterpreter. Uses Rhino API to run commands
 * 
 */

public class JSInterpreter implements CommandInterpreter {
	private Scriptable scope;
	private Map<Class<?>, Class<?>> clsToWrapper;
	private boolean initialise = false;
	private ProcessingCheckpoint pc;
	Context cx; 
	
	/**
	 * Constructs Interpreter
	 */
	public JSInterpreter(){
		cx = ContextFactory.getGlobal().enterContext();
	}
	
	public String process(String command, OutputPrinter out) {
		Object result = null;
		if(pc != null)
			pc.processingStarted();
		cx.getFactory().enterContext();
		try{
			result = cx.evaluateString(scope, command, "<cmd>", 1, null);
		}
		catch(Exception e){
			//e.printStackTrace();
			if(e instanceof WrappedException && ((WrappedException)e).getWrappedException() instanceof FunctionException){
				e.printStackTrace();
				out.printAndPrompt(((WrappedException)e).getWrappedException().getMessage());
				return null;
			}
			else if(e instanceof EcmaError){
				e.printStackTrace();
				out.printAndPrompt(e.getMessage().substring(0, e.getMessage().length()-10));
				return null;
			}
			else if(e instanceof EvaluatorException){
				e.printStackTrace();
				//out.feedOutput("Unknow command or syntax error.");
				out.printAndPrompt(e.getMessage());
				return null;
			}
			out.printAndPrompt(e.getMessage());
			Context.exit();
			return null;
		}
		finally{
			try{
				if(pc != null)
					pc.processingFinished();	
			}
			catch(Exception y){
				y.printStackTrace();
			}
		}
		try{
			if(result == null){
				out.printAndPrompt("null");
			}
			else if(result.equals(Context.getUndefinedValue())){
				out.printAndPrompt("Undefined");
			}
			else{
				Class<?> wrp = clsToWrapper.get(result.getClass());
				if(wrp != null)
					out.printAndPrompt("[Object "+wrp.getSimpleName()+"]");
				else if(result instanceof NativeJavaObject){
					Object d = ((NativeJavaObject)result).unwrap();
					if(Number.class.isAssignableFrom(d.getClass()) || d instanceof String){
						out.printAndPrompt(Context.toString(result));	
					}
					else{
						out.printAndPrompt("[Object "+d.getClass().getSimpleName()+"]");	
					}
				}
				else{
					out.printAndPrompt(Context.toString(result));		
				}
			}	
		}
		catch(Exception e){
			out.printAndPrompt(e.getMessage());
			e.printStackTrace();
		}
		
		try{
			Context.exit();	
		}
		catch(Exception e){
			out.printAndPrompt(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	public Object evaluate(String command) {
		cx.getFactory().enterContext();
		Object result = null;
		try{
			result = cx.evaluateString(scope, command, "<cmd>", 1, null);
		}

		catch(Exception e){
			if(e instanceof WrappedException && ((WrappedException)e).getWrappedException() instanceof FunctionException){
				result = (((WrappedException)e).getWrappedException().getMessage());
			}
			else if(e instanceof EcmaError){
				result = (e.getMessage().substring(0, e.getMessage().length()-10));
			}
			else if(e instanceof EvaluatorException){
				e.printStackTrace();
				//out.feedOutput("Unknow command or syntax error.");
				result = (e.getMessage());
			}
			else{
				result = (e.getMessage());
				e.printStackTrace();	
			}
			Context.exit();	
			return null;
		}
		if(result.equals(Context.getUndefinedValue())){
			Context.exit();
			return "Done.";
		}
		if(Number.class.isAssignableFrom(result.getClass()) || String.class.isAssignableFrom(result.getClass())){
			Context.exit();
			return result;		
		}
		Class<?> wrp = clsToWrapper.get(result.getClass());
		if(wrp != null){
			Context.exit();
			return("[Object "+wrp.getName()+"]");	
		}
		String output = Context.toString(result);
		Context.exit();
		return output;
	}
	
	/**
	 * 
	 * @author lysenkoa
	 * 
	 * implementation of a WrapFactory that auto wraps classes as specified in the ProxyTemplate
	 *
	 */
	class ExtendedWrapFactory extends WrapFactory{
		protected ExtendedWrapFactory(){
			super();
		}
	
		@Override
		public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, java.lang.Object javaObject, java.lang.Class staticType){
			Class<?> wrp = clsToWrapper.get(javaObject.getClass());
			if(wrp == null){
				return super.wrapAsJavaObject(cx, scope, javaObject, staticType);
			}
			try {
				ScriptingWrapper sw = (ScriptingWrapper)wrp.newInstance();
				sw.wrap(javaObject);
				return new NativeJavaObject(scope, sw, staticType);
			} catch (Exception e) {
				e.printStackTrace();
				return super.wrapAsJavaObject(cx, scope, javaObject, staticType);
			}
		}
	}

	public String getPrompt() {
		return null;
	}

	public String getWelcomeMessage() {
		return null;
	}

	@Override
	public List<Class<?>> getDependancies() {
		return Arrays.asList(new Class<?>[]{BasicProxyTemplate.class});
	}



	@Override
	public void initialize(ProxyTemplate... proxyTemplates) {
		for(ProxyTemplate a :proxyTemplates){
			if(a instanceof BasicProxyTemplate){
				try {
					BasicProxyTemplate sa = (BasicProxyTemplate)a;
					//this.scope = cx.initStandardObjects();
					cx.getFactory().enterContext();
					this.scope = new ImporterTopLevel(cx);

					this.clsToWrapper = sa.getClsToWrapperMap();


					//ScriptableObject.defineClass(scope, JSTest.class);
					/*for(Class<?> cls : sa.getClsToWrapperMap().values()){
						ScriptableObject.defineClass(scope, cls);
					}*/
					cx.evaluateString(scope, "importPackage("+UniversalConstants.WRAPPER_PACKAGE+")", "<cmd>", 1, null);
					for(Member m :sa.getShadowFunctions()){
						cx.evaluateString(scope, "var " +m.getName() + " = Packages."+m.getDeclaringClass().getCanonicalName()+"."+m.getName()+";", "<cmd>", 1, null);
					}
					cx.setWrapFactory(new ExtendedWrapFactory());
				} catch (Exception e) {e.printStackTrace();Context.exit();}
				Context.exit();
				initialise = true;
				break;
			}
		}
	}

	/**
	 * Allows for direct injection into Rhino of a Java object.
	 * Developed by Marco Brandizi, cause I cannot make the templates mechanism to work.
	 */
	public void injectDirectly ( Object javaObject, String jsName )
	{
		Object wrappedObj = Context.javaToJS ( javaObject, scope );
		ScriptableObject.putProperty ( scope, jsName, wrappedObj );		
	}

	
	@Override
	public boolean isInitialised() {
		return initialise;
	}

	public void setProcessingCheckpoint(ProcessingCheckpoint pc) {
		this.pc = pc;
	}
}
