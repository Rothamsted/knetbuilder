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
@SuppressWarnings ( "serial" )
public class DataSourcePrototype extends DataSourceImpl 
{
	public static final DataSourcePrototype OWL_PARSER = new DataSourcePrototype ( "owlParser", "The OWL Parser", "" );
	
	private String id; 

	public DataSourcePrototype ( String id, String fullname, String description ) 
	{
		super ( -1, id, fullname, description );
		this.setId ( id );
	}

	
	public DataSourcePrototype () {
		this ( "", "", "" );
	}

	public String getId () {
		return this.id;
	}
	
	public void setId ( String id ) {
		this.id = id;
	}


	@Override
	public void setFullname ( String fullname ) throws NullValueException, UnsupportedOperationException
	{
		this.fullname = fullname;
	}


	@Override
	public void setDescription ( String description ) throws NullValueException, UnsupportedOperationException
	{
		this.description = description;
	}	

}
