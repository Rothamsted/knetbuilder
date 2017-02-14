/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.ondex.stats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.export.ONDEXExport;
import uk.ac.ncl.cs.ondex.tools.Neighbour;

/**
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class InteractionExperimentStatistic extends ONDEXExport {

    private AttributeName anNegative;

    @Override
    public String getId() {
        return "interactionExpStat";
    }

    @Override
    public String getName() {
        return "Interaction-Experiment Statistic";
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[] {
            new FileArgumentDefinition(FileArgumentDefinition.EXPORT_DIR,
                    FileArgumentDefinition.EXPORT_DIR_DESC, true, true, true)
        };
    }

    @Override
    public void start() throws Exception {

        ConceptClass ccExp = requireConceptClass("Experiment");
        ConceptClass ccPI = requireConceptClass("MI:0914");
        ConceptClass ccPP = requireConceptClass("Polypeptide");
        ConceptClass ccGI = requireConceptClass("MI:0208");
        ConceptClass ccNucl = requireConceptClass("NucleotideFeature");
        anNegative = requireAttributeName("negative");

        File outDir = new File((String)getArguments().getUniqueValue(FileArgumentDefinition.EXPORT_DIR));
        
        try {
            //search physical interactions
            System.out.println("Scanning physical interactions");
            configureWriter(new File(outDir,getId()+"_PPI.tsv"));
            search(ccExp, ccPI, ccPP);
        } finally {
            closeWriter();
        }

        try {
            //search genetic interactions
            System.out.println("Scanning genetic interactions");
            configureWriter(new File(outDir,getId()+"_GI.tsv"));
            search(ccExp, ccGI, ccNucl);
        } finally {
            closeWriter();
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

    private void search(ConceptClass ccExp, ConceptClass ccIntn, ConceptClass ccIntr) {

        Set<ONDEXConcept> interactors = new HashSet<ONDEXConcept>();
        Set<ONDEXConcept> interactions = new HashSet<ONDEXConcept>();

        int experimentsWithoutGenes = 0;
        int interactionsWithoutGenes = 0;
        
        for (ONDEXConcept c : graph.getConcepts()) {
            if (c.inheritedFrom(ccExp)) {
                interactors.clear();
                interactions.clear();
                for (Neighbour nIntn : Neighbour.getNeighbourhood(c, graph)) {
                    if (nIntn.getConcept().inheritedFrom(ccIntn)) {
                        ONDEXConcept cIntn = nIntn.getConcept();
                        if (!isNegative(cIntn)) {
                            interactions.add(cIntn);
                        }
                        for (Neighbour nIntr : Neighbour.getNeighbourhood(cIntn, graph)) {
                            if (nIntr.getConcept().inheritedFrom(ccIntr)) {
                                interactors.add(nIntr.getConcept());
                            }
                        }
                    }
                }

                if (interactors.size() > 0) {
                    w(c.getPID());
                    w("\t");
                    w(interactors.size()+"");
                    w("\t");
                    w(interactions.size()+"");
                    w("\n");
                } else {
                    if (interactions.size() > 0) {
                        interactionsWithoutGenes += interactions.size();
                    }
                    experimentsWithoutGenes++;
                }
                
            }
        }
        
        if (experimentsWithoutGenes > 0) {
            System.err.println("Experiments without interactions of above type: "+experimentsWithoutGenes);
        }
        if (interactionsWithoutGenes > 0) {
            System.err.println("Empty interactions: "+interactionsWithoutGenes);
        }

    }

    private boolean isNegative(ONDEXConcept cIntn) {
        Attribute a = cIntn.getAttribute(anNegative);
        if (a != null && a.getValue() != null && a.getValue().equals(Boolean.TRUE)) {
            return true;
        } else {
            return false;
        }
    }

    private Writer w;

    private void configureWriter(File file) {
        try {
        w = new BufferedWriter(new FileWriter(file));
        } catch (IOException e ) {
            throw new RuntimeException(e);
        }
    }

    private Writer w(String s) {
        try {
            w.write(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return w;
    }

    private void closeWriter() {
        try {
            w.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
