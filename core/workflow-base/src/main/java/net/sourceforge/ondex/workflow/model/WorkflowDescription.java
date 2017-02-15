package net.sourceforge.ondex.workflow.model;

import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import net.sourceforge.ondex.init.ArgumentDescription;
import net.sourceforge.ondex.workflow.engine.OndexJob;
import net.sourceforge.ondex.workflow.engine.Processor;
import net.sourceforge.ondex.workflow.engine.Processor.Argument;
import net.sourceforge.ondex.workflow.model.WorkflowDescriptionConf.PluginArgMaker;

import org.apache.log4j.Logger;

import java.util.*;
import java.util.Map.Entry;

public class WorkflowDescription {
    private static final Logger LOG = Logger.getLogger(WorkflowDescription.class);
    public static final boolean DEBUG = true;
    private final Map<String, UUID> uuids = new HashMap<String, UUID>();
    private final List<WorkflowTask> components = new ArrayList<WorkflowTask>();
    private final Map<UUID, Object> globalResources = new HashMap<UUID, Object>();

    public WorkflowDescription() {}

    public void addResource(String strId, Object resource) {
        UUID id = uuids.get(strId);
        if (id == null) {
            id = UUID.randomUUID();
            uuids.put(strId, id);
        }
        globalResources.put(id, resource);
    }

    public List<WorkflowTask> getComponents() {
        return components;
    }

    public int addPlugin(WorkflowTask pb) {
        components.add(pb);
        return (components.size() - 1);
    }

    public void remove(int position) {
        components.remove(position);
    }

    public void toOndexJob(final OndexJob job) throws Exception {
        for (WorkflowTask t : components) {
        	Processor we = configure(t);
            if(we != null) job.addTask(we);
        }
        for (Entry<UUID, Object> ent : globalResources.entrySet()) {
            job.addResource(ent.getKey(), ent.getValue());
        }
    }

    Processor configure(WorkflowTask t) throws Exception
    {
        if(t.getPluginDescription().getName() == null)
            throw new PluginConfigurationException("Can't configure a plugin with null name");
        WorkflowDescriptionConf tdc = WorkflowDescriptionConf.instance();
        
        Processor we;
        try{
            we = tdc.getPluginProcessor(t);
        }
        catch(Exception e){
            LOG.error("Could not load "+t.getPluginDescription().getName()+" plug-in was skipped.", e);
            throw new PluginConfigurationException("Could not load " + t.getPluginDescription().getName(), e);
        }

        
        LOG.debug(t.getPluginDescription().getName() + " number of arguments: " + t.getArgs().size());

        Map<Integer, WorkflowDescriptionConf.ArgumentParser> ams = new HashMap<Integer, WorkflowDescriptionConf.ArgumentParser>();
        if(t.getPluginDescription().getOndexType() != null){// All Ondex plug-ins need an ONDEXPluginArguments object - even if it is empty
        	PluginArgMaker pam = WorkflowDescriptionConf.instance().getPluginArgMaker();
        	ams.put(0, pam);
        }
        
        UUID [] outputArguments = new UUID[t.getExpectedNumberOfOutputs()];
        Argument[] inputArguments = new Argument[t.getExpectedNumberOfInputs()];
       
        for (BoundArgumentValue at : t.getArgs()) {
            ArgumentDescription ab = at.getArg();
            WorkflowDescriptionConf.ArgumentParser argParser = ams.get(ab.getInputId());
            LOG.debug("Processing argument #" + ab.getInputId() + ": " + at.getArg().getName());
            if(ab.isConfigurationArgument()){//This argument is a configuration argument and should be passed by value
                if (argParser == null) {
                    try {
                        argParser = tdc.argParserForArgumentDescription(ab);// Argument parser does the convertion to the correct value type and packages ONDEXPluginArguments into the correct object 
                        ams.put(ab.getInputId(), argParser);
                    } catch (Exception e) {
                        LOG.warn("Problem initialising parser. Skipping", e);
                    }
                }
                argParser.addArgument(at);
            }
            else if(ab.isInputObject()){//This is an input object that will be supplied by the workflow
            	UUID inputId = getUUIDForLocalId(at.getValue());
            	try{
            		safeAssignment(inputArguments, ab.getInputId(), new Argument(null, inputId));	
            	}
            	catch(IllegalStateException e){
            		throw new IllegalStateException("["+t.getPluginDescription().getOndexId()+"]"+e.getMessage()+" for inputs.");
            	}
            		
            }
            else if (ab.isOutputObject()) {//This is an output object produced by the plug-in
            	UUID outputId = getUUIDForLocalId(at.getValue());
            	try{
            		safeAssignment(outputArguments, ab.getOutputId(), outputId);
            	}
            	catch(IllegalStateException e){
            		throw new IllegalStateException("["+t.getPluginDescription().getOndexId()+"]"+e.getMessage()+" for outputs.");
            	}
            }
        }
        
        /**
        This should be impossibe now 
        if(ams.isEmpty()) {
            throw new PluginConfigurationException("No arguments found");
        }
        */
        
        int l = -1;
        for (Integer i : ams.keySet()) {
            l = Math.max(l, i);
        }

        /** This check will not work, because optional arguments may be missing and
         *  in that case null value should go into the array
        if(ams.size() != inputArguments.length) {
            throw new PluginConfigurationException("Inputs, ams and max index do not match: "
                    + inputArguments.length + ":" + ams.size() + ":" + l);
        }*/
        
        //Packaging of configuration arguments
        for (Entry<Integer, WorkflowDescriptionConf.ArgumentParser> ent : ams.entrySet()) {
            Object value = ent.getValue().convertFor(t.getPluginDescription().getCls());
        	try{
        		safeAssignment(inputArguments, ent.getKey(), new Argument(value, null));	
        	}
        	catch(IllegalStateException e){
        		throw new IllegalStateException("["+t.getPluginDescription().getOndexId()+"]"+e.getMessage()+" for inputs.");
        	}
        }

        we.configure(inputArguments, outputArguments);
        return we;
    }
    
    /**
     * This method checks that no arguments are over-writing any other arguments. This is
     * the most common symptom of error in the workflow. Currently all configuration arguments (i.e. stuff from ONDEXPluginArguments)
     * are packaged into one container and should all have the same inputId  of 0. All other input
     * objects can occupy any other non-overlapping positions in the input array (>=1)
     * 
     * Positions in the output array is determined by outputId. This id is not related to inputId and
     * can have any unique position number in array (>=0)
     * 
     * Filters and producers are currently the only plug-ins that produce outputs. 
     * 
     * @param array
     * @param position
     * @param value
     * @throws IllegalStateException
     */
    private static void safeAssignment(Object[] array, int position, Object value) throws IllegalStateException{
    	if(array[position] != null){
    		throw new IllegalStateException("An attempt to override the arguement in position "+position+". Check the argument definitions ");
    	}
   		array[position] = value;	
    }

    /**
     * Matches the local id that is readable by user and has workflow scope to
     * the one which is guaranteed to be unique and resides in the global scope
     *
     * @param localId - id displayed to the user
     * @return - id used internally by the workflow
     */
    private UUID getUUIDForLocalId(String localId){
        UUID uid = uuids.get(localId);
        if (uid == null) {
            uid = UUID.randomUUID();
            uuids.put(localId, uid);
        }
        return uid;
    }
}