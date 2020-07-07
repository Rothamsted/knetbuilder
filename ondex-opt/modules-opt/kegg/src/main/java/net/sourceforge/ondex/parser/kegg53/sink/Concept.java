/*
 * Created on 25-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg53.sink;

import java.util.HashSet;
import java.util.Set;

/**
 * @author taubertj
 */ 
public class Concept implements Comparable<Concept> {

    private String id; //required
    private String description;
    private String element_of;
    private float graphical_x;
    private float graphical_y;
    private String taxid;
    private String of_type_fk;
    private String url;

    private boolean isSelfContext = false;

    private Set<String> keggConceptIdContext = new HashSet<String>();

    private Set<ConceptName> conceptNames = new HashSet<ConceptName>(4);
    private Set<ConceptAcc> conceptAccs = new HashSet<ConceptAcc>(4);

    /**
     * @param id
     * @param element_of
     * @param of_type_fk
     */
    public Concept(String id, String element_of, String of_type_fk) {
        if (id == null || id.length() == 0) throw new NullPointerException("ID is null");
        if (element_of == null || element_of.length() == 0) throw new NullPointerException("ElementOf is null");
        if (of_type_fk == null || of_type_fk.length() == 0) throw new NullPointerException("OfTypeFK is null");
        this.id = id.toUpperCase();
        this.element_of = element_of.intern();
        this.of_type_fk = of_type_fk.intern();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Concept) return this.id.equals(((Concept) o).getId());
        return false;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    public int compareTo(Concept c) {
        return this.id.compareTo(c.getId());
    }

    /**
     * Clones a KEGG internal Concept for a new id, element_of and of_type_fk.
     *
     * @param new_id         - String
     * @param new_element_of - String
     * @param new_of_type_fk - String
     * @return Concept
     */
    public Concept clone(String new_id, String new_element_of, String new_of_type_fk) {
        Concept newConcept = new Concept(new_id, new_element_of, new_of_type_fk);
        if (this.description != null)
            newConcept.setDescription(this.getDescription());
        newConcept.setGraphical_x(this.getGraphical_x());
        newConcept.setGraphical_y(this.getGraphical_y());
        if (this.getTaxid() != null)
            newConcept.setTaxid(this.getTaxid());
        if (this.url != null)
            newConcept.setUrl(this.getUrl());
        if (this.getConceptAccs().size() > 0) {
            for (ConceptAcc ca : conceptAccs) {
                ConceptAcc newCA = new ConceptAcc(
                        new_id,
                        ca.getConcept_accession(),
                        ca.getElement_of());
                newCA.setAmbiguous(ca.isAmbiguous());
                newConcept.getConceptAccs().add(newCA);
            }
        }
        if (this.getConceptNames().size() > 0) {
            for (ConceptName cn : getConceptNames()) {
                ConceptName newCN = new ConceptName(
                        new_id,
                        cn.getName());
                newCN.setName(cn.getName());
                newCN.setPreferred(cn.isPreferred());
                newConcept.getConceptNames().add(newCN);
            }
        }
        return newConcept;
    }

    @Override
    public String toString() {
        String s = "Concept\n"
                + "Id: " + id + "\n"
                + "Of_type_fk: " + of_type_fk + "\n"
                + "Element_of: " + element_of;
        return s;
    }

    /**
     * Returns description.
     *
     * @return String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets description.
     *
     * @param description - String
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns element_of.
     *
     * @return String
     */
    public String getElement_of() {
        return element_of;
    }

    /**
     * Sets element_of.
     *
     * @param element_of - String
     */
    public void setElement_of(String element_of) {
        if (element_of == null || element_of.length() == 0)
            throw new NullPointerException("ElementOf is null or empty");
        this.element_of = element_of.intern();
    }

    /**
     * Returns graphical_x.
     *
     * @return float
     */
    public float getGraphical_x() {
        return graphical_x;
    }

    /**
     * Sets graphical_x.
     *
     * @param graphical_x - float
     */
    public void setGraphical_x(float graphical_x) {
        this.graphical_x = graphical_x;
    }

    /**
     * Returns graphical_y.
     *
     * @return float
     */
    public float getGraphical_y() {
        return graphical_y;
    }

    /**
     * Set graphical_y.
     *
     * @param graphical_y - float
     */
    public void setGraphical_y(float graphical_y) {
        this.graphical_y = graphical_y;
    }

    /**
     * Returns Concept ID.
     *
     * @return String
     */
    public String getId() {
        return id;
    }

    /**
     * Modifies the Concept ID.
     *
     * @param id - String
     */
    public void setId(String id) {
        if (id == null) throw new NullPointerException("ID is null");
        this.id = id.toUpperCase();
    }

    /**
     * Returns of_type_fk.
     *
     * @return String
     */
    public String getOf_type_fk() {
        return of_type_fk;
    }

    /**
     * Sets of_type_fk.
     *
     * @param of_type_fk - String
     */
    public void setOf_type_fk(String of_type_fk) {
        if (of_type_fk == null || of_type_fk.length() == 0) throw new NullPointerException("of_type_fk is null");
        this.of_type_fk = of_type_fk.intern();
    }

    /**
     * Returns taxid.
     *
     * @return String
     */
    public String getTaxid() {
        return taxid;
    }

    /**
     * Sets taxid.
     *
     * @param taxid - String
     */
    public void setTaxid(String taxid) {
        this.taxid = taxid.intern();
    }

    /**
     * Returns URL.
     *
     * @return String
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets URL.
     *
     * @param url - String
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns set of Concept Accession.
     *
     * @return HashSet<ConceptAcc>
     */
    public Set<ConceptAcc> getConceptAccs() {
        return conceptAccs;
    }

    /**
     * Returns set of Concept Names.
     *
     * @return HashSet<ConceptName>
     */
    public Set<ConceptName> getConceptNames() {
        return conceptNames;
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

    /**
     * @return the isSelfContext
     */
    public boolean isSelfContext() {
        return isSelfContext;
    }

    /**
     * @param isSelfContext the isSelfContext to set
     */
    public void setSelfContext(boolean isSelfContext) {
        this.isSelfContext = isSelfContext;
    }
}
