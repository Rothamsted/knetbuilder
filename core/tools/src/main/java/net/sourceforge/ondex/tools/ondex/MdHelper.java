package net.sourceforge.ondex.tools.ondex;
//net.sourceforge.ondex.xten.functions.ControledVocabularyHelper

import net.sourceforge.ondex.core.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class to simplify checking for existing and creation of new controlled vocabulary entries
 * Use when the desired behaviour is to get existing entry if present and create it if it is
 * missing; e.g. the retrieval process must always succseed.
 *
 * @author lysenkoa
 */
public class MdHelper {

    /**
     * Return all of the concept classes of the concepts
     * contained in the collection
     *
     * @param cs
     * @return set of concept classes
     */
    public static Set<ConceptClass> getConceptClasses(Collection<ONDEXConcept> cs) {
        Set<ConceptClass> result = new HashSet<ConceptClass>();
        for (ONDEXConcept c : cs) {
            result.add(c.getOfType());
        }
        return result;
    }

    /**
     * Return all of the relation types of the relations
     * contained in the collection
     *
     * @param cs
     * @return set of relation types
     */
    public static Set<RelationType> getRelationTypes(Collection<ONDEXRelation> cs) {
        Set<RelationType> result = new HashSet<RelationType>();
        for (ONDEXRelation c : cs) {
            result.add(c.getOfType());
        }
        return result;
    }

    /**
     * Return all of the DataSources of the concepts
     * contained in the collection
     *
     * @param cs
     * @return set of DataSources
     */
    public static Set<DataSource> getConceptDataSources(Collection<ONDEXConcept> cs) {
        Set<DataSource> result = new HashSet<DataSource>();
        for (ONDEXConcept c : cs) {
            result.add(c.getElementOf());
        }
        return result;
    }

    /**
     * Return all of the evidence types of the entities
     * contained in the collection
     *
     * @param cs
     * @return set of evidence types
     */
    public static Set<EvidenceType> getConceptEvidence(Collection<ONDEXEntity> cs) {
        Set<EvidenceType> result = new HashSet<EvidenceType>();
        for (ONDEXEntity c : cs) {
            result.addAll(c.getEvidence());
        }
        return result;
    }

    /**
     * Return all of attribute names of the concepts
     * contained in the collection
     *
     * @param cs
     * @return set of attribute names
     */
    public static Set<AttributeName> getConceptAttNames(Collection<ONDEXConcept> cs) {
        Set<AttributeName> result = new HashSet<AttributeName>();
        for (ONDEXConcept c : cs) {
            for (Attribute g : c.getAttributes()) {
                result.add(g.getOfType());
            }
        }
        return result;
    }

    /**
     * Return all of attribute names of the relations
     * contained in the collection
     *
     * @param cs
     * @return set of attribute names
     */
    public static Set<AttributeName> getRelationAttNames(Collection<ONDEXRelation> cs) {
        Set<AttributeName> result = new HashSet<AttributeName>();
        for (ONDEXRelation c : cs) {
            for (Attribute g : c.getAttributes()) {
                result.add(g.getOfType());
            }
        }
        return result;
    }

    /**
     * Get evidence type
     *
     * @param graph
     * @param type
     * @return
     */
    public static EvidenceType createEvidence(final ONDEXGraph graph, final String type) {
        final ONDEXGraphMetaData meta = graph.getMetaData();
        EvidenceType evidence = meta.getEvidenceType(type);
        if (evidence == null) evidence = meta.getFactory().createEvidenceType(type);
        return evidence;
    }

    /**
     * Get concpet class
     *
     * @param graph
     * @param type
     * @return
     */
    public static ConceptClass createCC(final ONDEXGraph graph, final String type) {
        final ONDEXGraphMetaData meta = graph.getMetaData();
        ConceptClass cc = meta.getConceptClass(type);
        if (cc == null) cc = meta.getFactory().createConceptClass(type);
        return cc;
    }

    /**
     * Get cv
     *
     * @param graph
     * @param type
     * @return
     */
    public static DataSource createDataSource(final ONDEXGraph graph, final String type) {
        ONDEXGraphMetaData meta = graph.getMetaData();
        DataSource dataSource = meta.getDataSource(type);
        if (dataSource == null) dataSource = meta.getFactory().createDataSource(type);
        return dataSource;
    }

    /**
     * Get relation type
     *
     * @param graph
     * @param type
     * @return
     */
    public static RelationType createRT(final ONDEXGraph graph, final String type) {
        ONDEXGraphMetaData meta = graph.getMetaData();
        RelationType rt = meta.getRelationType(type);
        if (rt == null) rt = meta.getFactory().createRelationType(type);
        return rt;
    }

    /**
     * Get Attribute name
     *
     * @param graph
     * @param type
     * @param cls
     * @return
     */
    public static AttributeName createAttName(final ONDEXGraph graph, final String type, final Class<?> cls) {
        ONDEXGraphMetaData meta = graph.getMetaData();
        AttributeName att = meta.getAttributeName(type);
        if (att == null) att = meta.getFactory().createAttributeName(type, cls);
        return att;
    }

    /**
     * Return an array of relation types that correspond to the ids in the metadata.
     *
     * @param graph  - ondex graph
     * @param strRts - collection of ids
     * @return
     */
    public static RelationType[] convertRelationTypes(final ONDEXGraph graph, final Collection<String> strRts) {
        return convertRelationTypes(graph, strRts.toArray(new String[strRts.size()]));
    }

    /**
     * Return an array of relation types that correspond to the ids in the metadata.
     *
     * @param graph  - ondex graph
     * @param strRts - array of ids
     * @return
     */
    public static RelationType[] convertRelationTypes(final ONDEXGraph graph, final String[] strRts) {
        RelationType[] result = new RelationType[strRts.length];
        for (int i = 0; i < strRts.length; i++) {
            result[i] = createRT(graph, strRts[i]);
        }
        return result;
    }

    /**
     * Return an array of Concept Classes that correspond to the ids in the metadata.
     *
     * @param graph  - ondex graph
     * @param strIds - collection of ids
     * @return
     */
    public static ConceptClass[] convertConceptClasses(final ONDEXGraph graph, final Collection<String> strIds) {
        return convertConceptClasses(graph, strIds.toArray(new String[strIds.size()]));
    }

    /**
     * Return an array of ConceptClasses that correspond to the ids in the metadata.
     *
     * @param graph  - ondex graph
     * @param strIds - array of ids
     * @return
     */
    public static ConceptClass[] convertConceptClasses(final ONDEXGraph graph, final String[] strIds) {
        ConceptClass[] result = new ConceptClass[strIds.length];
        for (int i = 0; i < strIds.length; i++) {
            result[i] = createCC(graph, strIds[i]);
        }
        return result;
    }

    /**
     * Return an array of DataSources that correspond to the ids in the metadata.
     *
     * @param graph  - ondex graph
     * @param strIds - collection of ids
     * @return
     */
    public static DataSource[] convertDataSources(final ONDEXGraph graph, final Collection<String> strIds) {
        return convertDataSources(graph, strIds.toArray(new String[strIds.size()]));
    }

    /**
     * Return an array of DataSources that correspond to the ids in the metadata.
     *
     * @param graph  - ondex graph
     * @param strIds - array of ids
     * @return
     */
    public static DataSource[] convertDataSources(final ONDEXGraph graph, final String[] strIds) {
        final DataSource[] result = new DataSource[strIds.length];
        for (int i = 0; i < strIds.length; i++) {
            result[i] = createDataSource(graph, strIds[i]);
        }
        return result;
    }

    /**
     * Return an array of EvidenceTypes that correspond to the ids in the metadata.
     *
     * @param graph  - ondex graph
     * @param strIds - collection of ids
     * @return
     */
    public static EvidenceType[] convertEvidenceTypes(ONDEXGraph graph, Collection<String> strIds) {
        return convertEvidenceTypes(graph, strIds.toArray(new String[strIds.size()]));
    }

    /**
     * Return an array of EvidenceTypes that correspond to the ids in the metadata.
     *
     * @param graph  - ondex graph
     * @param strIds - array of ids
     * @return
     */
    public static EvidenceType[] convertEvidenceTypes(final ONDEXGraph graph, final String[] strIds) {
        final EvidenceType[] result = new EvidenceType[strIds.length];
        for (int i = 0; i < strIds.length; i++) {
            result[i] = createEvidence(graph, strIds[i]);
        }
        return result;
    }

    /**
     * Return an array of AttributeNames that correspond to the ids in the metadata.
     *
     * @param graph  - ondex graph
     * @param strIds - collection of ids
     * @return
     */
    public static AttributeName[] convertAttributeNames(final ONDEXGraph graph, final Collection<String> strIds, final Collection<Class> classes) {
        if (strIds.size() != classes.size())
            throw new RuntimeException("convertAttributeNames - arument error. The number of classes supplied is not the same as the nubmer of ids.");
        return convertAttributeNames(graph, strIds.toArray(new String[strIds.size()]), classes.toArray(new Class[classes.size()]));
    }

    /**
     * Return an array of EvidenceTypes that correspond to the ids in the metadata.
     *
     * @param graph  - ondex graph
     * @param strIds - array of ids
     * @return
     */
    public static AttributeName[] convertAttributeNames(final ONDEXGraph graph, final String[] strIds, final Class[] classes) {
        if (strIds.length != classes.length)
            throw new RuntimeException("convertAttributeNames - arument error. The number of classes supplied is not the same as the nubmer of ids.");
        final AttributeName[] result = new AttributeName[strIds.length];
        for (int i = 0; i < strIds.length; i++) {
            result[i] = createAttName(graph, strIds[i], classes[i]);
        }
        return result;
    }
    
    /**
     * Return an array of EvidenceTypes that correspond to the ids in the metadata.
     *
     * @param graph  - ondex graph
     * @param strIds - array of ids
     * @return
     */
    public static AttributeName[] convertAttributeNames(final ONDEXGraph graph, final String[] strIds, final Class cls) {
        final AttributeName[] result = new AttributeName[strIds.length];
        for (int i = 0; i < strIds.length; i++) {
            result[i] = createAttName(graph, strIds[i], cls);
        }
        return result;
    }

    /**
     * Converts a collection of ids to a set of respective concepts
     *
     * @param graph
     * @param ids
     * @return
     */
    public static Set<ONDEXConcept> toConcepts(ONDEXGraph graph, Collection<Integer> ids) {
        Set<ONDEXConcept> cs = new HashSet<ONDEXConcept>();
        for (Integer id : ids) {
            cs.add(graph.getConcept(id));
		}
		return cs;
	}

	/**
     * Converts a collection of ids to a set of respective relations
     *
     * @param graph
     * @param ids
     * @return
     */
	public static Set<ONDEXRelation> toRelations(ONDEXGraph graph, Collection<Integer> ids){
		Set<ONDEXRelation> cs = new HashSet<ONDEXRelation>();
		for(Integer  id: ids){
			cs.add(graph.getRelation(id));
		}
		return cs;
	}
}