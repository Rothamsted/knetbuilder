package net.sourceforge.ondex.cytoscape.mapping;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;

/**
 * this class searches instances of a metagraph path in the ondex graph
 * given a path description, specified as a concept class array and a
 * relation type array.
 *
 * CCs[A,B,C,...], RTs[1,2,...] = A 1 B 2 C ...
 *
 * @author jweile
 * @author Matthew Pocock
 *
 */
public class PathSearcher {

        /**
         * ondex graph to search in.
         */
        private ONDEXGraph og;

        /**
         * path description arrays
         */
        private ConceptClass[] ccs;
        private RelationType[] rts;

        private RelationType rt_eq;

        /**
         * result vector each entry in the vector is a path represented
         * by an integer array storing concept and relation ids in turn.
         */
        private List<List<Integer>> paths = new ArrayList<List<Integer>>();

        /**
         * iteration cursor for result querying.
         */
        private int cursor = 0;

        /**
         * constructor.
         * @throws IllegalArgumentException if ccs.length != rts.length + 1
         */
        public PathSearcher(ONDEXGraph og, ConceptClass[] ccs, RelationType[] rts) throws
IllegalArgumentException {
                if (ccs.length != rts.length + 1)
                        throw new IllegalArgumentException("Invalid path definition");
                this.ccs = ccs;
                this.rts = rts;
                this.og = og;

                rt_eq = og.getMetaData().getRelationType("equ");
        }

        /**
         * moves the cursor forward
         * @return whether there is a next path to query
         */
        public Path nextPath() {
                try {
                        if (cursor < paths.size()-1) {
                                return new Path(paths.get(++cursor));
                        } else {
                                return null;
                        }
                } catch (MalformedPathException e) {
                        throw new RuntimeException(e);
                }
        }

        /**
         * resets the cursor to the first path
         */
        public void resetCursor() {
                cursor = 0;
        }


        private boolean instanceOf(ConceptClass cc, ONDEXConcept c) {
                ConceptClass cc_query = c.getOfType();
                while (!cc_query.equals(cc)) {
                        if (cc_query.getSpecialisationOf() == null) {
                                return false;
                        }
                        cc_query = cc_query.getSpecialisationOf();
                }
                return true;
        }

        private boolean instanceOf(RelationType rt, ONDEXRelation r) {
                RelationType rt_query = r.getOfType();
                while (!rt_query.equals(rt)) {
                        if (rt_query.getSpecialisationOf() == null) {
                                return false;
                        }
                        rt_query = rt_query.getSpecialisationOf();
                }
                return true;
        }

        /**
         * searches for paths.
         */
        public void search() {
                for (ONDEXConcept c : og.getConcepts()) {
                        if (!instanceOf(ccs[0], c)) {
                                continue;
                        }
                        PathNode pn = new PathNode(c.getId());
                        searchRelations(c, pn, 1, false);
                }
        }

        /**
         * recursive helper method
         */
        private void searchRelations(ONDEXConcept c, PathNode pn, int depth, boolean
allowSkip) {
                //recursion base
                if (depth >= ccs.length) {
                        List<Integer> a = new ArrayList<Integer>();
                        traceback(pn, a);
                        paths.add(a);
                        return;
                }

                //get neighbours
                for (ONDEXRelation r : og.getRelationsOfConcept(c)) {
                        if (instanceOf(rts[depth-1], r)) {
                                //get neighbour
                                ONDEXConcept cn = r.getFromConcept().equals(c) ? r.getToConcept() :
r.getFromConcept();
                                if (instanceOf(ccs[depth], cn)) {
                                        PathNode pn_next = new PathNode(cn.getId(), r.getId(), pn);
                                        searchRelations(cn, pn_next, depth+1, true);
                                }
                        } else if (allowSkip && r.getOfType().equals(rt_eq)) {
                                //get neighbour
                                ONDEXConcept cn = r.getFromConcept().equals(c) ? r.getToConcept() :
r.getFromConcept();
                                if (instanceOf(ccs[depth-1], cn)) {
                                        PathNode pn_next = new PathNode(cn.getId(), r.getId(), pn);
                                        searchRelations(cn, pn_next, depth+1, false);
                                }
                        }
                }
        }

        /**
         * traces the results back. private helper method.
         *
         */
        private void traceback(PathNode pn, List<Integer> a) {
                a.add(pn.cid);
                if (pn.parent != null) {
                        a.add(pn.rid);
                        traceback(pn.parent, a);
                }
        }

        /**
         * private class representing a node in a path, that is back-traceable.
         */
        private class PathNode {
                public int cid, rid;
                public PathNode parent;
                public PathNode(int cid, int rid, PathNode parent) {
                        this(cid);
                        this.rid = rid;
                        this.parent = parent;
                }
                public PathNode(int cid) {
                        this.cid = cid;
                }
        }

        public class Path {
                private ONDEXEntity[] path;
                private Path(List<Integer> ids) throws MalformedPathException {
                        path = new ONDEXEntity[ids.size()];
                        for (int i = 0; i < path.length; i++) {
                                if (i % 2 == 0) {
                                        path[i] = og.getConcept(ids.get(i));
                                        if (path[i] == null) {
                                                throw new MalformedPathException("No concept for id "+ids.get(i));
                                        }
                                } else {
                                        path[i] = og.getRelation(ids.get(i));
                                        if (path[i] == null) {
                                                throw new MalformedPathException("No relation for id"+ids.get(i));
                                        }
                                }
                        }
                }
                public ONDEXConcept head() {
                        return (ONDEXConcept) path[0];
                }
                public ONDEXConcept tail() {
                        return (ONDEXConcept) path[path.length -1];
                }
        }
}
