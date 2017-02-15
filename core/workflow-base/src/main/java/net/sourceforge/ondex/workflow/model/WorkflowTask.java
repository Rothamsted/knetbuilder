package net.sourceforge.ondex.workflow.model;

import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import net.sourceforge.ondex.init.ArgumentDescription;
import net.sourceforge.ondex.init.PluginDescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class stores information about the plug-in to be executed as part of the
 *  task. It holds a reference to the pulg-in description, and argument descriptions
 *  matched with corresponding values.
 * 
 */
public class WorkflowTask {
    private final PluginDescription pluginDescription;
    private final List<BoundArgumentValue> args;
    private int priority = 0;
    private String comment = null;

    public WorkflowTask(PluginDescription pluginDescription, List<BoundArgumentValue> args) {
        super();
        if(pluginDescription == null) throw new NullPointerException("Can't create a PluginConfiguration with a null PluginDescription");
        if(pluginDescription.getArgDef() == null) throw new NullPointerException("The getArgDef() on the plugin bean must not be null.");        
        if(args == null) throw new NullPointerException(("Can't create a PluginConfiguration with a null args List"));
        this.pluginDescription = pluginDescription;
        this.args = args;
    }

    public WorkflowTask(PluginDescription pluginDescription, BoundArgumentValue ... args) {
        this(pluginDescription, new ArrayList<BoundArgumentValue>(Arrays.asList(args)));
    }

    public PluginDescription getPluginDescription() {
        return pluginDescription;
    }

    public List<BoundArgumentValue> getArgs() {
        return args;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getPriority() {
        return priority;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void addArgument(String id, String value) throws PluginConfigurationException
    {
        args.add(new BoundArgumentValue(getDefForId(id), value));
    }

    private ArgumentDescription getDefForId(String id) throws PluginConfigurationException
    {
        for (ArgumentDescription ab : pluginDescription.getArgDef()) {
            if (ab.getInteranlName().equalsIgnoreCase(id)) {
                return ab;
            }
        }
        throw new PluginConfigurationException("Could not find argument '" + id + "' for " + pluginDescription.getName()
                + " among " + Arrays.asList(pluginDescription.getArgDef()));
    }
    
    public int getExpectedNumberOfInputs(){
        int l = 0;
        boolean hasInputs = false;
        //Iterate through all input ids and find the maximum
        for (ArgumentDescription ab : pluginDescription.getArgDef()) {
        	if(ab.isInputObject()){
        		hasInputs =true;
        		l = Math.max(l, ab.getInputId());	
        	}
        }
        
        if(hasInputs){
        	//as positions in array start at 0, add 1 to get the required array size
        	return l+1;	
        }
        //If it is an Ondex plugin, it has to have an ONDEXPlugiArguments -  even if it is empty.
        if(pluginDescription.getOndexType() != null){
       		return 1;
        }
        //if no inputs are required array size is 0
        return 0;
    }
    
    public int getExpectedNumberOfOutputs(){
        int l = 0;
        boolean hasOutputs = false;
        //Iterate through all output ids and find the maximum
        for (ArgumentDescription ab : pluginDescription.getArgDef()) {
        	if(ab.isOutputObject()){
        		hasOutputs = true;
        		//System.out.println(pluginDescription.getOndexId()+"::"+ab.getInteranlName()+"::"+ab.getOutputId());
        		l = Math.max(l, ab.getOutputId());	
        	}
        }
        if(hasOutputs){
        	//as positions in array start at 0, add 1 to get the required array size
        	return l+1;	
        }
      //if no inputs are required array size is 0
        return 0;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("PluginConfiguration");
        sb.append("{pluginDescription=").append(pluginDescription);
        sb.append(", args=").append(args);
        sb.append(", priority=").append(priority);
        sb.append(", comment='").append(comment).append('\'');
        sb.append('}');
        return sb.toString();
    }
}