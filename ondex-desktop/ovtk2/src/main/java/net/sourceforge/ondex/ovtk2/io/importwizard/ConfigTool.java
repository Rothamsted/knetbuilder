/*
 * Created on 15.10.2003
 *
 */
package net.sourceforge.ondex.ovtk2.io.importwizard;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * <b>ConfigTool</b> fills a configuration hash with content from a given XML
 * file, used for example by ImportWizard<br>
 * <font size=-1>(XML file should look like the example config.xml)</font>
 * 
 * @author Jan Taubert - Uni Bielefeld
 * @version oct03
 * @see ImportWizard
 */
public class ConfigTool extends DefaultHandler {

	private static Hashtable<String, Hashtable<String, String>> configHash = null;
	private static ConfigTool configTool = null;
	private static boolean inName = false;
	private static boolean inOptions = false;
	private static boolean inPart = false;
	private static Hashtable<String, String> optionsHash = null;
	private static String optionsName = null;
	private static String partName = null;

	private static XMLReader xmlReader = null;

	/**
	 * Call this method to process a XML file into a configuration hash
	 * 
	 * @param filename
	 *            relative or absolute path to the XML file
	 * @return hash containing the configuration data from the XML file
	 */
	public static Hashtable<String, Hashtable<String, String>> loadFromFile(String filename) {

		// create new class reference if not exist
		if (configTool == null) {
			configTool = new ConfigTool();
		}

		// load given filename
		InputStream inputStream = null;
		if (filename.startsWith("http:") || filename.startsWith("file:") || filename.startsWith("https:")) {

			// special case for applet
			try {
				URL url = new URL(filename);
				inputStream = url.openStream();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {

			try {
				inputStream = new FileInputStream(filename);
			} catch (FileNotFoundException fe) {
				System.out.println("File " + filename + " not found");
				return null;
			}
		}

		// parse file
		if (inputStream != null && xmlReader != null) {
			try {
				xmlReader.parse(new InputSource(inputStream));
			} catch (SAXException se) {
				System.out.println(se);
				return null;
			} catch (IOException ie) {
				System.out.println(ie);
				return null;
			}
		}

		return configHash;
	}

	/**
	 * Default Constructor - private because of static class behavior
	 * 
	 */
	private ConfigTool() {

		super();

		configHash = new Hashtable<String, Hashtable<String, String>>();

		// create new XMLReader using xerces
		try {
			xmlReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		} catch (SAXException se) {
			System.out.println(se);
		}

		// add eventhandler
		if (xmlReader != null) {
			xmlReader.setContentHandler(this);
			xmlReader.setErrorHandler(this);
		}
	}

	/**
	 * Overwritten methode from DefaultHandler
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#characters
	 */
	@Override
	public void characters(char ch[], int start, int length) {

		// parse name of actual configuration part
		if (inPart && inName) {
			char content[] = new char[length];
			System.arraycopy(ch, start, content, 0, length);
			partName = new String(content);
		}

		// parse each configuration option and add it to a hash
		if (inPart && inOptions && optionsName != null) {
			char content[] = new char[length];
			System.arraycopy(ch, start, content, 0, length);
			optionsHash.put(optionsName, new String(content));
		}
	}

	/**
	 * Overwritten methode from DefaultHandler
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#endElement
	 */
	@Override
	public void endElement(String uri, String name, String qName) {

		// check if a fully quallified namespace (xmlns) is given
		if ("".equals(uri)) {
			// name section ends here
			if (inPart && qName.equals("name"))
				inName = false;
			// kill each option name to set a new
			if (inPart && inOptions && qName.equals(optionsName))
				optionsName = null;
			// we are leaving global options section
			if (qName.equals("options")) {
				inOptions = false;
				optionsName = null;
			}
			// section part is closed, so lets insert optionsHash in configHash
			// with key partName and release partName and optionsHash
			if (qName.equals("part")) {
				inPart = false;
				configHash.put(partName, optionsHash);
				partName = null;
				optionsHash = null;
			}
		} else {
			// same as above
			if (inPart && name.equals("name"))
				inName = false;
			if (inPart && inOptions && name.equals(optionsName))
				optionsName = null;
			if (name.equals("options")) {
				inOptions = false;
				optionsName = null;
			}
			if (name.equals("part")) {
				inPart = false;
				configHash.put(partName, optionsHash);
				partName = null;
				optionsHash = null;
			}
		}
	}

	/**
	 * Overwritten methode from DefaultHandler
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#startElement
	 */
	@Override
	public void startElement(String uri, String name, String qName, Attributes atts) {

		// check if a fully quallified namespace (xmlns) is given
		if ("".equals(uri)) {
			// now we have section part-->name
			if (inPart && qName.equals("name"))
				inName = true;
			// now get the name of each option
			if (inPart && inOptions)
				optionsName = qName;
			// section part begins, so create a new optionsHash
			if (qName.equals("part")) {
				inPart = true;
				optionsHash = new Hashtable<String, String>();
			}
			// now we are in the options
			if (qName.equals("options"))
				inOptions = true;
		} else {
			// same as above
			if (inPart && name.equals("name"))
				inName = true;
			if (inPart && inOptions)
				optionsName = name;
			if (name.equals("part")) {
				inPart = true;
				optionsHash = new Hashtable<String, String>();
			}
			if (name.equals("options"))
				inOptions = true;
		}
	}

}
