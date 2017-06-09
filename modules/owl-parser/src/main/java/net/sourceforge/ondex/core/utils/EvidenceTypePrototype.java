package net.sourceforge.ondex.core.utils;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.base.EvidenceTypeImpl;
import net.sourceforge.ondex.exception.type.NullValueException;

/**
 * These prototypes are a quick way to prepare ONDEX entities to be created later (when a {@link ONDEXGraph} is available, 
 * with the necessary parameters. They're also designed to be used with IoC frameworks like Spring.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>8 May 2017</dd></dl>
 *
 */
public class EvidenceTypePrototype extends DescribeablePrototype 
{
	/** "Imported from Database", it is often used as default. */
	public static final EvidenceTypePrototype IMPD = new EvidenceTypePrototype ( "IMPD", "IMPD", "Imported from Database" );

	public EvidenceTypePrototype () {
		this ( "", "", "" );
	}

	public EvidenceTypePrototype ( String id, String fullname, String description ) 
	{
		super ( id, fullname, description );
	}
}
