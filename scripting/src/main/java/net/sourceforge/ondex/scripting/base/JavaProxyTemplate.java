package net.sourceforge.ondex.scripting.base;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import net.sourceforge.ondex.scripting.ProxyTemplate;


/**
 * 
 * @author lysenkoa
 *
 */
public interface JavaProxyTemplate extends ProxyTemplate {
	/**
	 * 
	 * @return wrapped methods 
	 */
	public abstract Method[] getShadowFunctions();
	/**
	 * 
	 * @return class files that contain wrapped functions
	 */
	public abstract Class<?>[] getClassesOfGlobalFunctions();
	/**
	 * 
	 * @return map of classes to their wrappers
	 */
	public abstract Map<Class<?>, Class<?>> getClsToWrapperMap();
	/**
	 * 
	 * @return list of wrapper classes
	 */
	public Collection<Class<?>> getWrapperClasses();
	
	/**
	 * 
	 * @return a set of internal method names, these methods are not supposed to be parsed to the scripting interface
	 */
	public Collection<String> getReservedMethodNames();

}