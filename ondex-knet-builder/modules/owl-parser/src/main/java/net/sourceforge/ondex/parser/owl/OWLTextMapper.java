package net.sourceforge.ondex.parser.owl;

import org.apache.jena.ontology.OntClass;

import net.sourceforge.ondex.parser.DefaultTextMapper;
import net.sourceforge.ondex.parser.TextMapper;
import net.sourceforge.ondex.parser.TextsMapper;

/**
 * A {@link TextMapper} that maps the lexical value of an {@link #getPropertyIri() RDF property} to a string.
 * 
 * This is based on {@link OWLTextsMapper} and the {@link #getBaseMapper() base mapper} must be based on
 * {@link OWLTextsMapper}.  
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Jul 2017</dd></dl>
 *
 */
public class OWLTextMapper extends DefaultTextMapper<OntClass> 
	implements TextMapper<OntClass>, RdfPropertyConfigurator
{
	public OWLTextMapper () {
		this ( new OWLTextsMapper () );
	}
	
	public OWLTextMapper ( OWLTextsMapper baseMapper ) {
		super ( baseMapper );
	}

	@Override
	public String getPropertyIri () {
		return ((OWLTextsMapper) this.getBaseMapper ()).getPropertyIri ();
	}

	@Override
	public void setPropertyIri ( String propertyIri ) {
		((OWLTextsMapper) this.getBaseMapper ()).setPropertyIri ( propertyIri );
	}

	@Override
	public void setBaseMapper ( TextsMapper<OntClass> baseMapper )
	{
		if ( ! (baseMapper instanceof OWLTextsMapper ) ) throw new IllegalArgumentException (
		  String.format ( "%s must have a base mapper of type OWLTextsMapper", this.getClass ().getSimpleName () )
		);
		super.setBaseMapper ( baseMapper );
	}

	public void setBaseMapper ( OWLTextsMapper baseMapper )
	{
		this.setBaseMapper ( (TextsMapper<OntClass>) baseMapper );
	}
}
