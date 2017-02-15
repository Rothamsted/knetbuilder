package net.sourceforge.ondex.mapping.basicaccessionbased;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.mapping.ONDEXMapping;
import net.sourceforge.ondex.mapping.accessionbased.MetaData;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.Map.Entry;


/**
 * Implements a ConceptAccession based mapping.
 *
 * @author lysenkoa
 * @version 21.01.2008
 */
@Authors(authors = {"Artem Lysenko"}, emails = {"lysenkoa at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Mapping extends ONDEXMapping implements ArgumentNames {

    private static final Logger logger = Logger.getLogger(Mapping.class);

    /**
     * Simply calls super constructor.
     */
    public Mapping() {
        super();
    }

    /**
     * Specifies neccessary arguments for this mapping.
     *
     * @return ArgumentDefinition<?>[]
     */
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new StringArgumentDefinition(ONE_ARG, ONE_ARG_DESC, false, null, false),
                new StringArgumentDefinition(TWO_ARG, TWO_ARG_DESC, false, null, false)
        };

    }

    @Override
    public String getName() {
        return new String("Proper Accession based mapping");
    }

    @Override
    public String getVersion() {
        return new String("21.01.2008");
    }

    @Override
    public String getId() {
        return "basicaccessionbased";
    }

    /**
     * Requires an indexed ondex graph.
     *
     * @return true
     */
    public boolean requiresIndexedGraph() {
        return false;
    }

    public void start() throws InvalidPluginArgumentException {
        logger.info("start called");
        int relMade = 0;
        ONDEXGraphMetaData om = graph.getMetaData();
        EvidenceType eviType = om.getEvidenceType(MetaData.evidence);
        RelationType equ = om.getRelationType("equ");
        Set<ONDEXConcept> cs = null;
        //Set<ONDEXConcept> refSet = null;
        ConceptClass cc2 = null;
        ConceptClass cc1 = null;
        List<ONDEXConcept> mapped = null;
        if (args.getUniqueValue(ONE_ARG) != null) {
            cc1 = graph.getMetaData().getConceptClass(args.getUniqueValue(ONE_ARG).toString());
            if (cc1 != null) {
                cs = graph.getConceptsOfConceptClass(cc1);
                //refSet = graph.getConceptsOfConceptClass(cc1);
                System.err.println(cs.size());
                mapped = new LinkedList<ONDEXConcept>();
                if (args.getUniqueValue(TWO_ARG) != null) {
                    cc2 = graph.getMetaData().getConceptClass(args.getUniqueValue(TWO_ARG).toString());
                }
            }
        }
        if (cs == null) {
            cs = graph.getConcepts();
        }
        DataOutputStream out;
        try {
            out = new DataOutputStream(new FileOutputStream("acc_stats.txt"));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));

            Set<DataSource> cvs = new HashSet<DataSource>();
            Map<String, List<ONDEXConcept>> toMap = new HashMap<String, List<ONDEXConcept>>();
            logger.info("toMap created: " + toMap);
            for (ONDEXConcept c : cs) {
                for (ConceptAccession ca : c.getConceptAccessions()) cvs.add(ca.getElementOf());
            }
            System.err.println(cvs);

            for (DataSource type : cvs) {

                if (cc2 != null && cc1 != null) {
                    cs = BitSetFunctions.or(graph.getConceptsOfConceptClass(cc1), graph.getConceptsOfConceptClass(cc2));
                } else {
                    cs = graph.getConcepts();
                }

                for (ONDEXConcept c: cs) {
                    List<String> acc = getAcc(c, type);
                    if (acc.size() == 0) {
                        continue;
                    }
                    for (String ac : acc) {
                        List<ONDEXConcept> cset = toMap.get(ac);
                        if (cset == null) {
                            cset = new ArrayList<ONDEXConcept>();
                            toMap.put(ac, cset);
                        }
                        cset.add(c);
                    }

                }

                logger.info("toMap" + toMap);
                logger.info("values" + toMap.values());
                for (List<ONDEXConcept> set : toMap.values()) {
                    if (set.size() > 1) {
                        for (int i = 0; i < set.size(); i++) {
                            for (int j = i + 1; j < set.size(); j++) {
                                if (!set.get(i).getElementOf().equals(set.get(j).getElementOf())) {
                                    if (set.get(i).getOfType().equals(cc1)) {
                                        mapped.add(set.get(i));
                                    }
                                    if (set.get(j).getOfType().equals(cc1)) {
                                        mapped.add(set.get(j));
                                    }
                                    graph.getFactory().createRelation(set.get(i), set.get(j), equ, eviType);
                                    relMade++;
                                    graph.getFactory().createRelation(set.get(j), set.get(i), equ, eviType);
                                    relMade++;
                                }
                            }
                        }
                    }
                }
                if (mapped != null) {
                    for (ONDEXConcept c : mapped) {
                        ConceptAccession ca = c.getConceptAccessions().iterator().next();
                        if (ca != null)
                            bw.write(ca.getAccession() + "\t" + c.getConceptName().getName() + "\n");
                        bw.flush();
                    }
                } else {
                    for (Entry<String, List<ONDEXConcept>> ent : toMap.entrySet()) {
                        if (ent.getValue().size() > 2)
                            bw.write(ent.toString() + "\n");
                        bw.flush();
                    }
                }
                toMap.clear();
            }
            bw.close();
        } catch (Exception e) {
        }

        System.err.println("Relations created: " + relMade);
    }

    private List<String> getAcc(ONDEXConcept c, DataSource elOf) {
        List<String> accessions = new ArrayList<String>();
        for (ConceptAccession acc : c.getConceptAccessions()) {
            if (acc.getElementOf().equals(elOf)) {
                accessions.add(acc.getAccession());
            }
        }
        return accessions;
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }
}
