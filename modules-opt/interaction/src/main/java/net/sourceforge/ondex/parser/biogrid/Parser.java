package net.sourceforge.ondex.parser.biogrid;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.InconsistencyEvent;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import net.sourceforge.ondex.exception.type.PluginException;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.tools.MetaDataLookup;
import org.apache.log4j.Level;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses BioGRID database flatfiles.
 * <p/>
 * It creates an interaction concept for each interaction in the file.
 * Each interaction concept is connected to an active participant
 * and a passive participant, as well as to a publication that documents
 * it.
 * <p/>
 * The parser automatically recognizes whether the interaction is a
 * genetic or physical interaction and what subclass of interaction it is.
 * It then sets the interaction's class and the participants' classes
 * accordingly. Genetic interactions have 'Gene' participants, physical
 * interactions have 'Polypeptide' participants.
 *
 * @author Jan Taubert, Jochen Weile
 */
public class Parser extends ONDEXParser
{

    /**
     * lookup table for interactions concept classes
     */
    private MetaDataLookup<ConceptClass> itCC_lookup;

    /*
      * various hashtables for registring concepts and relations
      */
    private Map<String, ONDEXConcept> polypeps = new HashMap<String, ONDEXConcept>();
    private Map<String, ONDEXConcept> genes = new HashMap<String, ONDEXConcept>();
    private Map<String, ONDEXConcept> rnas = new HashMap<String, ONDEXConcept>();
    private Map<String, ONDEXConcept> publications = new HashMap<String, ONDEXConcept>();
    private Map<String, ONDEXConcept> interactions = new HashMap<String, ONDEXConcept>();
    private Map<String, EvidenceType> experiments = new HashMap<String, EvidenceType>();

    private boolean taxidRestr;
    private ArrayList<Integer> taxids;

    /*
      * Global metadata
      */
    private ConceptClass ccPolyPep, ccGene, ccTRNAGene, ccRNA, ccTRNA, ccPublication, ccGI, ccPI;
    private RelationType rtEnBy, rtPassPart, rtActPart, rtPubIn;
    private DataSource dataSourceUnknown, dataSourceBiogrid, dataSourceEntrez, dataSourcePubmed, dataSourceMips;
    private EvidenceType etImpd;
    private AttributeName atTaxid, atAuthors, atYear;

    /*
      * ArgumentDefinition parameters.
      */
    public static final String TRL_FILE_ARG = "TranslationFile";
    public static final String TRL_FILE_ARG_DESC = "absolute path to the desired translation file";
    public static final String TAXID_ARG = "TaxIDRestriction";
    public static final String TAXID_ARG_DESC = "Taxonomy ID restriction. Only genes/proteins of the " +
            "given taxa will be considered. if empty, all will be considered";

    /**
     * genetic or molecular interaction
     *
     * @author jweile
     */
    private enum InteractionType {
        genetic, molecular;
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#getArgumentDefinitions()
     */
    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition[]{
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
                        FileArgumentDefinition.INPUT_FILE_DESC, true, true, false, false),
                new FileArgumentDefinition(TRL_FILE_ARG, TRL_FILE_ARG_DESC, true, true, false),
                new StringArgumentDefinition(TAXID_ARG, TAXID_ARG_DESC, false, null, true)
        };
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#getName()
     */
    @Override
    public String getName() {
        return "BioGrid";
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#getVersion()
     */
    @Override
    public String getVersion() {
        return "22.07.2009";
    }

    @Override
    public String getId() {
        return "biogrid";
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#requiresValidators()
     */
    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#start()
     */
    @Override
    public void start() throws Exception {

        fetchArguments();

        // get all metadata
        fetchMetaData();

        File file = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE));
        BufferedReader reader = new BufferedReader(new FileReader(file));

        // skip first 35 lines comment TODO: this is dirty!
        int i = 1;
        for (; i <= 35; i++)
            reader.readLine();

        String line;
        while ((line = reader.readLine()) != null) {
            i++;
            if (line.length() == 0) {
                continue;
            }
            String[] splitline = line.split("\t");
            if (!(splitline.length == 11)) {
                throw new PluginException("Invalid content in line " + i);
            }

            String interactor_a = splitline[0],
                    interactor_b = splitline[1],
                    official_symbol_a = splitline[2],
                    official_symbol_b = splitline[3],
                    aliases_for_a = splitline[4],
                    aliases_for_b = splitline[5],
                    experimental_system = splitline[6],
                    source = splitline[7],
                    pubmed_id = splitline[8],
                    organism_a_id = splitline[9],
                    organism_b_id = splitline[10];

            if (taxidRestr) {
                try {
                    int taxidA = Integer.parseInt(organism_a_id);
                    int taxidB = Integer.parseInt(organism_b_id);
                    if (!taxids.contains(taxidA) || !taxids.contains(taxidB)) {
                        continue;
                    }
                } catch (NumberFormatException e) {
                    complain("Invalid taxonomy id in line" + i);
                    continue;
                }
            }

            String experimental_system_id = experimental_system.replaceAll("\\s", "_");

            //examine type
            InteractionType category;
            ConceptClass ccInt = itCC_lookup.get(experimental_system);
            if (ccInt != null) {
                if (inherited(ccInt, ccGI)) {
                    category = InteractionType.genetic;
                } else if (inherited(ccInt, ccPI)) {
                    category = InteractionType.molecular;
                } else {
                    throw new PluginConfigurationException("translation file references non-interaction concept class: " + ccInt);
                }
            } else {
                throw new PluginConfigurationException("unknown experimental system: " + experimental_system + " in line " + i);
            }

            //create interactors
            boolean rna = experimental_system.equals("Protein-RNA");
            ONDEXConcept from = getInteractor(category, interactor_a, official_symbol_a, aliases_for_a, organism_a_id, false);
            ONDEXConcept to = getInteractor(category, interactor_b, official_symbol_b, aliases_for_b, organism_b_id, rna);

            // retrieve or construct publication concept
            ONDEXConcept pub = publications.get(pubmed_id);
            if (pub == null) {
                pub = graph.getFactory().createConcept(source, dataSourcePubmed, ccPublication,
                        etImpd);
                // PUBMED_ID is non ambiguous
                pub.createConceptAccession(pubmed_id, dataSourcePubmed, false);
                // SOURCE as GDSs
                String year = "";
                if (source.indexOf("(") > 0 && source.indexOf(")") > 0)
                    year = source.substring(source.indexOf("(") + 1, source.indexOf(")"));
                if (year.length() > 0) {
                    pub.createAttribute(atAuthors, source.substring(0, source.indexOf("(")), true);
                    pub.createAttribute(atYear, Integer.parseInt(year), false);
                } else {
                    pub.createAttribute(atAuthors, source, true);
                }
                publications.put(pubmed_id, pub);
            }

            // retrieve of construct interaction concept
            String intKey = interactor_a + interactor_b + ccInt.getId();
            ONDEXConcept interaction = interactions.get(intKey);
            if (interaction == null) {
                interaction = graph.getFactory().createConcept(intKey, dataSourceBiogrid, ccInt, etImpd);
                graph.getFactory().createRelation(from, interaction, rtActPart, etImpd);
                graph.getFactory().createRelation(to, interaction, rtPassPart, etImpd);
                interactions.put(intKey, interaction);
            }

            ONDEXRelation pubInRel = graph.getRelation(interaction, pub, rtPubIn);
            if (pubInRel == null) {
                graph.getFactory().createRelation(interaction, pub, rtPubIn, etImpd);
            }

            // retrieve or construct experiment evidence type for relation
            if (category == InteractionType.molecular) {
                EvidenceType exp = experiments.get(experimental_system_id);
                if (exp == null) {
                    exp = graph.getMetaData().getFactory().createEvidenceType(experimental_system_id);
                    experiments.put(experimental_system_id, exp);
                }
                interaction.addEvidenceType(exp);
            }
        }

        //map genes and proteins
        for (String orf : polypeps.keySet()) {
            ONDEXConcept protein = polypeps.get(orf);
            ONDEXConcept gene = genes.get(orf);
            if (protein != null && gene != null) {
                graph.getFactory().createRelation(protein, gene, rtEnBy, etImpd);
            }
        }
    }

    private void complain(String string) {
        InconsistencyEvent event = new InconsistencyEvent(string, "");
        event.setLog4jLevel(Level.DEBUG);
        fireEventOccurred(event);
    }

    private void fetchArguments() throws Exception {
        String trlFileName = (String) args.getUniqueValue(TRL_FILE_ARG);
        File trlFile = new File(trlFileName);
        ONDEXGraphMetaData md = graph.getMetaData();
        itCC_lookup = new MetaDataLookup<ConceptClass>(trlFile, md, ConceptClass.class);

        taxids = new ArrayList<Integer>();
        List<?> taxidsRaw = args.getObjectValueList(TAXID_ARG);
        if (taxidsRaw != null) {
            for (Object taxidRaw : taxidsRaw) {
                try {
                    int taxid = Integer.parseInt((String) taxidRaw);
                    taxids.add(taxid);
                } catch (Exception e) {
                    throw new PluginConfigurationException("\"" + taxidRaw + "\" is no valid taxonomy id!");
                }
            }
        }
        taxidRestr = taxids.size() > 0;

    }

    /**
     * returns whether the ConceptClass <code>subClass</code> equals or is a subclass of
     * the ConceptClass <code>superClass</code>
     *
     * @param subClass   the potential subclass
     * @param superClass the potential superclass
     * @return the above.
     */
    private boolean inherited(ConceptClass subClass, ConceptClass superClass) {
        ConceptClass cc_curr = subClass;
        while (!cc_curr.equals(superClass)) {
            cc_curr = cc_curr.getSpecialisationOf();
            if (cc_curr == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * inits all required metadata
     *
     * @throws MetaDataMissingException if any of the required metadata could not be found in the graph.
     */
    private void fetchMetaData() throws MetaDataMissingException {
        ccPolyPep = requireConceptClass("Polypeptide");
        ccGene = requireConceptClass("Gene");
        ccTRNAGene = requireConceptClass("TRNAGene");
        ccRNA = requireConceptClass("RNA");
        ccTRNA = requireConceptClass("TRNA");
        ccPublication = requireConceptClass("Publication");
        ccGI = requireConceptClass("GenInt");
        ccPI = requireConceptClass("PhysInt");

        rtEnBy = requireRelationType("en_by");
        rtActPart = requireRelationType("part_act");
        rtPassPart = requireRelationType("part_pass");
        rtPubIn = requireRelationType("pub_in");

        dataSourceUnknown = requireDataSource("unknown");
        dataSourceBiogrid = requireDataSource("BIOGRID");
        dataSourceEntrez = requireDataSource("NC_GE");
        dataSourcePubmed = requireDataSource("NLM");
        dataSourceMips = requireDataSource("MIPS");

        etImpd = requireEvidenceType("IMPD");

        atTaxid = requireAttributeName("TAXID");
        atAuthors = requireAttributeName("AUTHORS");
        atYear = requireAttributeName("YEAR");
    }

    /**
     * fetches an interactor if it exists. if not it creates one.
     *
     * @return
     */
    private ONDEXConcept getInteractor(InteractionType category, String orf, String symbol, String aliases, String taxid, boolean rna) {
        ONDEXConcept interactor = null;
        switch (category) {
            case molecular:
                if (rna) {
                    interactor = rnas.get(orf);
                    if (interactor == null) {
                        interactor = graph.getFactory().createConcept(orf, dataSourceBiogrid, ccRNA, etImpd);
                        fillConcept(interactor, orf, symbol, aliases, taxid);
                    }
                    rnas.put(orf, interactor);
                } else {
                    interactor = polypeps.get(orf);
                    if (interactor == null) {
                        ConceptClass cc = orf.startsWith("T") ? ccTRNA : ccPolyPep;
                        interactor = graph.getFactory().createConcept(orf, dataSourceBiogrid, cc, etImpd);
                        fillConcept(interactor, orf, symbol, aliases, taxid);
                    }
                    polypeps.put(orf, interactor);
                }
                break;
            case genetic:
                interactor = genes.get(orf);
                if (interactor == null) {
                    ConceptClass cc = orf.startsWith("T") ? ccTRNAGene : ccGene;
                    interactor = graph.getFactory().createConcept(orf, dataSourceBiogrid, cc, etImpd);
                    fillConcept(interactor, orf, symbol, aliases, taxid);
                }
                genes.put(orf, interactor);
                break;
        }
        return interactor;
    }

    /**
     * fills an interactor concept with all kinds of additional info.
     */
    private void fillConcept(ONDEXConcept interactor, String orf, String symbol, String aliases, String taxid) {
        // OFFICIAL_SYMBOL_A as preferred, rest as synonyms
        if (!symbol.trim().equals("N/A")) {
            interactor.createConceptName(symbol, true);
        }
        if (!symbol.equals(aliases)) {
            for (String name : aliases.split("\\|")) {
                if (!name.trim().equalsIgnoreCase("N/A"))
                    interactor.createConceptName(name, false);
            }
        }
        // INTERACTOR_A as non ambiguous for DataSource
        // process special known cases
        //TODO: write a data source classifier String->DataSource that uses regex's
        if (orf.startsWith("EG"))
            interactor.createConceptAccession(orf.substring(2, orf.length()), dataSourceEntrez, true);
        else if (orf.startsWith("Y") || orf.startsWith("Q") || orf.startsWith("T"))
            interactor.createConceptAccession(orf, dataSourceMips, false);
        else
            interactor.createConceptAccession(orf, dataSourceUnknown, false);
        // NCBI taxonomy id from ORGANISM_A_ID
        interactor.createAttribute(atTaxid, taxid, false);
    }

}
