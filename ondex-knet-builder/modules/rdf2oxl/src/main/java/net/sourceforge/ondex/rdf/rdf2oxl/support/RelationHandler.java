package net.sourceforge.ondex.rdf.rdf2oxl.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import net.sourceforge.ondex.core.ONDEXRelation;

/**
 * # The {@link QuerySolutionHandler} specific for {@link ONDEXRelation}.
 * 
 * This is defined just to get the concept IDs computed by the {@link ConceptIdHandler} injected here, using
 * the the {@link DataPreProcessor} form and the {@link #setDataPreProcessor(DataPreProcessor)}.  
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
