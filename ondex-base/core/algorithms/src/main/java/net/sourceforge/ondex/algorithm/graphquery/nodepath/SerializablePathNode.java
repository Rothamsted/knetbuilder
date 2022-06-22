package net.sourceforge.ondex.algorithm.graphquery.nodepath;

import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;

/**
 *
 */
@XmlRootElement(name = "Path")
public class SerializablePathNode {

    @XmlElement(name = "concept", required = true)
    private int[] concepts;
    @XmlElement(name = "relation", required = true)
    private int[] relations;

    private SerializablePathNode() {
    }

    public SerializablePathNode(PathNode pathNode) {
        List<ONDEXConcept> conceptL = pathNode.getConceptsInPositionOrder();
        List<ONDEXRelation> relationL = pathNode.getRelationsInPositionOrder();

        concepts = new int[conceptL.size()];
        for (int i = 0; i < concepts.length; i++)
            concepts[i] = conceptL.get(i).getId();

        relations = new int[relationL.size()];
        for (int i = 0; i < relations.length; i++)
            relations[i] = relationL.get(i).getId();
    }

}
