package net.sourceforge.ondex.parser.chemblactivity;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.ParsingErrorEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.tools.chemical.ChEMBLWrapper;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parser for bio activity data from ChEMBL database
 *
 * @author taubertj
 *
 */
@Authors(authors = {"Jan Taubert"}, emails = {"jantaubert at users.sourceforge.net"})
@Custodians(custodians = {"Jan Taubert"}, emails = {"jantaubert at users.sourceforge.net"})
@Status(status = StatusType.STABLE, description = "Tested December 2013 (Jacek Grzebyta)")
public class Parser extends ONDEXParser implements MetaData {

    private Logger log = Logger.getLogger(getClass());
    private static final String MODE = "MODE";

    /**
     * Different modes for extracting information
     *
     * @author taubertj
     *
     */
    public enum EXMODE {

        AssayToComps, AssayToTargets, CompToAssays, CompToTargets, TargetToAssays, TargetToComps, CompSimilarity
    };
    /**
     * Mapping of comment terms for normalization
     */
    private static Map<String, String> mapping = new HashMap<String, String>();

    static {
        mapping.put("Inconclusive", "Inconclusive");
        mapping.put("inconclusive", "Inconclusive");
        mapping.put("Not Active", "Not Active");
        mapping.put("Inactive", "Not Active");
        mapping.put("inactive", "Not Active");
        mapping.put("Active", "Active");
        mapping.put("active", "Active");
    }

    public static String getTagValue(String sTag, Element eElement) {
        if (eElement.getElementsByTagName(sTag) == null
                || eElement.getElementsByTagName(sTag).item(0) == null) {
            return null;
        }

        NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
                .getChildNodes();

        Node nValue = (Node) nlList.item(0);

        return nValue.getNodeValue();
    }
    AttributeName anRef, anOrg, anType, anComment, anConf, anUnit;
    ConceptClass ccAssay, ccComp, ccTarget;
    DataSource dsCHEMBL, dsASSAY, dsTARGET;
    EvidenceType evidencetype;
    RelationType rtAssay, rtComp, rtTarget, rtSim;
    Map<String, AttributeName> anTypes = new HashMap<String, AttributeName>();
    Map<String, ONDEXConcept> compounds = new HashMap<String, ONDEXConcept>();
    Map<String, ONDEXConcept> targets = new HashMap<String, ONDEXConcept>();

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{new StringArgumentDefinition(
                    MODE,
                    "In which mode to parse bioactivies. Options are: AssayToComps, AssayToTargets, CompToAssays, CompToTargets, TargetToAssays, TargetToComps",
                    false, "CompToAssays", false)};
    }

    @Override
    public String getId() {
        return "chemblactivity";
    }

    @Override
    public String getName() {
        return "ChEMBL BioActivity";
    }

    @Override
    public String getVersion() {
        return "28/06/2012";
    }

    /**
     * initialise Ondex meta-data
     */
    public void initMetaData() {

        anComment = graph.getMetaData().getAttributeName(AN_COMMENT);
        if (anComment == null) {
            anComment = graph.getMetaData().getFactory()
                    .createAttributeName(AN_COMMENT, String.class);
        }

        anConf = graph.getMetaData().getAttributeName(AN_CONF);
        if (anConf == null) {
            anConf = graph.getMetaData().getFactory()
                    .createAttributeName(AN_CONF, Double.class);
        }

        anOrg = graph.getMetaData().getAttributeName(AN_ORGANISM);
        if (anOrg == null) {
            anOrg = graph.getMetaData().getFactory()
                    .createAttributeName(AN_ORGANISM, String.class);
        }

        anRef = graph.getMetaData().getAttributeName(AN_REFERENCE);
        if (anRef == null) {
            anRef = graph.getMetaData().getFactory()
                    .createAttributeName(AN_REFERENCE, String.class);
        }

        anType = graph.getMetaData().getAttributeName(AN_TYPE);
        if (anType == null) {
            anType = graph.getMetaData().getFactory()
                    .createAttributeName(AN_TYPE, String.class);
        }

        anUnit = graph.getMetaData().getAttributeName(AN_UNIT);
        if (anUnit == null) {
            anUnit = graph.getMetaData().getFactory()
                    .createAttributeName(AN_UNIT, String.class);
        }

        ccAssay = graph.getMetaData().getConceptClass(CC_ASSAY);
        if (ccAssay == null) {
            ccAssay = graph.getMetaData().getFactory()
                    .createConceptClass(CC_ASSAY);
        }

        ccComp = graph.getMetaData().getConceptClass(CC_COMP);
        if (ccComp == null) {
            ccComp = graph.getMetaData().getFactory()
                    .createConceptClass(CC_COMP);
        }

        ccTarget = graph.getMetaData().getConceptClass(CC_TARGET);
        if (ccTarget == null) {
            ccTarget = graph.getMetaData().getFactory()
                    .createConceptClass(CC_TARGET);
        }

        dsASSAY = graph.getMetaData().getDataSource(DS_CHEMBLASSAY);
        if (dsASSAY == null) {
            dsASSAY = graph.getMetaData().getFactory()
                    .createDataSource(DS_CHEMBLASSAY);
        }

        dsCHEMBL = graph.getMetaData().getDataSource(DS_CHEMBL);
        if (dsCHEMBL == null) {
            dsCHEMBL = graph.getMetaData().getFactory()
                    .createDataSource(DS_CHEMBL);
        }

        dsTARGET = graph.getMetaData().getDataSource(DS_CHEMBLTARGET);
        if (dsTARGET == null) {
            dsTARGET = graph.getMetaData().getFactory()
                    .createDataSource(DS_CHEMBLTARGET);
        }

        evidencetype = graph.getMetaData().getEvidenceType(ET_IMPD);
        if (evidencetype == null) {
            evidencetype = graph.getMetaData().getFactory()
                    .createEvidenceType(ET_IMPD);
        }

        rtAssay = graph.getMetaData().getRelationType(RT_ASSAY);
        if (rtAssay == null) {
            rtAssay = graph.getMetaData().getFactory()
                    .createRelationType(RT_ASSAY);
        }

        rtComp = graph.getMetaData().getRelationType(RT_COMP);
        if (rtComp == null) {
            rtComp = graph.getMetaData().getFactory()
                    .createRelationType(RT_COMP);
        }

        rtTarget = graph.getMetaData().getRelationType(RT_TARGET);
        if (rtTarget == null) {
            rtTarget = graph.getMetaData().getFactory()
                    .createRelationType(RT_TARGET);
        }

        rtSim = graph.getMetaData().getRelationType(RT_SIM);
        if (rtSim == null) {
            rtSim = graph.getMetaData().getFactory().createRelationType(RT_SIM);
        }
    }

    /**
     * Extracts all assay information and creates a new Ondex concept
     *
     * @param eElement
     * @param created
     * @return
     */
    private ONDEXConcept createAssay(Element eElement) {

        // this is the assay concept
        ONDEXConcept assay = graph.getFactory().createConcept(
                getTagValue("assay__chemblid", eElement),
                getTagValue("assay__description", eElement), dsCHEMBL, ccAssay,
                evidencetype);

        // used to link back to CHEMBL
        assay.createConceptAccession(getTagValue("assay__chemblid", eElement),
                dsASSAY, false);

        // the journal reference
        String ref = getTagValue("reference", eElement);
        if (ref != null && !ref.equals("Unspecified")) {
            assay.createAttribute(anRef, getTagValue("reference", eElement),
                    true);
        }

        // the assay organism
        String org = getTagValue("organism", eElement);
        if (org != null && !org.equals("Unspecified")) {
            assay.createAttribute(anOrg, getTagValue("organism", eElement),
                    true);
        }

        // the activity type and value
        String type = getTagValue("bioactivity__type", eElement);
        type = type.replace(" ", "_");
        if (!anTypes.containsKey(type)) {
            AttributeName an = graph.getMetaData().getFactory()
                    .createAttributeName(type, Double.class);
            anTypes.put(type, an);
        }

        // parse value
        String value = getTagValue("value", eElement);
        if (value != null) {
            try {
                Double d = Double.valueOf(value);
                assay.createAttribute(anTypes.get(type), d, false);
            } catch (NumberFormatException nfe) {
            }
        }

        // unit as separate attribute
        String units = getTagValue("units", eElement);
        if (units != null && !units.equals("Unspecified")) {
            assay.createAttribute(anUnit, units, false);
        }

        // the activity comment
        String comment = getTagValue("activity__comment", eElement);
        if (comment != null && comment.trim().length() > 0
                && !comment.equals("Unspecified")) {
            if (mapping.containsKey(comment)) {
                comment = mapping.get(comment);
            }
            assay.createAttribute(anComment, comment, true);
        }

        return assay;
    }

    /**
     * Extracts all target information and creats a new Ondex concept
     *
     * @param eElement
     * @return
     */
    private ONDEXConcept createTarget(Element eElement) {
        // check for existing targets
        String key = getTagValue("target__chemblid", eElement);
        if (!targets.containsKey(key)) {
            // new target concept
            ONDEXConcept c = graph.getFactory().createConcept(key, dsCHEMBL,
                    ccTarget, evidencetype);
            c.createConceptName(getTagValue("target__name", eElement), true);
            c.createConceptAccession(key, dsTARGET, false);
            targets.put(key, c);
        }

        return targets.get(key);
    }

    /**
     * Extracts all compounds and creates Ondex concepts
     *
     * @param eElement
     * @return
     */
    private Set<ONDEXConcept> createCompounds(Element eElement) {
        Set<ONDEXConcept> result = new HashSet<ONDEXConcept>();

        // check for existing compounds
        String key = getTagValue("parent__cmpd__chemblid", eElement);
        if (!compounds.containsKey(key)) {
            // new compound concept
            ONDEXConcept c = graph.getFactory().createConcept(key,
                    "parent compound", dsCHEMBL, ccComp, evidencetype);
            c.createConceptAccession(key, dsCHEMBL, false);
            compounds.put(key, c);
        }
        result.add(compounds.get(key));

        // special case for another compound
        if (!key.equals(getTagValue("ingredient__cmpd__chemblid", eElement))) {
            key = getTagValue("ingredient__cmpd__chemblid", eElement);
            if (!compounds.containsKey(key)) {
                // new compound concept
                ONDEXConcept c = graph.getFactory().createConcept(key,
                        "ingredient compound", dsCHEMBL, ccComp, evidencetype);
                c.createConceptAccession(key, dsCHEMBL, false);
                compounds.put(key, c);

            }
            result.add(compounds.get(key));
        }

        return result;
    }

    /**
     * Retrieve all XML documents from web service
     *
     * @param accessions
     * @param mode
     * @return
     * @throws Exception
     */
    public Map<String, Document> retrieveXML(
            Map<String, Set<ONDEXConcept>> accessions, EXMODE mode)
            throws Exception {

        // check webservice to retrieve XML
        Map<String, Document> docs = new HashMap<String, Document>();
        for (String accession : accessions.keySet()) {

            // construct RESTful URL
            URL url = null;

            switch (mode) {
                case CompSimilarity:
                    log.debug("accession: " + accession);
                    url = new URL(Config.properties.getProperty(mode.name())
                            + accession + "/90");
                    break;
                default:
                    log.debug("accession: " + accession);
                    url = new URL(Config.properties.getProperty(mode.name())
                    + accession + "/bioactivities");
            }

            // System.out.println(url);

            // open http connection
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();

            // check for response code
            int code = uc.getResponseCode();

            if (code != 200) {
                // in the event of error
                String response = uc.getResponseMessage();
                fireEventOccurred(new ParsingErrorEvent("HTTP/1.x " + code
                        + " " + response, getCurrentMethodName()));
            } else {

                // get main content
                InputStream in = new BufferedInputStream(uc.getInputStream());

                // parse XML content
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory
                        .newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(in);
                doc.getDocumentElement().normalize();
                docs.put(accession, doc);

                //Solution for issue # http://ondex.rothamsted.ac.uk/tracking/browse/PLUGINS-102
                if (mode == EXMODE.CompSimilarity) {
                    issuePLUGINS102(doc);
                }
            }
        }
        return docs;
    }

    /**
     * Solution for issue #
     * http://ondex.rothamsted.ac.uk/tracking/browse/PLUGINS-102
     *
     * @param docs
     */
    private void issuePLUGINS102(Document doc) {
        log.debug("solution for issue PLUGINS-102");

        String pathCompoud = "/list/compound";
        String pathSimilarity = "similarity";
        String pathchemblId = "chemblId";
        XPath xpath = XPathFactory.newInstance().newXPath();

        log.trace("create xpath evaluator: " + xpath);

        try {
            NodeList compoundsNodeList = (NodeList) xpath.evaluate(pathCompoud, doc, XPathConstants.NODESET);

            log.trace("number of compounds: " + compoundsNodeList.getLength());

            // edit compounds
            for (int i = 0; i < compoundsNodeList.getLength(); i++) {
                Node compound = compoundsNodeList.item(i);
                // get chemblId
                String chemblId =
                        (String) xpath.evaluate(pathchemblId, compound, XPathConstants.STRING);
                log.trace("chembl Id: " + chemblId);

                // get value od similarity
                Node similarityNode =
                        (Node) xpath.evaluate(pathSimilarity, compound, XPathConstants.NODE);
                log.trace("similarity: " + similarityNode.getTextContent());

                if (similarityNode.getTextContent() != null) {
                    double similarity = Double.valueOf(similarityNode.getTextContent().trim());
                    
                    // the main functionality
                    double reducedSimilarity = similarity/100;
                    // set up precission 5 decimal places
                    reducedSimilarity = Math.round(reducedSimilarity*100000)/100000d;                    
                    
                    similarityNode.setTextContent(Double.toString(reducedSimilarity));
                }
            }

        } catch (XPathExpressionException e) {
            log.warn(e, e);
        }

    }

    /**
     * Turn web service documents into concepts and relations
     *
     * @param docs
     * @param accessions
     * @param mode
     * @return
     * @throws Exception
     */
    public Set<ONDEXConcept> parseActivities(Map<String, Document> docs,
            Map<String, Set<ONDEXConcept>> accessions, EXMODE mode)
            throws Exception {

        // utility class providing XML parsing
        ChEMBLWrapper wrapper = new ChEMBLWrapper(graph);

        Set<ONDEXConcept> created = new HashSet<ONDEXConcept>();
        for (String accession : accessions.keySet()) {

            // get XML document
            Document doc = docs.get(accession);
            if (doc != null) {

                // get all bioactivity elements
                NodeList nList = doc.getElementsByTagName("bioactivity");
                if (mode == EXMODE.CompSimilarity) {
                    nList = doc.getElementsByTagName("compound");
                }

                // iterate over all activities
                for (int temp = 0; temp < nList.getLength(); temp++) {

                    Node nNode = nList.item(temp);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element eElement = (Element) nNode;
                        ONDEXConcept assay, target;
                        ONDEXRelation r;
                        Set<ONDEXConcept> assays, comps, targets;

                        switch (mode) {
                            case AssayToComps:
                                // create compounds
                                comps = createCompounds(eElement);
                                created.addAll(comps);

                                // get all concepts with that accession
                                assays = accessions.get(accession);
                                for (ONDEXConcept comp : comps) {
                                    for (ONDEXConcept c : assays) {
                                        // HasComp
                                        graph.getFactory().createRelation(c, comp,
                                                rtComp, evidencetype);
                                    }
                                }

                                break;
                            case AssayToTargets:
                                // create target concept
                                target = createTarget(eElement);
                                created.add(target);

                                // get all concepts with that accession
                                assays = accessions.get(accession);
                                for (ONDEXConcept c : assays) {
                                    // HasTarget
                                    r = graph.getFactory().createRelation(c,
                                            target, rtTarget, evidencetype);
                                    r.createAttribute(
                                            anConf,
                                            Double.valueOf(getTagValue(
                                            "target__confidence", eElement)),
                                            false);
                                }

                                break;
                            case CompToAssays:
                                // create assay concept
                                assay = createAssay(eElement);
                                created.add(assay);

                                // get all concepts with that accession
                                comps = accessions.get(accession);
                                for (ONDEXConcept c : comps) {
                                    // HasAssay
                                    graph.getFactory().createRelation(c, assay,
                                            rtAssay, evidencetype);
                                }

                                break;
                            case CompToTargets:
                                // create target concept
                                target = createTarget(eElement);
                                created.add(target);

                                // get all concepts with that accession
                                comps = accessions.get(accession);
                                for (ONDEXConcept c : comps) {
                                    // HasTarget
                                    r = graph.getFactory().createRelation(c,
                                            target, rtTarget, evidencetype);
                                }

                                break;
                            case TargetToAssays:
                                // create assay concept
                                assay = createAssay(eElement);
                                created.add(assay);

                                // get all concepts with that accession
                                targets = accessions.get(accession);
                                for (ONDEXConcept c : targets) {
                                    // HasAssay
                                    graph.getFactory().createRelation(c, assay,
                                            rtAssay, evidencetype);
                                }

                                break;
                            case TargetToComps:
                                // create compounds
                                comps = createCompounds(eElement);
                                created.addAll(comps);

                                // get all concepts with that accession
                                targets = accessions.get(accession);
                                for (ONDEXConcept comp : comps) {
                                    for (ONDEXConcept c : targets) {
                                        // HasComp
                                        graph.getFactory().createRelation(c, comp,
                                                rtComp, evidencetype);
                                    }
                                }

                                break;
                            case CompSimilarity:
                                // create compound
                                ONDEXConcept compound = wrapper.parseCompound(
                                        eElement, null);
                                created.add(compound);

                                // get all concepts with that accession
                                targets = accessions.get(accession);

                                for (ONDEXConcept c : targets) {
                                    // similarity relation to existing
                                    r = graph.getFactory().createRelation(compound,
                                            c, rtSim, evidencetype);
                                    r.createAttribute(anConf, Double
                                            .valueOf(getTagValue("similarity",
                                            eElement)), false);
                                }

                                break;
                        }
                    }
                }
            }
        }
        return created;
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws Exception {

        // setup meta data
        initMetaData();

        EXMODE m = EXMODE.CompToAssays;
        String s = args.getUniqueValue(MODE).toString();
        if (s != null && s.trim().length() > 0) {
            // Options are: AssayToComps, AssayToTargets, CompToAssays,
            // CompToTargets, TargetToAssays, TargetToComps
            if (s.equalsIgnoreCase("AssayToComps")) {
                m = EXMODE.AssayToComps;
            } else if (s.equalsIgnoreCase("AssayToTargets")) {
                m = EXMODE.AssayToTargets;
            } else if (s.equalsIgnoreCase("CompToAssays")) {
                m = EXMODE.CompToAssays;
            } else if (s.equalsIgnoreCase("CompToTargets")) {
                m = EXMODE.CompToTargets;
            } else if (s.equalsIgnoreCase("TargetToAssays")) {
                m = EXMODE.TargetToAssays;
            } else if (s.equalsIgnoreCase("TargetToComps")) {
                m = EXMODE.TargetToComps;
            } else {
                fireEventOccurred(new WrongParameterEvent("Extraction mode "
                        + s + " is unknown. Ignoring.", getCurrentMethodName()));
            }
        }

        // get concepts to process
        Set<ONDEXConcept> concepts = null;
        DataSource ds = null;
        switch (m) {
            case AssayToComps:
                ds = dsASSAY;
                concepts = graph.getConceptsOfConceptClass(ccAssay);
                fireEventOccurred(new GeneralOutputEvent("Processing "
                        + concepts.size() + " assays.", getCurrentMethodName()));
                break;
            case AssayToTargets:
                ds = dsASSAY;
                concepts = graph.getConceptsOfConceptClass(ccAssay);
                fireEventOccurred(new GeneralOutputEvent("Processing "
                        + concepts.size() + " assays.", getCurrentMethodName()));
                break;
            case CompToAssays:
                ds = dsCHEMBL;
                concepts = graph.getConceptsOfConceptClass(ccComp);
                fireEventOccurred(new GeneralOutputEvent("Processing "
                        + concepts.size() + " compounds.", getCurrentMethodName()));
                break;
            case CompToTargets:
                ds = dsCHEMBL;
                concepts = graph.getConceptsOfConceptClass(ccComp);
                fireEventOccurred(new GeneralOutputEvent("Processing "
                        + concepts.size() + " compounds.", getCurrentMethodName()));
                break;
            case TargetToAssays:
                ds = dsTARGET;
                concepts = graph.getConceptsOfConceptClass(ccTarget);
                fireEventOccurred(new GeneralOutputEvent("Processing "
                        + concepts.size() + " targets.", getCurrentMethodName()));
                break;
            case TargetToComps:
                ds = dsTARGET;
                concepts = graph.getConceptsOfConceptClass(ccTarget);
                fireEventOccurred(new GeneralOutputEvent("Processing "
                        + concepts.size() + " targets.", getCurrentMethodName()));
                break;
            case CompSimilarity:
                // do nothing for now
                break;
            default:
                break;
        }

        // parse all accessions contained in graph
        Map<String, Set<ONDEXConcept>> accessions = new HashMap<String, Set<ONDEXConcept>>();
        for (ONDEXConcept c : concepts) {
            for (ConceptAccession ca : c.getConceptAccessions()) {
                if (ca.getElementOf().equals(ds)
                        && !accessions.containsKey(ca.getAccession())) {
                    accessions.put(ca.getAccession(),
                            new HashSet<ONDEXConcept>());
                }
                accessions.get(ca.getAccession()).add(c);
            }
        }
        fireEventOccurred(new GeneralOutputEvent(
                "Total accessions to be processed: " + accessions.size(),
                getCurrentMethodName()));

        Map<String, Document> docs = retrieveXML(accessions, m);
        int count = parseActivities(docs, accessions, m).size();
        fireEventOccurred(new GeneralOutputEvent("Total concepts created:"
                + count, getCurrentMethodName()));
    }
}
