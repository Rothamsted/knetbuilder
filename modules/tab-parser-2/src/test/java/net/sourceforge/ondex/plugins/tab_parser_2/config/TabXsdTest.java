package net.sourceforge.ondex.plugins.tab_parser_2.config;

import java.io.IOException;

import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xmlunit.validation.Languages;
import org.xmlunit.validation.ValidationProblem;
import org.xmlunit.validation.ValidationProblem.ProblemType;
import org.xmlunit.validation.ValidationResult;
import org.xmlunit.validation.Validator;

import com.google.common.io.Resources;

/**
 * Simple JUnit tests to validate /tab_parser.xsd and its instance examples.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>12 Dec 2016</dd></dl>
 *
 */
public class TabXsdTest
{
	private Logger log = Logger.getLogger ( this.getClass () );
	
	@Test
	public void testSchema () throws IOException
	{
		Validator v = Validator.forLanguage ( Languages.W3C_XML_SCHEMA_NS_URI );
		v.setSchemaSource ( new StreamSource ( Resources.getResource ( this.getClass (), "/tab_parser.xsd" ).openStream () ) );
		ValidationResult r = v.validateSchema ();
		for ( ValidationProblem p: r.getProblems () )
			log.log ( 
				ProblemType.ERROR.equals ( p.getType () ) ? Level.ERROR : Level.WARN,
				"Validation error: " + p.toString ()
		);
		Assert.assertTrue ( "Schema validation error!", r.isValid () );
	}

	@Test
	public void testSchemaExamples () throws IOException
	{
		for ( String inResName: new String[] { 
			"test_tab_spec_1.xml",
			"test_biogrid_yeast_tab_spec.xml"
		})
		{
			Validator v = Validator.forLanguage ( Languages.W3C_XML_SCHEMA_NS_URI );
			v.setSchemaSource ( new StreamSource ( Resources.getResource ( this.getClass (), "/tab_parser.xsd" ).openStream () ) );
			ValidationResult r = v.validateInstance ( 
				new StreamSource ( Resources.getResource ( this.getClass (), "/" + inResName ).openStream () ) 
			);
			for ( ValidationProblem p: r.getProblems () )
				log.log ( 
					ProblemType.ERROR.equals ( p.getType () ) ? Level.ERROR : Level.WARN,
					"Validation error: " + p.toString ()
			);
			Assert.assertTrue ( String.format ( "Validation error for %s!", inResName ), r.isValid () );
		}
	}

}
