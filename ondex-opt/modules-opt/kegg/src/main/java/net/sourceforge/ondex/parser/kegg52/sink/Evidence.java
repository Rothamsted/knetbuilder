/*
 * Created on 13-May-2005
 *
 */
package net.sourceforge.ondex.parser.kegg52.sink;

/**
 * @author taubertj
 */
public class Evidence {

    private String mapping_method_fk;
    private String from_concept_fk;
    private String to_concept_fk;
    private String of_type_fk;

    @Override
    public String toString() {
        String s = "Evidence\n"
                + "Mapping_method_fk: " + mapping_method_fk + "\n"
                + "From_concept_fk: " + from_concept_fk + "\n"
                + "To_concept_fk: " + to_concept_fk + "\n"
                + "Of_type_fk: " + of_type_fk;
        return s;
    }

    public String getFrom_concept_fk() {
        return from_concept_fk;
    }

    public void setFrom_concept_fk(String from_concept_fk) {
        this.from_concept_fk = from_concept_fk.intern();
    }

    public String getMapping_method_fk() {
        return mapping_method_fk;
    }

    public void setMapping_method_fk(String mapping_method_fk) {
        this.mapping_method_fk = mapping_method_fk.intern();
    }

    public String getOf_type_fk() {
        return of_type_fk;
    }

    public void setOf_type_fk(String of_type_fk) {
        this.of_type_fk = of_type_fk.intern();
    }

    public String getTo_concept_fk() {
        return to_concept_fk;
    }

    public void setTo_concept_fk(String to_concept_fk) {
        this.to_concept_fk = to_concept_fk.intern();
    }
}
