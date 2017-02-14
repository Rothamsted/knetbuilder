/*
 *  Copyright (C) 2011 Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ncl.cs.nogold;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Records the burn-in phase of the MCMC method and
 * analyzes the autocorrelation to determine the best thinning factor.
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class BurnIn {

    private static final int FPR_OFFSET = 0;
    private static final int FNR_OFFSET = 1;

    /**
     * Determines the record indices associated with each graph.
     */
    private Map<Network,Integer> graphIndices;

    /**
     * Matrix storing the error rates for each cycle.
     * records[rateIndex][cycleIndex]
     */
    private double[][] records;

    /**
     * points to the next record. strictly internal use only.
     */
    private int recordPointer = 0;

    /**
     * the autocorrelation lag to use
     */
    private int lag;

    /**
     * Autocorrelation wrt given lag must be smaller than this threshold
     * for all rates to accept current thinning rate.
     */
    private double acThreshold;

    /**
     * Constructs a new BurnIn instance.
     * @param size number of burn-in cycles.
     * @param numGraphs number of experimental graphs
     * @param acLag autocorrelation lag to use
     * @param acThreshold autocorrelation threshold (determines thinning).
     */
    public BurnIn(int size, int numGraphs, int acLag, double acThreshold) {
        records = new double[numGraphs*2][size];
        this.lag = acLag;
        this.acThreshold = acThreshold;
    }

    /**
     * records a new set of error rates
     * @param rates the rates to record.
     * @return whether the burn-in has completed.
     */
    public boolean record(Map<Network,ErrorRates> rates) {

        if (recordPointer < records[0].length) {

            lazyInitGraphIndexMap(rates.keySet());

            for (Network g : rates.keySet()) {

                int index_base = graphIndices.get(g) * 2;

                records[index_base+FPR_OFFSET][recordPointer] =
                        rates.get(g).getFalsePositiveRate();
                records[index_base+FNR_OFFSET][recordPointer] =
                        rates.get(g).getFalseNegativeRate();

            }

            recordPointer++;

            return false;

        } else {
            return true;
        }

    }

    /**
     * computes the optimal thinning factor based on the burn-in records.
     * @param maxSkip maximal thinning factor to test for. Return value will not
     * be larger than this.
     * @return the thinning factor (number of cycles to skip when recording values)
     */
    public int computeThinningFactor(int maxSkip) {

        nextSkip:for (int skip = 1; skip < maxSkip; skip++) {

            for (int seriesIndex = 0; seriesIndex < records.length; seriesIndex++) {

                DoubleArrayList subseries = new DoubleArrayList();
                for (int i = 0; i < records[seriesIndex].length; i += skip) {
                    subseries.add(records[seriesIndex][i]);
                }

                double mean = Descriptive.mean(subseries);
                double var = Descriptive.moment(subseries,2,mean);
                double autoCorrelation = Descriptive
                        .autoCorrelation(subseries, lag, mean, var);

                if (autoCorrelation > acThreshold) {
                    continue nextSkip;
                }

            }
            //only reached if "continue nextSkip" hasn't been called
            return skip;
        }

        //only reached if no lower skip was found
        return maxSkip;
    }

    /**
     * initializes the graph index map if necessary.
     * @param graphs set of experimental graphs
     */
    private void lazyInitGraphIndexMap(Set<Network> graphs) {

        if (graphIndices == null) {
            graphIndices = new HashMap<Network,Integer>(graphs.size());
            int i = 0;
            for (Network g : graphs) {
                graphIndices.put(g,i++);
            }
        }
    }


}
