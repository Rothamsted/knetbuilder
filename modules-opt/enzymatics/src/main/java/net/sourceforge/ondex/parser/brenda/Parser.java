package net.sourceforge.ondex.parser.brenda;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.DataURL;
import net.sourceforge.ondex.annotations.DatabaseTarget;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.annotations.metadata.*;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.config.ValidatorRegistry;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.*;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.validator.AbstractONDEXValidator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for the BRENDA database see: http://www.brenda-enzymes.info
 * <p>
 * <u>Example Parameter</u>
 * </p>
 * <p>
 * <table class="basic">
 * <tr><td><b>&lt;Parser name="brenda" datadir="/importdata/brenda"&gt;</b></td></tr>
 * <tr><td><b>&#160;&#160;&#160;&#160;&#160;&lt;Parameter name="Species"&gt;3702&lt;/Parameter&gt;</b></td></tr>
 * <tr><td><b>&lt;/Parser&gt;</b></td></tr>
 * </table>
 * </p>
 *
 * @author canevetc
 */
@Status(description = "not tested yet", status = StatusType.EXPERIMENTAL)
@DatabaseTarget(name = "BRENDA", description = "The BRENDA enzyme database", version = "July 2009", url = "http://www.brenda-enzymes.org")
@DataURL(name = "brenda zip and exception file", description = "brenda zip file and an exceptions file for data file inconsistancies", urls = {"http://www.brenda-enzymes.org/brenda_download/brenda_dl_0905.zip", "https://ondex.svn.sourceforge.net/svnroot/ondex/trunk/ondex-parent/modules/enzymatics/data/brenda/exception.txt"})
@DataSourceRequired(ids = {
        MetaData.CV_ec, MetaData.CV_brenda,
        MetaData.CV_cas, MetaData.CV_NLM})
@ConceptClassRequired(ids = {
        MetaData.cc_Publication, MetaData.cc_PublicationList,
        MetaData.cc_CelComp, MetaData.cc_Compound,
        MetaData.cc_Ec, MetaData.cc_Enzyme,
        MetaData.cc_Gene, MetaData.cc_Protein,
        MetaData.cc_Reaction, MetaData.cc_Tissue})
@EvidenceTypeRequired(ids = {MetaData.ET})
@RelationTypeRequired(ids = {
        MetaData.rt_ac_by, MetaData.rt_ca_by,
        MetaData.rt_cat_c, MetaData.rt_co_by,
        MetaData.rt_cs_by, MetaData.rt_in_by,
        MetaData.rt_is_a, MetaData.rt_loc_in,
        MetaData.rt_m_isp, MetaData.rt_pd_by})
@AttributeNameRequired(ids = {
        MetaData.at_AUTH, MetaData.at_taxID,
        MetaData.at_TITLE_JOURNAL, MetaData.at_YEAR,
        MetaData.ATTR_NAME_KI, MetaData.ATTR_NAME_KM})
public class Parser extends ONDEXParser implements ArgumentNames, MetaData {

    public enum Category {
        PROTEIN, RECOMMENDED_NAME, SYNONYMS, CAS_REGISTRY_NUMBER, SOURCE_TISSUE, LOCALIZATION,
        SUBSTRATE_PRODUCT, KM_VALUE, COFACTOR, ACTIVATING_COMPOUND, INHIBITORS, KI_VALUE, REFERENCE, NOVALUE;

        public static Category toCat(String str) {
            try {
                return valueOf(str);
            }
            catch (Exception ex) {
                return NOVALUE;
            }
        }
    }

    private DataSource dataSource_ec;
    private ConceptClass cc_ec;
    private DataSource dataSource_brenda;
    private EvidenceType et;
    private AttributeName attr;
    private AttributeName year;
    private AttributeName authors;
    private AttributeName title_journal;
    private DataSource dataSource_NLM;
    private RelationType co_by;
    private RelationType in_by;
    private ConceptClass celcomp;
    private ConceptClass tissue;
    private AttributeName KM;
    private AttributeName KI;
    private ConceptClass protein;
    private RelationType cat_c;
    private AbstractONDEXValidator taxValidator;
    private DataSource dataSource_cas;
    private ConceptClass compd;
    private ConceptClass reaction;
    private RelationType cs_by;
    private RelationType pd_by;
    private RelationType ca_by;
    private RelationType is_a;
    private RelationType m_isp;
    private RelationType located_in;
    private ConceptClass publi;
    private ConceptClass publi_list;
    private RelationType ac_by;
    private ConceptClass enzyme;

    public static int publist_count = 0;
    public static Pattern p_id = Pattern.compile("ID\\t(\\w+?\\.\\w+?\\.\\w+?\\.\\w+)"); // EC - accession number for the enzyme
    public static Pattern p_pr = Pattern.compile("PR\\t#(.*?)#\\s?(.*)\\s+.*"); // protein
    public static Pattern p_rn = Pattern.compile("RN\\t(.*)"); // recommended name
    public static Pattern p_sy = Pattern.compile("SY\\t\\s*(.*)"); // synonyms - name for the protein
    public static Pattern p_cr = Pattern.compile("CR\\t(.*)"); // CAS - accession number for the protein
    public static Pattern p_st = Pattern.compile("ST\\t#(.*)"); // source tissue
    public static Pattern p_lo = Pattern.compile("LO\\t#(.*)"); // localization
    public static Pattern p_sp = Pattern.compile("SP\\t#(.*?)#\\s?(.*)"); // substrate product
    public static Pattern p_km = Pattern.compile("KM\\t#(.*?)#\\s?(.*?)\\{(.*?)\\}.*"); // KM value
    public static Pattern p_cf = Pattern.compile("CF\\t#(.*)"); // cofactor
    public static Pattern p_ac = Pattern.compile("AC\\t#(.*)"); // activating compound
    public static Pattern p_in = Pattern.compile("IN\\t#(.*)"); // inhibitor
    public static Pattern p_ki = Pattern.compile("KI\\t#(.*?)#\\s?(.*?)\\{(.*?)\\}.*"); // KI value
    public static Pattern p_ref = Pattern.compile("RF\\t<(\\d+)>\\s?(.*)"); // reference
    public static Pattern commaPattern = Pattern.compile(",");
    public static Pattern forwardSlashPattern = Pattern.compile("///");
    public static Pattern plusPattern = Pattern.compile(" \\+ ");
    public static boolean debug = true;

    private static Set<Integer> taxIDsToParse;

    /**
     * initializes all metadata required for this parser
     */
    private void initMetaData() {

        dataSource_ec = graph.getMetaData().getDataSource(CV_ec);
        if (dataSource_ec == null) {
            fireEventOccurred(new DataSourceMissingEvent(CV_ec, Parser.getCurrentMethodName()));
        }
        cc_ec = graph.getMetaData().getConceptClass(cc_Ec);
        if (cc_ec == null) {
            fireEventOccurred(new DataSourceMissingEvent(cc_Ec, Parser.getCurrentMethodName()));
        }
        dataSource_brenda = graph.getMetaData().getDataSource(CV_brenda);
        if (dataSource_brenda == null) {
            fireEventOccurred(new DataSourceMissingEvent(CV_brenda, Parser.getCurrentMethodName()));
        }
        attr = graph.getMetaData().getAttributeName(MetaData.at_taxID);
        if (attr == null) {
            fireEventOccurred(new EvidenceTypeMissingEvent(MetaData.at_taxID, Parser.getCurrentMethodName()));
        }
        year = graph.getMetaData().getAttributeName(MetaData.at_YEAR);
        if (year == null) {
            fireEventOccurred(new EvidenceTypeMissingEvent(MetaData.at_YEAR, Parser.getCurrentMethodName()));
        }
        authors = graph.getMetaData().getAttributeName(MetaData.at_AUTH);
        if (authors == null) {
            fireEventOccurred(new EvidenceTypeMissingEvent(MetaData.at_AUTH, Parser.getCurrentMethodName()));
        }
        title_journal = graph.getMetaData().getAttributeName(MetaData.at_TITLE_JOURNAL);
        if (title_journal == null) {
            fireEventOccurred(new EvidenceTypeMissingEvent(MetaData.at_TITLE_JOURNAL, Parser.getCurrentMethodName()));
        }
        dataSource_NLM = graph.getMetaData().getDataSource(MetaData.CV_NLM);
        if (dataSource_NLM == null) {
            fireEventOccurred(new DataSourceMissingEvent(MetaData.CV_NLM, Parser.getCurrentMethodName()));
        }
        et = graph.getMetaData().getEvidenceType(ET);
        if (et == null) {
            fireEventOccurred(new EvidenceTypeMissingEvent(ET, Parser.getCurrentMethodName()));
        }
        co_by = graph.getMetaData().getRelationType(rt_co_by);
        if (co_by == null) {
            fireEventOccurred(new RelationTypeMissingEvent(rt_co_by, Parser.getCurrentMethodName()));
        }
        in_by = graph.getMetaData().getRelationType(rt_in_by);
        if (in_by == null) {
            fireEventOccurred(new RelationTypeMissingEvent(rt_in_by, Parser.getCurrentMethodName()));
        }
        celcomp = graph.getMetaData().getConceptClass(cc_CelComp);
        if (celcomp == null) {
            fireEventOccurred(new ConceptClassMissingEvent(cc_CelComp, Parser.getCurrentMethodName()));
        }
        tissue = graph.getMetaData().getConceptClass(cc_Tissue);
        if (tissue == null) {
            fireEventOccurred(new ConceptClassMissingEvent(cc_Tissue, Parser.getCurrentMethodName()));
        }
        protein = graph.getMetaData().getConceptClass(cc_Protein);
        if (protein == null) {
            fireEventOccurred(new ConceptClassMissingEvent(cc_Protein, Parser.getCurrentMethodName()));
        }
        enzyme = graph.getMetaData().getConceptClass(cc_Enzyme);
        if (enzyme == null) {
            fireEventOccurred(new ConceptClassMissingEvent(cc_Enzyme, Parser.getCurrentMethodName()));
        }
        cat_c = graph.getMetaData().getRelationType(rt_cat_c);
        if (cat_c == null) {
            fireEventOccurred(new RelationTypeMissingEvent(rt_cat_c, Parser.getCurrentMethodName()));
        }
        dataSource_cas = graph.getMetaData().getDataSource(CV_cas);
        if (dataSource_cas == null) {
            fireEventOccurred(new DataSourceMissingEvent(CV_cas, Parser.getCurrentMethodName()));
        }
        compd = graph.getMetaData().getConceptClass(cc_Compound);
        if (compd == null) {
            fireEventOccurred(new ConceptClassMissingEvent(cc_Compound, Parser.getCurrentMethodName()));
        }
        reaction = graph.getMetaData().getConceptClass(cc_Reaction);
        if (reaction == null) {
            fireEventOccurred(new ConceptClassMissingEvent(cc_Reaction, Parser.getCurrentMethodName()));
        }
        cs_by = graph.getMetaData().getRelationType(rt_cs_by);
        if (cs_by == null) {
            fireEventOccurred(new RelationTypeMissingEvent(rt_cs_by, Parser.getCurrentMethodName()));
        }
        pd_by = graph.getMetaData().getRelationType(rt_pd_by);
        if (pd_by == null) {
            fireEventOccurred(new RelationTypeMissingEvent(rt_pd_by, Parser.getCurrentMethodName()));
        }
        ca_by = graph.getMetaData().getRelationType(rt_ca_by);
        if (ca_by == null) {
            fireEventOccurred(new RelationTypeMissingEvent(rt_ca_by, Parser.getCurrentMethodName()));
        }
        is_a = graph.getMetaData().getRelationType(rt_is_a);
        if (is_a == null) {
            fireEventOccurred(new RelationTypeMissingEvent(rt_is_a, Parser.getCurrentMethodName()));
        }
        m_isp = graph.getMetaData().getRelationType(rt_m_isp);
        if (m_isp == null) {
            fireEventOccurred(new RelationTypeMissingEvent(rt_m_isp, Parser.getCurrentMethodName()));
        }
        located_in = graph.getMetaData().getRelationType(rt_loc_in);
        if (located_in == null) {
            fireEventOccurred(new RelationTypeMissingEvent(rt_loc_in, Parser.getCurrentMethodName()));
        }
        publi = graph.getMetaData().getConceptClass(cc_Publication);
        if (publi == null) {
            fireEventOccurred(new ConceptClassMissingEvent(cc_Publication, Parser.getCurrentMethodName()));
        }
        publi_list = graph.getMetaData().getConceptClass(cc_PublicationList);
        if (publi_list == null) {
            fireEventOccurred(new ConceptClassMissingEvent(cc_PublicationList, Parser.getCurrentMethodName()));
        }
        ac_by = graph.getMetaData().getRelationType(rt_ac_by);
        if (ac_by == null) {
            fireEventOccurred(new RelationTypeMissingEvent(rt_ac_by, Parser.getCurrentMethodName()));
        }
        KM = graph.getMetaData().getAttributeName("KM");
        if (KM == null) {
            fireEventOccurred(new AttributeNameMissingEvent("KM", Parser.getCurrentMethodName()));
        }
        KI = graph.getMetaData().getAttributeName("KI");
        if (KI == null) {
            fireEventOccurred(new AttributeNameMissingEvent("KI", Parser.getCurrentMethodName()));
        }

        taxValidator = ValidatorRegistry.validators.get("taxonomy");
    }

    @Override
    public String[] requiresValidators() {
        return new String[]{"taxonomy"};
    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition[]{
                new StringArgumentDefinition(
                        SPECIES_ARG,
                        SPECIES_ARG_DESC,
                        false, null, true),
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_DIR, FileArgumentDefinition.INPUT_DIR_DESC, true, true, true, false)
        };
    }

    public String getName() {
        return "BRENDA Parser";
    }

    public String getVersion() {
        return "07-Feb-08";
    }

    @Override
    public String getId() {
        return "brenda";
    }


    /**
     * Convenience method for outputing the current method name in a dynamic way
     *
     * @return the calling method name
     */
    public static String getCurrentMethodName() {
        Exception e = new Exception();
        StackTraceElement trace = e.fillInStackTrace().getStackTrace()[1];
        String name = trace.getMethodName();
        String className = trace.getClassName();
        int line = trace.getLineNumber();
        return "[CLASS:" + className + " - METHOD:" + name + " LINE:" + line + "]";
    }


    public void start() throws InvalidPluginArgumentException {

        File dir = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_DIR));

        taxIDsToParse = null;
        String[] values = (String[]) args.getObjectValueArray(SPECIES_ARG);
        if (values != null) {
            for (String value : values) {

                if (taxIDsToParse == null) {
                    taxIDsToParse = new HashSet<Integer>();
                }

                int taxId = -1;
                try {
                    taxId = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    String taxIdSt = (String) taxValidator.validate((String) value);
                    if (taxIdSt != null) {
                        taxId = Integer.parseInt(taxIdSt);
                    } else {
                        fireEventOccurred(new WrongParameterEvent(value + " is not a valid taxonomy ignoring", Parser.getCurrentMethodName()));

                    }
                }
                if (taxId > 0) {
                    taxIDsToParse.add(taxId);
                }
            }
        }
        initMetaData();

        //index BRENDA ref number to PMIDs and other details about publications
        HashMap<String, String> refToPublidetails = new HashMap<String, String>();

        //index BRENDA protein number to graph id for protein concepts
        HashMap<String, Integer> pidToGid = new HashMap<String, Integer>();

        //index BRENDA enzyme number to graph id for protein concepts
        HashMap<String, Integer> eidToGid = new HashMap<String, Integer>();

        //index BRENDA substrate/product number to graph ids for reactions
        // to later on add KM and KI values as relation Attribute
        HashMap<String, ArrayList<Integer>> kidToGid = new HashMap<String, ArrayList<Integer>>();

        // in PR section, each protenz is assigned a publication list (publist)
        // Each publist is uniq and corresponds to a list of protenz
        HashMap<String, ArrayList<String>> publistToProtenz = new HashMap<String, ArrayList<String>>();

        // list of BRENDA publication IDs to graph id for publication list concept
        HashMap<String, Integer> publistToGid = new HashMap<String, Integer>();

        // publication BRENDA ID to graph id for publication concept
        HashMap<String, Integer> publicationToGid = new HashMap<String, Integer>();

        // compound name to graph id for compound concept
        HashMap<String, Integer> compoundToGid = new HashMap<String, Integer>();

        // location name to graph id for compound concept (STLO)
        HashMap<String, Integer> locToGid = new HashMap<String, Integer>();

        // location name to Brenda ids for protenz concepts (STLO)
        HashMap<String, String> locToBid = new HashMap<String, String>();

        ArrayList<Compound> cofactors = null;
        ArrayList<Compound> inhibitors = null;
        ArrayList<Compound> activators = null;
        boolean firstline = true;
        boolean newEC = false;

        // get the content of the file
        FileReader myFile = null;
        BufferedReader buff = null;
        try {
            // server
            myFile = new FileReader(dir.getAbsolutePath() + "/brenda_download.txt");
            //myFile = new FileReader(dir + "/test_111104.txt");
            buff = new BufferedReader(myFile);

            ONDEXConcept ec = null;
            Set<ONDEXConcept> proteins = new HashSet<ONDEXConcept>();
            Set<ONDEXConcept> enzymes = new HashSet<ONDEXConcept>();

            while (buff.ready()) {
                String line = buff.readLine().trim();
                //System.out.println("\n*** LINE: " + line);

                if (firstline) {
                    ec = createNewEC(graph, line);
                    firstline = false;
                    continue;
                } else {
                    if (line.startsWith("///")) {
                        if (debug) {
                            System.out.println("\n\n\nNew EC entry found -------------------------------------------------");
                        }

                        // make relations from information stored in hashmaps
                        makeRelations_proteins(graph, publistToGid, publicationToGid, refToPublidetails, publistToProtenz);
                        makeRelations_compounds(graph, eidToGid, refToPublidetails, publistToGid, publicationToGid, activators, ac_by);
                        makeRelations_compounds(graph, eidToGid, refToPublidetails, publistToGid, publicationToGid, cofactors, co_by);
                        makeRelations_compounds(graph, eidToGid, refToPublidetails, publistToGid, publicationToGid, inhibitors, in_by);

                        // prepare for next BRENDA entry
                        newEC = true;
                        // re-initialise data structures
                        enzymes.clear();
                        proteins.clear();
                        refToPublidetails.clear();
                        pidToGid.clear();
                        eidToGid.clear();
                        kidToGid.clear();
                        publistToProtenz.clear();
                        publistToGid.clear();
                        publicationToGid.clear();
                        compoundToGid.clear();
                        locToGid.clear();
                        locToBid.clear();
                        activators = null;
                        cofactors = null;
                        inhibitors = null;
                        continue;
                    } else {
                        if (newEC) {
                            ec = createNewEC(graph, line);
                            newEC = false;
                            continue;
                        } else {
                            switch (Category.toCat(line)) {
                                case PROTEIN:
                                    // read until empty line
                                    ArrayList<String> section = new ArrayList<String>();
                                    line = buff.readLine().trim();
                                    // when a category end there is one or more empty lines
                                    while (line.length() != 0) {
                                        //System.out.println("*** section: " + line);
                                        section.add(line);
                                        line = buff.readLine().trim();
                                    }
                                    // call method createConcepts4eachSpe with pattern and section of interest
                                    createConcepts4eachSpe(ec, graph, proteins, enzymes, publistToProtenz, pidToGid, eidToGid, section);
                                    break;

                                case RECOMMENDED_NAME:
                                    section = new ArrayList<String>();
                                    line = buff.readLine().trim();
                                    addRN(proteins, enzymes, graph, line);
                                    break;

                                case SYNONYMS:
                                    section = new ArrayList<String>();
                                    line = buff.readLine().trim();
                                    while (line.length() != 0) {
                                        section.add(line);
                                        line = buff.readLine().trim();
                                    }
                                    addSY(graph, proteins, pidToGid, section);
                                    break;

                                case CAS_REGISTRY_NUMBER:
                                    section = new ArrayList<String>();
                                    line = buff.readLine().trim();
                                    while (line.length() != 0) {
                                        section.add(line);
                                        line = buff.readLine().trim();
                                    }
                                    addCAS(graph, proteins, enzymes, section);
                                    break;

                                case SOURCE_TISSUE:
                                    section = new ArrayList<String>();
                                    line = buff.readLine().trim();
                                    while (line.length() != 0) {
                                        section.add(line);
                                        line = buff.readLine().trim();
                                    }
                                    addSTLO(graph, tissue, locToGid, locToBid, pidToGid, p_st, section);
                                    break;

                                case LOCALIZATION:
                                    section = new ArrayList<String>();
                                    line = buff.readLine().trim();
                                    while (line.length() != 0) {
                                        section.add(line);
                                        line = buff.readLine().trim();
                                    }
                                    addSTLO(graph, celcomp, locToGid, locToBid, pidToGid, p_lo, section);
                                    break;

                                case SUBSTRATE_PRODUCT:
                                    section = new ArrayList<String>();
                                    line = buff.readLine().trim();
                                    while (line.length() != 0) {
                                        section.add(line);
                                        line = buff.readLine().trim();
                                    }
                                    addSP(graph, eidToGid, kidToGid, compoundToGid, section);
                                    break;

                                case KM_VALUE:
                                    section = new ArrayList<String>();
                                    line = buff.readLine().trim();
                                    while (line.length() != 0) {
                                        section.add(line);
                                        line = buff.readLine().trim();
                                    }
                                    addKMKI(graph, pidToGid, kidToGid, KM, p_km, section);
                                    break;

                                case COFACTOR:
                                    section = new ArrayList<String>();
                                    line = buff.readLine().trim();
                                    while (line.length() != 0) {
                                        section.add(line);
                                        line = buff.readLine().trim();
                                    }

                                    cofactors = addComp(graph, compoundToGid, "CF", section);
                                    break;

                                case ACTIVATING_COMPOUND:
                                    section = new ArrayList<String>();
                                    line = buff.readLine().trim();
                                    while (line.length() != 0) {
                                        section.add(line);
                                        line = buff.readLine().trim();
                                    }
                                    activators = addComp(graph, compoundToGid, "AC", section);
                                    break;

                                case INHIBITORS:
                                    section = new ArrayList<String>();
                                    line = buff.readLine().trim();
                                    while (line.length() != 0) {
                                        section.add(line);
                                        line = buff.readLine().trim();
                                    }

                                    inhibitors = addComp(graph, compoundToGid, "IN", section);
                                    break;

                                case KI_VALUE:
                                    section = new ArrayList<String>();
                                    line = buff.readLine().trim();
                                    while (line.length() != 0) {
                                        section.add(line);
                                        line = buff.readLine().trim();
                                    }
                                    addKMKI(graph, pidToGid, kidToGid, KI, p_ki, section);
                                    break;

                                case REFERENCE:
                                    section = new ArrayList<String>();
                                    line = buff.readLine().trim();
                                    while (line.length() != 0 && buff.ready()) {
                                        section.add(line);
                                        line = buff.readLine().trim();
                                    }
                                    addRef(graph, refToPublidetails, section);
                                    break;
                                default:
//		    					if (debug){
//		    						System.out.println("Default case");
//		    					}
                                    break;

                            }// end switch
                        }// end else
                    }// end else
                }// end else
            } // end while
        } // end try

        catch (IOException e) {
            e.printStackTrace();
            System.out.println("Can't read brenda");
        }
        finally { // Closing the streams
            try {
                System.out.println("\n\nTrying to close the streams");
                buff.close();
                myFile.close();
                System.out.println("Closing the streams\n");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }// end finally
    }// end setONDEXGraph method


    /**
     * Create new concept EC
     *
     * @param g     graph
     * @param entry line to parse
     *              returns the EC Concept of the EC block
     */
    public ONDEXConcept createNewEC(ONDEXGraph g, String entry) {

        if (debug) {
            System.out.println("\nHello createNewEC");
        }
        Matcher m = null;
        boolean matchFound = false;
        String id = null;
        ONDEXConcept c_ec = null;

        m = p_id.matcher(entry);
        matchFound = m.find();
        if (matchFound) {
            id = m.group(1);
            c_ec = g.getFactory().createConcept(id, dataSource_brenda, cc_ec, et);
            c_ec.createConceptAccession(id, dataSource_ec, false);
            if (debug) {
                System.out.println("--EC ID is: " + id); //c_ec.getId(s) );
            }

            // add the new EC to the enzymes, proteins ArrayList<Integer>[]
            return c_ec;
        }
        return null;
    }


    /**
     * For each organism, create concepts for protein and enzyme
     *
     * @param ec       EC concept for the EC block
     * @param g        graph
     * @param proteins list of protein concepts
     * @param enzymes  list of enzyme concepts
     * @param p2g      hashmap from BRENDA protein id to graph protein id
     * @param e2g      hashmap from BRENDA enzyme id to graph enzyme id
     * @param sec      section to parse
     */
    public void createConcepts4eachSpe(ONDEXConcept ec, ONDEXGraph g, Set<ONDEXConcept> proteins,
                                       Set<ONDEXConcept> enzymes, HashMap<String, ArrayList<String>> p2p,
                                       HashMap<String, Integer> p2g, HashMap<String, Integer> e2g, ArrayList<String> sec) throws InvalidPluginArgumentException {
        File dir = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_DIR));

        if (debug) {
            System.out.println("\nHello createConcepts4eachSpe");
        }

        Matcher m = null;
        boolean matchFound = false;
        String pr = null; // protein
        String sp = null; // species
        String ref = null; // references
        ONDEXConcept c_p = null; // protein concept
        ONDEXConcept c_e = null; // enzyme concept
        String protenz = null; // String which stores both ids for proteins and enzymes

        Iterator<String> itr = sec.iterator();
        while (itr.hasNext()) {
            String line = itr.next();
            //System.out.println("line is: " + line);
            m = p_pr.matcher(line);
            matchFound = m.find();
            if (matchFound) {

                // capturing protein id
                pr = m.group(1);
                if (debug) {
                    System.out.println("\n-----PR is: " + pr);
                }

                // capturing species
                sp = m.group(2).trim();
                int index = sp.indexOf('(');
                // checking for comments in () before refs in <>
                if (index != -1) {
                    sp = sp.substring(0, index).trim();
                }

//	    		// Creating concepts only for Arabidopsis thaliana (taxId 3702)
//    			// to check if dealing with only one specie will still cause Java heap space error
//    			if ( sp.indexOf("Arabidopsis thaliana") != -1){

                String taxId = (String) taxValidator.validate(sp);
                if (debug) {
                    System.out.println("-----SP is: " + sp + " --> " + taxId);
                }

                // Dealing with taxonomy exceptions
                if (taxId == null) {
                    System.out.println("Dealing with taxonomy exceptions");

                    // does sp contain a very common species name
                    // if so, retrieve it from sp
                    if (sp.indexOf("mus musculus") != -1 || sp.indexOf("Mus musculus") != -1) {
                        sp = "Mus musculus";
                        taxId = (String) taxValidator.validate(sp);
                        if (taxId == null ||
                                taxIDsToParse != null && !taxIDsToParse.contains(Integer.parseInt(taxId))) {
                            continue;
                        }
                        if (debug) {
                            System.out.println("Found Mus musculus and taxId: " + taxId);
                        }
                    } else {
                        if (sp.indexOf("homo sapiens") != -1 || sp.indexOf("Homo sapiens") != -1) {
                            sp = "Homo sapiens";
                            taxId = (String) taxValidator.validate(sp);
                            if (taxId == null ||
                                    taxIDsToParse != null && !taxIDsToParse.contains(Integer.parseInt(taxId))) {
                                continue;
                            }
                            if (debug) {
                                System.out.println("Found Homo sapiens and taxId: " + taxId);
                            }
                        } else {
                            if (sp.indexOf("arabidopsis thaliana") != -1 || sp.indexOf("Arabidopsis thaliana") != -1) {
                                sp = "Arabidopsis thaliana";
                                taxId = (String) taxValidator.validate(sp);
                                if (taxId == null ||
                                        taxIDsToParse != null && !taxIDsToParse.contains(Integer.parseInt(taxId))) {
                                    continue;
                                }
                                if (debug) {
                                    System.out.println("Found Arabidopsis thaliana and taxId: " + taxId);
                                }
                            } else {
                                if (sp.indexOf("rattus norvegicus") != -1 || sp.indexOf("Rattus norvegicus") != -1) {
                                    sp = "Rattus norvegicus";
                                    taxId = (String) taxValidator.validate(sp);
                                    if (taxId == null ||
                                            taxIDsToParse != null && !taxIDsToParse.contains(Integer.parseInt(taxId))) {
                                        continue;
                                    }
                                    if (debug) {
                                        System.out.println("Found Rattus norvegicus and taxId: " + taxId);
                                    }
                                } else {
                                    if (sp.indexOf("saccharomyces cerevisiae") != -1 || sp.indexOf("Saccharomyces cerevisiae") != -1) {
                                        sp = "Saccharomyces cerevisiae";
                                        taxId = (String) taxValidator.validate(sp);
                                        if (taxId == null ||
                                                taxIDsToParse != null && !taxIDsToParse.contains(Integer.parseInt(taxId))) {
                                            continue;
                                        }
                                        if (debug) {
                                            System.out.println("Found Saccharomyces cerevisiae and taxId: " + taxId);
                                        }
                                    } else {
                                        // look into exceptions file
                                        FileReader exceptionsFile = null;
                                        BufferedReader exceptionsBuff = null;
                                        try {
                                            // server
                                            //exceptionsFile = new FileReader("/home/usern/canevetc/ONDEXPlugins/exceptions.txt");
                                            exceptionsFile = new FileReader(dir.getAbsolutePath() + "/exceptions.txt");
                                            exceptionsBuff = new BufferedReader(exceptionsFile);
                                            while (exceptionsBuff.ready()) {
                                                String exception = exceptionsBuff.readLine().trim();
                                                String bad_name = exception.substring(0, exception.indexOf(",")).trim();
                                                String good_name = exception.substring(exception.indexOf(",") + 1).trim();
                                                if (bad_name.compareToIgnoreCase(sp) == 0) {
                                                    sp = good_name;
                                                    taxId = (String) taxValidator.validate(sp);
                                                    if (taxId == null ||
                                                            taxIDsToParse != null && !taxIDsToParse.contains(Integer.parseInt(taxId))) {
                                                        continue;
                                                    }
                                                    if (debug) {
                                                        System.out.println("Correct name: " + sp + " and taxId: " + taxId);
                                                    }
                                                    break;
                                                } else {
                                                    if (bad_name.compareToIgnoreCase(sp) > 0) {
                                                        // sp > current bad name
                                                        // we have gone past where the word would have been (exceptions file is sorted)
                                                        if (debug) {
                                                            System.out.println("We have gone past where the word would have been");
                                                            System.out.println("current name was: " + bad_name);
                                                        }
                                                        break;
                                                    }
                                                }
                                            }

                                        }
                                        catch (IOException e) {
                                            e.printStackTrace();
                                            System.out.println("Can't read exceptions file");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (taxId == null ||
                        taxIDsToParse != null && !taxIDsToParse.contains(Integer.parseInt(taxId))) {
//	    			System.out.println("Going to next loop");
//	    			continue;
                    taxId = "0000";
                }

                if (taxId != null) {

                    // capturing references
                    int index1 = line.indexOf('<');
                    if (index1 == -1) {
                        line = itr.next();
                        index1 = line.indexOf('<');
                    }
                    int index2 = line.indexOf('>');
                    while (index2 == -1 && itr.hasNext()) {
                        line = line + itr.next();
                        index2 = line.indexOf('>');
                    }
                    ref = line.substring(index1 + 1, index2);

                    // Dealing with BRENDA's mistakes, e.g.
                    // PR	#10# no activity in Clostridium sp. <   <4>
                    int index_typo = ref.indexOf('<');
                    if (index_typo != -1) {
                        ref = ref.substring(index_typo + 1);
                    }

                    if (debug) {
                        System.out.println("-----REF is: " + ref);
                    }

                    c_p = g.getFactory().createConcept(pr, dataSource_brenda, protein, et);
                    p2g.put(pr, c_p.getId());
                    //System.out.println("p2g.put(pr, g.getLastIdForConcepts()) with pr: " + pr + " and g.getLastId: " + g.getLastIdForConcepts());
                    c_e = g.getFactory().createConcept(pr, dataSource_brenda, enzyme, et);
                    e2g.put(pr, c_e.getId());

                    // relation from enzyme to EC from current BRENDA entry
                    g.getFactory().createRelation(c_e, ec, cat_c, et);

                    // store new concepts in enzymes and proteins
                    proteins.add(c_p);
                    enzymes.add(c_e);

                    // add taxId as Attribute
                    c_p.createAttribute(attr, taxId, false);
                    c_e.createAttribute(attr, taxId, false);

                    // store publication lists and protein, enzyme concepts
                    protenz = c_p.getId() + "-" + c_e.getId();
                    System.out.println("Checking if " + protenz + " is already in structure");
                    ArrayList<String> protenz_list = p2p.get(ref);
                    if (protenz_list == null) {
                        protenz_list = new ArrayList<String>();
                        protenz_list.add(protenz);
                        System.out.println("Not in structure. Creating new list: " + protenz_list);
                    } else {
                        protenz_list.add(protenz);
                        System.out.println("Already in structure. Adding to it with: " + protenz_list);
                    }
                    System.out.println("Updating p2p with ref: " + ref + " and protenz_list: " + protenz_list);
                    p2p.put(ref, protenz_list);
                } else {
                    System.out.println("taxId was null for " + sp);
                }

            }
        }
    }


    /**
     * Add recommended names on all proteins and enzymes
     *
     * @param proteins list of protein concepts
     * @param enzymes  list of enzyme concepts
     * @param g        graph
     * @param entry    line to parse
     */
    public void addRN(Set<ONDEXConcept> proteins, Set<ONDEXConcept> enzymes, ONDEXGraph g,
                      String entry) {

        if (debug) {
            System.out.println("\nHello addRN");
        }
        Matcher m = null;
        boolean matchFound = false;
        String rn = null;

        m = p_rn.matcher(entry);
        matchFound = m.find();
        if (matchFound) {
            rn = m.group(1);
            if (debug) {
                System.out.println("-----RN is: " + rn);
            }

            if (rn.trim().length() > 0) {
                // use recommended name on all proteins and enzymes
                // some proteins will get other names added from the SY section

                Iterator<ONDEXConcept> protIt = proteins.iterator();
                while (protIt.hasNext()) {
                    ONDEXConcept prot = protIt.next();
                    prot.createConceptName(rn, true);
                }

                Iterator<ONDEXConcept> enzIt = enzymes.iterator();
                while (enzIt.hasNext()) {
                    ONDEXConcept enz = enzIt.next();
                    enz.createConceptName(rn, true);
                }
            }
        }
    }


    /**
     * Add extra names to proteins (synonyms)
     *
     * @param g        graph
     * @param proteins list of protein concepts
     * @param p2g      BRENDA protein id to graph id for that concept
     * @param sec      section to parse
     */
    public void addSY(ONDEXGraph g, Set<ONDEXConcept> proteins, HashMap<String, Integer> p2g,
                      ArrayList<String> sec) {

        if (debug) {
            System.out.println("\nHello addSY");
        }
        Matcher m = null;
        boolean matchFound = false;
        int index1 = 0;
        int index2 = 0;
        int index3 = 0;
        String name = null;
        String pid = null;

        Iterator<String> itr = sec.iterator();
        while (itr.hasNext()) {
            String line = itr.next();
            //System.out.println("line is: " + line);

            m = p_sy.matcher(line);
            matchFound = m.find();
            if (matchFound) {
                name = m.group(1);
                index1 = name.indexOf('#');

                ///////////////////////////////////
                // we're located after first #
                // capture id, name is specific
                ///////////////////////////////////
                if (index1 != -1) {
                    //System.out.println("organism specific");
                    name = name.substring(index1 + 1).trim();
                    index1 = name.indexOf('#');
                    while (index1 == -1 && itr.hasNext()) {
                        name = name + "," + itr.next();
                        index1 = name.indexOf('#');
                    }
                    pid = name.substring(0, index1).trim();
                    if (debug) {
                        System.out.println("pid is: " + pid);
                    }

                    name = name.substring(index1 + 1).trim();
                    // we're located after second #
                    // capture name
                    // there might be comments () and/or references <>
                    // to get rid of
                    index2 = name.indexOf('(');
                    index3 = name.indexOf('<');
                    if (index3 == -1) {
                        index3 = 500;
                    }
                    if (index2 == -1) {
                        index2 = 500;
                    }
                    if (index3 < index2) {
                        index2 = index3;
                    }

                    if (index2 != 500) {
                        //System.out.println("index2 and 3: " + index2 + " and " + index3);
                        name = name.substring(0, index2).trim();
                        if (debug) {
                            System.out.println("----- name is: " + name);
                        }
                    } else {
                        // no comments, no refs, just trim
                        name = name.trim();
                        if (debug) {
                            System.out.println("----- name is: " + name);
                        }
                    }

                    if (name.compareToIgnoreCase("more") != 0) {
                        // add name if it's not "More"
                        // and if it's not already been added
                        // then add to the relevant protein(s)
                        String[] ids = commaPattern.split(pid);
                        for (String id : ids) {
                        	Integer key = p2g.get(id);
                        	if (key == null) 
                        		continue;
	                        ONDEXConcept concept = g.getConcept(key);
							if (concept != null) {
								boolean found = false;

								for (ConceptName cn : concept.getConceptNames()) {
									String name_already_in = cn.getName();
									// System.out.println("name already in: " + name_already_in + " - vs - " + name);
									if (name_already_in
											.compareToIgnoreCase(name) == 0) {
										found = true;
										// System.out.println("so it will not be added");
										break;
									}
								}
								if (!found && name.trim().length() > 0) {
									concept.createConceptName(name, false);
									// System.out.println(name + " was added");
								}
							}
                        }// end for
                    }// end if name not more
                }// end if organism specific


                ///////////////////////////////////////////////////////
                // if there is no id then name is general
                // createConceptName in loop to apply to all proteins created previously
                ///////////////////////////////////////////////////////
                else {
                    //System.out.println("not organism specific");
                    index2 = name.indexOf('(');
                    index3 = name.indexOf('<');
                    if (index3 == -1) {
                        index3 = 500;
                    }
                    if (index2 == -1) {
                        index2 = 500;
                    }
                    if (index3 < index2) {
                        index2 = index3;
                    }

                    if (index2 == 500 && index3 == 500) {
                        name = name.trim();
                        if (debug) {
                            System.out.println("----- SY is: " + name);
                        }
                    } else {
                        name = name.substring(0, index2).trim();
                        if (debug) {
                            System.out.println("----- SY is: " + name);
                        }
                    }
                    if (name.compareToIgnoreCase("more") != 0) {
                        for (ONDEXConcept protein : proteins) {
                            boolean found = false;
                            for (ConceptName cn : protein.getConceptNames()) {
                                String name_already_in = cn.getName();
                                //System.out.println("name already in: " + name_already_in + " - vs - " + name);
                                if (name_already_in.compareToIgnoreCase(name) == 0) {
                                    found = true;
                                    //System.out.println("so it will not be added");
                                }
                            }
                            if (!found && name.trim().length() > 0) {
                                protein.createConceptName(name, false);
                                //System.out.println(name + " was added");
                            }
                        }
                    }
                }// end else
            }
        }// end while
    }


    /**
     * Add accession numbers to proteins and enzymes
     *
     * @param g        graph
     * @param proteins list of protein concepts
     * @param enzymes  list of enzyme concepts
     * @param sec      section to parse
     */
    public void addCAS(ONDEXGraph g, Set<ONDEXConcept> proteins, Set<ONDEXConcept> enzymes,
                       ArrayList<String> sec) {

        if (debug) {
            System.out.println("\nHello addCAS");
        }

        Matcher m = null;
        boolean matchFound = false;
        String accession = null;
        ArrayList<String> accessions = new ArrayList<String>();

        boolean already_in = false;

        Iterator<String> itr = sec.iterator();
        while (itr.hasNext()) {
            String line = itr.next();
            m = p_cr.matcher(line);
            matchFound = m.find();
            if (matchFound) {
                accession = m.group(1);
                if (debug) {
                    System.out.println("----- CAS is: " + accession);
                }

                if (accessions != null) {
                    // there was more than one CAS given
                    for (String acc : accessions) {
                        if (acc.equals(accession)) {
                            // set boolean to true and get out of the loop
                            already_in = true;
                            break;
                        }
                    }
                }

                if (accessions == null || !already_in) {
                    for (ONDEXConcept protein : proteins) {
                        protein.createConceptAccession(accession, dataSource_cas, false);
                    }
                    for (ONDEXConcept enzyme : enzymes) {
                        enzyme.createConceptAccession(accession, dataSource_cas, false);
                    }
                    accessions.add(accession);
                }
            }
        }
    }


    /**
     * addSP uses the SUBSTRATE_PRODUCT section to add new reactions and compounds (substrates and products of reactions)
     *
     * @param g          graph
     * @param e2g        BRENDA enzyme id to graph id for that concept
     * @param k2g        name of compound /// organism id links to graph ids for relations to later add KM and KI values as relation Attribute
     * @param compound2g compound name to graph id
     * @param sec        section to parse
     */
    public void addSP(ONDEXGraph g, HashMap<String, Integer> e2g,
                      HashMap<String, ArrayList<Integer>> k2g, HashMap<String, Integer> compound2g, ArrayList<String> sec) {

        if (debug) {
            System.out.println("\n\nHello addSP");
        }
        Matcher m = null;
        boolean matchFound = false;
        String ids = null;
        String[] id_list = null;
        String substrate = null;
        String product = null;
        boolean newSP = false;
        boolean lastSP = false;
        String line = "";
        ONDEXConcept re = null;

        //index compound name to graph id for compounds already created
        HashMap<String, Integer> RnameToGid = new HashMap<String, Integer>();

        Iterator<String> itr = sec.iterator();
        while (itr.hasNext() || lastSP) {
            if (!line.startsWith("SP") && !newSP && !lastSP) {
                line = itr.next();
            }
            lastSP = false;
            m = p_sp.matcher(line);
            matchFound = m.find();
            if (matchFound) {
                ids = m.group(1).trim();
                id_list = commaPattern.split(ids);
                line = m.group(2).trim();

                int index1 = line.indexOf('=');
                //System.out.println("line is: " + line + " and index1 is " + index1);
                while (index1 == -1 && itr.hasNext()) {
                    String newline = itr.next();
                    line = line + " " + newline;
                    //System.out.println("line is: " + line + " and index1 is " + index1);
                    index1 = line.indexOf('=');
                }
                substrate = line.substring(0, index1).trim();
                //System.out.println("Substrate is: " + substrate);

                line = line.substring(index1 + 1);
                index1 = line.indexOf("(#");
                int index2 = line.indexOf("|#");
                int index3 = line.indexOf("<");
                while (index1 == -1 && index2 == -1 && index3 == -1 && !newSP && itr.hasNext()) {
                    String newline = itr.next();
                    line = line + " " + newline;
                    // we wish to stop before comments and references
                    index1 = line.lastIndexOf("(#");
                    index2 = line.lastIndexOf("|#");
                    index3 = line.lastIndexOf("<");
                    newSP = newline.startsWith("SP");
                    //System.out.println("line is: " + line + " and index1 is " + index1);
                }
                if (index1 != -1) {
                    product = line.substring(0, index1).trim();
                } else {
                    if (index2 != -1) {
                        product = line.substring(0, index2).trim();
                    } else {
                        if (index3 != -1) {
                            product = line.substring(0, index3).trim();
                        } else {
                            // there was no (
                            // we got to end of section
                            product = line.trim();
                        }
                    }
                }
                //System.out.println("Product is: " + product);

                ///////////////////
                // Possible to add: capture references and add publication list as qualifier to ca_by relation
                ///////////////////

                if (!substrate.contains("more") && !product.contains("more")
                        && !substrate.contains("?") && !product.contains("?")
                        && !substrate.trim().isEmpty() && !product.trim().isEmpty()) {
                    // create concept Reaction if not already existing
                    String reaction_name = substrate + " = " + product;
                    if (!RnameToGid.containsKey(reaction_name)) {
                        re = g.getFactory().createConcept(reaction_name, dataSource_brenda, reaction, et);
                        re.createConceptName(reaction_name, true);
                        //System.out.println("Created a reaction of name: " + reaction_name );
                        RnameToGid.put(reaction_name, re.getId());

                        // make relation from substrates to reaction
                        make_rel_from_compound_to_other_concept(g, substrate, cs_by, re, null, compound2g);

                        // make relation from products to reaction
                        make_rel_from_compound_to_other_concept(g, product, pd_by, re, null, compound2g);

                        for (String id : id_list) {
                            // make relation from reaction to enzyme (organism specific)
                        	if (e2g.get(id) == null)
                        		continue;
                            ONDEXRelation ar = g.getFactory().createRelation(re, g.getConcept(e2g.get(id)), ca_by, et);

                            //System.out.println("ONDEXRelation id is: " + ar.getId(s));
                            if (k2g.get(id) == null) {
                                ArrayList<Integer> relation_ids = new ArrayList<Integer>();
                                relation_ids.add(ar.getId());
                                k2g.put(id, relation_ids);
                            } else {
                                if (!k2g.get(id).contains(ar.getId())) {
                                    k2g.get(id).add(ar.getId());
                                }
                            }
                        }
                    } else {
                        re = g.getConcept(RnameToGid.get(reaction_name));
                        //System.out.println("Reaction already created: " + reaction_name );
                    }

                }// end if there are no ? or more in the reaction name
            }// end if

            // Match was not found
            else {
                if (line.startsWith("SP")) {
                    if (itr.hasNext()) {
                        // SP\\t#(.*?)#\\s?(.*) was not found, second # was on next line
                        // so add next line and get to next iteration of the loop
                        line = line + "," + itr.next();
                        continue;
                    } else {
                        //System.out.println("Getting out of the loop");
                        break;
                    }
                }
            }

            // checking for new SP
            if (line.contains("SP")) {
                line = line.substring(line.indexOf("SP"));
                newSP = false;
            } else {
                if (itr.hasNext()) {
                    line = itr.next();
                    //System.out.println("line is: " + line);
                    if (!itr.hasNext()) {
                        //System.out.println("\nGot to the end of the block of SPs");
                        lastSP = true;
                    }
                }
            }

        }
    }


    /**
     * Source Tissue and Localization have the same structure and we can treat them in the same manner
     * so common method for both of them.
     *
     * @param g     graph
     * @param type  tissue or celcomp
     * @param loc2g location to graph id hashmap
     * @param loc2b location to brenda ids for protenz hashmap
     * @param p2g   BRENDA protein id to graph id for that concept
     * @param pat   pattern	regular expression pattern
     * @param sec   section	section to parse
     */
    public void addSTLO(ONDEXGraph g, ConceptClass type, HashMap<String, Integer> loc2g, HashMap<String, String> loc2b,
                        HashMap<String, Integer> p2g, Pattern pat, ArrayList<String> sec) {

        if (debug) {
            System.out.println("\nHello addSTLO");
        }
        Matcher m = null;
        boolean matchFound = false;
        boolean already_in = false;
        String match = null;
        String loc = null;
        String name = null;
        String[] protein_ids = null;
        ONDEXConcept c = null;

        Iterator<String> itr = sec.iterator();
        while (itr.hasNext()) {
            String line = itr.next();
            m = pat.matcher(line);
            matchFound = m.find();
            if (matchFound) {
                match = m.group(1).trim();
                System.out.println("----- STLO: " + match);
                int index1 = match.indexOf('#');
                while (index1 == -1 && itr.hasNext()) {
                    match = match + "," + itr.next();
                    index1 = match.indexOf('#');
                }
                loc = match.substring(0, index1);
                protein_ids = commaPattern.split(loc);
                // retrieve name
                // looking to cut at whichever comes first:
                // comments in () or references in <>
                int index2 = match.indexOf('(');
                int index3 = match.indexOf('<');

                if (index2 == -1 && index3 == -1) {
                    while ((index2 == -1 || index3 == -1) && itr.hasNext()) {
                        match = match + " " + itr.next();
                        index2 = match.indexOf('(');
                        index3 = match.indexOf('<');
                    }
                }
                if (index3 == -1) {
                    index3 = 500;
                }
                if (index2 == -1) {
                    index2 = 500;
                }
                if (index3 < index2) {
                    // < is before (
                    index2 = index3;
                }
                name = match.substring(index1 + 1, index2).trim();

                if (name.compareToIgnoreCase("more") != 0) {
                    name = name.trim();
                    if (name.length() > 0) {
                        String lowercasename = name.toLowerCase();
                        if (!loc2g.containsKey(lowercasename)) {
                            if (debug) {
                                System.out.println("Creating new concept for loc name: " + name);
                            }
                            c = g.getFactory().createConcept(lowercasename, dataSource_brenda, type, et);
                            c.createConceptName(lowercasename, true);
                            loc2g.put(lowercasename, c.getId());
                            for (String id : protein_ids) {
                            	if (p2g.get(id) == null)
                            		continue;
                            	g.getFactory().createRelation(g.getConcept(p2g.get(id)), c, located_in, et);
                            }
                            loc2b.put(lowercasename, loc);
                        } else {
                            if (debug) {
                                System.out.println("Loc name already exists for: " + name);
                            }
                            for (String new_id : protein_ids) {
                                if (loc2b.containsKey(lowercasename)) {
                                    for (String id_already_in : commaPattern.split(loc2b.get(lowercasename))) {
                                        if (new_id.compareTo(id_already_in) == 0) {
                                            System.out.println("relation already exists to this protein");
                                            already_in = true;
                                            break;
                                        }
                                    }
                                    if (!already_in) {
                                        // get location concept
                                        c = g.getConcept(loc2g.get(lowercasename));
                                        // create relation to it from the right protein
                                        if (p2g.get(new_id) == null)
                                        	continue;
                                        g.getFactory().createRelation(g.getConcept(p2g.get(new_id)), c, located_in, et);
                                        // update the hashmap
                                        loc2b.put(lowercasename, loc2b.get(lowercasename) + ",new_id");
                                    }
                                }
                            }
                        }
                    }
                }// end if not more
            }// end if matchfound
        }// end while
    }


    /**
     * Activating Compounds, Cofactors and Inhibitors have the same structure and we can treat them in the same manner
     * so common method for both of them.
     *
     * @param g          graph
     * @param compound2g compound name to graph id
     * @param sec        section to parse
     * @return list of refs to the literature
     */
    public ArrayList<Compound> addComp(ONDEXGraph g, HashMap<String, Integer> compound2g, String compType, ArrayList<String> sec) {

        if (debug) {
            System.out.println("\nHello addComp");
        }
        Matcher m = null;
        Pattern pat = null;
        boolean matchFound = false;
        ArrayList<Compound> compoundList = new ArrayList<Compound>();
        String match = null;
        String enzyme_idlist = null;
        String name = null;
        boolean gotref = false;
        int index2 = 0;
        int index3 = 0;

        Iterator<String> itr = sec.iterator();
        while (itr.hasNext()) {
            Compound comp = new Compound();
            String line = itr.next();

            if (compType.equals("CF")) {
                pat = p_cf;
            } else {
                if (compType.equals("IN")) {
                    pat = p_in;
                } else {
                    pat = p_ac;
                }
            }
            m = pat.matcher(line);
            matchFound = m.find();
            if (matchFound) {
                boolean newComp = true;
                match = m.group(1).trim();

                int index1 = match.indexOf('#');
                while (index1 == -1 && itr.hasNext()) {
                    match = match + "," + itr.next();
                    index1 = match.indexOf('#');
                }
                enzyme_idlist = match.substring(0, index1);
                if (debug) {
                    System.out.println("----- Compounds: " + enzyme_idlist);
                }

                // looking to cut at whichever comes first:
                // comments in () or references in <>
                index2 = match.indexOf("(#");
                index3 = match.indexOf('<');
                newComp = false;

                if (index2 == -1 && index3 == -1) {
                    while ((index2 == -1 || index3 == -1) && !newComp && itr.hasNext()) {
                        String newline = itr.next();
                        if (newline.startsWith(compType)) {
                            newComp = true;
                        } else {
                            match = match + " " + newline;
                            index2 = match.indexOf("(#");
                            index3 = match.indexOf('<');
                        }
                    }
                }
                if (index3 == -1) {
                    index3 = 500;
                }
                if (index2 == -1) {
                    index2 = 500;
                }

                // second condition is
                // Dealing with BRENDA's mistakes, e.g.
                // IN	#12# SnCl2 < (#12# partial <30>) <30>
                // checking < is not followed by a space
                if (index3 < index2 && !match.substring(index3 + 1, index3 + 2).trim().isEmpty()) {
                    // < is before (
                    System.out.println("index2 for ( is: " + index2 + " and index3 for < is: " + index3);
                    index2 = index3;
                    gotref = true;
                } else {
                    gotref = false;
                }

                if (debug) {
                    System.out.println("debug match is: " + match);
                }
                if (index1 + 1 < index2) {
                    name = match.substring(index1 + 1, index2).trim();
                    if (debug) {
                        System.out.println("----- Compound name: " + name);
                    }
                } else {
                    name = "None";
                    if (debug) {
                        System.out.println("----- Compound name was set to none because of indexOf problem");
                    }
                }

                // create new concept comps
                comp.enzyme_idlist = enzyme_idlist;
                ONDEXConcept compoundConcept = null;
                String lowercasename = name.toLowerCase().trim();

                if (!compound2g.containsKey(lowercasename)) {
                    compoundConcept = g.getFactory().createConcept(lowercasename, dataSource_brenda, compd, et);
                    compoundConcept.createConceptName(lowercasename, true);
                    compound2g.put(lowercasename, compoundConcept.getId());
                } else {
                    if (compound2g.containsKey(lowercasename)) {
                        System.out.println("Compound already exists (reaction) but has not been parsed (as CFIN)");
                        compoundConcept = g.getConcept(compound2g.get(lowercasename));
                        compound2g.put(lowercasename, compoundConcept.getId());
                    }
                }
                comp.compoundConcept = compoundConcept;

                // Capturing references
                if (name != null && !newComp) {
                    if (gotref) {
                        if (debug) {
                            System.out.println("in gotref");
                        }
                        match = match.substring(index2 + 1);
                        index1 = match.indexOf('>');
                        while (index1 == -1 && itr.hasNext()) {
                            match = match + "," + itr.next();
                            index1 = match.indexOf('>');
                        }
                        comp.references = match.substring(0, index1);
                        gotref = false;
                    } else {
                        // need to get past comments first
                        index1 = match.indexOf(')');
                        while (index1 == -1 && itr.hasNext()) {
                            match = match + itr.next();
                            index1 = match.indexOf(')');
                        }
                        match = match.substring(index1 + 1);

                        // then look for refs in <>
                        index1 = match.lastIndexOf('<');
                        while (index1 == -1 && itr.hasNext()) {
                            match = match + itr.next();
                            index1 = match.lastIndexOf('<');
                        }

                        index2 = match.lastIndexOf('>');
                        while (index2 == -1 && itr.hasNext()) {
                            match = match + "," + itr.next();
                            index2 = match.lastIndexOf('>');
                        }
                        comp.references = match.substring(index1 + 1, index2);
                    }
                    if (debug) {
                        System.out.println("---------- REF for Compound are: " + comp.references);
                    }
                }
            }// end if matchfound
            compoundList.add(comp);
        }// end while
        return compoundList;
    }


    /**
     * addKMKI is called to add KM or KI values as relation Attribute
     *
     * @param g     graph
     * @param p2g   protein id to graph id hashmap
     * @param k2g   name of compound /// organism id links to graph ids for relations to later add KM and KI values as relation Attribute
     * @param attrn AttributeName concept
     * @param pat   regular expression pattern
     * @param sec   section to parse
     */
    public void addKMKI(ONDEXGraph g, HashMap<String, Integer> p2g,
                        HashMap<String, ArrayList<Integer>> k2g, AttributeName attrn, Pattern pat, ArrayList<String> sec) {

        if (debug) {
            System.out.println("\n\nHello addKMKI");
        }
        Matcher m = null;
        boolean matchFound = false;
        String list_ids = null;
        String[] ids = null;
        String value = null;

        Iterator<String> itr = sec.iterator();
        while (itr.hasNext()) {
            String line = itr.next();
            m = pat.matcher(line);
            matchFound = m.find();
            if (matchFound) {
                list_ids = m.group(1).trim();
                ids = commaPattern.split(list_ids);
                value = m.group(2).trim();
                String string_name = m.group(3).trim();
                if (debug) {
                    System.out.println("-----K ids: " + list_ids);
                    System.out.println("-----K value: " + value);
                    System.out.println("-----K name: " + string_name);
                }
                if (!string_name.equalsIgnoreCase("more") && !string_name.equalsIgnoreCase("?")) {
                    // create relation Attribute
                    for (String id : ids) {

                        if (debug) {
                            System.out.println("Id is: " + id);
                        }
                        ArrayList<Integer> rel_ids = k2g.get(id);
                        if (rel_ids != null) {
                            if (debug) {
                                System.out.println("k2g.get(id) is: " + rel_ids);
                            }
                            for (Integer rel_id : rel_ids) {

                                if (g.getRelation(rel_id) != null && g.getRelation(rel_id).getAttribute(attrn) == null) {
                                    g.getRelation(rel_id).createAttribute(attrn, value, false);
                                    if (debug) {
                                        System.out.println(value + " was added as relation Attribute to reaction for organism " + id);
                                    }
                                } else {
                                    if (g.getRelation(rel_id) == null && debug) {
                                        System.out.println("Warning relation was null: " + rel_id);
                                    } else {
                                        if (g.getRelation(rel_id).getAttribute(attrn) != null && debug) {
                                            System.out.println("Relation already had a Attribute: " + g.getRelation(rel_id).getAttribute(attrn).getValue());
                                        }
                                    }
                                }
                            }// end for
                        } else {
                            if (debug) {
                                System.out.println("k2g.get(id) is null");
                            }
                        }
                    }//end for
                }
            }
        }
    }


    /**
     * Link up references between BRENDA id and PMID
     *
     * @param g                 graph
     * @param refToPublidetails index BRENDA ref number to PMIDs and other details about publications
     * @param sec               section to parse
     */
    public void addRef(ONDEXGraph g, HashMap<String, String> refToPublidetails, ArrayList<String> sec) {

        if (debug) {
            System.out.println("\nHello addRef");
        }
        Matcher m = null;
        boolean matchFound = false;

        // will structure details as pmid///authors///title + journal///year
        // in order to get accession (and name), Attribute authors, Attribute title + journal, Attribute year
        String details = null;

        String ref = null;
        String title_journal = null;
        String pmid = null;
        String authors = null;
        String year = null;

        Iterator<String> itr = sec.iterator();
        while (itr.hasNext()) {
            String line = itr.next();
            m = p_ref.matcher(line);
            // Pattern was "RF\\t<(\\d+)> (.*)"
            matchFound = m.find();
            boolean stop = false; // reached end of current reference, need to stop loop

            while (!matchFound && !stop && itr.hasNext()) {
                line = itr.next();
                m.reset(line);
                matchFound = m.find();
                stop = line.contains("{Pubmed");
                //System.out.println("line is now: " + line + "and stop is: " + stop);
            }
            if (matchFound) {
                // get ref
                ref = m.group(1).trim();
//	    		if (debug){
//	    			System.out.println("\n----- ref is: " + ref);
//	    		}
                // get rest of the line
                line = m.group(2).trim();
            } else {
                if (debug) {
                    System.out.println("*********************** Ref number was not found on line: " + line);
                }
                continue;
            }

            // get authors
            int index1 = line.indexOf(": ");
            while (index1 == -1 && itr.hasNext()) {
                line = line + " " + itr.next();
                index1 = line.indexOf(": ");
            }
            authors = line.substring(0, index1).trim();
//    		if (debug){
//    			System.out.println("author is: " + authors);
//    		}
            line = line.substring(index1 + 1);

            // get title and journal
            // Example:
            // RF	<28> Khalyfa, A.; Kermasha, S.; Khamessan, A.; Marsot, P.; Alli, I.:
            //Purification and characterization of chlorophyllase from alga
            //(Phaeodactylum tricornutum) by preparative isoelectric focusing.
            //Biosci. Biotechnol. Biochem. (1993) 57, 433-437. {Pubmed:NOT_FOUND} (c)

            // perl style Pattern p_title_journal = Pattern.compile("");
            Pattern p_title_journal = Pattern.compile("(.*?)\\((\\d\\d\\d\\d)\\).*?");
            m = p_title_journal.matcher(line);
            matchFound = m.find();
            while (!matchFound && !stop && itr.hasNext()) {
                line = line + " " + itr.next();
                m.reset(line);
                matchFound = m.find();
                stop = line.contains("{Pubmed");
            }
            title_journal = m.group(1).trim();
            year = m.group(2).trim();

            line = line.substring(title_journal.length() + 8);
            // 8 = space after journal + (year) + space after year
            //System.out.println("line is now: " + line);

//	    	if (debug){
//	    		System.out.println("Titl&journ: " + title_journal);
//	    		System.out.println("Year is: " + year);
//	    	}

            // get pmid
            index1 = line.indexOf(':');
            while (index1 == -1 && itr.hasNext()) {
                line = line + " " + itr.next();
                index1 = line.indexOf(':');
            }
            line = line.substring(index1 + 1);

            index1 = line.indexOf('}');
            while (index1 == -1 && itr.hasNext()) {
                line = line + itr.next();
                index1 = line.indexOf('}');
            }

            if ((index1 == 0) || (index1 == -1)) {
                if (debug) {
                    System.out.println("-----No pmid");
                }
                pmid = "NOT_FOUND";
            } else {
                pmid = line.substring(0, index1);
//    			if (debug){
//    				System.out.println("Pmid is: " + pmid);
//    			}
            }

            // fill in hashmap
            if (pmid.isEmpty()) {
                pmid = "not found";
            }
            if (authors.isEmpty()) {
                authors = "not found";
            }
            if (title_journal.isEmpty()) {
                title_journal = "not found";
            }
            if (year.isEmpty()) {
                year = "not found";
            }
            details = pmid + "///" + authors + "///" + title_journal + "///" + year;
            refToPublidetails.put(ref, details);
        }

    }


    /**
     * Make ternary relations protein is_a enzyme with publication
     *
     * @param g                 graph
     * @param publist2g         list of BRENDA publication IDs to graph id for publication list concept
     * @param publi2g           publication BRENDA ID to graph id for publication concept
     * @param refToPublidetails index BRENDA ref number to PMIDs and other details about publications
     * @param p2p               each protenz is assigned a publication list in PR section
     */
    public void makeRelations_proteins(ONDEXGraph g, HashMap<String, Integer> publist2g,
                                       HashMap<String, Integer> publi2g, HashMap<String, String> refToPublidetails,
                                       HashMap<String, ArrayList<String>> p2p) {

        if (debug) {
            System.out.println("\nHello makeRelations PR");
        }
        //String[] refs = null;
        //String[] details = null;
        ArrayList<String> protenz_list = null;
        int index = 0;
        String protein = null;
        String enzyme = null;
        int protein_id = 0;
        int enzyme_id = 0;
        //ONDEXConcept publication = null;
        ONDEXConcept publication_list = null;
        String publist = null;

        Set<String> keySet = p2p.keySet();
        Iterator<String> keySetIterator = keySet.iterator();
        while (keySetIterator.hasNext()) {
            publist = keySetIterator.next();
            publication_list = check_publistToGid(graph, null, refToPublidetails, publist2g, publi2g, publist, null);

            // get protein_id and enzyme_id
            // and create relation is_a with qualifier publication list
            protenz_list = p2p.get(publist);
            if (debug) {
                System.out.println("protenz is: " + protenz_list);
            }
            for (String protenz : protenz_list) {
                index = protenz.indexOf('-');
                protein = protenz.substring(0, index);
                enzyme = protenz.substring(index + 1).trim();
                protein_id = Integer.parseInt(protein);
                if (debug) {
                    System.out.println("protein_id is: " + protein_id);
                }
                enzyme_id = Integer.parseInt(enzyme);
                g.getFactory().createRelation(g.getConcept(protein_id), g.getConcept(enzyme_id), is_a, et);
                //g.getFactory().createRelation(g.getConcept(protein_id), g.getConcept(enzyme_id), publication_list, is_a, et);
            }
        }// end while
    }


    /**
     * Make relations between compounds and enzymes with publication (ternary)
     *
     * @param g                 graph
     * @param eid2g             index BRENDA enzyme number to graph id for protein concepts
     * @param refToPublidetails references to publication details
     * @param publist2g         list of BRENDA publication IDs to graph id for publication list concept
     * @param publi2g           publication BRENDA ID to graph id for publication concept
     * @param compList          list of objects of class Compounds
     * @param rel               type of relation (cf_by or in_by)
     */

    public void makeRelations_compounds(ONDEXGraph g, HashMap<String, Integer> eid2g,
                                        HashMap<String, String> refToPublidetails, HashMap<String, Integer> publist2g,
                                        HashMap<String, Integer> publi2g, ArrayList<Compound> compList, RelationType rel) {

        if (debug) {
            System.out.println("\nHello makeRelations_compounds: " + rel.toString());
        }
        String[] refs = null;
        ONDEXConcept publication_list = null;

        if (compList != null) {
            for (Compound compound : compList) {
                System.out.println("\nIds for the enzymes are: " + compound.enzyme_idlist);
                if (compound.references != null) {
                    if (debug) {
                        System.out.println("List of refs is: " + compound.references);
                    }
                    if (compound.references.contains(",")) {
                        refs = commaPattern.split(compound.references);
                    } else {
                        refs = new String[1];
                        refs[0] = compound.references;
                    }

                    ONDEXConcept comp = compound.compoundConcept;
                    String[] enzyme_ids = commaPattern.split(compound.enzyme_idlist);
                    for (String enzyme_id : enzyme_ids) {
                    	if (eid2g.get(enzyme_id) == null)
                    		continue;
                        ONDEXConcept enzyme = g.getConcept(eid2g.get(enzyme_id));
                        publication_list = check_publistToGid(g, null, refToPublidetails, publist2g, publi2g, compound.references, null);
                        g.getFactory().createRelation(comp, enzyme, rel, et);
                        //g.getFactory().createRelation(comp, enzyme, publication_list, rel, et);
                        System.out.println("Relation created between compound and enzyme " + enzyme_id);
                    }
                } else {
                    if (debug) {
                        System.out.println("Warning: Compounds refs are null");
                    }
                }
            }
        }
    }


    /**
     * check_publistToGid
     * if publication list already exists, get id and link
     * else create publication list, add to publist2g and link
     * return publication list (old or new)
     *
     * @param g                 graph
     * @param structure         hashmap currently dealt with
     * @param refToPublidetails references to publication details
     * @param publist2g         list of BRENDA publication IDs to graph id for publication list concept
     * @param publi2g           publication BRENDA ID to graph id for publication concept
     * @param references        references
     * @param item              String to add to the structure
     */
    public ONDEXConcept check_publistToGid(ONDEXGraph g, HashMap<String, Integer> structure,
                                           HashMap<String, String> refToPublidetails, HashMap<String, Integer> publist2g, HashMap<String, Integer> publi2g,
                                           String references, String item) {

        if (debug) {
            System.out.println("\nHello check_publistToGid");
        }
        String[] details = null; // publication details
        ONDEXConcept publication_list = null;
        ONDEXConcept publication = null;
        String publist_accession = ""; // publication list accession: string containing the BRENDA ids for the articles in the list

        // if publication list already exists, get id and link
        if (publist2g.containsKey(references)) {
            if (debug) {
                System.out.println("Publication list already in structure publist2g.");
            }
            publication_list = g.getConcept(publist2g.get(references));
            if (structure != null && item != null) {
                structure.put(item, publist2g.get(references));
            }
        }

        // create publication list, add to publist2g and link
        else {
            publist_count++;
            if (debug) {
                System.out.println("Creating publication list number: " + publist_count);
                System.out.println("with references: " + references);
            }
            publication_list = g.getFactory().createConcept("publist_" + publist_count, dataSource_brenda, publi_list, et);
            publist2g.put(references, publication_list.getId());
            String[] refs = commaPattern.split(references);
            for (String ref : refs) {
                System.out.println("in for refs");
                if (!publi2g.containsKey(ref)) {
                    // create a new publication concept, add to hashmap and link to it
                    if (refToPublidetails.get(ref) != null) {
                        details = forwardSlashPattern.split(refToPublidetails.get(ref));
                        if (details != null) {
                            // [0] contains PMID (accession and name)
                            if (details[0].trim().length() > 0) {
                                publication = g.getFactory().createConcept(details[0], dataSource_brenda, publi, et);
                                publication.createConceptName(details[0], true);
                                publication.createConceptAccession(details[0], dataSource_NLM, false);
                                // the PMIDs are also concanated to create a list used for the accession of the publication list
                                if (publist_accession.isEmpty()) {
                                    publist_accession += "PMIDs contained in this list are " + details[0];
                                } else {
                                    publist_accession += ", " + details[0];
                                }
                            }
                            // [1] contains authors
                            publication.createAttribute(authors, details[1], false);
                            // old: publication = g.createConcept(s, details[1], dataSource_brenda, publi, et);

                            // [2] contains the title and journal name
                            publication.createAttribute(title_journal, details[2], false);
                            // old: publication.setAnnotation(s, details[2]);

                            // [3] contains the year
                            publication.createAttribute(year, details[3], false);
                            // old: publication.setDescription(s, details[3]);

                            g.getFactory().createRelation(publication, publication_list, m_isp, et);
                            publi2g.put(ref, publication.getId());
                        }
                    }
                } else {
                    // adding to publist_accession
                    details = forwardSlashPattern.split(refToPublidetails.get(ref));
                    if (publist_accession.isEmpty()) {
                        publist_accession += "PMIDs contained in this list are " + details[0];
                    } else {
                        publist_accession += ", " + details[0];
                    }

                    // link to publication concept in hashmap
                    g.getFactory().createRelation(g.getConcept(publi2g.get(ref)), publication_list, m_isp, et);
                }
            }
            System.out.println("publist_accession: " + publist_accession);
            publication_list.createConceptAccession(publist_accession, dataSource_brenda, false);

            if (structure != null) {
                structure.put(item, publication_list.getId());
            }
        }
        if (debug) {
            System.out.println("Bye check_publistToGid\n");
        }
        return publication_list;
    }


    /**
     * make_rel_from_compound_to_other_concept
     * Creates relation from a compound to a specified concept
     *
     * @param g             graph
     * @param compound_list String listing compounds
     * @param r_type        relation type to be made
     * @param other_concept destination of the relation
     * @param publist       publication list
     * @param compound2g    compound name to graph id for compound concept
     */
    public void make_rel_from_compound_to_other_concept(ONDEXGraph g, String compound_list,
                                                        RelationType r_type, ONDEXConcept other_concept, ONDEXConcept publist, HashMap<String, Integer> compound2g) {

        if (debug) {
            System.out.println("\nHello make_rel_from_compound_to_other_concept");
        }
        ONDEXConcept co = null;
        String[] compounds = plusPattern.split(compound_list);
        for (String compound : compounds) {
            compound = compound.toLowerCase();
            if (!compound2g.containsKey(compound)) {
                co = g.getFactory().createConcept(compound, dataSource_brenda, compd, et);
                co.createConceptName(compound, false);
                System.out.println("Created a compound of name: " + compound);
                compound2g.put(compound, co.getId());
            } else {
                // this compound has already been created as a concept
                co = g.getConcept(compound2g.get(compound));
                System.out.println("Compound already created: " + compound);
            }

            if (publist == null) {
                g.getFactory().createRelation(co, other_concept, r_type, et);
            } else {
                g.getFactory().createRelation(co, other_concept, r_type, et);
                //g.getFactory().createRelation(co, other_concept, publist, r_type, et);
            }
        }
    }


    /**
     * Cofactor/Inhibitor/Activating compound information parsed for a cofactor/inhibitor/activating compound
     *
     * @author hindlem
     */
    class Compound {
        public String enzyme_idlist;
        public String references;
        public ONDEXConcept compoundConcept;
    }

    //TO DO: could add context to all concepts of an EC (even if previously created)
    // e.g. add context c to protein
    //g.getConcept(p2g.get(id)).addContext(c);

}