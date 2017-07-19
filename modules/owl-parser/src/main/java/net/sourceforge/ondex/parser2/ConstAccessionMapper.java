package net.sourceforge.ondex.parser2;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.utils.AccessionPrototype;
import net.sourceforge.ondex.core.utils.CachedGraphWrapper;
import net.sourceforge.ondex.core.utils.DataSourcePrototype;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Jul 2017</dd></dl>
 *
 */
public class ConstAccessionMapper<S> 
  extends ConstPairMapper<S, ONDEXConcept, AccessionPrototype, ConceptAccession>
  implements AccessionMapper<S>
{

	@Override
	public ConceptAccession map ( S source, ONDEXConcept concept, ONDEXGraph graph )
	{
		return CachedGraphWrapper
		.getInstance ( graph )
		.getAccession ( this.getValue (), concept );
	}
}
