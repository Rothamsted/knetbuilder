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

/**
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class IncrementalAverage {

    private double average;

    private double i;

    public IncrementalAverage(double initialValue) {
        average = initialValue;
        i = 1;
    }

    public void updateWith(double value) {
        double n = (double)i;
        average = n/(n+1) * average + value / (n+1);
        i++;
    }

    public double getAverage() {
        return average;
    }

    

}
