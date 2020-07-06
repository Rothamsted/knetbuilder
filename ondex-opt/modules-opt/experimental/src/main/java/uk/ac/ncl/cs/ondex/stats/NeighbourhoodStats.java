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
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.exception.type.ConceptClassMissingException;
import net.sourceforge.ondex.export.ONDEXExport;
import uk.ac.ncl.cs.ondex.stats.datastructures.Neighbour;

/**
 * Given two concept classes A and B, this statistic creates a histogram of
 * how many neighbours of type B there are for nodes of type A.
 *
 * @author Jochen Weile, M.Sc.
 */
public class NeighbourhoodStats extends ONDEXExport {

    public static final String TARGETCC_ARG = "TargetCC";
    public static final String TARGETCC_ARG_DESC = "ID of the target concept class.";
    public static final String NEIGHBOURCC_ARG = "NeighbourCC";
    public static final String NEIGHBOURCC_ARG_DESC = "ID of the neighbouring concept class.";

    private ConceptClass targetCC, neighbourCC;

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[] {
            new StringArgumentDefinition(TARGETCC_ARG, TARGETCC_ARG_DESC, true, "Thing", false),
            new StringArgumentDefinition(NEIGHBOURCC_ARG, NEIGHBOURCC_ARG_DESC, true, "Thing", false),
            new FileArgumentDefinition(FileArgumentDefinition.EXPORT_FILE,
                    FileArgumentDefinition.EXPORT_FILE_DESC, true, false, false)
        };
    }

    private void fetchArgs() throws InvalidPluginArgumentException, ConceptClassMissingException {

        String targetCcId = (String)getArguments().getUniqueValue(TARGETCC_ARG);
        if (targetCcId != null && targetCcId.length() > 0) {
            targetCC = requireConceptClass(targetCcId);
        } else {
            throw new InvalidPluginArgumentException(targetCcId);
        }

        String neighbourCcId = (String)getArguments().getUniqueValue(NEIGHBOURCC_ARG);
        if (neighbourCcId != null && neighbourCcId.length() > 0) {
            neighbourCC = requireConceptClass(neighbourCcId);
        } else {
            throw new InvalidPluginArgumentException(neighbourCcId);
        }
        
    }

    /**
     * returns a <code>Set</code> containing the concepts
     * of <code>ConceptClass cc</code>.
     *
     * @param cc The concerned <code>ConceptClass</code>.
     */
    private Set<ONDEXConcept> conceptsOfCC(ConceptClass cc) {
        Set<ONDEXConcept> set = new HashSet<ONDEXConcept>();

        for (ONDEXConcept c : graph.getConcepts()) {
            if (c.inheritedFrom(cc)) {
                set.add(c);
            }
        }

        return set;
    }

    @Override
    public void start() throws Exception {

        File outFile = new File((String) getArguments()
                .getUniqueValue(FileArgumentDefinition.EXPORT_FILE));
        OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));
        OutputStreamWriter w = new OutputStreamWriter(out);

        fetchArgs();

        Histogram histo = new Histogram();

        for (ONDEXConcept c : conceptsOfCC(targetCC)) {
            int freq = 0;
            for (Neighbour neighbour : Neighbour.getNeighbourhood(c, graph)) {
                if (neighbour.getConcept().inheritedFrom(neighbourCC)) {
                    freq++;
                }
            }
            histo.inc(freq);
        }

        histo.print(w);

        w.close();

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
        return "neighbourhood";
    }

    @Override
    public String getName() {
        return "Ondex neighbourhood histogram generator";
    }

    @Override
    public String getVersion() {
        return "0.0.1";
    }

    /**
     * 
     */
    private class Histogram extends HashMap<Integer,Integer> {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private int maxIndex = 0;      

        /* (non-Javadoc)
		 * @see java.util.HashMap#get(java.lang.Object)
		 */
		@Override
		public Integer get(Object key) {
			if (!super.containsKey(key))
				return Integer.valueOf(0);
			else
				return super.get(key);
		}

		public void inc(int index) {

            int val = get(index);
            put(index,val+1);

            if (index > maxIndex) {
                maxIndex = index;
            }

        }

        public void print(OutputStreamWriter w) throws IOException {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < maxIndex; i++) {
                b.append(i).append("\t").append(get(i)).append("\n");
            }
            w.write(b.toString());
        }
    }

}
