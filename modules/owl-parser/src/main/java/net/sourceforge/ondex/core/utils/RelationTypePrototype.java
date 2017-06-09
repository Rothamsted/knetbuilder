package net.sourceforge.ondex.core.utils;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.base.RelationTypeImpl;
import net.sourceforge.ondex.exception.type.NullValueException;

/**
 * These prototypes are a quick way to prepare ONDEX entities to be created later (when a {@link ONDEXGraph} is available, 
 * with the necessary parameters. They're also designed to be used with IoC frameworks like Spring.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>8 May 2017</dd></dl>
 *
 */
@SuppressWarnings ( "serial" )
public class RelationTypePrototype extends RelationTypeImpl
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
		null // specialisationof 
	);
	
	private String id;
	private RelationTypePrototype parentPrototype;

	public RelationTypePrototype () {
		this ( "", "", "", "", false, false, false, false, null );
	}

	public RelationTypePrototype ( 
		String id, String fullname, String description, String inverseName,
		boolean isAntisymmetric, boolean isReflexive, boolean isSymmetric, boolean isTransitiv,
		RelationType specialisationOf )
	{
		super ( 
			-1, id, fullname, description, inverseName, isAntisymmetric, isReflexive, isSymmetric, isTransitiv,
			specialisationOf 
		);
		this.setId ( id );
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

	@Override
	public void setInverseName ( String inverseName ) throws NullValueException, UnsupportedOperationException
	{
		this.inverseName = inverseName;
	}

	@Override
	public void setAntisymmetric ( boolean isAntisymmetric ) throws UnsupportedOperationException
	{
		this.isAntisymmetric = isAntisymmetric;
	}

	@Override
	public void setReflexive ( boolean isReflexive ) throws UnsupportedOperationException
	{
		this.isReflexive = isReflexive;
	}

	@Override
	public void setSymmetric ( boolean isSymmetric ) throws UnsupportedOperationException
	{
		this.isSymmetric = isSymmetric;
	}

	@Override
	public void setTransitiv ( boolean isTransitiv ) throws UnsupportedOperationException
	{
		this.isTransitiv = isTransitiv;
	}

	@Override
	public void setSpecialisationOf ( RelationType specialisationOf ) throws UnsupportedOperationException
	{
		this.specialisationOf = specialisationOf;
	}

	@Override
	public void setFullname ( String fullname ) throws NullValueException, UnsupportedOperationException
	{
		this.fullname = fullname;
	}

	public RelationTypePrototype getParentPrototype ()
	{
		return parentPrototype;
	}

	public void setParentPrototype ( RelationTypePrototype parentPrototype )
	{
		this.parentPrototype = parentPrototype;
	}
}
