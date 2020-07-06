package net.sourceforge.ondex.rdf.rdf2oxl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import info.marcobrandizi.rdfutils.jena.TDBEndPointHelper;
import net.sourceforge.ondex.rdf.rdf2oxl.support.TestUtils;

/**
 * A common base to test the path: `RDF->OXL->load XML->xpath tests`.
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

	/**
	 * Triggers the TDB/OXL conversion.
	 */
	@SafeVarargs
	protected static String generateOxl ( String outPath, String tdbPath, Pair<InputStream, String>... rdfInputs ) throws IOException
	{
		TestUtils.generateTDB ( springContext, tdbPath, rdfInputs );
		
		try ( OutputStream out = new TestUtils.CollectingOutputStream ( outPath ) )
		{
			Rdf2OxlConverter converter = springContext.getBean ( Rdf2OxlConverter.class );
			converter.convert ( out, false );
			
			// writer.flush ();
			return out.toString ();
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