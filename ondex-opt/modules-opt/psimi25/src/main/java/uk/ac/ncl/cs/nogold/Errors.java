/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.nogold;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class Errors {

    int tp,fp,fn,tn;

    public Errors(Set<SetOfTwo<Integer>> background, Network foreground) {
        //extract true edges for given subgraph
        Set<SetOfTwo<Integer>> realEdges = new HashSet<SetOfTwo<Integer>>();
        for (SetOfTwo<Integer> e : background) {
            if (foreground.contains(e.getA()) && foreground.contains(e.getB())) {
                realEdges.add(e);
            }
        }


        int n = foreground.getNodes().size();
        int all = (n*n - n) / 2;
        int real = realEdges.size();
        int nreal = all - real;

        Set<SetOfTwo<Integer>> set = new HashSet<SetOfTwo<Integer>>(foreground.getEdges());
        int posEdges = set.size();

        set.retainAll(realEdges);
        tp = set.size();
        fp = posEdges - tp;
        fn = real - tp;
        tn = nreal - fp;
    }

    public int getFn() {
        return fn;
    }

    public int getFp() {
        return fp;
    }

    public int getTn() {
        return tn;
    }

    public int getTp() {
        return tp;
    }

    
}
