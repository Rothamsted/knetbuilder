package net.sourceforge.ondex.rdf.rdf2oxl.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * # The Concept ID Handler.
 * 
 * This is a special {@link QuerySolutionHandler} to be used with {@link QueryProcessor} with SPARQL that fetches all 
 * concept IRIs. It prepares a map of {@code IRI -> int ID} to be used in {@link ConceptHandler} and 
 * {@link RelationHandler}. The IDs are either taken from RDF, using the `bk:ondexId` property, or auto-generated here.   
 * 
 * So it's not a real OXL renderer, it actually does some preparation work and then the OXL rendering is up to 
 * {@link ConceptHandler}.  
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>7 Aug 2018</dd></dl>
 *
 */
@Component ( "conceptIdHandler" )
public class ConceptIdHandler extends QuerySolutionHandler implements Resettable
{
	private Map<String, Integer> conceptIds = new HashMap<> ( 50000 );
				
	public ConceptIdHandler ()
	{
		super ();
		this.setDataPreProcessor ( (model, data) -> data.put ( "conceptIds", conceptIds ) );
	}


	@Override
	public void accept ( List<QuerySolution> sols )
	{
		sols.stream ()
		.forEach ( sol ->
		{
			Integer ondexId = Optional.ofNullable ( sol.getLiteral ( "ondexId" ) )
				.map ( Literal:: getInt  )
				.orElse ( null );
						  
			synchronized ( conceptIds ) 
			{
				// We are assuming they're all distinct, so each one is new and we don't need putIfAbsent()
				conceptIds.put ( 
					sol.get ( "resourceIri" ).asResource ().getURI (),
					ondexId != null ? ondexId : conceptIds.size () 
				);
			} 
		});
	}
	
	@Override
	public void reset () {
		this.conceptIds.clear ();
	}
	
	/**
	 * The concept IDs collected by this handler.
	 */
	@Bean ( "conceptIds" )
	public Map<String, Integer> getConceptIds () {
		return conceptIds;
	}


	/**
	 * This is auto-wired into {@link ConceptHandler#setDataPreProcessor(DataPreProcessor)} for the OXL rendering.
	 */
	@Bean ( "conceptIdsTemplateRef" ) @Override
	protected DataPreProcessor getDataPreProcessor ()
	{
		return super.getDataPreProcessor ();
	}	
}
