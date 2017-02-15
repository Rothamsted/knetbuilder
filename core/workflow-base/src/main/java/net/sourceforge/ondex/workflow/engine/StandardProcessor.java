package net.sourceforge.ondex.workflow.engine;

import net.sourceforge.ondex.exception.type.PluginException;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author lysenkoa
 */
public class StandardProcessor extends AbstractProcessor {
    private static final Logger LOG = Logger.getLogger(AbstractProcessor.class);


    private static final Object[] NOARGS = new Object[0];
    private Method m;
    public static boolean DEBUG = true;

    private StandardProcessor(Object descriptionRef) {
        this.descriptionRef = descriptionRef;
    }

    public StandardProcessor(Method targetFunction, Object descriptionRef) {
        this(descriptionRef);
        this.m = targetFunction;
    }

    public StandardProcessor(Class<?> c, String methodName, Object descriptionRef) {
        this(descriptionRef);
        for (Method m : c.getMethods()) {
            if (m.getName().equals(methodName)) {
                this.m = m;
                LOG.debug("StandardProcessor - "+this.toString()+"method found: "+m.getName()+" class loader: "+Thread.currentThread().getContextClassLoader());
                return;
            }
        }
        
        throw new IllegalArgumentException("No method with name '" + methodName + "' found in class " + c.getCanonicalName());
    }

    @Override
    public UUID[] execute(ResourcePool rp) throws Exception {
        UUID[] result = super.execute(rp);
        LOG.debug("StandardProcessor - "+this.toString()+" class loader: "+Thread.currentThread().getContextClassLoader());
        try {
            if (result.length != 0) {
                if (m.getParameterTypes().length == 0) {
                    rp.putResource(result[0], m.invoke(null, NOARGS), false);
                } else {
                    rp.putResource(result[0], m.invoke(null, values), false);
                }
            } else {
                if (m.getParameterTypes().length == 0) {
                    m.invoke(null, NOARGS);
                } else {
                    m.invoke(null, values);
                }
            }
            //System.err.println(rp.getResource(result[0]));
            return result;
        }
        catch (IllegalAccessException e) {
            throw new PluginException("Could not execute processor for method: " + m.getName() + " with arguments: " + Arrays.asList(values), e);
        }
        catch (InvocationTargetException e) {
            throw new PluginException("Could not execute processor for method: " + m.getName() + " with arguments: " + Arrays.asList(values), e);
        }
        catch (ClassCastException e) {
            throw new PluginException("Could not execute processor for method: " + m.getName() + " with arguments: " + Arrays.asList(values), e);
        }
        catch (IllegalArgumentException e) {
            throw new PluginException("Could not execute processor for method: " + m.getName() + " with arguments: " + Arrays.asList(values), e);
        }
    }
}