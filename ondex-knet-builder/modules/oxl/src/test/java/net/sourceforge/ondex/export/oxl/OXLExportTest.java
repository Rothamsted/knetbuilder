/**
 *
 */
package net.sourceforge.ondex.export.oxl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.MetaDataFactory;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.core.util.CachedGraphWrapper;
import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import net.sourceforge.ondex.oxl.jaxb.CDATAWriterFilter;
import net.sourceforge.ondex.parser.oxl.Parser;

/**
 * @author hindlem
 */
public class OXLExportTest 
{
		private Logger log = Logger.getLogger ( this.getClass () );

    @SuppressWarnings ( "unchecked" )
		@Test
    public void testListGDSCompatibility() throws Throwable {
        File testfile = new File(System.getProperty("java.io.tmpdir") + File.separator + "testoxl.xml");
        ONDEXGraph g = new MemoryONDEXGraph("test");

        //init export first as then Attribute class must be done adhoc
        Export export = new Export();

        ONDEXPluginArguments ea = new ONDEXPluginArguments(export.getArgumentDefinitions());
        ea.setOption(FileArgumentDefinition.EXPORT_FILE, testfile.getAbsolutePath());
        ea.setOption(ArgumentNames.EXPORT_AS_ZIP_FILE, false);
        export.setONDEXGraph(g);
        export.setArguments(ea);

        //create test graph

        DataSource dataSource = g.getMetaData().getFactory().createDataSource("cv");
        ConceptClass cc = g.getMetaData().getFactory().createConceptClass("cc");
        EvidenceType et = g.getMetaData().getFactory().createEvidenceType("et");

        ONDEXConcept c = g.getFactory().createConcept("concept", dataSource, cc, et);

        AttributeName an = g.getMetaData().getFactory().createAttributeName("an", List.class);
        List<Integer> list = new ArrayList<>();
        list.add(1);
        c.createAttribute(an, list, false);

        //export
        export.start();

        //import

        ONDEXGraph g2 = new MemoryONDEXGraph("test2");

        Parser parser = new Parser();

        ONDEXPluginArguments pa = new ONDEXPluginArguments(parser.getArgumentDefinitions());
        pa.setOption(FileArgumentDefinition.INPUT_FILE, testfile.getAbsolutePath());

        parser.setONDEXGraph(g2);
        parser.setArguments(pa);
        parser.start();

        //check results

        assertEquals("There should be one concept.", 1, g2.getConcepts().size());
        for (ONDEXConcept c2 : g2.getConcepts()) {
            Attribute attribute = c2.getAttribute(an);
            assertTrue(attribute.getValue() instanceof ArrayList);
            list = (List<Integer>) attribute.getValue();
            assertEquals("There should be one value in the list.", 1, list.size());
            for (int i : list) {
                assertEquals("The value should be 1.", 1, i);
            }
        }

    }

    @SuppressWarnings ( "rawtypes" )
		@Test
    public void testMapGDSCompatibility() throws Throwable {
        File testfile = new File(System.getProperty("java.io.tmpdir") + File.separator + "testoxl.xml");
        ONDEXGraph g = new MemoryONDEXGraph("test");

        //init export first as then Attribute class must be done adhoc
        Export export = new Export();

        ONDEXPluginArguments ea = new ONDEXPluginArguments(export.getArgumentDefinitions());
        ea.setOption(FileArgumentDefinition.EXPORT_FILE, testfile.getAbsolutePath());
        ea.setOption(ArgumentNames.EXPORT_AS_ZIP_FILE, false);
        export.setONDEXGraph(g);
        export.setArguments(ea);

        //create test graph

        DataSource dataSource = g.getMetaData().getFactory().createDataSource("cv");
        ConceptClass cc = g.getMetaData().getFactory().createConceptClass("cc");
        EvidenceType et = g.getMetaData().getFactory().createEvidenceType("et");

        ONDEXConcept c = g.getFactory().createConcept("concept", dataSource, cc, et);

        AttributeName an = g.getMetaData().getFactory().createAttributeName("an", Map.class);
        Map<Integer, Map<Integer, List<Integer>>> map = new HashMap<Integer, Map<Integer, List<Integer>>>();
        Map<Integer, List<Integer>> submap = new HashMap<Integer, List<Integer>>();
        List<Integer> sublist = new ArrayList<Integer>();
        sublist.add(1);
        sublist.add(2);
        submap.put(2, sublist);
        map.put(4, submap);
        c.createAttribute(an, map, false);

        //export
        export.start();

        //import

        ONDEXGraph g2 = new MemoryONDEXGraph("test2");

        Parser parser = new Parser();

        ONDEXPluginArguments pa = new ONDEXPluginArguments(parser.getArgumentDefinitions());
        pa.setOption(FileArgumentDefinition.INPUT_FILE, testfile.getAbsolutePath());

        parser.setONDEXGraph(g2);
        parser.setArguments(pa);
        parser.start();

        //check results

        assertEquals("There should be one concept.", 1, g2.getConcepts().size());
        for (ONDEXConcept c2 : g2.getConcepts()) {
            Attribute attribute = c2.getAttribute(an);
            assertTrue(attribute.getValue() instanceof HashMap);
            map = (Map<Integer, Map<Integer, List<Integer>>>) attribute.getValue();
            assertEquals("There should be one value in the list.", 1, map.size());
            for (Map.Entry<Integer, Map<Integer, List<Integer>>> entry : map.entrySet()) {
                assertEquals(4, entry.getKey().intValue());
                assertTrue(entry.getValue() instanceof HashMap);
                for (Map.Entry<Integer, List<Integer>> entry2 : entry.getValue().entrySet()) {
                    assertEquals(2, entry2.getKey().intValue());
                    assertTrue(entry2.getValue() instanceof ArrayList);
                    assertEquals(2, entry2.getValue().size());
                    assertTrue(((ArrayList) entry2.getValue()).contains(1));
                    assertTrue(((ArrayList) entry2.getValue()).contains(2));
                }
            }
        }

    }

    @SuppressWarnings ( "unchecked" )
		@Test
    public void testSetGDSCompatibility() throws Throwable {
        File testfile = new File(System.getProperty("java.io.tmpdir") + File.separator + "testoxl.xml");
        ONDEXGraph g = new MemoryONDEXGraph("test");

        //init export first as then Attribute class must be done adhoc
        Export export = new Export();

        ONDEXPluginArguments ea = new ONDEXPluginArguments(export.getArgumentDefinitions());
        ea.setOption(FileArgumentDefinition.EXPORT_FILE, testfile.getAbsolutePath());
        ea.setOption(ArgumentNames.EXPORT_AS_ZIP_FILE, false);
        export.setONDEXGraph(g);
        export.setArguments(ea);

        //create test graph

        DataSource dataSource = g.getMetaData().getFactory().createDataSource("cv");
        ConceptClass cc = g.getMetaData().getFactory().createConceptClass("cc");
        EvidenceType et = g.getMetaData().getFactory().createEvidenceType("et");

        ONDEXConcept c = g.getFactory().createConcept("concept", dataSource, cc, et);

        AttributeName an = g.getMetaData().getFactory().createAttributeName("an", Set.class);
        Set<Integer> set = new HashSet<>();
        set.add(1);
        set.add(2);
        set.add(3);
        c.createAttribute(an, set, false);

        //export
        export.start();

        //import

        ONDEXGraph g2 = new MemoryONDEXGraph("test2");

        Parser parser = new Parser();

        ONDEXPluginArguments pa = new ONDEXPluginArguments(parser.getArgumentDefinitions());
        pa.setOption(FileArgumentDefinition.INPUT_FILE, testfile.getAbsolutePath());

        parser.setONDEXGraph(g2);
        parser.setArguments(pa);
        parser.start();

        //check results

        assertEquals("There should be one concept.", 1, g2.getConcepts().size());
        for (ONDEXConcept c2 : g2.getConcepts()) {
            Attribute attribute = c2.getAttribute(an);
            System.out.println(attribute);
            assertTrue(attribute.getValue() instanceof HashSet);
            set = (Set<Integer>) attribute.getValue();
            assertEquals("There should be one value in the list.", 3, set.size());
        }

    }


    @SuppressWarnings ( "unchecked" )
		@Test
    public void testListOfListsGDSCompatibility() throws Throwable {
        File testfile = new File(System.getProperty("java.io.tmpdir") + File.separator + "testoxl2.xml");

        System.out.println(testfile);

        ONDEXGraph g = new MemoryONDEXGraph("test");

        //init export first as then Attribute class must be done adhoc
        Export export = new Export();

        ONDEXPluginArguments ea = new ONDEXPluginArguments(export.getArgumentDefinitions());
        ea.setOption(FileArgumentDefinition.EXPORT_FILE, testfile.getAbsolutePath());
        ea.setOption(ArgumentNames.EXPORT_AS_ZIP_FILE, false);
        export.setONDEXGraph(g);
        export.setArguments(ea);

        //create test graph

        DataSource dataSource = g.getMetaData().getFactory().createDataSource("cv");
        ConceptClass cc = g.getMetaData().getFactory().createConceptClass("cc");
        EvidenceType et = g.getMetaData().getFactory().createEvidenceType("et");

        ONDEXConcept c = g.getFactory().createConcept("concept", dataSource, cc, et);

        AttributeName an = g.getMetaData().getFactory().createAttributeName("an", List.class);
        List<Integer> list = new ArrayList<>();
        list.add(1);
        List<Integer> list1 = new ArrayList<>();
        list1.add(2);
        List<Integer> list2 = new ArrayList<>();
        list2.add(3);

        List<List<Integer>> listAll = new ArrayList<>();
        listAll.add(list);
        listAll.add(list1);
        listAll.add(list2);
        c.createAttribute(an, listAll, false);

        //export
        export.start();

        //import

        ONDEXGraph g2 = new MemoryONDEXGraph("test2");

        Parser parser = new Parser();

        ONDEXPluginArguments pa = new ONDEXPluginArguments(parser.getArgumentDefinitions());
        pa.setOption(FileArgumentDefinition.INPUT_FILE, testfile.getAbsolutePath());

        parser.setONDEXGraph(g2);
        parser.setArguments(pa);
        parser.start();

        //check results

        assertEquals("There should be one concept.", 1, g2.getConcepts().size());
        for (ONDEXConcept c2 : g2.getConcepts()) {
            Attribute attribute = c2.getAttribute(an);
            listAll = (List<List<Integer>>) attribute.getValue();
            assertEquals("There should be three lists in the list.", 3, listAll.size());
            for (List<Integer> l : listAll) {
                assertTrue("should be list", l instanceof ArrayList);
                assertEquals("should be size 1", 1, l.size());
            }
        }
    }

    @SuppressWarnings ( "unchecked" )
		@Test
    public void testListOfListOfColorsGDSCompatibility() throws Throwable {
        File testfile = new File(System.getProperty("java.io.tmpdir") + File.separator + "testoxl2.xml");

        System.out.println(testfile);

        ONDEXGraph g = new MemoryONDEXGraph("test");

        //init export first as then Attribute class must be done adhoc
        Export export = new Export();

        ONDEXPluginArguments ea = new ONDEXPluginArguments(export.getArgumentDefinitions());
        ea.setOption(FileArgumentDefinition.EXPORT_FILE, testfile.getAbsolutePath());
        ea.setOption(ArgumentNames.EXPORT_AS_ZIP_FILE, false);
        export.setONDEXGraph(g);
        export.setArguments(ea);

        //create test graph

        DataSource dataSource = g.getMetaData().getFactory().createDataSource("cv");
        ConceptClass cc = g.getMetaData().getFactory().createConceptClass("cc");
        EvidenceType et = g.getMetaData().getFactory().createEvidenceType("et");

        ONDEXConcept c = g.getFactory().createConcept("concept", dataSource, cc, et);

        AttributeName an = g.getMetaData().getFactory().createAttributeName("an", List.class);
        List<Color> list = new ArrayList<>();
        list.add(Color.BLACK);
        list.add(Color.RED);
        List<Color> list1 = new ArrayList<>();
        list1.add(Color.GREEN);
        list1.add(Color.RED);
        List<Color> list2 = new ArrayList<>();
        list2.add(Color.RED);
        list2.add(Color.ORANGE);

        List<List<Color>> listAll = new ArrayList<>();
        listAll.add(list);
        listAll.add(list1);
        listAll.add(list2);
        c.createAttribute(an, listAll, false);

        //export
        export.start();

        //import

        ONDEXGraph g2 = new MemoryONDEXGraph("test2");

        Parser parser = new Parser();

        ONDEXPluginArguments pa = new ONDEXPluginArguments(parser.getArgumentDefinitions());
        pa.setOption(FileArgumentDefinition.INPUT_FILE, testfile.getAbsolutePath());

        parser.setONDEXGraph(g2);
        parser.setArguments(pa);
        parser.start();

        //check results

        assertEquals("There should be one concept.", 1, g2.getConcepts().size());
        for (ONDEXConcept c2 : g2.getConcepts()) {
            Attribute attribute = c2.getAttribute(an);
            listAll = (List<List<Color>>) attribute.getValue();
            assertEquals("There should be one value in the list.", 3, listAll.size());
            for (List<Color> l : listAll) {
                assertTrue("should be list", l instanceof ArrayList);
                assertEquals("should be size 2", 2, l.size());
                for (Color col : l) {
                    assertTrue("should be col", col instanceof Color);
                }
            }
        }

    }


    @Test
    public void testPlainGDS() throws Throwable {
        File testfile = new File(System.getProperty("java.io.tmpdir") + File.separator + "testoxl.xml");

        //create test graph

        ONDEXGraph g = new MemoryONDEXGraph("test");
        DataSource dataSource = g.getMetaData().getFactory().createDataSource("cv");
        ConceptClass cc = g.getMetaData().getFactory().createConceptClass("cc");
        EvidenceType et = g.getMetaData().getFactory().createEvidenceType("et");

        ONDEXConcept c = g.getFactory().createConcept("concept", dataSource, cc, et);

        AttributeName an = g.getMetaData().getFactory().createAttributeName("an", Double.class);
        Double doubleI = 6546546543.4545d;
        c.createAttribute(an, doubleI, false);
        AttributeName an2 = g.getMetaData().getFactory().createAttributeName("an2", String.class);
        c.createAttribute(an2, "my String", false);
        AttributeName an3 = g.getMetaData().getFactory().createAttributeName("an3", Integer.class);
        c.createAttribute(an3, Integer.valueOf(3), false);
        AttributeName an4 = g.getMetaData().getFactory().createAttributeName("an4", Color.class);
        c.createAttribute(an4, Color.BLACK, false);

        //export

        Export export = new Export();

        ONDEXPluginArguments ea = new ONDEXPluginArguments(export.getArgumentDefinitions());
        ea.setOption(FileArgumentDefinition.EXPORT_FILE, testfile.getAbsolutePath());
        ea.setOption(ArgumentNames.EXPORT_AS_ZIP_FILE, false);

        export.setONDEXGraph(g);
        export.setArguments(ea);
        export.start();

        //import

        ONDEXGraph g2 = new MemoryONDEXGraph("test2");

        Parser parser = new Parser();

        ONDEXPluginArguments pa = new ONDEXPluginArguments(parser.getArgumentDefinitions());
        pa.setOption(FileArgumentDefinition.INPUT_FILE, testfile.getAbsolutePath());

        parser.setONDEXGraph(g2);
        parser.setArguments(pa);
        parser.start();

        //check results

        assertEquals("There should be one concept.", 1, g2.getConcepts().size());
        for (ONDEXConcept c2 : g2.getConcepts()) {
            Attribute attribute = c2.getAttribute(an);
            Double doubleOut = (Double) attribute.getValue();
            assertEquals(doubleI, doubleOut);
            attribute = c2.getAttribute(an2);
            String stringOut = (String) attribute.getValue();
            assertEquals("my String", stringOut);
            attribute = c2.getAttribute(an3);
            Integer intOut = (Integer) attribute.getValue();
            assertEquals(Integer.valueOf(3), intOut);
            attribute = c2.getAttribute(an4);
            Color colorOut = (Color) attribute.getValue();
            assertEquals(Color.BLACK, colorOut);
        }
    }

    // OXL-4 regression test

    @SuppressWarnings ( { "unchecked", "unused" } )
		@Test
    public void testConceptSetGDSExport() throws PluginConfigurationException, JAXBException, XMLStreamException, IOException {
        File testfile = new File(System.getProperty("java.io.tmpdir") + File.separator + "testoxl.xml");

        //create test graph

        ONDEXGraph g = new MemoryONDEXGraph("test");
        DataSource dataSource = g.getMetaData().getFactory().createDataSource("cv");
        ConceptClass cc = g.getMetaData().getFactory().createConceptClass("cc");
        EvidenceType et = g.getMetaData().getFactory().createEvidenceType("et");

        ONDEXConcept c = g.getFactory().createConcept("concept", dataSource, cc, et);

        AttributeName an = g.getMetaData().getFactory().createAttributeName("an", Set.class);
        Set<Integer> set = new HashSet<Integer>();
        set.add(1);
        c.createAttribute(an, set, false);

        Export export = new Export();

        ONDEXPluginArguments ea = new ONDEXPluginArguments(export.getArgumentDefinitions());
        ea.setOption(FileArgumentDefinition.EXPORT_FILE, testfile.getAbsolutePath());
        ea.setOption(ArgumentNames.EXPORT_AS_ZIP_FILE, false);

        export.setONDEXGraph(g);
        export.setArguments(ea);
        export.start();

        //import

        ONDEXGraph g2 = new MemoryONDEXGraph("test2");

        Parser parser = new Parser();

        ONDEXPluginArguments pa = new ONDEXPluginArguments(parser.getArgumentDefinitions());
        pa.setOption(FileArgumentDefinition.INPUT_FILE, testfile.getAbsolutePath());

        parser.setONDEXGraph(g2);
        parser.setArguments(pa);
        parser.start();

        //check results

        assertEquals("There should be one concept.", 1, g2.getConcepts().size());
        for (ONDEXConcept c2 : g2.getConcepts()) {
            Attribute attribute = c2.getAttribute(an);
            Set<Integer> s = (Set<Integer>) attribute.getValue();
            assertEquals("There should be one value in the list.", 1, s.size());
            for (int i : s) {
                assertEquals("The value should be 1.", 1, i);
            }
        }
    }

    @Test
    public void testCData () throws FileNotFoundException, IOException
    {
  		log.info ( "Testing CDATA exports on plain strings" );
  		
  		ONDEXGraph g = new MemoryONDEXGraph ( "test" );
  		
  		CachedGraphWrapper gw = CachedGraphWrapper.getInstance ( g );
  		
  		ConceptClass cc = gw.getConceptClass ( "TestCC", "A Test CC", "A test concept class.", null );
  		DataSource ds = gw.getDataSource ( "testDS", "Test Data Source", "A test data source." );
  		EvidenceType ev = gw.getEvidenceType ( "testEvidence", "Test Evidence", "A test evidence type." );
  		ONDEXConcept c = gw.getConcept ( "testConcept", "", "A test concept.", ds, cc, ev );
  		
  		MetaDataFactory gfact = g.getMetaData().getFactory();

  		AttributeName an = gfact.createAttributeName ( "testAttr", String.class );
      String val = "A <b>test attribute</b> value";
      c.createAttribute ( an, val, false);
      
      String oxlPath = "target/test_cdata.xml";
      Export.exportOXL ( g, oxlPath, false, true );
      
      g = Parser.loadOXL ( oxlPath );
      c = g.getConcepts ().iterator ().next ();
      Assert.assertEquals ( "Wrong reloaded attr value!", val, c.getAttribute ( an ).getValue () );
      
      // verify the XML
      //String oxlStr = IOUtils.toString ( new FileReader ( oxlPath ) );
      //Assert.assertTrue ( "CDATA-wrapped attribute not found!",  oxlStr.contains ( "<![CDATA[" + val + "]]>" ) );      
    }
    
		@Test
    @SuppressWarnings ( "unchecked" )
    public void testCollectionsCData () throws FileNotFoundException, IOException
    {
  		log.info ( "Testting CDATA exports on collections" );
  		
  		ONDEXGraph g = new MemoryONDEXGraph ( "test" );
  		
  		CachedGraphWrapper gw = CachedGraphWrapper.getInstance ( g );
  		
  		ConceptClass cc = gw.getConceptClass ( "TestCC", "A Test CC", "A test concept class.", null );
  		DataSource ds = gw.getDataSource ( "testDS", "Test Data Source", "A test data source." );
  		EvidenceType ev = gw.getEvidenceType ( "testEvidence", "Test Evidence", "A test evidence type." );
  		ONDEXConcept c = gw.getConcept ( "testConcept", "", "A test concept.", ds, cc, ev );
  		
  		MetaDataFactory gfact = g.getMetaData().getFactory();
  		
      AttributeName an = gfact.createAttributeName ( "testAttr", List.class );
      String val = "A <b>test attribute</b> value";
      c.createAttribute ( an, Stream.of ( val ).collect ( Collectors.toList () ), false);
      
      String oxlPath = "target/test_cdata_list.xml";
      Export.exportOXL ( g, oxlPath, false, true );
      
      g = Parser.loadOXL ( oxlPath );
      c = g.getConcepts ().iterator ().next ();
      Assert.assertEquals ( 
    		"Wrong reloaded attr value!", 
    		val, 
    		((List<Object>) c.getAttribute ( an ).getValue ()).get ( 0 ) 
    	);
      
      // verify the XML
      //String oxlStr = IOUtils.toString ( new FileReader ( oxlPath ) );
      //Assert.assertTrue ( "CDATA-wrapped attribute not found!",  oxlStr.contains ( "<![CDATA[" + val + "]]>" ) );      
    } 
    
    @Test
    public void testNewLineJaxbBug () throws FileNotFoundException, IOException
    {
  		log.info ( "Testting CDATA exports on plain strings" );
  		
  		ONDEXGraph g = new MemoryONDEXGraph ( "test" );
  		
  		CachedGraphWrapper gw = CachedGraphWrapper.getInstance ( g );
  		
  		ConceptClass cc = gw.getConceptClass ( "TestCC", "A Test CC", "A test concept class.", null );
  		DataSource ds = gw.getDataSource ( "testDS", "Test Data Source", "A test data source." );
  		EvidenceType ev = gw.getEvidenceType ( "testEvidence", "Test Evidence", "A test evidence type." );
  		ONDEXConcept c = gw.getConcept ( "testConcept", "", "A test concept.", ds, cc, ev );
  		
  		MetaDataFactory gfact = g.getMetaData().getFactory();

  		AttributeName an = gfact.createAttributeName ( "testAttr", String.class );
      String val = "A test\nattribute value";
      c.createAttribute ( an, val, false);
      
      String oxlPath = "target/test_newline.xml";
      Export.exportOXL ( g, oxlPath, false, true );
      
      g = Parser.loadOXL ( oxlPath );
      c = g.getConcepts ().iterator ().next ();
      Assert.assertEquals ( "Wrong reloaded attr value!", val, c.getAttribute ( an ).getValue () );
      
      // verify the XML
      //String oxlStr = IOUtils.toString ( new FileReader ( oxlPath ) );
      //Assert.assertTrue ( "CDATA-wrapped attribute not found!",  oxlStr.contains ( "<![CDATA[" + val + "]]>" ) );      
    }    

    
    /**
     * To test a problem that occurred in {@link CDATAWriterFilter#writeCharacters(char[], int, int)}, due to the
     * fact that the call to the delegate had wrong invocation parameters.
     */
    @Test
    public void testCDATAFilterParamsBug () throws FileNotFoundException, IOException
    {
  		log.info ( "Testting CDATA exports on plain strings" );
  		
  		ONDEXGraph g = new MemoryONDEXGraph ( "test" );
  		
  		CachedGraphWrapper gw = CachedGraphWrapper.getInstance ( g );
  		
  		ConceptClass cc = gw.getConceptClass ( "TestCC", "A Test CC", "A test concept class.", null );
  		DataSource ds = gw.getDataSource ( "testDS", "Test Data Source", "A test data source." );
  		EvidenceType ev = gw.getEvidenceType ( "testEvidence", "Test Evidence", "A test evidence type." );
  		ONDEXConcept c = gw.getConcept ( "testConcept", "", "A test concept.", ds, cc, ev );
  		
  		MetaDataFactory gfact = g.getMetaData().getFactory();

  		AttributeName an = gfact.createAttributeName ( "testAttr", String.class );
      String val = 
				"Overexpression of the Gm<i>GAL2</i>Gene Accelerates Flowering in<b>\r\n" + 
				"                    <i>Arabidopsis.</i>\r\n" + 
				"                </b>";      		
      c.createAttribute ( an, val, false);
      
      String oxlPath = "target/test_cdata_params.xml";
      Export.exportOXL ( g, oxlPath, false, true );
      
      g = Parser.loadOXL ( oxlPath );
      c = g.getConcepts ().iterator ().next ();
      Assert.assertEquals ( "Wrong reloaded attr value!", val, c.getAttribute ( an ).getValue () );      
    }
    
    @Test @Ignore ( "Not a real test, just to know set export format" )
    public void testSetAttr ()
    {
    	ONDEXGraph g = new MemoryONDEXGraph ( "default" );
    	ONDEXGraphMetaData meta = g.getMetaData ();
    	    	
      DataSource ds = meta.createDataSource ( "fooDs", "Foo DS", "" );
    	ConceptClass cc = meta.createConceptClass ( "FooClass", "Foo Class", "", null );
    	EvidenceType ev = meta.createEvidenceType ( "fooEv", "Foo Evidence", "" );
    	
    	ONDEXConcept c = g.createConcept ( "fooConcept", "", "", ds, cc, Collections.singleton ( ev ) );
    	AttributeName aname = meta.createAttributeName ( "fooAttr", "Foo Attribute", "", null, Set.class , null );
    	
    	Set<Double> aval = new HashSet<> ();
    	//aval.add ( "Foo Value 1" );
    	//aval.add ( "Foo Value 2" );
    	aval.add ( 0.5 );
    	aval.add ( 1.8 );
    	
    	c.createAttribute ( aname, aval, false );
    	
    	Export.exportOXL ( g, "target/set-export-test.xml", false, true );
    }
}
