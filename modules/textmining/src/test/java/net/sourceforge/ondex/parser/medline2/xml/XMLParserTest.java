package net.sourceforge.ondex.parser.medline2.xml;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import static org.junit.Assert.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.parser.medline2.sink.Abstract;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>23 Mar 2018</dd></dl>
 *
 */
public class XMLParserTest
{
	Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	/**
	 * Tests that the CDATA wrapper works (fixes 
	 * <a href = 'https://github.com/Rothamsted/ondex-knet-builder/issues/12'>#12</a>).
	 */
	@Test
	public void testCDATA () throws IOException, XMLStreamException
	{
		XMLParser pmParser = new XMLParser ();
		Set<Abstract> abstracts = pmParser.parseMedlineXML ( new File ( "target/test-classes/test_pmed_article.xml" ) );
		Abstract testAbs = abstracts.stream ().filter ( abs -> abs.getId () == 29497438 ).findAny ().orElse ( null );

		log.info ( "Title: " + testAbs.getTitle () );
		log.info ( "Abstract: " + testAbs.getBody () );
		
		assertNotNull ( "Test Article not found!", testAbs );
		assertTrue ( 
			"Wrong title for test article!", 
			testAbs.getTitle ().contains ( "Embryo Development in<i>Arabidopsis</i>." )
		);
		assertTrue ( 
			"Wrong title for test article!", 
			testAbs.getBody ().contains ( "but not in wild-type (WT) and<i>aterg2-1</i>+<i>/</i>- developed seeds." )
		);
	}
	
	/**
	 * Tests that the Year in the new PM XML (fixes 
	 * <a href = 'https://github.com/Rothamsted/ondex-knet-builder/issues/13'>#13</a>).
	 */
	@Test
	public void testYear () throws IOException, XMLStreamException
	{
		XMLParser pmParser = new XMLParser ();
		Set<Abstract> abstracts = pmParser.parseMedlineXML ( new File ( "target/test-classes/test_pmed_new_date.xml" ) );

		Abstract testAbs = abstracts.stream ().filter ( abs -> abs.getId () == 24882934 ).findAny ().orElse ( null );
		assertNotNull ( "Test Article not found!", testAbs );
		assertEquals ( "Wrong year for test article!", 2014, testAbs.getYear () );

		// the <MedlineDate> tag
		testAbs = abstracts.stream ().filter ( abs -> abs.getId () == 11706173 ).findAny ().orElse ( null );
		assertNotNull ( "Test Article not found (MedlineDate case)!", testAbs );		
		assertEquals ( "Wrong year for MedlineDate case!", 2000, testAbs.getYear () );
	}
}
