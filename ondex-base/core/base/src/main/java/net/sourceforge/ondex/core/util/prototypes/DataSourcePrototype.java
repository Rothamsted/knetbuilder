package net.sourceforge.ondex.core.util.prototypes;

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
	public DataSourcePrototype () {
		this ( null, "", "" );
	}

	public DataSourcePrototype ( String id, String fullName, String description ) 
	{
		super ( id, fullName, description );
	}
}
