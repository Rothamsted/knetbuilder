package net.sourceforge.ondex.core.util.prototypes;

/**
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
