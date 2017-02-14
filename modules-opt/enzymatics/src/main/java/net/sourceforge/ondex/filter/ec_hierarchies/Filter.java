package net.sourceforge.ondex.filter.ec_hierarchies;

import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.annotations.metadata.DataSourceRequired;
import net.sourceforge.ondex.annotations.metadata.ConceptClassRequired;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.RangeArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.parser.ec.MetaData;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static net.sourceforge.ondex.filter.ec_hierarchies.ArgumentNames.EC_LEVEL_ARG;
import static net.sourceforge.ondex.filter.ec_hierarchies.ArgumentNames.EC_LEVEL_DESC;
import static net.sourceforge.ondex.filter.ec_hierarchies.MetaData.CC_EC;

/**
 * A simple filter that allows a user to filter EC concepts based on there EC level in the hierarchy
 *
 * @author hindlem
 */
@Status(description = "not tested yet", status = StatusType.EXPERIMENTAL)
@DataSourceRequired(ids = {
        net.sourceforge.ondex.parser.ec.MetaData.DS_EC})
@ConceptClassRequired(ids = {
        MetaData.CC_EC})
public class Filter extends ONDEXFilter
{

    private Set<ONDEXConcept> visibleConcepts;
    private Set<ONDEXRelation> visibleRelations;

    @Override
    public String getName() {
        return "ec-hierarchies";
    }

    @Override
    public String getVersion() {
        return "alpha";
    }

    @Override
    public String getId() {
        return "ec_hierarchies";
    }


    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new RangeArgumentDefinition<Integer>(EC_LEVEL_ARG, EC_LEVEL_DESC, true, null, 1, 4, Integer.class, true)
        };
    }

    @Override
    public void start() throws Exception {

        Set<ONDEXConcept> hideConcepts = new HashSet<ONDEXConcept>();
        Set<ONDEXRelation> hideRelations = new HashSet<ONDEXRelation>();

        List<Integer> ecTerms = (List<Integer>) args.getObjectValueList(EC_LEVEL_ARG);

        ConceptClass ccEC = graph.getMetaData().getConceptClass(CC_EC);
        DataSource dataSourceEC = graph.getMetaData().getDataSource(CC_EC);

        for (ONDEXConcept ecConcept : graph.getConceptsOfConceptClass(ccEC)) {
            for (ConceptAccession accession : ecConcept.getConceptAccessions()) {
                if (accession.getElementOf().equals(dataSourceEC)) {
                    int intLevel = getLevel(accession.getAccession());
                    if (intLevel == 0 || intLevel > 4) {
                        System.err.println("EC accession of " + intLevel + " levels is invalid :" + accession.getAccession());
                    }
                    if (ecTerms.contains(Integer.valueOf(intLevel))) {
                        hideConcepts.add(ecConcept);
                        for (ONDEXRelation relation : graph.getRelationsOfConcept(ecConcept)) {
                            hideRelations.add(relation);
                        }
                    }
                }
            }
        }

        System.out.println("finished found " + hideConcepts.size() + " concepts and " + hideRelations.size() + "relations");

        visibleConcepts = new HashSet<ONDEXConcept>(graph.getConcepts());
        visibleConcepts.removeAll(hideConcepts);

        visibleRelations = new HashSet<ONDEXRelation>(graph.getRelations());
        visibleRelations.removeAll(hideRelations);
    }

    private final Pattern dotPattern = Pattern.compile("\\.");

    private int getLevel(String accession) {

        int num_level = 0;
        String[] levels = dotPattern.split(accession);
        for (String level : levels) {
            if (!level.trim().equalsIgnoreCase("-")) {
                try {
                    Integer.parseInt(level);
                    num_level++;
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return num_level;
    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void copyResultsToNewGraph(ONDEXGraph exportGraph) {
        ONDEXGraphCloner graphCloner = new ONDEXGraphCloner(graph, exportGraph);
        for (ONDEXConcept c : visibleConcepts) {
            graphCloner.cloneConcept(c);
        }
        for (ONDEXRelation r : visibleRelations) {
            graphCloner.cloneRelation(r);
        }
    }

    @Override
    public Set<ONDEXConcept> getVisibleConcepts() {
        return visibleConcepts;
    }

    @Override
    public Set<ONDEXRelation> getVisibleRelations() {
        return visibleRelations;
    }
}
