package net.sourceforge.ondex.rdf.rdf2oxl.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.apache.jena.query.QuerySolution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import net.sourceforge.ondex.rdf.rdf2oxl.support.freemarker.FreeMarkerHelper;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>28 Sep 2018</dd></dl>
 *
 */
@Component ( "graphSummaryHandler" )
public class GraphSummaryHandler extends QuerySolutionHandler
{	
	private Map<String, Object> graphSummary = new HashMap<> ();
	
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
