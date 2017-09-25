package net.sourceforge.ondex.core.util.prototypes;

import net.sourceforge.ondex.core.ConceptClass;

/**
 * Facility to create a constant {@link ConceptClass}.
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
		this ( id, fullName, description );
		this.setParentPrototype ( parentProto );
	}

	public ConceptClassPrototype ( String id, String fullName, String description, ConceptClass parent )
	{
		this ( id, fullName, description );
		this.setParent ( parent );
	}

	public ConceptClassPrototype ( String id, String fullName, String description )
	{
		super ( id, fullName, description );
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
