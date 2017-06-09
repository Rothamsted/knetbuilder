package net.sourceforge.ondex.core.utils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>6 Jun 2017</dd></dl>
 *
 */
public abstract class DescribeablePrototype
{
	private String id, fullName, description;

	protected DescribeablePrototype ()
	{
		super ();
	}
	
	protected DescribeablePrototype ( String id, String fullName, String description )
	{
		super ();
		this.setId ( id );
		this.setFullName ( fullName );
		this.setDescription ( description );
	}



	public String getId ()
	{
		return id;
	}

	public void setId ( String id )
	{
		this.id = id;
	}

	public String getFullName ()
	{
		return fullName;
	}

	public void setFullName ( String fullName )
	{
		this.fullName = fullName;
	}

	public String getDescription ()
	{
		return description;
	}

	public void setDescription ( String description )
	{
		this.description = description;
	}
	
}
