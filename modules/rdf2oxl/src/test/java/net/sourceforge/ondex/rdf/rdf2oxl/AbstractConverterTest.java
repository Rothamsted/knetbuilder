package net.sourceforge.ondex.rdf.rdf2oxl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.system.Txn;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.machinezoo.noexception.Exceptions;

import info.marcobrandizi.rdfutils.jena.TDBEndPointHelper;
import net.sourceforge.ondex.rdf.rdf2oxl.support.TestUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Oct 2018</dd></dl>
 *
 */
public abstract class AbstractConverterTest
{
	protected static String resultOxl;
	
	protected static ClassPathXmlApplicationContext springContext;

	@BeforeClass
	public static void initResources () throws IOException {
		springContext = new ClassPathXmlApplicationContext ( "default_beans.xml" );
	}

	@SafeVarargs
	protected static String generateOxl ( String outPath, String tdbPath, Pair<InputStream, String>... rdfInputs ) throws IOException
	{
		try ( Writer writer = new TestUtils.OutputCollectorWriter ( outPath ) )
		{
			TDBEndPointHelper sparqlHelper = springContext.getBean ( TDBEndPointHelper.class );
			sparqlHelper.open ( tdbPath );
			
			Dataset ds = sparqlHelper.getDataSet ();
			Txn.executeWrite ( ds, Exceptions.sneak ().runnable ( () ->
			{
				Model m = ds.getDefaultModel ();
				for ( Pair<InputStream, String> rdfInput: rdfInputs )
					m.read ( rdfInput.getLeft (),  null, rdfInput.getRight () );
			}));
			
			Rdf2OxlConverter converter = springContext.getBean ( Rdf2OxlConverter.class );
			converter.convert ( writer );
			
			writer.flush ();
			return writer.toString ();
		}		
	}

	
	@AfterClass
	public static void closeResources ()
	{
		springContext.close ();
	}

	/**
	 * We close the TDB after each test method, since conflicts might arise if not.
	 */
	@After
	public void closeSession ()
	{
		@SuppressWarnings ( "resource" )
		TDBEndPointHelper sparqlHelper = springContext.getBean ( TDBEndPointHelper.class );
		sparqlHelper.close ();
	}

}