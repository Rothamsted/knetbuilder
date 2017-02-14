package net.sourceforge.ondex.tools;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.exception.type.ParsingFailedException;
import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import net.sourceforge.ondex.parser.oxl.Parser;

/**
 *
 * @author jweile
 */
public class MetaDataTopup {

    private ONDEXGraph graph, templateGraph;

    public MetaDataTopup(ONDEXGraph graph, InputStream mdIn) throws ParsingFailedException, InconsistencyException  {

        this.graph = graph;

        this.templateGraph = new MemoryONDEXGraph("MetadataTemplate");

        Parser oxlParser = new Parser();
        oxlParser.setONDEXGraph(templateGraph);
        oxlParser.start(mdIn);

    }

    public void topup() throws PluginConfigurationException {

        for (ConceptClass cc : templateGraph.getMetaData().getConceptClasses()) {
            transferConceptClass(cc);
        }

        for (RelationType rt : templateGraph.getMetaData().getRelationTypes()) {
            transferRelationType(rt);
        }

        for (Unit u : templateGraph.getMetaData().getUnits()) {
            transferUnit(u);
        }

        for (AttributeName an : templateGraph.getMetaData().getAttributeNames()) {
            transferAttributeName(an);
        }

        for (EvidenceType et : templateGraph.getMetaData().getEvidenceTypes()) {
            transferEvidenceType(et);
        }

        for (DataSource dataSource : templateGraph.getMetaData().getDataSources()) {
            transferDataSource(dataSource);
        }

    }

    private Set<String> __closedCCs = new HashSet<String>();

    private ConceptClass transferConceptClass(ConceptClass template) throws PluginConfigurationException {

        ConceptClass peer = graph.getMetaData().getConceptClass(template.getId());

        if (peer != null) {
            //Then it's known already

            //Check if this class has been processed already.
            if (__closedCCs.contains(template.getId())) {
                return peer;
            }

            //Check if all parents match
            if (doCCParentsMatch(template, peer)) {
                __closedCCs.add(template.getId());
                return peer;
            } else {
                //Then it's an ID clash!
                throw new PluginConfigurationException("Unable to top-up metadata: " +
                        "The concept class with ID "+template.getId()+" is already defined otherwise.");
            }
        } else {
            //Then its novel!
//            System.err.println("Adding to Metadata: ConceptClass "+template.getFullname());
            //Import its parents
            ConceptClass peerParent = transferConceptClass(template.getSpecialisationOf());

            //Import itself
            peer = graph.getMetaData().createConceptClass(template.getId(),
                            template.getFullname(),
                            template.getDescription(),
                            peerParent);
            __closedCCs.add(template.getId());
            return peer;
        }

    }

    private boolean doCCParentsMatch(ConceptClass template, ConceptClass peer) {

        ConceptClass templateP = template.getSpecialisationOf();
        ConceptClass peerP = peer.getSpecialisationOf();

        if (templateP == null && peerP == null) {
            __closedCCs.add(template.getId());
            return true;
        } else if (templateP == null || peerP == null) {
            return false;
        } else if (__closedCCs.contains(templateP.getId())) {
            return true;
        } else if (templateP.getId().equals(peerP.getId())) {
            boolean match = doCCParentsMatch(templateP,peerP);
            if (match) {
                __closedCCs.add(template.getId());
            }
            return match;
        } else {//different parents
            return false;
        }
        
    }

    
    private Set<String> __closedRTs = new HashSet<String>();

    private RelationType transferRelationType(RelationType rt) throws PluginConfigurationException {

        RelationType peer = graph.getMetaData().getRelationType(rt.getId());

        if (peer != null) {
            //Then it's known already

            //Check if this class has been processed already.
            if (__closedRTs.contains(rt.getId())) {
                return peer;
            }

            //Check if all parents match
            if (doRTParentsMatch(rt, peer)) {
                __closedRTs.add(rt.getId());
                return peer;
            } else {
                //Then it's an ID clash!
                throw new PluginConfigurationException("Unable to top-up metadata: " +
                        "The relation type with ID "+rt.getId()+" is already defined otherwise.");
            }
        } else {
            //It's novel!
//            System.err.println("Adding to Metadata: RelationType "+rt.getFullname());
            //Import its parents
            RelationType peerParent = transferRelationType(rt.getSpecialisationOf());

            //Import itself
            peer = graph.getMetaData().createRelationType(rt.getId(),
                            rt.getFullname(),
                            rt.getDescription(),
                            rt.getInverseName(),
                            rt.isAntisymmetric(),
                            rt.isReflexive(),
                            rt.isSymmetric(),
                            rt.isTransitiv(), 
                            peerParent);
            __closedRTs.add(rt.getId());
            return peer;
        }

    }

    private boolean doRTParentsMatch(RelationType template, RelationType peer) {

        RelationType templateP = template.getSpecialisationOf();
        RelationType peerP = peer.getSpecialisationOf();

        if (templateP == null && peerP == null) {
            __closedRTs.add(template.getId());
            return true;
        } else if (templateP == null || peerP == null) {
            return false;
        } else if (__closedRTs.contains(templateP.getId())) {
            return true;
        } else if (templateP.getId().equals(peerP.getId())) {
            boolean match = doRTParentsMatch(templateP, peerP);
            if (match) {
                __closedRTs.add(template.getId());
            }
            return match;
        } else {//different parents
            return false;
        }

    }





    private Set<String> __closedANs = new HashSet<String>();

    private AttributeName transferAttributeName(AttributeName template) throws PluginConfigurationException {

        if (template == null) {
            return null;//hack to bypass a bug in the oxl parser
        }

        AttributeName peer = graph.getMetaData().getAttributeName(template.getId());

        if (peer != null) {
            //Then it's known already

            //Check if this class has been processed already.
            if (__closedANs.contains(template.getId())) {
                return peer;
            }

            //Check if all parents match
            if (doANParentsMatch(template, peer)) {
                __closedANs.add(template.getId());
                return peer;
            } else {
                //Then it's an ID clash!
                throw new PluginConfigurationException("Unable to top-up metadata: " +
                        "The attribute name with ID "+template.getId()+" is already defined otherwise.");
            }
        } else {
            //Then its novel!
//            System.err.println("Adding to Metadata: AttributeName "+template.getFullname());
            //Import its parents
            AttributeName peerParent = transferAttributeName(template.getSpecialisationOf());


            Unit peerUnit = template.getUnit() != null ?
                graph.getMetaData().getUnit(template.getUnit().getId()) : null;

            //Import itself
            peer = graph.getMetaData().createAttributeName(template.getId(),
                            template.getFullname(),
                            template.getDescription(),
                            peerUnit,
                            template.getDataType(),
                            peerParent);
            __closedANs.add(template.getId());
            return peer;
        }

    }

    private boolean doANParentsMatch(AttributeName template, AttributeName peer) {

        AttributeName templateP = template.getSpecialisationOf();
        AttributeName peerP = peer.getSpecialisationOf();

        if (templateP == null && peerP == null) {
            __closedANs.add(template.getId());
            return true;
        } else if (templateP == null || peerP == null) {
            return false;
        } else if (__closedANs.contains(templateP.getId())) {
            return true;
        } else if (templateP.getId().equals(peerP.getId())) {
            boolean match = doANParentsMatch(templateP, peerP);
            if (match) {
                __closedANs.add(template.getId());
            }
            return match;
        } else {//different parents
            return false;
        }

    }

    private void transferUnit(Unit template) {

        Unit peer = graph.getMetaData().getUnit(template.getId());

        if (peer == null) {
//            System.err.println("Adding to Metadata: Unit "+template.getFullname());
            graph.getMetaData().createUnit(template.getId(),
                    template.getFullname(),
                    template.getDescription());
        } else {
            //let's assume everything is fine then.
        }

    }

    private void transferEvidenceType(EvidenceType template) {

        EvidenceType peer = graph.getMetaData().getEvidenceType(template.getId());

        if (peer == null) {
//            System.err.println("Adding to Metadata: EvidenceType "+template.getFullname());
            graph.getMetaData().createEvidenceType(template.getId(),
                    template.getFullname(),
                    template.getDescription());
        } else {
            //let's assume everything is fine then.
        }

    }

     private void transferDataSource(DataSource template) {

        DataSource peer = graph.getMetaData().getDataSource(template.getId());

        if (peer == null) {
//            System.err.println("Adding to Metadata: DataSource "+template.getFullname());
            graph.getMetaData().createDataSource(template.getId(),
                    template.getFullname(),
                    template.getDescription());
        } else {
            //let's assume everything is fine then.
        }

    }


}
