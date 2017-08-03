package net.sourceforge.ondex.parser2;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.utils.CachedGraphWrapper;
import net.sourceforge.ondex.core.utils.ConceptClassPrototype;
import net.sourceforge.ondex.core.utils.DataSourcePrototype;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>3 Aug 2017</dd></dl>
 *
 */
public class ConstantConceptClassMapper<S> extends ConstMapper<S, ConceptClassPrototype, ConceptClass>
{

	public ConstantConceptClassMapper () {
		this ( null );
	}

	public ConstantConceptClassMapper ( ConceptClassPrototype value ) {
		super ( value );
	}

	@Override
	public synchronized ConceptClass map ( S source, ONDEXGraph graph ) {
		return CachedGraphWrapper.getInstance ( graph ).getConceptClass ( this.getValue () );
	}
	
}
