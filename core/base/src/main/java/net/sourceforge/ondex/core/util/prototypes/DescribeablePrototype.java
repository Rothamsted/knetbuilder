package net.sourceforge.ondex.core.util.prototypes;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.util.CachedGraphWrapper;

/**
 * These prototypes are a quick way to prepare ONDEX entities to be created later (when a {@link ONDEXGraph} is available, 
 * with the necessary parameters. They're also designed to be used with IoC frameworks like Spring.
 * 
 * Use {@link CachedGraphWrapper} as a convenience wrapper to create ONDEX entities
 * from these constants.
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
