package net.sourceforge.ondex.emolecules.utils;

/**
 *
 * @author grzebyta
 */
public class ClassUtils {
    
    public static Object getInstance(String name) throws ClassNotFoundException, 
            InstantiationException, IllegalAccessException {
        ClassLoader cLoader = Thread.currentThread().getContextClassLoader();
        Class<?> clazz = cLoader.loadClass(name);
        
        return clazz.newInstance();
    }
}
