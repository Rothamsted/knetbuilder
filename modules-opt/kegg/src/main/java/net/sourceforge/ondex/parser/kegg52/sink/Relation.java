/*
 * Created on 13-May-2005
 *
 */
package net.sourceforge.ondex.parser.kegg52.sink;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import java.util.HashSet;
import java.util.Set;

/**
 * @author taubertj
 */
@Entity
public class Relation {

    private static final long serialVersionUID = 4861767076901200748L;

    private String from_concept; // required

    private String from_element_of;

    private String of_type; // required

    @PrimaryKey
    public String pk;

    private String to_concept; // required

    private String to_element_of;

    private Set<String> keggConceptIdContext = new HashSet<String>();

    @SuppressWarnings("unused")
    private Relation() {
    }

    // CONSTRAINT relation_pkey PRIMARY KEY (from_concept, to_concept, of_type)

    public Relation(String from_concept, String to_concept, String of_type) {
        if (from_concept == null)
            throw new NullPointerException("from_concept is null");
        if (to_concept == null)
            throw new NullPointerException("to_concept is null");
        if (of_type == null)
            throw new NullPointerException("of_type is null");

        this.from_concept = from_concept.toUpperCase();
        this.to_concept = to_concept.toUpperCase();
        this.of_type = of_type.intern();
        this.pk = this.to_concept + this.from_concept + this.of_type;
    }

    public int compareTo(Relation r) {
        return this.pk.compareTo(r.pk);
    }

    public String getFrom_concept() {
        return from_concept;
    }

    public String getFrom_element_of() {
        return from_element_of;
    }

    public String getID() {
        return from_concept + "," + to_concept + "," + of_type;
    }

    public String getOf_type() {
        return of_type;
    }
   
    public String getTo_concept() {
        return to_concept;
    }

    public String getTo_element_of() {
        return to_element_of;
    }

    public void setFrom_element_of(String from_element_of) {
        this.from_element_of = from_element_of.intern();
    }

    public void setTo_element_of(String to_element_of) {
        this.to_element_of = to_element_of.intern();
    }

    public Set<String> getContext() {
        return keggConceptIdContext;
    }

    /**
     * @param ondexConceptId
     */
    public void addContext(String keggConceptId) {
        keggConceptIdContext.add(keggConceptId);
	}
}
