package net.sourceforge.ondex.algorithm.metaplanecrawler;

import net.sourceforge.ondex.core.*;

import java.util.*;

/**
 * @author jweile
 */
public class MetaPlaneCrawler {

    //####FIELDS####

    private boolean DEBUG = true;
    private int ABORT_THRESHOLD = 12000;

    private ONDEXGraph aog;

    private HashSet<Integer> rels, cons;

    private HashSet<String> uniqs;

    private HashSet<String> nospawns;

    private HashSet<String> nosearch;

    private int maxPlanes;

    private int currPlanes;

    //####CONSTRUCTOR####

    public MetaPlaneCrawler(ONDEXGraph aog) {
        this.aog = aog;
        cons = new HashSet<Integer>();
        rels = new HashSet<Integer>();
        uniqs = new HashSet<String>();
        nospawns = new HashSet<String>();
        nosearch = new HashSet<String>();
    }

    //####METHODS####

    public Collection<ONDEXConcept> getConceptSet() {
        Vector<ONDEXConcept> cs = new Vector<ONDEXConcept>();
        HashMap<String, Integer> ccMap = new HashMap<String, Integer>();
        for (int c : cons) {
            ONDEXConcept concept = aog.getConcept(c);
            cs.add(concept);
            String cc = concept.getOfType().getId();
            Integer count = ccMap.get(cc);
            if (count == null) {
                ccMap.put(cc, 1);
            } else {
                ccMap.put(cc, count + 1);
            }
        }
        if (DEBUG) System.out.println(">>>>> found " + cs.size() + " concepts");
        printMap(ccMap);
        return cs;
    }

    public void setMaximalNumberOfPlanes(int p) {
        maxPlanes = p;
    }

    public Collection<ONDEXRelation> getRelationSet() {
        Vector<ONDEXRelation> rs = new Vector<ONDEXRelation>();
        for (int r : rels)
            rs.add(aog.getRelation(r));
        if (DEBUG) System.out.println(">>>>> found " + rs.size() + " relations");
        return rs;
    }

    public void addUniqueConceptClass(ConceptClass cc) {
        uniqs.add(cc.getId());
    }

    public void addConceptClassSpawnExclusion(ConceptClass cc) {
        nospawns.add(cc.getId());
    }

    public void addConceptClassSearchExclusion(ConceptClass cc) {
        nosearch.add(cc.getId());
    }

    public void crawl(ONDEXConcept startConcept, int depthCutoff) {

        rels = new HashSet<Integer>();
        cons = new HashSet<Integer>();
        currPlanes = 0;

        crawlPlane(new Link(startConcept), depthCutoff);
    }

    private void crawlPlane(Link entry, int depthCutoff) {

        if (depthCutoff <= 0)
            return;

        currPlanes++;

//		if (DEBUG) System.out.println("start crawling plane "+depthCutoff+" for concept "+entry.c.getId());

        HashSet<String> usedCCs = new HashSet<String>();

        LinkedList<Link> open = new LinkedList<Link>();
        HashSet<ONDEXConcept> closed = new HashSet<ONDEXConcept>();

        open.offer(entry);

        while (!open.isEmpty()) {
            if (cons.size() >= ABORT_THRESHOLD) {
                return;
            }

            Link curr = open.poll();
            String cc = curr.c.getOfType().getId();
            if (closed.contains(curr.c) || cons.contains(curr.c.getId())) {
                //it's an internal cycle or a used trapdoor
                if (curr.r != null) {
                    rels.add(curr.r.getId());
                }
            } else if (uniqs.contains(cc) && usedCCs.contains(cc)) {
                //it's a new trapdoor
                if (currPlanes < maxPlanes && !nospawns.contains(cc))
                    crawlPlane(curr, depthCutoff - 1);
            } else {
                //it's an internal link
                cons.add(curr.c.getId());
                if (curr.r != null) {
                    rels.add(curr.r.getId());
                }
                usedCCs.add(cc);
                closed.add(curr.c);

                if (!nosearch.contains(cc)) {
                    for (ONDEXRelation newRel : aog.getRelationsOfConcept(curr.c)) {
                        ONDEXConcept newCon = (newRel.getFromConcept().equals(curr.c)) ?
                                newRel.getToConcept() :
                                newRel.getFromConcept();
                        open.offer(new Link(newRel, newCon));
                    }
                }
            }
        }
//		if (DEBUG) {
//			System.out.println("finished crawling plane "+depthCutoff+" for concept "+entry.c.getId()+". "+
//					cons.size()+" hits so far.");
//		}
    }

    private void printMap(HashMap<String, Integer> map) {
        for (String key : map.keySet()) {
            System.out.println(key + "\t" + map.get(key));
        }
    }

    private class Link {
        public ONDEXRelation r;
        public ONDEXConcept c;

        public Link(ONDEXRelation r, ONDEXConcept c) {
            this.r = r;
            this.c = c;
        }

        public Link(ONDEXConcept c) {
            this.c = c;
            this.r = null;
        }
    }

}
