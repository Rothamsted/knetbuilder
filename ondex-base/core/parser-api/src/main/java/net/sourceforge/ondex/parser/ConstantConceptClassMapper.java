package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.util.CachedGraphWrapper;
import net.sourceforge.ondex.core.util.prototypes.ConceptClassPrototype;

/**
 * Maps anything to a constant class mapper, which can be configured in Spring via {@link ConceptClassPrototype} passed
 * to {@link #setValue(Object)}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>3 Aug 2017</dd></dl>
 *
 */
public class ConstantConceptClassMapper<S> extends ConstMapper<S, ConceptClassPrototype, ConceptClass>
  implements ConceptClassMapper<S>
{

	public ConstantConceptClassMapper () {		
		this ( null );
	}

	public ConstantConceptClassMapper ( ConceptClassPrototype value ) {
		super ( value );
	}

	@Override
	public ConceptClass map ( S source, ONDEXGraph graph ) {
		return CachedGraphWrapper.getInstance ( graph ).getConceptClass ( this.getValue () );
	}
	
}
