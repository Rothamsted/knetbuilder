package net.sourceforge.ondex.rdf.convert.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Function;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import uk.ac.ebi.utils.ids.IdUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>7 Aug 2018</dd></dl>
 *
 */
@Component ( "conceptHandler" )
public class ConceptHandler extends ResourceHandler
{
	private Map<String, Integer> conceptIds = new HashMap<> ( 50000 );
			
	public ConceptHandler ()
	{
		super ();
		
		this.setDataPreProcessor ( m -> 
		{
			Map<String, Object> result = new HashMap<> ();
			result.put ( "conceptIds", conceptIds );
			return result;
		});
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
		
		super.accept ( res );
	}
	
	
	public void reset () {
		this.conceptIds.clear ();
	}


	@Bean ( "conceptIdsTemplateRef" )
	@Override
	public Function<Model, Map<String, Object>> getDataPreProcessor () {
		return super.getDataPreProcessor ();
	}
}
