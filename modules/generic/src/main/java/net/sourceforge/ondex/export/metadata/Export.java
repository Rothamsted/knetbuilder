package net.sourceforge.ondex.export.metadata;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.export.ONDEXExport;

/**
 * For a quick view of the important metadata
 *
 * @author hindlem
 */
@Authors(authors = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
@Status(description = "Set to DISCONTINUED 4 May 2010 due to System.out usage. (Christian)", status = StatusType.DISCONTINUED)
public class Export extends ONDEXExport
{

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[0];
    }

    public String getName() {
        return "MetaData Stats";
    }

    public String getVersion() {
        return "02-DEC-08";
    }

    @Override
    public String getId() {
        return "metadata";
    }

    public boolean requiresIndexedGraph() {
        return false;
    }

    public void start() {
        ONDEXGraphMetaData metadata = graph.getMetaData();

        for (ConceptClass cc : metadata.getConceptClasses()) {
            System.out.println("ConceptClass " + cc.getId() + " " + cc.getFullname());
            System.out.println("\tConcepts = " + graph.getConceptsOfConceptClass(cc).size());
        }

        for (DataSource dataSource : metadata.getDataSources()) {
            System.out.println("DataSource " + dataSource.getId() + " " + dataSource.getFullname());
            System.out.println("\tConcepts = " + graph.getConceptsOfDataSource(dataSource).size());
        }

        for (AttributeName att : metadata.getAttributeNames()) {
            System.out.println("AttributeName " + att.getId() + " " + att.getFullname());
            System.out.println("\tConcepts = " + graph.getConceptsOfAttributeName(att).size());
            System.out.println("\tRelations = " + graph.getRelationsOfAttributeName(att).size());
        }

        for (RelationType rt : metadata.getRelationTypes()) {
            System.out.println("RelationType " + rt.getId() + " " + rt.getFullname());
            System.out.println("\tRelations = " + graph.getRelationsOfRelationType(rt).size());
        }

    }


    /**
     * Convenience method for outputing the current method name in a dynamic way
     *
     * @return the calling method name
     */
    public static String getCurrentMethodName() {
        Exception e = new Exception();
        StackTraceElement trace = e.fillInStackTrace().getStackTrace()[1];
        String name = trace.getMethodName();
        String className = trace.getClassName();
        int line = trace.getLineNumber();
        return "[CLASS:" + className + " - METHOD:" + name + " LINE:" + line + "]";
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }
}
