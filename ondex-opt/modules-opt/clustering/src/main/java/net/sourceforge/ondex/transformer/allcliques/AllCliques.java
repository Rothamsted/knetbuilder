package net.sourceforge.ondex.transformer.allcliques;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * 
 * @author Hang T. Lau
 *
 */
public class AllCliques {

	/**
	 * 
	 * @param n number of nodes to the simple undirected graph (not necessarily connected but without isolated nodes)
	 * @param m number of edges to the simple undirected graph
	 * @param nodei nodei[m+1]: nodei[p] is the end nodes of the p-th edge in the graph p=1,2...,m
	 * @param nodej nodej[m+1]: nodej[p] is the end nodes of the p-th edge in the graph p=1,2...,m
	 * @param clique int[n][n+1] clique[0][0] gives the total number of cliques in the graph
	 * clique[i][0] gives the total number of nodes in the i-th clique in the graph and the nodes in the i-th clique are clique[i][1], clique[i][2] ...
	 */
	public static List<int[]> allCliques(int n, int m, int nodei[], int nodej[], int minCliques, int maxCliques) {
		int i, j, k, level, depth, num, small, nodeu, nodev, nodew = 0;
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

		List<int[]> cliques = new ArrayList<int[]>(n);
		
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
						return cliques;
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
						if (num >= minCliques && num <= maxCliques) {
							int[] newArray = Arrays.copyOfRange(currentclique, 1, num+1);
							Arrays.sort(newArray);
							cliques.add(newArray);
						}
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

	  public static void main(String args[]) {
		
		 int n=8;
		 int m=12;
		 int nodei[] = {0, 2, 4, 1, 3, 5, 3, 2, 6, 1, 2, 3, 7};
		 int nodej[] = {0, 3, 5, 6, 4, 1, 5, 1, 5, 4, 6, 1, 8};
		
		HashSet<Integer> nodes = new HashSet<Integer>();
		
		for (int i: nodei) {
			nodes.add(i);
		}
		for (int i: nodej) {
			nodes.add(i);
		}

		List<int[]> cliques = allCliques(n, m, nodei, nodej, 3, Integer.MAX_VALUE);
		System.out.println("Number of cliques = " + cliques.size());
		for (int[] clique: cliques) {
			System.out.print("\nnodes of clique " + clique.length + ": ");
			for (int i: clique)
				System.out.printf("%3d", i);
		}
		System.out.println();
	}
	
}
