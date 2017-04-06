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
	private String classUri;
	private IdMapper<String, OntModel> idMapper = new IRIBasedIdMapper<> (); 
	
	@Override
	public ConceptClass map ( OntModel model, ONDEXGraph graph )
	{
		OntClass ontCls = model.getOntClass ( classUri );
		String clsLabel = ontCls.getLabel ( "en" ); // TODO: custom mapper. TODO: null check
		String clsId = this.idMapper.map ( ontCls.getURI (), model ); 
		String description = ontCls.getComment ( "en" ); // TODO: configurable
		ConceptClass cc = graph.getMetaData ().createConceptClass ( clsId, clsLabel, description, null );
		return cc;
	}

	public String getClassUri ()
	{
		return classUri;
	}

	public void setClassUri ( String classUri )
	{
		this.classUri = classUri;
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
