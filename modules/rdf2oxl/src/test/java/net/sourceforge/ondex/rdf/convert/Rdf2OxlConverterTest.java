package net.sourceforge.ondex.rdf.convert;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>1 Aug 2018</dd></dl>
 *
 */
public class Rdf2OxlConverterTest
{
	@Test
	public void testBasics () throws IOException
	{
		try (
			Writer writer = new FileWriter ( new File ( "target/rdf2OxlConverterTest.oxl" ) );	
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext ( "default_beans.xml" ) )
		{
			TDBEndPointHelper sparqlHelper = ctx.getBean ( TDBEndPointHelper.class );
			sparqlHelper.open ( "target/test-classes/support_test/basics_tdb" );
			
			Dataset ds = sparqlHelper.getDataSet ();
			Txn.executeWrite ( ds, Exceptions.sneak ().runnable ( () ->
			{
				Model m = ds.getDefaultModel ();
				m.read ( Resources.getResource ( "bioknet.owl" ).openStream (), "RDF/XML" );
				m.read ( Resources.getResource ( "oxl_templates_test/bk_ondex.owl" ).openStream (), "RDF/XML" );
			}));
			
			Rdf2OxlConverter converter = ctx.getBean ( Rdf2OxlConverter.class );
			converter.convert ( writer );
		}
	}
}
