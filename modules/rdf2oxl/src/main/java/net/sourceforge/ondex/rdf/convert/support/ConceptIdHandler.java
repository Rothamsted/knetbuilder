package net.sourceforge.ondex.rdf.convert.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * To be used with {@link ResourceProcessor} with SPARQL that fetches all concept IRIs. This handler
 * prepares a map of {@code IRI -> int ID} to be used in {@link ConceptHandler} and {@link RelationHandler},
 * so you need this before re-running  
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>7 Aug 2018</dd></dl>
 *
 */
@Component ( "conceptIdHandler" )
public class ConceptIdHandler extends ResourceHandler implements Resettable
{
	private Map<String, Integer> conceptIds = new HashMap<> ( 50000 );
			
	public ConceptIdHandler ()
	{
		super ();
		
		final Map<String, Object> conceptIdsWrapper = new HashMap<> ();
		conceptIdsWrapper.put ( "conceptIds", conceptIds );
		this.setDataPreProcessor ( m -> conceptIdsWrapper );
	}


	@Override
	public void accept ( Set<Resource> res )
	{
		res.stream ()
		.map ( Resource::getURI )
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
	public Map<String, Integer> getConceptIds ()
	{
		return conceptIds;
	}

	@Bean ( "conceptIdsTemplateRef" )
	@Override
	public Function<Model, Map<String, Object>> getDataPreProcessor () {
		return super.getDataPreProcessor ();
	}
}
