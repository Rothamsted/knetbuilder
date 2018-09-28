package net.sourceforge.ondex.rdf.convert.support;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>31 Jul 2018</dd></dl>
 *
 */
@org.springframework.context.annotation.Configuration 
@DependsOn ({ 
	"resourceHandler", "conceptHandler", "relationHandler",
	"resourceProcessor", "conceptProcessor" 
})
public class Rdf2OxlConfiguration implements ApplicationContextAware
{
	private ApplicationContext applicationContext;

	@Bean ( name = "templateClassPath" )
	public String getTemplateClassPath ()
	{
		return "oxl_templates";
	}
	
	@Bean
	public Configuration getTemplateConfiguration ()
	{
		Configuration result = new freemarker.template.Configuration ( Configuration.VERSION_2_3_28 );
		result.setDefaultEncoding ( "UTF-8" );
		result.setTemplateExceptionHandler ( TemplateExceptionHandler.RETHROW_HANDLER );
		result.setLogTemplateExceptions ( true );
		result.setWrapUncheckedExceptions ( true );
		result.setClassForTemplateLoading ( this.getClass (), '/' + getTemplateClassPath () );
		
		return result;
	}
	
	@Bean ( name = "itemConfigurations" )
	public List<ItemConfiguration> getItemConfigurations ()
	{		
		return Arrays.asList ( new ItemConfiguration[] 
		{
			new ItemConfiguration (
				"Concept IDs", "concept_iris.sparql", null, 
				null, null, null,
				null, (ResourceHandler) applicationContext.getBean ( "conceptIdHandler" )
			),			
			new ItemConfiguration (
				"Concepts", null, "concept_graph.sparql", 
				"\t\t<concepts>\n", "concept.ftlx", "\t\t</concepts>\n",
				(ResourceProcessor) applicationContext.getBean ( "conceptProcessor" ),
				(ConceptHandler) applicationContext.getBean ( "conceptHandler" )
			),
			new ItemConfiguration ( 
				"Attribute Names", "attribute_name_iris.sparql", "attribute_name_graph.sparql", 
				"\t\t<attrnames>\n", "attribute_name.ftlx", "\t\t</attrnames>\n"
			),
			new ItemConfiguration ( 
				"Concept Classes", "concept_class_iris.sparql", "concept_class_graph.sparql", 
				"\t\t<conceptclasses>\n", "concept_class.ftlx", "\t\t</conceptclasses>\n"
			),
			new ItemConfiguration (
				"Relation Types", "relation_type_iris.sparql", "relation_type_graph.sparql", 
				"\t\t<relationtypes>\n", "relation_type.ftlx", "\t\t</relationtypes>\n"
			)
		});
	}

	@Override
	public void setApplicationContext ( ApplicationContext applicationContext ) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
