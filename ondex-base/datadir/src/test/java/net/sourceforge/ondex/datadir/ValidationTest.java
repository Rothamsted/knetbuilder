package net.sourceforge.ondex.datadir;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Validate certain XML files against the XSDs
 * 
 * @author taubertj
 * 
 */
public class ValidationTest extends TestCase {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMetadata() {

		// location relative to execution directory
		File file = new File("src/main/resources/xml/ondex_metadata.xml");
		assertTrue("File to test must exist", file.exists());

		// XSD to validate against
		File schema = new File("src/main/resources/xml/ondex.xsd");
		assertTrue("Schema to test must exist", schema.exists());

		// factory for builders
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);

		// schema compliance
		SchemaFactory schemaFactory = SchemaFactory
				.newInstance("http://www.w3.org/2001/XMLSchema");

		try {
			// set XSD schema
			factory.setSchema(schemaFactory
					.newSchema(new Source[] { new StreamSource(schema) }));

			// new document builder
			DocumentBuilder builder = factory.newDocumentBuilder();

			// catch errors, fail test
			builder.setErrorHandler(new SimpleErrorHandler());

			// validate while parsing
			builder.parse(new InputSource(new FileReader(file)));
		} catch (SAXException e) {
			fail(e.getMessage());
		} catch (ParserConfigurationException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testConfig() {

		// location relative to execution directory
		File file = new File("src/main/resources/config.xml");
		assertTrue("File to test must exist", file.exists());

		// factory for builders
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		factory.setNamespaceAware(true);

		try {
			// new document builder
			DocumentBuilder builder = factory.newDocumentBuilder();

			// catch errors, fail test
			builder.setErrorHandler(new SimpleErrorHandler());

			// validate while parsing
			builder.parse(new InputSource(new FileReader(file)));
		} catch (SAXException e) {
			fail(e.getMessage());
		} catch (ParserConfigurationException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Catches validation errors and fails test
	 * 
	 * @author taubertj
	 * 
	 */
	private class SimpleErrorHandler implements ErrorHandler {

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			fail(exception.getMessage());
		}

		@Override
		public void error(SAXParseException exception) throws SAXException {
			fail(exception.getMessage());
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			fail(exception.getMessage());
		}
	}

}
