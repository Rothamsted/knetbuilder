package net.sourceforge.ondex.rdf.convert.support;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.jena.query.Dataset;
import org.apache.jena.system.Txn;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import info.marcobrandizi.rdfutils.jena.SparqlEndPointHelper;
import info.marcobrandizi.rdfutils.jena.TDBEndPointHelper;
import net.sourceforge.ondex.rdf.convert.support.freemarker.FreeMarkerHelper;
import uk.ac.ebi.utils.io.IOUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>27 Jul 2018</dd></dl>
 *
 */
public class ResourceProcessorTest
{
	private static Configuration tplConfig = new Configuration ( Configuration.VERSION_2_3_28 );
	
	@BeforeClass
	public static void initFreeMarker () throws IOException
	{
		tplConfig.setDirectoryForTemplateLoading ( new File ( "target/test-classes/support_test" ) );
		tplConfig.setDefaultEncoding ( "UTF-8" );
		tplConfig.setTemplateExceptionHandler ( TemplateExceptionHandler.RETHROW_HANDLER );
		tplConfig.setLogTemplateExceptions ( true );
		tplConfig.setWrapUncheckedExceptions ( true );
	}
	
	@Test
	public void testBasics () throws Exception
	{
		try ( 
			TDBEndPointHelper sparqlHelper = new TDBEndPointHelper ( "target/test-classes/support_test/basics_tdb" );
		)
		{
			Dataset ds = sparqlHelper.getDataSet ();
			Txn.executeWrite ( ds, () -> ds.getDefaultModel ().read ( "target/test-classes/support_test/publications.ttl" ) );
			
			FreeMarkerHelper tplHelper = new FreeMarkerHelper ();
			tplHelper.setTemplateConfig ( tplConfig );
			
			ResourceHandler handler = new ResourceHandler ();
			handler.setConstructTemplate ( IOUtils.readFile ( "target/test-classes/support_test/resource_graph.sparql" ) );
			handler.setOxlTemplateName ( "resource.ftlh" );
			handler.setTemplateHelper ( tplHelper );
			handler.setSparqlHelper ( sparqlHelper );
			handler.setOutWriter ( new OutputStreamWriter ( System.out ) );
			
			ResourceProcessor proc = new ResourceProcessor ();						
			proc.setConsumer ( handler );
			proc.setSparqlHelper ( sparqlHelper );
			
			proc.process ( IOUtils.readFile ( "target/test-classes/support_test/resources.sparql" ) );
		}
	}
	
	@Test
	public void testSpring () throws Exception
	{
		try ( ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext ( "default_beans.xml" ) )
		{
			TDBEndPointHelper sparqlHelper = ctx.getBean ( TDBEndPointHelper.class );
			sparqlHelper.open ( "target/test-classes/support_test/basics_tdb" );
			
			Dataset ds = sparqlHelper.getDataSet ();
			Txn.executeWrite ( ds, () -> ds.getDefaultModel ().read ( "target/test-classes/support_test/publications.ttl" ) );
						
			Configuration tplConfig = ctx.getBean ( Configuration.class );
			tplConfig.setDirectoryForTemplateLoading ( new File ( "target/test-classes/support_test" ) );
			
			ResourceHandler handler = ctx.getBean ( ResourceHandler.class );
			handler.setConstructTemplate ( IOUtils.readFile ( "target/test-classes/support_test/resource_graph.sparql" ) );
			handler.setOxlTemplateName ( "resource.ftlh" );
			handler.setOutWriter ( new OutputStreamWriter ( System.out ) );
						
			ResourceProcessor proc = ctx.getBean ( ResourceProcessor.class );
			proc.setConsumer ( handler );
			
			proc.process ( IOUtils.readFile ( "target/test-classes/support_test/resources.sparql" ) );
		}
	}
}
