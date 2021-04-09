package net.sourceforge.ondex.rdf.rdf2oxl.support;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import info.marcobrandizi.rdfutils.jena.ModelEndPointHelper;
import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import uk.ac.ebi.utils.io.IOUtils;
import uk.ac.ebi.utils.regex.RegEx;

/**
 * Basic tests with the FreeMarker framework.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Jul 2018</dd></dl>
 *
 */
public class FreeMarkerTest
{
	private static Configuration tplConfig = new Configuration ( Configuration.VERSION_2_3_28 );
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
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
		Map<String, Object> data = new HashMap<> () {{
			put ( "who", "Marco" );
		}};
		
		String out = TestUtils.processTemplate ( tpl, data );		
		assertTrue ( "Wrong output!", out.contains ( "Hello, Marco!" ) );
	}
	
	
	/**
	 * Tests templates that use @ResultSet straight.
	 */
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
		var sparqlHelper = new ModelEndPointHelper ( model );
		ResultSet rs = sparqlHelper.select ( sparql );
		
		/*
		rs.forEachRemaining ( sol -> out.format ( "%s\t%s\n", 
			Optional.ofNullable ( sol.getLiteral ( "aname" ) ).map ( Literal::getString ).orElse ( "-" ), 
			Optional.ofNullable ( sol.getLiteral ( "bname" ) ).map ( Literal::getString ).orElse ( "-" )
		)); */
		
		@SuppressWarnings ( "serial" )
		Map<String, Object> data = new HashMap<> () {{
			put ( "who", "Marco" );
			put ( "solutions", rs ); // pass the cursor to the template, it will loop over it.
		}};

		Template tpl = tplConfig.getTemplate ( "fremarker_sparql_test.ftlh" );		
		String out = TestUtils.processTemplate ( tpl, data );		
		
		assertTrue ( "Wrong output (header)!", out.contains ( "Hello, Marco!" ) );
		assertTrue ( "Wrong output (friendship 1)", out.contains ( "Tim Berners-Lee	Jim Hendler" ) );
		assertTrue ( "Wrong output (friendship 2)", out.contains ( "Tim Berners-Lee	John Gage" ) );
	}
	
	
	/**
	 * Tests a template that gets data as a JSON string. 
	 */
	@Test
	public void testJson () throws IOException, TemplateException
	{		
		// See StringTemplateLoader for the template-from-string case
		
		Template tpl = tplConfig.getTemplate ( "json_test.ftlh" );
		
		String json = IOUtils.readResource ( "freemarker_test/test.json" );
		@SuppressWarnings ( "serial" )
		Map<String, Object> data = new HashMap<> () {{
			put ( "json", json );
		}};
				
		tpl.process ( data, new OutputStreamWriter ( System.out ) );
		String out = TestUtils.processTemplate ( tpl, data );
		
		assertTrue ( "Wrong output (header)!", out.contains ( "Title: Hello, World!" ) );
		assertTrue ( "Wrong output (row 1)!", out.contains ( "1	Item 1" ) );
		assertTrue ( "Wrong output (row 2)!", out.contains ( "2	Item 2" ) );
	}	
	
	
	/**
	 * Tests the approach of first transforming a {@code SPARQL CONSTRUCT} result (or any {@link Model} into a 
	 * JSON string, which is then passed to the template.
	 */	
	@Test
	public void testJsSparql () throws Exception
	{
		Model model = ModelFactory.createDefaultModel ();
		model.read ( "file:target/test-classes/freemarker_test/foaf_ex.rdf" );
		
		String sparql = NamespaceUtils.asSPARQLProlog () + 
			"CONSTRUCT {\n" + 
			"  ?a bk:name ?aname; bk:friendName ?bname\n" +
			"}\n" +
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
		
		var sparqlHelper = new ModelEndPointHelper ( model );
		Model m = sparqlHelper.construct ( sparql );
		StringWriter sw = new StringWriter ();
		m.write ( sw, "JSON-LD" );
		
		// out.print ( sw.toString () );		
		
		@SuppressWarnings ( "serial" )
		Map<String, Object> data = new HashMap<String, Object> () {{
			put ( "json", sw.toString () );
		}};

		Template tpl = tplConfig.getTemplate ( "js_sparql_test.ftlh" );		
		String out = TestUtils.processTemplate ( tpl, data );
		
		assertTrue ( "Wrong output (name 1)", out.contains ( "Name: Danny Ayers" ) );
		assertTrue ( 
			"Wrong output (friendship 1)", 
			new RegEx ( 
				".+Name: Tim Berners-Lee.+$.*(^\\s+Friend: .+$)+.*^\\s+Friend: Ora Lassila.+",
				Pattern.MULTILINE | Pattern.DOTALL
			).matches ( out )
		);
	}	
	
	@Test
	public void testJsSparqlFraming () throws Exception
	{
		Model model = ModelFactory.createDefaultModel ();
		model.read ( "file:target/test-classes/freemarker_test/foaf_ex.rdf" );
		
		String sparql = NamespaceUtils.asSPARQLProlog () + 
			"CONSTRUCT {\n" + 
			"  ?a a foaf:Person;"
			+ "   foaf:name ?aname;\n"
			+ "   foaf:knows ?b.\n"
			+ "\n"
			+ "?b a foaf:Person; foaf:name ?bname\n" +
			"}\n" +
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
		
		var sparqlHelper = new ModelEndPointHelper ( model );
		Model m = sparqlHelper.construct ( sparql );
		StringWriter sw = new StringWriter ();
		
		m.write ( sw, "JSON-LD" );
		
		@SuppressWarnings ( "unchecked" )
		Map<String, Object> js = (Map<String, Object>) JsonUtils.fromString ( sw.toString () );
		
		JsonLdOptions jsOpts = new JsonLdOptions ();
		jsOpts.setEmbed ( "@always" );
		
		Object jsldCtx = js.get ( "@context" );
		js = JsonLdProcessor.frame ( js, jsldCtx, jsOpts );
		// Compaction needs to be redone
		js = JsonLdProcessor.compact ( js, jsldCtx, jsOpts );
	
		Map<String, Object> data = new HashMap<> ();
		data.put ( "persons", js.get ( "@graph" ) );

		Template tpl = tplConfig.getTemplate ( "framed_js_sparql_test.ftlh" );		

		String out = TestUtils.processTemplate ( tpl, data );
		
		assertTrue ( "Wrong output (name 1)", out.contains ( "Name: Danny Ayers" ) );
		assertTrue ( 
			"Wrong output (friendship 1)", 
			new RegEx ( 
				".+Name: Tim Berners-Lee.+$.*(^\\s+Friend: .+$)+.*^\\s+Friend: Ora Lassila.+",
				Pattern.MULTILINE | Pattern.DOTALL
			).matches ( out )
		);
		
	}	
}
