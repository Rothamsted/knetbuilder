package net.sourceforge.ondex.rdf.rdf2oxl.support;

import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

/**
 * # Some basic configuration for Spring.
 * 
 * This is inherited from `default_beans.xml`, which defines many other Spring beans. If you want to define your own 
 * Spring config file, we recommend that you import from `default_beans.xml`, or from this configuration object.  
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>31 Jul 2018</dd></dl>
 *
 */
@org.springframework.context.annotation.Configuration 
@Order ( +10 ) // I'm using several beans that have to be initialised in advance.
public class Rdf2OxlConfiguration
{
	
	@Bean ( name = "templateClassPath" )
	public String getTemplateClassPath ()
	{
		return "oxl_templates";
	}
	
	@Bean
	public Configuration getTemplateEngineConfiguration ()
	{
		Configuration result = new freemarker.template.Configuration ( Configuration.VERSION_2_3_28 );
		result.setDefaultEncoding ( "UTF-8" );
		result.setTemplateExceptionHandler ( TemplateExceptionHandler.RETHROW_HANDLER );
		result.setLogTemplateExceptions ( true );
		result.setWrapUncheckedExceptions ( true );
		result.setClassForTemplateLoading ( this.getClass (), '/' + getTemplateClassPath () );
		
		return result;
	}
	
}
