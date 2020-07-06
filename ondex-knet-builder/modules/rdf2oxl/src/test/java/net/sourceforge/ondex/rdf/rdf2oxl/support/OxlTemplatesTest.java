package net.sourceforge.ondex.rdf.rdf2oxl.support;

import static org.junit.Assert.assertEquals;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.system.Txn;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.machinezoo.noexception.Exceptions;

import info.marcobrandizi.rdfutils.jena.TDBEndPointHelper;
import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import uk.ac.ebi.utils.io.IOUtils;
import uk.ac.ebi.utils.xml.XPathReader;

/**
 * Simple tests about using FreeMarker for OXL conversion.
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
			OutputStream out = new TestUtils.CollectingOutputStream ();	
			Writer writer = new OutputStreamWriter ( out );
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext ( "default_beans.xml" ) 
		)
		{
			TDBEndPointHelper sparqlHelper = ctx.getBean ( TDBEndPointHelper.class );
			sparqlHelper.open ( "target/test-classes/support_test/oxl_templates_tdb" );
			
			Dataset ds = sparqlHelper.getDataSet ();
			Txn.executeWrite ( ds, Exceptions.sneak ().runnable ( () ->
			{
				Model m = ds.getDefaultModel ();
				m.read ( "src/main/assembly/resources/data/bioknet.owl" );
				m.read ( "src/main/assembly/resources/data/bk_ondex.owl" );
			}));
									
			QuerySolutionHandler handler = (QuerySolutionHandler) ctx.getBean ( "resourceHandler" );
			handler.setConstructTemplate ( 
				NamespaceUtils.asSPARQLProlog () + IOUtils.readResource ( "oxl_templates/concept_class_graph.sparql" ) 
			);
			handler.setOxlTemplateName ( "concept_class.ftlx" );
			
			handler.setOutWriter ( writer );
						
			QueryProcessor proc = (QueryProcessor) ctx.getBean ( "resourceProcessor" );
			proc.setBatchJob ( handler );
			proc.setHeader ( "<conceptclasses>\n" );
			proc.setTrailer ( "</conceptclasses>\n" );
			
			proc.process (
				NamespaceUtils.asSPARQLProlog () + 
				IOUtils.readResource ( "oxl_templates/concept_class_iris.sparql" ) 
			);
			
			writer.flush ();
						
			// Verify 
			XPathReader xpath = new XPathReader ( out.toString () );
			assertEquals ( "Disease class not found or too many of them!", 
				1,
				xpath.readNodeList ( "/conceptclasses/cc[id = 'Disease']" ).getLength ()
			);
			assertEquals ( "EC's fullname not found!",
				"Enzyme Classification", xpath.readString ( "/conceptclasses/cc[id='EC']/fullname" )
			);
			assertEquals ( "Environment's description not found!",
				"Treatment or surrounding conditions",
				xpath.readString ( "/conceptclasses/cc[id='Environment']/description" )
			);
			assertEquals ( "GeneOntologyTerms' parent not found!",
				"OntologyTerms",
				xpath.readString ( "/conceptclasses/cc[id='GeneOntologyTerms']/specialisationOf/idRef" )
			);			
			
		} // try
	}	// testConceptClasses()
}
