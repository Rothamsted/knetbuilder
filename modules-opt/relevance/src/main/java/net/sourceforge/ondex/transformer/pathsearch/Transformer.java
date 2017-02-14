package net.sourceforge.ondex.transformer.pathsearch;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.cytoscape.mapping.PathSearcher;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A transformer that wraps Jochen's PathSearch algorithm.
 *
 * @author Matthew Pocock
 */
public class Transformer
        extends ONDEXTransformer
{
    private static final String EVIDENCE = "evidence";
    private static final String RELATION = "relation";
    private static final String PATH = "path";

    public String getName() {
        return "pathsearch";
    }

    public String getVersion() {
        return "alpha";
    }

    @Override
    public String getId() {
        return "pathsearch";
    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new StringArgumentDefinition(
                        PATH, "Space-seperated list of concept and edge types defining a path.",
                        true, null, false),
                new StringArgumentDefinition(
                        RELATION, "The relation to create between the first and last concept in paths.",
                        true, null, false),
                new StringArgumentDefinition(
                        EVIDENCE, "The evidence code to use",
                        true, null, false)
        };
    }

    public void start() throws Exception {
        // initialize the path to search
        String searchPath = (String) getArguments().getUniqueValue(PATH);
        String[] steps = searchPath.split("\\s+");

        List<ConceptClass> ccs = new ArrayList<ConceptClass>();
        List<RelationType> rts = new ArrayList<RelationType>();
        for (int i = 0; i < steps.length; i++) {
            switch (i % 2) {
                case 0:
                    // concept class
                    ConceptClass cc = graph.getMetaData().getConceptClass(steps[i]);
                    ccs.add(cc);
                    break;
                case 1:
                    // relation type
                    RelationType rt = graph.getMetaData().getRelationType(steps[i]);
                    rts.add(rt);
                    break;
            }
        }
        PathSearcher pathSearcher = new PathSearcher(
                graph, ccs.toArray(new ConceptClass[ccs.size()]), rts.toArray(new RelationType[rts.size()]));

        // initialize the relation to add
        String relation = (String) getArguments().getUniqueValue(RELATION);
        RelationType typeToAdd;
        if (graph.getMetaData().checkRelationType(relation)) {
            typeToAdd = graph.getMetaData().getRelationType(
                    relation);
        } else {
            typeToAdd = graph.getMetaData().createRelationType(relation, relation, relation, relation,
                    false, false, false, false, null);
        }


        // fetch the evidence code
        String ev = (String) getArguments().getUniqueValue(EVIDENCE);
        EvidenceType evidence;
        if (graph.getMetaData().checkDataSource(ev)) {
            evidence = graph.getMetaData().getEvidenceType(ev);
        } else {
            evidence = graph.getMetaData().createEvidenceType(ev, ev, ev);
        }
        Set<EvidenceType> evidenceTypes = Collections.singleton(evidence);

        // do the search
        pathSearcher.search();

        // write the results into the graph
        for (PathSearcher.Path path = pathSearcher.nextPath(); path != null; path = pathSearcher.nextPath()) {
            graph.createRelation(path.head(), path.tail(), typeToAdd, evidenceTypes);
        }
    }

    public boolean requiresIndexedGraph() {
        return false;
    }

    public String[] requiresValidators() {
        return new String[0];
    }
}