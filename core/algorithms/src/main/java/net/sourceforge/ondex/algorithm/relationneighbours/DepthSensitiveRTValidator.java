package net.sourceforge.ondex.algorithm.relationneighbours;

import net.sourceforge.ondex.core.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A validator for registering valid relation types that will be
 * valid only at a specified depth
 *
 * @author hindlem
 */
public class DepthSensitiveRTValidator implements LogicalRelationValidator {

    //when true assumes if restraint not specified at a depth then true, otherwise if false then presumptive exclusion
    private boolean isInclusivePresumption = true;

    private HashMap<Integer, ArrayList<DataSource>> validConceptDataSourceAtDepth;

    private HashMap<Integer, ArrayList<ConceptClass>> validConceptClassAtDepth;

    private HashMap<Integer, ArrayList<RelationType>> validRelationTypeAtDepth;

    /**
     * Adds a ConceptClass restriction to a depth if no restrictions are in
     * place all ConceptClasses are allowed. More than one ConceptClass may be
     * added
     *
     * @param depth
     * @param cc
     */
    public void addConceptClassConstraint(int depth, ConceptClass cc) {
        if (validConceptClassAtDepth == null) {
            validConceptClassAtDepth = new HashMap<Integer, ArrayList<ConceptClass>>();
        }
        ArrayList<ConceptClass> ccArray = validConceptClassAtDepth.get(depth);
        if (ccArray == null) {
            ccArray = new ArrayList<ConceptClass>(1);
            validConceptClassAtDepth.put(depth, ccArray);
        }
        ccArray.add(cc);
    }

    /**
     * Adds a ConceptClass restriction to a depth if no restrictions are in
     * place all DataSource are allowed. More than one ConceptClass may be added
     *
     * @param depth
     * @param dataSource
     */
    public void addConceptDataSourceConstraint(int depth, DataSource dataSource) {
        if (validConceptDataSourceAtDepth == null) {
            validConceptDataSourceAtDepth = new HashMap<Integer, ArrayList<DataSource>>();
        }
        ArrayList<DataSource> dataSourceArray = validConceptDataSourceAtDepth.get(depth);
        if (dataSourceArray == null) {
            dataSourceArray = new ArrayList<DataSource>(1);
            validConceptDataSourceAtDepth.put(depth, dataSourceArray);
        }
        dataSourceArray.add(dataSource);
    }

    /**
     * Adds a RelationType restriction to a depth if no restrictions are in
     * place all RelationType are allowed. More than one RelationType may be
     * added
     *
     * @param depth
     * @param rt
     */
    public void addRelationTypeConstraint(int depth, RelationType rt) {
        if (validRelationTypeAtDepth == null) {
            validRelationTypeAtDepth = new HashMap<Integer, ArrayList<RelationType>>();
        }
        ArrayList<RelationType> rtArray = validRelationTypeAtDepth.get(depth);
        if (rtArray == null) {
            rtArray = new ArrayList<RelationType>(1);
            validRelationTypeAtDepth.put(depth, rtArray);
        }
        rtArray.add(rt);
    }

    public boolean isValidConceptAtDepth(ONDEXConcept toConcept,
                                         int currentPosition) {
        if (validConceptDataSourceAtDepth != null) {
            ArrayList<DataSource> list = validConceptDataSourceAtDepth.get(currentPosition);
            if (list != null) {
                if (list.contains(toConcept.getElementOf())) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return isInclusivePresumption;
            }
        }
        if (validConceptClassAtDepth != null) {
            ArrayList<ConceptClass> list = validConceptClassAtDepth.get(currentPosition);
            if (list != null) {
                if (list.contains(toConcept.getOfType())) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return isInclusivePresumption;
            }
        }
        return true;
    }

    public boolean isValidRelationAtDepth(ONDEXRelation relation,
                                          int currentPosition, ONDEXConcept conceptAtHead) {

        ONDEXConcept toConcept = relation.getToConcept();
        if (toConcept.equals(conceptAtHead)) {
            toConcept = relation.getFromConcept();
        }

        if (!isValidConceptAtDepth(toConcept, currentPosition)) return false;

        if (validRelationTypeAtDepth != null) {
            ArrayList<RelationType> list = validRelationTypeAtDepth.get(currentPosition);
            if (list != null) {
                if (list.contains(relation.getOfType()))
                    return true;
            } else {
                return isInclusivePresumption;
            }
        }
        return false;
    }

    /**
     * when true assumes if restraint not specified at a depth then true, otherwise if false then presumptive exclusion
     *
     * @return
     */
    public boolean isInclusivePresumption() {
        return isInclusivePresumption;
    }

    /**
     * when true assumes if restraint not specified at a depth then true, otherwise if false then presumptive exclusion
     *
     * @param isInclusivePresumption
     */
    public void setInclusivePresumption(boolean isInclusivePresumption) {
        this.isInclusivePresumption = isInclusivePresumption;
    }

}
