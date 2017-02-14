package net.sourceforge.ondex.transformer.graphalgo.cutnodes;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.transformer.graphalgo.GraphAlgoTransformer;

/**
 * A cut node of a component is a node whose removal will disconnect the
 * component.
 *
 * @author taubertj
 */
public class Transformer extends GraphAlgoTransformer
{

    @Override
    public String getName() {
        return "cutnodes";
    }

    @Override
    public String getVersion() {
        return "25.11.2008";
    }

    @Override
    public String getId() {
        return "cutnodes";
    }

    @Override
    public void start() throws Exception {

        // start method
        int n = numberOfNodes();
        int m = numberOfEdges();
        int[][] array = graphAsArray();
        int[] nodei = array[0];
        int[] nodej = array[1];
        int[] cutnode = new int[n + 1];
        int k = cutNodes(n, m, nodei, nodej, cutnode);

        // attribute name
        AttributeName an = graph.getMetaData().getAttributeName(
                getIdentifier());
        if (an == null)
            an = graph.getMetaData().getFactory().createAttributeName(getIdentifier(),
                    java.lang.Integer.class);

        // parse results
        System.out.print("number of components = " + k
                + "\n  number of cutnodes = " + cutnode[0]
                + "\n\nThe cut nodes are: ");
        for (int i = 1; i <= cutnode[0]; i++) {
            System.out.printf("%4d", cutnode[i]);
            ONDEXConcept c = getConceptForIndex(cutnode[i]);
            Attribute attribute = c.getAttribute(an);
            if (attribute == null)
                attribute = c.createAttribute(an, Integer.valueOf(i), false);
            else
                attribute.setValue(Integer.valueOf(i));
        }
    }

    private int cutNodes(int n, int m, int nodei[], int nodej[], int cutnode[]) {
        int i, j, k, nodeu, nodev, node1, node2, node3, node4, numblocks;
        int root, p, edges, index, len1, len2, low, up, components;
        int totalcutnodes, numcutnodes = 0;
        int firstedges[] = new int[n + 1];
        int label[] = new int[n + 1];
        int nextnode[] = new int[n + 1];
        int length[] = new int[n + 1];
        int cutvertex[] = new int[n + 1];
        int cutedge[] = new int[m + 1];
        boolean mark[] = new boolean[n + 1];
        boolean join;

        totalcutnodes = 0;
        for (i = 1; i <= n; i++)
            nextnode[i] = 0;
        components = 0;
        for (root = 1; root <= n; root++) {
            if (nextnode[root] == 0) {
                components++;
                // set up the forward star representation of the graph
                k = 0;
                for (i = 1; i <= n - 1; i++) {
                    firstedges[i] = k + 1;
                    for (j = 1; j <= m; j++) {
                        nodeu = nodei[j];
                        nodev = nodej[j];
                        if ((nodeu == i) && (nodeu < nodev)) {
                            k++;
                            cutedge[k] = nodev;
                        } else {
                            if ((nodev == i) && (nodev < nodeu)) {
                                k++;
                                cutedge[k] = nodeu;
                            }
                        }
                    }
                }
                firstedges[n] = m + 1;
                for (i = 1; i <= n; i++) {
                    label[i] = 0;
                    mark[i] = false;
                }
                length[root] = 0;
                nextnode[root] = -1;
                label[root] = -root;
                index = 1;
                cutvertex[1] = root;
                edges = 2;
                do {
                    node3 = cutvertex[index];
                    index--;
                    nextnode[node3] = -nextnode[node3];
                    len1 = 0;
                    for (node2 = 1; node2 <= n; node2++) {
                        join = false;
                        if (node2 != node3) {
                            if (node2 < node3) {
                                nodeu = node2;
                                nodev = node3;
                            } else {
                                nodeu = node3;
                                nodev = node2;
                            }
                            low = firstedges[nodeu];
                            up = firstedges[nodeu + 1];
                            if (up > low) {
                                up--;
                                for (k = low; k <= up; k++)
                                    if (cutedge[k] == nodev) {
                                        join = true;
                                        break;
                                    }
                            }
                        }
                        if (join) {
                            node1 = nextnode[node2];
                            if (node1 == 0) {
                                nextnode[node2] = -node3;
                                index++;
                                cutvertex[index] = node2;
                                length[node2] = length[node3] + 1;
                                label[node2] = -node2;
                            } else {
                                if (node1 < 0) {
                                    // next block
                                    node4 = label[node2];
                                    if (node4 > 0)
                                        mark[node4] = true;
                                    label[node2] = node3;
                                    len2 = length[node3] - length[-node1];
                                    if (len2 > len1)
                                        len1 = len2;
                                }
                            }
                        }
                    }
                    if (len1 > 0) {
                        j = node3;
                        while (true) {
                            len1--;
                            if (len1 < 0)
                                break;
                            p = label[j];
                            if (p > 0)
                                mark[p] = true;
                            label[j] = node3;
                            j = nextnode[j];
                        }
                        for (i = 1; i <= n; i++) {
                            p = label[i];
                            if (p > 0)
                                if (mark[p])
                                    label[i] = node3;
                        }
                    }
                    edges++;
                } while ((edges <= n) && (index > 0));
                nextnode[root] = 0;
                node3 = cutvertex[1];
                nextnode[node3] = Math.abs(nextnode[node3]);
                numblocks = 0;
                numcutnodes = 0;
                for (i = 1; i <= n; i++)
                    if (i != root) {
                        node3 = label[i];
                        if (node3 < 0) {
                            numblocks++;
                            label[i] = n + numblocks;
                        } else {
                            if ((node3 <= n) && (node3 > 0)) {
                                numblocks++;
                                node4 = n + numblocks;
                                for (j = i; j <= n; j++)
                                    if (label[j] == node3)
                                        label[j] = node4;
                            }
                        }
                    }
                for (i = 1; i <= n; i++) {
                    p = label[i];
                    if (p > 0)
                        label[i] = p - n;
                }
                i = 1;
                while (nextnode[i] != root)
                    i++;
                label[root] = label[i];
                for (i = 1; i <= n; i++) {
                    node1 = nextnode[i];
                    if (node1 > 0) {
                        p = Math.abs(label[node1]);
                        if (Math.abs(label[i]) != p)
                            label[node1] = -p;
                    }
                }
                for (i = 1; i <= n; i++)
                    if (label[i] < 0)
                        numcutnodes++;
                // store the cut nodes
                j = 0;
                for (i = 1; i <= n; i++)
                    if (label[i] < 0) {
                        j++;
                        cutvertex[j] = i;
                    }
                // find the end-nodes
                for (i = 1; i <= n; i++)
                    length[i] = 0;
                for (i = 1; i <= m; i++) {
                    j = nodei[i];
                    length[j]++;
                    j = nodej[i];
                    length[j]++;
                }
                for (i = 1; i <= n; i++)
                    if (length[i] == 1)
                        if (label[i] > 0)
                            label[i] = -label[i];
                for (p = 1; p <= numcutnodes; p++) {
                    totalcutnodes++;
                    cutnode[totalcutnodes] = cutvertex[p];
                }
            }
        }
        cutnode[0] = totalcutnodes;
        return components;
    }

}
