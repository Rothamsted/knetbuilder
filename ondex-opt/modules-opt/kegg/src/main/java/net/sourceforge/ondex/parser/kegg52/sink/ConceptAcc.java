/*
 * Created on 26-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg52.sink;

/**
 * @author taubertj
 */
public class ConceptAcc {

    private String id; //required
    private String concept_accession; //required
    private String element_of; //required
    private boolean ambiguous = false;
    private String s = "";

    //CONSTRAINT concept_acc_pkey PRIMARY KEY (id, concept_accession, element_of),

    public ConceptAcc(String id, String concept_accession, String element_of) {
        if (id == null) throw new NullPointerException("ID is null");
        if (concept_accession == null) throw new NullPointerException("concept_accession is null");
        if (element_of == null) throw new NullPointerException("element_of is null");
        this.id = id;
        this.concept_accession = concept_accession;
        this.element_of = element_of.intern();
        this.s = this.id + this.concept_accession + this.element_of;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ConceptAcc) {
            ConceptAcc acc = (ConceptAcc) o;
            return this.id.equals(acc.getId()) &&
                    this.concept_accession.equals(acc.getConcept_accession()) &&
                    this.element_of.equals(acc.getElement_of());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return s.hashCode();
    }

    @Override
    public String toString() {
        String s = "ConceptAcc\n"
                + "Id: " + id + "\n"
                + "Concept_accession: " + concept_accession + "\n"
                + "Element_of: " + element_of;
        return s;
    }

    public boolean isAmbiguous() {
        return ambiguous;
    }

    public void setAmbiguous(boolean ambiguous) {
        this.ambiguous = ambiguous;
    }

    public String getConcept_accession() {
        return concept_accession.replace("_", ".");
    }

    public String getElement_of() {
        return element_of;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null) throw new NullPointerException("ID is null");
        this.id = id;
    }

    public void setElement_of(String element_of) {
        if (element_of == null) throw new NullPointerException("element_of is null");
        this.element_of = element_of;
    }
}
