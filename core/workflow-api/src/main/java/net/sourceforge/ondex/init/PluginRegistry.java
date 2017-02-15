package net.sourceforge.ondex.init;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import net.sourceforge.ondex.ONDEXPlugin;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.validator.AbstractONDEXValidator;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.clapper.util.classutil.AbstractClassFilter;
import org.clapper.util.classutil.AndClassFilter;
import org.clapper.util.classutil.ClassFilter;
import org.clapper.util.classutil.ClassFinder;
import org.clapper.util.classutil.ClassInfo;
import org.clapper.util.classutil.InterfaceOnlyClassFilter;
import org.clapper.util.classutil.NotClassFilter;
import org.clapper.util.classutil.SubclassClassFilter;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;

/**
 * @author lysenkoa
 */
public class PluginRegistry {
    private static final Logger LOG = Logger.getLogger(PluginRegistry.class);
    private static final boolean LIST_DUPLICATE_NAMES = true;
    private URLClassLoader ucl;

    //registers duplicated plugins ids that could not be loaded and had to be renamed
    private final Map<ArrayKey<String>, List<ArrayKey<String>>> blacklisted = new HashMap<ArrayKey<String>, List<ArrayKey<String>>>();

    private final Map<ArrayKey<String>, PluginDescription> internalIdToPluginDescription = new HashMap<ArrayKey<String>, PluginDescription>();

    private static PluginRegistry instance;

    /**
     * @param readConfigFromJar  flag to indicate if config should be read from jar or not
     * @param dirOrFiles         the jar files or directories to scan
     * @return a PluginRegistry built from the listed resources
     * @throws IOException
     * @throws URISyntaxException
     */
    public static synchronized PluginRegistry init(boolean readConfigFromJar, String... dirOrFiles) throws IOException, URISyntaxException {
        if (instance == null) {
            instance = new PluginRegistry(readConfigFromJar, dirOrFiles);
        }
        return instance;
    }

    /**
     * @return
     */
    public static synchronized PluginRegistry getInstance() {
        if (instance == null) {
            throw new RuntimeException("Can not get an instance of PluginRegistry, as it was not initialized!");
        }
        return instance;
    }

    /**
     * @param id   the id of the producer
     * @param type the type of producer
     * @return
     */
    public Object loadPlugin(String id, PluginType type) throws PluginLoadingException {
        PluginDescription bean = internalIdToPluginDescription.get(new ArrayKey<String>(new String[]{type.getName(), id}));
        if (bean == null) {
            StringBuilder sb = new StringBuilder();
            for (ArrayKey<String> k : internalIdToPluginDescription.keySet()) {
                sb.append("\t")
                        .append(k.getArray()[0])
                        .append(":")
                        .append(k.getArray()[1])
                        .append("(")
                        .append(internalIdToPluginDescription.get(k).getCls())
                        .append(")\n");
            }
            throw new UnregisteredPluginException(
                    "No plugin of id \"" + id + "\" and type \"" + type.getName() +
                            "\" is registered among:\n" + sb.toString());
        }

        try {
            Class<?> cls = Thread.currentThread().getContextClassLoader().loadClass(bean.getCls());
            return cls.getConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        throw new PluginLoadingException("Plugin of id: " + id + " and type :" + type.getName() + " could not be instantiated");
    }

    /**
     * @param readConfigFromJar
     * @throws IOException
     * @throws URISyntaxException
     */
    private PluginRegistry(boolean readConfigFromJar, String... dirOrFiles) throws IOException, URISyntaxException {
        PluginRegistry.instance = this;
        LOG.debug("Reading configuration from jar - " + readConfigFromJar);
        if (readConfigFromJar) {
            readConfigFromJar(dirOrFiles);
        } else {
            readSimpleConfigFormat(dirOrFiles);
        }
    }

    private void addRecursively(File dir, Set<URL> jars) {
        LOG.debug("Scanning directory for plugins: " + dir.getAbsolutePath());

        for (File child : dir.listFiles()) {
            if (child.isDirectory()) {
                addRecursively(child, jars);
            }
            try {
                LOG.debug("Adding jar: " + child);
                jars.add(child.getAbsoluteFile().toURI().toURL());
            } catch (MalformedURLException e) {
                LOG.debug("Failed to add jar", e);
            }
        }
    }

    /**
     * @throws IOException
     * @throws URISyntaxException
     */
    private void readConfigFromJar(String... pluginDirectories) throws IOException {

        Set<URL> jarFileURLs = new HashSet<URL>();

        for (String pluginDirectory : pluginDirectories) {
            File pluginDir = new File(pluginDirectory).getAbsoluteFile();

            if (!pluginDir.exists()) {
                System.err.println("Plugin directory missing! " + pluginDir.getAbsolutePath());
            } else {
                addRecursively(pluginDir, jarFileURLs);
            }
        }

        //set up the classloader
        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        if (parent == null) {
            parent = ClassLoader.getSystemClassLoader();
        }
        ucl = new URLClassLoader(jarFileURLs.toArray(new URL[jarFileURLs.size()]), parent);
        Thread.currentThread().setContextClassLoader(ucl);

        Set<URL> urlsToScan = new HashSet<URL>();
        urlsToScan.addAll(jarFileURLs);

        Set<File> filesToScan = new HashSet<File>();
        for (URL url : urlsToScan) {
            LOG.debug("Scanning for plugins in: " + url);
            try {
                File f = new File(url.toURI());
                if (f.getName().endsWith(".jar")) {
                    JarFile jar = new JarFile(f);
                    ZipEntry ent = jar.getEntry("workflow-component-description.xml");
                    if (ent != null) {
                        InputStream is = jar.getInputStream(ent);
                        LOG.debug("Loading plugins from: " + f);
                        try {
                            parseWorkflowComponentDescription(is, f.getName());
                        } catch (JDOMException e) {
                            LOG.warn("Error in processing " + ent.getName() + " in " + f.getAbsolutePath(), e);
                        } catch (DuplicateOndexPluginsIdsException e) {
                            LOG.warn("Error in processing " + ent.getName() + " in " + f.getAbsolutePath(), e);
                        }
                    } else {
                        LOG.debug("No workflow-component-description.xml found");
                    }
                } else if (f.isDirectory()) {
                    filesToScan.add(f);
                }
            } catch (URISyntaxException e) {
                LOG.debug("Problem processing url", e);
            }
        }

        if (Boolean.parseBoolean(System.getProperty("plugin.scan.lib"))) {
            LOG.debug("Scanning lib directory for plugins");

            String urls = System.getProperties().getProperty("java.class.path", null);
            Set<File> libFileURLs = new HashSet<File>();
            for (String lib : urls.split(";")) {
                File child = new File(lib).getAbsoluteFile();
                if (child.exists() && !child.isFile()) {
                    libFileURLs.add(child.getAbsoluteFile());
                }
            }
            libFileURLs.addAll(filesToScan);
            scanClassesForPlugins(libFileURLs);
        }
        if (LIST_DUPLICATE_NAMES) listDuplicateNames();

        LOG.warn("Loaded " + internalIdToPluginDescription.size() + " plugins");
        LOG.debug("  " + internalIdToPluginDescription.keySet());
    }

    private void scanClassesForPlugins(Collection<File> files) {
        ClassFinder finder = new ClassFinder();
        finder.add(files);

        for (PluginType plugin : PluginType.values()) {

            ClassFilter filter =
                    new AndClassFilter
                            // Must not be an interface
                            (new NotClassFilter(new InterfaceOnlyClassFilter()),
                                    // Must implement the ClassFilter interface
                                    new SubclassClassFilter(plugin.getPluginClass()),
                                    // Must not be abstract
                                    new NotClassFilter(new AbstractClassFilter()));

            Collection<ClassInfo> foundClasses = new ArrayList<ClassInfo>();
            finder.findClasses(foundClasses, filter);

            for (ClassInfo classInfo : foundClasses) {
                try {
                    PluginDescription pb = new LazyPluginDefinition();
                    String clsName = classInfo.getClassName();
                    Class<?> cls = Thread.currentThread().getContextClassLoader().loadClass(clsName);

                    pb.setOndexTypeName(plugin.getName());
                    pb.setOndexType(plugin);
                    pb.setCls(clsName);

                    try {
                        if (ONDEXPlugin.class.isAssignableFrom(cls)) {
                            ONDEXPlugin pluginInstance = cls.asSubclass(ONDEXPlugin.class)
                                    .getConstructor().newInstance();
                            pb.setName(pluginInstance.getName());
                            pb.setOndexId(pluginInstance.getId());
                            pb.setVersion(pluginInstance.getVersion());
                        } else if (AbstractONDEXValidator.class.isAssignableFrom(cls)) {
                            AbstractONDEXValidator pluginInstance = cls.asSubclass(AbstractONDEXValidator.class)
                                    .getConstructor().newInstance();
                            pb.setCls(clsName);
                            pb.setName(pluginInstance.getName());
                            pb.setOndexId(pluginInstance.getId());
                            //pb.setVersion(pluginInstance.getVersion()); @TODO: version validators
                        }
                    } catch (Exception e) {
                        System.err.println("Could not load plugin " + clsName + " " + e.getMessage());
                        continue;
                    }

                    Status statusAnnotation = cls.getAnnotation(Status.class);
                    if (statusAnnotation != null) {
                        String status = statusAnnotation.status().getValue().trim();
//                            String description = statusAnnotation.description().trim();
                        pb.setPath(plugin.getName() + "/" + status);
                    } else {
                        pb.setPath(plugin.getName() + "/Experimental");
                    }
                    pb.setDescription("");
                    pb.setGUIType("plugin");

                    registerPlugin(new ArrayKey<String>(new String[]{pb.getOndexType().getName(), pb.getOndexId()}), pb);
                } catch (ClassNotFoundException e) {
                    LOG.error("Problem loading class for plugin: " + classInfo.getClassName(), e);
                }
            }
        }
    }

    /**
     * Parses PluginDescription from the xml of the InputStream and registers them to the internalIdToPluginDescription map
     *
     * @param is      the imput stream the xml is parsed from
     * @param jarName name for the jar, used to help track the plug-in issues to the correct module
     * @throws IOException on reading the xml file
     */
    private void parseWorkflowComponentDescription(InputStream is, String jarName) throws IOException, JDOMException, DuplicateOndexPluginsIdsException {
        SAXBuilder builder = new SAXBuilder(); // parameters control validation,
        builder.setValidation(false);
        Document doc = builder.build(is);

        Element artifactElement = doc.getRootElement().getChild("artifactId");
        String module = "";
        if (artifactElement != null) {
            module = artifactElement.getText();
        }

        Iterator<?> plugins = doc.getDocument().getDescendants(new ElementFilter("plugin"));
        while (plugins.hasNext()) {
            Element plugin = (Element) plugins.next();

            String clsName = plugin.getChildText("entryClass", plugin.getNamespace());

            try {
                Class<?> cls = Thread.currentThread().getContextClassLoader().loadClass(clsName);

                if (Modifier.isAbstract(cls.getModifiers())) {
                    LOG.warn(clsName + " is abstract and not a valid plugin: ignoring");
                    continue;
                } else if (Modifier.isInterface(cls.getModifiers())) {
                    LOG.warn(clsName + " is an interface and not a valid plugin: ignoring");
                    continue;
                }

                Object pluginObject;

                PluginDescription pb = new LazyPluginDefinition();
                if (ONDEXPlugin.class.isAssignableFrom(cls)) {
                	ONDEXPlugin pluginInstance;
                	try{
                		 pluginInstance = cls.asSubclass(ONDEXPlugin.class).getConstructor().newInstance();
                	}
                	catch(NoClassDefFoundError e){
                		 LOG.debug("Class "+ cls.getCanonicalName()+" could not be loaded due to NoClassDefFoundError");
                		continue;
                	}
                    
                    pb.setName(processName(pluginInstance.getName(), clsName, jarName));
                    pb.setOndexId(pluginInstance.getId());
                    pb.setVersion(pluginInstance.getVersion());
                    pluginObject = pluginInstance;
                    LOG.debug(clsName + " being registered as as a plugin: " + pb.getName() + "~" + pb.getOndexId());
                } else if (AbstractONDEXValidator.class.isAssignableFrom(cls)) {
                    AbstractONDEXValidator validatorInstance = (AbstractONDEXValidator) cls.getConstructor().newInstance();
                    pb.setName(validatorInstance.getName());
                    pb.setOndexId(validatorInstance.getId());
                    pluginObject = validatorInstance;

                    LOG.debug(clsName + " being registered as as a validator: " + pb.getName() + "~" + pb.getOndexId());
                } else {
                    LOG.debug(clsName + " being registered as an unknown type of plugin");
                    pluginObject = cls.getConstructor().newInstance();
                }

                pb.setModuleId(module);

                PluginType ondexType = PluginType.getType(pluginObject);
                pb.setCls(clsName);
                pb.setOndexTypeName(ondexType.getName());
                pb.setOndexType(ondexType);
                List<Element> children = plugin.getChildren();

                Element e = null;
                for (Element child : children) {
                    if (child.getName().equals("status")) {
                        e = child;
                    }
                }
                if (e != null) {
                    pb.setPath(ondexType.getName() + "/" + e.getAttributeValue("type"));
                } else {
                    pb.setPath(ondexType.getName() + "/Experimental");
                }

                pb.setDescription(plugin.getChildText("comment", plugin.getNamespace()));
                pb.setGUIType("plugin");


                registerPlugin(new ArrayKey<String>(new String[]{pb.getOndexType().getName(), pb.getOndexId()}), pb);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                throw new RuntimeException("Missing default constructor for " + clsName + e.getMessage());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (PluginType.UnknownPluginTypeException e) {
                e.printStackTrace();
            }
        }
    }

    private void registerPlugin(ArrayKey<String> key, PluginDescription pb) {
        if (internalIdToPluginDescription.containsKey(key)) {
            //blacklist bean
            List<ArrayKey<String>> renames = new ArrayList<ArrayKey<String>>();
            PluginDescription originalBean = internalIdToPluginDescription.remove(key);
            String newIdAppender = pb.getModuleId();
            if (newIdAppender == null) {
                newIdAppender = "";
            }
            newIdAppender = newIdAppender + "[" + (renames.size() + 1) + "]";

            ArrayKey<String> newKey = new ArrayKey<String>(new String[]{originalBean.getOndexType().getName(), originalBean.getOndexId() + "#" + newIdAppender});
            internalIdToPluginDescription.put(newKey, originalBean);
            renames.add(newKey);
            blacklisted.put(key, renames);
            System.err.println("Plugin id clash for " + originalBean.getOndexId() + " renamed to " + originalBean.getOndexId() + "#" + newIdAppender);
            registerPlugin(key, pb); //try again with the key blacklisted

        } else if (blacklisted.keySet().contains(key)) {
            List<ArrayKey<String>> renames = blacklisted.get(key);
            for (ArrayKey<String> renameKey : renames) {
                PluginDescription otherBeans = internalIdToPluginDescription.get(renameKey);
                if (otherBeans.getCls().equals(pb.getCls())) {
                    System.err.println("Warning: Plugin " + pb.getCls() + " has been registered multiple times");
                }
            }
            String newIdAppender = pb.getModuleId();
            if (newIdAppender == null) {
                newIdAppender = "";
            }
            newIdAppender = newIdAppender + "[" + (renames.size() + 1) + "]";

            ArrayKey<String> newKey = new ArrayKey<String>(new String[]{pb.getOndexType().getName(), pb.getOndexId() + "#" + newIdAppender});
            System.err.println("Plugin id clash for " + pb.getOndexId() + " renamed to " + pb.getOndexId() + "#" + newIdAppender);

            internalIdToPluginDescription.put(newKey, pb);
            renames.add(newKey);
        } else {
            internalIdToPluginDescription.put(key, pb);
        }
    }

    /**
     * @param ids
     * @return
     */
    public PluginDescription getPluginDescription(String[] ids) {
        return internalIdToPluginDescription.get(new ArrayKey<String>(ids));
    }

    /**
     * @param ondexType
     * @param ondexId
     * @return
     */
    public PluginDescription getPluginDescription(PluginType ondexType, String ondexId) {
        return internalIdToPluginDescription.get(new ArrayKey<String>(new String[]{ondexType.getName(), ondexId}));
    }

    /**
     * @param inputFiles
     */
    public void readSimpleConfigFormat(String... inputFiles) {
        for (String inputFile : inputFiles) {
            try {
                DataInputStream in = new DataInputStream(new FileInputStream(inputFile));
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String strLine;
                while ((strLine = br.readLine()) != null) {
                    String[] data = strLine.split("#");
                    Class<?> cls = Thread.currentThread().getContextClassLoader().loadClass(data[1]);
                    if (Modifier.isAbstract(cls.getModifiers()) || !Modifier.isPublic(cls.getModifiers()) || !ONDEXPlugin.class.isAssignableFrom(cls)) {
                        continue;
                    }
                    String[] clsName = data[1].split("\\.");
                    if (data.length > 2) {
                        addOndexPlugin(cls, firstUpper(data[0]), clsName[clsName.length - 2], null, data[2], "Experimental/" + firstUpper(data[0]));
                    } else {
                        addOndexPlugin(cls, firstUpper(data[0]), clsName[clsName.length - 2], null, "No description", "Experimental/" + firstUpper(data[0]));
                    }
                }
            }
            catch (Exception e) {
                System.err.println("Failed to read simple configuration file from: " + inputFile);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * A shortcut to add plug-in programmatically, from the PluginBean description. Warning - this method by-passes
     * the id clash handling, so will replace the plug-in with the same id, if one was previously added.
     * This method is intended primarily to be used in JUnit test cases - under normal circomstances, the 
     * plug-ins should be added using via the jar scan.
     * 
     * @param pb - PluginBean description
     */
    public void addPlugin(PluginDescription pb){
        internalIdToPluginDescription.put(new ArrayKey<String>(new String[]{pb.getOndexType().getName(), pb.getOndexId()}), pb);	
    }

    @SuppressWarnings("unchecked")
    public void addOndexPlugin(Class cls, String ondexType, String ondexId, String name, String description, String path) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, PluginType.UnknownPluginTypeException {

        ONDEXPlugin plugin = (ONDEXPlugin) cls.getConstructor().newInstance();
        
        PluginDescription pb = new PluginDescription();
        pb.setCls(cls.getCanonicalName());
        if (name != null) {
            pb.setName(name);
        } else {
            String[] plName = cls.getCanonicalName().split("\\.");
            pb.setName(firstUpper(plName[plName.length - 2]));
        }
        pb.setOndexTypeName(PluginType.getType(plugin).getName());
        pb.setOndexType(PluginType.getType(plugin));
        pb.setOndexId(plugin.getId());
        pb.setVersion(plugin.getVersion());
        if (path == null) {
            pb.setPath(ondexType);
        } else {
            pb.setPath(path);
        }
        pb.setDescription(description);
        pb.setGUIType("plugin");

        pb.setArgDef(constructArgumentArray(plugin));

        internalIdToPluginDescription.put(new ArrayKey<String>(new String[]{pb.getOndexType().getName(), pb.getOndexId()}), pb);
    }

    /**
     * @return
     */
    public List<PluginDescription> getAllPlugins() {
        List<PluginDescription> all = new ArrayList<PluginDescription>();
        all.addAll(internalIdToPluginDescription.values());
        Collections.sort(all, new Comparator<PluginDescription>() {
            @Override
            public int compare(PluginDescription o1, PluginDescription o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return all;

    }


    /**
     * @param in
     * @return
     */
    private static String firstUpper(String in) {
        return in.substring(0, 1).toUpperCase() + in.substring(1);
    }

    /**
     * @param file
     */
    public void save(String file) {
        try {
            //"D:/Test.xml"
            FileOutputStream out = new FileOutputStream(file);
            XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(out));
            for (PluginDescription pb : internalIdToPluginDescription.values()) {
                encoder.writeObject(pb);
                encoder.flush();
            }
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param classname
     * @return
     */
    public Class<? extends ONDEXPlugin> loadCls(String classname) {
        Thread.currentThread().setContextClassLoader(ucl);
        try {
            return ucl.loadClass(classname).asSubclass(ONDEXPlugin.class);
        }
        catch(ClassCastException e){
        	throw new ClassCastException("Classes loaded by plugin registry should extend ONDEXPlugin, got "+classname+" instead.");
        	
        }catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * @param plugin
     * @return
     * @throws Exception
     */
    private ArgumentDescription[] constructArgumentArray(ONDEXPlugin plugin) {
        LOG.debug("Constructing argument array for: " + plugin.getName());
        net.sourceforge.ondex.args.ArgumentDefinition<?>[] ads = plugin.getArgumentDefinitions();
        List<ArgumentDescription> list = new ArrayList<ArgumentDescription>();
        int position = 0;
        // plugin-specific arguments
        if (ads != null && ads.length > 0) {
            LOG.debug("Processing " + ads.length + " argument definitions");
            for (net.sourceforge.ondex.args.ArgumentDefinition<?> ad : ads) {
                String adType = "net.sourceforge.ondex.ONDEXPluginArguments";
                ArgumentDescription ab = new ArgumentDescription();
                String defaultValue = null;
                if (ad.getDefaultValue() != null)
                    defaultValue = ad.getDefaultValue().toString();
                ab.setInputId(position);
                ab.setDefaultValue(defaultValue);
                ab.setDescription(ad.getDescription());
                ab.setInteranlName(ad.getName());
                ab.setName(firstUpper(ad.getName()));
                ab.setCls(adType);
                ab.setIsRequired(ad.isRequiredArgument());
                ab.setParser("pluginArgument");
                ab.setIsConfigurationArgument(true);

                String guitype = "field";
                if (ad.isAllowedMultipleInstances()) {
                    guitype = "list";
                } else if (net.sourceforge.ondex.args.BooleanArgumentDefinition.class.isAssignableFrom(ad.getClass())) {
                    guitype = "checkbox";
                } else if (ad instanceof FileArgumentDefinition) {
                    FileArgumentDefinition fad = (FileArgumentDefinition) ad;
                    if (fad.isDirectory()) {//|| !fad.isPreExisting()
                        ab.setContentHint("browse_folder");
                    } else {
                        ab.setContentHint("browse_file");
                    }
                }
                ab.setType(guitype);
                list.add(ab);

                LOG.debug("Added #" + ab.getInputId() + ": " + ab.getName());
            }
        }
        
        list.addAll(plugin.getArgumentDescriptions(++position));

        return list.toArray(new ArgumentDescription[list.size()]);
    }


    /**
     * @author lysenkoa
     */
    class LazyPluginDefinition extends PluginDescription {

        /**
         *
         */
        public LazyPluginDefinition() {
        }

        @SuppressWarnings("deprecation")
		@Override
        public ArgumentDescription[] getArgDef() {
            if (argDef == null) {
                try {
                    this.argDef = constructArgumentArray(((Class<? extends ONDEXPlugin>) loadCls(this.cls)).getConstructor().newInstance());
                    LOG.debug("Loaded: " + this.cls);
                } catch (Exception e) {
                    //Should never happen by this point.
                	Logger.getRootLogger().log(Priority.WARN, e.getMessage());
                    ;
                }
            }
            return this.argDef;
        }
    }

    /**
     * @author hindlem
     */
    @SuppressWarnings("serial")
    public class DuplicateOndexPluginsIdsException extends PluginLoadingException {

        /**
         * @param error
         */
        public DuplicateOndexPluginsIdsException(String error) {
            super(error);
        }
    }

    @SuppressWarnings("serial")
    public class UnregisteredPluginException extends PluginLoadingException {
        /**
         * @param error
         */
        public UnregisteredPluginException(String error) {
            super(error);
        }
    }

    @SuppressWarnings("serial")
    public class PluginLoadingException extends Throwable {
        /**
         * @param error
         */
        public PluginLoadingException(String error) {
            super(error);
        }
    }

    public ClassLoader getClassLoader() {
        return ucl;
    }

    private Map<String, Integer> usedNames = new HashMap<String, Integer>();
    private Map<String, Map<String, List<String>>> dups = new HashMap<String, Map<String, List<String>>>();

    private String processName(String name, String cls, String jarName) {
        Integer count = usedNames.get(name);
        if (count == null) {
            usedNames.put(name, 1);
            if (LIST_DUPLICATE_NAMES) {
                List<String> list = new LinkedList<String>();
                list.add(cls);
                Map<String, List<String>> map = new HashMap<String, List<String>>();
                map.put(jarName, list);
                dups.put(name, map);
            }
            return firstUpperCase(name);

        } else {

            usedNames.put(name, ++count);
            if (LIST_DUPLICATE_NAMES) {
                Map<String, List<String>> map = dups.get(name);
                List<String> list = map.get(jarName);
                if (list == null) {
                    list = new LinkedList<String>();
                    map.put(jarName, list);
                }
                list.add(cls);
            }
            return firstUpperCase(name + " ver." + count);
        }
    }

    private void listDuplicateNames() {
        for (Entry<String, Integer> ent : usedNames.entrySet()) {
            if (ent.getValue() > 1) {
                System.out.println("Duplicate name: " + ent.getKey());
                Map<String, List<String>> map = dups.get(ent.getKey());
                for (Entry<String, List<String>> ent1 : map.entrySet()) {
                    System.out.println("\t" + ent1.getKey());
                    for (String s : ent1.getValue()) {
                        System.out.println("\t\t" + s);
                    }
                }
            }
        }
        dups.clear();
    }

    private String firstUpperCase(String s) {
        Character c = s.charAt(0);
        if (!Character.isUpperCase(c)) {
            return s.replaceFirst(c.toString(), String.valueOf(Character.toUpperCase(c)));
        }
        return s;
    }

}
