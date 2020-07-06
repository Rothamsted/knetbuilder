package net.sourceforge.ondex.tools.tab.importer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
/**
 * Defines the scope within the data. Each scope can contain zero or more sub-scopes,
 * @author lysenkoa
 *
 */
public class InputScope {
	private Object id = null;
	Map<Object, InputScope> idToScope = new HashMap<Object, InputScope>();
	/**
	 * A top level scope identifier
	 */
	public static final String TOP_LEVLE_SCOPE= "___Top_LEVEL___";
	
	protected static InputScope getTopLevelScope(){
		return new InputScope(TOP_LEVLE_SCOPE);
	}
	
	private InputScope(Object id){
		this.id = id;
	}
	
	/**
	 * 
	 * @return sub scopes of this scope
	 */
	public Collection<InputScope>getChildren(){
		return idToScope.values();
	}
	/**
	 * 
	 * @return id of this scope
	 */
	public Object getScopeId(){
		return id;
	}
	/**
	 * 
	 * @return adds a new sub scope
	 */
	public InputScope addChild(Object id){
		InputScope result = new InputScope(id);
		idToScope.put(id, result);	
		return result;
	}
	/**
	 * 
	 * Adds multiple sub scopes
	 */
	public Set<InputScope>  addChilden(Object ... ids){
		Set<InputScope> result = new HashSet<InputScope>();
		for(Object id:ids){
			result.add(this.addChild(id));	
		}
		return result;
	}
	/**
	 * 
	 * Remove a sub scope
	 */
	public InputScope removeChild(InputScope is){
		return idToScope.remove(is.getScopeId());	
	}
	/**
	 * 
	 * Remove a sub scope using id
	 */
	public InputScope removeChild(Object id){
		return idToScope.remove(id);	
	}
}
