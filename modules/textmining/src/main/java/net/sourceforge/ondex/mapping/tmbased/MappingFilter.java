package net.sourceforge.ondex.mapping.tmbased;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.mapping.tmbased.args.ArgumentNames;

/**
 * Definition of different filters for text mining mappings
 *
 * @author keywan
 */
public class MappingFilter {

    private ONDEXGraph graph;
    private String[] filters;

    private static final String LOWSCORE_FILTER = "lowscore";
    private static final String MAXSPECIFICITY_FILTER = "maxspecificity";
    private static final String BESTHITS_FILTER = "besthits";

    private static int numOfFilteredRelations = 0;

    /**
     * Standard constructor for initialisation. Obtains the list of filters from
     * ONDEXParameters file.
     *
     * @param graph AbstractONDEXGraph
     * @param ma    MappingArguments
     */
    public MappingFilter(ONDEXGraph graph, ONDEXPluginArguments ma) throws InvalidPluginArgumentException {
        this.graph = graph;
        this.filters = ((String) ma.getUniqueValue(ArgumentNames.FILTER_ARG))
                .split(",");

        String filterNames = (String) ma
                .getUniqueValue(ArgumentNames.FILTER_ARG);
        System.out.println("Filter text mining mappings: " + filterNames);
    }

    /**
     * filter a list of publication mappings
     *
     * @param pubMaps
     */
    public void filterMappings(Collection<PublicationMapping> pubMaps) {

        Iterator<PublicationMapping> pubIt = pubMaps.iterator();
        while (pubIt.hasNext()) {
            PublicationMapping pubMap = pubIt.next();
            filterMappings(pubMap);
        }

        System.out.println("Filtered text mining relations: "
                + getNumOfFilteredRelations());
    }

    /**
     * Delegates to certain filter methods that are specified in mapping
     * arguments. Uses predefined parameters: Depth->6, LowScore->0.3,
     * BestHits->6
     *
     * @param pubMap PublicationMapping
     */
    public void filterMappings(PublicationMapping pubMap) {

        for (String filter : filters) {
            if (filter.toLowerCase()
                    .equals(MAXSPECIFICITY_FILTER.toLowerCase())) {
                filterNonSpecificMappings(pubMap, 6);
            }
            if (filter.toLowerCase().equals(LOWSCORE_FILTER.toLowerCase())) {
                filterLowScore(pubMap, 0.3);
            }
            if (filter.toLowerCase().equals(BESTHITS_FILTER.toLowerCase())) {
                takeBestHitsPerConceptClass(pubMap, 3);
            }
        }
    }

    /**
     * Filters those Hits to a publication for which Hits with greater
     * specificity within the ontology hierarchy are present.
     *
     * @param pubMap   PublicationMapping
     * @param maxDepth depth for BreadthFirstSearch
     */
    public void filterNonSpecificMappings(PublicationMapping pubMap,
                                          int maxDepth) {

        HashMap<String, ArrayList<Hit>> results = pubMap.getHits();

        for (String cc : results.keySet()) {
            Iterator<Hit> hitIt = results.get(cc).iterator();
            HashSet<Hit> filter = new HashSet<Hit>();
            while (hitIt.hasNext()) {
                Hit hit = hitIt.next();

                // calculate distance of hit to other parent concepts
                BreadthFirstSearch bfs = new BreadthFirstSearch(graph);
                Map<Integer, Integer> distance = bfs.breadthFirstSearch(hit
                        .getHitConID(), maxDepth, false);

                Iterator<Integer> disIt = distance.keySet().iterator();
                while (disIt.hasNext()) {
                    int conID = disIt.next();
                    // check if pubMap contains parent concepts of hit (less
                    // specific)
                    if (pubMap.containsHit(cc, conID)) {
                        if (filter.add(pubMap.getHit(cc, conID))) {
                            numOfFilteredRelations++;
                        }
                    }
                }
            }

            // remove all less specific mappings
            pubMap.removeHits(cc, filter);
        }
    }

    /**
     * filter mappings with low score
     *
     * @param pubMap PublicationMapping
     * @return
     */
    public void filterLowScore(PublicationMapping pubMap, double minScore) {

        HashMap<String, ArrayList<Hit>> results = pubMap.getHits();

        for (String cc : results.keySet()) {
            Iterator<Hit> hitIt = results.get(cc).iterator();
            while (hitIt.hasNext()) {
                Hit hit = hitIt.next();
                if (hit.getScore() < minScore) {
                    hitIt.remove();
                    numOfFilteredRelations++;
                }
            }
        }
    }

    /**
     * Take only the best n mappings to each concept class. Sometimes one is
     * only interested to categorize the publication only into the best category
     * of every concept class.
     *
     * @param pubMap PublicationMapping
     * @return
     */
    public void takeBestHitsPerConceptClass(PublicationMapping pubMap, int n) {

        HashMap<String, ArrayList<Hit>> results = pubMap.getHits();

        for (String cc : results.keySet()) {

            Object[] sortedHits = results.get(cc).toArray();
            Arrays.sort(sortedHits);
            for (int i = 0; i < sortedHits.length; i++) {
                // take best n hits and remove rest from set
                if (i >= n) {
                    Hit hit = (Hit) sortedHits[i];
                    pubMap.removeHit(cc, hit);
                    numOfFilteredRelations++;
				}
			}
		}
	}

	public int getNumOfFilteredRelations() {
		return numOfFilteredRelations;
	}

}
