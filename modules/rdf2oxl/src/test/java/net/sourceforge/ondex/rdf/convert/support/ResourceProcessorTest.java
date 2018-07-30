package net.sourceforge.ondex.rdf.convert.support;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.jena.query.Dataset;
import org.apache.jena.system.Txn;
import org.junit.BeforeClass;
import org.junit.Test;

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
			handler.setOutWriter ( new OutputStreamWriter ( System.out ) );
			handler.setTemplateHelper ( tplHelper );
			handler.setSparqlHelper ( sparqlHelper );
			
			ResourceProcessor proc = new ResourceProcessor ();						
			proc.setConsumer ( handler );
			proc.setSparqlHelper ( sparqlHelper );
			
			proc.process ( IOUtils.readFile ( "target/test-classes/support_test/resources.sparql" ) );
		}
	}
}
