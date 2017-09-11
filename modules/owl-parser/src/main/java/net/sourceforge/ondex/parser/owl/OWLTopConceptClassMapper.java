package net.sourceforge.ondex.parser.owl;

import org.apache.jena.ontology.OntClass;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.parser.ConceptClassMapper;
import net.sourceforge.ondex.parser.DefaultConceptClassMapper;
import net.sourceforge.ondex.parser.HoldingMapper;
import net.sourceforge.ondex.parser.Mapper;
import net.sourceforge.ondex.parser.TextMapper;

/**
 * Maps an OWL class that is on top of an ontology hierarchy. This is an {@link HoldingMapper}, since the initially
 * mapped class (a top-one) is retained for subsequent usage (typically, to assign the same ONDEX concept class
 * to all the concepts in the same root tree, as they are mapped from descendant classes).
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>3 Aug 2017</dd></dl>
 *
 */
public class OWLTopConceptClassMapper extends HoldingMapper<OntClass, ConceptClass>
  implements ConceptClassMapper<OntClass>
{	
	public OWLTopConceptClassMapper ()
	{
		super ( new DefaultConceptClassMapper<> () );
	}

	@SuppressWarnings ( { "unchecked", "rawtypes" } )
	private DefaultConceptClassMapper<OntClass> getMyBaseMapper ()
	{
		return (DefaultConceptClassMapper<OntClass>) (Mapper) this.getBaseMapper ();
	}
	
	
	public TextMapper<OntClass> getIdMapper ()
	{
		return this.getMyBaseMapper().getIdMapper ();
	}

	public void setIdMapper ( TextMapper<OntClass> idMapper )
	{
		this.getMyBaseMapper().setIdMapper ( idMapper );
	}

	public TextMapper<OntClass> getFullNameMapper ()
	{
		return this.getMyBaseMapper().getFullNameMapper ();
	}

	public void setFullNameMapper ( TextMapper<OntClass> fullNameMapper )
	{
		this.getMyBaseMapper().setFullNameMapper ( fullNameMapper );
	}

	public TextMapper<OntClass> getDescriptionMapper ()
	{
		return this.getMyBaseMapper().getDescriptionMapper ();
	}

	public void setDescriptionMapper ( TextMapper<OntClass> descriptionMapper )
	{
		this.getMyBaseMapper().setDescriptionMapper ( descriptionMapper );
	}

	public ConceptClassMapper<OntClass> getParentMapper ()
	{
		return this.getMyBaseMapper().getParentMapper ();
	}

	public void setParentMapper ( ConceptClassMapper<OntClass> parentMapper )
	{
		this.getMyBaseMapper().setParentMapper ( parentMapper );
	}
	
}
