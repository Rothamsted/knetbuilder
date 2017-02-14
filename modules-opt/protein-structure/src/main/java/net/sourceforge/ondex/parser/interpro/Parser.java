package net.sourceforge.ondex.parser.interpro;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.parser.ONDEXParser;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Parser for the interpro database just parses domains and EC terms at the moment
 *
 * @author hindlem
 */
@Status(description = "parses a very limited set of interpro", status = StatusType.EXPERIMENTAL)
public class Parser extends ONDEXParser
{


    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    /**
     * @return always null
     */
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
                        FileArgumentDefinition.INPUT_FILE_DESC, true, true, false, false)
        };
    }

    public String getName() {
        return "InterPro";
    }

    public String getVersion() {
        return "6.2.10";
    }

    @Override
    public String getId() {
        return "interpro";
    }

    public void start() throws ParserConfigurationException, IOException, SAXException, InvalidPluginArgumentException {
        String file = (String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE);


        EvidenceType ev = graph.getMetaData().getEvidenceType(MetaData.EV);
        DataSource ipro_dataSource = graph.getMetaData().getDataSource(MetaData.CV_InterPro);
        ConceptClass domain_cc = graph.getMetaData().getConceptClass(MetaData.CC_ProteinFamily);
        ConceptClass ec_cc = graph.getMetaData().getConceptClass(MetaData.CC_EC);
        DataSource ec_dataSource = graph.getMetaData().getDataSource(MetaData.CV_EC);

        RelationType cat_c = graph.getMetaData().getRelationType(MetaData.RT_cat_c);


        Map<String, ONDEXConcept> ec2Concept = new HashMap<String, ONDEXConcept>();

        DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();

        dbfactory.setIgnoringComments(true);
        dbfactory.setValidating(false);
        DocumentBuilder builder = dbfactory.newDocumentBuilder();
        Document doc = builder.parse(file);

        NodeList interproElements = doc.getElementsByTagName("interpro");
        for (int i = 0; i < interproElements.getLength(); i++) {
            Node node = interproElements.item(i);
            NamedNodeMap atts = node.getAttributes();
            String interproId = atts.getNamedItem("id").getNodeValue().trim();
            String type = atts.getNamedItem("type").getNodeValue().trim();
            String name = atts.getNamedItem("short_name").getNodeValue().trim();

            ONDEXConcept domain = graph.getFactory().createConcept(interproId, name, ipro_dataSource, domain_cc, ev);
            domain.createConceptAccession(interproId, ipro_dataSource, false);

            Set<String> ecTermsProcessed = new HashSet<String>();

            NodeList xrefs = ((Element) node).getElementsByTagName("db_xref");
            for (int j = 0; j < xrefs.getLength(); j++) {
                Node nodeXref = xrefs.item(j);

                NamedNodeMap xrefAtts = nodeXref.getAttributes();

                Node key = xrefAtts.getNamedItem("dbkey");
                Node db = xrefAtts.getNamedItem("db");

                if (key != null && db != null) {
                    //System.out.println("\t"+key.getNodeValue()+" "+db.getNodeValue());
                    if (db.getNodeValue().equalsIgnoreCase("EC")) {
                        String rawEcValue = key.getNodeValue().trim();
                        try {
                            String ecValue = fixECValue(rawEcValue);
                            ecTermsProcessed.add(ecValue);
                        } catch (InvalidValueException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            removeRedundantECTerms(ecTermsProcessed);

            for (String ecValue : ecTermsProcessed) {
                ONDEXConcept concept = ec2Concept.get(ecValue);
                if (concept == null) {
                    concept = graph.getFactory().createConcept(interproId, name, ipro_dataSource, ec_cc, ev);
                    concept.createConceptAccession(ecValue, ec_dataSource, false);
                    ec2Concept.put(ecValue, concept);
                }
                graph.getFactory().createRelation(domain, concept, cat_c, ev);
            }
        }
    }

    private static void removeRedundantECTerms(Set<String> ecTermsProcessed) {
        Set<String> redundantOnes = new HashSet<String>();

        for (String ecTerm : ecTermsProcessed) {
            String[] levels = ecTerm.split("\\.");

            if (ecTerm.contains("-")) {
                String shortecTerm = ecTerm.replaceAll(".-", "") + ".";
                for (String ecTermComp : ecTermsProcessed) {
                    if (!ecTerm.equals(ecTermComp) && ecTermComp.startsWith(shortecTerm)) {
                        redundantOnes.add(ecTerm);
                    }
                }
            }
        }
        ecTermsProcessed.removeAll(redundantOnes);
    }

    private String fixECValue(String ecValue) throws InvalidValueException {
        String level1 = "-";
        String level2 = "-";
        String level3 = "-";
        String level4 = "-";

        String[] components = ecValue.replaceAll(":", "").replaceAll("EC", "").trim().split("\\.");
        for (int i = 0; i < components.length; i++) {
            switch (i) {
                case 0:
                    try {
                        level1 = String.valueOf(Integer.parseInt(components[i]));
                        continue;
                    } catch (NumberFormatException e) {
                        throw new InvalidValueException("EC term " + ecValue + " does not start with a integer value, i.e. problem = \"" + components[i] + "\"");
                    }
                case 1:
                    try {
                        level2 = String.valueOf(Integer.parseInt(components[i]));
                        continue;
                    } catch (NumberFormatException e) {
                        break;
                    }
                case 2:
                    try {
                        level3 = String.valueOf(Integer.parseInt(components[i]));
                        continue;
                    } catch (NumberFormatException e) {
                        break;
                    }
                case 3:
                    try {
                        level4 = String.valueOf(Integer.parseInt(components[i]));
                        continue;
                    } catch (NumberFormatException e) {
                        break;
                    }
                default:
                    break;
            }

        }
        return level1 + '.' + level2 + '.' + level3 + '.' + level4;
    }

    private class InvalidValueException extends Exception {

        public InvalidValueException(String message) {
            super(message);
        }

    }
}