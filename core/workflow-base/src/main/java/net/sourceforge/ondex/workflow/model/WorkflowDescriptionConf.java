package net.sourceforge.ondex.workflow.model;

import net.sourceforge.ondex.ONDEXPlugin;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import net.sourceforge.ondex.init.ArgumentDescription;
import net.sourceforge.ondex.workflow.engine.*;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration for TaskDescription.
 *
 * @author Matthew Pocock
 * @author lysenkoa
 */
public class WorkflowDescriptionConf
{
	public static interface ArgumentParserType{
		public static final String CONFIGURATION = "pluginArgument";
		public static final String OBJECT = "default";
	}
	
    private static Logger LOG = Logger.getLogger(WorkflowDescriptionConf.class);

    private static class ConfHolder {
        public static WorkflowDescriptionConf instance = new WorkflowDescriptionConf();
    }

    public static WorkflowDescriptionConf instance() {
        return ConfHolder.instance;
    }

    private final Map<String, Class<? extends ArgumentParser>> typeToAM = new HashMap<String, Class<? extends ArgumentParser>>();

    private WorkflowDescriptionConf() {
        typeToAM.put(ArgumentParserType.CONFIGURATION, WorkflowDescriptionConf.PluginArgMaker.class);
    }

    
    public Processor getPluginProcessor(WorkflowTask ct) throws ClassNotFoundException{
        PluginProcessor p = null;
        p = new PluginProcessor(Thread.currentThread().getContextClassLoader().loadClass(ct.getPluginDescription().getCls()).asSubclass(ONDEXPlugin.class), ct);
        return p;
    }

    WorkflowDescriptionConf.ArgumentParser argParserForArgumentDescription(ArgumentDescription ab)
            throws InstantiationException, IllegalAccessException
    {
        Class<? extends ArgumentParser> amc = typeToAM.get(ab.getParser());
        if(amc == null) amc = WorkflowDescriptionConf.StandardArgMaker.class;
        return amc.newInstance();
    }

    /**
     * @param pc  PluginConfiguration to check
     * @return values for arguments
     * @throws Exception 
     */
    public Object[] parseArgumentsForChecking(WorkflowTask pc) throws Exception{
    	/**
     	    for (ArgDefValuePair at : t.getArgs()) {
            ArgumentDescription ab = at.getArg();
            TaskDescriptionConf.ArgumentParser argParser = ams.get(ab.getInputId());
            LOG.debug("Processing argument #" + ab.getInputId() + ": " + at.getArg().getName());
            if(ab.isInputObject()){
                if (argParser == null) {
                    try {
                        argParser = tdc.argParserForArgumentDescription(ab);
                        ams.put(ab.getInputId(), argParser);
                    } catch (Exception e) {
                        LOG.warn("Problem initialising parser. Skipping", e);
                    }
                    if(DEBUG){
                        debugInputMap.put(ab.getInputId(), at);
                    }
                }
                argParser.addArgument(at);
            }
            if (ab.isOutputObject()) {
                outputs.put(ab.getOutputId(), at);
                if(DEBUG){
                    debugOutputMap.put(ab.getOutputId(), at);
                }
            }
        }
    	*/
        Map<Integer, ArgumentParser> ams = new HashMap<Integer, ArgumentParser>();
        for (BoundArgumentValue at : pc.getArgs()) {
            ArgumentDescription ab = at.getArg();
            if(ab == null) throw new NullPointerException("No argument bean on " + at + " for " + pc);
            if(ab.isConfigurationArgument()){
            	ArgumentParser argParser = ams.get(ab.getInputId());
                System.err.println(ab.getInputId()+" : "+ab.getInteranlName()+" : "+ab.getParser()+" : "+(typeToAM.get(ab.getParser()) == null));
                if (argParser == null) {
                    try {
                        argParser = (typeToAM.get(ab.getParser()) == null
                                ? StandardArgMaker.class.newInstance()
                                : typeToAM.get(ab.getParser()).newInstance());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ams.put(ab.getInputId(), argParser);
                }
                argParser.addArgument(at);	
            }
        }

        int l = 0;
        for (Integer i : ams.keySet()) {
            l = Math.max(l, i);
        }

        Object[] values = new Object[l+1];
        for (Map.Entry<Integer, ArgumentParser> ent : ams.entrySet()) {
            Object value = ent.getValue().convertFor(pc.getPluginDescription().getCls());
            values[ent.getKey()] = value;
        }
        return values;
    }

    //static enum ResourcePlaceholder {
    //     DEFAULT
    //}
    
    public WorkflowDescriptionConf.PluginArgMaker getPluginArgMaker(){
    	return new WorkflowDescriptionConf.PluginArgMaker();
    }

    static interface ArgumentParser {
        public void addArgument(BoundArgumentValue ab);

        public Object convertFor(String pluginClass) throws PluginConfigurationException, Exception;

        public String getStringValue();
    }

    /**
     *
     */
    static interface ComponentParser {
        public Processor parseTemplate(WorkflowTask ct) throws Exception;
    }

    /**
     *
     */
    static class StandardArgMaker implements ArgumentParser {
        private String strValue = null;
        private Object resource = null;

        public StandardArgMaker() {
        }

        public void addArgument(BoundArgumentValue ab) {
            if (strValue == null) strValue = ab.getValue();
            String rAsStr;
            if (ab.getValue() != null && ab.getValue().length() > 0) {
                rAsStr = ab.getValue();
            } else return;

            // this is the case for AbstractONDEXGraph being passed from plugin to plugin
            //if (ab.getArg().isInputObject() || ab.getArg().isOutputObject()) {
            //  resource = ResourcePlaceholder.DEFAULT;
            //    return;
            //}
            
            if (ab.getArg().getCls().equals("java.lang.String") && ab.getValue().length() != 0) {
                resource = rAsStr;
            } else {
                try {
                	// this is the case for Double or other argument classes
                    resource = Thread.currentThread().getContextClassLoader().loadClass(ab.getArg().getCls()).getMethod("valueOf", String.class).invoke(null, rAsStr);
                } catch (Exception e) {
                    LOG.warn("Problem invoking method", e);
                }
            }
        }

        @Override
        public Object convertFor(String pClas) {
            return resource;
        }

        @Override
        public String getStringValue() {
            return strValue;
        }
    }

    /**
     *
     */
    static class PluginArgMaker implements ArgumentParser {
        private String strValue = null;
        private String cls = null;
        private List<BoundArgumentValue> rawResource = new ArrayList<BoundArgumentValue>();

        public PluginArgMaker() {
        }

        public void addArgument(BoundArgumentValue ab) {
            rawResource.add(ab);
            if (cls == null) cls = ab.getArg().getCls();
            if (strValue == null) strValue = ab.getValue();
        }

        public Object convertFor(String pluginClass) throws Exception
        {
            List<ValuePair<String, String>> value = new ArrayList<ValuePair<String, String>>();
            for (BoundArgumentValue ab : rawResource) {
                if (ab.getValue() != null) {
                    System.err.println(ab.getArg().getInputId() + "::" + ab.getArg().getName() + "::" + ab.getArg().getParser());
                    value.add(new ValuePair<String, String>(ab.getArg().getInteranlName(), ab.getValue()));
                }
            }
            if(cls == null){
            	cls = ONDEXPluginArguments.class.getCanonicalName(); 
            }
            return Engine.process(value, cls, pluginClass);
        }

        @Override
        public String getStringValue() {
            return strValue;
        }
    }

    /**
     *
     */
    static class FunctionMaker implements ComponentParser {
        public FunctionMaker() {
        }

        public Processor parseTemplate(WorkflowTask ct) throws Exception {
        	
            StandardProcessor s = null;
                if (ct.getPluginDescription().getMethodArgs() == null) {
                    s = new StandardProcessor(Thread.currentThread().getContextClassLoader().loadClass(ct.getPluginDescription().getCls()), ct.getPluginDescription().getMethod(), ct.getPluginDescription().getName());
                } else {
                    //Thread.currentThread().getContextClassLoader().loadClass(
                    Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(ct.getPluginDescription().getCls());
                    String[] argTypes = ct.getPluginDescription().getMethodArgs().split(", *?");
                    Class<?>[] argCls = new Class<?>[argTypes.length];

                    for (int i = 0; i < argTypes.length; i++) {
                        String name = argTypes[i].trim();
                        try {
                            argCls[i] = Thread.currentThread().getContextClassLoader().loadClass(name);
                        }
                        catch (ClassNotFoundException e) {
                            if (name.equals("net.sourceforge.ondex.core.AbstractONDEXGraph")) {
                                name = "net.sourceforge.ondex.core.ONDEXGraph";
                                argCls[i] = Thread.currentThread().getContextClassLoader().loadClass(name);
                            }
                        }
                    }
                    Method m = null;
                    try {
                        m = c.getMethod(ct.getPluginDescription().getMethod(), argCls);
                    }
                    catch (Exception e) {
                        LOG.warn("Problem getting method", e);
                        throw e;
                    }
                    s = new StandardProcessor(m, ct);
                }
            return s;
        }
    }
}
