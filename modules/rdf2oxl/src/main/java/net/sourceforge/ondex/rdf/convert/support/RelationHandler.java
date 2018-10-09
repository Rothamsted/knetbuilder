package net.sourceforge.ondex.rdf.convert.support;

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
public class RelationHandler extends QuerySolutionHandler
{
	@Autowired @Qualifier ( "conceptIdsTemplateRef" ) @Override
	protected void setDataPreProcessor ( DataPreProcessor dataPreProcessor ) {
		super.setDataPreProcessor ( dataPreProcessor );
	}	
}
