package net.sourceforge.ondex.validator.htmlaccessionlink;


/**
 * Condition acts as a multikey for a hashmap
 *
 * @author hindlem
 */
public class Condition {

    public Condition(String cv) {
        this(cv, null);
    }

    public Condition(String cv, String cc) {
        this.cv = cv;
        this.cc = cc;
    }

    private String cv = "";
    private String cc = "";

    public boolean equals(Object o) {
        if (o instanceof Condition) {

            System.out.println(((Condition) o).getCv());

            Condition oc = (Condition) o;
            if (cv != null && oc.getCv() != null && !cv.equals(oc.getCv())) {
                return false;
            } else if (cv != null || oc.getCv() != null) {
                return false;
            }

            if (cc != null && oc.getConceptClass() != null && !cc.equals(oc.getConceptClass())) {
                return false;
            } else if (cc != null || oc.getConceptClass() != null) {
                return false;
            }

            return true;
        }
        return false;
    }

    public String getCv() {
        return cv;
    }

    public String toString() {
        return cv + ':' + cc;
    }

    public String getConceptClass() {
        return cc;
    }

    public int hashcode() {
        return toString().hashCode();
    }

}
