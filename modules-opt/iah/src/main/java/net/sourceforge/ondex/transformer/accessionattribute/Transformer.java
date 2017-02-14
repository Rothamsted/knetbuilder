package net.sourceforge.ondex.transformer.accessionattribute;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.InconsistencyEvent;
import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import net.sourceforge.ondex.tools.MetaDataUtil;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

import org.apache.log4j.Level;

public class Transformer extends ONDEXTransformer
{

    public static final String FILE_ARG = "File";
    public static final String FILE_ARG_DESC = "Input file";
    public static final String AN_ARG = "AttributeNames";
    public static final String AN_ARG_DESC = "Comma separated list of attribute names to use";
    public static final String ANT_ARG = "Attribute types";
    public static final String ANT_ARG_DESC = "Comma separated list of attribute types (Java classes) to use." +
            "Allowed classes are: java.lang.String, java.lang.Boolean, java.lang.Integer";
    public static final String CV_ARG = "DataSource";
    public static final String CV_ARG_DESC = "DataSource of accessions to use";

    private DataSource dataSource;

    private AttributeName[] ans;

    private Class<?>[] anClasses;

    private File file;

    private Map<String, List<Integer>> acc2cids = new HashMap<String, List<Integer>>();


    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new StringArgumentDefinition(FILE_ARG, FILE_ARG_DESC, true, null, false),
                new StringArgumentDefinition(AN_ARG, AN_ARG_DESC, true, null, false),
                new StringArgumentDefinition(ANT_ARG, ANT_ARG_DESC, true, null, false),
                new StringArgumentDefinition(CV_ARG, CV_ARG_DESC, true, null, false)
        };
    }

    @Override
    public String getName() {
        return "Accession attribute transformer";
    }

    @Override
    public String getVersion() {
        return "15.05.2009";
    }

    @Override
    public String getId() {
        return "accessionattribute";
    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws Exception {
        fetchArguments();

        indexAccessions();

//		Pattern yPatt = Pattern.compile("Y\\w{2}\\d{3}\\w{1}(\\-\\w)?");

        BufferedReader r = new BufferedReader(new FileReader(file));
        String line;
        int lineCount = 0;
        while ((line = r.readLine()) != null) {
            lineCount++;
            String[] cols = line.split("\t");
            if (cols.length == ans.length + 1) {
                String acc = cols[0].trim();
                for (ONDEXConcept c : conceptsForAccession(acc)) {
                    for (int i = 0; i < ans.length; i++) {
                        Object value = cast(anClasses[i], cols[i + 1]);
                        c.createAttribute(ans[i], value, false);
                    }
                }
            } else {
                logFail("One or more columns missing in line: " + lineCount);
            }
        }
    }

    private Object cast(Class<?> c, String s) throws ClassCastException {
        Object o = null;
        if (c.equals(Boolean.class)) {
            o = Boolean.parseBoolean(s);
        } else if (c.equals(Integer.class)) {
            o = Integer.parseInt(s);
        } else if (c.equals(String.class)) {
            o = s;
        } else if (c.equals(Double.class)) {
            o = Double.parseDouble(s);
        }
        return o;
    }

    private Collection<ONDEXConcept> conceptsForAccession(String acc) {
        List<Integer> ids = acc2cids.get(acc);
        ArrayList<ONDEXConcept> cs = new ArrayList<ONDEXConcept>();
        if (ids != null) {
            for (int id : ids) {
                cs.add(graph.getConcept(id));
            }
        }
        return cs;
    }

    private void indexAccessions() {
        for (ONDEXConcept c : graph.getConcepts()) {
            for (ConceptAccession acc : c.getConceptAccessions()) {
                if (acc.getElementOf().equals(dataSource)) {
                    List<Integer> cids = acc2cids.get(acc.getAccession());
                    if (cids == null) {
                        cids = new ArrayList<Integer>();
                        acc2cids.put(acc.getAccession(), cids);
                    }
                    cids.add(c.getId());
                    break;
                }
            }
        }
    }

    private void fetchArguments() throws PluginConfigurationException {
        MetaDataUtil mdu = new MetaDataUtil(graph.getMetaData(), null);
        String fileName = (String) args.getUniqueValue(FILE_ARG);
        File tempFile = new File(fileName);
        if (tempFile.isAbsolute()) {
            file = tempFile;
        } else {
            String prefix = Config.ondexDir.endsWith(File.separator) ?
                    Config.ondexDir :
                    Config.ondexDir + File.separator;
            file = new File(prefix + fileName);
        }

        if (!file.exists())
            throw new PluginConfigurationException("File does not exist: " + file.getAbsolutePath());

        Set<Class<?>> allowedClasses = new HashSet<Class<?>>();
        allowedClasses.add(Boolean.class);
        allowedClasses.add(Integer.class);
        allowedClasses.add(String.class);
        allowedClasses.add(Double.class);

        String anTypeList = (String) args.getUniqueValue(ANT_ARG);
        String[] anTypes = anTypeList.split(",");
        anClasses = new Class<?>[anTypes.length];
        try {
            for (int i = 0; i < anTypes.length; i++) {
                anClasses[i] = Class.forName(anTypes[i].trim());
                if (!allowedClasses.contains(anClasses[i])) {
                    throw new PluginConfigurationException("Class " + anClasses[i] + " is not allowed!");
                }
            }
        } catch (ClassNotFoundException e) {
            throw new PluginConfigurationException(e);
        }

        String anNameList = (String) args.getUniqueValue(AN_ARG);
        String[] anNames = anNameList.split(",");

        if (anNames.length != anClasses.length) {
            throw new PluginConfigurationException("Attribute names and types do not match!");
        }

        ans = new AttributeName[anNames.length];
        for (int i = 0; i < anNames.length; i++) {
            String anName = anNames[i].trim();
            ans[i] = mdu.safeFetchAttributeName(anName, anClasses[i]);
            if (!ans[i].getDataType().equals(anClasses[i])) {
                throw new PluginConfigurationException("Type " + anClasses[i] +
                        " incompatible with existing Attribute name " + ans[i]);
            }
        }

        String cvName = (String) args.getUniqueValue(CV_ARG);
        dataSource = mdu.safeFetchDataSource(cvName);
    }

    private void logFail(String s) {
        EventType e = new InconsistencyEvent(s, "");
        e.setLog4jLevel(Level.ERROR);
        fireEventOccurred(e);
    }

//	private void log(String s) {
//		EventType e = new GeneralOutputEvent(s,"");
//		e.setLog4jLevel(Level.INFO);
//		fireEventOccurred(e);
//	}

}
