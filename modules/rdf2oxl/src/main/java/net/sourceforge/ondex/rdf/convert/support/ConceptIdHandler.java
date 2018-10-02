package net.sourceforge.ondex.rdf.convert.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.QuerySolution;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * To be used with {@link QueryProcessor} with SPARQL that fetches all concept IRIs. This handler
 * prepares a map of {@code IRI -> int ID} to be used in {@link ConceptHandler} and {@link RelationHandler},
 * so you need this before re-running  
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
		.map ( sol -> sol.getResource ( "resourceIri" ).getURI () )
		.forEach ( uri -> { 
			synchronized ( conceptIds ) {
				// We are assuming they're all distinct, so each one is new and we don't need putIfAbsent()
				conceptIds.put ( uri, conceptIds.size () );
			} 
		});
	}
	
	@Override
	public void reset () {
		this.conceptIds.clear ();
	}
		
	@Bean ( "conceptIds" )
	public Map<String, Integer> getConceptIds () {
		return conceptIds;
	}


	@Bean ( "conceptIdsTemplateRef" ) @Override
	protected DataPreProcessor getDataPreProcessor ()
	{
		return super.getDataPreProcessor ();
	}	
}
