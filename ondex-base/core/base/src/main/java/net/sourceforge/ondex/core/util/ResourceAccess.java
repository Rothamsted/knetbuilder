package net.sourceforge.ondex.core.util;

import java.util.Map;
/**
 * 
 * @author lysenkoa 
 *
 */
public interface ResourceAccess {
	/**
	 * 
	 * @return a map with names & resource handled by this  ResourceManager
	 */
	public Map<String, Object> getResources();
	
	/**
	 * Ensures that the resource is not in use by any other classes. All of the
	 * bindings on the resource will be released by calling this method.
	 * @param resourceName
	 */
	public void detachResource(String resourceName);
}
