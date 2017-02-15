package net.sourceforge.ondex.algorithm.pathmodel;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXRelation;

import java.util.List;


/**
 * Exposes the protected constructors of the abstract class SimplePath
 *
 * @author hindlem
 */
public class ONDEXEntityPath extends SimplePath {

    /**
     * @param entitiesInPositionOrder
     */
    public ONDEXEntityPath(List<ONDEXEntity> entitiesInPositionOrder) {
        super(entitiesInPositionOrder);
    }

    /**
     * @param c
     */
    public ONDEXEntityPath(ONDEXConcept c) {
        super(c);
    }

    /**
     * @param r
     */
    public ONDEXEntityPath(ONDEXRelation r) {
        super(r);
    }

    /**
     * @param r
     * @param c
     */
    public void addPathStep(ONDEXRelation r, ONDEXConcept c) {
        super.addPathStep(r, c);
    }

    /**
     * @param r
     */
    public void addPathStep(ONDEXRelation r) {
        super.addPathStep(r);
    }

    /**
     * @param c
     */
    public void addPathStep(ONDEXConcept c) {
        super.addPathStep(c);
    }

}
