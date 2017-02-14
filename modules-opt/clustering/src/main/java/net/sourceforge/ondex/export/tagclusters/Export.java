package net.sourceforge.ondex.export.tagclusters;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.export.ONDEXExport;

import java.io.*;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * Identifies cluster within the graph and exports them in a spreadsheet manner.
 *
 * @author taubertj
 * @version 13.03.2009
 */
@Authors(authors = {"Jan Taubert"}, emails = {"jantaubert at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Export extends ONDEXExport implements ArgumentNames {

    AttributeName[] anGDS = null;

    ConceptClass classContext = null;

    boolean exclusive = false;

    Map<ONDEXConcept, Collection<ONDEXConcept>> seeds = new Hashtable<ONDEXConcept, Collection<ONDEXConcept>>();

    /**
     * Returns requirement for a filename argument.
     *
     * @return ArgumentDefinition<?>[]
     */
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        StringArgumentDefinition gdsArg = new StringArgumentDefinition(ATTRIBUTE_ARG,
                ATTRIBUTE_ARG_DESC, true, null, true);
        StringArgumentDefinition contextArg = new StringArgumentDefinition(
                TAG_ARG, TAG_ARG_DESC, true, null, false);
        BooleanArgumentDefinition exclusiveArg = new BooleanArgumentDefinition(
                EXCLUSIVE_ARG, EXCLUSIVE_ARG_DESC, false, false);
        FileArgumentDefinition exportFile = new FileArgumentDefinition(FileArgumentDefinition.EXPORT_FILE,
                FileArgumentDefinition.EXPORT_FILE_DESC, true, false, true, false);
        return new ArgumentDefinition<?>[]{gdsArg, contextArg, exclusiveArg, exportFile};
    }

    /**
     * Returns name of this export.
     *
     * @return String
     */
    public String getName() {
        return "Context Cluster Export";
    }

    /**
     * Returns version of this export.
     *
     * @return String
     */
    public String getVersion() {
        return "13.03.2009";
    }

    @Override
    public String getId() {
        return "tagclusters";
    }


    /**
     * Does not require an indexed graph.
     *
     * @return false
     */
    public boolean requiresIndexedGraph() {
        return false;
    }

    /**
     * Starts the export process and writes results to a file.
     */
    public void start() throws InvalidPluginArgumentException {

        // get AttributeNames to include into output
        if (args.getObjectValueArray(ATTRIBUTE_ARG).length > 0) {
            int i = 0;
            anGDS = new AttributeName[args.getObjectValueArray(ATTRIBUTE_ARG).length];
            for (Object o : args.getObjectValueArray(ATTRIBUTE_ARG)) {
                anGDS[i] = graph.getMetaData().getAttributeName(
                        o.toString());
                i++;
                if (anGDS == null) {
                    fireEventOccurred(new AttributeNameMissingEvent(
                            "Given AttributeName not found. " + o.toString(),
                            "[Export - start]"));
                    return;
                }
            }
        }

        // get class for context
        classContext = graph.getMetaData().getConceptClass(
                args.getUniqueValue(TAG_ARG).toString());
        if (classContext == null) {
            fireEventOccurred(new ConceptClassMissingEvent(
                    "Given ConceptClass not found. "
                            + args.getUniqueValue(TAG_ARG).toString(),
                    "[Export - start]"));
            return;
        }

        // Attribute exclusive mode
        if (args.getUniqueValue(EXCLUSIVE_ARG) != null)
            exclusive = (Boolean) args.getUniqueValue(EXCLUSIVE_ARG);

        // iterate over possible seed nodes
        for (ONDEXConcept root : graph.getConceptsOfConceptClass(classContext)) {
            seeds.put(root, graph.getConceptsOfTag(root));
        }

        // get output file
        File file = new File((String) args.getUniqueValue(FileArgumentDefinition.EXPORT_FILE));

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            for (ONDEXConcept key : seeds.keySet()) {
                Collection<ONDEXConcept> cluster = seeds.get(key);

                // only output clusters with at least 2 nodes
                if (cluster.size() > 1) {

                    // seed node record
                    writer.append("Cluster for context:");
                    outputConceptInfo(writer, key);

                    // display all nodes of the cluster as well
                    for (ONDEXConcept c : cluster) {
                        if (!c.equals(key)) {

                            // check for Attribute exclusive mode
                            boolean match = false;
                            if (exclusive && anGDS != null) {
                                for (AttributeName an : anGDS) {
                                    if (c.getAttribute(an) != null) {
                                        match = true;
                                        break;
                                    }
                                }
                            }

                            // in non exclusive mode or is match
                            if (!exclusive || match) {
                                outputConceptInfo(writer, c);
                            }
                        }
                    }
                    writer.append("\n");
                }
            }

            writer.flush();
            writer.close();
        } catch (FileNotFoundException fnfe) {
            this.fireEventOccurred(new DataFileMissingEvent(fnfe.getMessage(),
                    "[Export - start]"));

        } catch (IOException ioe) {
            this.fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
                    "[Export - start]"));
        }

    }

    /**
     * Constructs information about concepts.
     *
     * @param writer  BufferedWriter
     * @param concept ONDEXConcept
     * @throws IOException
     */
    private void outputConceptInfo(BufferedWriter writer, ONDEXConcept concept)
            throws IOException {

        // ONDEX id first
        writer.append("\t").append(String.valueOf(concept.getId())).append("\t");

        // output concept names
        Iterator<ConceptName> it_cn = concept.getConceptNames().iterator();
        writer.append("[");
        if(it_cn.hasNext()) {
            ConceptName cn = it_cn.next();
            writer.append(cn.getName());
        }
        while (it_cn.hasNext()) {
            writer.append(", ");
            ConceptName cn = it_cn.next();
            writer.append(cn.getName());
        }
        writer.append("]\t");

        // output concept accessions
        Iterator<ConceptAccession> it_ca = concept.getConceptAccessions().iterator();
        if (it_ca.hasNext()) {
            writer.append("{");
            while (it_ca.hasNext()) {
                ConceptAccession ca = it_ca.next();
                writer.append(" ").append(ca.getAccession()).append(" (").append(ca.getElementOf().getId()).append(")");
            }
            writer.append("}");
        }

        // possible Attribute output
        if (anGDS != null) {
            for (AttributeName an : anGDS) {
                writer.append("\t");
                Attribute attribute = concept.getAttribute(an);
                if (attribute != null) {
                    writer.append(attribute.getValue().toString());
                }
            }
        }

        writer.append("\n");
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

}
