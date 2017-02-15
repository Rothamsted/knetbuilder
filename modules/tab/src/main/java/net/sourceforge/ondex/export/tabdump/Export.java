package net.sourceforge.ondex.export.tabdump;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.export.ONDEXExport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

public class Export extends ONDEXExport
{

    private static final String TAB = "\t";

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{new FileArgumentDefinition(
                FileArgumentDefinition.EXPORT_FILE, "tab delimited base filename", true,
                false, false, false)};
    }

    @Override
    public String getName() {
        return "Tabular Graph Dump";
    }

    @Override
    public String getVersion() {
        return "06.01.2009";
    }

    @Override
    public String getId() {
        return "tabdump";
    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public String[] requiresValidators() {
        return null;
    }

    @Override
    public void start() throws Exception {

        // get filename
        File file = new File((String) args.getUniqueValue(FileArgumentDefinition.EXPORT_FILE));
        String filename = file.getAbsolutePath();

        // write concepts to file
        File fileNodes = new File(filename + ".nodes");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileNodes));
            for (ONDEXConcept c : graph.getConcepts()) {
                writer.write(String.valueOf(c.getId()));
                writer.write(TAB);
                if (c.getConceptName() != null)
                    writer.write(c.getConceptName().getName());
                else
                    writer.write("");
                writer.write(TAB);
                writer.write(c.getOfType().getId());
                writer.write(TAB);
                Iterator<ONDEXConcept> context = c.getTags().iterator();
                while (context.hasNext()) {
                    writer.write(String.valueOf(context.next().getId()));
                    if (context.hasNext())
                        writer.write(TAB);
                }
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        // write relations to file
        File fileEdges = new File(filename + ".edges");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileEdges));
            for (ONDEXRelation r : graph.getRelations()) {
                writer.write(String.valueOf(r.getId()));
                writer.write(TAB);
                writer.write(r.getOfType().getId());
                writer.write(TAB);
                writer.write(String.valueOf(r.getFromConcept().getId()));
                writer.write(TAB);
                writer.write(String.valueOf(r.getToConcept().getId()));
                writer.write(TAB);
                Iterator<ONDEXConcept> context = r.getTags().iterator();
                while (context.hasNext()) {
                    writer.write(String.valueOf(context.next().getId()));
                    if (context.hasNext())
                        writer.write(TAB);
                }
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

}
