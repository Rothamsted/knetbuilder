package net.sourceforge.ondex.rdf.convert.support;

import static net.sourceforge.ondex.rdf.convert.support.JsonUtils.indexJsonLdTypes;

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
		this.setDataPreProcessor ( 
			(model, data) -> indexJsonLdTypes ( data, "parentProps", "_OxlParentProperty" ) 
		);
	}
}
