package net.sourceforge.ondex.parser.owl;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.utils.CachedGraphWrapper;
import net.sourceforge.ondex.parser.ConceptClassMapper;
import net.sourceforge.ondex.parser.SimpleIdMapper;
import net.sourceforge.ondex.parser.SimpleLabelMapper;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Apr 2017</dd></dl>
 *
 */
public class OWLConceptClassMapper implements ConceptClassMapper<OntModel>
{
	private String classIri;
	
	private SimpleIdMapper<OntClass> idMapper;
	private SimpleLabelMapper<OntClass> labelMapper;
	private SimpleLabelMapper<OntClass> descriptionMapper;
	
	@Override
	public ConceptClass map ( OntModel model, ONDEXGraph graph )
	{
		OntClass ontCls = model.getOntClass ( classIri );
		String clsId = this.idMapper.map ( ontCls ); 
		String clsLabel = this.labelMapper.map ( ontCls );
		String description = this.descriptionMapper.map ( ontCls );
		ConceptClass cc = CachedGraphWrapper.getInstance ( graph ).getConceptClass ( clsId, clsLabel, description, null );
		return cc;
	}

	public String getClassIri ()
	{
		return classIri;
	}

	public void setClassIri ( String classIri )
	{
		this.classIri = classIri;
	}

	public SimpleIdMapper<OntClass> getIdMapper ()
	{
		return idMapper;
	}

	public void setIdMapper ( SimpleIdMapper<OntClass> idMapper )
	{
		this.idMapper = idMapper;
	}

	public SimpleLabelMapper<OntClass> getLabelMapper ()
	{
		return labelMapper;
	}

	public void setLabelMapper ( SimpleLabelMapper<OntClass> labelMapper )
	{
		this.labelMapper = labelMapper;
	}

	public SimpleLabelMapper<OntClass> getDescriptionMapper ()
	{
		return descriptionMapper;
	}

	public void setDescriptionMapper ( SimpleLabelMapper<OntClass> descriptionMapper )
	{
		this.descriptionMapper = descriptionMapper;
	}
	
}
