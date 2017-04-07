package net.sourceforge.ondex.parser.owl;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.ConceptClassMapper;
import net.sourceforge.ondex.parser.IdMapper;

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
	private IdMapper<String, OntModel> idMapper = new IRIBasedIdMapper<> (); 
	
	@Override
	public ConceptClass map ( OntModel model, ONDEXGraph graph )
	{
		OntClass ontCls = model.getOntClass ( classIri );
		String clsLabel = ontCls.getLabel ( "en" ); // TODO: custom mapper. TODO: null check
		String clsId = this.idMapper.map ( ontCls.getURI (), model ); 
		String description = ontCls.getComment ( "en" ); // TODO: configurable
		ConceptClass cc = graph.getMetaData ().createConceptClass ( clsId, clsLabel, description, null );
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

	public IdMapper<String, OntModel> getIdMapper ()
	{
		return idMapper;
	}

	public void setIdMapper ( IdMapper<String, OntModel> idMapper )
	{
		this.idMapper = idMapper;
	}
	
}
