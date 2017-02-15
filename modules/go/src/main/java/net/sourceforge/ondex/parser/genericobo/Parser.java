package net.sourceforge.ondex.parser.genericobo;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.DataURL;
import net.sourceforge.ondex.annotations.DatabaseTarget;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.config.ValidatorRegistry;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.genericobo.chebi.ChebiReferenceContainer;
import net.sourceforge.ondex.parser.genericobo.chebi.Compound;
import net.sourceforge.ondex.parser.genericobo.go.GOReferenceContainer;
import net.sourceforge.ondex.parser.genericobo.go.Organism;
import net.sourceforge.ondex.parser.genericobo.go.StringMod;
import net.sourceforge.ondex.parser.genericobo.po.POReferenceContainer;
import net.sourceforge.ondex.validator.AbstractONDEXValidator;

import org.apache.log4j.Level;

/**
 * OBO Ontology Parser
 *
 * @author hoekmanb
 */
@Status(description = "Tested September 2013 (Artem Lysenko)", status = StatusType.STABLE)
@Authors(authors = {"Berend Hoekmann", "Keywan Hassani-Pak"}, emails = {"", "keywan at users.sourceforge.net"})
@DatabaseTarget(name = "GenericOBO", description = "A parser for the OBO format. Currently it understands GO, PO, TO, CHEBI dialects.", version = "OBO 1.2", url = "http://www.obofoundry.org/")
@DataURL(name = "OBO",
        description = "It works with Plant Ontology, Ttrait Ontology, Gene Ontology and CHEBI",
        urls = {"http://palea.cgrb.oregonstate.edu/viewsvn/Poc/trunk/ontology/collaborators_ontology/gramene/traits/trait.obo?view=co",
                "http://palea.cgrb.oregonstate.edu/viewsvn/Poc/trunk/ontology/OBO_format/po_anatomy.obo?view=co"})
public class Parser extends ONDEXParser
{

    private static Parser instance;
    
    private String oboType;

    public Parser() {
        Parser.instance = this;
    }

    public String getName() {
        return "genericOBO";
    }

    public String getVersion() {
        return "03.11.2010";
    }

    @Override
    public String getId() {
        return "genericobo";
    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition[]{
                new StringArgumentDefinition(ArgumentNames.OBOTYPE_ARG, 
                		ArgumentNames.OBOTYPE_ARG_DESC, true, "PO", false),
                new BooleanArgumentDefinition(ArgumentNames.OBSOLETES_ARG,
                        ArgumentNames.OBSOLETES_ARG_DESC, false, false),
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
                        FileArgumentDefinition.INPUT_FILE_DESC, true, true, false, false),
        };
    }

    public void start() throws InvalidPluginArgumentException {
    	
    	// type/data source
        this.oboType = (String) args.getUniqueValue(ArgumentNames.OBOTYPE_ARG);

        // get importdata dir
        String oboFile = (String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE);

        // parse obsoletes
        Boolean getObsoletes = (Boolean) args.getUniqueValue(ArgumentNames.OBSOLETES_ARG);
        
        GeneralOutputEvent so = new GeneralOutputEvent(
                "Parsing OBO file: "+oboFile, getCurrentMethodName());
        so.setLog4jLevel(Level.INFO);
        fireEventOccurred(so);
        
        if(oboType.equalsIgnoreCase(MetaData.cvPO)
        	||	oboType.equalsIgnoreCase(MetaData.cvGO)
        	||	oboType.equalsIgnoreCase(MetaData.cvChebi)){
        	
        	parseFile(oboFile, getObsoletes, graph, null, null);
        }else{
        	DataSource dataSource = graph.getMetaData().getDataSource(oboType);
        	ConceptClass cc = graph.getMetaData().getConceptClass(oboType);
        	
        	parseFile(oboFile, getObsoletes, graph, dataSource, cc);
        }

        so = new GeneralOutputEvent("OBO parsing finished!",
        		getCurrentMethodName());
        so.setLog4jLevel(Level.INFO);
        fireEventOccurred(so);
    }

    private void parseFile(String obo_file, boolean getObsoletes,
                           ONDEXGraph graph, DataSource dataSource, ConceptClass ccGeneric) {
        // define here the abbrev. for the relations to be used
        Map<String, String> relation_name = new TreeMap<String, String>();
        relation_name.put("is_a", MetaData.is_a); // Generic
        relation_name.put("part_of", MetaData.is_p); // Generic
        relation_name.put("is_part_of", MetaData.is_p); // Generic
        relation_name.put("has_part", MetaData.is_p); // Generic
        relation_name.put("regulates", MetaData.rtRegulates); // Generic
        relation_name.put("positively_regulates", MetaData.rtPos_reg); // Generic
        relation_name.put("negatively_regulates", MetaData.rtNeg_reg); // Generic
        relation_name.put("rp_by", MetaData.rtRp_by); // Generic
        relation_name.put("develops_from", MetaData.dev); // Generic
        relation_name.put("is_substituent_group_from", MetaData.isChemSubGrFrom); // Chebi
        relation_name.put("is_conjugate_base_of", MetaData.isConjugate); // Chebi
        relation_name.put("is_conjugate_acid_of", MetaData.isConjugate); // Chebi
        relation_name.put("is_enantiomer_of", MetaData.isEnantiomer); // Chebi
        relation_name.put("is_tautomer_of", MetaData.isEnantiomer); // Chebi
        relation_name.put("has_functional_parent", MetaData.hasChemFunctionalParent);// Chebi
        relation_name.put("has_parent_hydride", MetaData.hasChemParentHybride); // Chebi
        relation_name.put("has_role", MetaData.hasFunction); //Chebi
        relation_name.put("adjacent_to", MetaData.rtAdjacentTo); //PO
        relation_name.put("develops_from", MetaData.rtDevelopsFrom); //PO
        relation_name.put("has_part", MetaData.rtHasPart); //PO
        relation_name.put("derives_from", MetaData.rtDerivesFrom); //PO
        relation_name.put("participates_in", MetaData.rtParticipatesIn); //PO
        relation_name.put("has_participant", MetaData.rtHasParticipant); //PO

        List<OboConcept> results = null;
        OboImporter imp = new OboImporter();

        try {
            results = imp.getFileContent(obo_file, getObsoletes);
        } catch (Exception e) {
            DataFileMissingEvent dfm = new DataFileMissingEvent(
                    "Could not find flat file " + obo_file + "("
                            + e.getLocalizedMessage() + ")",
                    getCurrentMethodName());
            dfm.setLog4jLevel(Level.ERROR);
            fireEventOccurred(dfm);
            e.printStackTrace();
            return;
        }

        EvidenceType et = graph.getMetaData().getEvidenceType(MetaData.IMPD); // imported from database

        // required for creating concepts and relations
        ONDEXConcept concept = null;
        ONDEXConcept fromConcept = null;
        ONDEXConcept toConcept = null;

        // go through the results ArrayList and create Concepts and Relations
        Hashtable<String, ONDEXConcept> createdConcepts = new Hashtable<String, ONDEXConcept>();
        for (OboConcept obo : results) {

            String id = obo.getId();
            String annotation = obo.getDefinition(); // definition field in
            // CHEBI flat file

            String[] annotationRefs = obo.getDefinitionRefs();

            if (annotation == null)
                annotation = "";

            if (annotationRefs != null) // adding annotation references
            {
                annotation += " ( Reference: ";

                for (String annotationRef : annotationRefs) {
                    annotation += annotationRef;
                    annotation += " ";
                }
                annotation += ")";
            }

            if (oboType.equalsIgnoreCase(MetaData.cvPO)) {
            	dataSource = graph.getMetaData().getDataSource(MetaData.cvPO);
                ConceptClass ccAnat = graph.getMetaData()
                        .getConceptClass(MetaData.POStructure);
                ConceptClass ccDev = graph.getMetaData()
                        .getConceptClass(MetaData.POGrowthStage);

                if (obo.getNamespace().equalsIgnoreCase("plant_growth_and_development_stage")
                        || obo.getNamespace().equalsIgnoreCase("plant_structure_development_stage")) {
                    concept = graph.getFactory().createConcept(id, annotation, dataSource, ccDev, et);
                } else if (obo.getNamespace().equalsIgnoreCase("plant_anatomy")) {
                    concept = graph.getFactory().createConcept(id, annotation, dataSource, ccAnat, et);
                } // file: temporal_gramene.obo                
                else if (obo.getNamespace().equalsIgnoreCase("")) {
                    concept = graph.getFactory().createConcept(id, annotation, dataSource, ccDev, et);
                } else {
                    ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new GeneralOutputEvent(
                            "Unknown CC in PO Ontology: " + obo.getNamespace(),
                            getCurrentMethodName()));
                }
            } else if (oboType.equalsIgnoreCase(MetaData.cvGO)) {
            	dataSource = graph.getMetaData().getDataSource(MetaData.cvGO);
                ConceptClass ccMolFunc = graph.getMetaData()
                        .getConceptClass(MetaData.MolFunc);
                ConceptClass ccBioProc = graph.getMetaData()
                        .getConceptClass(MetaData.BioProc);
                ConceptClass ccCelComp = graph.getMetaData()
                        .getConceptClass(MetaData.CelComp);

                if (obo.getNamespace().equalsIgnoreCase("molecular_function")) {
                    // create ONDEXConcept
                    concept = graph.getFactory().createConcept(id, annotation, dataSource,
                            ccMolFunc, et);
                    createdConcepts.put(id, concept);
                } else if (obo.getNamespace().equalsIgnoreCase(
                        "biological_process")) {
                    // create ONDEXConcept
                    concept = graph.getFactory().createConcept(id, annotation, dataSource,
                            ccBioProc, et);
                    createdConcepts.put(id, concept);
                } else if (obo.getNamespace().equalsIgnoreCase(
                        "cellular_component")) {
                    // create ONDEXConcept
                    concept = graph.getFactory().createConcept(id, annotation, dataSource,
                            ccCelComp, et);
                    createdConcepts.put(id, concept);
                } else {
                    ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new GeneralOutputEvent(
                            "Unknown CC in GO Ontology: " + obo.getNamespace(),
                            getCurrentMethodName()));
                }
            } else if (oboType.equalsIgnoreCase(MetaData.cvChebi)) {
            	dataSource = graph.getMetaData().getDataSource(MetaData.cvChebi);
                ConceptClass ccT = graph.getMetaData().getConceptClass(
                        MetaData.Thing);
                ConceptClass ccC = graph.getMetaData().getConceptClass(
                        MetaData.Comp);

                if (Compound.isCompound(obo)) {
                    // logical parsing
                    concept = graph.getFactory().createConcept(id, annotation, dataSource, ccC,
                            et);
                } else if (Compound.isCompoundBasedOnRelations(obo)) {
                    // grep compound or thing based on the relations the "thing"
                    // has.
                    concept = graph.getFactory().createConcept(id, annotation, dataSource, ccC,
                            et);
                } else {
                    concept = graph.getFactory().createConcept(id, annotation, dataSource, ccT,
                            et);
                }
            }else{
                concept = graph.getFactory().createConcept(id, annotation, dataSource, ccGeneric,
                        et);
            }

            // create ONDEXConcept

            createdConcepts.put(id, concept);

            // go through list of ConceptAccessions from other CVs

            ReferenceContainer xRef = null;
            
               
            if (oboType.equalsIgnoreCase(MetaData.cvPO)) {
                xRef = new POReferenceContainer(graph);
            } else if (oboType.equalsIgnoreCase(MetaData.cvChebi)) {
                xRef = new ChebiReferenceContainer(graph);
            } else if (oboType.equalsIgnoreCase(MetaData.cvGO)) {
                xRef = new GOReferenceContainer(graph);
            }else{
            	 xRef = new GenericReferenceContainer(graph);
            }

/*// xref omitted
            for (int j = 0; j < obo.getRefs().size(); j++) {
                xRef.setXrefString(obo.getRefs().get(j));
                xRef.analyseXRef();
                if (xRef.writeXRef()) {
                    if (concept.getConceptAccession(xRef.getXref()
                            .toUpperCase(), xRef.getDataSourceAccession()) == null) {
                        concept.createConceptAccession(xRef.getXref()
                                .toUpperCase(), xRef.getDataSourceAccession(), xRef
                                .getAmbigous());
                    }
                }
            }
*/
            if (oboType.equalsIgnoreCase(MetaData.cvChebi)) {
                String accession = id.toUpperCase();
                int prefix = id.indexOf(':');
                if (prefix > -1) {
                    accession = accession.substring(prefix + 1, accession.length());
                }

                concept.createConceptAccession(accession, dataSource, false);

                // go through list of alternative ConceptAccessions
                for (String altId : obo.getAlt_ids()) {

                    prefix = altId.indexOf(':');
                    if (prefix > -1) {
                        altId = altId.substring(prefix + 1, altId.length());
                    }
                    concept.createConceptAccession(altId.toUpperCase(), dataSource,
                            false);
                }

            } else {
                concept.createConceptAccession(id.toUpperCase(), dataSource, false);

                // go through list of alternative ConceptAccessions
                for (String altId : obo.getAlt_ids()) {
                    concept.createConceptAccession(altId.toUpperCase(), dataSource,
                            false);
                }
            }


            // create preferred ConceptName
            String concept_name = obo.getName().trim();
            // concept_name = concept_name.trim();
            concept.createConceptName(concept_name, true);

            HashSet<String> writtenSyns = new HashSet<String>();// represents
            // already
            // written syns
            // -- prevents
            // duplication
            // accross syn
            // types

            for (String synType : obo.getSynonyms().keySet()) {
                // write exact syns first these are the most important
                if (synType.equalsIgnoreCase(OboConcept.exactSynonym)) {
                    for (String syn : obo.getSynonyms().get(synType)) {
                        syn = syn.trim();

                        syn = StringMod.removeActivity(syn); // GO specifc
                        // but, doesn't
                        // do any harm

                        if (!syn.equalsIgnoreCase(concept_name)
                                && syn.length() > 0
                                && !writtenSyns.contains(syn)) {
                            concept.createConceptName(syn, true); // pref
                            // exact
                            writtenSyns.add(syn);
                        }
                    }

                } else {
                    for (String syn : obo.getSynonyms().get(synType)) {
                        syn = syn.trim();

                        syn = StringMod.removeActivity(syn); // GO specifc
                        // but, doesn't
                        // do any harm

                        if (!syn.equalsIgnoreCase(concept_name)
                                && syn.length() > 0
                                && !writtenSyns.contains(syn)) { // NB there
                            // was no
                            // need to
                            // do 2nd
                            // part for
                            // exactSyns
                            // as each
                            // type of
                            // syns is a
                            // set
                            concept.createConceptName(syn, false);
                            writtenSyns.add(syn);
                        }
                    }
                }
            }
        }

        // create ONDEXRelations
        RelationType rtset = null;

        for (OboConcept obo : results) {

            // get fromConcept from ID
            fromConcept = createdConcepts.get(obo.getId());

            for (int j = 0; j < obo.getRelations().size(); j++) {
                List<String> relation = obo.getRelations().get(j); // type_of_relation,
                // accession

                // get current RelationType
                String rel_type_orig = relation.get(0);
                String rel_type = relation_name.get(rel_type_orig);

                if (rel_type == null) {
                    fireEventOccurred(new RelationTypeMissingEvent(
                            "Relation Type \"" + rel_type_orig + "\" missing!",
                            getCurrentMethodName()));
                }
                rtset = graph.getMetaData().getRelationType(rel_type);

                if (rtset == null) {
                    fireEventOccurred(new RelationTypeMissingEvent(
                            "Relation Typset \"" + rel_type + "\" missing!",
                            getCurrentMethodName()));
                }

                // get toConcept from ID
                String to_id = relation.get(1);
                toConcept = createdConcepts.get(to_id);

                if (toConcept == null) {
                    System.out.println("Missing Concept ID: " + to_id);
                } else {
                    if (graph.getRelation(fromConcept, toConcept, rtset) == null) {
                        graph.getFactory().createRelation(fromConcept, toConcept, rtset,
                                et);
                    } else {
                        //System.out.println("RELATION Already Exists (from: "
                        //		+ obo.getId() + " to: " + to_id + ")");
                    }
                }
            }
        }

        if (oboType.equalsIgnoreCase(MetaData.cvGO)) {
            // create links to TX

            AbstractONDEXValidator taxonomy = ValidatorRegistry.validators
                    .get("taxonomy");
            RelationType rtSensu = graph.getMetaData()
                    .getRelationType(MetaData.sensu);
            ConceptClass ccTaxon = graph.getMetaData().getConceptClass(
                    MetaData.Taxon);
            DataSource dataSourceTX = graph.getMetaData().getDataSource(MetaData.cvTX);
            if (dataSourceTX == null)
                fireEventOccurred(new DataSourceMissingEvent("DataSource " + dataSourceTX
                        + " not in meta data", getCurrentMethodName()));

            Hashtable<String, ONDEXConcept> taxons = new Hashtable<String, ONDEXConcept>();
            Set<String> unclassified_sensus = new TreeSet<String>();

            for (OboConcept obo : results) {

                String concept_name = obo.getName();
                String sensu = Organism.getOrganism(concept_name);
                concept_name = StringMod.removeUnwanted(concept_name);

                if (sensu.compareTo("nosensu") != 0) {

                    String taxid = (String) taxonomy.validate(sensu);

                    if (taxid.length() > 0) {
                        // if a tax id was found:
                        // change name (remove sensu) and add sensu relations

                        if (taxons.containsKey(taxid)) {
                            toConcept = taxons.get(taxid);
                        } else {
                            toConcept = graph.getFactory().createConcept(taxid, dataSource,
                                    ccTaxon, et);
                            toConcept.createConceptName(sensu, false);
                            toConcept.createConceptAccession(taxid, dataSourceTX, true);
                            taxons.put(taxid, toConcept);
                        }

                        fromConcept = createdConcepts.get(obo.getId());

                        // create Relation
                        graph.getFactory().createRelation(fromConcept, toConcept,
                                rtSensu, et);

                    } else
                        unclassified_sensus.add(sensu);
                }
            }

            // print out all sensu terms that couldn't be classified
            for (String unclassified_sensu : unclassified_sensus) {
                DataFileErrorEvent dfi = new DataFileErrorEvent(
                        "No Taxonomy mapping found for " + unclassified_sensu,
                        getCurrentMethodName());
                dfi.setLog4jLevel(Level.INFO);
                fireEventOccurred(dfi);
            }
        }
    }

    public static void propagateEventOccurred(EventType et) {
        if (instance != null)
            instance.fireEventOccurred(et);
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
        return "[CLASS:" + className + " - METHOD:" + name + " LINE:" + line
                + "]";
    }

    @Override
    public String[] requiresValidators() {
        return null;
        //return new String[] { "taxonomy" };
    }

}
