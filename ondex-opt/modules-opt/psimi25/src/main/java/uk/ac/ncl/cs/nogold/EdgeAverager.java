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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Average over edge existence.
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class EdgeAverager {

    /**
     * Counts edges
     */
    private Map<SetOfTwo<Integer>,Integer> edgeCounts = new HashMap<SetOfTwo<Integer>, Integer>();

    /**
     * number of samples taken
     */
    private int numSamples = 0;

    /**
     * updates the counts
     * @param edges set of currently existing edges
     */
    public void update(Set<SetOfTwo<Integer>> edges) {
        for (SetOfTwo<Integer> e : edges) {
            Integer count = edgeCounts.get(e);
            if (count == null) {
                count = 0;
            }
            edgeCounts.put(e,count+1);
        }
        numSamples++;
    }

    /**
     * computes the averages
     * @return a map linking each edge to its existence average
     */
    public Map<SetOfTwo<Integer>,Double> getEdgeAverages() {

        Map<SetOfTwo<Integer>,Double> averages = new HashMap<SetOfTwo<Integer>, Double>();

        double numSamplesD = (double)numSamples;

        for (Map.Entry<SetOfTwo<Integer>,Integer> entry : edgeCounts.entrySet()) {
            averages.put(entry.getKey(), (double)entry.getValue() / numSamplesD);
        }

        return averages;
    }

    public int getNumSamples() {
        return numSamples;
    }

    

}
