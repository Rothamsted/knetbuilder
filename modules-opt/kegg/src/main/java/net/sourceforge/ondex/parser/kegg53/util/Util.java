/*
 * Created on 12-May-2005
 *
 */
package net.sourceforge.ondex.parser.kegg53.util;

import com.sleepycat.persist.EntityCursor;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.parser.kegg53.GenomeParser;
import net.sourceforge.ondex.parser.kegg53.sink.*;

import java.util.*;

/**
 * @author taubertj
 */ 
public class Util {

    private final ConceptWriter cw;
    private final RelationWriter rw;
    private final SequenceWriter sw;

    private final Set<String> element_of;
    private final GenomeParser genomeParser;


    public Util(ONDEXGraph og, GenomeParser genomeParser) {
        this.genomeParser = genomeParser;
        element_of = Collections.synchronizedSet(new HashSet<String>());
        cw = new ConceptWriter(og);
        rw = new RelationWriter(og, cw);
        sw = new SequenceWriter(og, cw);
    }

    /**
     * @param concept         kegg concept to write
     * @param speciesSpecific contains a species in the id
     * @throws MetaDataMissingException
     */
    public void writeConcept(Concept concept, boolean speciesSpecific) throws MetaDataMissingException {
        cw.createConcept(concept, genomeParser, speciesSpecific);
    }

    /**
     * @param concept kegg concept to write
     * @throws MetaDataMissingException
     */
    public void writeConcept(Concept concept) throws MetaDataMissingException {
        cw.createConcept(concept, genomeParser, true);
    }

    public Set<String> getClashedElementOf() {
        return element_of;
    }

    /**
     * @param conceptsToWrite kegg concepts to write
     * @param speciesSpecific contains a species in the id
     * @throws MetaDataMissingException
     */
    public void writeConcepts(Collection<Concept> conceptsToWrite, boolean speciesSpecific) throws MetaDataMissingException {
        for (Concept concept : conceptsToWrite) {
            writeConcept(concept, speciesSpecific);
        }
    }

    /**
     * @param conceptsToWrite kegg concepts to write
     * @throws MetaDataMissingException
     */
    public void writeConcepts(Collection<Concept> conceptsToWrite) throws MetaDataMissingException {
        writeConcepts(conceptsToWrite, true);
    }


    /**
     * @param seqs
     * @return jobID
     */
    public void writeSequences(DPLPersistantSet<Sequence> seqs) throws MetaDataMissingException {
        EntityCursor<Sequence> cursor = seqs.getCursor();
        try {
            for (Sequence seq : cursor) {
                try {
                    sw.createSequence(seq);
                } catch (ConceptWriter.ConceptNotExistingException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        } finally {
            seqs.closeCursor(cursor);
        }
    }

    /**
     * @param relationsToWrite
     */
    public void writeRelations(DPLPersistantSet<Relation> relationsToWrite) throws MetaDataMissingException {
        EntityCursor<Relation> cursor = relationsToWrite.getCursor();
        try {
            for (Relation relation : cursor) {
                writeRelation(relation);
            }
        } finally {
            relationsToWrite.closeCursor(cursor);
        }
    }


    /**
     * @param relationsToWrite
     */
    public void writeRelations(ArrayList<Relation> relationsToWrite) throws MetaDataMissingException {
        for (Relation relation : relationsToWrite) {
            writeRelation(relation);
        }
    }

    public void writeRelation(Relation relation) throws MetaDataMissingException {
        try {
            rw.createRelation(relation);
        } catch (ConceptWriter.ConceptNotExistingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public ConceptWriter getCw() {
        return cw;
    }

    public RelationWriter getRw() {
        return rw;
    }

    public SequenceWriter getSw() {
        return sw;
    }

}
