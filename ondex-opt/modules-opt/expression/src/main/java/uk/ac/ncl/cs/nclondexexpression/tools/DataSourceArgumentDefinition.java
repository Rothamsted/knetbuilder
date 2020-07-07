
package uk.ac.ncl.cs.nclondexexpression.tools;

/**
 *
 * @author gambiting
 */


import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.AbstractArgumentDefinition;
import net.sourceforge.ondex.core.DataSource;
public class DataSourceArgumentDefinition extends AbstractArgumentDefinition<DataSource> {

    

    /**
     * Constructor which fills all internal fields.
     *
     * @param name                     String
     * @param description              String
     * @param required                 boolean
     */
    public DataSourceArgumentDefinition(String name, String description,
                                  boolean required) {
        super(name, description, required, false);

    }

    
    /**
     * Returns associated java class.
     *
     * @return Class
     */
    @Override
    public Class<DataSource> getClassType() {
        return DataSource.class;
    }

    /**
     * Returns default value.
     *
     * @return null
     */
    public DataSource getDefaultValue() {
        return null;
    }

    /**
     * Checks for valid argument.
     *
     * @return boolean
     */
    public void isValidArgument(Object obj) throws InvalidPluginArgumentException {
        return ;
        //throw new InvalidPluginArgumentException("A " + getName() + " argument is required to be specified as a String for " + this.getName() + " class " + obj.getClass().getName() + " was found ");
        
    }

    @Override
    public DataSource parseString(String argument) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
    
}

    


