package net.sourceforge.ondex.ovtk2.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.EntityResolver;
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
	public void testConfig() {

		// location relative to execution directory
		File file = new File("config/config.xml");
		assertTrue("File to test must exist", file.exists());

		// factory for builders
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		factory.setNamespaceAware(true);

		try {
			// new document builder
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver(new EntityResolver() {
				@Override
				public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
					if (systemId.contains("properties.dtd")) {
						return new InputSource(new FileReader("config/properties.dtd"));
					} else {
						return null;
					}
				}
			});

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
	public void testEnglish() {

		// location relative to execution directory
		File file = new File("config/english.xml");
		assertTrue("File to test must exist", file.exists());

		// factory for builders
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		factory.setNamespaceAware(true);

		try {
			// new document builder
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver(new EntityResolver() {
				@Override
				public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
					if (systemId.contains("properties.dtd")) {
						return new InputSource(new FileReader("config/properties.dtd"));
					} else {
						return null;
					}
				}
			});

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
	public void testVisual() {

		// location relative to execution directory
		File file = new File("config/visual.xml");
		assertTrue("File to test must exist", file.exists());

		// factory for builders
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		factory.setNamespaceAware(true);

		try {
			// new document builder
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver(new EntityResolver() {
				@Override
				public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
					if (systemId.contains("properties.dtd")) {
						return new InputSource(new FileReader("config/properties.dtd"));
					} else {
						return null;
					}
				}
			});

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
