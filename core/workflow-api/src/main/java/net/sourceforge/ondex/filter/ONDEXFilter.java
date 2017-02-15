package net.sourceforge.ondex.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.sourceforge.ondex.AbstractONDEXPlugin;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPlugin;
import net.sourceforge.ondex.RequiresGraph;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.exception.type.AttributeNameMissingException;
import net.sourceforge.ondex.exception.type.DataSourceMissingException;
import net.sourceforge.ondex.exception.type.ConceptClassMissingException;
import net.sourceforge.ondex.exception.type.EvidenceTypeMissingException;
import net.sourceforge.ondex.exception.type.RelationTypeMissingException;
import net.sourceforge.ondex.exception.type.UnitMissingException;
import net.sourceforge.ondex.exception.type.WrongParameterException;
import net.sourceforge.ondex.init.ArgumentDescription;

/**
 * Parent class for filter functionality implements ONDEXPlugin.
 *
 * @author hindelm
 */
public abstract class ONDEXFilter extends AbstractONDEXPlugin implements
        ONDEXPlugin, RequiresGraph {

    /**
     * Copies filter results in the given AbstractONDEXGraph.
     *
     * @param exportGraph AbstractONDEXGraph
     */
    public abstract void copyResultsToNewGraph(ONDEXGraph exportGraph);

    /**
     * Returns concepts that are visible post filter.
     *
     * @return Set<AbstractConcept>
     */
    public abstract Set<ONDEXConcept> getVisibleConcepts();

    /**
     * Returns relations that are visible post filter.
     *
     * @return Set<AbstractRelation>
     */
    public abstract Set<ONDEXRelation> getVisibleRelations();

    /**
     * Returns a map of allowed from and to ConceptClasses for relations.
     *
     * @param graph AbstractONDEXGraph to handle meta data
     * @return map of allowed from and to ConceptClasses
     */
    protected Map<ConceptClass, HashSet<ConceptClass>> getAllowedCCs(
            ONDEXGraph graph) throws InvalidPluginArgumentException {
        Object[] ccs = args.getObjectValueArray(ArgumentNames.CONCEPTCLASS_RESTRICTION_ARG);

        // add CC restriction pairs
        HashMap<ConceptClass, HashSet<ConceptClass>> ccMapping = new HashMap<ConceptClass, HashSet<ConceptClass>>();
        if (ccs != null && ccs.length > 0) {
            for (Object cc : ccs) {
                String pair = ((String) cc).trim();
                String[] values = pair.split(",");
                if (values.length != 2) {
                    fireEventOccurred(new WrongParameterEvent(
                            "Invalid Format for ConceptClass pair " + pair,
                            getCurrentMethodName()));
                }

                ConceptClass fromConceptClass;
                ConceptClass toConceptClass;

                try {
                    fromConceptClass = graph.getMetaData().getConceptClass(values[0]);
                    toConceptClass = graph.getMetaData().getConceptClass(values[1]);
                } catch (WrongParameterException e) {
                    //invalid parameter given to filter...
                    continue;
                }

                if (fromConceptClass != null && toConceptClass != null) {

                    if (!ccMapping.containsKey(fromConceptClass)) {
                        HashSet<ConceptClass> toCCs = new HashSet<ConceptClass>();
                        ccMapping.put(fromConceptClass, toCCs);
                    }

                    ccMapping.get(fromConceptClass).add(toConceptClass);

                    fireEventOccurred(new GeneralOutputEvent(
                            "Added ConceptClass restriction for "
                                    + fromConceptClass.getId() + " ==> "
                                    + toConceptClass.getId(),
                                    getCurrentMethodName()));
                } else {
                    if (fromConceptClass == null)
                        fireEventOccurred(new WrongParameterEvent(values[0]
                                + " is not a valid from ConceptClass.",
                                getCurrentMethodName()));
                    if (toConceptClass == null)
                        fireEventOccurred(new WrongParameterEvent(values[1]
                                + " is not a valid to ConceptClass.",
                                getCurrentMethodName()));
                }
            }
        }
        return ccMapping;
    }

    /**
     * Returns a map of allowed from and to DataSources for relations.
     *
     * @param graph AbstractONDEXGraph to handle meta data
     * @return map of allowed from and to DataSources
     */
    protected Map<DataSource, HashSet<DataSource>> getAllowedDataSources(ONDEXGraph graph) throws InvalidPluginArgumentException {
        Object[] cvs = args.getObjectValueArray(ArgumentNames.DATASOURCE_RESTRICTION_ARG);

        // add DataSource restriction pairs
        HashMap<DataSource, HashSet<DataSource>> dataSourceMapping = new HashMap<DataSource, HashSet<DataSource>>();
        if (cvs != null && cvs.length > 0) {
            for (Object cv : cvs) {
                String pair = ((String) cv).trim();
                String[] values = pair.split(",");

                if (values.length != 2) {
                    fireEventOccurred(new WrongParameterEvent(
                            "Invalid Format for DataSource pair " + pair,
                            getCurrentMethodName()));
                }

                DataSource fromDataSource;
                DataSource toDataSource;

                try {
                    fromDataSource = graph.getMetaData().getDataSource(values[0]);
                    toDataSource = graph.getMetaData().getDataSource(values[1]);
                } catch (WrongParameterException e) {
                    continue;
                }

                if (fromDataSource != null && toDataSource != null) {

                    if (!dataSourceMapping.containsKey(fromDataSource)) {
                        HashSet<DataSource> toDataSources = new HashSet<DataSource>();
                        dataSourceMapping.put(fromDataSource, toDataSources);
                    }

                    dataSourceMapping.get(fromDataSource).add(toDataSource);

                    fireEventOccurred(new GeneralOutputEvent(
                            "Added DataSource restriction for " + fromDataSource.getId()
                                    + " ==> " + toDataSource.getId(),
                                    getCurrentMethodName()));
                } else {
                    if (fromDataSource == null)
                        fireEventOccurred(new WrongParameterEvent(values[0]
                                + " is not a valid from DataSource.",
                                getCurrentMethodName()));
                    if (toDataSource == null)
                        fireEventOccurred(new WrongParameterEvent(values[1]
                                + " is not a valid to DataSource.",
                                getCurrentMethodName()));
                }
            }
        }
        return dataSourceMapping;
    }

    /**
     * fetches the required metadata or throws a respective runtime
     * exception if it doesn't exist.
     *
     * @param id the id of the metadata object.
     */
    public ConceptClass requireConceptClass(String id) throws ConceptClassMissingException {
        ConceptClass cc = graph.getMetaData().getConceptClass(id);
        if (cc == null)
            throw new ConceptClassMissingException("Concept Class " + id + " missing");
        else
            return cc;
    }

    /**
     * fetches the required metadata or throws a respective runtime
     * exception if it doesn't exist.
     *
     * @param id the id of the metadata object.
     */
    public RelationType requireRelationType(String id) throws RelationTypeMissingException {
        RelationType rt = graph.getMetaData().getRelationType(id);
        if (rt == null)
            throw new RelationTypeMissingException("Relation Type " + id + " missing");
        else
            return rt;
    }

    /**
     * fetches the required metadata or throws a respective runtime
     * exception if it doesn't exist.
     *
     * @param id the id of the metadata object.
     */
    public DataSource requireDataSource(String id) throws DataSourceMissingException {
        DataSource dataSource = graph.getMetaData().getDataSource(id);
        if (dataSource == null)
            throw new DataSourceMissingException("Data Source " + id + " missing");
        else
            return dataSource;
    }

    /**
     * fetches the required metadata or throws a respective runtime
     * exception if it doesn't exist.
     *
     * @param id the id of the metadata object.
     */
    public EvidenceType requireEvidenceType(String id) throws EvidenceTypeMissingException {
        EvidenceType e = graph.getMetaData().getEvidenceType(id);
        if (e == null)
            throw new EvidenceTypeMissingException("Evidence Type " + id + " missing");
        else
            return e;
    }

    /**
     * fetches the required metadata or throws a respective runtime
     * exception if it doesn't exist.
     *
     * @param id the id of the metadata object.
     */
    public AttributeName requireAttributeName(String id) throws AttributeNameMissingException {
        AttributeName a = graph.getMetaData().getAttributeName(id);
        if (a == null)
            throw new AttributeNameMissingException("Attribute Name " + id + " missing");
        else
            return a;
    }

    /**
     * fetches the required metadata or throws a respective runtime
     * exception if it doesn't exist.
     *
     * @param id the id of the metadata object.
     */
    public Unit requireUnit(String id) throws UnitMissingException {
        Unit u = graph.getMetaData().getUnit(id);
        if (u == null)
            throw new UnitMissingException("Unit " + id + " missing");
        else
            return u;
    }

    @Override
    public Collection<ArgumentDescription> getArgumentDescriptions(int position)
    {
        ArgumentDescription graph = new ArgumentDescription();
        graph.setCls("net.sourceforge.ondex.core.ONDEXGraph");
        graph.setName("Graph id");
        graph.setInteranlName("graphId");
        graph.setInputId(position);
        graph.setDescription("Graph that will be operated on by this plugin.");
        graph.setIsRequired(true);
        graph.setIsInputObject(true);

        ArgumentDescription graph2 = new ArgumentDescription();
        //TODO this does not handle the output ids at the moment
        graph2.setOutputId(0);
        graph2.setCls("net.sourceforge.ondex.core.AbstractONDEXGraph");
        graph2.setName("Secondary graph id");
        graph2.setInteranlName("secondaryGraphId");
        graph2.setDescription("Create a new graph with the this name for the output of the filter, leave original unmodified.");
        graph2.setIsRequired(false);
        graph2.setIsOutputObject(true);

        return Arrays.asList(graph, graph2);
    }
}
