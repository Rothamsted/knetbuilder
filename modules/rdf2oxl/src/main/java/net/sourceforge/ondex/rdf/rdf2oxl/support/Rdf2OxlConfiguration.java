package net.sourceforge.ondex.rdf.rdf2oxl.support;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

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
@Order ( +10 ) // this is using several beans that have to be initialised before.
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
			new ItemConfigurationBuilder ( "Graph Summary" )
				.withResourcesQueryName ( "graph_summary.sparql" )
				.withGraphTemplateName ( "graph_summary.ftlx" )
				.withQuerySolutionHandler ( applicationContext.getBean ( GraphSummaryHandler.class ) )
			.build (),
			new ItemConfigurationBuilder ( "Data Sources" )
				.withResourcesQueryName ( "data_source_iris.sparql" )
				.withConstructTemplateName ( "metadata_graph.sparql" )
				.withGraphTemplateName ( "data_source.ftlx" )
				.withHeader ( "\t<ondexmetadata>\n\t\t<cvs>\n" )
				.withTrailer ( "\t\t</cvs>\n" )
			.build (),
			new ItemConfigurationBuilder ( "Units" )
				.withResourcesQueryName ( "unit_iris.sparql" )
				.withConstructTemplateName ( "metadata_graph.sparql" )
				.withGraphTemplateName ( "unit.ftlx" )
				.withHeader ( "\t\t<units>\n" )
				.withTrailer ( "\t\t</units>\n" )
			.build (),
			new ItemConfigurationBuilder ( "Attribute Names" )
				.withResourcesQueryName ( "attribute_name_iris.sparql" )
				.withConstructTemplateName ( "attribute_name_graph.sparql" )
				.withGraphTemplateName ( "attribute_name.ftlx" )
				.withHeader ( "\t\t<attrnames>\n" )
				.withTrailer ( "\t\t</attrnames>\n" )
			.build (),
			new ItemConfigurationBuilder ( "Evidences" )
				.withResourcesQueryName ( "evidence_iris.sparql" )
				.withConstructTemplateName ( "metadata_graph.sparql" )
				.withGraphTemplateName ( "evidence.ftlx" )
				.withHeader ( "\t\t<evidences>\n" )
				.withTrailer ( "\t\t</evidences>\n" )
			.build (),
			new ItemConfigurationBuilder ( "Concept Classes" )
				.withResourcesQueryName ( "concept_class_iris.sparql" )
				.withConstructTemplateName ( "concept_class_graph.sparql" )
				.withGraphTemplateName ( "concept_class.ftlx" )
				.withHeader ( "\t\t<conceptclasses>\n" )
				.withTrailer ( "\t\t</conceptclasses>\n" )
			.build (),
			new ItemConfigurationBuilder ( "Relation Types" )
				.withResourcesQueryName ( "relation_type_iris.sparql" )
				.withConstructTemplateName ( "relation_type_graph.sparql" )
				.withGraphTemplateName ( "relation_type.ftlx" )
				.withHeader ( "\t\t<relationtypes>\n" )
				.withTrailer ( "\t\t</relationtypes>\n\t</ondexmetadata>\n" )
			.build (),
			new ItemConfigurationBuilder ( "Concept IDs" )
				.withResourcesQueryName ( "concept_iris.sparql" )
				.withQuerySolutionHandler ( applicationContext.getBean ( ConceptIdHandler.class ) )
			.build (),
			new ItemConfigurationBuilder ( "Concepts" )
				.withConstructTemplateName ( "concept_graph.sparql" )
				.withHeader ( "\t<ondexdataseq>\n\t\t<concepts>\n" )
				.withTrailer ( "\t\t</concepts>\n" )
				.withGraphTemplateName ( "concept.ftlx" )
				.withQueryProcessor ( applicationContext.getBean ( ConceptProcessor.class ) )
				.withQuerySolutionHandler ( applicationContext.getBean ( ConceptHandler.class ) )
			.build (),
			new ItemConfigurationBuilder ( "Straight Relations" )
				.withResourcesQueryName ( "straight_relation_iris.sparql" )
				.withHeader ( "\t\t<relations>\n" )
				.withGraphTemplateName ( "straight_relation.ftlx" )
				.withQuerySolutionHandler ( applicationContext.getBean ( StraightRelationHandler.class ) )
			.build (),
			new ItemConfigurationBuilder ( "Reified Relations" )
				.withResourcesQueryName ( "reified_relation_iris.sparql" )
				.withConstructTemplateName ( "reified_relation_graph.sparql" )
				.withTrailer ( "\t\t</relations>\n\t</ondexdataseq>\n</ondex>\n" )
				.withGraphTemplateName ( "reified_relation.ftlx" )
				.withQuerySolutionHandler ( applicationContext.getBean ( RelationHandler.class ) )
			.build ()
		});
	}

	@Override
	public void setApplicationContext ( ApplicationContext applicationContext ) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
