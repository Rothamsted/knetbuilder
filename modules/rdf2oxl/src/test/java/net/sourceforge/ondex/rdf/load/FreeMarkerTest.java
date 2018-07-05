package net.sourceforge.ondex.rdf.load;

import static java.lang.System.out;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ModelMaker;
import org.junit.BeforeClass;
import org.junit.Test;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateNotFoundException;
import info.marcobrandizi.rdfutils.jena.SparqlUtils;
import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Jul 2018</dd></dl>
 *
 */
public class FreeMarkerTest
{
	private static Configuration tplConfig = new Configuration ( Configuration.VERSION_2_3_28 );
	
	@BeforeClass
	public static void initFreeMarker () throws IOException
	{
		tplConfig.setDirectoryForTemplateLoading ( new File ( "target/test-classes/freemarker_test" ) );
		tplConfig.setDefaultEncoding ( "UTF-8" );
		tplConfig.setTemplateExceptionHandler ( TemplateExceptionHandler.RETHROW_HANDLER );
		tplConfig.setLogTemplateExceptions ( true );
		tplConfig.setWrapUncheckedExceptions ( true );
	}

	
	@Test
	public void testBasics () throws IOException, TemplateException
	{		
		// See StringTemplateLoader for the template-from-string case
		
		Template tpl = tplConfig.getTemplate ( "fremarker_test.ftlh" );
		
		@SuppressWarnings ( "serial" )
		Map<String, Object> data = new HashMap<String, Object> () {{
			put ( "who", "Marco" );
		}};
		
		tpl.process ( data, new OutputStreamWriter ( System.out ) );
	}
	
	
	@Test
	public void testWithSparql () throws Exception
	{
		Model model = ModelFactory.createDefaultModel ();
		model.read ( "file:target/test-classes/freemarker_test/foaf_ex.rdf" );
		
		String sparql = NamespaceUtils.asSPARQLProlog () + 
			"SELECT ?aname ?bname\n" + 
			"WHERE {\n" + 
			"  ?a a foaf:Person;\n" + 
			"	    foaf:name ?aname .\n" + 
			"\n" + 
			"	 OPTIONAL \n" + 
			"	 {\n" + 
			"	   ?a foaf:knows ?b .\n" + 
			"	   ?b foaf:name ?bname .\n" + 
			"	 }\n" + 
			"}\n";

		//out.println ( sparql );
		
		ResultSet rs = SparqlUtils.select ( sparql, model );
		
		/*
		rs.forEachRemaining ( sol -> out.format ( "%s\t%s\n", 
			Optional.ofNullable ( sol.getLiteral ( "aname" ) ).map ( Literal::getString ).orElse ( "-" ), 
			Optional.ofNullable ( sol.getLiteral ( "bname" ) ).map ( Literal::getString ).orElse ( "-" )
		)); */
		
		@SuppressWarnings ( "serial" )
		Map<String, Object> data = new HashMap<String, Object> () {{
			put ( "who", "Marco" );
			put ( "solutions", rs );
		}};

		Template tpl = tplConfig.getTemplate ( "fremarker_sparql_test.ftlh" );		
		tpl.process ( data, new OutputStreamWriter ( out ) );		
	}
}
