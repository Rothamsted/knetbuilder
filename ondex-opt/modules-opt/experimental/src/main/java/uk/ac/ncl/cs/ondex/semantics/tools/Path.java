package uk.ac.ncl.cs.ondex.semantics.tools;

import java.util.List;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;

public class Path {

    private ONDEXEntity[] path;
    private ONDEXGraph og;

    Path(ONDEXGraph og, List<Integer> ids) throws MalformedPathException {
        this.og = og;
        path = new ONDEXEntity[ids.size()];
        for (int i = 0; i < path.length; i++) {
            if (i % 2 == 0) {
                path[i] = og.getConcept(ids.get(i));
                if (path[i] == null) {
                    throw new MalformedPathException("No concept for id " + ids.get(i));
                }
            } else {
                path[i] = og.getRelation(ids.get(i));
                if (path[i] == null) {
                    throw new MalformedPathException("No relation for id" + ids.get(i));
                }
            }
        }
    }

    public ONDEXConcept head() {
        return (ONDEXConcept) path[0];
    }

    public ONDEXConcept tail() {
        return (ONDEXConcept) path[path.length -1];
    }

    public ONDEXEntity[] getElements() {
        return path;
    }
}
