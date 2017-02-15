/**
 *
 */
package net.sourceforge.ondex.export.oxl;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import net.sourceforge.ondex.parser.oxl.Parser;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author hindlem
 */
public class OXLExport {

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
        List<Integer> list = new ArrayList<Integer>();
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
        Set<Integer> set = new HashSet<Integer>();
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
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        List<Integer> list1 = new ArrayList<Integer>();
        list1.add(2);
        List<Integer> list2 = new ArrayList<Integer>();
        list2.add(3);

        List<List<Integer>> listAll = new ArrayList<List<Integer>>();
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
        List<Color> list = new ArrayList<Color>();
        list.add(Color.BLACK);
        list.add(Color.RED);
        List<Color> list1 = new ArrayList<Color>();
        list1.add(Color.GREEN);
        list1.add(Color.RED);
        List<Color> list2 = new ArrayList<Color>();
        list2.add(Color.RED);
        list2.add(Color.ORANGE);

        List<List<Color>> listAll = new ArrayList<List<Color>>();
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
        Double doubleI = new Double(6546546543.4545);
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

}
