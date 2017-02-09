package net.sourceforge.ondex.scripting.base;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.LoaderClassPath;

import net.sourceforge.ondex.scripting.ApplicationTreeBuilder;

import net.sourceforge.ondex.scripting.ProxyTemplate;
import net.sourceforge.ondex.scripting.ProxyTemplateBuilder;
import net.sourceforge.ondex.scripting.ProxyTemplateBuilderInteraface;
import net.sourceforge.ondex.scripting.ValueTupleInternal;
import net.sourceforge.ondex.scripting.ui.AutoDoc;


/**
 * @author lysenkoa
 * Builds the ProxyTemplate for the implementation-free access to the functionality of the application
 */
public class UniversalProxyTemplateBuilder extends ApplicationTreeBuilder implements UniversalConstants, ProxyTemplateBuilderInteraface<BasicProxyTemplate>{
	
	public static final Set<String> METHODS_OF_OBJECT;
	protected static final CtClass[] EMPTY_CT = new CtClass[0]; 
	protected Map<Class<?>, Class<?>> shadowClasses = new HashMap<Class<?>, Class<?>>();
	protected Map<String, ProxyTemplateBuilder> shadowTemplates = new HashMap<String, ProxyTemplateBuilder>();
	protected Map<Class<?>, String> wrapperNameLookup = new HashMap<Class<?>, String>();
	protected Map<Class<?>, String> classToFieldGlobal = new HashMap<Class<?>, String>();
	protected Map<Class<?>, String> classToFieldLocal = new HashMap<Class<?>, String>();
	protected Set<Class<?>> trueSources = new HashSet<Class<?>>();
	protected List<CtClass> ctClasses = new LinkedList<CtClass>();
	protected List<String> commonFileds = new LinkedList<String>();
	protected String baseMethod = null;
	protected List<Class<?>> libClasses = new LinkedList<Class<?>>();
	protected Class<?>[] globalFunctionsClasses;
	protected final ClassPool pool = ClassPool.getDefault();
	private static UniversalProxyTemplateBuilder instance = null;
	private boolean initialised = false;
	private BasicProxyTemplate content = null;
	
	static{
		METHODS_OF_OBJECT = new HashSet<String>();
		for(Method m : Object.class.getMethods()){
			METHODS_OF_OBJECT.add(m.getName());
		}
	}
	
	public static UniversalProxyTemplateBuilder getInstance(){
		if(instance == null)
			instance = new 	UniversalProxyTemplateBuilder();
		return instance;
	}
	
	private UniversalProxyTemplateBuilder() {
		super();
		pool.appendClassPath(new LoaderClassPath(this.getClass().getClassLoader()));
	}

	public void addCompatableLibraryClass(Class<?> c){
		libClasses.add(c);
	}

	public void addSClass(Class<?> baseClass, Class<?> wraperClass) {
		wrapperNameLookup.put(baseClass, wraperClass.getCanonicalName());
		shadowClasses.put(baseClass, wraperClass);
	}
	
	public void deepAddSClass(Class<?> baseClass, Class<?> wraperClass) {
		addSClass(baseClass, wraperClass);
		for(Class<?> cls : baseClass.getClass().getInterfaces())addSClass(cls, wraperClass);;
		Class<?> temp = baseClass.getClass().getSuperclass();
		while(temp != Object.class){
			addSClass(temp, wraperClass);
			temp = temp.getClass().getSuperclass();
		}
	}
	
	private void defineCommonFields(){
		int num = 0;
		StringBuffer carBld = new StringBuffer();
		carBld.append(APPBASE_MTHD);
		for(Entry<Class<?>, Object> ent : rootObjects.entrySet()){
			String name = "root"+num;
			commonFileds.add(PRIVATE_STATIC+ent.getKey().getCanonicalName()+SPACE+name+SC);
			classToFieldGlobal.put(ent.getKey(), name);
			carBld.append(name+ EQ+ BR_OPEN+ ent.getKey().getCanonicalName()+ BR_CLOSE+ MAP_GET_STR+ ent.getKey().getCanonicalName()+ MAP_GET_END);
		}
		carBld.append(CLS);
		baseMethod = carBld.toString();
	}
	
	private void parseGlobalFunctions() {
		if(baseMethod == null)defineCommonFields();
		List<Class<?>> functionClasses = new ArrayList<Class<?>>();
		CtClass globalFunctions;
		try {
			globalFunctions = pool.makeClass(GLOBAL_FUNCTIONS);
			for(String field : commonFileds){
				globalFunctions.addField(CtField.make(field, globalFunctions));	
			}
			globalFunctions.addMethod(CtMethod.make(baseMethod, globalFunctions));
			for(ValueTupleInternal<String, Method> e:functionMethods) {
				Class<?>[] argTypes = e.getValue().getParameterTypes();
				for(Class<?> argType: argTypes){
					if(convinienceMap.containsKey(argType)){
						makeMethod(globalFunctions, null, e.getKey(), e.getValue(), true, convinienceMap);
						break;
					}
				}
				makeMethod(globalFunctions, null, e.getKey(), e.getValue(), true, null);
			}

			Class<?> gf = globalFunctions.toClass();
			gf.getDeclaredMethod(SET_BASE, new Class<?>[]{Map.class}).invoke(null, new Object[]{rootObjects});
			functionClasses.add(gf);
		} catch (Exception e) {
			e.printStackTrace();
		}
		functionClasses.addAll(libClasses);
		globalFunctionsClasses = functionClasses.toArray(new Class<?>[functionClasses.size()]);
	}
	
	@SuppressWarnings("unchecked")
	private Class<?> makeSClass(ProxyTemplateBuilder st) {
		pool.appendClassPath(new ClassClassPath(st.getSource()));
		if(baseMethod == null)defineCommonFields();
		Map<Class<?>, Method> subsourceGetterMethods = st.getSubsourceGetterMethods();
		CtClass jsWrapper;
		try {
			jsWrapper = pool.get(st.getShadowName());
			for(String field : commonFileds){
				jsWrapper.addField(CtField.make(field, jsWrapper));	
			}
			int num = 0;
			StringBuffer temp = new StringBuffer();
			if(subsourceGetterMethods.size() > 0){
				for(Entry<Class<?>, Method> ent : subsourceGetterMethods.entrySet()){
					String name = "sub"+num;
					jsWrapper.addField(CtField.make(PRIVATE_STATIC+ent.getKey().getCanonicalName()+SPACE+name+SC, jsWrapper));
					classToFieldLocal.put(ent.getKey(), name);
					temp.append(name+ EQ+ BS+ DOT+ ent.getValue().getName()+BR_OPEN+BR_CLOSE+SC);
				}
				temp.append(CLS);
			}
			boolean addConvinience;
			
			List<ValueTupleInternal<String, Method>> [] all = new List[]{st.getFnMethods(), st.getSetMthds(), st.getGetMthds()};

			for(List<ValueTupleInternal<String, Method>> subset : all){
				for(ValueTupleInternal<String, Method> e:subset) {
					Class<?>[] argTypes = e.getValue().getParameterTypes();
					addConvinience = false;
					for(Class<?> argType: argTypes){
						//ensureComplience(argType);
						if(convinienceMap.containsKey(argType))
							addConvinience = true;
					}
					if(addConvinience){
						makeMethod(jsWrapper, st.getSource(),  e.getKey(), e.getValue(), false, convinienceMap);
					}
					makeMethod(jsWrapper, st.getSource(), e.getKey(), e.getValue(), false, null);
				}
			}
			
			for(Constructor<?> e:st.getConstructors()) {
				Class<?>[] argTypes = e.getParameterTypes();
				addConvinience = false;
				for(Class<?> argType: argTypes){
					//ensureComplience(argType);
					if(convinienceMap.containsKey(argType))
						addConvinience = true;
				}
				if(addConvinience){
					makeMethod(jsWrapper, st.getSource(), st.getShadowName(), e, false, convinienceMap);
				}
				makeMethod(jsWrapper, st.getSource(), st.getShadowName(), e, false, null);
			}

			for(String s:st.getCustomFnCode()){
				jsWrapper.addMethod(CtMethod.make(s, jsWrapper));
			}
			Class<?> gf = jsWrapper.toClass();
			//System.err.println(gf.getCanonicalName());
			classToFieldLocal.clear();
			return gf;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	private void addCnstr(CtClass ctCls, CtClass [] par,  String body){
		try{
			CtConstructor ctc = new CtConstructor(par, ctCls);
			if(body != null){
				ctc.setBody(body);
			}
			ctCls.addConstructor(ctc);	
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static String t1 (String ...strings){
		return null;
	}
	
	public static void t2 (String ...strings){
	}
	
	@SuppressWarnings({})
	private void makeMethod(final CtClass ct, final Class<?> parent, final String name, final Member member, boolean globalFunctionsMode, final Map<Class<?>, Object[]> cnvMap) throws CannotCompileException{
		StringBufferWrap cm = new StringBufferWrap();
		Class<?>[] parTypes = null;
		boolean isVarArg = false;
		Method method = null;
		Map<Class<?>, Method> parnetMethodReturns = new HashMap<Class<?>, Method>();
		if(parent != null){
			for(Method m :parent.getMethods()){
				if(m.getGenericParameterTypes().length==0 && m.getReturnType() != null){
					parnetMethodReturns.put(m.getReturnType(), m);
				}
			}	
		}
		Constructor constructor = null;
		if(member instanceof Method){
			method = (Method)member;
			isVarArg = method.isVarArgs();
			if(globalFunctionsMode){
				cm.append(PUBLIC_STATIC , getWrappedName(method.getReturnType()) , SPACE , name , BR_OPEN);
			}
			else{
				cm.append(PUBLIC , getWrappedName(method.getReturnType()) , SPACE , name , BR_OPEN);
			}
			parTypes = method.getParameterTypes();
		}
		else if(member instanceof Constructor){
			constructor = (Constructor)member;
			isVarArg = constructor.isVarArgs();
			cm.append(PUBLIC , SPACE , ct.getSimpleName() , BR_OPEN);
			parTypes = ((Constructor)member).getParameterTypes();
		}
		
		StringBufferWrap args1 = new StringBufferWrap();
		int functionArgCount = 0;
		final ResolutionTreeParser apt = new ResolutionTreeParser(ct, parent);
		if(cnvMap != null)
			apt.setConvMethods(cnvMap);
		String owner = OWNER;
		if(Modifier.isStatic(member.getModifiers())){
			owner = member.getDeclaringClass().getCanonicalName();
		}
		else if (isResolved(member.getDeclaringClass())) {
			owner = apt.addInternalArg(owner, member.getDeclaringClass());
		} 
		else if (globalFunctionsMode && cnvMap != null && cnvMap.containsKey(member.getDeclaringClass())) {
			apt.addConvinienceArg(owner, member.getDeclaringClass(), "cOwner", (Class<?>)cnvMap.get(member.getDeclaringClass())[0]);
		}
		else {
			owner = BS;
		}
		boolean throwsException = false;
		List<StringBufferWrap> arrayUnwrappers = new LinkedList<StringBufferWrap>();
		StringBufferWrap listMethodVersion = null;
		StringBufferWrap listToArray = null;
		if(isVarArg){
			listMethodVersion = new StringBufferWrap();
			listToArray = new StringBufferWrap();
		}
		for (int i = 0; i < parTypes.length; i++) {
			String argName = ARG + i;
			String wrapperName = null;
			String nameInsideMethod = null;
			if(parTypes[i].isArray()){
				wrapperName = wrapperNameLookup.get(parTypes[i].getComponentType());
				if(wrapperName != null){
					wrapperName= wrapperName+"[]";
					nameInsideMethod = "arr_"+argName;
					StringBufferWrap unwrapper = new StringBufferWrap();
					unwrapper.append(getClassName(parTypes[i])," ",nameInsideMethod," = new ",parTypes[i].getComponentType().getCanonicalName(),
						"[",argName,".length];\n");
					unwrapper.append("for(int i = 0; i< ",argName,".length; i++){\n"
						,nameInsideMethod,"[i] = ",argName,"[i].unwrap();\n"
						,"\n}");
					arrayUnwrappers.add(unwrapper);	
				}
				if(isVarArg && i == parTypes.length-1){
					wrapperName = wrapperNameLookup.get(parTypes[i].getComponentType());
					String nameInsideMethod1 = argName;
					if(wrapperName != null){
						wrapperName= wrapperName+"[]";
						nameInsideMethod1 = "arr_"+argName;
					}
					
					listToArray.append(getClassName(parTypes[i])," ",nameInsideMethod1," = new ",parTypes[i].getComponentType().getCanonicalName(),
						"[",argName,".size()];\n");
					listToArray.append("for(int i = 0; i< ",argName,".size(); i++){\n"
						,nameInsideMethod1,"[i] = (",parTypes[i].getComponentType().getCanonicalName(),")",argName,"get(i).unwrap();\n"
						,"\n}");
					listMethodVersion.append(cm.toString() , "java.util.List", SPACE , argName);			
				}
			}
			else{
				wrapperName = wrapperNameLookup.get(parTypes[i]);	
			}
			
			boolean implementConvinience = false;
			if (isResolved(parTypes[i])) {
				argName = apt.addInternalArg(argName, parTypes[i]);
				throwsException = true;
			}
			else if(cnvMap != null && cnvMap.containsKey(parTypes[i])){
				implementConvinience = true;
				final Object [] obj = cnvMap.get(parTypes[i]);
				apt.addConvinienceArg(argName, parTypes[i], "conv"+i, (Class<?>)obj[0]);
				cm.append(synElementAt(functionArgCount) + getClassName((Class<?>)obj[0]) + SPACE + "conv"+i);
				functionArgCount++;	
			}
			else if(wrapperName != null){
				cm.append(synElementAt(functionArgCount) , wrapperName, SPACE , argName);			
				functionArgCount++;		
			}
			else {
				cm.append(synElementAt(functionArgCount) , getClassName(parTypes[i]) , SPACE , argName);	
				functionArgCount++;
			}
			
			if(nameInsideMethod != null){
				argName = nameInsideMethod;
			}
			if(!isResolved(parTypes[i]) && wrapperName != null && !implementConvinience && !parTypes[i].isArray()){
				args1.append(synElementAt(i) ,BR_OPEN,getClassName(parTypes[i]),BR_CLOSE, WRP,argName,UNWRP_ARG);	
			}
			else{
				args1.append(synElementAt(i) , argName);
			}
			
		}
		
		if(throwsException){
			cm.append(BR_CLOSE,FNEX,M_START);
			cm.append(TRY_OPN,NL_TB);
		}
		else{
			cm.append(BR_CLOSE,M_START);
		}
		apt.addAppTreeResolution(cm);
		
		for(StringBufferWrap unwrapper :arrayUnwrappers){
			cm.append(unwrapper.toString());	
		}
			
		String cast = null;
		try{
			if(method != null){
				cast = wrapperNameLookup.get(method.getReturnType());
				if(cast != null){
					cm.append(cast," wrapped_result = new ",cast,"();");
					Set<Class<?>> embedded = shadowTemplates.get(cast).getEmbeddedClasses();
					if(embedded.size()>0){
						for(Class<?> cls : embedded){
							Method m = parnetMethodReturns.get(cls);
							if(m != null){
								cm.append("wrapped_result.setEmb",cls.getSimpleName(),"(this.baseObject.",m.getName(),"());");	
							}
							else if(shadowTemplates.get(wrapperNameLookup.get(parent)).getEmbeddedClasses().contains(cls)){
								cm.append("wrapped_result.setEmb",cls.getSimpleName(),"(this.getEmb",cls.getSimpleName(),"());");		
							}
						}
					}
					cm.append("wrapped_result.wrap(" ,owner , DOT , method.getName() , BR_OPEN, args1.toString(), BR_CLOSE,BR_CLOSE,";\n",RETURN,"wrapped_result",SC,CLS,NL_TB);
				}
				else if(method.getReturnType().isArray() && method.getReturnType().getComponentType().equals(Object.class)){
					// || this.wrapperNameLookup.get(method.getReturnType().getComponentType().getName()) != null
					//System.err.println(method.getDeclaringClass().getName()+" - "+method.getName());
					//net.sourceforge.ondex.scripting.base.UniversalProxyTemplateBuilder.wrapArray();
					cm.append(RETURN , " net.sourceforge.ondex.scripting.base.UniversalProxyTemplateBuilder.wrapArray(" ,owner , DOT , method.getName() , BR_OPEN, args1.toString(), "))", unwrapPrimitive(method.getReturnType()), ";}", NL_TB);	
				}
				else{
					cm.append(RETURN , owner , DOT , method.getName() , BR_OPEN, args1.toString(), ")", unwrapPrimitive(method.getReturnType()), ";}", NL_TB);	
				}
				if(throwsException){
					cm.append(TRY_CLS,QT,member.getName(),ERRMSG,BR_LN_M_END,NL_TB,CLS);	
				}
				try{
					CtMethod ctmethod = CtMethod.make(cm.toString(), ct);
					if(isVarArg){
						if(globalFunctionsMode){
							ctmethod.getMethodInfo().setAccessFlags(137);		
						}
						else{
							ctmethod.getMethodInfo().setAccessFlags(129);	
						}
					}
					ct.addMethod(ctmethod);
				}
				catch(Exception e){
					if(!(e instanceof javassist.bytecode.DuplicateMemberException)){
						if(parent != null)
							System.err.println(parent.getCanonicalName());
						System.err.println(cm.toString());
						e.printStackTrace();
					}
				}
			}
			else {
				cm.append(owner , " = new ",  member.getDeclaringClass().getCanonicalName() , BR_OPEN, args1.toString() , BR_LN_M_END , NL_TB);
				if(throwsException){
					cm.append(TRY_CLS,QT,member.getName(),ERRMSG,BR_LN_M_END,NL_TB,CLS);	
				}
				try{
					CtConstructor ctor = CtNewConstructor.make(cm.toString(), ct);
					if(isVarArg){
						ctor.getMethodInfo().setAccessFlags(129);	
					}
					ct.addConstructor(ctor);
				}
				catch(Exception e){
					if(!(e instanceof javassist.bytecode.DuplicateMemberException)){
						e.printStackTrace();	
					}
				}
			}
		}
		catch(final Exception e){
			e.printStackTrace();
			//System.err.println(e.getMessage()+" -- "+cm.getString());
		}

	}
	
	private String unwrapPrimitive(Class<?> cls){
		if(cls.equals(Integer.class))
			return ".intValue()";
		else if(cls.equals(Boolean.class))
			return ".booleanValue()";
		else if(cls.equals(Double.class))
			return ".doubleValue()";
		return "";
	}

	private String getClassName(Class<?> cls){
		String append = " []";
		if(cls.isArray()){
			Class<?> component = cls.getComponentType();
			while(component.isArray()){
				append = append +"[]";
				component = component.getComponentType();
			}
			return component.getCanonicalName()+append;
		}
		return cls.getCanonicalName();
	}
	
	private String getWrappedName(Class<?> cls){
		String wrapped = null;
		if(cls.equals(Integer.class))
			return "int";
		else if(cls.equals(Boolean.class))
			return "boolean";
		else if(cls.equals(Double.class))
			return "double";
		String append = " []";
		if(cls.isArray()){
			Class<?> component = cls.getComponentType();
			while(component.isArray()){
				append = append +"[]";
				component = component.getComponentType();
			}
			wrapped = wrapperNameLookup.get(component);
			if(wrapped != null)
				return wrapped+append;
			else if(component.equals(int.class))
				return "int "+append;
			else if(component.equals(boolean.class))
				return "boolean "+append;
			else if(component.equals(double.class))
				return "double "+append;
			else if(component.equals(float.class))
				return "float "+append;
			else if(component.equals(long.class))
				return "long "+append;
			else if(component.equals(char.class))
				return "char "+append;
			return component.getCanonicalName()+append;
		}
		wrapped = wrapperNameLookup.get(cls);
		if(wrapped != null){
			return wrapped;
		}
		return cls.getCanonicalName();	
	}

	private String synElementAt(int pos) {
		String result = EMPTY;
		if (pos != 0)
			result = NEXT;
		return result;
	}
	
	public BasicProxyTemplate getProxyTemplate() {
		if(content != null)
			return content;
		for(ProxyTemplateBuilder wt:shadowTemplates.values()){
			trueSources.addAll(wt.getAllSources());
			for(Class<?> cls : wt.getAllSources()){
				wrapperNameLookup.put(cls, wt.getShadowName());
			}
			try {
				CtClass tmp = pool.makeClass(wt.getShadowName());
				ctClasses.add(tmp);
				tmp.addInterface(pool.get(WRAPPER));
				tmp.addField(CtField.make(PROTECTED+wt.getSource().getCanonicalName()+BS+SC, tmp));
				tmp.addMethod(CtMethod.make(UNWRP_METHOD, tmp));
				tmp.addMethod(CtMethod.make(WRP_METHOD1+wt.getSource().getCanonicalName()+WRP_METHOD2, tmp));
				if(baseMethod == null)
					defineCommonFields();
				tmp.addMethod(CtMethod.make(baseMethod, tmp));
				tmp.addMethod(CtMethod.make(MTH_NM1+wt.getShadowName()+MTH_NM2, tmp));
				for(Class<?> emb: wt.getEmbeddedClasses()){
					tmp.addField(CtField.make("private "+emb.getCanonicalName()+" emb"+emb.getSimpleName()+";", tmp));
					CtMethod ctmethod = CtMethod.make("public "+emb.getCanonicalName()+" getEmb"+emb.getSimpleName()+"(){return  emb"+emb.getSimpleName()+";}", tmp);
					tmp.addMethod(ctmethod);
					ctmethod = CtMethod.make("public void setEmb"+emb.getSimpleName()+"("+emb.getCanonicalName()+" emb"+emb.getSimpleName()+"){this.emb"+emb.getSimpleName()+"=emb"+emb.getSimpleName()+";}", tmp);
					tmp.addMethod(ctmethod);
				}
				try{
					Constructor<?> ctor = wt.getSource().getConstructor(new Class[0]);
					makeMethod(tmp, wt.getSource(), null, ctor, false, null);
				}
				catch(Exception e){
					addCnstr(tmp, EMPTY_CT, NO_BODY);	
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			//createBasicWrapper(wt.getShadowName(), wt.getSource().getCanonicalName());
		}
		List<Class<?>> classes = new ArrayList<Class<?>>(shadowTemplates.size()+shadowClasses.size());
		
		for(ProxyTemplateBuilder wt:shadowTemplates.values()){
			Class<?> sc = makeSClass(wt);
			classes.add(sc);
			for(Class<?> cls : wt.getAllSources()){
				shadowClasses.put(cls, sc);
			}
		}

		this.parseGlobalFunctions();
		try {
			for(Class<?> cls:classes)
				cls.getDeclaredMethod(SET_BASE, new Class<?>[]{Map.class}).invoke(null, new Object[]{rootObjects});
		} catch (Exception e){
			e.printStackTrace();
		} 

		Set<String> reservedNames = new HashSet<String>();
		reservedNames.addAll(METHODS_OF_OBJECT);
		reservedNames.add(SET_BASE);
		content = new BasicProxyTemplate(globalFunctionsClasses, shadowClasses, reservedNames);
		return content;
	}
	
	/*
	private void createBasicWrapper(String wrapperName, String sourceName){
		try {
			CtClass tmp = pool.makeClass(wrapperName);
			ctClasses.add(tmp);
			addCnstr(tmp, EMPTY_CT, NO_BODY);
			tmp.addInterface(pool.get(WRAPPER));
			tmp.addField(CtField.make(PROTECTED+sourceName+BS+SC, tmp));
			tmp.addMethod(CtMethod.make(UNWRP_METHOD, tmp));
			tmp.addMethod(CtMethod.make(WRP_METHOD1+sourceName+WRP_METHOD2, tmp));
			if(baseMethod == null)
				defineCommonFields();
			tmp.addMethod(CtMethod.make(baseMethod, tmp));
			tmp.addMethod(CtMethod.make(MTH_NM1+wrapperName+MTH_NM2, tmp));
		} catch (Exception e) {e.printStackTrace();}	
	}
	*/

	/**
	 * 
	 * @author lysenkoa
	 * generates solution for finding necessary components that are required by the exposed 
	 * methods  and functions
	 */
	class ResolutionTreeParser {
		protected Map<Class<?>, String> classToArgs = new HashMap<Class<?>, String>();
		protected Map<String, Class<?>> argToClass = new HashMap<String, Class<?>>();
		protected Map<String, List<Class<?>>> argToDependancies = new HashMap<String, List<Class<?>>>(15);
		protected Set<Class<?>> allDepTypes = new HashSet<Class<?>>();
		protected Map<Class<?>, String> cashed = new HashMap<Class<?>, String>();
		protected Map<Class<?>, Object[]> convMethods = new HashMap<Class<?>, Object[]> ();
		protected Map<Class<?>, List<String>> convTypeToNames = new HashMap<Class<?>, List<String>> (5, 1);
		protected Map<String, String> namePassedOnToNameFromOutside = new HashMap<String, String>(5, 1);
		protected int cashedVarCounter = 0;
		protected final CtClass proxy;
		protected final Class<?> parent;
		protected final Map<Class<?>, Method> parnetMethodReturns = new HashMap<Class<?>, Method>();

		protected ResolutionTreeParser(CtClass proxy, Class<?> parent) {
			this.proxy = proxy;
			this.parent = parent;
			if(parent != null){
				for(Method m :parent.getMethods()){
					if(m.getGenericParameterTypes().length==0 && m.getReturnType() != null){
						parnetMethodReturns.put(m.getReturnType(), m);
					}
				}	
			}
		}
		
		protected void setListOfConvinienceMethods(Map<Class<?>, Object[]> convMethods){
			this.convMethods = convMethods;
		}
		
		protected String addInternalArg(String name, Class<?> cls) {
			if (classToArgs.containsKey(cls)) {
				return classToArgs.get(cls);
			}
			classToArgs.put(cls, name);
			return name;
		}
		
		protected void addConvinienceArg(String internalName, Class<?> internalClass, String externalName, Class<?> externalClass){
			namePassedOnToNameFromOutside.put(internalName, externalName);
			List<String> names = convTypeToNames.get(internalClass);
			if(names == null){
				names = new ArrayList<String>();
				convTypeToNames.put(internalClass, names);
			}
			names.add(internalName);
			argToClass.put(externalName, externalClass);
			addInternalArg(internalName, internalClass);
		}
		
		protected void setConvMethods(Map<Class<?>, Object[]> convMethods){
			this.convMethods  = convMethods;
		}

		/**
		 * Orders the resolution call based on their interdepndence and and adds them to merger. 
		 * @param cmRef
		 */
		protected void addAppTreeResolution(StringBufferWrap cmRef) {
			if (classToArgs.size() == 0)return;
			Map<String, List<String>> argToCashes = new HashMap<String, List<String>>(5);
			List<String> argsInOrder = new ArrayList<String>();
			cashedVarCounter = 0;
			for(Entry<Class<?>, String> ent : classToArgs.entrySet()) {
				addVar(ent.getValue(), ent.getKey());
			}
			allDepTypes.clear();
			classToArgs.clear();
			
			for (Entry<String, List<Class<?>>> ent: argToDependancies.entrySet()) {
				List<String> cashes = new ArrayList<String>();
				for (Class<?> cls : ent.getValue()) {
					String casheName = cashed.get(cls);
					if (casheName != null)
						cashes.add(casheName);
				}
				argToCashes.put(ent.getKey(), cashes);
			}
			argToDependancies.clear();

			Set<String> varsToResolve = new HashSet<String>(argToCashes.keySet());
			while(varsToResolve.size() >0){
				for (Entry<String, List<String>> ent: argToCashes.entrySet()) {
					boolean addToList = true;
					String s = ent.getKey();
					if (ent.getValue().size() > 0) {
						for (String arg : ent.getValue()) {
							if (varsToResolve.contains(arg)) {
								addToList = false;
								break;
							}
						}
					}
					if (varsToResolve.contains(s) && addToList) {
						argsInOrder.add(s);
						varsToResolve.remove(s);
					}
				}	
			}
			argToCashes.clear();
			for (String arg : argsInOrder) {
				Class<?> varCls = argToClass.get(arg);
				List<String> temp = convTypeToNames.get(varCls);
				if(temp != null){
					for(String argName: temp){
						cmRef.append(varCls.getCanonicalName() , SPACE , argName , EQ);
						addArgResolution(argName, varCls, cmRef, namePassedOnToNameFromOutside.get(argName));
						cmRef.append(LN_END);		
					}
				}
				else{
					cmRef.append(varCls.getCanonicalName() , SPACE , arg , EQ);
					addArgResolution(arg, varCls, cmRef, null);
					cmRef.append(LN_END);	
				}
			}
		}

		private void findDependancies(Class<?> argClass, String argName, List<Class<?>> resolvedSoFar, boolean topLevel) {
			if(!isResolved(argClass) && !convMethods.containsKey(argClass))
				return;
			if (!topLevel)
				resolvedSoFar.add(argClass);
			if (classToArgs.containsKey(argClass) && !argName.equals(classToArgs.get(argClass)))
				return;
			if (isLongTerm(argClass))
				return;
			if(!argName.startsWith(CASHE)){
				if (allDepTypes.contains(argClass)) {
					if (cashed.containsKey(argClass)) {
						return;
					}
					addVar(CASHE + Integer.toString(cashedVarCounter++), argClass);
					return;
				}
				allDepTypes.add(argClass);
			}
			
			Method m = stoMethods.get(argClass);
			if(m == null && convMethods.containsKey(argClass)){
				m = (Method)convMethods.get(argClass)[1];
			}
			else if(m == null){
				return;
			}
			if(m == null){
				System.err.println("Did not find method for "+argClass);
			}
			if (!Modifier.isStatic(m.getModifiers())) {
				findDependancies(m.getDeclaringClass(), argName, resolvedSoFar, false);
			}	


			for (Class<?> argType : m.getParameterTypes()) {
				findDependancies(argType, argName, resolvedSoFar, false);
			}
		}

		private void addVar(String name, Class<?> cls) {
			List<Class<?>> dependancies = new ArrayList<Class<?>>();
			cashed.put(cls, name);
			argToClass.put(name, cls);
			findDependancies(cls, name, dependancies, true);
			argToDependancies.put(name, dependancies);
		}

		private void addArgResolution(String name, Class<?> cls, StringBufferWrap cmRef, String passedArg) {
			if(parent != null && parent.equals(cls)){
				cmRef.append("((",parent.getCanonicalName(),")this.baseObject)");
				return ;
			}
			if(argToClass.containsKey(passedArg) && argToClass.get(passedArg).equals(cls)){
				cmRef.append(passedArg);
				return ;
			}
			if(parent != null && !convinienceMap.containsKey(cls) && parnetMethodReturns.containsKey(cls)){
				cmRef.append("((",parent.getCanonicalName(),")this.baseObject).",parnetMethodReturns.get(cls).getName(),"()");
				return ;
			}
			if(parent != null && shadowTemplates.get(wrapperNameLookup.get(parent)).getEmbeddedClasses().contains(cls)){
				cmRef.append("this.getEmb",cls.getSimpleName(),"()");
				return;
			}
			if (classToFieldLocal.containsKey(cls)){
				cmRef.append(classToFieldLocal.get(cls));
				return;	
			}
			if (classToFieldGlobal.containsKey(cls)){
				cmRef.append(classToFieldGlobal.get(cls));
				return;	
			}
			String casheName = cashed.get(cls);
			if (casheName != null && !name.equals(casheName)){
				cmRef.append(casheName);
				return;	
			}	
			Method m = stoMethods.get(cls);
			if(m == null){
				Object[] o = convMethods.get(cls);
				if(o != null)m = (Method)o[1];
			}
			if (m != null) {
				Class<?> dectl = m.getDeclaringClass();
				if (Modifier.isStatic(m.getModifiers())) {
					cmRef.append(dectl.getCanonicalName());
				} else {
					addArgResolution(name, dectl, cmRef, passedArg);
				}
				cmRef.append(DOT , m.getName() , BR_OPEN);
				Class<?>[] argTypes = m.getParameterTypes();
				if (argTypes.length > 0) {
					for (int i = 0; i < argTypes.length; i++) {
						cmRef.append(synElementAt(i));
						addArgResolution(name, argTypes[i], cmRef, passedArg);
					}
				}
				cmRef.append(BR_CLOSE);
				
				return;
			}
		}
	}
	
	private class WrapperSpawner extends ProxyTemplateBuilder{
		protected WrapperSpawner(String shadowName, Class<?> source, boolean autoProcess,  Map<Class<?>, Object> _rootObjects, Map<Class<?>, Method> _stoMethods) {
			super(shadowName, source, autoProcess, _rootObjects, _stoMethods, WRAPPER_PACKAGE);
		}
	}
	
	/*private void ensureComplience(Class<?> cls){
		if(cls.isPrimitive() ||isResolved(cls) || wrapperNameLookup.containsKey(cls) || cls.getCanonicalName().startsWith("java.lang")){
			return;
		}
		createBasicWrapper(WRAPPER_PACKAGE+"."+cls.getSimpleName(), cls.getCanonicalName());
		wrapperNameLookup.put(cls, WRAPPER_PACKAGE+"."+cls.getSimpleName());
	}*/

	public void removeSClass(Class<?> baseClass) {
		shadowClasses.remove(baseClass);
	}

	public ProxyTemplateBuilder spawnTemplate(String shadowName, Class<?> source, boolean autoProcess) {
		ProxyTemplateBuilder st = new WrapperSpawner(shadowName, source, autoProcess, rootObjects, stoMethods);
		shadowTemplates.put(st.getShadowName(), st);
		return st;
	}
	
	/**
	 * Getting ProxyTemplate via this method will generate the bare-bones
	 * doc file for all features currently available via the scripting interface.
	 * Creates the Scripting_ref.htm file in the application folder if one is not present already.
	 * This file is required to access help information from scripting command line
	 * @param generateDocumentation
	 * @return ProxyTemplate constructed, 
	 */
	@SuppressWarnings("unchecked")
	public BasicProxyTemplate getTemplateWithDoc() {
		BasicProxyTemplate result;
		Map<Class<?>,Map<String, String>[]> docMap = new HashMap<Class<?>,Map<String, String>[]>();
		for(Entry<Class<?>, Class<?>>ent : shadowClasses.entrySet()){
			Map<String, String> properties = new HashMap<String, String>();
			Map<String, String> functions = new HashMap<String, String>();
			Method[] methods = ent.getValue().getDeclaredMethods();
			for(Method method : methods){
				String name = method.getName().substring(11);
				String methodStr = method.toString();
				if(method.getReturnType().toString().equals("void")){
					if(method.getExceptionTypes().length > 0){
						functions.put(name+methodStr.substring(methodStr.indexOf(method.getName())+method.getName().length(), methodStr.indexOf("throws")), "Backed by: N/A");	
					}
					else{
						functions.put(name+methodStr.substring(methodStr.indexOf(method.getName())+method.getName().length()), "Backed by:");	
					}
				}
				else{
					if(method.getExceptionTypes().length > 0){
						functions.put(method.getReturnType().getCanonicalName()+" "+name+methodStr.substring(methodStr.indexOf(method.getName())+method.getName().length(), methodStr.indexOf("throws")), "Backed by: N/A");	
					}
					else{
						functions.put(method.getReturnType().getCanonicalName()+" "+name+methodStr.substring(methodStr.indexOf(method.getName())+method.getName().length()), "Backed by: N/A");	
					}

				}
			}
			docMap.put(ent.getKey(), new Map[]{properties, functions});
		}
		result = getProxyTemplate();
		Map<Class<?>,ProxyTemplateBuilder> templateLookup = new HashMap<Class<?>,ProxyTemplateBuilder>(shadowTemplates.size(), 1);
		Map<Class<?>, Class<?>> baseClsToWrapperCls = new HashMap<Class<?>, Class<?>>();
		for(ProxyTemplateBuilder t :shadowTemplates.values()){
			templateLookup.put(t.getSource(), t);
			baseClsToWrapperCls.put(t.getSource(), shadowClasses.get(t.getSource()));
		}
		for(Entry<Class<?>, Class<?>>ent : shadowClasses.entrySet()){
			Map<String, String> properties = new HashMap<String, String>();
			Map<String, String> functions = new HashMap<String, String>();
			Method[] methods = ent.getValue().getDeclaredMethods();
			ProxyTemplateBuilder template = templateLookup.get(ent.getKey());
			for(Method method : methods){
				String name = method.getName();
				String methodStr = method.toString();
				String functionDesc;
				if(method.getExceptionTypes().length > 0){
					functionDesc = (method.getReturnType().getCanonicalName()+" "+name+methodStr.substring(methodStr.indexOf(method.getName())+method.getName().length(), methodStr.indexOf("throws")));	
				}
				else{
					functionDesc = (method.getReturnType().getCanonicalName()+" "+name+methodStr.substring(methodStr.indexOf(method.getName())+method.getName().length()));	
				}
				String backerStr = "";
				if(template != null){
					Method backer = template.getFnLookup().get(name);
					if(backer != null){
						backerStr = ("Backed by: "+backer.toString()+" \n");	
					}
				}
				functions.put(functionDesc, backerStr);
			}
			docMap.put(ent.getKey(), new Map[]{properties, functions});
		}
		
		AutoDoc.begin();
		
		// table of contents
		AutoDoc.addSectionLink("Global functions");
		for(Entry<Class<?>, Class<?>> ent: baseClsToWrapperCls.entrySet()){
			AutoDoc.addSectionLink(ent.getValue().getSimpleName());
		}

		AutoDoc.addMajorHeader("Global functions");
		for(ValueTupleInternal<String, Method> ent:functionMethods){
			for(Method m: getMethodsByName(globalFunctionsClasses[0], ent.getKey())){
				String methodStr = m.toString();
				if(m.getExceptionTypes().length > 0){
					AutoDoc.addMajorSubitemType1(m.getReturnType().getCanonicalName()+" \n"+methodStr.substring(methodStr.indexOf(m.getName()), methodStr.indexOf("throws")), "Backed by: \n"+ent.getValue().toString());	
				}
				else{
					AutoDoc.addMajorSubitemType1(m.getReturnType().getCanonicalName()+" \n"+methodStr.substring(methodStr.indexOf(m.getName())), "Backed by: \n"+ent.getValue().toString());	
				}
			}
		}
		AutoDoc.addMajorHeader("Data structures");
		for(Entry<Class<?>, Class<?>> ent: baseClsToWrapperCls.entrySet()){
			AutoDoc.addMajorSubitemType1(ent.getValue().getSimpleName(), "Backed by: \n"+ent.getKey().getCanonicalName());
			if(docMap.get(ent.getKey())[0].size() > 0){
				AutoDoc.addMinorHeader("Properties");
				for(Entry<String, String> info :docMap.get(ent.getKey())[0].entrySet()){
					AutoDoc.addMinorSubitem(info.getKey(), info.getValue());	
				}
			}
			if(docMap.get(ent.getKey())[1].size() > 0){
				AutoDoc.addMinorHeader("Methods");
				for(Entry<String, String> info :docMap.get(ent.getKey())[1].entrySet()){
					AutoDoc.addMinorSubitem(info.getKey(), info.getValue());	
				}	
			}
		}
		AutoDoc.end();
		return result;
	}
	
	private static List<Method> getMethodsByName(Class<?>cls, String name){
		List<Method> result = new ArrayList<Method>();
		for(Method m : cls.getDeclaredMethods()){
			if(m.getName().equals(name))result.add(m);
		}
		return result;
	}

	public Class<BasicProxyTemplate> getProxyTemplateType() {
		return BasicProxyTemplate.class;
	}

	@Override
	public List<Class<?>> getDependancies() {
		return Arrays.asList(new Class<?>[0]);
	}


	public void initialize(ProxyTemplate... proxyTemplates) {
		initialised = true;
	}

	@Override
	public boolean isInitialised() {
		return initialised;
	}
	//net.sourceforge.ondex.scripting.base.UniversalProxyTemplateBuilder.wrapArray();
	public static Object [] wrapArray(final Object [] input) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		final Object [] result = new Object[input.length];
		final UniversalProxyTemplateBuilder up = getInstance();
		for(int i = 0; i < input.length; i++){
			if(input[i] == null){
				result[i] = null;
				continue;
			}
			final String s = up.wrapperNameLookup.get(input[0].getClass());
			if(s == null){
				result[i] = input[i];
				continue;
			}
			final Class<?> wrapperCls = Class.forName(s);
			final ScriptingWrapper inst = (ScriptingWrapper)wrapperCls.newInstance();
			inst.wrap(input[i]);
			result[i] = inst;
		}
		return result;
	}
}
