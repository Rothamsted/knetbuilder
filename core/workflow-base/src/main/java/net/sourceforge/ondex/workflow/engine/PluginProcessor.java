package net.sourceforge.ondex.workflow.engine;

import net.sourceforge.ondex.ONDEXPlugin;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.export.ONDEXExport;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.mapping.ONDEXMapping;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.producer.ProducerONDEXPlugin;
import net.sourceforge.ondex.transformer.ONDEXTransformer;
import net.sourceforge.ondex.workflow.model.PluginAndArgs;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
/**
 * @author lysenkoa
 * 
 * 
 * Wraps all of the Ondex plug-in types in a generic processor interface. Extends the API by allowing
 * multiple inputs and outputs to be retured/supplied by the plug-in. The inputs and outputs are
 * supplied as an array of objects. For all Ondex plug-ins the order of inputs should be as follows:
 * 
 * 0 - ArgumetnDefinition
 * 1 - Ondex graph
 * 
 * 
 */
public class PluginProcessor extends AbstractProcessor {
    private Class<? extends ONDEXPlugin> pluginClass;

    private PluginProcessor(Object descriptionRef) {
        this.descriptionRef = descriptionRef;
    }

    public PluginProcessor(Class<? extends ONDEXPlugin> pluginClass, Object descriptionRef) {
        this(descriptionRef);
        this.pluginClass = pluginClass;
    }

    @Override
    public UUID[] execute(ResourcePool rp) throws Exception {
        //TODO this is a bit iffy with some assumptions...
        UUID[] result = super.execute(rp);


        ONDEXPlugin p;
        ONDEXPluginArguments args = null;
        try {
            p = pluginClass.getConstructor().newInstance();
        }
        catch (InstantiationException e) {
            throw new Exception("Failed to instantiate plugin for: " + pluginClass, e);
        }
        catch (IllegalAccessException e) {
            throw new Exception("Failed to instantiate plugin for: " + pluginClass, e);
        }
        catch (InvocationTargetException e) {
            throw new Exception("Failed to instantiate plugin for: " + pluginClass, e);
        }
        catch (NoSuchMethodException e) {
            throw new Exception("Failed to instantiate plugin for: " + pluginClass, e);
        }
        int inputPosition = 0;
        if (inputArguments[0].value != null && inputArguments[0].value instanceof PluginAndArgs) {
            PluginAndArgs paa = (PluginAndArgs) inputArguments[0].value;
            args = paa.getArguments();
            inputPosition++;
        }
        
        ONDEXGraph[] graphs;
        if(ProducerONDEXPlugin.class.isInstance(p)){
        	graphs = new ONDEXGraph[0];
        }
        else{
            graphs = new ONDEXGraph[2];

            for (int i = inputPosition; i < inputArguments.length; i++) {
                graphs[i - inputPosition] = (ONDEXGraph) inputArguments[i].value;
            }	
        }

        Object[] output = makeSimplePlugin(pluginClass).run(p, args, graphs);
        
        if(result.length != output.length)
        {
            throw new Exception("Unexpected number of outputs when running plugin " + p.getName()
                    + ". Expecting: " + result.length + " but found " + output.length);
        }

        for (int i = 0, resultLength = result.length; i < resultLength; i++) {
        	if(output[i] != null){
        		rp.putResource(result[i], output[i], false);	
        	}
        }
        return result;
    }

    interface SimplePlugin<P extends ONDEXPlugin, A extends ONDEXPluginArguments> {
        public Object [] run(P plugin, A arguments, ONDEXGraph[] graphs) throws Exception;
    }

    private static SimplePlugin makeSimplePlugin(Class<?> cls) throws Exception {
    	if(ProducerONDEXPlugin.class.isAssignableFrom(cls)){
            return new SimplePlugin<ProducerONDEXPlugin, ONDEXPluginArguments>() {
				public Object[] run(ProducerONDEXPlugin plugin, ONDEXPluginArguments arguments, ONDEXGraph[] graphs)throws Exception {
                    plugin.setArguments(arguments);
					return plugin.collectResults();
				}
            };	
    	} else if (ONDEXExport.class.isAssignableFrom(cls)) {
            return new SimplePlugin<ONDEXExport, ONDEXPluginArguments>() {
                public Object [] run(ONDEXExport exporter, ONDEXPluginArguments args, ONDEXGraph[] graphs) throws Exception {
                    Engine.getEngine().runExport(exporter, args, graphs[0]);
                    return new Object[0];
                }
            };
        } else if (ONDEXFilter.class.isAssignableFrom(cls)) {
            return new SimplePlugin<ONDEXFilter, ONDEXPluginArguments>() {
                public Object [] run(ONDEXFilter plugin, ONDEXPluginArguments args, ONDEXGraph[] graphs) throws Exception {
                    Object  newGraph = Engine.getEngine().runFilter(plugin, args, graphs[0], graphs[1]);
                   	return new Object[]{newGraph};
                }
            };
        } else if (ONDEXMapping.class.isAssignableFrom(cls)) {
            return new SimplePlugin<ONDEXMapping, ONDEXPluginArguments>() {
                public Object [] run(ONDEXMapping plugin, ONDEXPluginArguments args, ONDEXGraph[] graphs) throws Exception {
                    Engine.getEngine().runMapping(plugin, args, graphs[0]);
                    return new Object[0];
                }
            };
        } else if (ONDEXParser.class.isAssignableFrom(cls)) {
            return new SimplePlugin<ONDEXParser, ONDEXPluginArguments>() {
                public Object [] run(ONDEXParser parser, ONDEXPluginArguments args, ONDEXGraph[] graphs) throws Exception {
                    Engine.getEngine().runParser(parser, args, graphs[0]);
                    return new Object[0];
                }
            };
        } else if (ONDEXTransformer.class.isAssignableFrom(cls)) {
            return new SimplePlugin<ONDEXTransformer, ONDEXPluginArguments>() {
                public Object [] run(ONDEXTransformer transformer, ONDEXPluginArguments args, ONDEXGraph[] graphs) throws Exception {
                    Engine.getEngine().runTransformer(transformer, args, graphs[0]);
                    return new Object[0];
                }
            };
        }
        throw new Exception("Unknown plugin type");
    }

}
