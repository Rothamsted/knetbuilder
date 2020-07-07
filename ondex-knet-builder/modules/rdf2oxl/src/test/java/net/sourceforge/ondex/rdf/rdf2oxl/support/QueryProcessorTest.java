package net.sourceforge.ondex.rdf.rdf2oxl.support;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.jena.query.Dataset;
import org.apache.jena.system.Txn;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import info.marcobrandizi.rdfutils.jena.TDBEndPointHelper;
import net.sourceforge.ondex.rdf.rdf2oxl.support.freemarker.FreeMarkerHelper;
import uk.ac.ebi.utils.io.IOUtils;

/**
 * Simple tests for the {@link QueryProcessor} basics.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>27 Jul 2018</dd></dl>
 *
 */
public class QueryProcessorTest
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
			OutputStream out = new TestUtils.CollectingOutputStream ();
		)
		{
			Dataset ds = sparqlHelper.getDataSet ();
			Txn.executeWrite ( ds, () -> ds.getDefaultModel ().read ( "target/test-classes/support_test/publications.ttl" ) );
			
			FreeMarkerHelper tplHelper = new FreeMarkerHelper ();
			tplHelper.setTemplateConfig ( tplConfig );
			
			QuerySolutionHandler handler = new QuerySolutionHandler ();
			handler.setConstructTemplate ( IOUtils.readFile ( "target/test-classes/support_test/resource_graph.sparql" ) );
			handler.setOxlTemplateName ( "resource.ftlh" );
			handler.setTemplateHelper ( tplHelper );
			handler.setSparqlHelper ( sparqlHelper );
			
			handler.setOutWriter ( new OutputStreamWriter ( out ) );
			
			QueryProcessor proc = new QueryProcessor ();						
			proc.setBatchJob ( handler );
			proc.setSparqlHelper ( sparqlHelper );
			
			proc.process ( IOUtils.readFile ( "target/test-classes/support_test/resources.sparql" ) );
			
			out.flush ();
			
			assertTrue ( "Wrong output (title)",
				out.toString ().contains ( "Title: Assessment of drought tolerance of 49 switchgrass" ) 
			);
			assertTrue ( "Wrong output (abstract)", 
				out.toString ().contains ( "Abstract:\nThe putative raffinose synthase gene from rice" ) 
			);
		}
	}
	
	@Test
	public void testSpring () throws Exception
	{
		try ( 
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext ( "default_beans.xml" );
			OutputStream out = new TestUtils.CollectingOutputStream ();
		)
		{
			TDBEndPointHelper sparqlHelper = ctx.getBean ( TDBEndPointHelper.class );
			sparqlHelper.open ( "target/test-classes/support_test/basics_tdb" );
			
			Dataset ds = sparqlHelper.getDataSet ();
			Txn.executeWrite ( ds, () -> ds.getDefaultModel ().read ( "target/test-classes/support_test/publications.ttl" ) );
						
			Configuration tplConfig = ctx.getBean ( Configuration.class );
			tplConfig.setDirectoryForTemplateLoading ( new File ( "target/test-classes/support_test" ) );
			
			QuerySolutionHandler handler = (QuerySolutionHandler) ctx.getBean ( "resourceHandler" );
			handler.setConstructTemplate ( IOUtils.readFile ( "target/test-classes/support_test/resource_graph.sparql" ) );
			handler.setOxlTemplateName ( "resource.ftlh" );
			
			handler.setOutWriter ( new OutputStreamWriter ( out ) );
						
			QueryProcessor proc = (QueryProcessor) ctx.getBean ( "resourceProcessor" );
			proc.setBatchJob ( handler );
			proc.process ( IOUtils.readFile ( "target/test-classes/support_test/resources.sparql" ) );
			
			out.flush ();
			
			assertTrue ( "Wrong output (title)",
				out.toString ().contains ( "Title: Assessment of drought tolerance of 49 switchgrass" ) 
			);
			assertTrue ( "Wrong output (abstract)", 
				out.toString ().contains ( "Abstract:\nThe putative raffinose synthase gene from rice" ) 
			);	
		}
	}
}
