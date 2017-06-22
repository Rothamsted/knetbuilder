package net.sourceforge.ondex.core.utils;

import net.sourceforge.ondex.core.ConceptClass;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 Jun 2017</dd></dl>
 *
 */
public class ConceptClassPrototype extends DescribeablePrototype
{
	private ConceptClass parent;	
	private ConceptClassPrototype parentPrototype;
	
	public ConceptClassPrototype ()
	{
		super ();
	}
	
	public ConceptClassPrototype ( String id, String fullName, String description, ConceptClassPrototype parentProto )
	{
		super ( id, fullName, description );
		this.setParentPrototype ( parentProto );
	}

	public ConceptClassPrototype ( String id, String fullName, String description, ConceptClass parent )
	{
		super ( id, fullName, description );
		this.setParent ( parent );
	}

	
	public ConceptClass getParent ()
	{
		return parent;
	}

	public void setParent ( ConceptClass parent )
	{
		this.parent = parent;
	}

	public ConceptClassPrototype getParentPrototype ()
	{
		return parentPrototype;
	}

	public void setParentPrototype ( ConceptClassPrototype parentPrototype )
	{
		this.parentPrototype = parentPrototype;
	}
	
}
