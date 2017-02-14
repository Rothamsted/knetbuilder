/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sourceforge.ondex.tools;

import java.util.Collection;
import java.util.HashMap;
import net.sourceforge.ondex.core.*;

/**
 *
 * @author jweile
 */
public class MetaDataMatcher<M extends MetaData> extends HashMap<String,M> {

    /**
     * A MDType enum representation of the parameterisation type M.
     */
    private MDType type;

    /**
     * The metadata object from the concerned ONDEXGraph.
     */
    private ONDEXGraphMetaData md;

    /**
     * Constructor over metadata object from the current graph and the class object
     * corresponding to the parameterisation type M.
     * @param md Metadata object from the current graph.
     * @param type  class object corresponding to the parameterisation type M.
     * This hack is necessary because the identity of M is not accessible at runtime.
     */
    public MetaDataMatcher(ONDEXGraphMetaData md, Class<? extends MetaData> type) {
        this.md = md;
        this.type = MDType.determine(type);

        makeIndex();
    }

    /**
     * Creates an index over all names and identifiers of the the metadata of
     * type M
     */
    public void makeIndex() {

        Collection<? extends MetaData> set = null;

        switch (type) {
            case CC:
                set = md.getConceptClasses();
                break;
            case RT:
                set = md.getRelationTypes();
                break;
            case AN:
                set = md.getAttributeNames();
                break;
            case CV:
                set = md.getDataSources();
                break;
            case ET:
                set = md.getEvidenceTypes();
                break;
            case U:
                set = md.getUnits();
                break;
        }

        for (MetaData m : set) {

            put(m.getId(), (M)m);
            put(m.getFullname(), (M)m);
            
            if (type == MDType.RT) {
                put(((RelationType)m).getInverseName(), (M)m);
            }

        }
        
    }

    /**
     * Helper enum to represent different metadata types.
     */
    private enum MDType {
        CC, RT, AN, CV, ET, U;

        public static MDType determine(Class<?> clazz) {
            if (clazz.equals(ConceptClass.class)) {
                return CC;
            } else if (clazz.equals(RelationType.class)) {
                return RT;
            } else if (clazz.equals(AttributeName.class)) {
                return AN;
            } else if (clazz.equals(DataSource.class)) {
                return CV;
            } else if (clazz.equals(EvidenceType.class)) {
                return ET;
            } else if (clazz.equals(Unit.class)) {
                return U;
            } else {
                return null;
            }
        }
    }

}
