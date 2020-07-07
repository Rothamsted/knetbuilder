package net.sourceforge.ondex.transformer.rankedpathways;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sourceforge.ondex.algorithm.graphquery.FilterPaths;
import net.sourceforge.ondex.algorithm.graphquery.exceptions.UnrankableRouteException;
import net.sourceforge.ondex.algorithm.graphquery.nodepath.EvidencePathNode;
import net.sourceforge.ondex.algorithm.graphquery.pathrank.NumericalRank;

/**
 * @author hindlem
 */
public class FilterPathResults implements FilterPaths<EvidencePathNode> {

    private final Map<Integer, List<NumericalRank>> priorityOfRankIndex;
    private final Integer[] priorities;
    private final boolean includeUnrankable;
    private final boolean removeDuplicatePaths;


    /**
     * @param priorityOfRankIndex  the ranks
     * @param priorities           all priorities to account for in priorityOfRankIndex
     * @param includeUnrankable    includes paths that have no metrix for ranking
     * @param removeDuplicatePaths removes paths that have the same concept/relation list but different evidence
     */
    public FilterPathResults(
            Map<Integer, List<NumericalRank>> priorityOfRankIndex,
            Integer[] priorities, boolean includeUnrankable, boolean removeDuplicatePaths) {
        this.priorityOfRankIndex = priorityOfRankIndex;
        this.priorities = priorities;
        this.includeUnrankable = includeUnrankable;
        this.removeDuplicatePaths = removeDuplicatePaths;
        Arrays.sort(priorities);
    }

    @Override
    public List<EvidencePathNode> filterPaths(List<EvidencePathNode> paths) {

        List<EvidencePathNode> highestScoringRoutes = null;

        for (int relativePriority : priorities) {
            List<NumericalRank> ranksOfLevel = priorityOfRankIndex.get(relativePriority);

            //define the ranks we extract calculate for this level
            List<EvidencePathNode> highestScoringRoutesInRelativeRank = null;

            for (NumericalRank rank : ranksOfLevel) {
                List<EvidencePathNode> highestScoringRoutesforRank
                        = getHighestScoreingRoutesForRank(paths, rank);
                if (highestScoringRoutesInRelativeRank == null || highestScoringRoutesInRelativeRank.size() == 0) {
                    highestScoringRoutesInRelativeRank = highestScoringRoutesforRank;
                } else {
                    highestScoringRoutesInRelativeRank.addAll(highestScoringRoutesforRank);
                }
            }
            if (highestScoringRoutes == null || highestScoringRoutes.size() == 0) {
                highestScoringRoutes = highestScoringRoutesInRelativeRank;
            } else if (highestScoringRoutesInRelativeRank != null && highestScoringRoutesInRelativeRank.size() > 0) {
                highestScoringRoutesInRelativeRank.retainAll(highestScoringRoutes);
                if (highestScoringRoutesInRelativeRank.size() > 0) {
                    highestScoringRoutes = highestScoringRoutesInRelativeRank;
                }
            }
        }

        if (includeUnrankable) {
            if (highestScoringRoutes == null) {
                highestScoringRoutes = getUnrankablePaths(priorityOfRankIndex, paths);
            } else {
                highestScoringRoutes.addAll(getUnrankablePaths(priorityOfRankIndex, paths));
            }
        }

        if (removeDuplicatePaths) {
            List<EvidencePathNode> filteredRoutes = new ArrayList<EvidencePathNode>();
            for (EvidencePathNode node : highestScoringRoutes) {
                boolean exists = false;
                for (EvidencePathNode existing : filteredRoutes) {
                    if (existing.getLength() == node.getLength()
                            && existing.getEvidencesInPositionOrder().equals(node.getEvidencesInPositionOrder())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists)
                    filteredRoutes.add(node);
            }
            highestScoringRoutes = filteredRoutes;
        }

        return highestScoringRoutes;
    }

    /**
     * @param priorityOfRankIndex
     * @param routes
     * @return
     */
    private List<EvidencePathNode> getUnrankablePaths(
            Map<Integer, List<NumericalRank>> priorityOfRankIndex,
            List<EvidencePathNode> routes) {
        List<EvidencePathNode> unrankablePath = new ArrayList<EvidencePathNode>();
        for (EvidencePathNode route : routes)
            if (!isRankable(route, priorityOfRankIndex))
                unrankablePath.add(route);
        return unrankablePath;
    }

    /**
     * @param path
     * @param priorityOfRankIndex
     * @return
     */
    private boolean isRankable(EvidencePathNode path, Map<Integer, List<NumericalRank>> priorityOfRankIndex) {
        for (List<NumericalRank> ranks : priorityOfRankIndex.values()) {
            for (NumericalRank rank : ranks) {
                if (rank.containsRankableElements(path)) { //contains the right concepts
                    try {
                        rank.getComparativeValue(path);
                    } catch (UnrankableRouteException e) {
                        continue; //not rankable for this
                    }
                    return true; ///no need to continue we can rank this
                }
            }
        }
        return false;
    }

    /**
     * Finds the best scoring routes for this NumericalRank
     *
     * @param subRoutes the routes to be scores
     * @param rank      the rank to score on
     * @return the best scoring route/s
     */
    private List<EvidencePathNode> getHighestScoreingRoutesForRank(List<EvidencePathNode> subRoutes,
                                                                   NumericalRank rank) {

        Collections.sort(subRoutes, rank); //sort routes by this rank

        List<EvidencePathNode> highestScoringRoutesforRank = new ArrayList<EvidencePathNode>();
        double highestScoreForRank = 0;

        for (EvidencePathNode path : subRoutes) {

            if (!rank.containsRankableElements(path)) {
                break; //we can't rank on this route
            }

            double value;
            try {
                value = rank.getComparativeValue(path);
            } catch (UnrankableRouteException e) {
                break; //we can't rank on this route
            }

            if (highestScoringRoutesforRank.size() == 0 || value == highestScoreForRank) {
                //extend array and add
                highestScoreForRank = value;
                highestScoringRoutesforRank.add(path);
            } else if (!rank.isInvertedOrder() && value < highestScoreForRank) {
                break; //it's less as expected
            } else if (rank.isInvertedOrder() && value > highestScoreForRank) {
                break; //it's more as expected
            } else {
                //it's not as expected! rounding error probably
                if (Math.abs(value - highestScoreForRank) > 0.000001)
                    throw new RuntimeException("Rank is not ordered as expected " + value + " " + highestScoreForRank);
            }
        }

        return highestScoringRoutesforRank;
    }

}

