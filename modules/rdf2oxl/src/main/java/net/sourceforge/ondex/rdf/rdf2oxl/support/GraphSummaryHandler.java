package net.sourceforge.ondex.rdf.rdf2oxl.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections15.map.HashedMap;
import org.apache.jena.query.QuerySolution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import net.sourceforge.ondex.rdf.rdf2oxl.support.freemarker.FreeMarkerHelper;
import uk.ac.ebi.utils.exceptions.ExceptionUtils;

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
	@Autowired @Qualifier ( "conceptIds" )
	private Map<String, Integer> conceptIds = new HashMap<> ( 50000 );

	@Override
	public void accept ( List<QuerySolution> sols )
	{
		FreeMarkerHelper tplHelper = this.getTemplateHelper ();
		Map<String, Object> data = tplHelper.getTemplateData ( null );
		
		QuerySolution sol = sols.iterator ().next ();

		Stream.of ( "conceptsCount", "relationsCount" )
			.forEach ( key -> data.put ( key, sol.getLiteral ( key ).getLong () ) );
		
		DataPreProcessor dpp = this.getDataPreProcessor ();
		if ( dpp != null ) dpp.accept ( null, data );
		tplHelper.processTemplate ( this.getOxlTemplateName (), this.getOutWriter (), data );
	}
}
