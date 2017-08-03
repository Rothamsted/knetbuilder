package net.sourceforge.ondex.parser.owl;

import org.apache.jena.ontology.OntClass;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.parser2.ConceptClassMapper;
import net.sourceforge.ondex.parser2.DefaultConceptClassMapper;
import net.sourceforge.ondex.parser2.HoldingMapper;
import net.sourceforge.ondex.parser2.Mapper;
import net.sourceforge.ondex.parser2.TextMapper;

/**
 * TODO: comment me!
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
