package net.sourceforge.ondex.core.utils;

import net.sourceforge.ondex.core.base.DataSourceImpl;
import net.sourceforge.ondex.exception.type.NullValueException;

/**
 * TODO: comment me!
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
