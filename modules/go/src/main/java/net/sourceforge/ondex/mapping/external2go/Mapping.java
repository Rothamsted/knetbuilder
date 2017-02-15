package net.sourceforge.ondex.mapping.external2go;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.config.LuceneRegistry;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.searchable.LuceneConcept;
import net.sourceforge.ondex.core.searchable.LuceneEnv;
import net.sourceforge.ondex.core.searchable.LuceneQueryBuilder;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;
import net.sourceforge.ondex.mapping.ONDEXMapping;

import org.apache.lucene.search.Query;

/**
 * Parses any *2go mapping from a given file.
 *
 * @author peschr
 */
@Custodians(custodians = {"Keywan Hassani-pak"}, emails = {"keywan at users.sourceforge.net"})
public class Mapping extends ONDEXMapping implements MetaData, ArgumentNames {
    private ConceptClass thing;
    private DataSource goDataSource;
    private EvidenceType etExternal2Go;

    /**
     * Constructor
     */
    public Mapping() {
    }

    @Override
    public void start() throws InvalidPluginArgumentException {
        String filename = (String) args.getUniqueValue(INPUT_FILE_ARG);

        AttributeName att = graph.getMetaData().getAttributeName(ATT_DATASOURCE);
        if (att == null) {
            att = graph.getMetaData().getFactory().createAttributeName(ATT_DATASOURCE, "Datasource where this ONDEXEntity originated", String.class);
        }

        etExternal2Go = graph.getMetaData().getEvidenceType(MetaData.EV_EXTERNAL2GO);
        // RTset: m_isp
        RelationType rt_m_isp = graph.getMetaData().getRelationType(MetaData.RT_MSP);
        if (rt_m_isp == null)
            fireEventOccurred(new RelationTypeMissingEvent(MetaData.RT_MSP, "[Mapping - setONDEXGraph]"));

        RelationType rt_equ = graph.getMetaData().getRelationType(MetaData.RT_EQU);
        if (rt_equ == null)
            fireEventOccurred(new RelationTypeMissingEvent(MetaData.RT_EQU, "[Mapping - setONDEXGraph]"));

        RelationType rt_proc = graph.getMetaData().getRelationType(MetaData.RT_participatesIn);
        if (rt_proc == null)
            fireEventOccurred(new RelationTypeMissingEvent(MetaData.RT_participatesIn, "[Mapping - setONDEXGraph]"));

        RelationType rt_func = graph.getMetaData().getRelationType(MetaData.RT_hasFunction);
        if (rt_func == null)
            fireEventOccurred(new RelationTypeMissingEvent(MetaData.RT_hasFunction, "[Mapping - setONDEXGraph]"));

        RelationType rt_comp = graph.getMetaData().getRelationType(MetaData.RT_locatedIn);
        if (rt_comp == null)
            fireEventOccurred(new RelationTypeMissingEvent(MetaData.RT_locatedIn, "[Mapping - setONDEXGraph]"));

        goDataSource = graph.getMetaData().getDataSource(CV_GO);

        thing = graph.getMetaData().getConceptClass(CC_Thing);

        Pattern spaceSplitter = Pattern.compile("[\\s|\\t]+");

        ConceptClass ccFrom = graph.getMetaData().getConceptClass(args.getUniqueValue(ArgumentNames.FROM_CONCEPT_CLASS_ARG).toString());
        if (ccFrom == null)
            fireEventOccurred(new ConceptClassMissingEvent(args.getUniqueValue(ArgumentNames.FROM_CONCEPT_CLASS_ARG).toString(), "[Mapping - setONDEXGraph]"));

        DataSource dataSourceFrom = graph.getMetaData().getDataSource(args.getUniqueValue(ArgumentNames.FROM_CV_ARG).toString());
        if (dataSourceFrom == null)
            fireEventOccurred(new DataSourceMissingEvent(args.getUniqueValue(ArgumentNames.FROM_CV_ARG).toString(), "[Mapping - setONDEXGraph]"));


        HashMap<String, List<String>> mapping = new HashMap<String, List<String>>();
        int mappingCount = 0;
        try {
            // read in mapping file
            BufferedReader reader = new BufferedReader(new FileReader(filename));

            while (reader.ready()) {
                String line = reader.readLine();
                if (line.charAt(0) != '!') {
                    String from = line.substring(0, line.indexOf('>') - 1);
                    String go = line.substring(line.length() - 10, line.length());

                    String[] values = spaceSplitter.split(from);

                    String accession = values[0];

                    int index = accession.indexOf(':');
                    if (index > -1)
                        accession = accession.substring(index + 1);

                    mappingCount++;

                    List<String> goTerms = mapping.get(accession);
                    if (goTerms == null) {
                        goTerms = new ArrayList<String>();
                        mapping.put(accession, goTerms);
                    }
                    goTerms.add(go);
                }
            }

            reader.close();
        } catch (FileNotFoundException fnfe) {
            fireEventOccurred(new DataFileMissingEvent("File " + filename + " not found.", "[Mapping - setONDEXGraph]"));
        } catch (IOException ioe) {
            fireEventOccurred(new DataFileErrorEvent("Error reading file " + filename + ".", "[Mapping - setONDEXGraph]"));
        }
        ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new GeneralOutputEvent("Found " + mappingCount + " described relations in *2go mapping file", Mapping.class.toString()));

        int relations = 0;

        for (String fromAccession : mapping.keySet()) {
            Query query = LuceneQueryBuilder.searchConceptByConceptAccessionExact(dataSourceFrom, fromAccession, ccFrom, true);
            LuceneEnv lenv = LuceneRegistry.sid2luceneEnv.get(graph.getSID());
            Set<ONDEXConcept> results = lenv.searchInConcepts(query);

            List<String> goAccessions = mapping.get(fromAccession);

            for (ONDEXConcept externalConcept : results) {
                if (externalConcept instanceof LuceneConcept) {
                    externalConcept = ((LuceneConcept) externalConcept).getParent();
                }


                for (String goAccession : goAccessions) {
                    Query query2 = LuceneQueryBuilder.searchConceptByConceptAccessionExact(goDataSource, goAccession, true);
                    Set<ONDEXConcept> results2 = lenv.searchInConcepts(query2);

                    if (results2 == null || results2.size() == 0) {
                        System.err.println("Accession: " + goAccession + " not found creating new one");
                        ONDEXConcept concept = graph.getFactory().createConcept(goAccession, this.goDataSource, thing, etExternal2Go);
                        concept.createConceptAccession(goAccession, this.goDataSource, false);

                        BitSet sbs = new BitSet();
                        sbs.set(concept.getId());
                        results2 = BitSetFunctions.create(graph, ONDEXConcept.class, sbs);
                    }

                    for (ONDEXConcept goConcept : results2) {
                        if (goConcept instanceof LuceneConcept) {
                            goConcept = ((LuceneConcept) goConcept).getParent();
                        }

                        RelationType rt = null;
                        String goCC = goConcept.getOfType().getId();

                        //Define relation type, EC2GO should have EQU relations
                        if (dataSourceFrom.getId().toUpperCase().equals("EC") &&
                                (goCC.equals(CC_BioProc) || goCC.equals(CC_CelComp) ||
                                        goCC.equals(CC_MolFunc))) {
                            rt = rt_equ;
                        } else if (goCC.equals(CC_BioProc)) {
                            rt = rt_proc;
                        } else if (goCC.equals(CC_CelComp)) {
                            rt = rt_comp;
                        } else if (goCC.equals(CC_MolFunc)) {
                            rt = rt_func;
                        } else if (goCC.equals(CC_Thing)) {
                            rt = rt_m_isp;
                        } else {
                            continue; //prob. a protein or something with a GO accession ignore
                        }

                        ONDEXRelation relation = graph.getRelation(externalConcept,
                                goConcept, rt);

                        if (relation == null) {
                            relation = graph.getFactory().createRelation(externalConcept, goConcept, rt, etExternal2Go);
                        } else if (!relation.getEvidence().contains(etExternal2Go)) {
                            relation.addEvidenceType(etExternal2Go);
                        }
                        relation.createAttribute(att, "external2go", false);
                        relations++;
                    }
                }
            }
        }

        ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new GeneralOutputEvent("Mapped " + relations + " relations", Mapping.class.toString()));
    }

    /**
     * Returns the input file ArgumentDefinition of this mapping.
     *
     * @return ArgumentDefinition<?>[]
     */
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        FileArgumentDefinition
                filenameARG = new FileArgumentDefinition(
                ArgumentNames.INPUT_FILE_ARG,
                ArgumentNames.INPUT_FILE_ARG_DESC, true, true, false, false);
        StringArgumentDefinition fromCCType = new StringArgumentDefinition(
                ArgumentNames.FROM_CONCEPT_CLASS_ARG,
                ArgumentNames.FROM_CONCEPT_CLASS_ARG_DESC, true, null, false);
        StringArgumentDefinition fromCVType = new StringArgumentDefinition(
                ArgumentNames.FROM_CV_ARG,
                ArgumentNames.FROM_CV_ARG_DESC, true, null, false);
        return new ArgumentDefinition<?>[]{filenameARG, fromCVType, fromCCType};
    }

    /**
     * Returns name of this mapping.
     *
     * @return String
     */
    public String getName() {
        return "external2go";
    }

    /**
     * Returns version of this mapping.
     *
     * @return String
     */
    public String getVersion() {
        return "29.01.08";
    }

    @Override
    public String getId() {
        return "external2go";
    }

    /**
     * No IndexONDEXGraph is required.
     *
     * @return false
     */
    public boolean requiresIndexedGraph() {
        return true;
    }

    public String[] requiresValidators() {
        return new String[0];
    }
}
