package net.sourceforge.ondex.workflow.validation;

import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import net.sourceforge.ondex.init.ArgumentDescription;
import net.sourceforge.ondex.init.PluginDescription;
import net.sourceforge.ondex.init.PluginType;
import net.sourceforge.ondex.workflow.model.BoundArgumentValue;
import net.sourceforge.ondex.workflow.model.WorkflowTask;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Check that PluginValidator is working as expected.
 * 
 * @author Matthew Pocock
 */
public class PluginValidatorTest
{
    private PluginValidator validator;
    private PluginDescription pb;
    private ArgumentDescription ab = new ArgumentDescription();


    @Before
    public void initValidator() {
        validator = new PluginValidator();
    }

    @Before
    public void initPluginBean() {
        pb = new PluginDescription();
        pb.setCls(String.class.getName());
        pb.setDescription("Dummy required argument");
        pb.setName("Required");
        pb.setOndexType(PluginType.PARSER);
        pb.setVersion("0.0");
        pb.setOndexId("required");

        ab = new ArgumentDescription();
        ab.setDescription("Required argument");
        ab.setName("requiredArg");
        ab.setCls("java.lang.String");
        ab.setDefaultValue(null);
        ab.setInputId(0);
        ab.setIsRequired(true);

        pb.setArgDef(new ArgumentDescription[] { ab });
    }

    @Test
    public void testRequiredButNull() throws PluginConfigurationException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException
    {
        // required argument, value set to null
        List<BoundArgumentValue> args = Arrays.asList(new BoundArgumentValue(ab, null));
        WorkflowTask pc = new WorkflowTask(pb, args);
        List<ErrorReport> errors = validator.check(pc, 0);
        System.out.println("Errors for present but null argument def value pair: " + errors);
        assertNotNull("Errors must not be null", errors);
        assertEquals("There must be one error, indicating that a required argument is missing", 1, errors.size());
    }

    @Test
    public void testRequiredAbsent() throws PluginConfigurationException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException
    {
        // required argument, value missing
        List<BoundArgumentValue> args = Collections.emptyList();
        WorkflowTask pc = new WorkflowTask(pb, args);
        List<ErrorReport> errors = validator.check(pc, 0);
        System.out.println("Errors for absent argument def value pair: " + errors);
        assertNotNull("Errors must not be null", errors);
        assertEquals("There must be one error, indicating that a required argument is missing", 1, errors.size());
    }

    @Test
    public void testRequiredPresent() throws PluginConfigurationException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException
    {
        // required argument, value present
        List<BoundArgumentValue> args = Arrays.asList(new BoundArgumentValue(ab, "bob"));
        WorkflowTask pc = new WorkflowTask(pb, args);
        List<ErrorReport> errors = validator.check(pc, 0);
        System.out.println("No errors for required argument that is present: " + errors);
        assertNotNull("Errors must not be null", errors);
        assertEquals("There must be no error", 0, errors.size());
    }
}
