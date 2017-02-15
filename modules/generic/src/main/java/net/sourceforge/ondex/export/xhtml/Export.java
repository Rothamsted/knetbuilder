package net.sourceforge.ondex.export.xhtml;

import com.ctc.wstx.io.CharsetNames;
import com.ctc.wstx.stax.WstxOutputFactory;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.export.ONDEXExport;
import net.sourceforge.ondex.tools.threading.monitoring.Monitorable;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Exports an ONDEX to a xhtml website made up of linked tables with various
 * indices. This export was designed as a half way house between tab delimited
 * data dump and OVTK style graph traversal.
 *
 * @author hindlem
 */
@Authors(authors = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Export extends ONDEXExport implements ArgumentNames,
        Monitorable {

    private final static boolean PRETTY = true;

    private final WstxOutputFactory xmlf;
    private final String enc = CharsetNames.CS_UTF8;

    private static final String PID = "Parser ID";

    private int progress = 0;
    private int maxProgress = 1000;
    private boolean isCancelled = false;
    private String state = Monitorable.STATE_IDLE;

    private File workingDir;

    private Throwable caught = null;

    public Export() {
        xmlf = (WstxOutputFactory) WstxOutputFactory.newInstance();
        xmlf.configureForRobustness();
    }

    @Override
    public void start() {
        try {

            workingDir = new File((String) args
                    .getUniqueValue(FileArgumentDefinition.EXPORT_DIR));
            System.out.println("Exporting to " + workingDir.getAbsolutePath());

            if (workingDir.exists()) {
                System.out.println("removing previous dir at "
                        + workingDir.getAbsolutePath());
                deleteDirectory(workingDir);
                System.out.println("finished clearing up");
            }

            workingDir.mkdirs();
            workingDir.mkdir();

            try {
                new File("readme.txt").createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            String relationDir = "relation_index";
            new File(workingDir, relationDir).mkdir();

            String conceptDir = "concepts";
            new File(workingDir, conceptDir).mkdir();

            String metaDataDir = "metadata";
            new File(workingDir, metaDataDir).mkdir();

            String relationGDSDir = "relationgds";
            new File(workingDir, relationGDSDir).mkdir();

            try {

                state = "Writing metadata";
                // stage 1 write metadata

                writeMetaData(graph.getMetaData(), metaDataDir);
                progress = 250;

                state = "Writing concepts";
                // stage 2 write concepts

                Set<ONDEXConcept> concepts = graph.getConcepts();
                double conceptnum = concepts.size();
                double current = 1;

                for (ONDEXConcept concept : concepts) {

                    progress = progress + (int) (current / conceptnum * 250d);
                    if (isCancelled) {
                        deleteDirectory(workingDir);
                        return;
                    }

                    String currentConceptDir = calculateConceptDir(conceptDir,
                            concept);

                    File newDir = new File(workingDir, currentConceptDir);
                    newDir.mkdirs();

                    XMLStreamWriter2 xmlw = (XMLStreamWriter2) xmlf
                            .createXMLStreamWriter(new FileOutputStream(
                                    new File(workingDir, currentConceptDir
                                            + File.separator + concept.getId()
                                            + ".html")), enc);

                    XHTMLHelper.initializeXHTMLFile(xmlw, "Ondex Concepts");

                    xmlw.writeStartElement("h1");
                    xmlw.writeCharacters("Ondex Concept");
                    xmlw.writeEndElement();
                    if (PRETTY)
                        xmlw.writeCharacters("\n");

                    writeConceptTables(concept, xmlw, relationGDSDir,
                            currentConceptDir, metaDataDir);

                    XHTMLHelper.closeXHTMLFile(xmlw);

                    // Close the writer to flush the output
                    xmlw.close();
                    current++;
                }
                progress = 500;

                state = "Writing ConceptClass Index";
                // stage 3 write conceptClass Index

                String conceptClassIndex = "conceptClassIndex";
                new File(workingDir, conceptClassIndex).mkdir();

                current = 1;

                Set<ConceptClass> conceptclasses = graph.getMetaData().getConceptClasses();

                double ccnum = conceptclasses.size();
                for (ConceptClass conceptClass : conceptclasses) {
                    Set<ONDEXConcept> conceptOfC = graph
                            .getConceptsOfConceptClass(conceptClass);

                    progress = progress + (int) (current / ccnum * 250d);
                    if (isCancelled) {
                        deleteDirectory(workingDir);
                        return;
                    }

                    if (conceptOfC != null && conceptOfC.size() > 0) {
                        XMLStreamWriter2 xmlw = (XMLStreamWriter2) xmlf
                                .createXMLStreamWriter(new FileOutputStream(
                                        new File(workingDir, conceptClassIndex
                                                + File.separator
                                                + conceptClass.getId()
                                                + "_index.html")), enc);

                        XHTMLHelper.initializeXHTMLFile(xmlw, "Ondex Concepts");

                        xmlw.writeStartElement("h1");
                        xmlw.writeCharacters("ConceptClass "
                                + conceptClass.getId());
                        xmlw.writeEndElement();
                        if (PRETTY)
                            xmlw.writeCharacters("\n");

                        createConceptClassIndexTable(graph, conceptOfC,
                                conceptDir, relationDir, xmlw);

                        XHTMLHelper.closeXHTMLFile(xmlw);

                        // Close the writer to flush the output
                        xmlw.close();
                    }
                    current++;
                }

                progress = 750;
                state = "Writing Relations Index";
                // stage 4 write relations Index

                current = 1;

                Set<RelationType> rts = graph.getMetaData()
                        .getRelationTypes();

                int rtsnum = rts.size();
                for (RelationType rtset : rts) {
                    progress = progress + (int) (current / rtsnum * 250d);
                    if (isCancelled) {
                        deleteDirectory(workingDir);
                        return;
                    }

                    Set<ONDEXRelation> rtsRelations = graph
                            .getRelationsOfRelationType(rtset);
                    if (rtsRelations == null || rtsRelations.size() == 0)
                        continue;

                    for (ConceptClass conceptClass : graph.getMetaData().getConceptClasses()) {
                        Set<ONDEXRelation> ccRelations = graph
                                .getRelationsOfConceptClass(conceptClass);
                        if (ccRelations == null || ccRelations.size() == 0)
                            continue;

                        ccRelations.retainAll(rtsRelations);
                        if (ccRelations.size() > 0) {

                            File relationsFile = new File(workingDir,
                                    relationDir + File.separator
                                            + rtset.getId() + "_"
                                            + conceptClass.getId() + ".html");
                            relationsFile.getParentFile().mkdir();

                            XMLStreamWriter2 xmlw = (XMLStreamWriter2) xmlf
                                    .createXMLStreamWriter(
                                            new FileOutputStream(relationsFile),
                                            enc);

                            XHTMLHelper.initializeXHTMLFile(xmlw,
                                    "Ondex Relations");

                            xmlw.writeStartElement("h1");
                            xmlw.writeCharacters("Ondex Relations");
                            xmlw.writeEndElement();
                            if (PRETTY)
                                xmlw.writeCharacters("\n");

                            writeRelationsTable(ccRelations, metaDataDir,
                                    conceptDir, relationGDSDir, "../", xmlw);

                            XHTMLHelper.closeXHTMLFile(xmlw);
                            // Close the writer to flush the output
                            xmlw.close();
                        }
                    }
                    current++;
                }

                progress = 1000;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
            state = Monitorable.STATE_TERMINAL;
        } catch (Throwable t) {
            caught = t;
        }
    }

    /**
     * Create a ConceptClass index table
     *
     * @param graph       the current graph
     * @param conceptOfC  concept of a single concept
     * @param conceptDir  the dir where concepts are written
     * @param relationDir the dir where relation indexes are written
     * @param xmlw        the xml stream writer
     * @throws XMLStreamException on StAX error
     */
    private void createConceptClassIndexTable(ONDEXGraph graph,
                                              Set<ONDEXConcept> conceptOfC, String conceptDir,
                                              String relationDir, XMLStreamWriter2 xmlw)
            throws XMLStreamException {
        xmlw.writeStartElement("table");
        xmlw.writeAttribute("border", "1");

        boolean[] htmlValues = new boolean[]{false, true, true};

        for (ONDEXConcept c : conceptOfC) {
            StringBuilder relationLinks = new StringBuilder();

            Set<ONDEXRelation> relations = graph.getRelationsOfConcept(c);
            for (ONDEXRelation relation : relations) {
                ConceptClass conceptClass = c.getOfType();
                RelationType rtset = relation.getOfType();

                ONDEXConcept target = relation.getFromConcept();
                if (c.equals(target)) {
                    target = relation.getToConcept();
                }

                String link = "<a href=\"../" + relationDir + File.separator
                        + rtset.getId() + "_" + conceptClass.getId() + ".html#"
                        + relation.getId() + "\" class=\"new-window\" >"
                        + getDefaultConceptName(target) + "(" + rtset.getId()
                        + ")";

                link = link + "</a>";

                if (relationLinks.length() == 0) {
                    relationLinks.append(link);
                } else {
                    relationLinks.append(", " + link);
                }
            }
            String link = "<a href=\"../" + calculateConceptDir(conceptDir, c)
                    + File.separator + c.getId()
                    + ".html\" class=\"new-window\" >" + c.getId() + ". "
                    + getDefaultConceptName(c) + "</a>";

            XHTMLTable.writeHeaders(xmlw, new String[]{"ID", "Name",
                    "Relations"});
            XHTMLTable.writeMixed(xmlw,
                    new String[]{String.valueOf(c.getId()), link,
                            relationLinks.toString()}, htmlValues);

        }

        xmlw.writeEndElement();
        if (PRETTY)
            xmlw.writeCharacters("\n");

        xmlw.writeStartElement("br");
        xmlw.writeEndElement();
        if (PRETTY)
            xmlw.writeCharacters("\n");
    }

    /**
     * Creates a Metadata site in the specified folder
     *
     * @param graphData metadata to be written to dir
     * @param dir       Folder to write metadata to
     * @throws FileNotFoundException if folder is invalid
     * @throws XMLStreamException    on STaX writing error
     */
    public void writeMetaData(ONDEXGraphMetaData graphData, String dir)
            throws FileNotFoundException, XMLStreamException {

        String file = dir + File.separator + "cvs.html";
        new File(workingDir, file).getParentFile().mkdirs();

        XMLStreamWriter2 xmlw = (XMLStreamWriter2) xmlf.createXMLStreamWriter(
                new FileOutputStream(new File(workingDir, file)), enc);

        XHTMLHelper.initializeXHTMLFile(xmlw, "Ondex CVs");

        xmlw.writeStartElement("h1");
        xmlw.writeCharacters("Ondex CVs");
        xmlw.writeEndElement();

        for (DataSource dataSource : graphData.getDataSources()) {
            writeMetaDataTable(dataSource, xmlw);
        }

        XHTMLHelper.closeXHTMLFile(xmlw);
        xmlw.close();

        String ccFile = dir + File.separator + File.separator + "ccs.html";

        xmlw = (XMLStreamWriter2) xmlf.createXMLStreamWriter(
                new FileOutputStream(new File(workingDir, ccFile)), enc);

        XHTMLHelper.initializeXHTMLFile(xmlw, "Ondex ConcpetClasses");

        xmlw.writeStartElement("h1");
        xmlw.writeCharacters("Ondex Concept Classes");
        xmlw.writeEndElement();

        for (ConceptClass cc : graphData.getConceptClasses()) {
            writeMetaDataTable(cc, xmlw);
        }

        XHTMLHelper.closeXHTMLFile(xmlw);
        xmlw.close();

        String atFile = dir + File.separator + "ats.html";

        xmlw = (XMLStreamWriter2) xmlf.createXMLStreamWriter(
                new FileOutputStream(new File(workingDir, atFile)), enc);

        XHTMLHelper.initializeXHTMLFile(xmlw, "Ondex AttributeNames");

        xmlw.writeStartElement("h1");
        xmlw.writeCharacters("Ondex AttributeNames");
        xmlw.writeEndElement();

        for (AttributeName at : graphData.getAttributeNames()) {
            writeMetaDataTable(at, xmlw);
        }

        XHTMLHelper.closeXHTMLFile(xmlw);
        xmlw.close();

        String etFile = dir + File.separator + "ets.html";

        xmlw = (XMLStreamWriter2) xmlf.createXMLStreamWriter(
                new FileOutputStream(new File(workingDir, etFile)), enc);

        XHTMLHelper.initializeXHTMLFile(xmlw, "Ondex EvidenceTypes");

        xmlw.writeStartElement("h1");
        xmlw.writeCharacters("Ondex AttributeNames");
        xmlw.writeEndElement();

        for (EvidenceType et : graphData.getEvidenceTypes()) {
            writeMetaDataTable(et, xmlw);
        }

        XHTMLHelper.closeXHTMLFile(xmlw);
        xmlw.close();

        String usFile = dir + File.separator + "us.html";

        xmlw = (XMLStreamWriter2) xmlf.createXMLStreamWriter(
                new FileOutputStream(new File(workingDir, usFile)), enc);

        XHTMLHelper.initializeXHTMLFile(xmlw, "Ondex EvidenceTypes");

        xmlw.writeStartElement("h1");
        xmlw.writeCharacters("Ondex AttributeNames");
        xmlw.writeEndElement();

        for (Unit u : graphData.getUnits()) {
            writeMetaDataTable(u, xmlw);
        }

        XHTMLHelper.closeXHTMLFile(xmlw);
        xmlw.close();

        String rtFile = dir + File.separator + "rts.html";

        xmlw = (XMLStreamWriter2) xmlf.createXMLStreamWriter(
                new FileOutputStream(new File(workingDir, rtFile)), enc);

        XHTMLHelper.initializeXHTMLFile(xmlw, "Ondex RelationTypes");

        xmlw.writeStartElement("h1");
        xmlw.writeCharacters("Ondex RelationTypes");
        xmlw.writeEndElement();

        for (RelationType rt : graphData.getRelationTypes()) {
            writeMetaDataTable(rt, xmlw);
        }

        XHTMLHelper.closeXHTMLFile(xmlw);
        xmlw.close();

        String rtsFile = dir + File.separator + "rtss.html";

        xmlw = (XMLStreamWriter2) xmlf.createXMLStreamWriter(
                new FileOutputStream(new File(workingDir, rtsFile)), enc);

        XHTMLHelper.initializeXHTMLFile(xmlw, "Ondex RelationTypes");

        xmlw.writeStartElement("h1");
        xmlw.writeCharacters("Ondex RelationTypes");
        xmlw.writeEndElement();

        for (RelationType arts : graphData.getRelationTypes()) {
            writeMetaDataTable(arts, xmlw);
        }

        XHTMLHelper.closeXHTMLFile(xmlw);
        xmlw.close();
    }

    /**
     * Generic method writes a MetaData object and its common attributes to a
     * xhtml table
     *
     * @param md   Object to write
     * @param xmlw StAX StreamWriter2
     * @throws XMLStreamException on StAX error
     */
    public void writeMetaDataTable(MetaData md, XMLStreamWriter2 xmlw)
            throws XMLStreamException {

        XHTMLHelper.createAnchor(xmlw, md.getId());

        xmlw.writeStartElement("table");
        xmlw.writeAttribute("border", "1");

        XHTMLTable.writeHeaderPair(xmlw, "Name", "Value");
        XHTMLTable.writeNameValuePair(xmlw, "ID", md.getId(), false);
        XHTMLTable.writeNameValuePair(xmlw, "Full Name", conditionLinks(md
                .getFullname()), true);
        XHTMLTable.writeNameValuePair(xmlw, "Description", conditionLinks(md
                .getDescription()), true);

        xmlw.writeEndElement();
        if (PRETTY)
            xmlw.writeCharacters("\n");

        xmlw.writeStartElement("br");
        xmlw.writeEndElement();
        if (PRETTY)
            xmlw.writeCharacters("\n");
    }

    /**
     * Creates a xhtml file containing various tables with a given concept in
     * the specified dir; format n.html where n is the concept internal id
     *
     * @param concept     the concept to write
     * @param xmlw        the stream to write the tables to
     * @param metadatadir metadata dir to be referenced in links from this html file
     * @throws FileNotFoundException when dir is invalid
     */
    public void writeConceptTables(ONDEXConcept concept, XMLStreamWriter2 xmlw,
                                   String relationsGDS, String conceptDir, String metadatadir)
            throws FileNotFoundException {
        try {

            String relativeMetadata = "../../" + metadatadir;

            xmlw.writeStartElement("table");
            xmlw.writeAttribute("border", "1");

            XHTMLTable.writeHeaderPair(xmlw, "Name", "Value");

            XHTMLTable.writeNameValuePair(xmlw, "Internal ID", String
                    .valueOf(concept.getId()), false);
            XHTMLTable.writeNameValuePair(xmlw, PID, concept.getPID(), false);
            XHTMLTable.writeNameValuePair(xmlw, "Annotation",
                    conditionLinks(concept.getAnnotation()), true);
            XHTMLTable.writeNameValuePair(xmlw, "Description",
                    conditionLinks(concept.getDescription()), true);

            XHTMLTable.writeNameValuePair(xmlw, "ConceptClass", "<a href=\""
                    + relativeMetadata + File.separator + "ccs.html#"
                    + concept.getOfType().getId()
                    + "\" onclick=\"popup(this.href);return false;\" >"
                    + concept.getOfType().getId() + "</a>", true);
            XHTMLTable.writeNameValuePair(xmlw, "DataSource", "<a href=\""
                    + relativeMetadata + File.separator + "cvs.html#"
                    + concept.getElementOf().getId()
                    + "\" onclick=\"popup(this.href);return false;\" >"
                    + concept.getElementOf().getId() + "</a>", true);

            XHTMLTable.writeNameValuePair(xmlw, "Evidence", formatEvidence(
                    concept.getEvidence(), relativeMetadata), true);

            ConceptName prefName = concept.getConceptName();
            if (prefName != null)
                XHTMLTable.writeNameValuePair(xmlw, "Prefered Name", prefName
                        .getName(), false);

            xmlw.writeEndElement();
            if (PRETTY)
                xmlw.writeCharacters("\n");

            xmlw.writeStartElement("h2");
            xmlw.writeCharacters("Concept Names Table");
            xmlw.writeEndElement();
            if (PRETTY)
                xmlw.writeCharacters("\n");

            xmlw.writeStartElement("table");
            xmlw.writeAttribute("border", "1");

            XHTMLTable.writeHeaders(xmlw, new String[]{"Names"});

            for (ConceptName conceptName : concept.getConceptNames()) {
                XHTMLTable.writeRow(xmlw,
                        new String[]{conceptName.getName()}, false);
            }

            xmlw.writeEndElement();
            if (PRETTY)
                xmlw.writeCharacters("\n");

            xmlw.writeStartElement("h2");
            xmlw.writeCharacters("Concept Accessions Table");
            xmlw.writeEndElement();
            if (PRETTY)
                xmlw.writeCharacters("\n");

            xmlw.writeStartElement("table");
            xmlw.writeAttribute("border", "1");

            XHTMLTable.writeHeaders(xmlw, new String[]{"DataSource", "Accession"});

            for (ConceptAccession conceptAccession : concept.getConceptAccessions()) {
                String cvId = conceptAccession.getElementOf().getId();
                String cvLink = "<a href=\"" + relativeMetadata
                        + File.separator + "cvs.html#" + cvId
                        + "\" onclick=\"popup(this.href);return false;\" >"
                        + cvId + "</a>";

                Map<String, String> cvPrefix = new HashMap<String, String>();
                cvPrefix.put("PROSITE",
                        "http://www.expasy.ch/cgi-bin/prosite-search-ac?");
                cvPrefix
                        .put("EMBL",
                                "http://www.ebi.ac.uk/cgi-bin/emblfetch?style=html&Submit=Go&id=");
                cvPrefix.put("IPRO",
                        "http://www.ebi.ac.uk/interpro/ISearch?query=");
                cvPrefix
                        .put(
                                "NC_GE",
                                "http://www.ncbi.nlm.nih.gov/sites/entrez?db=gene&cmd=Retrieve&dopt=Graphics&list_uids=");
                cvPrefix.put("UNIPROTKB", "http://beta.uniprot.org/uniprot/");
                cvPrefix
                        .put(
                                "TAIR",
                                "http://www.arabidopsis.org/servlets/Search?type=general&search_action=detail&method=1&show_obsolete=T&sub_type=protein&name=");
                cvPrefix
                        .put("PFAM",
                                "http://www.sanger.ac.uk/cgi-bin/Pfam/qquerypfam.pl?terms=");
                cvPrefix
                        .put("NLM",
                                "http://www.ncbi.nlm.nih.gov/sites/entrez?db=pubmed&cmd=search&term=");

                if (cvId.equals("EC")) {
                    String link = "<a href=\"http://expasy.org/enzyme/"
                            + conceptAccession.getAccession()
                            + "\" class=\"new-window\" >" + "(ExPASy_LINK)</a>";

                    link = link
                            + ",<a href=\"http://www.genome.ad.jp/dbget-bin/www_bget?enzyme+"
                            + conceptAccession.getAccession()
                            + "\"  class=\"new-window\" >"
                            + "(KEGG_ENZYME_LINK)</a>";

                    link = link
                            + ",<a href=\"http://metacyc.org/META/substring-search?type=NIL&object=+"
                            + conceptAccession.getAccession()
                            + "\"  class=\"new-window\" >"
                            + "(METACYC_LINK)</a>";

                    if (concept.getElementOf().getId().contains("BRENDA")) {
                        link = link
                                + ",<a href=\"http://www.brenda-enzymes.info/php/result_flat.php4?ecno="
                                + conceptAccession.getAccession()
                                + "\"  class=\"new-window\" >"
                                + "(BRENDA_LINK)</a>";
                    }
                    XHTMLTable.writeNameValuePair(xmlw, cvLink,
                            conceptAccession.getAccession() + link, true);
                } else if (cvId.equals("KEGG")
                        && concept.getOfType().getId().equals("Path")) {
                    String link = "<a href=\"http://www.genome.ad.jp/dbget-bin/www_bget?pathway+"
                            + conceptAccession.getAccession()
                            + "\"  class=\"new-window\" >"
                            + conceptAccession.getAccession() + "</a>";
                    XHTMLTable.writeNameValuePair(xmlw, cvLink, link, true);
                } else if (cvId.equals("KEGG")
                        && (concept.getOfType().getId().equals("Gene") || concept
                        .getOfType().getId().equals("Protein"))) {
                    String link = "<a href=\"http://www.genome.ad.jp/dbget-bin/www_bfind_sub?max_hit=1000&dbkey=genes&mode=bfind&keywords="
                            + conceptAccession.getAccession()
                            + "\" class=\"new-window\" >"
                            + conceptAccession.getAccession() + "</a>";
                    XHTMLTable.writeNameValuePair(xmlw, cvLink, link, true);
                } else if (cvId.equals("TAIR")
                        && concept.getOfType().getId().equals("Protein")) {
                    String link = "<a href=\"http://www.arabidopsis.org/servlets/Search?type=general&search_action=detail&method=1&show_obsolete=T&sub_type=protein&name="
                            + conceptAccession.getAccession()
                            + "\" class=\"new-window\" >"
                            + conceptAccession.getAccession() + "</a>";
                    XHTMLTable.writeNameValuePair(xmlw, cvLink, link, true);
                } else if (cvId.equals("TAIR")
                        && concept.getOfType().getId().equals("Gene")) {
                    String link = "<a href=\"http://www.arabidopsis.org/servlets/Search?type=general&search_action=detail&method=1&show_obsolete=T&sub_type=gene&name="
                            + conceptAccession.getAccession()
                            + "\" class=\"new-window\" >"
                            + conceptAccession.getAccession() + "</a>";
                    XHTMLTable.writeNameValuePair(xmlw, cvLink, link, true);
                } else if (cvPrefix.containsKey(cvId)) {
                    String link = "<a href=\"" + cvPrefix.get(cvId)
                            + conceptAccession.getAccession()
                            + "\"  class=\"new-window\" >"
                            + conceptAccession.getAccession() + "</a>";
                    XHTMLTable.writeNameValuePair(xmlw, cvLink, link, true);
                } else {
                    XHTMLTable.writeMixedPairRow(xmlw, cvLink, conceptAccession
                            .getAccession(), true, false);
                }
            }

            xmlw.writeEndElement();
            if (PRETTY)
                xmlw.writeCharacters("\n");

            xmlw.writeStartElement("h2");
            xmlw.writeCharacters("General Data Structure (Attribute) Table");
            xmlw.writeEndElement();
            if (PRETTY)
                xmlw.writeCharacters("\n");

            xmlw.writeStartElement("table");
            xmlw.writeAttribute("border", "1");

            XHTMLTable.writeHeaders(xmlw, new String[]{"AttributeName",
                    "Value"});

            for (Attribute conceptAttribute : concept.getAttributes()) {
                writeAttributeCell(xmlw, relativeMetadata, conceptAttribute);
            }

            xmlw.writeEndElement();
            if (PRETTY)
                xmlw.writeCharacters("\n");

            Set<ONDEXRelation> relations = graph
                    .getRelationsOfConcept(concept);

            xmlw.writeStartElement("h2");
            xmlw.writeCharacters("Concept Relations");
            xmlw.writeEndElement();
            if (PRETTY)
                xmlw.writeCharacters("\n");

            writeRelationsTable(relations, metadatadir, new File(workingDir,
                    conceptDir).getParentFile().getName(), // we want the root
                    // concept dir
                    relationsGDS, "../../", xmlw);

        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculates the target subdir for a given concept (dirs are grouped in
     * 1000 concepts)
     *
     * @param dir     concept root dir
     * @param concept the concept to assign a dir to
     * @return the sub dir for this concept
     */
    private String calculateConceptDir(String dir, ONDEXConcept concept) {
        Integer id = concept.getId();

        String folder = "0_to_1,000";
        if (id > 1000) {
            int thousands = (int) Math.floor(id / 1000d);
            folder = thousands + ",000_to_" + (thousands + 1) + ",000";
        }

        return dir + File.separator + folder;
    }

    /**
     * Formats evidence into a series of comma seperated EvidenceType metadata
     * html links
     *
     * @param evidence    The evidence to convert
     * @param metadatadir the dir to link to
     * @return A series of EvidenceType metadata html links
     */
    private String formatEvidence(Set<EvidenceType> evidence,
                                  String metadatadir) {
        StringBuilder evidenceLinks = new StringBuilder();
        for (EvidenceType evidenceType : evidence) {
            if (evidenceLinks.length() > 0) {
                evidenceLinks.append(", ");
            }
            evidenceLinks.append("<a href=\"" + metadatadir + File.separator
                    + "ets.html#" + evidenceType.getId()
                    + "\" onclick=\"popup(this.href);return false;\" >"
                    + evidenceType.getId() + "</a>");
        }
        return evidenceLinks.toString();
    }

    /**
     * Writes the given relations to a relation html index
     *
     * @param relations   the relations to write
     * @param metadatadir the metadata dir to link to
     * @param conceptDir  the concept dir to link to
     * @param relationGDS the name of the new relation html index to create
     * @throws XMLStreamException    on StAX error
     * @throws FileNotFoundException where relationFile is invalid
     */
    public void writeRelationsTable(Set<ONDEXRelation> relations,
                                    String metadatadir, String conceptDir, String relationGDS,
                                    String relativityToRoot, XMLStreamWriter2 xmlw)
            throws XMLStreamException, FileNotFoundException {

        xmlw.writeStartElement("table");
        xmlw.writeAttribute("border", "1");

        XHTMLTable.writeHeaders(xmlw, new String[]{"FromConcept",
                "RelationType", "ToConcept", "Evidence",
                "General Data Storage (Attribute)"});

        for (ONDEXRelation ONDEXRelation : relations) {
            String gdsLink = "none";

            Set<Attribute> gdss = ONDEXRelation
                    .getAttributes();
            if (gdss.size() > 0) {
                String gdsFile = relationGDS + File.separator + "gds_for_"
                        + ONDEXRelation.getId() + ".html";
                writeRelationGDSTable(ONDEXRelation, metadatadir, "../",
                        gdsFile);
                gdsLink = "<a href=\"" + relativityToRoot + gdsFile + "\">Attribute("
                        + gdss.size() + ")</a>";
            }

            RelationType typeSet = ONDEXRelation.getOfType();
            String typeSetLink = "<a href=\"" + relativityToRoot + metadatadir
                    + File.separator + "rtss.html#" + typeSet.getId()
                    + "\" onclick=\"popup(this.href);return false;\" >"
                    + typeSet.getId() + "</a>";

            String fromLink = "<a href=\""
                    + relativityToRoot
                    + calculateConceptDir(conceptDir, ONDEXRelation
                    .getFromConcept()) + File.separator
                    + ONDEXRelation.getFromConcept().getId()
                    + ".html\" class=\"new-window\" >"
                    + ONDEXRelation.getFromConcept().getId() + ". "
                    + getDefaultConceptName(ONDEXRelation.getFromConcept())
                    + "</a>";

            String toLink = "<a href=\""
                    + relativityToRoot
                    + calculateConceptDir(conceptDir, ONDEXRelation
                    .getToConcept()) + File.separator
                    + ONDEXRelation.getToConcept().getId()
                    + ".html\" class=\"new-window\" >"
                    + ONDEXRelation.getToConcept().getId() + ". "
                    + getDefaultConceptName(ONDEXRelation.getToConcept())
                    + "</a>";
          

            XHTMLHelper.createAnchor(xmlw, typeSet.getId());

            XHTMLTable.writeRow(xmlw, new String[]{
                    fromLink,
                    typeSetLink,
                    toLink,
                    formatEvidence(ONDEXRelation.getEvidence(),
                            relativityToRoot + metadatadir), gdsLink}, true);

        }

        xmlw.writeEndElement();
        if (PRETTY)
            xmlw.writeCharacters("\n");

    }

    /**
     * Writes a Relation Attribute Table in html
     *
     * @param relation    the relation to get Attribute objects from
     * @param metadataDir the metadata dir to reference
     * @param file        The file to write the gds to
     * @throws FileNotFoundException where file is invalid
     * @throws XMLStreamException    StAX error
     */
    private void writeRelationGDSTable(ONDEXRelation relation,
                                       String metadataDir, String relativityToRoot, String file)
            throws FileNotFoundException, XMLStreamException {
        XMLStreamWriter2 xmlw = (XMLStreamWriter2) xmlf.createXMLStreamWriter(
                new FileOutputStream(new File(workingDir, file)), enc);

        String title = relation.getOfType().getDescription();
        if (title.length() == 0) {
            title = relation.getOfType().getFullname();
        }

        if (title.length() == 0) {
            title = relation.getOfType().getId();
        }

        XHTMLHelper.initializeXHTMLFile(xmlw, relation.getId() + ". (" + title
                + ") " + " Attribute Table");

        xmlw.writeStartElement("h1");
        xmlw.writeCharacters(relation.getId() + ". ("
                + relation.getOfType().getDescription() + ") " + " Attribute Table");
        xmlw.writeEndElement();
        if (PRETTY)
            xmlw.writeCharacters("\n");

        xmlw.writeStartElement("table");
        xmlw.writeAttribute("border", "1");

        XHTMLTable
                .writeHeaders(xmlw, new String[]{"AttributeName", "Value"});

        for (Attribute relationAttribute : relation.getAttributes()) {
            writeAttributeCell(xmlw, relativityToRoot + metadataDir, relationAttribute);
        }

        xmlw.writeEndElement();
        if (PRETTY)
            xmlw.writeCharacters("\n");

        XHTMLHelper.closeXHTMLFile(xmlw);
        xmlw.close();
    }

    /**
     * Writes a Attribute object to xhtml table row (2 cells)
     *
     * @param xmlw        the stream to write to
     * @param metadataDir the metadata dir to reference
     * @param attribute         the Attribute to wrtite
     * @throws XMLStreamException on StAX error
     */
    private static void writeAttributeCell(XMLStreamWriter2 xmlw, String metadataDir,
                                     Attribute attribute) throws XMLStreamException {
        String attLink = "<a href=\"" + metadataDir + File.separator
                + "ats.html#" + attribute.getOfType().getId()
                + "\" onclick=\"popup(this.href);return false;\" >"
                + attribute.getOfType().getId() + "</a>";

        if (attribute.getOfType().getId().equals("TAXID")) {
            String link = "<a href=\"http://www.ncbi.nlm.nih.gov/sites/entrez?db=taxonomy&cmd=search&term="
                    + attribute.getValue().toString()
                    + "\" class=\"new-window\" >"
                    + attribute.getValue().toString() + "</a>";
            XHTMLTable.writeNameValuePair(xmlw, attLink, link, true);
        } else if (attribute.getOfType().getId().equals("URL")) {
            String link = "<a href=\"" + attribute.getValue().toString()
                    + "\" class=\"new-window\" >" + attribute.getValue().toString()
                    + "</a>";
            XHTMLTable.writeNameValuePair(xmlw, attLink, link, true);
        } else if (attribute.getOfType().getId().equals("PMID")) {
            String link = "<a href=\"http://www.ncbi.nlm.nih.gov/sites/entrez?db=pubmed&cmd=search&term="
                    + attribute.getValue().toString()
                    + "\" >"
                    + attribute.getValue().toString() + "</a>";
            XHTMLTable.writeNameValuePair(xmlw, attLink, link, true);
        } else {
            XHTMLTable.writeMixedPairRow(xmlw, attLink, attribute.getValue()
                    .toString(), true, false);
        }
    }

    /**
     * Gets the default concept name, defined in preference as; 1. Prefered
     * concept name 2. Annotation 3. Description 4. PID 5. Concept ID
     * <p/>
     * Where a name has a length > 0
     *
     * @param concept the Concept to get the name for
     * @return what we consider the best name
     */
    private String getDefaultConceptName(ONDEXConcept concept) {

        String defaultName = null;

        ConceptName cn = concept.getConceptName();
        if (cn != null) {
            defaultName = cn.getName();
        }

        if (defaultName == null || defaultName.trim().length() == 0) {
            for (ConceptName conceptName : concept.getConceptNames()) {
                cn = conceptName;
                if (cn.isPreferred()) {
                    defaultName = cn.getName();
                    break;
                }
            }

            // next try annotation
            if (defaultName == null || defaultName.trim().length() == 0) {
                defaultName = concept.getAnnotation();
            }
            // next try description
            if (defaultName == null || defaultName.trim().length() == 0) {
                defaultName = concept.getDescription();
            }
            // next try pid
            if (defaultName == null || defaultName.trim().length() == 0) {
                defaultName = concept.getPID();
            }
            // last resort to concept id
            if (defaultName == null || defaultName.trim().length() == 0) {
                defaultName = String.valueOf(concept.getId());
            }

            if (defaultName == null || defaultName.trim().length() == 0) {
                for (ConceptAccession conceptAccession : concept.getConceptAccessions()) {
                    defaultName = conceptAccession.getAccession();
                    break;
                }
            }

        }

        return defaultName;
    }

    // detect existing hyperlink pattern
    private static Pattern hyperlinkPattern = Pattern
            .compile(
                    "[a-z]*[\\://]*[a-z]+\\.[a-z]+\\.[a-z]+[/[a-z]+]*[.[a-z]+]*[^\\s|\\t|\\n|(|)]+",
                    Pattern.CASE_INSENSITIVE);

    /**
     * Formats hyper links in text to html
     *
     * @param text text to find hyper links in
     * @return text with links wrapped in xhtml hyperlinks
     */
    public String conditionLinks(String text) {

        HashSet<String> links = new HashSet<String>();
        Matcher matcher = hyperlinkPattern.matcher(text);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            String link = text.substring(start, end);
            links.add(link);
        }

        for (String link : links) {
            String hyperlink = "<a href=\"" + link
                    + "\" class=\"new-window\" >" + link + "</a>";
            text = text.replaceAll(link, hyperlink);
        }

        return text;
    }

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{new FileArgumentDefinition(
                FileArgumentDefinition.EXPORT_DIR, "directory for xhtml files",
                true, false, true, false)};

    }

    @Override
    public String getName() {
        return "xhtml exporter";
    }

    @Override
    public String getVersion() {
        return "alpha";
    }

    @Override
    public String getId() {
        return "xhtml";
    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public int getMaxProgress() {
        return maxProgress;
    }

    @Override
    public int getMinProgress() {
        return 0;
    }

    @Override
    public int getProgress() {
        return progress;
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public boolean isIndeterminate() {
        return false;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    private static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public Throwable getUncaughtException() {
        return caught;
    }

    @Override
    public boolean isAbortable() {
        return true;
    }
}
