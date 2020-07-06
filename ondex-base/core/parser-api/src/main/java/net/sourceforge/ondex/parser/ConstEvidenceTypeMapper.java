package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.util.CachedGraphWrapper;
import net.sourceforge.ondex.core.util.prototypes.EvidenceTypePrototype;

/**
 * Maps anything to constant {@link EvidenceType}, which can be defined in a Spring configuration file, by means of
 * {@link EvidenceTypePrototype}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Jul 2017</dd></dl>
 *
 */
public class ConstEvidenceTypeMapper<S> extends ConstMapper<S, EvidenceTypePrototype, EvidenceType>
  implements EvidenceTypeMapper<S>
{
	public ConstEvidenceTypeMapper () {
		this ( null );
	}

	public ConstEvidenceTypeMapper ( EvidenceTypePrototype value ) {
		super ( value );
	}

	@Override
	public EvidenceType map ( S source, ONDEXGraph graph )
	{
		return CachedGraphWrapper
		.getInstance ( graph )
		.getEvidenceType ( this.getValue () );
	}
}
