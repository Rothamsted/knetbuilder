package net.sourceforge.ondex.mapping.pfam2go;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.*;
import net.sourceforge.ondex.mapping.ONDEXMapping;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/**
 * Parses the pfam2go mapping from a given file.
 *
 * @author taubertj
 */
@Status(description = "Tested September 2013 (Artem Lysenko)", status = StatusType.STABLE)
public class Mapping extends ONDEXMapping implements ArgumentNames {

    /**
     * Constructor
     */
    public Mapping() {
    }

    @Override
    public void start() throws InvalidPluginArgumentException {

        String filename = (String) args.getUniqueValue(INPUT_FILE_ARG);
        fireEventOccurred(new GeneralOutputEvent("Using Pfam2GO file "
                + filename + ".", "[Mapping - setONDEXGraph]"));

        // RTset: equ
        RelationType rtset = graph.getMetaData()
                .getRelationType(MetaData.relType);
        if (rtset == null)
            fireEventOccurred(new RelationTypeMissingEvent(MetaData.relType,
                    "[Mapping - setONDEXGraph]"));

        // ET: EXTERNAL2GO
        EvidenceType et = graph.getMetaData().getEvidenceType(
                MetaData.evidence);
        if (et == null)
            fireEventOccurred(new EvidenceTypeMissingEvent(MetaData.evidence,
                    "[Mapping - setONDEXGraph]"));

        // DataSource: PFAM
        DataSource dataSourcePFAM = graph.getMetaData().getDataSource(
                MetaData.CV_Pfam);
        if (dataSourcePFAM == null)
            fireEventOccurred(new DataSourceMissingEvent(
                    MetaData.CV_Pfam,
                    "[Mapping - setONDEXGraph]"));

        // DataSource: GO
        DataSource dataSourceGO = graph.getMetaData().getDataSource(
                net.sourceforge.ondex.parser.go.MetaData.cvGO);
        if (dataSourceGO == null)
            fireEventOccurred(new DataSourceMissingEvent(
                    net.sourceforge.ondex.parser.go.MetaData.cvGO,
                    "[Mapping - setONDEXGraph]"));

        Hashtable<String, List<String>> input = new Hashtable<String, List<String>>();

        try {
            // read in mapping file
            BufferedReader reader = new BufferedReader(new FileReader(filename));

            while (reader.ready()) {
                String line = reader.readLine();
                if (!line.startsWith("!")) {
                    String pfam = line.substring(5, line.indexOf(" "));
                    // handle some of the mistakes in ec2go file
                    String go = line.substring(line.length() - 10, line
                            .length());
                    if (!input.containsKey(pfam))
                        input.put(pfam, new ArrayList<String>());
                    if (!input.containsKey(go))
                        input.put(go, new ArrayList<String>());
                    input.get(pfam).add(go);
                    input.get(go).add(pfam);
                }
            }

            reader.close();
        } catch (FileNotFoundException fnfe) {
            fireEventOccurred(new DataFileMissingEvent("File " + filename
                    + " not found.", "[Mapping - setONDEXGraph]"));
        } catch (IOException ioe) {
            fireEventOccurred(new DataFileErrorEvent("Error reading file "
                    + filename + ".", "[Mapping - setONDEXGraph]"));
        }

        Hashtable<String, ONDEXConcept> mapping = new Hashtable<String, ONDEXConcept>();

        // get concepts of PFAM and GO
        Set<ONDEXConcept> viewPFAM = graph.getConceptsOfDataSource(dataSourcePFAM);
        Set<ONDEXConcept> viewGO = graph.getConceptsOfDataSource(dataSourceGO);

        // map of pid to ONDEXConcept
        for (ONDEXConcept c : viewPFAM) {
            if (input.containsKey(c.getPID()))
                mapping.put(c.getPID(), c);
        }
        for (ONDEXConcept c : viewGO) {
            if (input.containsKey(c.getPID()))
                mapping.put(c.getPID(), c);
        }

        // create Relations
        for (String from : mapping.keySet()) {
            ONDEXConcept fromConcept = mapping.get(from);
            List<String> to = input.get(from);
            for (String toS : to) {
                ONDEXConcept toConcept = mapping.get(toS);
                if (toConcept == null) {
                    fireEventOccurred(new InconsistencyEvent(
                            "ToConcept for mapping " + from + " = " + toS
                                    + " not present in graph.",
                            "[Mapping - setONDEXGraph]"));
                }
                else {
                    graph.getFactory().createRelation(fromConcept, toConcept, rtset, et);
                }
            }
        }
    }

    /**
     * Returns name of this mapping.
     *
     * @return String
     */
    public String getName() {
        return "PFAM2GO";
    }

    /**
     * Returns version of this mapping.
     *
     * @return String
     */
    public String getVersion() {
        return "14.04.08";
    }

    @Override
    public String getId() {
        return "pfam2go";
    }

    /**
     * Returns the input file ArgumentDefinition of this mapping.
     *
     * @return ArgumentDefinition<?>[]
     */
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        StringArgumentDefinition filenameARG = new StringArgumentDefinition(
                ArgumentNames.INPUT_FILE_ARG,
                ArgumentNames.INPUT_FILE_ARG_DESC, true, null, false);
        return new ArgumentDefinition<?>[]{filenameARG};
    }

    /**
     * No IndexONDEXGraph is required.
     *
     * @return false
     */
    public boolean requiresIndexedGraph() {
        return false;
    }

    public String[] requiresValidators() {
        return new String[0];
    }
}
