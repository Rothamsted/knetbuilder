package net.sourceforge.ondex.algorithm.graphquery.nodepath;

import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import net.sourceforge.ondex.algorithm.graphquery.StateMachineComponent;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;

/**
 *
 */
@XmlRootElement(name = "Path")
public class SerializableEvidencePathNode {

    @XmlElement(name = "concept", required = true)
    private int[] concepts;

    @XmlElement(name = "relation", required = true)
    private int[] relations;

    @XmlElement(name = "evidence", required = true)
    List<StateMachineComponent> evidences;

    private SerializableEvidencePathNode() {

    }

    public SerializableEvidencePathNode(EvidencePathNode pathNode) {
        List<ONDEXConcept> conceptL = pathNode.getConceptsInPositionOrder();
        List<ONDEXRelation> relationL = pathNode.getRelationsInPositionOrder();


        concepts = new int[conceptL.size()];
        for (int i = 0; i < concepts.length; i++)
            concepts[i] = conceptL.get(i).getId();

        relations = new int[relationL.size()];
        for (int i = 0; i < relations.length; i++)
            relations[i] = relationL.get(i).getId();

        evidences = pathNode.getEvidencesInPositionOrder();

    }

}