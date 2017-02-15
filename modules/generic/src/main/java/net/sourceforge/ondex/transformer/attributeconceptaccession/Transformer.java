package net.sourceforge.ondex.transformer.attributeconceptaccession;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.args.StringMappingPairArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

import java.util.*;

/**
 * This transformer turns a specified Attribute into a separate concept with the value
 * as accession.
 *
 * @author taubertj
 */
@Authors(authors = {"Jan Taubert"}, emails = {"jantaubert at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Transformer extends ONDEXTransformer implements
        ArgumentNames {

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition[]{
                new StringArgumentDefinition(TARGET_CONCEPT_CLASS,
                        TARGET_CONCEPT_CLASS_DESC, true, null, false),
                new StringArgumentDefinition(TARGET_DATASOURCE, TARGET_DATASOURCE_DESC, true,
                        null, false),
                new StringArgumentDefinition(RELATION_TYPE_ARG,
                        RELATION_TYPE_ARG_DESC, true, null, false),
                new StringArgumentDefinition(ATTR_NAME, ATTR_NAME_DESC, true,
                        null, false),
                new StringMappingPairArgumentDefinition(
                        CONCEPTCLASS_RESTRICTION_ARG,
                        CONCEPTCLASS_RESTRICTION_ARG_DESC, false, null, true),
                new StringMappingPairArgumentDefinition(DATASOURCE_RESTRICTION_ARG,
                        DATASOURCE_RESTRICTION_ARG_DESC, false, null, true)};
    }

    @Override
    public String getName() {
        return "Attribute to concept accession";
    }

    @Override
    public String getVersion() {
        return "06.07.2009";
    }

    @Override
    public String getId() {
        return "attributeconceptaccession";
    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws Exception {

        Object[] ccs = args.getObjectValueArray(CONCEPTCLASS_RESTRICTION_ARG);

        Object[] cvs = args.getObjectValueArray(DATASOURCE_RESTRICTION_ARG);

        // add CC restriction
        Set<ConceptClass> ccRestriction = new HashSet<ConceptClass>();
        if (ccs != null && ccs.length > 0) {
            for (Object cc : ccs) {
                ConceptClass tempCC = graph.getMetaData()
                        .getConceptClass((String) cc);

                if (tempCC != null) {
                    ccRestriction.add(tempCC);
                    fireEventOccurred(new GeneralOutputEvent(
                            "Added ConceptClass restriction for "
                                    + tempCC.getId(),
                            "[Transformer - start]"));
                } else {
                    ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
                            .fireEventOccurred(
                                    new WrongParameterEvent(cc
                                            + " is not a valid ConceptClass.",
                                            "[Transformer - start]"));
                }
            }
        }

        // add DataSource restriction
        Set<DataSource> dataSourceRestriction = new HashSet<DataSource>();
        if (cvs != null && cvs.length > 0) {
            for (Object cv : cvs) {
                DataSource tempDataSource = graph.getMetaData().getDataSource((String) cv);

                if (tempDataSource != null) {
                    dataSourceRestriction.add(tempDataSource);
                    ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
                            .fireEventOccurred(
                                    new GeneralOutputEvent(
                                            "Added DataSource restriction for "
                                                    + tempDataSource.getId(),
                                            "[Transformer - start]"));
                } else {
                    ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
                            .fireEventOccurred(
                                    new WrongParameterEvent(cv
                                            + " is not a valid DataSource.",
                                            "[Transformer - start]"));
                }
            }
        }

        // get all specified meta data
        ONDEXGraphMetaData meta = graph.getMetaData();

        // evidence type for target concept
        EvidenceType targetET = meta.getEvidenceType(MetaData.ET);
        if (targetET == null)
            targetET = meta.getFactory().createEvidenceType(MetaData.ET);

        // target concept class, might be missing
        String ccID = (String) args.getUniqueValue(TARGET_CONCEPT_CLASS);
        ConceptClass targetCC = meta.getConceptClass(ccID);
        if (targetCC == null)
            targetCC = meta.getFactory().createConceptClass(ccID);

        // target DataSource, might be missing
        String cvID = (String) args.getUniqueValue(TARGET_DATASOURCE);
        DataSource targetDataSource = meta.getDataSource(cvID);
        if (targetDataSource == null)
            targetDataSource = meta.getFactory().createDataSource(cvID);

        // target RT, might be missing
        String rtID = (String) args.getUniqueValue(RELATION_TYPE_ARG);
        RelationType targetRT = meta.getRelationType(rtID);
        if (targetRT == null)
            targetRT = meta.getFactory().createRelationType(rtID);

        // the attribute name has to exist
        String anID = (String) args.getUniqueValue(ATTR_NAME);
        AttributeName an = meta.getAttributeName(anID);
        if (an == null) {
            ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
                    .fireEventOccurred(
                            new WrongParameterEvent(anID
                                    + " is not a valid AttributeName.",
                                    "[Transformer - start]"));
            return;
        }

        Map<String, List<ONDEXConcept>> map = new HashMap<String, List<ONDEXConcept>>();

        // iterate over all concepts which have the Attribute
        for (ONDEXConcept concept : graph.getConceptsOfAttributeName(an)) {

            // check if concept class allowed
            if (ccRestriction.size() > 0 && !ccRestriction.contains(concept.getOfType())) {
                continue; // skip to next one
            }

            // check if DataSource allowed
            if (dataSourceRestriction.size() > 0 && !dataSourceRestriction.contains(concept.getElementOf())) {
                continue; // skip to next one
            }

            // get Attribute from concept and its string value
            Attribute attribute = concept.getAttribute(an);
            String value = attribute.getValue().toString();

            // make sure that target concepts are unique
            if (!map.containsKey(value))
                map.put(value, new ArrayList<ONDEXConcept>());
            map.get(value).add(concept);
        }

        // finally create target concepts and all the relations
        for (String value : map.keySet()) {
            // create target concept
            ONDEXConcept target = graph.getFactory().createConcept(value, targetDataSource, targetCC, targetET);
            target.createConceptAccession(value, targetDataSource, false);

            for (ONDEXConcept concept : map.get(value)) {
                // create relation to target concept
                graph.getFactory().createRelation(concept, target, targetRT, targetET);
            }
        }

    }

}
