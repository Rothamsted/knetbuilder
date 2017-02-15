package net.sourceforge.ondex.export.datasource;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.export.ONDEXExport;

import java.util.Set;

/**
 * Prints very simple statistics on the abundance of data source C.V.
 *
 * @author hindlem
 */
@Authors(authors = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
@Status(description = "Set to DISCONTINUED 4 May 2010 due to System.out usage. (Christian)", status = StatusType.DISCONTINUED)
public class Export extends ONDEXExport
{

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[0];
    }

    @Override
    public String getName() {
        return "data source C.V. statistics";
    }

    @Override
    public String getVersion() {
        return "alpha 11.08.2008";
    }

    @Override
    public String getId() {
        return "cv";
    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public void start() {
        for (DataSource dataSource : graph.getMetaData().getDataSources()) {
            Set<ONDEXConcept> concepts = graph.getConceptsOfDataSource(dataSource);
            System.out.println("Data source C.V. id\tNumber of instances");
            if (concepts.size() > 0) {
                System.out.println(dataSource.getId() + "\t" + concepts.size());
            }
        }
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

}
