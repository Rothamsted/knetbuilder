package net.sourceforge.ondex.parser.ec;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.DataURL;
import net.sourceforge.ondex.annotations.DatabaseTarget;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.annotations.metadata.*;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.*;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.ec.data.Entry;
import net.sourceforge.ondex.parser.ec.data.Extractor;
import net.sourceforge.ondex.parser.ec.data.Relation;
import org.apache.log4j.Level;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * EC Nomenclature Parser for the files: "enzclass.txt" "enzyme.dat" from
 * ftp://ftp.expasy.org/databases/enzyme/release_with_updates/
 * <p/>
 * latest tested version: 2012/11/28
 *
 * @author winnenbr, taubertj
 */
@Status(description = "Tested December 2013 (Jacek Grzebyta)", status = StatusType.STABLE)
@DatabaseTarget(name = "EXPASY ENZYME", description = "EXPASY ENZYME database", version = "2012/11/28", url = "http://www.expasy.ch/")
@DataURL(name = "enzclass and enzyme files", description = "enzclass and enzyme, all txt and dat files in directory files", urls = {"ftp://ftp.expasy.org/databases/enzyme"})
@DataSourceRequired(ids = {
        MetaData.DS_EC, MetaData.DS_PROTSITE, MetaData.DS_UNIPROTKB})
@ConceptClassRequired(ids = {
        MetaData.CC_EC, MetaData.CC_PROTEIN, MetaData.CC_PROTEIN_FAMILY})
@EvidenceTypeRequired(ids = {MetaData.ET})
@RelationTypeRequired(ids = {
        MetaData.RT_CATALYSEING_CLASS, MetaData.RT_IS_A})
@AttributeNameRequired(ids = {
        MetaData.ATT_TAXID})
public class Parser extends ONDEXParser
{

    /**
     * Returns name of parser.
     *
     * @return String
     */
    public String getName() {
        return new String("EXPASY ENZYME");
    }

    /**
     * Returns version of parser.
     *
     * @return String
     */
    public String getVersion() {
        return "27.12.2012";
    }

    @Override
    public String getId() {
        return "ec";
    }


    /**
     * Returns ArgumentDefinitions for this parser.
     *
     * @return ArgumentDefinition<?>[]
     */
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new BooleanArgumentDefinition(ArgumentNames.DELETED_ARG, ArgumentNames.DELETED_ARD_DESC, false, false),
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_DIR, FileArgumentDefinition.INPUT_DIR_DESC, true, true, true, false)
        };
    }

    /**
     * Start processing.
     */
    public void start() throws InvalidPluginArgumentException {

        instance = this;

        GeneralOutputEvent goe = new GeneralOutputEvent("Starting EC parsing...", getCurrentMethodName());
        goe.setLog4jLevel(Level.INFO);
        fireEventOccurred(goe);

        Boolean getDeleted = (Boolean) args.getUniqueValue(ArgumentNames.DELETED_ARG);
        if (getDeleted == null) {
            getDeleted = false;
        }

        if (getDeleted) {
            goe = new GeneralOutputEvent("Parsing also deleted entries.", getCurrentMethodName());
            goe.setLog4jLevel(Level.INFO);
            fireEventOccurred(goe);
        }

        // check DataSource
        DataSource dataSource_ec = graph.getMetaData().getDataSource(MetaData.DS_EC);
        if (dataSource_ec == null) {
            this.fireEventOccurred(new DataSourceMissingEvent(
                    MetaData.DS_EC, getCurrentMethodName()));
            return;
        }
        // check CC
        ConceptClass cc_ec = graph.getMetaData()
                .getConceptClass(MetaData.CC_EC);
        if (cc_ec == null) {
            this.fireEventOccurred(new ConceptClassMissingEvent(
                    MetaData.CC_EC, getCurrentMethodName()));
            return;
        }

        ConceptClass cc_domain = graph.getMetaData()
                .getConceptClass(MetaData.CC_PROTEIN_FAMILY);
        if (cc_domain == null) {
            this.fireEventOccurred(new ConceptClassMissingEvent(
                    MetaData.CC_PROTEIN_FAMILY, getCurrentMethodName()));
            return;
        }

        ConceptClass cc_protein = graph.getMetaData()
                .getConceptClass(MetaData.CC_PROTEIN);
        if (cc_protein == null) {
            this.fireEventOccurred(new ConceptClassMissingEvent(
                    MetaData.CC_PROTEIN, getCurrentMethodName()));
            return;
        }
        // check ET
        EvidenceType et = graph.getMetaData()
                .getEvidenceType(MetaData.ET);
        if (et == null) {
            this.fireEventOccurred(new EvidenceTypeMissingEvent(
                    MetaData.ET, getCurrentMethodName()));
            return;
        }
        // check RT
        RelationType rt_is_a = graph.getMetaData()
                .getRelationType(MetaData.RT_IS_A);
        if (rt_is_a == null) {
            this.fireEventOccurred(new RelationTypeMissingEvent(
                    MetaData.RT_IS_A, getCurrentMethodName()));
            return;
        }

        RelationType cat_c = graph.getMetaData()
                .getRelationType(MetaData.RT_CATALYSEING_CLASS);
        if (cat_c == null) {
            this.fireEventOccurred(new RelationTypeMissingEvent(
                    MetaData.RT_CATALYSEING_CLASS, getCurrentMethodName()));
            return;
        }

        AttributeName taxId = graph.getMetaData().getAttributeName(MetaData.ATT_TAXID);
        if (taxId == null) {
            this.fireEventOccurred(new AttributeNameMissingEvent(
                    MetaData.ATT_TAXID, getCurrentMethodName()));
            return;
        }

        AttributeName attLevel = graph.getMetaData().getAttributeName(MetaData.ATT_LEVEL);
        if (attLevel == null) {
            attLevel = graph.getMetaData().getFactory()
                    .createAttributeName(MetaData.ATT_LEVEL, "Depth of element in hierarchy", Integer.class);
        }
        File dir = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_DIR));

        Extractor ex = new Extractor(graph, dir.getAbsolutePath(), getDeleted);

        // first create all concepts and keep track of them
        Hashtable<String, ONDEXConcept> map = new Hashtable<String, ONDEXConcept>();
        List<Entry> result = ex.getEntries();
        Iterator<Entry> it = result.iterator();
        while (it.hasNext()) {
            Entry entry = it.next();

            ConceptClass conceptCC = null;

            //determine the concept class
            switch (entry.getType()) {
                case EC:
                    conceptCC = cc_ec;
                    break;

                case DOMAIN:
                    conceptCC = cc_domain;
                    break;

                case PROTEIN:
                    conceptCC = cc_protein;
                    break;

                default:
                    throw new RuntimeException("Unknown Case " + entry.getType());
            }

            ONDEXConcept c = graph.getFactory().createConcept(entry.getId(), entry.getDescription(), dataSource_ec, conceptCC, et);

            if (entry.getTaxid() != null) {
                c.createAttribute(taxId, entry.getTaxid().trim(), false);
            }

            Iterator<String> it_acc = entry.getAccessions().keySet().iterator();
            while (it_acc.hasNext()) {
                String ds = it_acc.next();
                String[] accessions = entry.getAccessions().get(ds);

                if (ds.equalsIgnoreCase(dataSource_ec.getId())) { //ec metadata is pre cached
                    for (String accession : accessions) {
                        c.createConceptAccession(accession, dataSource_ec, false);
                    }
                } else {
                    DataSource accDataSource = graph.getMetaData().getDataSource(ds);
                    if (accDataSource == null) {
                        throw new RuntimeException("DataSource unknown : " + ds);
                    }
                    for (String accession : accessions) {
                        c.createConceptAccession(accession, accDataSource, false);
                    }
                }

            }
            if (entry.getName().length() > 0)
                c.createConceptName(entry.getName(), true);
            Iterator<String> it_cn = entry.getSynonyms().iterator();
            while (it_cn.hasNext()) {
                String cn = it_cn.next();
                c.createConceptName(cn, false);
            }
            map.put(entry.getId(), c);

        }

        // now add EC relations
        result = ex.getEntries();
        it = result.iterator();
        while (it.hasNext()) {
            Entry entry = it.next();

            if (entry.getType() == Entry.Type.EC) {
                String id = entry.getId();
                ONDEXConcept c = map.get(id);
                String[] classes = id.split("\\.");
                c.createAttribute(
                        attLevel,
                        Integer.valueOf(classes.length),
                        false);

                ONDEXConcept first = map.get(classes[0] + ".-.-.-");
                c.addTag(first);
                ONDEXConcept second = null;
                if (!classes[1].equals("-")) {
                    second = map.get(classes[0] + "." + classes[1] + ".-.-");
                    if (second != null)
                        c.addTag(second);
                }
                Iterator<Relation> it_rel = entry.getRelations().iterator();
                while (it_rel.hasNext()) {
                    Relation rel = it_rel.next();
                    ONDEXConcept from = map.get(rel.getFrom());
                    ONDEXConcept to = map.get(rel.getTo());

                    if (rel.getRelationType().equals(rt_is_a.getId())) { //pre set action for is relations
                        ONDEXRelation r = graph.getFactory().createRelation(from, to, rt_is_a, et);
                        r.addTag(first);
                        if (second != null)
                            r.addTag(second);
                    } else if (rel.getRelationType().equals(MetaData.RT_CATALYSEING_CLASS)) { //pre set action for cat_class
                        graph.getFactory().createRelation(from, to, cat_c, et);
                    } else {
                        throw new RuntimeException("Unknown Relation Type on EC " + rel.getRelationType());
                    }
                }
            } else {
                for (Relation relation : entry.getRelations()) {
                    ONDEXConcept from = map.get(relation.getFrom());
                    ONDEXConcept to = map.get(relation.getTo());

                    if (relation.getRelationType().equals(MetaData.RT_CATALYSEING_CLASS)) {
                        graph.getFactory().createRelation(from, to, cat_c, et);
                    } else {
                        throw new RuntimeException("Unknown Relation Type on EC " + relation.getRelationType());
                    }

                }
            }
        }

        goe = new GeneralOutputEvent("EC parsing finished!", getCurrentMethodName());
        goe.setLog4jLevel(Level.INFO);
        fireEventOccurred(goe);
    }

    // used for event propagation
    private static Parser instance;

    /**
     * Propagates an event for the current instance of this parser.
     *
     * @param et EventType
     */
    public static void propagateEventOccurred(EventType et) {
        if (instance != null)
            instance.fireEventOccurred(et);
    }

    @Override
    public String[] requiresValidators() {
        return new String[]{"taxonomy"};
    }

}