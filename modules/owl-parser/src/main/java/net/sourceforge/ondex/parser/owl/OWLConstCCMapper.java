package net.sourceforge.ondex.parser.owl;

import org.apache.jena.ontology.OntModel;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.utils.ConceptClassPrototype;
import net.sourceforge.ondex.parser.ConstantConceptClassMapper;

/**
 * Just a wrapper of {@link ConstantConceptClassMapper}
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 Jun 2017</dd></dl>
 *
 */
public class OWLConstCCMapper extends OWLConceptClassMapper
{
	ConstantConceptClassMapper<OntModel> delegate = new ConstantConceptClassMapper<> ();

	public ConceptClass map ( OntModel source, ONDEXGraph graph )
	{
		return delegate.map ( source, graph );
	}

	public ConceptClassPrototype getValue ()
	{
		return delegate.getValue ();
	}

	public void setValue ( ConceptClassPrototype value )
	{
		delegate.setValue ( value );
	}

}
