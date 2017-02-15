package net.sourceforge.ondex.algorithm.graphquery.nodepath;

import net.sourceforge.ondex.algorithm.graphquery.StateMachineComponent;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

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