package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.utils.CachedGraphWrapper;
import net.sourceforge.ondex.core.utils.ConceptClassPrototype;

/**
 * Generate a constant CC, to map every concept a parser gets to a fixed concept class.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 Jun 2017</dd></dl>
 *
 */
public class ConstantConceptClassMapper<S> implements ConceptClassMapper<S>
{
	private ConceptClassPrototype value;
	
	@Override
	public synchronized ConceptClass map ( S source, ONDEXGraph graph )
	{
		return CachedGraphWrapper.getInstance ( graph ).getConceptClass ( this.getValue () );
	}

	/**
	 * This is the fixed CC you want, {@link ConceptClassPrototype} can be used here in a Spring config file (eg, see
	 * doid_cfg.xml) 
	 */
	public ConceptClassPrototype getValue ()
	{
		return value;
	}

	public void setValue ( ConceptClassPrototype value )
	{
		this.value = value;
	}

}
