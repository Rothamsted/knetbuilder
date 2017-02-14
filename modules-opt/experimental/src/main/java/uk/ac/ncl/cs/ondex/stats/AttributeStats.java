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
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.export.ONDEXExport;
import uk.ac.ncl.cs.ondex.stats.datastructures.RelationTypeTree;
import uk.ac.ncl.cs.ondex.stats.datastructures.Tree;

/**
 * This statistic analyzes the attribute coverage for all concept classes and
 * relation types.
 * 
 * @author Jochen Weile
 */
public class AttributeStats extends ONDEXExport {


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

       analyzeRelationTypes(out);

       out.close();

    }

    private void analyzeConceptClasses(OutputStream out) throws IOException {

        PropertyMaps maps = new PropertyMaps();

        indexMetadata(graph.getConcepts(), maps);

        Tree tree = new ConceptClassTree(graph, maps.keySet()) {
            @Override
            public String retrieveFullName(String id) {
                return graph.getMetaData().getAttributeName(id).getFullname();
            }
        };

        tree.printStats(maps, out);
        
    }
    
    private void analyzeRelationTypes(OutputStream out) throws IOException {

        PropertyMaps maps = new PropertyMaps();

        indexMetadata(graph.getRelations(), maps);

        Tree tree = new RelationTypeTree(graph, maps.keySet()) {
            @Override
            public String retrieveFullName(String id) {
                return graph.getMetaData().getAttributeName(id).getFullname();
            }
        };

        tree.printStats(maps, out);

    }

    private void indexMetadata(Set<? extends ONDEXEntity> es, PropertyMaps maps) {

        for (ONDEXEntity e : es) {

            MetaData type = e instanceof ONDEXConcept ?
                            ((ONDEXConcept)e).getOfType() :
                            ((ONDEXRelation)e).getOfType();

            for (Map<String,Integer> map : maps.getMapsHierarchyAware(type)) {

                PropertyMaps.increment(map, PropertyMaps.KEY_COUNT);

                Set<String> antidup = new HashSet<String>();

                for (Attribute attribute : e.getAttributes()) {
                    String aname = attribute.getOfType().getId();
                    if (!antidup.contains(aname)) {
                        PropertyMaps.increment(map, aname);
                        antidup.add(aname);
                    }
                }
            }

        }
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
        return "attrubutestats";
    }

    @Override
    public String getName() {
        return "Ondex Attribute Statistics";
    }

    @Override
    public String getVersion() {
        return "0.0.1";
    }

}
