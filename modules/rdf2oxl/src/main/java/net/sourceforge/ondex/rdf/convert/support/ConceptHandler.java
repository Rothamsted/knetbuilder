package net.sourceforge.ondex.rdf.convert.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Resource;

import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

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
	public ConceptHandler ()
	{
		super ();
		super.setDataPreProcessor ( (m, js) -> 
		{
		  @SuppressWarnings ( "unchecked" )
			List<Map<String, Object>> graphArray = (List<Map<String, Object>>) js.get ( "js" );
		  Map<String, Object> result = new HashMap<> ();
		  
		  // Wrap in its slot for FTL
		  result.put ( "evidenceTypes",
		  	// Index (i.e., create a map) over @id	
		  	JsonUtils.indexJsArray (
			  	// Extracts objects of right @type
		  		JsonUtils.filterOnProp ( graphArray.stream (), "@type","EvidenceType" ), 
		  		"@id"
		  	)
		  );
		  return result;
		});
	}

	/**
	 * Since we already return something for this, here we merge our Json with the Json returned by the parameter
	 */
	@Autowired @Qualifier ( "conceptIdsTemplateRef" ) @Override
	protected void setDataPreProcessor ( BiFunction<Model, Map<String, Object>, Map<String, Object>> dataPreProcessor )
	{
		if ( dataPreProcessor == null ) return; 
		
		final BiFunction<Model, Map<String, Object>, Map<String, Object>> mine = super.getDataPreProcessor ();
		
		super.setDataPreProcessor ( (m, js) -> 
		{
			Map<String, Object> myJs = mine.apply ( m, js );
			myJs.putAll ( dataPreProcessor.apply ( m, myJs ) );
			return myJs;
		});
	}	
}
