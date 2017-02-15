package net.sourceforge.ondex.tools.subgraph;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.ondex.core.util.ValueTuple;

/**
 * @author lysenkoa
 */
//TODO way to convoluted :( Port new version from Aug '09 branch.
public class AttributePrototype {
    public static final String DEFATTR = "ATTRIBUTE";
    public static final String DEFEVIDENCE = "EVIDENCE";
    public static final String DEFNAME = "NAME";
    public static final String DEFACC = "ACC";
    public static final String DEFDATASOURCE = "DATASOURCE";
    public static final String DEFCC = "CC";
    public static final String DEFRT = "RT";
    public static final String DEFPID = "PID";
    public static final String NUMBER = "NUMBER";
    public static final String TEXT = "TEXT";
    public static final String INTEGER = "INTEGER";
    public static final String SMILES = "SMILES";
    public static final String DOUBLE = "DOUBLE";
    public static final String OBJECT = "OBJECT";
    private static final Map<String, String[]> defaultPrototype = new HashMap<String, String[]>();

    static {
        defaultPrototype.put(DEFPID, new String[]{DEFPID, ""});
        defaultPrototype.put(DEFATTR, new String[]{DEFATTR, "Attribute", TEXT, "false", null});
        defaultPrototype.put(DEFEVIDENCE, new String[]{DEFEVIDENCE, "M"});
        defaultPrototype.put(DEFNAME, new String[]{DEFNAME, "true", null});
        defaultPrototype.put(DEFDATASOURCE, new String[]{DEFDATASOURCE, "UC"});
        defaultPrototype.put(DEFACC, new String[]{DEFACC, "false", "UC", null});
        defaultPrototype.put(DEFCC, new String[]{DEFCC, "Thing"});
        defaultPrototype.put(DEFRT, new String[]{DEFRT, "r"});
    }
    private String[] rawTypes = null;
    private String[] prot = null;
    private List<String[]> currentValues = new LinkedList<String[]>();
    private List<Integer[]> vectorToPrototype = new LinkedList<Integer[]>();
    private final Map<Integer, Map<String, String>> exactDictionary = new HashMap<Integer, Map<String, String>>();
    private final Map<Integer, Map<Pattern, String>> regexDictionary = new HashMap<Integer, Map<Pattern, String>>();
    private final Map<Integer, ValueTuple<Pattern, Integer>> regexExtractor = new HashMap<Integer, ValueTuple<Pattern, Integer>>();
    private int intialiser = Integer.MAX_VALUE;
    private Object[] userInput = null;
    private String split = null;

    public AttributePrototype(Object... args) {
        if (args.length == 0) {
            return;
        }
        if (args[0] instanceof String) {
            initialise((String) args[0], args);
        } else if (args[0] instanceof Integer) {
            intialiser = (Integer) args[0];
            userInput = args;
        }
    }

    public static Object[] getDefault(String id) {
        String[] tmp = defaultPrototype.get(id);
        if (tmp == null) {
            return null;
        }
        Object[] result = new Object[tmp.length];
        System.arraycopy(tmp, 0, result, 0, tmp.length);
        return result;
    }

    private void initialise(String attType, Object... args) {

        if (DEFATTR.equals(attType)) {
            if (args[4] == null) {
                args[4] = "false";
            }
        }

        String[] var = defaultPrototype.get(attType);
        if (var == null) {
            throw new RuntimeException(attType + " - this attribute type is invalid.");
        }
        prot = Arrays.copyOf(var, var.length);
        rawTypes = new String[prot.length];
        rawTypes[0] = attType;
        int min = Math.min(prot.length, args.length);
        for (int i = 1; i < min; i++) {
            if (args[i] == null) {
                continue;
            }
            if (args[i] instanceof String) {
                rawTypes[i] = (String) args[i];
                prot[i] = (String) args[i];
            } else if (args[i].getClass().equals(Integer.class)) {
                vectorToPrototype.add(new Integer[]{(Integer) args[i], i});
            }
        }
    }

    public String getType() {
        if (prot == null || prot.length == 0) {
            return null;
        }
        return prot[0];
    }

    public void addExactTranslation(int type, String term, String... translations) {
        Map<String, String> trMap = exactDictionary.get(type);
        if (trMap == null) {
            trMap = new HashMap<String, String>();
            exactDictionary.put(type, trMap);
        }
        for (String translation : translations) {
            trMap.put(translation, term);
        }
    }

    public void addRegexTranslation(int type, String term, String... translations) {
        Map<Pattern, String> trMap = regexDictionary.get(type);
        if (trMap == null) {
            trMap = new LinkedHashMap<Pattern, String>();
            regexDictionary.put(type, trMap);
        }
        for (String translation : translations) {
            trMap.put(Pattern.compile(translation), term);
        }
    }

    public void addRegexExtractor(int type, String regexPattern, int matchGroup) {
        regexExtractor.put(type, new ValueTuple<Pattern, Integer>(Pattern.compile(regexPattern), matchGroup));
    }

    public AttributePrototype extractWithRegex(String pattern, int matchGroup) throws Exception {
        if (prot == null || (!prot[0].equals(DEFATTR) && !prot[0].equals(DEFNAME) && !prot[0].equals(DEFACC))) {
            throw new Exception("Regex exraction can only be performed on ATTRIBUTE, NAME and ACC.");
        }
        regexExtractor.put(prot.length - 1, new ValueTuple<Pattern, Integer>(Pattern.compile(pattern), matchGroup));
        return this;
    }

    public AttributePrototype extractWithRegex(String pattern) throws Exception {
        return extractWithRegex(pattern, 0);
    }

    public void parse(String[] line) {
        if (intialiser < line.length) {
            String attType = line[intialiser];
            if (attType == null) {
                return;
            }
            userInput[0] = attType;
            initialise(attType, userInput);
        }
        if (prot == null) {
            return;
        }
        String[] currentValue = Arrays.copyOf(prot, prot.length);
        for (Integer[] pos : vectorToPrototype) {
            if (pos[0] >= line.length) {
                currentValue[pos[1]] = null;
            } else {
                currentValue[pos[1]] = translate(pos[1], line[pos[0]]);
            }
        }

        if (split != null) {
            for (Integer[] pos : vectorToPrototype) {
                String[] substrings = (line[pos[0]]).split(split);
                for (String s : substrings) {
                    String[] copyOfCurrentValue = Arrays.copyOf(currentValue, currentValue.length);
                    copyOfCurrentValue[pos[1]] = s;
                    currentValues.add(copyOfCurrentValue);
                }
            }
        } else {
            currentValues.add(currentValue);
        }
    }

    public List<String[]> getValue() {
        List<String[]> result = currentValues;
        currentValues = new ArrayList<String[]>();
        return result;
    }

    public String[] getDef() {
        return rawTypes;
    }

    private String translate(int type, String value) {
        if (value == null) {
            return null;
        }
        if (regexExtractor.containsKey(type)) {
            Matcher m = regexExtractor.get(type).getKey().matcher(value);
            if (m.find()) {
                return m.group(regexExtractor.get(type).getValue());
            }
            return null;
        }
        if (regexDictionary.containsKey(type)) {
            value = exactDictionary.get(type).get(value);
        }
        if (exactDictionary.containsKey(type)) {
            value = exactDictionary.get(type).get(value);
            for (Entry<Pattern, String> match : regexDictionary.get(type).entrySet()) {
                if (match.getKey().matcher(value).find()) {
                    value = match.getValue();
                    break;
                }
            }
        }
        return value;
    }

    public AttributePrototype split(String split) {
        this.split = split;
        return this;
    }
}
