package net.sourceforge.ondex.parser.atregnet;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.parser.ONDEXParser;

import java.io.*;
import java.util.Hashtable;
import java.util.Map;

import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createAttName;

/**
 * Parses AtRegNet files
 *
 * @author lysenkoa
 * @version 24.05.2008
 */
public class Parser extends ONDEXParser implements MetaData {

    /**
     * No validators required.
     *
     * @return String[]
     */
    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    /**
     * Returns long list of ArgumentDefinitions to facilitate parsing of tabular
     * file.
     *
     * @return ArguementDefinition<?>[]
     */
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
                        FileArgumentDefinition.INPUT_FILE_DESC, true, true, false, false)
        };
    }

    /**
     * Returns name of this parser.
     *
     * @return name of parser
     */
    public String getName() {
        return "AtRegNet Parser (Expanded version)";
    }

    /**
     * Returns version of this parser.
     *
     * @return version of parser
     */
    public String getVersion() {
        return "24.05.2008";
    }

    /**
     * Does the actual parsing process.
     */
    public void start() throws InvalidPluginArgumentException {
        // get user defined meta data
        RelationType rgby = graph.getMetaData().getRelationType(rg_by);
        RelationType reby = graph.getMetaData().getRelationType(re_by);
        RelationType acby = graph.getMetaData().getRelationType(ac_by);
        ConceptClass cc = graph.getMetaData().getConceptClass(protein);
        DataSource dataSource = graph.getMetaData().getDataSource(at_cv);
        DataSource tairdataSource = graph.getMetaData().getDataSource(tair_cv);
        EvidenceType eviType = graph.getMetaData().getEvidenceType(evidence);
        AttributeName taxidAn = graph.getMetaData().getAttributeName(taxidAttr);
        AttributeName isTf = createAttName(graph, tfAttr, String.class);
        AttributeName TFFamily = createAttName(graph, TFFamilyAttr, String.class);
        AttributeName nlm = createAttName(graph, NLM, String.class);

        File file = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE));

        Map<String, ONDEXConcept> concepts = new Hashtable<String, ONDEXConcept>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            int i = 1;
            while (reader.ready()) {
                String current = reader.readLine();
                String[] line = current.split("\t");

                String fromId = line[1];
                String additionalIdsFrom[] = fromId.split("/");
                if (additionalIdsFrom.length > 1)
                    fromId = additionalIdsFrom[0];
                fromId = fromId.trim().toUpperCase();
                ONDEXConcept from = concepts.get(fromId);
                if (from == null) {
                    from = graph.getFactory().createConcept(fromId, dataSource, cc, eviType);
                    for (String id : additionalIdsFrom) {
                        from.createConceptAccession(id.trim().toUpperCase(), tairdataSource, false);
                        concepts.put(id.trim().toUpperCase(), from);
                    }

                    if (line[0] != null) from.createConceptName(line[0], true);
                    from.createAttribute(taxidAn, String.valueOf(3702), false);
                    from.createAttribute(isTf, "Yes", false);
                    from.createAttribute(TFFamily, line[2].trim(), false);
                }

                String toId = line[4];
                String additionalIdsTo[] = toId.split("/");
                if (additionalIdsTo.length > 1)
                    toId = additionalIdsTo[0];
                toId = toId.trim().toUpperCase();
                ONDEXConcept to = concepts.get(toId);
                if (to == null) {
                    to = graph.getFactory().createConcept(toId, dataSource, cc, eviType);
                    for (String id : additionalIdsTo) {
                        from.createConceptAccession(id.trim().toUpperCase(), tairdataSource, false);
                        concepts.put(id.trim().toUpperCase(), to);
                    }

                    if (line[3] != null) to.createConceptName(line[3].trim(), true);
                    to.createAttribute(taxidAn, String.valueOf(3702), false);
                    if (!line[3].trim().equalsIgnoreCase("No"))
                        to.createAttribute(isTf, line[3], false);
                }
                ONDEXRelation r = null;
                if (line[9].equals("Activation")) {
                    r = graph.getFactory().createRelation(to, from, acby, eviType);
                } else if (line[9].equals("Repression")) {
                    r = graph.getFactory().createRelation(to, from, reby, eviType);
                } else if (line[9].equals("Unknown")) {
                    r = graph.getFactory().createRelation(to, from, rgby, eviType);
                } else {
                    System.err.println("Row: " + i + " Bad data: " + line[9]);
                }
                if (r != null && line.length > 12 && !line[12].trim().equals("NA")) {
                    r.createAttribute(nlm, line[12].trim(), false);
                }
            }
            i++;

            reader.close();
        } catch (FileNotFoundException fnfe) {
            fireEventOccurred(new DataFileMissingEvent(fnfe.getMessage(), "[Parser - start]"));
        } catch (IOException ioe) {
            fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(), "[Parser - start]"));
        }
    }

    @Override
    public String getId() {
        return "atregnet";
    }
}
