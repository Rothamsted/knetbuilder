package net.sourceforge.ondex.rdf.convert.support;

import static net.sourceforge.ondex.rdf.convert.support.JsonUtils.indexJsonLdTypes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
		super.setDataPreProcessor ( 
			(model, data) -> indexJsonLdTypes ( data, "evidenceTypes", "EvidenceType" )
		);
	}

	/**
	 * Since we already return something for this, here we merge our Json with the Json returned by the parameter
	 */
	@Autowired @Qualifier ( "conceptIdsTemplateRef" ) @Override
	protected void setDataPreProcessor ( DataPreProcessor dataPreProcessor )
	{		
		if ( dataPreProcessor == null ) return;		
		final DataPreProcessor mine = super.getDataPreProcessor ();
		super.setDataPreProcessor ( mine.merge ( dataPreProcessor ) );
	}	
}
