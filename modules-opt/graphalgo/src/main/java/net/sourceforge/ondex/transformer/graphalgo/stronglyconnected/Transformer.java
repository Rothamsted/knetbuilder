package net.sourceforge.ondex.transformer.graphalgo.stronglyconnected;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.transformer.graphalgo.GraphAlgoTransformer;

/**
 * A strongly connected component of a directed graph is a maximal set of nodes
 * in which there is a directed path from any one node in the set to any other
 * node in the set.
 *
 * @author taubertj
 */
public class Transformer extends GraphAlgoTransformer
{

    @Override
    public String getName() {
        return "stronglyconnected";
    }

    @Override
    public String getVersion() {
        return "27.11.2008";
    }

    @Override
    public String getId() {
        return "stronglyconnected";
    }

    @Override
    public void start() throws Exception {

        // start method
        int n = numberOfNodes();
        int m = numberOfEdges();
        int[][] array = graphAsArray();
        int[] nodei = array[0];
        int[] nodej = array[1];
        int[] component = new int[n + 1];

        // attribute name
        AttributeName an = graph.getMetaData().getAttributeName(
                getIdentifier());
        if (an == null)
            an = graph.getMetaData().getFactory().createAttributeName(getIdentifier(),
                    java.lang.Integer.class);

        stronglyConnectedComponents(n, m, nodei, nodej, component);
        System.out.println("Number of strongly connected components = "
                + component[0]);
        for (int i = 1; i <= component[0]; i++) {
            System.out.printf("\n Nodes in component " + i + ": ");
            for (int j = 1; j <= n; j++)
                if (component[j] == i) {
                    System.out.printf("%3d", j);
                    ONDEXConcept c = getConceptForIndex(j);
                    Attribute attribute = c.getAttribute(an);
                    if (attribute == null)
                        attribute = c.createAttribute(an, Integer.valueOf(i), false);
                    else
                        attribute.setValue(Integer.valueOf(i));
                }
        }
        System.out.println();
    }

    private void stronglyConnectedComponents(int n, int m, int nodei[],
                                             int nodej[], int component[]) {
        int i, j, k, series, stackpointer, numcompoents, p, q, r;
        int backedge[] = new int[n + 1];
        int parent[] = new int[n + 1];
        int sequence[] = new int[n + 1];
        int stack[] = new int[n + 1];
        int firstedges[] = new int[n + 2];
        int endnode[] = new int[m + 1];
        boolean next[] = new boolean[n + 1];
        boolean trace[] = new boolean[n + 1];
        boolean fresh[] = new boolean[m + 1];
        boolean skip, found;

        // set up the forward star representation of the graph
        firstedges[1] = 0;
        k = 0;
        for (i = 1; i <= n; i++) {
            for (j = 1; j <= m; j++)
                if (nodei[j] == i) {
                    k++;
                    endnode[k] = nodej[j];
                }
            firstedges[i + 1] = k;
        }
        for (j = 1; j <= m; j++)
            fresh[j] = true;
        // initialize
        for (i = 1; i <= n; i++) {
            component[i] = 0;
            parent[i] = 0;
            sequence[i] = 0;
            backedge[i] = 0;
            next[i] = false;
            trace[i] = false;
        }
        series = 0;
        stackpointer = 0;
        numcompoents = 0;
        // choose an unprocessed node not in the stack
        while (true) {
            p = 0;
            while (true) {
                p++;
                if (n < p) {
                    component[0] = numcompoents;
                    return;
                }
                if (!trace[p])
                    break;
            }
            series++;
            sequence[p] = series;
            backedge[p] = series;
            trace[p] = true;
            stackpointer++;
            stack[stackpointer] = p;
            next[p] = true;
            while (true) {
                skip = false;
                for (q = 1; q <= n; q++) {
                    // find an unprocessed edge (p,q)
                    found = false;
                    for (i = firstedges[p] + 1; i <= firstedges[p + 1]; i++)
                        if ((endnode[i] == q) && fresh[i]) {
                            // mark the edge as processed
                            fresh[i] = false;
                            found = true;
                            break;
                        }
                    if (found) {
                        if (!trace[q]) {
                            series++;
                            sequence[q] = series;
                            backedge[q] = series;
                            parent[q] = p;
                            trace[q] = true;
                            stackpointer++;
                            stack[stackpointer] = q;
                            next[q] = true;
                            p = q;
                        } else {
                            if (trace[q]) {
                                if (sequence[q] < sequence[p] && next[q]) {
                                    backedge[p] = (backedge[p] < sequence[q]) ? backedge[p]
                                            : sequence[q];
                                }
                            }
                        }
                        skip = true;
                        break;
                    }
                }
                if (skip)
                    continue;
                if (backedge[p] == sequence[p]) {
                    numcompoents++;
                    while (true) {
                        r = stack[stackpointer];
                        stackpointer--;
                        next[r] = false;
                        component[r] = numcompoents;
                        if (r == p)
                            break;
                    }
                }
                if (parent[p] != 0) {
                    backedge[parent[p]] = (backedge[parent[p]] < backedge[p]) ? backedge[parent[p]]
                            : backedge[p];
                    p = parent[p];
                } else
                    break;
            }
        }
    }
}
