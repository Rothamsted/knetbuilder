package net.sourceforge.ondex.rdf.convert.support;

import static net.sourceforge.ondex.rdf.convert.support.JsonUtils.toList;
import static org.apache.commons.collections15.CollectionUtils.containsAny;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>29 Aug 2018</dd></dl>
 *
 */
@Component ( "relationTypeHandler" )
public class RelationTypeHandler extends ResourceHandler
{
	public RelationTypeHandler ()
	{
		super ();
		this.setDataPreProcessor ( (m, js) -> 
		{
		  @SuppressWarnings ( "unchecked" )
			List<Map<String, Object>> graphArray = (List<Map<String, Object>>) js.get ( "js" );
		  Map<String, Object> result = new HashMap<> ();
		  
		  // Wrap in its slot for FTL
		  result.put ( "parentProps",
		  	// Index (i.e., create a map) over @id	
		  	JsonUtils.indexJsArray (
			  	// Extracts objects of right @type
		  		JsonUtils.filterOnProp ( graphArray.stream (), "@type","_OxlParentProperty" ), 
		  		"@id"
		  	)
		  );
		  return result;
		});
	}
}
