package net.sourceforge.ondex.parser.pfam.transformer;

import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.parser.pfam.MetaData;
import net.sourceforge.ondex.parser.pfam.sink.DbLink;
import net.sourceforge.ondex.parser.pfam.sink.Family;

import java.util.HashMap;
import java.util.Map;

/**
 * transforms a Family object to a concept with all related information attached
 *
 * @author peschr
 */
public class FamilyTransformer {
    private ConceptClass ccProteinFamily;

    private EvidenceType etIMPD = null;

    private DataSource dataSourcePfam;

    private Map<String, DataSource> cvs = new HashMap<String, DataSource>();

    private ONDEXGraph graph;

    /**
     * @param graph the current AbstractONDEXGraph
     */
    public FamilyTransformer(ONDEXGraph graph) {
        this.graph = graph;

        etIMPD = graph.getMetaData().getEvidenceType("IMPD");
        ccProteinFamily = graph.getMetaData().getConceptClass(
                MetaData.CC_ProteinFamily);

        dataSourcePfam = graph.getMetaData().getDataSource(MetaData.CV_Pfam);

        cvs.put("PROSITE_PROFILE", graph.getMetaData().getDataSource(
                MetaData.CV_PROSITE));
        cvs.put("PROSITE", graph.getMetaData().getDataSource(
                MetaData.CV_PROSITE));
        cvs.put("SMART", graph.getMetaData().getDataSource(
                MetaData.CV_SMARTDB));
        cvs.put("PRINTS", graph.getMetaData().getDataSource(
                MetaData.CV_PRINTS));
        cvs.put("SCOP", graph.getMetaData().getDataSource(MetaData.CV_SCOP));
        cvs.put("INTERPRO", graph.getMetaData().getDataSource(
                MetaData.CV_InterPro));
        cvs.put("TC", graph.getMetaData().getDataSource(
                MetaData.CV_TC));
        cvs.put("PDB", graph.getMetaData().getDataSource(MetaData.CV_PDB));
        cvs.put("CAZY", graph.getMetaData().getDataSource(MetaData.CV_CAZY));
        cvs.put("HOMSTRAD", graph.getMetaData().getDataSource(MetaData.CV_HOMSTRAD));
        cvs.put("MEROPS", graph.getMetaData().getDataSource(MetaData.CV_MEROPS));


    }

    /**
     * transforms a family object to an concept
     *
     * @param family
     */
    public void transform(Family family) {
        ONDEXConcept concept = graph.getFactory().createConcept(family.getAccession(), family.getDescription(),
                dataSourcePfam, ccProteinFamily, etIMPD);
        concept.createConceptName(family.getId(), true);

        concept.createConceptAccession(family.getAccession(), dataSourcePfam, false);
        for (DbLink dbLink : family.getDblinks().values()) {

            if (dbLink.getDbName().equalsIgnoreCase("URL")) {
                concept.setAnnotation(concept.getAnnotation() + " URL:" + dbLink.getAccession());
                continue;
            }

            boolean ambiguous = false;

            if (dbLink.getDbName().equalsIgnoreCase("PDB")) {
                ambiguous = true;
            }

            DataSource dataSource = cvs.get(dbLink.getDbName().toUpperCase().trim());
            if (dataSource != null)
                concept.createConceptAccession(dbLink.getAccession(), dataSource, ambiguous);
            else
                System.out.println("Unknown database " + dbLink.getDbName() + " :" + dbLink.getAccession());
        }
        concept.addTag(concept);
	}
}
