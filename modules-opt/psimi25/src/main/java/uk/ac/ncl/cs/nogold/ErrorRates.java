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

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class ErrorRates {
    private double fpr;
    private double fnr;

    public ErrorRates(double fpr, double fnr) {
        this.fpr = fpr;
        this.fnr = fnr;
    }

    public double getFalseNegativeRate() {
        return fnr;
    }

    public double getFalsePositiveRate() {
        return fpr;
    }

    private double lambdaNeg = Double.NaN, lambdaPos = Double.NaN;

    public double getLogPositiveBayesFactor() {
        if (Double.isNaN(lambdaPos)) {
            lambdaPos = Math.log((1.0 - fnr) / fpr);
        }
        return lambdaPos;
    }

    public double getLogNegativeBayesFactor() {
        if (Double.isNaN(lambdaNeg)) {
            lambdaNeg = Math.log(fnr / (1.0 - fpr));
        }
        return lambdaNeg;
    }
    
}
