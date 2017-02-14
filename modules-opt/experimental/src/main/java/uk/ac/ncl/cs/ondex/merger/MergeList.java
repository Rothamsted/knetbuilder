package uk.ac.ncl.cs.ondex.merger;

import java.util.ArrayList;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;

/**
 * An integer list that carries a boolean flag indicating whether
 * the contents of this list have been merged already.
 */
@SuppressWarnings(value = "serial")
public class MergeList extends ArrayList<ONDEXConcept> {

    private boolean merged = false;

    private ConceptClass type;

    public ConceptClass getType() {
        return type;
    }

    public void setType(ConceptClass type) {
        this.type = type;
    }

    

    public void markAsMerged() {
        merged = true;
    }

    public void join(MergeList otherlist) {
        for (ONDEXConcept element : otherlist) {
            if (!this.contains(element)) {
                this.add(element);
            }
        }
    }

    public boolean hasBeenMerged() {
        return merged;
    }
}
