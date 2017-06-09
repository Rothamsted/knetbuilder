package net.sourceforge.ondex.core.utils;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.base.DataSourceImpl;
import net.sourceforge.ondex.exception.type.NullValueException;

/**
 * These prototypes are a quick way to prepare ONDEX entities to be created later (when a {@link ONDEXGraph} is available, 
 * with the necessary parameters. They're also designed to be used with IoC frameworks like Spring.
 * 
 * TODO: move to core, as most of the classes in this package.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>8 May 2017</dd></dl>
 *
 */
public class DataSourcePrototype extends DescribeablePrototype 
{
	public static final DataSourcePrototype OWL_PARSER = new DataSourcePrototype ( "owlParser", "The OWL Parser", "" );
	
	private String id; 

	public DataSourcePrototype () {
		this ( null, null, null );
	}

	public DataSourcePrototype ( String id, String fullName, String description ) 
	{
		super ( id, fullName, description );
	}
}
