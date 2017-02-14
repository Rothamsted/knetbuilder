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
import uk.ac.ncl.cs.ondex.stats.datastructures.ConceptClassTree;
import uk.ac.ncl.cs.ondex.stats.datastructures.PropertyMaps;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.export.ONDEXExport;

/**
 * This statistic analyzes the accession coverage for all concept classes.
 * @author jweile
 */
public class AccessionStats extends ONDEXExport {

    /**
     * see supertype.
     */
    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[] {
            new FileArgumentDefinition(FileArgumentDefinition.EXPORT_FILE,
                    FileArgumentDefinition.EXPORT_FILE_DESC, true, false, false)
        };
    }

    /**
     * Starts the stats computation.
     * @throws Exception
     */
    @Override
    public void start() throws Exception {

        File outFile = new File((String) getArguments()
                .getUniqueValue(FileArgumentDefinition.EXPORT_FILE));

        OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));

        analyzeConceptClasses(out);

        out.close();
    }

    private void analyzeConceptClasses(OutputStream out) throws IOException {
        
        PropertyMaps maps = new PropertyMaps();

        for (ONDEXConcept c : graph.getConcepts()) {

            for (Map<String,Integer> map : maps.getMapsHierarchyAware(c.getOfType())) {
                PropertyMaps.increment(map, PropertyMaps.KEY_COUNT);

                Set<String> antidup = new HashSet<String>();

                for (ConceptAccession acc : c.getConceptAccessions()) {
                    String accId = acc.getElementOf().getId();
                    if (!antidup.contains(accId)) {
                        PropertyMaps.increment(map, accId);
                        antidup.add(accId);
                    }
                }
            }

        }

        ConceptClassTree tree = new ConceptClassTree(graph, maps.keySet()) {
            @Override
            public String retrieveFullName(String id) {
                return graph.getMetaData().getDataSource(id).getFullname();
            }
        };

        tree.printStats(maps, out);

    }
    

    @Override
    public String getId() {
        return "accstats";
    }

    @Override
    public String getName() {
        return "Ondex Accession Statistics";
    }

    @Override
    public String getVersion() {
        return "0.0.1";
    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }



}
