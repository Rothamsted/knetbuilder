/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.nclondexexpression.tools;


import java.util.ArrayList;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;

/**
 *
 * @author jweile
 */
public class PathTemplate {

    private ONDEXGraph graph;


    private MDIndex mdindex;

    private ConceptClass[] ccs;
    private RelationType[] rts;
    
    private String motif = null;

    public PathTemplate(String descriptor, ONDEXGraph graph) throws MalformedPathException {
        this.graph = graph;
        mdindex = new MDIndex(graph);
        motif = descriptor;
        
        parsePath(descriptor);
    }

    public ConceptClass[] getCcs() {
        return ccs;
    }

    public RelationType[] getRts() {
        return rts;
    }
    
    public String getMotif()
    {
        return motif;
    }



    /**
     * parses a given path descriptor into concept class and relation type arrays.
     * @param s
     */
    private void parsePath(String s) throws MalformedPathException {
            String[] fields = s.split(" ");
            ArrayList<String> ccVec = new ArrayList<String>(),
                              rtVec = new ArrayList<String>();
            for (int i = 0; i < fields.length; i++) {
                boolean cc = (i % 2) == 0;
                if (cc) {
                        ccVec.add(fields[i]);
                } else {
                        rtVec.add(fields[i]);
                }
            }

            if (ccVec.size() != rtVec.size() + 1) {
                    throw new MalformedPathException("No valid path.");
            }
            if (!ccVec.get(0).equals(ccVec.get(ccVec.size() -1))) {
                    throw new MalformedPathException("Path is not circular.");
            }

            StringBuilder errors = new StringBuilder("");

            ccs = new ConceptClass[ccVec.size()];
            for (int i = 0; i < ccVec.size(); i++) {
                String ccId = ccVec.get(i);
                ConceptClass cc = mdindex.resolveConceptClass(ccId);
                if (cc != null) {
                    ccs[i] = cc;
                } else {
                    errors.append(ccId+" ");
                }
            }

            rts = new RelationType[rtVec.size()];
            for (int i = 0; i < rtVec.size(); i++) {
                String rtId = rtVec.get(i);
                RelationType rt = mdindex.resolveRelationType(rtId);
                if (rt != null) {
                    rts[i] = rt;
                } else {
                    errors.append(rtId+" ");
                }
            }

            String unmatched = errors.toString().trim();
            if (unmatched.length() > 0) {
                throw new MalformedPathException("Unmatched: "+unmatched);
            }

    }

}
