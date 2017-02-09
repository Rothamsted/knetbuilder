package net.sourceforge.ondex.ovtk2.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import net.sourceforge.ondex.core.Attribute;
//import net.sourceforge.ondex.exception.type.ONDEXConfigurationException;
import net.sourceforge.ondex.ovtk2.annotator.OVTK2Annotator;
import net.sourceforge.ondex.ovtk2.filter.OVTK2Filter;
import net.sourceforge.ondex.ovtk2.io.OVTK2IO;
import net.sourceforge.ondex.ovtk2.layout.OVTK2Layouter;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.gds.GDSEditor;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;

public class OVTK2PluginLoader {

	/**
	 * An enumeration of different producer types providing their package and
	 * class names.
	 * 
	 * @author jweile
	 * 
	 */
	private enum Type {
		ANNOTATOR("annotator"), FILTER("filter"), LAYOUT("layout"), IO("io"), GDSEDITOR("ui.gds"), POPUPITEM("ui.popup.items");

		String pack;

		private Type(String pack) {
			this.pack = pack;
		}
	}

	/**
	 * singleton getter.
	 * 
	 * @return the singleton instance of this class.
	 * @throws MalformedURLException
	 */
	public static OVTK2PluginLoader getInstance() throws FileNotFoundException, MalformedURLException {
		if (instance == null) {
			instance = new OVTK2PluginLoader();
		}
		return instance;
	}

	/**
	 * the class loader
	 */
	public URLClassLoader ucl;

	/**
	 * the producer directory name
	 */
	public static final String PLUGIN_DIR = "plugins/";

	/**
	 * sets of names of available plugins of respective type.
	 */
	private Set<PluginID> annotatorNames, filterNames;

	/**
	 * set of names of available layouts, IOs, GDSEditors and PopupItems.
	 */
	private Set<String> layoutNames, ioNames, gdseditorNames, popupitemNames;

	/**
	 * singleton.
	 */
	private static OVTK2PluginLoader instance;

	/**
	 * debug mode.
	 */
	private static final boolean DEBUG = true;

	/**
	 * singleton constructor.
	 * 
	 * @throws MalformedURLException
	 */
	private OVTK2PluginLoader() throws FileNotFoundException, MalformedURLException {
		reload();
	}

	/**
	 * Finds instances of the given producer type in the given list of names.
	 * 
	 * @param list
	 *            the string
	 * @param type
	 * @return a Set of producer names of the given types.
	 */
	private Set<PluginID> findInstances(String list, Type type) {
		Pattern p = Pattern.compile("net/sourceforge/ondex/ovtk2/" + type.pack + "/([a-zA-Z0-9]+?)/([a-zA-Z0-9]+?)\\.class");
		Matcher m = p.matcher(list);
		Set<PluginID> set = new HashSet<PluginID>();
		while (m.find()) {
			String pack = m.group(1);
			String name = m.group(2);
			PluginID plid = new PluginID(pack, name);
			if (!pack.contains("/") && !name.contains("/")) {
				// hack to cope with insufficient regex
				boolean confirmed = false;
				switch (type) {
				case ANNOTATOR:
					confirmed = testAnnotator(plid);
					break;
				case FILTER:
					confirmed = testFilter(plid);
					break;
				}

				if (confirmed) {
					set.add(plid);
				}
			}
		}
		if (DEBUG) {
			printSet(type, set);
		}
		return set;
	}

	/**
	 * Finds instances of the given producer type in the given list of names.
	 * 
	 * @param list
	 *            the string
	 * @param type
	 * @return a Set of producer names of the given types.
	 */
	private Set<String> findIOInstances(String list) {
		Pattern p = Pattern.compile("net/sourceforge/ondex/ovtk2/" + Type.IO.pack + "/([a-zA-Z0-9]+?)\\.class");
		Matcher m = p.matcher(list);
		Set<String> set = new HashSet<String>();
		while (m.find()) {
			String name = m.group(1);
			if (!name.contains("/")) {// hack to cope with insufficient regex
				/*
				 * We have to hope that the class is actually a IO, because we
				 * can't instantiate it without a graph present.
				 */
				set.add(name);
			}
		}
		if (DEBUG) {
			printSet(Type.IO, set);
		}
		return set;
	}

	/**
	 * Finds instances of the given producer type in the given list of names.
	 * 
	 * @param list
	 *            the string
	 * @param type
	 * @return a Set of producer names of the given types.
	 */
	private Set<String> findLayoutInstances(String list) {
		Pattern p = Pattern.compile("net/sourceforge/ondex/ovtk2/" + Type.LAYOUT.pack + "/([a-zA-Z0-9]+?)\\.class");
		Matcher m = p.matcher(list);
		Set<String> set = new HashSet<String>();
		while (m.find()) {
			String name = m.group(1);
			if (!name.contains("/")) {// hack to cope with insufficient regex
				/*
				 * We have to hope that the class is actually a layout, because
				 * we can't instantiate it without a graph present.
				 */
				set.add(name);
			}
		}
		if (DEBUG) {
			printSet(Type.LAYOUT, set);
		}
		return set;
	}

	/**
	 * Finds instances of the given producer type in the given list of names.
	 * 
	 * @param list
	 *            the string
	 * @param type
	 * @return a Set of producer names of the given types.
	 */
	private Set<String> findPopupitemInstances(String list) {
		Pattern p = Pattern.compile("net/sourceforge/ondex/ovtk2/" + Type.POPUPITEM.pack + "/([a-zA-Z0-9]+?)\\.class");
		Matcher m = p.matcher(list);
		Set<String> set = new HashSet<String>();
		while (m.find()) {
			String name = m.group(1);
			if (!name.contains("/")) {// hack to cope with insufficient regex
				/*
				 * We have to hope that the class is actually a layout, because
				 * we can't instantiate it without a graph present.
				 */
				set.add(name);
			}
		}
		if (DEBUG) {
			printSet(Type.POPUPITEM, set);
		}
		return set;
	}

	/**
	 * Finds instances of the given producer type in the given list of names.
	 * 
	 * @param list
	 *            the string
	 * @param type
	 * @return a Set of producer names of the given types.
	 */
	private Set<String> findGDSEditorInstances(String list) {
		Pattern p = Pattern.compile("net/sourceforge/ondex/ovtk2/" + Type.GDSEDITOR.pack + "/([a-zA-Z0-9]+?)\\.class");
		Matcher m = p.matcher(list);
		Set<String> set = new HashSet<String>();
		while (m.find()) {
			String name = m.group(1);
			if (!name.contains("/")) {// hack to cope with insufficient regex
				/*
				 * We have to hope that the class is actually a layout, because
				 * we can't instantiate it without a graph present.
				 */
				set.add(name);
				try {
					// hack to get static part of class registering to
					// JABRegistry
					Thread.currentThread().setContextClassLoader(ucl);
					Class<?>[] args = new Class<?>[] { Attribute.class };
					Class<?> clazz = ucl.loadClass(toClassName(Type.GDSEDITOR, name));
					Constructor<?> constr = clazz.getConstructor(args);
					Attribute attribute = null;
					constr.newInstance(attribute);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		if (DEBUG) {
			printSet(Type.GDSEDITOR, set);
		}
		return set;
	}

	public Set<String> getAnnotatorClassNames() {
		Set<String> set = new HashSet<String>();
		for (PluginID plid : annotatorNames) {
			set.add(toClassName(Type.ANNOTATOR, plid));
		}
		return set;
	}

	/**
	 * gets the available annotator ids.
	 * 
	 * @return the available annotator ids.
	 */
	public Set<PluginID> getAvailableAnnotatorIDs() {
		return annotatorNames;
	}

	/**
	 * gets the available filter ids.
	 * 
	 * @return the available filter ids.
	 */
	public Set<PluginID> getAvailableFilterIDs() {
		return filterNames;
	}

	/**
	 * gets the available popup item ids.
	 * 
	 * @return the available popup item ids.
	 */
	public Set<String> getAvailablePopupitemIDs() {
		return popupitemNames;
	}

	/**
	 * gets the available IO ids.
	 * 
	 * @return the available IO ids.
	 */
	public Set<String> getAvailableIOIDs() {
		return ioNames;
	}

	/**
	 * gets the available layouter ids.
	 * 
	 * @return the available layouter ids.
	 */
	public Set<String> getAvailableLayoutIDs() {
		return layoutNames;
	}

	/**
	 * gets the available Attribute ids.
	 * 
	 * @return the available Attribute ids.
	 */
	public Set<String> getAvailableGDSEditorIDs() {
		return gdseditorNames;
	}

	public Set<String> getFilterClassNames() {
		Set<String> set = new HashSet<String>();
		for (PluginID plid : filterNames) {
			set.add(toClassName(Type.FILTER, plid));
		}
		return set;
	}

	public Set<String> getPopupitemClassNames() {
		Set<String> set = new HashSet<String>();
		for (String name : popupitemNames) {
			set.add(toClassName(Type.POPUPITEM, name));
		}
		return set;
	}

	public Set<String> getIOClassNames() {
		Set<String> set = new HashSet<String>();
		for (String name : ioNames) {
			set.add(toClassName(Type.IO, name));
		}
		return set;
	}

	public Set<String> getLayoutClassNames() {
		Set<String> set = new HashSet<String>();
		for (String name : layoutNames) {
			set.add(toClassName(Type.LAYOUT, name));
		}
		return set;
	}

	public Set<String> getGDSEditorClassNames() {
		Set<String> set = new HashSet<String>();
		for (String name : gdseditorNames) {
			set.add(toClassName(Type.GDSEDITOR, name));
		}
		return set;
	}

	@SuppressWarnings("unchecked")
	public OVTK2Annotator loadAnnotator(PluginID plid, OVTK2PropertiesAggregator viewer) throws InstantiationException {
		String classname = toClassName(Type.ANNOTATOR, plid);
		try {
			Thread.currentThread().setContextClassLoader(ucl);
			Class<OVTK2Annotator> clazz = (Class<OVTK2Annotator>) ucl.loadClass(classname);
			OVTK2Annotator plugin = clazz.getConstructor(OVTK2PropertiesAggregator.class).newInstance(viewer);
			return plugin;
		} catch (Throwable t) {
			if (DEBUG) {
				t.printStackTrace();
			}
			throw new InstantiationException("Annotator instantiation failed!");
		}
	}

	@SuppressWarnings("unchecked")
	public OVTK2Filter loadFilter(PluginID plid, OVTK2Viewer viewer) throws InstantiationException {
		String classname = toClassName(Type.FILTER, plid);
		try {
			Thread.currentThread().setContextClassLoader(ucl);
			Class<OVTK2Filter> clazz = (Class<OVTK2Filter>) ucl.loadClass(classname);
			OVTK2Filter plugin = clazz.getConstructor(OVTK2Viewer.class).newInstance(viewer);
			return plugin;
		} catch (Throwable t) {
			if (DEBUG) {
				t.printStackTrace();
			}
			throw new InstantiationException("Filter instantiation failed");
		}
	}

	/**
	 * Loads the IO with the given name
	 * 
	 * @param name
	 *            the name of the IO to load
	 * @return
	 * @throws InstantiationException
	 *             if the instantiation fails for any reason.
	 */
	@SuppressWarnings("unchecked")
	public OVTK2IO loadIO(String name) throws InstantiationException {
		String classname = toClassName(Type.IO, name);
		try {
			Thread.currentThread().setContextClassLoader(ucl);
			Class<OVTK2IO> clazz = (Class<OVTK2IO>) ucl.loadClass(classname);
			if (!OVTK2IO.class.isAssignableFrom(clazz)) {
				return null;
			}
			OVTK2IO plugin = clazz.getConstructor().newInstance();
			return plugin;
		} catch (Throwable t) {
			if (DEBUG) {
				t.printStackTrace();
			}
			throw new InstantiationException("IO instantiation failed");
		}
	}

	/**
	 * Loads the layouter with the given name.
	 * 
	 * @param name
	 *            the name of the layouter to load.
	 * @param viewer
	 *            the current viewer.
	 * @return
	 * @throws InstantiationException
	 *             if the instantiation fails for any reason.
	 */
	@SuppressWarnings("unchecked")
	public OVTK2Layouter loadLayouter(String name, OVTK2PropertiesAggregator viewer) throws InstantiationException {
		String classname = toClassName(Type.LAYOUT, name);
		try {
			Thread.currentThread().setContextClassLoader(ucl);
			Class<OVTK2Layouter> clazz = (Class<OVTK2Layouter>) ucl.loadClass(classname);
			OVTK2Layouter plugin = clazz.getConstructor(OVTK2PropertiesAggregator.class).newInstance(viewer);
			return plugin;
		} catch (Throwable t) {
			if (DEBUG) {
				t.printStackTrace();
			}
			throw new InstantiationException("Layouter instantiation failed!");
		}
	}

	/**
	 * Loads the GDSEditor with the given name.
	 * 
	 * @param name
	 *            the name of the GDSeditor to load.
	 * @param attribute
	 *            the current Attribute.
	 * @return
	 * @throws InstantiationException
	 *             if the instantiation fails for any reason.
	 */
	@SuppressWarnings("unchecked")
	public GDSEditor loadAttributeEditor(String name, Attribute attribute) throws InstantiationException {
		String classname = toClassName(Type.GDSEDITOR, name);
		try {
			Thread.currentThread().setContextClassLoader(ucl);
			Class<GDSEditor> clazz = (Class<GDSEditor>) ucl.loadClass(classname);
			GDSEditor plugin = clazz.getConstructor(Attribute.class).newInstance(attribute);
			return plugin;
		} catch (Throwable t) {
			if (DEBUG) {
				t.printStackTrace();
			}
			throw new InstantiationException("GDSEditor instantiation failed!");
		}
	}

	/**
	 * Loads the PopupItem with the given name
	 * 
	 * @param name
	 *            the name of the PopupItme to load
	 * @return
	 * @throws InstantiationException
	 *             if the instantiation fails for any reason.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public EntityMenuItem loadPopupItem(String name) throws InstantiationException {
		String classname = toClassName(Type.POPUPITEM, name);
		try {
			Thread.currentThread().setContextClassLoader(ucl);
			Class<EntityMenuItem> clazz = (Class<EntityMenuItem>) ucl.loadClass(classname);
			EntityMenuItem plugin = clazz.getConstructor().newInstance();
			return plugin;
		} catch (Throwable t) {
			if (DEBUG) {
				t.printStackTrace();
			}
			throw new InstantiationException("EntityMenuItem instantiation failed");
		}
	}

	/**
	 * Debug helper method
	 */
	private void printSet(Type type, Set<?> set) {
		System.out.println("Loaded " + type + ":\n=================");
		for (Object o : set) {
			System.out.println(o);
		}
	}

	/**
	 * Loads plugins from given path
	 * 
	 * @param urls
	 * @param classRegisterBuilder
	 * @param path
	 * @throws MalformedURLException
	 */
	private void loadFromPath(Vector<URL> urls, StringBuilder classRegisterBuilder, String path) throws MalformedURLException {
		// loading from URL for applet
		URL url = new URL(path);
		urls.add(url);
		scanClasses(classRegisterBuilder, url);
		scanConfig(url);
	}

	/**
	 * rescans the plugins directory and registers all found plugins with the
	 * class loader.
	 * 
	 * @throws MalformedURLException
	 * @throws FileNotFoundException
	 */
	public void reload() throws FileNotFoundException, MalformedURLException {

		Vector<URL> urls = new Vector<URL>();
		StringBuilder classRegisterBuilder = new StringBuilder();

		if (Config.ovtkDir.contains("://") || Config.ovtkDir.startsWith("file:/")) {
			System.out.println("Scanning http lib directory for plugins");
			String path = Config.ovtkDir.substring(0, Config.ovtkDir.lastIndexOf("config")) + "lib/";
			File dir = new File(path);
			if (dir.exists() && dir.isDirectory()) {
				for (File f : dir.listFiles()) {
					if (f.getName().contains("ovtk2-default-")) {
						loadFromPath(urls, classRegisterBuilder, f.getAbsolutePath());
					}
					if (f.getName().contains("ovtk2-experimental-")) {
						loadFromPath(urls, classRegisterBuilder, f.getAbsolutePath());
					}
				}
			} 
			
			else 
			{
				List<String> founds = getHttpFilePaths(path);				
	
		        for(String found : founds)
		        {   
		        	loadFromPath(urls, classRegisterBuilder, path+found);
		        }
				
			}

		} else {
			// make sure directory exists.
			File pluginDir = new File(PLUGIN_DIR);
			if (!pluginDir.exists()) {
				throw new FileNotFoundException("Could not find plugin directory: " + pluginDir.getAbsoluteFile());
			}

			// register urls
			for (File child : pluginDir.listFiles()) {
				if (child.getName().endsWith(".jar")) {
					try {
						urls.add(child.toURI().toURL());
					} catch (MalformedURLException e) {
					}
					scanClasses(classRegisterBuilder, child);
					scanConfig(child);
				}
			}
		}

		// create class loader on the collection of URLs with parent
		ClassLoader parent = Thread.currentThread().getContextClassLoader();
		if (parent == null)
			parent = ClassLoader.getSystemClassLoader();
		ucl = new URLClassLoader(urls.toArray(new URL[urls.size()]), parent);
		Thread.currentThread().setContextClassLoader(ucl);

		// write found classes into appropriate sets.
		String classRegister = classRegisterBuilder.toString();
		filterNames = findInstances(classRegister, Type.FILTER);
		annotatorNames = findInstances(classRegister, Type.ANNOTATOR);
		layoutNames = findLayoutInstances(classRegister);
		ioNames = findIOInstances(classRegister);
		gdseditorNames = findGDSEditorInstances(classRegister);
		popupitemNames = findPopupitemInstances(classRegister);
	}

	
	/**
	 * Search the file paths of ovtk2-default and ovtk2-experimental per http 
	 */	
	protected List<String> getHttpFilePaths(String path) throws FileNotFoundException
	{	
		List<String> founds = new ArrayList<String>();
		URL url=null;		
		
		try 
		{
			//Http connection
			url = new URL(path);   		    	 	
			URLConnection con = url.openConnection();
			// ConnectionTimout = 10sec
			con.setConnectTimeout(10*1000);
			con.setReadTimeout(10*1000);   	
    	
			BufferedReader in = new BufferedReader(
	    			new InputStreamReader(con.getInputStream()));

			String inputLine;
		
			// Pattern to find the line of the correct filename
			Pattern pat = Pattern.compile("ovtk2-default|ovtk2-experimental");
			// Pattern to find the start of the filename
			Pattern pat2 = Pattern.compile("href=");
			// Pattern to find the end of the filename
			Pattern pat3 = Pattern.compile("\"");        
		        
			while ((inputLine = in.readLine()) != null)
			{	   
				Matcher mat = pat.matcher(inputLine);			
				
				// if "ovtk2-..." was found
				if(mat.find()==true)
				{				
					Matcher mat2 = pat2.matcher(inputLine);
					
					// find the beginning of "href=" at this line
					if(mat2.find()==true)
					{	
						// the result string begin behind href" 
						inputLine = inputLine.substring(mat2.end()+1);        	
						Matcher mat3 = pat3.matcher(inputLine);	        	
						
						// the result string ends at the first found of " 
						if(mat3.find()==true)
						{
							inputLine = inputLine.substring(0,mat3.start());
							founds.add(inputLine);
						}			        
					}        	       
				}		        	
			}		            
			
			in.close();
			
		}
	
		catch (Exception e)
		{
			throw new FileNotFoundException("Could not find valid plugin directory at: " + path + "\n" + e.getMessage());
		}
		
		return founds;
	}
	
	
	
	/**
	 * scans a given zip file for file entries and appends them to the given
	 * string builder.
	 * 
	 * @param b
	 *            the string builder
	 * @param file
	 *            a zip file
	 */
	private void scanClasses(StringBuilder b, File file) {
		try {
			ZipFile zipfile = new ZipFile(file);
			Enumeration<? extends ZipEntry> entries = zipfile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				b.append(entry.getName() + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * scans a given zip URL for file entries and appends them to the given
	 * string builder.
	 * 
	 * @param b
	 *            the string builder
	 * @param url
	 *            a zip file URL
	 */
	private void scanClasses(StringBuilder b, URL url) {
		try {
			ZipInputStream stream = new ZipInputStream(url.openStream());
			ZipEntry entry;
			while ((entry = stream.getNextEntry()) != null) {
				b.append(entry.getName() + "\n");
			}
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads the contained config files from jar file.
	 * 
	 * @param file
	 */
	private void scanConfig(File file) {
		loadConfig(file, Config.config, "config/config.xml");
		loadConfig(file, Config.language, "config/english.xml");
	}

	/**
	 * Loads the contained config files from jar file.
	 * 
	 * @param url
	 */
	private void scanConfig(URL url) {
		loadConfig(url, Config.config, "config/config.xml");
		loadConfig(url, Config.language, "config/english.xml");
	}

	/**
	 * This hack is required as loadFromXML closes the InputStream.
	 * 
	 * @param file
	 * @param properties
	 * @param config
	 */
	private void loadConfig(File file, Properties properties, String config) {
		try {
			ZipInputStream stream = new ZipInputStream(new FileInputStream(file));
			ZipEntry entry;
			while ((entry = stream.getNextEntry()) != null) {
				if (entry.getName().equals(config)) {
					System.out.println("Loading: " + file.getAbsoluteFile() + "#" + config);
					properties.loadFromXML(stream);
				}
			}
			stream.close();
		} catch (Exception e) {
		}
	}

	/**
	 * This hack is required as loadFromXML closes the InputStream.
	 * 
	 * @param url
	 * @param properties
	 * @param config
	 */
	private void loadConfig(URL url, Properties properties, String config) {
		try {
			ZipInputStream stream = new ZipInputStream(url.openStream());
			ZipEntry entry;
			while ((entry = stream.getNextEntry()) != null) {
				if (entry.getName().equals(config)) {
					System.out.println("Loading: " + url.toString() + "#" + config);
					properties.loadFromXML(stream);
				}
			}
			stream.close();
		} catch (Exception e) {
		}
	}

	@SuppressWarnings("unchecked")
	public boolean testAnnotator(PluginID plid) {
		String classname = toClassName(Type.ANNOTATOR, plid);
		try {
			Thread.currentThread().setContextClassLoader(ucl);
			Class<OVTK2Annotator> clazz = (Class<OVTK2Annotator>) ucl.loadClass(classname);
			if (OVTK2Annotator.class.isAssignableFrom(clazz)) {
				return true;
			} else {
				return false;
			}
		} catch (Throwable t) {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public boolean testFilter(PluginID plid) {
		String classname = toClassName(Type.FILTER, plid);
		try {
			Thread.currentThread().setContextClassLoader(ucl);
			Class<OVTK2Filter> clazz = (Class<OVTK2Filter>) ucl.loadClass(classname);
			if (OVTK2Filter.class.isAssignableFrom(clazz)) {
				return true;
			} else {
				return false;
			}
		} catch (Throwable t) {
			return false;
		}
	}

	/**
	 * helper class constructs a classname for a producer type and id.
	 */
	private String toClassName(Type type, PluginID plid) {
		return "net.sourceforge.ondex.ovtk2." + type.pack + "." + plid.getPackage() + "." + plid.getClassName();
	}

	/**
	 * helper class constructs a classname for a producer type and class name.
	 */
	private String toClassName(Type type, String name) {
		return "net.sourceforge.ondex.ovtk2." + type.pack + "." + name;
	}

}
