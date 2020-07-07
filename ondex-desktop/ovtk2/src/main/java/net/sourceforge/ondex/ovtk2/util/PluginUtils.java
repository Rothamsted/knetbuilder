package net.sourceforge.ondex.ovtk2.util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sourceforge.ondex.init.PluginRegistry;
import net.sourceforge.ondex.ovtk2.config.Config;

public class PluginUtils {

	/**
	 * @author hindlem
	 */
	public static class MissingPluginException extends Throwable {

		/**
		 * added by default
		 */
		private static final long serialVersionUID = 1L;

		public MissingPluginException(String s) {
			super(s);
		}

		public MissingPluginException(String s, Exception e) {
			super(s + " : " + e.getMessage());
		}
	}

	/**
	 * Initializes the {@link PluginRegistry} by reading the plugins/ dir.
	 * 
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void initPluginRegistry() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if (!Config.isApplet) {
			String pluginsDirOndex = new File(new File(net.sourceforge.ondex.config.Config.ondexDir).getAbsoluteFile().getParent() + File.separator + "plugins").getAbsoluteFile().getAbsolutePath();
			String pluginsDirOvtk = new File(new File(Config.ovtkDir).getAbsoluteFile().getParent() + File.separator + "plugins").getAbsoluteFile().getAbsolutePath();
			Class<?> cls = Thread.currentThread().getContextClassLoader().loadClass("net.sourceforge.ondex.init.PluginRegistry");
			Method m = cls.getMethod("init", new Class[] { boolean.class, String[].class });
			m.invoke(null, new Object[] { true, new String[] { pluginsDirOndex, pluginsDirOvtk } });
		} else {
			// in-case of applet, no plugins dir should be used
			Class<?> cls = Thread.currentThread().getContextClassLoader().loadClass("net.sourceforge.ondex.init.PluginRegistry");
			Method m = cls.getMethod("init", new Class[] { boolean.class });
			m.invoke(null, new Object[] { true });
		}
	}

}
