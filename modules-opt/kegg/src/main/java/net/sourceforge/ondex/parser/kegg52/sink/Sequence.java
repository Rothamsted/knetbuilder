/*
 * Created on 12-May-2005
 *
 */
package net.sourceforge.ondex.parser.kegg52.sink;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * @author taubertj
 */
@Entity
public class Sequence {

    @PrimaryKey
    private String id; //required
    private String concept_fk; //required
    private String sequence_type_fk; //required
    private String seq;

    //CONSTRAINT seq_pkey PRIMARY KEY (id)

    public Sequence(String id) {
        this.id = id;
    }

    @SuppressWarnings("unused")
    //used by berkely store
    private Sequence() {
    }

    @Override
    public String toString() {
        return "Sequence\n"
                + "Id: " + id + "\n"
                + "Concept_fk: " + concept_fk + "\n"
                + "Sequence_type_fk: " + sequence_type_fk;
    }

    public String getConcept_fk() {
        return concept_fk;
    }

    public void setConcept_fk(String concept_fk) {
        this.concept_fk = concept_fk.intern();
    }

    public String getId() {
        return id;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public String getSequence_type_fk() {
        return sequence_type_fk;
    }

    public void setSequence_type_fk(String sequence_type_fk) {
        this.sequence_type_fk = sequence_type_fk.intern();
    }
}
