/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.ondex.stats;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.export.ONDEXExport;

/**
 * This statistic analyzes the combinations of accession namespaces that occurr
 * on the concepts of a given concept class.
 *
 * @author Jochen Weile
 */
public class AccessionClusters extends ONDEXExport {

    public static final String CC_ARG = "ConceptClass";
    public static final String CC_ARG_DESC = "Concept class to analyze";

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[] {
            new StringArgumentDefinition(CC_ARG, CC_ARG_DESC, true, "Thing", true),
            new FileArgumentDefinition(FileArgumentDefinition.EXPORT_FILE,
                    FileArgumentDefinition.EXPORT_FILE_DESC, true, false, false)
        };
    }

    @Override
    public void start() throws Exception {

        File outFile = new File((String) getArguments()
                .getUniqueValue(FileArgumentDefinition.EXPORT_FILE));

        OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));

        OutputStreamWriter w = new OutputStreamWriter(out);

        List<ConceptClass>targetCCs = new ArrayList<ConceptClass>();
        for (String ccId : getArguments().getObjectValueList(CC_ARG, String.class)) {
            targetCCs.add(requireConceptClass(ccId));
        }

        for (ConceptClass cc : targetCCs) {
            w.write("Concept class "+cc.getFullname()+"\n");
            analyze(graph.getConceptsOfConceptClass(cc), w);
        }

        out.close();

    }

    private void analyze(Set<ONDEXConcept> concepts, OutputStreamWriter w) throws IOException {
        Map<String,Integer> clusters = new HashMap<String, Integer>();

        Map<String,Set<String>> singletons = new HashMap<String, Set<String>>();

        for (ONDEXConcept concept : concepts) {

            if (concept.getConceptAccessions().size() == 1) {
                ConceptAccession accession = concept.getConceptAccessions().iterator().next();
                String namespace = accession.getElementOf().getId();
                Set<String> singletonSet = singletons.get(namespace);
                if (singletonSet == null) {
                    singletonSet = new TreeSet<String>();
                    singletons.put(namespace,singletonSet);
                }
                singletonSet.add(accession.getAccession());
            }

            TreeSet<String> signatureSet = new TreeSet<String>();
            for (ConceptAccession accession : concept.getConceptAccessions()) {
                signatureSet.add(accession.getElementOf().getId());
            }
            String signature = signatureSet.toString();

            Integer count = clusters.get(signature);
            count = count == null ? 1 : count+1;
            clusters.put(signature, count);
        }

        printClusters(clusters, w);
        printSingletons(singletons, w);
    }

    private void printClusters(Map<String, Integer> clusters, OutputStreamWriter w) throws IOException {
        StringBuilder output = new StringBuilder("\n");
        for (Map.Entry<String,Integer> entry : clusters.entrySet()) {
            output.append(entry.getKey()).append("\t")
                    .append(entry.getValue()).append("\n");
        }
        w.write(output.toString());
    }

    private void printSingletons(Map<String, Set<String>> singletons, OutputStreamWriter w) throws IOException {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String,Set<String>> entry : singletons.entrySet()) {
            b.append("Singletons of namespace ").append(entry.getKey())
                    .append(": ").append(entry.getValue()).append("\n");
        }
        w.write(b.toString());
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
    public String getId() {
        return "ncl_acc_clust";
    }

    @Override
    public String getName() {
        return "Newcastle Accession Cluster Statistic";
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

//    private void log(String string) {
//        GeneralOutputEvent e = new GeneralOutputEvent(string, "");
//        e.setLog4jLevel(Level.INFO);
//        fireEventOccurred(e);
//    }


}
