/*
 * Created on 26-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg53.sink;

import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.exception.type.RelationTypeMissingException;
import net.sourceforge.ondex.parser.kegg53.MetaData;

import java.util.HashMap;

/**
 * Turns a KEGG Relation into a binary or ternary ONDEXRelation.
 *
 * @author taubertj
 */ 
public class RelationWriter {

    private ONDEXGraph og;

    private ONDEXGraphMetaData omd;

    private ConceptWriter cw;

    /**
     * Constructor for current ONDEXGraph and ConceptWriter to track written
     * concepts.
     *
     * @param og current ONDEXGraph
     * @param cw ConceptWritter with history of concepts
     */
    public RelationWriter(ONDEXGraph og, ConceptWriter cw) {
        this.og = og;
        this.cw = cw;
        this.omd = og.getMetaData();
    }

    // standard relation evidence type IMPD
    private EvidenceType eviType;

    // cache for relation types
    private HashMap<String, RelationType> rts = new HashMap<String, RelationType>(
            50);

    /**
     * Create a ONDEXRelation on the ONDEXGraph from the temporary Relation
     *
     * @param relation KEGG Relation
     */
    public void createRelation(Relation relation) throws MetaDataMissingException, ConceptWriter.ConceptNotExistingException {

        // deal with from concept from ONDEXGraph
        int fromId = cw.getWrittenConceptId(relation.getFrom_concept().trim());

        ONDEXConcept fromConcept = og.getConcept(fromId);

        // deal with to concept from ONDEXGraph
        int toId = cw.getWrittenConceptId(relation.getTo_concept().trim());

        ONDEXConcept toConcept = og.getConcept(toId);

        // get standard evidence type
        if (eviType == null)
            eviType = omd.getEvidenceType(MetaData.EVIDENCE_IMPD);

        // get cached relation type
        RelationType rt = rts.get(relation.getOf_type());
        if (rt == null) {
            rt = omd.getRelationType(relation.getOf_type());
            if (rt != null) {
                rts.put(relation.getOf_type(), rt);
            } else {
                throw new RelationTypeMissingException(relation.getOf_type());
            }
        }

        ONDEXRelation r = null;

        // at least from, to, rt and eviType has to be present
        if (fromConcept != null && toConcept != null && rt != null
                && eviType != null) {

            // binary relation
            if (og.getRelation(fromConcept, toConcept, rt) == null) {

                r = og.getFactory().createRelation(fromConcept, toConcept, rt,
                        eviType);
                if (r == null)
                    System.err.println("Unable to create relation "
                            + fromConcept.getId() + " " + toConcept.getId());
            }

        } else {
            throw new RuntimeException(
                    "Missing one or more values for Relation writing in Kegg: "
                            + relation.getOf_type());
        }

        if (r != null) {
            // add relation context
            for (String context : relation.getContext()) {
                Integer existingId = cw.getWrittenConceptId(context);
                if (existingId != null) {
                    // when adding context, make sure context is on all concepts
                    r.addTag(og.getConcept(existingId));
                    r.getFromConcept().addTag(og.getConcept(existingId));
                    r.getToConcept().addTag(og.getConcept(existingId));
                } else
                    System.err.println("|" + context.toUpperCase()
                            + "|Context not found");
            }
        }
    }
}
