package net.sourceforge.ondex.rdf.rdf2oxl.support;

import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.system.Txn;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.io.Resources;
import com.machinezoo.noexception.Exceptions;

import info.marcobrandizi.rdfutils.jena.TDBEndPointHelper;
import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.rdf.rdf2oxl.support.QueryProcessor;
import net.sourceforge.ondex.rdf.rdf2oxl.support.QuerySolutionHandler;
import uk.ac.ebi.utils.io.IOUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>31 Jul 2018</dd></dl>
 *
 */
public class OxlTemplatesTest
{
	@Test
	public void testConceptClasses () throws Exception
	{
		try (
			Writer writer = new OutputStreamWriter ( System.out );	
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext ( "default_beans.xml" ) 
		)
		{
			TDBEndPointHelper sparqlHelper = ctx.getBean ( TDBEndPointHelper.class );
			sparqlHelper.open ( "target/test-classes/support_test/oxl_templates_tdb" );
			
			Dataset ds = sparqlHelper.getDataSet ();
			Txn.executeWrite ( ds, Exceptions.sneak ().runnable ( () ->
			{
				Model m = ds.getDefaultModel ();
				m.read ( Resources.getResource ( "bioknet.owl" ).openStream (), "RDF/XML" );
				m.read ( Resources.getResource ( "oxl_templates_test/bk_ondex.owl" ).openStream (), "RDF/XML" );
			}));
									
			QuerySolutionHandler handler = (QuerySolutionHandler) ctx.getBean ( "resourceHandler" );
			handler.setConstructTemplate ( 
				NamespaceUtils.asSPARQLProlog () + IOUtils.readResource ( "oxl_templates/concept_class_graph.sparql" ) 
			);
			handler.setOxlTemplateName ( "concept_class.ftlx" );
			
			handler.setOutWriter ( writer );
						
			QueryProcessor proc = (QueryProcessor) ctx.getBean ( "resourceProcessor" );
			proc.setConsumer ( handler );
			proc.setHeader ( "<conceptclasses>\n" );
			proc.setTrailer ( "</conceptclasses>\n" );
			
			proc.process (
				NamespaceUtils.asSPARQLProlog () + 
				IOUtils.readResource ( "oxl_templates/concept_class_iris.sparql" ) 
			);
		} // try
	}	// testConceptClasses()
}
