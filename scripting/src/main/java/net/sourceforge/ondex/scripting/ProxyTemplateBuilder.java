package net.sourceforge.ondex.scripting;

import net.sourceforge.ondex.core.util.BitSetFunctions;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Template of rules for building wrappers
 * @author lysenkoa
 *
 */
public class ProxyTemplateBuilder {
	//private final Map<Class<?>, Object[]> convinienceMap = new HashMap<Class<?>, Object[]> ();
	protected static final Set<String> OBJECTMETHODSET;
	protected final Class<?> primarySource;
	protected final Set<Class<?>> allSources = new HashSet<Class<?>>();
	protected final String shadowName;
	protected final Map<Class<?>, Method> subsourceGetterMethods = new HashMap<Class<?>, Method>();
	protected final List<ValueTupleInternal<String, Method>> fnMethods = new LinkedList<ValueTupleInternal<String, Method>>();
	protected final List<ValueTupleInternal<String, Method>> getters = new LinkedList<ValueTupleInternal<String, Method>>();
	protected final List<ValueTupleInternal<String, Method>> setters = new LinkedList<ValueTupleInternal<String, Method>>();
	protected final Map<String, Method> getterLookup = new HashMap<String, Method>();
	protected final Map<String, Method> settersLookup = new HashMap<String, Method>();
	protected final Map<String, Method> fnLookup = new HashMap<String, Method>();
	protected final Set<Constructor<?>> ctorLookup = new HashSet<Constructor<?>>();
	protected final List<String> customFunctions = new ArrayList<String>();
	protected final Set<Method> methodsToProcess = new HashSet<Method>();
	protected final Map<Class<?>, Object> rootObjects;
	protected final Map<Class<?>, Method> stoMethods;
	protected final String packageName;
	protected boolean isInitializedFlag = false;
	protected Set<Class<?>> toEmbed = new HashSet<Class<?>>(); 
	
	static{
		Set<String> temp = new HashSet<String>();
		for(Method m:Object.class.getMethods()){
			temp.add(m.getName());
		}
		OBJECTMETHODSET = BitSetFunctions.unmodifiableSet(temp);
		
	}
	/**
	 * 
	 * @param shadowName -  name alias that wrapper will have
	 * @param source - source class
	 * @param autoProcess - automatically generate that wrapper using default rules
	 */
	protected ProxyTemplateBuilder(String shadowName, Class<?> source, boolean autoProcess, Map<Class<?>, Object> rootObjects, Map<Class<?>, Method> stoMethods, String packageName){
		this.primarySource = source;
		this.rootObjects = rootObjects;
		this.stoMethods = stoMethods;
		allSources.add(source);
		this.packageName = packageName;
		if(this.packageName != null){
			this.shadowName = packageName+"."+shadowName;
		}
		else{
			this.shadowName = shadowName;
		}
		if(autoProcess)autoAddExclusive(OBJECTMETHODSET);
	}
	
	public void deepAddSource(Class<?> baseClass, ProxyTemplateBuilder st) {
		allSources.add(baseClass);
		for(Class<?> cls : baseClass.getClass().getInterfaces())allSources.add(cls);;
		Class<?> temp = baseClass.getClass().getSuperclass();
		while(temp != Object.class){
			allSources.add(temp);
			temp = temp.getClass().getSuperclass();
		}
	}
	
	public void removeSources(Class<?> baseClass) {
		allSources.remove(baseClass);
	}
	
	public void addEmbeddedClass(Class<?> cls){
		toEmbed.add(cls);
	}
	
	public Set<Class<?>> getEmbeddedClasses(){
		return toEmbed;
	}
	
	public void addSource(Class<?> source){
		allSources.add(source);
	}
	
	public Set<Class<?>> getAllSources(){
		return allSources;
	}
	/**
	 * Add custom method
	 * @param code method content
	 */
	public void addCustomFunction(String code){
		customFunctions.add(code);
	}
	/**
	 * 
	 * @param shadowFieldName - filed name
	 * @param getter - getter for field
	 * @param setter - setter for field
	 */
	public void addField(Class<?> source, String shadowFieldName, String getter, String setter){
		if(getter != null){
			Method mGetter = getMethodByName(source, getter);
			getters.add(new ValueTupleInternal<String, Method>(shadowFieldName, mGetter));
			getterLookup.put(shadowFieldName, mGetter);
		}
		if(setter != null){
			Method mSetter = getMethodByName(source, setter);
			setters.add(new ValueTupleInternal<String, Method>(shadowFieldName, mSetter));
		}
	}

	public void addSubSource(Class<?>subSource, Method getter){
		subsourceGetterMethods.put(subSource, getter);	
	}
	/*
	public List<ValueTuple<String, Method>> getGetMthds(){
		if(!isInitializedFlag)initialize();
		return getters;
	}

	public List<ValueTuple<String, Method>> getSetMthds(){
		if(!isInitializedFlag)initialize();
		return setters;
	}

	public List<ValueTuple<String, Method>> getFnMethods(){
		if(!isInitializedFlag)initialize();
		return fnMethods;
	}*/
	
	public List<ValueTupleInternal<String, Method>> getGetMthds(){
		if(!isInitializedFlag)initialize();
		return getters;
	}

	public List<ValueTupleInternal<String, Method>> getSetMthds(){
		if(!isInitializedFlag)initialize();
		return setters;
	}

	public List<ValueTupleInternal<String, Method>> getFnMethods(){
		if(!isInitializedFlag)initialize();
		return fnMethods;
	}
	
	public Set<Constructor<?>> getConstructors(){
		if(!isInitializedFlag)initialize();
		return ctorLookup;
	}
	
	public List<String> getCustomFnCode(){
		return customFunctions;
	}
	/**
	 * auto add all excluding the ones on the list
	 * and methods of Object
	 * @param filedsToExclude - list to exclude
	 */
	public void autoAddExclusive(Collection<String> toExclude){
		Method [] methods = primarySource.getMethods();
		for(Method method:methods){
			if(!toExclude.contains(method.getName()) &&  !OBJECTMETHODSET.contains(method.getName())){
				methodsToProcess.add(method);
			}
		}
	}
	
	/**
	 * auto add all excluding the ones on the list
	 * and methods of Object
	 * @param filedsToAdd - list to include
	 */
	public void autoAddInclusive(Collection<String> toInclude){
		Method [] methods = primarySource.getMethods();
		for(Method method:methods){
			if(toInclude.contains(method.getName()) &&  !OBJECTMETHODSET.contains(method.getName())){
				methodsToProcess.add(method);
			}
		}
	}
	
	protected void initialize(){
		for(Class<?> src : allSources){
			for(Constructor<?> ctor:src.getConstructors()){
				if(ctor.getParameterTypes().length > 0)
					ctorLookup.add(ctor);	
			}
		}
		for(Method method:methodsToProcess){
			if(method.getName().startsWith("get") && getTrueArgCount(method) == 0){
				getters.add(new ValueTupleInternal<String, Method>(method.getName(), method));
				getterLookup.put(method.getName(), method);
			}
			else if(method.getName().startsWith("set") && getTrueArgCount(method) == 1){
				setters.add(new ValueTupleInternal<String, Method>(method.getName(), method));
				settersLookup.put(method.getName(), method);
			}
			else{
				fnMethods.add(new ValueTupleInternal<String, Method>(method.getName(), method));
				fnLookup.put(method.getName(), method);
			}
		}
		isInitializedFlag = true;
	}
	
	protected int getTrueArgCount(Method m){
		int result = 0;
		for(Class<?> c: m.getParameterTypes()){
			if(!rootObjects.containsKey(c) && !stoMethods.containsKey(c)){
				result++;
			}
		}
		return result;
	}

	/**
	 * Add method under the name methodName
	 * @param functionName
	 * @param methodName
	 */
	public void addFunction(String functionName, String methodName){
		if(functionName == null)functionName = methodName;
		Method mFunction = getMethodByName(primarySource, methodName);
		if(mFunction != null){
			fnMethods.add(new ValueTupleInternal<String, Method>(functionName, mFunction));
		}
	}
	/**
	 * Convenience method to get method by name from the owner class
	 * @param owner
	 * @param name
	 * @return
	 */
	protected static Method getMethodByName(Class<?> owner, String name){
		try {
			Method [] methods = owner.getMethods();
			for(Method method:methods)if(method.getName().equals(name)){
				return method;
			}
		} catch (SecurityException e) {e.printStackTrace();}
		return null;
	}
	/**
	 * returns the source class of this template
	 * @return
	 */
	public Class<?> getSource() {
		return primarySource;
	}
	/**
	 * 
	 * returns alias to use 
	 * @return
	 */
	public String getShadowName() {
		return shadowName;
	}
	
	public Map<Class<?>, Method> getSubsourceGetterMethods() {
		return subsourceGetterMethods;
	}
	/**
	 * Auto converts the arguments for easier access from the scripting environment
	 * @param from what will be converted
	 * @param to to what it will be converted
	 * @param converter how to get there

	public void addConvinience(Class<?> from, Class<?> to, Method converter){
		convinienceMap.put(from, new Object[]{to, converter});
	}
	/**
	 * 
	 * @return

	public Map<Class<?>, Object[]> getConvinienceMap() {
		return convinienceMap;
	}
		*/

	public Map<String, Method> getGetterLookup() {
		return getterLookup;
	}

	public Map<String, Method> getSettersLookup() {
		return settersLookup;
	}

	public Map<String, Method> getFnLookup() {
		return fnLookup;
	}


}