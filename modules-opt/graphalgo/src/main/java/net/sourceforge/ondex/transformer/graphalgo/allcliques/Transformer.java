package net.sourceforge.ondex.transformer.graphalgo.allcliques;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.transformer.graphalgo.GraphAlgoTransformer;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds all maximal independent sets by an enumerative tree search.
 * [Lau07-2.11]
 *
 * @author taubertj
 */
public class Transformer extends GraphAlgoTransformer
{

    @Override
    public String getName() {
        return "allcliques";
    }

    @Override
    public String getVersion() {
        return "24.11.2008";
    }

    @Override
    public String getId() {
        return "graphalgo all cliques clustering";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void start() throws Exception {

        // start method
        int n = numberOfNodes();
        int m = numberOfEdges();
        int[][] array = graphAsArray();
        int[] nodei = array[0];
        int[] nodej = array[1];
        int[][] clique = new int[m][n + 1];
        allCliques(n, m, nodei, nodej, clique);

        // create attribute name
        AttributeName an = graph.getMetaData().getAttributeName(
                getIdentifier());
        if (an == null)
            an = graph.getMetaData().getFactory().createAttributeName(getIdentifier(),
                    java.util.List.class);

        // parse results
        System.out.println("Number of cliques = " + clique[0][0]);
        for (int i = 1; i <= clique[0][0]; i++) {
            System.out.print("\nnodes of clique " + i + ": ");
            for (int j = 1; j <= clique[i][0]; j++) {
                System.out.printf("%3d", clique[i][j]);
                ONDEXConcept c = getConceptForIndex(clique[i][j]);
                Attribute attribute = c.getAttribute(an);
                if (attribute == null) {
                    List<Integer> list = new ArrayList<Integer>();
                    list.add(Integer.valueOf(i));
                    c.createAttribute(an, list, false);
                } else {
                    List<Integer> list = (List<Integer>) attribute.getValue();
                    list.add(Integer.valueOf(i));
                    attribute.setValue(list);
                }
            }
        }
        System.out.println();
    }

    private void allCliques(int n, int m, int nodei[], int nodej[],
                            int clique[][]) {
        int i, j, k, level, depth, num, numcliques, small, nodeu, nodev, nodew = 0;
        int sum, p, up, low, index1, index2, indexv = 0;
        int currentclique[] = new int[n + 1];
        int aux1[] = new int[n + 1];
        int aux2[] = new int[n + 1];
        int notused[] = new int[n + 2];
        int used[] = new int[n + 2];
        int firstedges[] = new int[n + 2];
        int endnode[] = new int[m + m + 1];
        int stack[][] = new int[n + 2][n + 2];
        boolean join, skip, hop;

        // set up the forward star representation of the graph
        k = 0;
        for (i = 1; i <= n; i++) {
            firstedges[i] = k + 1;
            for (j = 1; j <= m; j++) {
                if (nodei[j] == i) {
                    k++;
                    endnode[k] = nodej[j];
                }
                if (nodej[j] == i) {
                    k++;
                    endnode[k] = nodei[j];
                }
            }
        }
        firstedges[n + 1] = k + 1;
        level = 1;
        depth = 2;
        for (i = 1; i <= n; i++)
            stack[level][i] = i;
        numcliques = 0;
        num = 0;
        used[level] = 0;
        notused[level] = n;
        while (true) {
            small = notused[level];
            nodeu = 0;
            aux1[level] = 0;
            while (true) {
                nodeu++;
                if ((nodeu > notused[level]) || (small == 0))
                    break;
                index1 = stack[level][nodeu];
                sum = 0;
                nodev = used[level];
                while (true) {
                    nodev++;
                    if ((nodev > notused[level]) || (sum >= small))
                        break;
                    p = stack[level][nodev];
                    if (p == index1)
                        join = true;
                    else {
                        join = false;
                        low = firstedges[p];
                        up = firstedges[p + 1];
                        if (up > low) {
                            up--;
                            for (k = low; k <= up; k++)
                                if (endnode[k] == index1) {
                                    join = true;
                                    break;
                                }
                        }
                    }
                    // store up the potential candidate
                    if (!join) {
                        sum++;
                        indexv = nodev;
                    }
                }
                if (sum < small) {
                    aux2[level] = index1;
                    small = sum;
                    if (nodeu <= used[level])
                        nodew = indexv;
                    else {
                        nodew = nodeu;
                        aux1[level] = 1;
                    }
                }
            }
            // backtrack
            aux1[level] += small;
            while (true) {
                hop = false;
                if (aux1[level] <= 0) {
                    if (level <= 1)
                        return;
                    level--;
                    depth--;
                    hop = true;
                }
                if (!hop) {
                    index1 = stack[level][nodew];
                    stack[level][nodew] = stack[level][used[level] + 1];
                    stack[level][used[level] + 1] = index1;
                    index2 = index1;
                    nodeu = 0;
                    used[depth] = 0;
                    while (true) {
                        nodeu++;
                        if (nodeu > used[level])
                            break;
                        p = stack[level][nodeu];
                        if (p == index2)
                            join = true;
                        else {
                            join = false;
                            low = firstedges[p];
                            up = firstedges[p + 1];
                            if (up > low) {
                                up--;
                                for (k = low; k <= up; k++)
                                    if (endnode[k] == index2) {
                                        join = true;
                                        break;
                                    }
                            }
                        }
                        if (join) {
                            used[depth]++;
                            stack[depth][used[depth]] = stack[level][nodeu];
                        }
                    }
                    notused[depth] = used[depth];
                    nodeu = used[level] + 1;
                    while (true) {
                        nodeu++;
                        if (nodeu > notused[level])
                            break;
                        p = stack[level][nodeu];
                        if (p == index2)
                            join = true;
                        else {
                            join = false;
                            low = firstedges[p];
                            up = firstedges[p + 1];
                            if (up > low) {
                                up--;
                                for (k = low; k <= up; k++)
                                    if (endnode[k] == index2) {
                                        join = true;
                                        break;
                                    }
                            }
                        }
                        if (join) {
                            notused[depth]++;
                            stack[depth][notused[depth]] = stack[level][nodeu];
                        }
                    }
                    num++;
                    currentclique[num] = index2;
                    if (notused[depth] == 0) {
                        // found a clique
                        numcliques++;
                        clique[numcliques][0] = num;
                        for (i = 1; i <= num; i++)
                            clique[numcliques][i] = currentclique[i];
                        clique[0][0] = numcliques;
                    } else {
                        if (used[depth] < notused[depth]) {
                            level++;
                            depth++;
                            break;
                        }
                    }
                }
                while (true) {
                    num--;
                    used[level]++;
                    if (aux1[level] > 1) {
                        nodew = used[level];
                        // look for candidate
                        while (true) {
                            nodew++;
                            p = stack[level][nodew];
                            if (p == aux2[level])
                                continue;
                            low = firstedges[p];
                            up = firstedges[p + 1];
                            if (up <= low)
                                break;
                            up--;
                            skip = false;
                            for (k = low; k <= up; k++)
                                if (endnode[k] == aux2[level]) {
                                    skip = true;
                                    break;
                                }
                            if (!skip)
                                break;
                        }
                    }
                    aux1[level]--;
                    break;
                }
            }
        }
    }

}
