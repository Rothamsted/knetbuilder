package uk.ac.ncl.cs.ondex.stats.datastructures;

import java.util.ArrayList;
import java.util.List;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;

public class Neighbour {

    private ONDEXConcept concept;
    private ONDEXRelation relation;
    private boolean outgoing;

    public Neighbour(ONDEXConcept concept, ONDEXRelation relation) {
        this.concept = concept;
        this.relation = relation;
        this.outgoing = relation.getToConcept().equals(concept);
    }

    public ONDEXConcept getConcept() {
        return concept;
    }

    public ONDEXRelation getRelation() {
        return relation;
    }

    public boolean isOutgoing() {
        return outgoing;
    }

    public static List<Neighbour> getNeighbourhood(ONDEXConcept c, ONDEXGraph graph) {

        List<Neighbour> neighbours = new ArrayList<Neighbour>();

        for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
            ONDEXConcept nc = r.getFromConcept().equals(c) ?
                            r.getToConcept() : r.getFromConcept();
            Neighbour neighbour = new Neighbour(nc,r);
            neighbours.add(neighbour);
        }

        return neighbours;
    }
}
