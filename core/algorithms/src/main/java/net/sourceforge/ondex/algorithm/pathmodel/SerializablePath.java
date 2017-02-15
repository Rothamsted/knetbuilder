package net.sourceforge.ondex.algorithm.pathmodel;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * @author hindlem
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "path")
public class SerializablePath {

    @XmlAttribute(name = "startsWithConcept")
    private boolean startsWithConcept;

    @XmlElement(name = "concepts")
    private int[] concepts;

    @XmlElement(name = "relations")
    private int[] relations;

    private SerializablePath() {
        //JAXPConstructor
    }

    public SerializablePath(Path path) {
        concepts = new int[path.getConceptLength()];
        List<ONDEXConcept> conceptToAdd = path.getConceptsInPositionOrder();
        for (int i = 0; i < concepts.length; i++) {
            concepts[i] = conceptToAdd.get(i).getId();
        }

        relations = new int[path.getRelationLength()];
        List<ONDEXRelation> relationToAdd = path.getRelationsInPositionOrder();
        for (int i = 0; i < relations.length; i++) {
            relations[i] = relationToAdd.get(i).getId();
        }

        startsWithConcept = path.getStartingEntity() instanceof ONDEXConcept;
    }

    public boolean isStartsWithConcept() {
        return startsWithConcept;
    }

    public void setStartsWithConcept(boolean startsWithConcept) {
        this.startsWithConcept = startsWithConcept;
    }

    public int[] getConcepts() {
        return concepts;
    }

    public void setConcepts(int[] concepts) {
        this.concepts = concepts;
    }

    public int[] getRelations() {
        return relations;
    }

    public void setRelations(int[] relations) {
        this.relations = relations;
    }
}
