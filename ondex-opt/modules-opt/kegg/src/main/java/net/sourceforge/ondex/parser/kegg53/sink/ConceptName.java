/*
 * Created on 26-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg53.sink;

/**
 * @author taubertj
 */ 
public class ConceptName {

    private String id; //required
    private String name; //required
    private boolean preferred = false; //optional

    // for internal use
    private String s = "";

    /**
     * Create a new ConceptName for KEGG internal use.
     *
     * @param id   - ID of KEGG Concept it belongs to
     * @param name - actual name
     */
    public ConceptName(String id, String name) {
        if (id == null)
            throw new NullPointerException("ID is null");
        if (name == null)
            throw new NullPointerException("Name is null");

        this.id = id;
        this.name = name;
        this.s = this.id + this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ConceptName) {
            ConceptName cn = (ConceptName) o;
            return this.id.equals(cn.getId()) &&
                    this.name.equals(cn.getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return s.hashCode();
    }

    @Override
    public String toString() {
        String s = "ConceptName\n"
                + "Id: " + id + "\n"
                + "Name: " + name;
        return s;
    }

    /**
     * Returns ID of KEGG Concept.
     *
     * @return String
     */
    public String getId() {
        return id;
    }

    /**
     * Returns actual name.
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Sets stored name.
     *
     * @param name - String
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns whether or not this name is preferred.
     *
     * @return boolean
     */
    public boolean isPreferred() {
        return preferred;
    }

    /**
     * Sets the preferred flag for this name.
     *
     * @param preferred - boolean
     */
    public void setPreferred(boolean preferred) {
        this.preferred = preferred;
    }
}
