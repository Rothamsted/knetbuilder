package net.sourceforge.ondex.parser.owl;

import java.util.Set;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.ConceptMapper;
import net.sourceforge.ondex.parser.IdMapper;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Apr 2017</dd></dl>
 *
 */
public class OWLConceptMapper implements ConceptMapper<OntModel>
{
	private OWLConceptClassMapper conceptClassMapper;
	private IdMapper<String, OntModel> idMapper = new IRIBasedIdMapper<> ();
	
	@Override
	public Set<ONDEXConcept> map ( OntModel model, ONDEXGraph graph )
	{
		String topClassUri = conceptClassMapper.getClassIri ();
		OntClass topOntCls = model.getOntClass ( topClassUri );
		ConceptClass cc = conceptClassMapper.map ( model, graph );
		topOntCls.listSubClasses ( true ).forEachRemaining ( ontCls -> this.mapTree ( ontCls, cc, graph ) );
		return graph.getConcepts ();
	}

	protected void mapTree ( OntClass ontClass, ConceptClass cc, ONDEXGraph graph )
	{
		this.mapClass ( ontClass, cc, graph );
		ontClass.listSubClasses ( true ).forEachRemaining ( ontCls -> this.mapTree ( ontCls, cc, graph ) );
	}
	
	protected ONDEXConcept mapClass ( OntClass ontClass, ConceptClass cc, ONDEXGraph graph )
	{
		OntModel model = ontClass.getOntModel ();
		String clsLabel = ontClass.getLabel ( "en" );
		String conceptId = idMapper.map ( ontClass.getURI (), model );
		String description = ontClass.getComment ( "en" ); // TODO: configurable
		
		// TODO: what is it?!
		EvidenceType evidence = graph.getMetaData ().createEvidenceType ( "IMPD", "IMPD", "" );
		
		// TODO: attach the file?
		DataSource ds = graph.getMetaData ().createDataSource ( "owlParser", "The OWL Parser", "" );
		
		ONDEXConcept concept = graph.getFactory ().createConcept ( conceptId, "", description, ds, cc, evidence );
		concept.createConceptName ( clsLabel, true );
		concept.createConceptAccession ( conceptId, ds, false );
		return concept;
	}

	public OWLConceptClassMapper getConceptClassMapper ()
	{
		return conceptClassMapper;
	}

	public void setConceptClassMapper ( OWLConceptClassMapper ccMapper )
	{
		this.conceptClassMapper = ccMapper;
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
