package net.sourceforge.ondex.workflow.transformer;

import java.util.Collection;
import java.util.Collections;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.exception.type.AttributeNameMissingException;
import net.sourceforge.ondex.exception.type.ConceptClassMissingException;
import net.sourceforge.ondex.exception.type.DataSourceMissingException;
import net.sourceforge.ondex.exception.type.EvidenceTypeMissingException;
import net.sourceforge.ondex.exception.type.RelationTypeMissingException;
import net.sourceforge.ondex.exception.type.UnitMissingException;
import net.sourceforge.ondex.workflow.AbstractONDEXPlugin;
import net.sourceforge.ondex.workflow.ONDEXPlugin;
import net.sourceforge.ondex.workflow.RequiresGraph;
import net.sourceforge.ondex.workflow.init.ArgumentDescription;

/**
 * Abstract implementation of an ONDEX transformer, manages listener handling.
 *
 * @author hindlem
 */
public abstract class ONDEXTransformer extends AbstractONDEXPlugin implements
        ONDEXPlugin, RequiresGraph
{

    /**
     * fetches the required metadata or throws a respective runtime
     * exception if it doesn't exist.
     *
     * @param id the id of the metadata object.
     */
    public ConceptClass requireConceptClass(String id) throws ConceptClassMissingException {
        ConceptClass cc = graph.getMetaData().getConceptClass(id);
        if (cc == null)
            throw new ConceptClassMissingException("concept class " + id + " missing");
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
            throw new RelationTypeMissingException("relation type " + id + " missing");
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
            throw new DataSourceMissingException("DataSource " + id + " missing");
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
        ArgumentDescription ab = new ArgumentDescription();
        ab.setCls("net.sourceforge.ondex.core.ONDEXGraph");
        ab.setName("Graph id");
        ab.setInteranlName("graphId");
        ab.setInputId(position);
        ab.setDescription("Graph that will be operated on by this plugin.");
        ab.setIsRequired(true);
        ab.setIsInputObject(true);
        return Collections.singleton(ab);
    }
}
