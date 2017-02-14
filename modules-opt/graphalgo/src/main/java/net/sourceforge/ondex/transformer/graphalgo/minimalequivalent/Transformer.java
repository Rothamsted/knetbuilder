package net.sourceforge.ondex.transformer.graphalgo.minimalequivalent;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.transformer.graphalgo.GraphAlgoTransformer;

/**
 * The minimal equivalent graph problem is to find a directed subgraph H from a
 * given strongly connected graph G by removing the maximum number of edges from
 * G without affecting its reachability properties.
 *
 * @author taubertj
 */
public class Transformer extends GraphAlgoTransformer
{

    @Override
    public String getName() {
        return "minimalequivalent";
    }

    @Override
    public String getVersion() {
        return "28.11.2008";
    }

    @Override
    public String getId() {
        return "minimalequivalent";
    }

    @Override
    public void start() throws Exception {

        // start method
        int n = numberOfNodes();
        int m = numberOfEdges();
        int[][] array = graphAsArray();
        int[] nodei = array[0];
        int[] nodej = array[1];
        boolean[] arc = new boolean[m + 1];

        // attribute name
        AttributeName an = graph.getMetaData().getAttributeName(
                getIdentifier());
        if (an == null)
            an = graph.getMetaData().getFactory().createAttributeName(getIdentifier(),
                    java.lang.Boolean.class);

        minimalEquivalentGraph(n, m, nodei, nodej, arc);
        System.out.print("Edges of the minimal equivalent graph:\n");
        for (int k = 1; k <= m; k++) {
            ONDEXRelation r = getRelationForIndex(k);
            Attribute attribute = r.getAttribute(an);
            if (attribute == null)
                attribute = r.createAttribute(an, Boolean.valueOf(arc[k]), false);
            else
                attribute.setValue(Boolean.valueOf(arc[k]));
            if (arc[k])
                System.out.printf(" %4d%3d\n", nodei[k], nodej[k]);
        }
    }

    private void minimalEquivalentGraph(int n, int m, int nodei[], int nodej[],
                                        boolean link[]) {
        int i, j, k, nodeu, nodev, n1, low, up, edges, index1, index2, high, kedge = 0;
        int nextnode[] = new int[n + 1];
        int ancestor[] = new int[n + 1];
        int descendant[] = new int[n + 1];
        int firstedges[] = new int[n + 2];
        int pointer[] = new int[m + 1];
        int endnode[] = new int[m + 1];
        boolean pathexist[] = new boolean[n + 1];
        boolean currentarc[] = new boolean[m + 1];
        boolean pexist[] = new boolean[1];
        boolean join, skip, hop;

        n1 = n + 1;
        // set up the forward star representation of the graph
        k = 0;
        for (i = 1; i <= n; i++) {
            firstedges[i] = k + 1;
            for (j = 1; j <= m; j++)
                if (nodei[j] == i) {
                    k++;
                    pointer[k] = j;
                    endnode[k] = nodej[j];
                    currentarc[k] = true;
                }
        }
        firstedges[n1] = m + 1;
        // compute number of descendants and ancestors of each node
        for (i = 1; i <= n; i++) {
            descendant[i] = 0;
            ancestor[i] = 0;
        }
        edges = 0;
        for (k = 1; k <= m; k++) {
            i = nodei[k];
            j = nodej[k];
            descendant[i]++;
            ancestor[j]++;
            edges++;
        }
        if (edges == n) {
            for (k = 1; k <= m; k++)
                link[pointer[k]] = currentarc[k];
            return;
        }
        index1 = 0;
        for (k = 1; k <= m; k++) {
            i = nodei[pointer[k]];
            j = nodej[pointer[k]];
            // check for the existence of an alternative path
            if (descendant[i] != 1) {
                if (ancestor[j] != 1) {
                    currentarc[k] = false;
                    minimalEqGraphFindp(n, m, n1, i, j, endnode, firstedges,
                            currentarc, pexist, nextnode, pathexist);
                    if (pexist[0]) {
                        descendant[i]--;
                        ancestor[j]--;
                        index1++;
                    } else
                        currentarc[k] = true;
                }
            }
        }
        if (index1 == 0) {
            for (k = 1; k <= m; k++)
                link[pointer[k]] = currentarc[k];
            return;
        }
        high = 0;
        nodeu = n;
        nodev = n;
        // store the current best solution
        iterate:
        while (true) {
            for (k = 1; k <= m; k++)
                link[k] = currentarc[k];
            index2 = index1;
            if ((edges - index2) == n) {
                for (k = 1; k <= m; k++)
                    currentarc[k] = link[k];
                for (k = 1; k <= m; k++)
                    link[pointer[k]] = currentarc[k];
                return;
            }
            // forward move
            while (true) {
                join = false;
                low = firstedges[nodeu];
                up = firstedges[nodeu + 1];
                if (up > low) {
                    up--;
                    for (k = low; k <= up; k++)
                        if (endnode[k] == nodev) {
                            join = true;
                            kedge = k;
                            break;
                        }
                }
                hop = false;
                if (join) {
                    if (!currentarc[kedge]) {
                        currentarc[kedge] = true;
                        descendant[nodeu]++;
                        ancestor[nodev]++;
                        index1--;
                        if (index1 + high - (n - nodeu) > index2) {
                            if (nodev != n)
                                nodev++;
                            else {
                                if (nodeu != n) {
                                    nodeu++;
                                    nodev = 1;
                                } else
                                    continue iterate;
                            }
                            while (true) {
                                // backtrack move
                                join = false;
                                low = firstedges[nodeu];
                                up = firstedges[nodeu + 1];
                                if (up > low) {
                                    up--;
                                    for (k = low; k <= up; k++)
                                        if (endnode[k] == nodev) {
                                            join = true;
                                            kedge = k;
                                            break;
                                        }
                                }
                                if (join) {
                                    high--;
                                    skip = false;
                                    if (descendant[nodeu] != 1) {
                                        if (ancestor[nodev] != 1) {
                                            currentarc[kedge] = false;
                                            minimalEqGraphFindp(n, m, n1,
                                                    nodeu, nodev, endnode,
                                                    firstedges, currentarc,
                                                    pexist, nextnode, pathexist);
                                            if (pexist[0]) {
                                                descendant[nodeu]--;
                                                ancestor[nodev]--;
                                                index1++;
                                                skip = true;
                                            } else
                                                currentarc[kedge] = true;
                                        }
                                    }
                                    if (!skip) {
                                        if (index1 + high - (n - nodeu) <= index2) {
                                            high++;
                                            hop = true;
                                            break;
                                        }
                                    }
                                    // check for the termination of the forward
                                    // move
                                    if (high - (n - nodeu) == 0)
                                        continue iterate;
                                }
                                if (nodev != n)
                                    nodev++;
                                else {
                                    if (nodeu != n) {
                                        nodeu++;
                                        nodev = 1;
                                    } else
                                        continue iterate;
                                }
                            }
                        }
                    }
                    if (!hop)
                        high++;
                }
                hop = false;
                if (nodev != 1) {
                    nodev--;
                    continue;
                }
                if (nodeu == 1) {
                    for (k = 1; k <= m; k++)
                        currentarc[k] = link[k];
                    for (k = 1; k <= m; k++)
                        link[pointer[k]] = currentarc[k];
                    return;
                }
                nodeu--;
                nodev = n;
            }
        }
    }

    private void minimalEqGraphFindp(int n, int m, int n1, int nodeu,
                                     int nodev, int endnode[], int firstedges[], boolean currentarc[],
                                     boolean pexist[], int nextnode[], boolean pathexist[]) {
        /* this method is used internally by minimalEquivalentGraph */

        // determine if a path exists from nodeu to nodev by Yen's algorithm
        int i, j, k, i2, j2, low, up, kedge = 0, index1, index2, index3;
        boolean join;

        // initialization
        for (i = 1; i <= n; i++) {
            nextnode[i] = i;
            pathexist[i] = false;
        }
        pathexist[nodeu] = true;
        nextnode[nodeu] = n;
        index1 = nodeu;
        index2 = n - 1;
        // compute the shortest distance labels
        i = 1;
        while (true) {
            j = nextnode[i];
            join = false;
            low = firstedges[index1];
            up = firstedges[index1 + 1];
            if (up > low) {
                up--;
                for (k = low; k <= up; k++)
                    if (endnode[k] == j) {
                        join = true;
                        kedge = k;
                        break;
                    }
            }
            if (join)
                if (currentarc[kedge])
                    pathexist[j] = true;
            if (pathexist[j]) {
                index3 = i + 1;
                if (index3 <= index2) {
                    for (i2 = index3; i2 <= index2; i2++) {
                        j2 = nextnode[i2];
                        join = false;
                        low = firstedges[index1];
                        up = firstedges[index1 + 1];
                        if (up > low) {
                            up--;
                            for (k = low; k <= up; k++)
                                if (endnode[k] == j2) {
                                    join = true;
                                    kedge = k;
                                    break;
                                }
                        }
                        if (join)
                            if (currentarc[kedge])
                                pathexist[j2] = true;
                    }
                }
                // check whether an alternative path exists
                if (pathexist[nodev]) {
                    pexist[0] = true;
                    return;
                }
                nextnode[i] = nextnode[index2];
                index1 = j;
                index2--;
                if (index2 > 1)
                    continue;
                join = false;
                low = firstedges[index1];
                up = firstedges[index1 + 1];
                if (up > low) {
                    up--;
                    for (k = low; k <= up; k++)
                        if (endnode[k] == nodev) {
                            join = true;
                            kedge = k;
                            break;
                        }
                }
                pexist[0] = false;
                if (join)
                    if (currentarc[kedge])
                        pexist[0] = true;
                return;
            }
            i++;
            if (i <= index2)
                continue;
            pexist[0] = false;
            return;
        }
    }
}
