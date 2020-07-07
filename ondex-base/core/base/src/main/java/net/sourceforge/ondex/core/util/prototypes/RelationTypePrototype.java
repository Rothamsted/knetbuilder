package net.sourceforge.ondex.core.util.prototypes;

import net.sourceforge.ondex.core.RelationType;

/**
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>8 May 2017</dd></dl>
 *
 */
public class RelationTypePrototype extends DescribeablePrototype
{	
	public static final RelationTypePrototype IS_A_PROTOTYPE = new RelationTypePrototype ( 
		"is_a", // id 
		"is a", // fullname
		"", // descr
		"", // inverseName
		true, // antisymmetric, 
		true, // reflexive, 
		false, // symmetic, 
		true, // transitive, 
		(RelationType) null // specialisationof 
	);
	
	private String id;
	private String inverseName;
	private boolean isSymmetric, isAntisymmetric, isReflexive, isTransitive;
	private RelationType parent;	
	private RelationTypePrototype parentPrototype;

	public RelationTypePrototype () {
		this ( "", "", "", "", false, false, false, false, (RelationTypePrototype) null );
	}

	public RelationTypePrototype ( 
		String id, String fullname, String description, String inverseName,
		boolean isAntisymmetric, boolean isReflexive, boolean isSymmetric, boolean isTransitive,
		RelationType specialisationOf )
	{
		super ( id, fullname, description );
		this.setInverseName ( inverseName );
		this.setAntisymmetric ( isAntisymmetric );
		this.setReflexive ( isReflexive );
		this.setSymmetric ( isSymmetric );
		this.setTransitive ( isTransitive );
		this.setParent ( specialisationOf );
	}

	public RelationTypePrototype ( 
		String id, String fullname, String description, String inverseName,
		boolean isAntisymmetric, boolean isReflexive, boolean isSymmetric, boolean isTransitiv,
		RelationTypePrototype specialisationOf )
	{
		this ( id, fullname, description, inverseName, isAntisymmetric, isReflexive, isSymmetric, isTransitiv, (RelationType) null );
		this.setParentPrototype ( specialisationOf );
	}
	
	
	public String getId () {
		return this.id;
	}
	
	public void setId ( String id ) {
		this.id = id;
	}

	public void setInverseName ( String inverseName )
	{
		this.inverseName = inverseName;
	}

	public void setReflexive ( boolean isReflexive )
	{
		this.isReflexive = isReflexive;
	}

	public void setSymmetric ( boolean isSymmetric )
	{
		this.isSymmetric = isSymmetric;
	}

	public RelationTypePrototype getParentPrototype ()
	{
		return parentPrototype;
	}

	public void setParentPrototype ( RelationTypePrototype parentPrototype )
	{
		this.parentPrototype = parentPrototype;
	}

	public boolean isAntisymmetric ()
	{
		return isAntisymmetric;
	}

	public void setAntisymmetric ( boolean isAntisymmetric )
	{
		this.isAntisymmetric = isAntisymmetric;
	}

	public boolean isTransitive ()
	{
		return isTransitive;
	}

	public void setTransitive ( boolean isTransitive )
	{
		this.isTransitive = isTransitive;
	}

	public RelationType getParent ()
	{
		return parent;
	}

	public void setParent ( RelationType parent )
	{
		this.parent = parent;
	}

	public String getInverseName ()
	{
		return inverseName;
	}

	public boolean isSymmetric ()
	{
		return isSymmetric;
	}

	public boolean isReflexive ()
	{
		return isReflexive;
	}	
	
}
