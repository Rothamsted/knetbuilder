package net.sourceforge.ondex.init;

import net.sourceforge.ondex.ONDEXPlugin;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.exception.type.ONDEXConfigurationException;
import net.sourceforge.ondex.export.ONDEXExport;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.mapping.ONDEXMapping;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.transformer.ONDEXTransformer;
import net.sourceforge.ondex.validator.AbstractONDEXValidator;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * The Ondex producer loader provides methods to access all currently available
 * plugins from the <code>plugins/</code> directory.
 * <p/>
 * <h3>Example usage:</h3>
 * <p/>
 * <code>ONDEXPluginLoader opl = ONDEXPluginLoader.getInstance();</code><br/>
 * <code>assert(opl.getParserNames().contains("kegg") {</code><br/><br/>
 * <p/>
 * <code>ONDEXParser keggParser = opl.loadParser("kegg");</code><br/>
 * <code>keggParser.setONDEXGraph(graph);</code><br/>
 * <code>keggParser.setArguments(args);</code><br/>
 * <code>keggParser.start();</code><br/>
 *
 * @author Jochen Weile, M.Sc.
 */
@Deprecated
public class ONDEXPluginLoaderLegacy {

    /**
     * the class loader
     */
    private URLClassLoader ucl;

    public URLClassLoader getURLClassLoader() {
        return ucl;
    }

    /**
     * the producer directory name
     */
    public static final String PLUGIN_DIR = "plugins/";

    /**
     * sets of names of available plugins of respective type.
     */
    private Set<String> exporterNames, filterNames, mappingNames, parserNames,
            statisticNames, transformerNames, validatorNames;

    /**
     * singleton.
     */
    private static ONDEXPluginLoaderLegacy instance;

    /**
     * debug mode.
     */
    private static final boolean DEBUG = true;

    /**
     * singleton constructor.
     */
    private ONDEXPluginLoaderLegacy() {
        reload();
    }

    /**
     * singleton getter.
     *
     * @return the singleton instance of this class.
     */
    public static ONDEXPluginLoaderLegacy getInstance() {
        if (instance == null) {
            instance = new ONDEXPluginLoaderLegacy();
        }
        return instance;
    }

    /**
     * An enumeration of different producer types providing their package and class names.
     *
     * @author jweile
     */
    private enum Type {
        EXPORT("export", "Export"),
        FILTER("filter", "Filter"),
        MAPPING("mapping", "Mapping"),
        PARSER("parser", "Parser"),
        STATISTICS("statistics", "Statistics"),
        TRANSFORMER("transformer", "Transformer"),
        VALIDATOR("validator", "Validator");

        String pack, clazz;

        private Type(String pack, String clazz) {
            this.pack = pack;
            this.clazz = clazz;
        }
    }

    /**
     * rescans the plugins directory and registers all found plugins
     * with the class loader.
     *
     * @throws ONDEXConfigurationException
     */
    public void reload() throws ONDEXConfigurationException {
        //make sure directory exists.
        File pluginDir = new File(PLUGIN_DIR);
        if (!pluginDir.exists()) {
            throw new ONDEXConfigurationException("Plugin directory missing!");
        }

        //register urls
        Vector<URL> urls = new Vector<URL>();
        Set<String> classRegisterBuilder = new HashSet<String>();
        for (File child : pluginDir.listFiles()) {
            if (child.getName().endsWith(".jar")) {
                try {
                    urls.add(child.toURI().toURL());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    scanClasses(classRegisterBuilder, child);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //create class loader on the collection of urls
        //will search in the parent first
        ucl = new URLClassLoader(urls.toArray(new URL[urls.size()]), ClassLoader.getSystemClassLoader());

        Thread.currentThread().setContextClassLoader(ucl);

        StringBuilder classList = new StringBuilder();

        for (String classPath : classRegisterBuilder) {
            classList.append(classPath);
            classList.append(';');
        }

        if (classList.length() == 0) {
            // you're running ondex all, hopefully, otherwise something is wrong.
            System.out.println("You're running Ondex-all, hopefully you know what you're doing.");
        } else {
            classList.setLength(classList.length() - 1); //chop the final ;
        }
        //write found classes into appropriate sets.
        String classRegister = classList.toString();
        exporterNames = findInstances(classRegister, Type.EXPORT);
        filterNames = findInstances(classRegister, Type.FILTER);
        mappingNames = findInstances(classRegister, Type.MAPPING);
        parserNames = findInstances(classRegister, Type.PARSER);
        statisticNames = findInstances(classRegister, Type.STATISTICS);
        transformerNames = findInstances(classRegister, Type.TRANSFORMER);
        validatorNames = findInstances(classRegister, Type.VALIDATOR);
    }

    /**
     * debug method
     */
    private void printSet(String title, Set<String> set) {
        System.out.println(title + ":\n=========");
        for (String s : set) {
            System.out.println(s);
        }
        System.out.println();
    }

    /**
     * Finds instances of the given producer type in the given list of names.
     *
     * @param list the string
     * @param type
     * @return a Set of producer names of the given types.
     */
    private Set<String> findInstances(String list, Type type) {


        Pattern p = Pattern.compile("net/sourceforge/ondex/" + type.pack + "/(.+?)/" + type.clazz + "\\.class");
        Matcher m = p.matcher(list);
        Set<String> set = new HashSet<String>();
        while (m.find()) {
            String match = m.group(1);
            if (!match.contains("/")) {//hack to cope with insufficient regex
                set.add(match);
            }
        }
        if (DEBUG) {
            printSet(type.clazz, set);
        }
        return set;
    }

    /**
     * scans a given zip file for file entries and
     * appends them to the given string builder.
     *
     * @param b    the string builder
     * @param file a zip file
     */
    private void scanClasses(Set<String> b, File file) throws IOException {
        ZipFile zipfile = new ZipFile(file);
        Enumeration<? extends ZipEntry> entries = zipfile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            b.add(entry.getName());
        }
    }

    /**
     * loads the producer of the given type with the given name
     *
     * @param type the producer type
     * @param name the producer name
     * @return the producer
     */
    @SuppressWarnings("unchecked")
    private ONDEXPlugin loadPlugin(Type type, String name) {
        String classname = "net.sourceforge.ondex." + type.pack + "." + name + "." + type.clazz;
        try {
            Class<ONDEXPlugin> clazz = (Class<ONDEXPlugin>) ucl.loadClass(classname);
            ONDEXPlugin plugin = clazz.getConstructor().newInstance();
            return plugin;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * loads the exporter with the given name
     *
     * @param name the name of the exporter
     * @return the exporter
     */
    public ONDEXExport loadExport(String name) {
        ONDEXPlugin plugin = loadPlugin(Type.EXPORT, name);
        ONDEXExport e = (ONDEXExport) plugin;
        return e;
    }

    /**
     * loads the filter with the given name
     */
    public ONDEXFilter loadFilter(String name) {
        ONDEXPlugin plugin = loadPlugin(Type.FILTER, name);
        ONDEXFilter f = (ONDEXFilter) plugin;
        return f;
    }

    /**
     * loads the mapping with the given name
     */
    public ONDEXMapping loadMapping(String name) {
        ONDEXPlugin plugin = loadPlugin(Type.MAPPING, name);
        ONDEXMapping m = (ONDEXMapping) plugin;
        return m;
    }

    /**
     * loads the parser with the given name
     */
    public ONDEXParser loadParser(String name) {
        ONDEXPlugin plugin = loadPlugin(Type.PARSER, name);
        ONDEXParser p = (ONDEXParser) plugin;
        return p;
    }

    /**
     * loads the transformer with the given name
     */
    public ONDEXTransformer loadTransformer(String name) {
        ONDEXPlugin plugin = loadPlugin(Type.TRANSFORMER, name);
        ONDEXTransformer t = (ONDEXTransformer) plugin;
        return t;
    }

    /**
     * loads the validator with the given name.
     *
     * @param name
     * @return
     */
    public AbstractONDEXValidator loadValidator(String name) {
        String classname = "net.sourceforge.ondex." + Type.VALIDATOR.pack + "." + name + "." + Type.VALIDATOR.clazz;
        try {
            Class<AbstractONDEXValidator> clazz = (Class<AbstractONDEXValidator>) ucl.loadClass(classname);
            AbstractONDEXValidator validator = clazz.getConstructor().newInstance();
            return validator;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


    /**
     * @return the set of known exporter names
     */
    public Set<String> getExporterNames() {
        return BitSetFunctions.unmodifiableSet(exporterNames);
    }

    /**
     * @return the set of known filter names
     */
    public Set<String> getFilterNames() {
        return BitSetFunctions.unmodifiableSet(filterNames);
    }

    /**
     * @return the set of known mapping names
     */
    public Set<String> getMappingNames() {
        return BitSetFunctions.unmodifiableSet(mappingNames);
    }

    /**
     * @return the set of known parser names
     */
    public Set<String> getParserNames() {
        return BitSetFunctions.unmodifiableSet(parserNames);
    }

    /**
     * @return the set of known statistic names
     */
    public Set<String> getStatisticNames() {
        return BitSetFunctions.unmodifiableSet(statisticNames);
    }

    /**
     * @return the set of known transformer names
     */
    public Set<String> getTransformerNames() {
        return BitSetFunctions.unmodifiableSet(transformerNames);
    }

    /**
     * @return the set of known validator names
     */
    public Set<String> getValidatorNames() {
        return BitSetFunctions.unmodifiableSet(validatorNames);
    }

}
