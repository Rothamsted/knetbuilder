package net.sourceforge.ondex.export.conceptclass;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.base.AbstractONDEXGraph;
import net.sourceforge.ondex.export.ONDEXExport;

import java.util.Set;

/**
 * Prints very simple statistics on the abundance of concept classes
 *
 * @author hindlem
 */
@Authors(authors = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
@Status(description = "Set to DISCONTINUED 4 May 2010 due to System.out usage. (Christian)", status = StatusType.DISCONTINUED)
public class Export extends ONDEXExport
{

    public Export() {
        super();
    }

    public Export(AbstractONDEXGraph graph) {
        this.setONDEXGraph(graph);
    }

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[0];
    }

    @Override
    public String getName() {
        return "concept class statistics";
    }

    @Override
    public String getVersion() {
        return "alpha 11.08.2008";
    }

    @Override
    public String getId() {
        return "conceptclass";
    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public void start() {
        for (ConceptClass conceptClass : graph.getMetaData().getConceptClasses()) {
            Set<ONDEXConcept> concepts = graph.getConceptsOfConceptClass(conceptClass);
            System.out.println("ConceptClassId\tNumber of instances");
            if (concepts.size() > 0) {
                System.out.println(conceptClass.getId() + "\t" + concepts.size());
            }
        }
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

}
