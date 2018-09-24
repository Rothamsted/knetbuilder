package net.sourceforge.ondex.rdf.convert.support;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import info.marcobrandizi.rdfutils.XsdMapper;

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

	/**
	 * Injects the {@code conceptIdsTemplateRef} bean, provided by {@link ConceptIdHandler} and also adds
	 * {@link XsdMapper} to the classes available to the template engine. This is used to translate 
	 * attribute values from xsd: values to Java 
   *
	 */
	@Autowired @Qualifier ( "conceptIdsTemplateRef" ) @Override
	protected void setDataPreProcessor ( DataPreProcessor dataPreProcessor )
	{	
		super.setDataPreProcessor ( dataPreProcessor );
	}	
}
