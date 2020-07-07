package net.sourceforge.ondex.rdf.rdf2oxl.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.jena.query.QuerySolution;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import net.sourceforge.ondex.rdf.rdf2oxl.support.freemarker.FreeMarkerHelper;

/**
 * # The Graph Summary Handler
 * 
 * This gets graph summarising figures from special aggregate-based query and renders these numbers on the corresponding
 * Ondex section.  
 * 
 * We also do some sanity check in {@link ConceptProcessor}, by comparing the no of entities collected and rendered by 
 * querying them one-by-one with the summaries obtained hereby.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>28 Sep 2018</dd></dl>
 *
 */
@Component ( "graphSummaryHandler" )
public class GraphSummaryHandler extends QuerySolutionHandler
{	
	private Map<String, Object> graphSummary = new HashMap<> ();
	
	/**
	 * Expects a single solution with conceptsCount and relationsCount.
	 */
	@Override
	public void accept ( List<QuerySolution> sols )
	{
		FreeMarkerHelper tplHelper = this.getTemplateHelper ();
		Map<String, Object> data = tplHelper.getTemplateData ( null );
		graphSummary.clear ();
		
		QuerySolution sol = sols.iterator ().next ();

		Stream.of ( "conceptsCount", "relationsCount" )
		.forEach ( key -> {
			long val = sol.getLiteral ( key ).getLong ();
			data.put ( key, val );
			graphSummary.put ( key, val );
		});
		
		DataPreProcessor dpp = this.getDataPreProcessor ();
		if ( dpp != null ) dpp.accept ( null, data );
		tplHelper.processTemplate ( this.getOxlTemplateName (), this.getOutWriter (), data );
	}

	@Bean ( "graphSummary" )
	public Map<String, Object> getGraphSummary () {
		return graphSummary;
	}
}
