package net.sourceforge.ondex.scripting;

import java.util.List;
/**
 * An interface used to automate the template resolution process, used to
 * resolve the dependencies of template builders and interpreters by 
 * the interpretation controller
 * @author lysenkoa
 *
 */
public interface ProxyTemplateDependant {
	/**
	 * 
	 * @param proxyTemplates that this class needs access to 
	 */
	public void initialize(ProxyTemplate ...proxyTemplates);
	/**
	 * 
	 * @return classes of proxyTemplates required
	 */	
	public List<Class<?>> getDependancies();
	/**
	 * Method to check whether this class has everything it 
	 * needs to work and was properly initialised
	 * @return
	 */
	public boolean isInitialised();
}
