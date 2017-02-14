/*
 * Created on 12-May-2005
 *
 */
package net.sourceforge.ondex.parser.kegg52.util;

import com.sleepycat.persist.EntityCursor;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.kegg52.Parser;
import net.sourceforge.ondex.parser.kegg52.sink.*;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author taubertj
 */
public class Util {

    private ConceptWriter cw;
    private RelationWriter rw;
    private SequenceWriter sw;

    private SingleThreadQueue queue;
    private Set<String> element_of;

    public Util(ONDEXGraph og) {
        if (queue != null) queue.finalize();
        queue = new SingleThreadQueue(Util.class.getName());
        queue.setDEBUG(Parser.DEBUG);
        element_of = Collections.synchronizedSet(new HashSet<String>());
        cw = new ConceptWriter(og);
        rw = new RelationWriter(og, cw);
        sw = new SequenceWriter(og, cw);
    }

    public void cleanup() {
        element_of = null;
        queue.finalize();
        queue = null;
        cw.cleanup();
        cw = null;
        rw.cleanup();
        rw = null;
        sw.cleanup();
        sw = null;
    }

    public void writeConcept(Concept concept) {
        cw.createConcept(concept);
    }

    public Set<String> getClashedElementOf() {
        return element_of;
    }

    /**
     * @param conceptsToWrite
     * @return jobId
     */
    public void writeConcepts(Collection<Concept> conceptsToWrite) {
        Iterator<Concept> it = conceptsToWrite.iterator();
        while (it.hasNext()) {
            Concept concept = it.next();
            it.remove();
            writeConcept(concept);
        }
        conceptsToWrite.clear();
    }


    /**
     * @param seqs
     * @return jobID
     */
    public void writeSequences(DPLPersistantSet<Sequence> seqs) {
        EntityCursor<Sequence> cursor = seqs.getCursor();
        Iterator<Sequence> it = cursor.iterator();
        while (it.hasNext()) {
            Sequence seq = it.next();
            sw.createSequence(seq);
            it.remove();
        }
        seqs.closeCursor(cursor);
    }

    /**
     * @return jobID
     */
    public void writeRelations(DPLPersistantSet<Relation> relationsToWrite) {
        EntityCursor<Relation> cursor = relationsToWrite.getCursor();

        Iterator<Relation> it = cursor.iterator();

        while (it.hasNext()) {
            Relation relation = it.next();
            rw.createRelation(relation);
            it.remove();
        }
        relationsToWrite.closeCursor(cursor);
    }

    /**
     * @return jobID
     */
    public void writeRelations(ArrayList<Relation> relationsToWrite) {
        Iterator<Relation> it = relationsToWrite.iterator();
        while (it.hasNext()) {
            Relation relation = it.next();
            it.remove();
            rw.createRelation(relation);
        }
    }

    public void writeRelation(Relation relation_gene) {
        rw.createRelation(relation_gene);
    }

    private static Pattern colonPattern = Pattern.compile(":");

    public static void getTaxidForConcept(Concept concept) {
        String[] result = colonPattern.split(concept.getId());
        if (result.length == 2) {
            String org = result[0].toUpperCase();
            if (TaxidMapping.getMapping().containsKey(org)) {
                concept.setTaxid(TaxidMapping.getMapping().get(org));
            }
        }
    }

    public ConceptWriter getCw() {
        return cw;
    }

    public void setCw(ConceptWriter cw) {
        this.cw = cw;
    }

    public SingleThreadQueue getQueue() {
        return queue;
    }

    public void setQueue(SingleThreadQueue queue) {
        this.queue = queue;
    }

    public RelationWriter getRw() {
        return rw;
    }

    public void setRw(RelationWriter rw) {
        this.rw = rw;
    }

    public SequenceWriter getSw() {
        return sw;
    }

    public void setSw(SequenceWriter sw) {
		this.sw = sw;
	}

	
}
