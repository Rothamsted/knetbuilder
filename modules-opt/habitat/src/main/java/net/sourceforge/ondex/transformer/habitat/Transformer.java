package net.sourceforge.ondex.transformer.habitat;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.InconsistencyEvent;
import net.sourceforge.ondex.exception.type.ConceptClassMissingException;
import net.sourceforge.ondex.tools.MetaDataUtil;
import net.sourceforge.ondex.transformer.ONDEXTransformer;
import org.apache.log4j.Level;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

/**
 * Transforms the NCL habitat database: Creates relations
 * Habitat->Domain and Habitat->Family with edge weights
 * according to the number of paths that exist between them
 * via the Habitat->Taxa->Protein->Domain/Family route.
 *
 * @author jweile
 */
public class Transformer extends ONDEXTransformer
{

    //metadata
    private ConceptClass ccHab, ccTaxa, ccProt, ccDom;
    private AttributeName atPaths;
    private RelationType rtFeat;
    private EvidenceType etCmpd;

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
        return "Habitat-Domain-Path Transformer";
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#getVersion()
     */
    @Override
    public String getVersion() {
        return "25.03.2009";
    }

    @Override
    public String getId() {
        return "habitat";
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
        HashMap<PairKey, Integer> domPaths = new HashMap<PairKey, Integer>();

        fetchMetaData();

        log("scanning...");

        Set<ONDEXConcept> proteins = graph.getConceptsOfConceptClass(ccProt);
        int max = proteins.size(), progress = 0, dummy, percentage = 0;
        for (ONDEXConcept protein : proteins) {
            progress++;
            dummy = progress * 100 / max;
            if (dummy > percentage) {
                percentage = dummy;
                log("\n" + percentage + "%");
            }

            Collection<ONDEXConcept> domains = new Vector<ONDEXConcept>();
            ONDEXConcept taxon = null;
            Collection<ONDEXConcept> habitats = new Vector<ONDEXConcept>();

            //scan first neighbours
            for (ONDEXRelation r : graph.getRelationsOfConcept(protein)) {
                ONDEXConcept neighbour = neighbour(r, protein);
                ConceptClass neighbCC = neighbour.getOfType();
                if (neighbCC.equals(ccDom)) {
                    domains.add(neighbour);
                } else if (neighbCC.equals(ccTaxa)) {
                    if (taxon == null) {
                        taxon = neighbour;
                    } else {
                        logFail("Protein " + protein.getPID() + " has more than one Taxon ID!");
                    }
                } else {
                    logFail("Illegal neigbouring concept class for protein: " + neighbCC.getFullname());
                }
            }

            //scan for habitats
            for (ONDEXRelation r : graph.getRelationsOfConcept(taxon)) {
                ONDEXConcept neighbour = neighbour(r, taxon);
                if (neighbour.getOfType().equals(ccHab)) {
                    habitats.add(neighbour);
                }
            }

            crossConnect(habitats, domains, domPaths);

        }

        log("scanning done!");
        log("creating relations...");

        createRelations(domPaths);

        log("done!");
    }

    /**
     * creates relations between all pairs in the map
     * with edge weight according to their value in the map
     */
    private void createRelations(HashMap<PairKey, Integer> map) {
        for (PairKey key : map.keySet()) {
            ONDEXRelation r = graph.getFactory().createRelation(key.from(), key.to(), rtFeat, etCmpd);
            r.createAttribute(atPaths, map.get(key), false);
        }
    }

    /**
     * creates <code>PairKey</code> instances for all combinations of
     * elements in <code>froms</code>
     * with elements in <code>tos</code> and stores the keys in
     * the given <code>map</code>
     */
    private void crossConnect(Collection<ONDEXConcept> froms,
                              Collection<ONDEXConcept> tos,
                              HashMap<PairKey, Integer> map) {
        for (ONDEXConcept from : froms) {
            for (ONDEXConcept to : tos) {
                PairKey key = new PairKey(from, to);
                Integer count = map.get(key);
                if (count == null) {
                    count = 0;
                }
                map.put(key, count + 1);
            }
        }
    }

    /**
     * returns the concept's neighbour on the given relation
     */
    private ONDEXConcept neighbour(ONDEXRelation r, ONDEXConcept c) {
        return r.getFromConcept().equals(c) ? r.getToConcept() : r.getFromConcept();
    }

    /**
     * initializes needed metadata
     *
     * @throws ConceptClassMissingException
     */
    private void fetchMetaData() throws ConceptClassMissingException {
        MetaDataUtil mdu = new MetaDataUtil(graph.getMetaData(), null);
        ConceptClass ccThing = requireConceptClass("Thing");
        ccHab = mdu.safeFetchConceptClass("Habitat", "Habitat of Organisms", ccThing);
        ccTaxa = mdu.safeFetchConceptClass("Taxa", "Organism category", ccThing);
        ccProt = mdu.safeFetchConceptClass("Protein", "Protein", ccThing);
        ccDom = mdu.safeFetchConceptClass("ProtDomain", "Protein Domain", ccThing);
        atPaths = mdu.safeFetchAttributeName("paths", Integer.class);
        rtFeat = mdu.safeFetchRelationType("feat", "features");
        etCmpd = mdu.safeFetchEvidenceType("CMPD");
    }

    /**
     * helper class storing two concept IDs and providing
     * a sensible hashcode for the pair.
     *
     * @author jweile
     */
    private class PairKey {
        /**
         * from and to concept id
         */
        private int fromId, toId;

        /**
         * storing the hashcode
         */
        private int hashCode;

        /**
         * constructor.
         *
         * @param from
         * @param to
         */
        public PairKey(ONDEXConcept from, ONDEXConcept to) {
            fromId = from.getId();
            toId = to.getId();
            String dummy = from + "-" + to;
            hashCode = dummy.hashCode();
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object o) {
            if (o instanceof PairKey) {
                PairKey ko = (PairKey) o;
                if (fromId == ko.fromId && toId == ko.toId) {
                    return true;
                }
            }
            return false;
        }

        /**
         * @return the from-concept
         */
        public ONDEXConcept from() {
            return graph.getConcept(fromId);
        }

        /**
         * @return the to-concept.
         */
        public ONDEXConcept to() {
            return graph.getConcept(toId);
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return hashCode;
        }
    }

    /**
     * logs an event
     *
     * @param s the message
     */
    private void log(String s) {
        fireEventOccurred(new GeneralOutputEvent(s, ""));
    }

    /**
     * logs an error event
     *
     * @param s message
     */
    private void logFail(String s) {
		EventType e = new InconsistencyEvent(s,"");
		e.setLog4jLevel(Level.ERROR);
		fireEventOccurred(e);
	}

}
