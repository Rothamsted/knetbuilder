package net.sourceforge.ondex.scripting;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;



/**
 * 
 * @author lysenkoa
 *
 *	Holder class for registering information about the implementation tree that supports scripting functions and objects
 * */
public class ApplicationTreeBuilder {
	protected static final Set<String> OBJECTMETHODSET;
	protected final Map<String, Object> accessableObjects = new HashMap<String, Object>();
	protected final Map<Class<?>, Object> rootObjects = new HashMap<Class<?>, Object>();
	protected final Map<Class<?>, Method> stoMethods = new HashMap<Class<?>, Method> ();
	protected final List<ValueTupleInternal<String, Method>> functionMethods = new LinkedList<ValueTupleInternal<String, Method>>  ();
	protected final Map<Class<?>, Object[]> convinienceMap = new HashMap<Class<?>, Object[]> ();
	public ApplicationTreeBuilder(){}
	
	static{
		OBJECTMETHODSET = new HashSet<String>();
		for(Method m : Object.class.getMethods()){
			OBJECTMETHODSET.add(m.getName());
		}
	}	
	/**
	 * Get root object by class
	 * @param cls
	 * @return
	 */
	public Object getRoot(Class<?> cls){
		return rootObjects.get(cls);
	}
	
	/**
	 * Adds object to be referrable
	 * @param alias - reference name
	 * @param obj - objects to be available
	 */
	public void addAccessableObject(String alias, Object obj){
		accessableObjects.put(alias, obj);
	}
	
	/**
	 * 
	 * Returns all objects mapped to their references
	 * @return map 
	 */
	public Map<String, Object> getAccessableObject(){
		return accessableObjects;
	}
		
	/**
	 * Returns true if the class is part of the defined application tree
	 * @param cls class to resolve
	 * @return boolean
	 */
	public boolean isResolved(Class<?> cls){
		if(rootObjects.containsKey(cls))return true;
		if(stoMethods.containsKey(cls))return true;
		return false;
	}
	
	/**
	 * Returns true if the class defines an object that does not expire throughout the lifetime of the application
	 * 
	 * @param cls - class
	 * @return boolean
	 */
	public boolean isLongTerm(Class<?> cls){
		if(rootObjects.containsKey(cls))return true;
		return false;
	}
	
	/**
	 * Returns true if the class defines a transient object that is retrievable by the defined methods
	 * 
	 * @param cls - class
	 * @return boolean
	 */
	public boolean isShortTerm(Class<?> cls){
		if(stoMethods.containsKey(cls))return true;
		return false;
	}
	
	/**
	 * Add an array of root objects
	 * @param roots - array
	 */
	public void addRoots(Object [] roots){
		for(Object object: roots)addRoot(object);
	}
	
	/**
	 * Add one root object
	 * @param object
	 */
	public void addRoot(Object object){
		rootObjects.put(object.getClass(), object);
	}
	
	/**
	 * Add root object as one of its implemented classes, rather than a top-level class
	 * 
	 * @param cls - one of the classes that object implements
	 * @param object - root object to add
	 */
	public void addRoot(Class<?> cls, Object object){
		rootObjects.put(cls, object);
	}
	
	/**
	 * add root object with all of its implemetned classes and interfaces
	 * @param object  - root object to add
	 */
	public void addDeepRoot(Object object){
		addRoot(object);
		for(Class<?> cls : object.getClass().getInterfaces())addRoot(cls, object);
		Class<?> temp = object.getClass().getSuperclass();
		while(temp != Object.class){
			addRoot(temp, object);
			temp = temp.getClass().getSuperclass();
		}
	}
	
	/**
	 * Add method that resolves an application component that is required by the exposed objects
	 * @param m - method to add
	 */
	public void addRootMethod(Method m){
		stoMethods.put(m.getReturnType(), m);
	}
	
	/**
	 * Add method that resolves an application component that is required by the exposed objects
	 * @param cls - class or interface implemented by the returned object
	 * @param m - method to add
	 */
	public void addRootMethod(Class<?> cls, Method m){
		stoMethods.put(cls, m);
	}
	
	/**
	 * Add method that resolves an application component that is required by the
	 * exposed objects with complete inheritance tree 
	 * @param m - method to add
	 */
	public void addDeepRootMethod( Method m){
		stoMethods.put(m.getReturnType(), m);
		for(Class<?> cls : m.getReturnType().getClass().getInterfaces())stoMethods.put(cls, m);
		Class<?> temp = m.getReturnType().getClass().getSuperclass();
		while(temp != Object.class){
			stoMethods.put(temp, m);
			temp = temp.getClass().getSuperclass();
		}
	}
	
	/**
	 * Add method to be exposed
	 * 
	 * @param owner - owner class of the method
	 * @param name - name of the method
	 * @param functionName - name to be used for the function
	 */
	public void addFunctionMethodByName(Class<?> owner, String name, String functionName)throws Exception{
		if(functionName == null)functionName = name;
		Method method = getMethodByName(owner, name);
		if(method != null)functionMethods.add(new ValueTupleInternal<String, Method>(functionName, method));

	}
	
	/**
	 * Add an entire method content of the class as exposed methods using the same
	 * names as in the source class
	 * 
	 * @param owner - class with methods to be exposed
	 */
	public void addAllMethodsAsFunctions(Class<?> owner){
		try {
			Method [] methods = owner.getMethods();
			for(Method method:methods){
				if(!OBJECTMETHODSET.contains(method.getName())){
					functionMethods.add(new ValueTupleInternal<String, Method>(method.getName(), method));	
				}
			}
		} catch (SecurityException e) {e.printStackTrace();}
	}
	
	/**
	 * Convenience method to get a method from the class by providing just the name and class of the method
	 * @param owner -  owner class
	 * @param name - name of the method
	 * @return method
	 */
	public static Method getMethodByName(Class<?> owner, String name) throws Exception{
		try {
			Method [] methods = owner.getMethods();
			for(Method method:methods)if(method.getName().equals(name)){
				return method;
			}
		} catch (SecurityException e) {e.printStackTrace();}
		
		throw new Exception ("Could not find method "+name+ "in class "+owner.getName());
	}
	
	/**
	 * Clears the entire content of the builder and releases used memory and references.
	 */
	public void clear(){
		accessableObjects.clear();
		rootObjects.clear();
		stoMethods.clear();
		functionMethods.clear();
	}
	
	/**
	 * Auto converts the arguments for easier access from the scripting environment
	 * @param from what will be converted
	 * @param to to what it will be converted
	 * @param converter how to get there
	 */
	public void addConvinience(Class<?> realClass, Class<?> standInClass, Method converter){
		convinienceMap.put(realClass, new Object[]{standInClass, converter});
	}
	
	@SuppressWarnings("unchecked")
	public static final Method getMethod(Class cls, String name, Class[] args){
		java.lang.reflect.Method result = null;
		try {
			result = cls.getMethod(name, args);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static final Constructor getConstructor(Class cls, Class[] args){
		java.lang.reflect.Constructor result = null;
		try {
			result = cls.getConstructor(args);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return result;
	}
}
