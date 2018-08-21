package net.sourceforge.ondex.rdf.convert.support;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
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
@Component ( "relationHandler" )
public class RelationHandler extends ResourceHandler
{
	@Override
	public void accept ( Set<Resource> res )
	{
		super.accept ( res );
	}


	@Autowired @Qualifier ( "conceptIdsTemplateRef" )
	@Override
	public void setDataPreProcessor ( Function<Model, Map<String, Object>> dataPreProcessor )
	{
		super.setDataPreProcessor ( dataPreProcessor );
	}
}
