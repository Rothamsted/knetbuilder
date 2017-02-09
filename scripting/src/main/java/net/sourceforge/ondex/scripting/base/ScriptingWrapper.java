package net.sourceforge.ondex.scripting.base;
/**
 * 
 * @author lysenkoa
 * 
 */
public interface ScriptingWrapper {
	public Object unwrap();
	public void wrap(Object o);
}
