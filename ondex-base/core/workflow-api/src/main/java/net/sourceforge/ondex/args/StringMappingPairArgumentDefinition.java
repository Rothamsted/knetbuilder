package net.sourceforge.ondex.args;

import net.sourceforge.ondex.InvalidPluginArgumentException;

/**
 * Extension of StringArgumentDefinition of mapping pairs.
 *
 * @author taubertj
 */
public class StringMappingPairArgumentDefinition extends StringArgumentDefinition
{
	private String separator = ",";

	/**
	 * Constructor which fills all internal fields.
	 *
	 */
	public StringMappingPairArgumentDefinition ( 
		String name, String description, boolean required, String defaultValue, boolean multipleInstancesAllowed
	)
	{
		super ( name, description, required, defaultValue, multipleInstancesAllowed );
	}

	public StringMappingPairArgumentDefinition (
		String name, String description, boolean required, String defaultValue, boolean multipleInstancesAllowed,
		String separator 
	)
	{
		this ( name, description, required, defaultValue, multipleInstancesAllowed );
		this.separator = separator;
	}

	/**
	 * Checks for valid argument.
	 *
	 * @return boolean
	 */
	public void isValidArgument ( Object obj ) throws InvalidPluginArgumentException
	{
		if ( obj instanceof String )
		{
			String[] pair = ( (String) obj ).split ( this.getSeparator () );
			if ( pair.length == 2 && pair[ 0 ].length () > 0 && pair[ 1 ].length () > 0 )
			{
				return;
			}
			throw new InvalidPluginArgumentException ( String.format ( 
				"'%s' must be two items separated by '%s'", getName (), getSeparator ()
			));

		}
		throw new InvalidPluginArgumentException (
			"The argument is required to be specified as a String for " + getName ()
		);
	}

	public String getSeparator ()
	{
		return separator;
	}
}
