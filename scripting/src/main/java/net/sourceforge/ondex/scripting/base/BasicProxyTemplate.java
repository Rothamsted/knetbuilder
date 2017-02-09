package net.sourceforge.ondex.scripting.base;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 
 * @author lysenkoa
 *	Trivial implementation of a ProxyTemplate
 */
public class BasicProxyTemplate implements JavaProxyTemplate {
	
	protected final Class<?>[] globalFunctionsClass;
	protected final Map<Class<?>, Class<?>> shadowClasses;
	protected final Set<String> reservedNames;
	
	public BasicProxyTemplate(Class<?>[] globalFunctionsClass, Map<Class<?>, Class<?>> shadowClasses, Set<String> reservedNames){
		this.globalFunctionsClass = globalFunctionsClass;
		this.shadowClasses = shadowClasses;
		this.reservedNames = reservedNames;
	}

	public Class<?>[] getClassesOfGlobalFunctions() {
		return globalFunctionsClass;
	}

	public Map<Class<?>, Class<?>> getClsToWrapperMap() {
		return shadowClasses;
	}

	@SuppressWarnings("unchecked")
	public Method[] getShadowFunctions() {
		List [] memberLists = new List[globalFunctionsClass.length];
		for(int i =0; i<globalFunctionsClass.length; i++) {
			Method [] members = globalFunctionsClass[i].getMethods();
			memberLists[i] = new ArrayList<Member>(members.length);
			for(Method member:members){
				if(!reservedNames.contains(member.getName())){
					memberLists[i].add(member);
				}
			}
			
		}
		int totalSize = 0;
		for(List<?> list : memberLists)totalSize = totalSize + list.size();
		Method[] result = new Method[totalSize];
		for(int i = 0; i < memberLists.length;i++){
			for(int j = 0; j < memberLists[i].size();j++){
				result[i+j] = (Method)memberLists[i].get(j);
			}
		}
		return result;
	}

	@Override
	public Collection<Class<?>> getWrapperClasses() {
		Set<Class<?>> result = new HashSet<Class<?>>();
		result.addAll(shadowClasses.values());
		return result;
	}

	@Override
	public Collection<String> getReservedMethodNames() {
		return this.reservedNames;
	}
}
