package net.sourceforge.ondex.scripting;

import net.sourceforge.ondex.scripting.base.UniversalProxyTemplateBuilder;

public abstract class AbstractScriptingInitialiser {
	public static OutputPrinter mainOutputPrinter;
	protected static UniversalProxyTemplateBuilder proxyTemplateBuilder;
	protected AbstractScriptingInitialiser(){}
	protected static void initialiseProxyTemplateBuilder(){
		proxyTemplateBuilder = UniversalProxyTemplateBuilder.getInstance();
		try{
			proxyTemplateBuilder.addRootMethod(UniversalProxyTemplateBuilder.getMethodByName(AbstractScriptingInitialiser.class, "getOutputPrinter"));
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	public static OutputPrinter getOutputPrinter(){
		return mainOutputPrinter;
	}
}
