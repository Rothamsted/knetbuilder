package net.sourceforge.ondex.scripting;

/**
 * 
 * @author lysenkoa
 *
 */
public interface ProxyTemplateBuilderInteraface<T extends ProxyTemplate> extends TemplateBuilder<T> {
	/**
	 * Method to allow addition of appropriate library classes
	 * where all of the methods have already been wrapped or do not have
	 * any dependencies 
	 * @param c class to add
	 */
	public abstract void addCompatableLibraryClass(Class<?> c);
	/**
	 * Add a wrapper class to use for wrapping a source class
	 * @param baseClass - source class
	 * @param wraperClass - wrapper class
	 */
	public abstract void addSClass(Class<?> baseClass, Class<?> wraperClass);
	/**
	 * Add a wrapper class to use for wrapping a source class(deep)
	 * @param baseClass - source class
	 * @param wraperClass - wrapper class
	 */
	public abstract void deepAddSClass(Class<?> baseClass, Class<?> wraperClass);
	/**
	 * Add a template class that corresponds to the source class
	 * @param baseClass - source class
	 * @param wraperClass - wrapper class
	 */
	public abstract void removeSClass(Class<?> baseClass);
	
	/**
	 * Creates a new template that can then be configured further by specifying addition 
	 * compatible classes and methods to include or exclude.
	 * @param shadowName - name to use on the scripting side
	 * @param source - base class (can be abstract, can not be an interface)
	 * @param autoProcess (create representation for all methods apart from the methods of object)
	 * @return template of the class
	 */
	public abstract ProxyTemplateBuilder spawnTemplate(String shadowName, Class<?> source, boolean autoProcess);
}