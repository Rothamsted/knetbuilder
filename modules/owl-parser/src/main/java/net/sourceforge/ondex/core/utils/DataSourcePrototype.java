package net.sourceforge.ondex.core.utils;

import net.sourceforge.ondex.core.ONDEXGraph;

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
	// TODO: it should not be here, it's OWL-specific
	public static final DataSourcePrototype OWL_PARSER = new DataSourcePrototype ( "owlParser", "The OWL Parser", "" );
	
	public DataSourcePrototype () {
		this ( null, null, null );
	}

	public DataSourcePrototype ( String id, String fullName, String description ) 
	{
		super ( id, fullName, description );
	}
}
