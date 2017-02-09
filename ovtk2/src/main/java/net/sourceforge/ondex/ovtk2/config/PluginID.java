package net.sourceforge.ondex.ovtk2.config;

/**
 * ID of a producer.
 * 
 * @author Jochen
 * 
 */
public class PluginID {
	private String packagename, classname;

	public PluginID(String pack, String clazz) {
		packagename = pack;
		classname = clazz;
	}

	public String getPackage() {
		return packagename;
	}

	public String getClassName() {
		return classname;
	}

	public String toString() {
		return packagename;
	}
}