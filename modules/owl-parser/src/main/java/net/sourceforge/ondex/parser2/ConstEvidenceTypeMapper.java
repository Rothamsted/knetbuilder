package net.sourceforge.ondex.parser2;

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.utils.CachedGraphWrapper;
import net.sourceforge.ondex.core.utils.DataSourcePrototype;
import net.sourceforge.ondex.core.utils.EvidenceTypePrototype;

/**
 * Maps anything to constant {@link EvidenceType}, which can be defined in a Spring configuration file, by means of
 * {@link EvidenceTypePrototype}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Jul 2017</dd></dl>
 *
 */
public class ConstEvidenceTypeMapper<S> extends ConstMapper<S, EvidenceTypePrototype, EvidenceType>
{
	@Override
	public EvidenceType map ( S source, ONDEXGraph graph )
	{
		return CachedGraphWrapper
		.getInstance ( graph )
		.getEvidenceType ( this.getValue () );
	}
}
