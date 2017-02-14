package net.sourceforge.ondex.transformer.keggenzymecentric;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * @author jweile
 */
@Authors(authors = {"Jochen Weile"}, emails = {" jweile at users.sourceforge.net"})
@Custodians(custodians = {"Shaochih Kuo"}, emails = {"sckuo at users.sourceforge.net"})
public class Transformer extends ONDEXTransformer
{

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#getArgumentDefinitions()
     */
    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[0];
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#getName()
     */
    @Override
    public String getName() {
        return "KEGG enzyme centric view transformer";
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#getVersion()
     */
    @Override
    public String getVersion() {
        return "10.02.2009";
    }

    @Override
    public String getId() {
        return "keggenzymecentric";
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#requiresIndexedGraph()
     */
    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#requiresValidators()
     */
    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#start()
     */
    @Override
    public void start() throws Exception {
        ConceptClass ccComp = requireConceptClass("Comp");
        ConceptClass ccPath = requireConceptClass("Path");
        ConceptClass ccReaction = requireConceptClass("Reaction");
        ConceptClass ccEnzyme = requireConceptClass("Enzyme");

        DataSource dataSourceMips = requireDataSource("MIPS");

        RelationType rtPdBy = requireRelationType("pd_by");
        RelationType rtCsBy = requireRelationType("cs_by");
        RelationType rtMIsp = requireRelationType("member_of");
        RelationType rtCaBy = requireRelationType("ca_by");

        RelationType rtPrecBy = requireRelationType("preceded_by");

        EvidenceType etNB = requireEvidenceType("NB");

        String ondexdir = System.getProperty("ondex.dir");
        BufferedWriter bw = new BufferedWriter(new FileWriter(ondexdir + "/enzymeCentric.tab"));
        bw.write("producer\tconsumer\n");

        Set<ONDEXConcept> comps = graph.getConceptsOfConceptClass(ccComp);
        int maxProgress = comps.size(), progress = 0, chunksize = maxProgress / 50;
        System.out.println("0%                                            100%");
        for (ONDEXConcept comp : comps) {
            progress++;
            if (progress % chunksize == chunksize - 1)
                System.out.print("=");
            Set<ONDEXRelation> rels = graph.getRelationsOfConcept(comp);
            Vector<ONDEXConcept> fromReactions = new Vector<ONDEXConcept>();
            Vector<ONDEXConcept> toReactions = new Vector<ONDEXConcept>();
            for (ONDEXRelation rel : rels) {
                ONDEXConcept otherEnd = rel.getFromConcept().equals(comp) ? rel.getToConcept() : rel.getFromConcept();
                if (otherEnd.getOfType().equals(ccReaction)) {
                    if (rel.getOfType().equals(rtCsBy)) {
                        fromReactions.add(otherEnd);
                    } else if (rel.getOfType().equals(rtPdBy)) {
                        toReactions.add(otherEnd);
                    }
                }
            }
            Vector<ONDEXConcept> fromEnzymes = new Vector<ONDEXConcept>();
            Vector<ONDEXConcept> toEnzymes = new Vector<ONDEXConcept>();
            for (ONDEXConcept fromReaction : fromReactions) {
                for (ONDEXConcept toReaction : toReactions) {
                    if (haveAtLeastOneCommonMember(neighbours(fromReaction, rtMIsp, ccPath),
                            neighbours(toReaction, rtMIsp, ccPath))) {
                        for (ONDEXConcept fromEnzyme : neighbours(fromReaction, rtCaBy, ccEnzyme)) {
                            for (ONDEXConcept toEnzyme : neighbours(toReaction, rtCaBy, ccEnzyme)) {
                                if (graph.getRelation(fromEnzyme, toEnzyme, rtPrecBy) == null) {
                                    graph.getFactory().createRelation(fromEnzyme, toEnzyme, rtPrecBy, etNB);
                                    String fromAcc = fetchAcc(fromEnzyme, dataSourceMips);
                                    String toAcc = fetchAcc(toEnzyme, dataSourceMips);
                                    if (fromAcc != null && toAcc != null) {
                                        bw.write(toAcc + "\t" + fromAcc + "\n");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println();
        bw.close();
    }

    private String fetchAcc(ONDEXConcept c, DataSource dataSource) {
        Set<ConceptAccession> accs = c.getConceptAccessions();
        for (ConceptAccession acc : accs) {
            if (acc.getElementOf().equals(dataSource)) {
                return acc.getAccession();
            }
        }
        return null;
    }

    private Collection<ONDEXConcept> neighbours(ONDEXConcept c, RelationType rt, ConceptClass cc) {
        HashSet<ONDEXConcept> result = new HashSet<ONDEXConcept>();
        Set<ONDEXRelation> rels = graph.getRelationsOfConcept(c);
        for (ONDEXRelation rel : rels) {
            ONDEXConcept otherEnd = rel.getFromConcept().equals(c) ? rel.getToConcept() : rel.getFromConcept();
            if (otherEnd.getOfType().equals(cc) && rel.getOfType().equals(rt)) {
                result.add(otherEnd);
            }
        }
        return result;
    }

    private boolean haveAtLeastOneCommonMember(Collection<?> a, Collection<?> b) {
        for (Object x : a) {
            if (b.contains(x)) {
                return true;
            }
        }
        return false;
    }

}
