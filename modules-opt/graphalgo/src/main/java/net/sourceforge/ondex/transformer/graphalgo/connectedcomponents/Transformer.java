package net.sourceforge.ondex.transformer.graphalgo.connectedcomponents;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.transformer.graphalgo.GraphAlgoTransformer;

/**
 * A connected component is a maximal connected subgraph.
 *
 * @author taubertj
 */
public class Transformer extends GraphAlgoTransformer
{

    @Override
    public String getName() {
        return "connectedcomponents";
    }

    @Override
    public String getVersion() {
        return "24.11.2008";
    }

    @Override
    public String getId() {
        return "connectedcomponents";
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
        connectedComponents(n, m, nodei, nodej, component);

        // create attribute name
        AttributeName an = graph.getMetaData().getAttributeName(
                getIdentifier());
        if (an == null)
            an = graph.getMetaData().getFactory().createAttributeName(getIdentifier(),
                    java.lang.Integer.class);

        // parse results
        System.out.println("Total number of components = " + component[0]);
        System.out.print("\n             Node: ");
        for (int i = 1; i <= n; i++)
            System.out.print("  " + i);
        System.out.print("\n Component Number: ");
        for (int i = 1; i <= n; i++) {
            System.out.print("  " + component[i]);
            ONDEXConcept c = getConceptForIndex(i);
            Attribute attribute = c.getAttribute(an);
            if (attribute == null)
                attribute = c.createAttribute(an, Integer.valueOf(component[i]),
                        false);
            else
                attribute.setValue(Integer.valueOf(component[i]));

        }
        System.out.println();
    }

    private void connectedComponents(int n, int m, int nodei[], int nodej[],
                                     int component[]) {
        int edges, i, j, numcomp, p, q, r, typea, typeb, typec, tracka, trackb;
        int compkey, key1, key2, key3, nodeu, nodev;
        int numnodes[] = new int[n + 1];
        int aux[] = new int[n + 1];
        int index[] = new int[3];

        typec = 0;
        index[1] = 1;
        index[2] = 2;
        q = 2;
        for (i = 1; i <= n; i++) {
            component[i] = -i;
            numnodes[i] = 1;
            aux[i] = 0;
        }
        j = 1;
        edges = m;
        do {
            nodeu = nodei[j];
            nodev = nodej[j];
            key1 = component[nodeu];
            if (key1 < 0)
                key1 = nodeu;
            key2 = component[nodev];
            if (key2 < 0)
                key2 = nodev;
            if (key1 == key2) {
                if (j >= edges) {
                    edges--;
                    break;
                }
                nodei[j] = nodei[edges];
                nodej[j] = nodej[edges];
                nodei[edges] = nodeu;
                nodej[edges] = nodev;
                edges--;
            } else {
                if (numnodes[key1] >= numnodes[key2]) {
                    key3 = key1;
                    key1 = key2;
                    key2 = key3;
                    typec = -component[key2];
                } else
                    typec = Math.abs(component[key2]);
                aux[typec] = key1;
                component[key2] = component[key1];
                i = key1;
                do {
                    component[i] = key2;
                    i = aux[i];
                } while (i != 0);
                numnodes[key2] += numnodes[key1];
                numnodes[key1] = 0;
                j++;
                if (j > edges || j > n)
                    break;
            }
        } while (true);
        numcomp = 0;
        for (i = 1; i <= n; i++)
            if (numnodes[i] != 0) {
                numcomp++;
                numnodes[numcomp] = numnodes[i];
                aux[i] = numcomp;
            }
        for (i = 1; i <= n; i++) {
            key3 = component[i];
            if (key3 < 0)
                key3 = i;
            component[i] = aux[key3];
        }
        if (numcomp == 1) {
            component[0] = numcomp;
            return;
        }
        typeb = numnodes[1];
        numnodes[1] = 1;
        for (i = 2; i <= numcomp; i++) {
            typea = numnodes[i];
            numnodes[i] = numnodes[i - 1] + typeb - 1;
            typeb = typea;
        }
        for (i = 1; i <= edges; i++) {
            typec = nodei[i];
            compkey = component[typec];
            aux[i] = numnodes[compkey];
            numnodes[compkey]++;
        }
        for (i = 1; i <= q; i++) {
            typea = index[i];
            do {
                if (typea <= i)
                    break;
                typeb = index[typea];
                index[typea] = -typeb;
                typea = typeb;
            } while (true);
            index[i] = -index[i];
        }
        if (aux[1] >= 0)
            for (j = 1; j <= edges; j++) {
                tracka = aux[j];
                do {
                    if (tracka <= j)
                        break;
                    trackb = aux[tracka];
                    aux[tracka] = -trackb;
                    tracka = trackb;
                } while (true);
                aux[j] = -aux[j];
            }
        for (i = 1; i <= q; i++) {
            typea = -index[i];
            if (typea >= 0) {
                r = 0;
                do {
                    typea = index[typea];
                    r++;
                } while (typea > 0);
                typea = i;
                for (j = 1; j <= edges; j++)
                    if (aux[j] <= 0) {
                        trackb = j;
                        p = r;
                        do {
                            tracka = trackb;
                            key1 = (typea == 1) ? nodei[tracka] : nodej[tracka];
                            do {
                                typea = Math.abs(index[typea]);
                                key1 = (typea == 1) ? nodei[tracka]
                                        : nodej[tracka];
                                tracka = Math.abs(aux[tracka]);
                                key2 = (typea == 1) ? nodei[tracka]
                                        : nodej[tracka];
                                if (typea == 1)
                                    nodei[tracka] = key1;
                                else
                                    nodej[tracka] = key1;
                                key1 = key2;
                                if (tracka == trackb) {
                                    p--;
                                    if (typea == i)
                                        break;
                                }
                            } while (true);
                            trackb = Math.abs(aux[trackb]);
                        } while (p != 0);
                    }
            }
        }
        for (i = 1; i <= q; i++)
            index[i] = Math.abs(index[i]);
        if (aux[1] > 0) {
            component[0] = numcomp;
            return;
        }
        for (j = 1; j <= edges; j++)
            aux[j] = Math.abs(aux[j]);
        typea = 1;
        for (i = 1; i <= numcomp; i++) {
            typeb = numnodes[i];
            numnodes[i] = typeb - typea + 1;
            typea = typeb;
        }
        component[0] = numcomp;
    }

}
