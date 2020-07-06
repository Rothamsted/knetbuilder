package net.sourceforge.ondex.algorithm.graphquery;


/**
 * A basic insertion sort for an int[] based on the position of ints in the second array
 * @author hindlem
 *
 */
public class IntSort {

	/**
	 * A basic insertion sort for short arrays (<7) (NB. Quicksort slow for presorted arrays which we expect)
	 * @param toSort the array to sort based on the position of int in subjectSet
	 * @param off the start point to sort
	 * @param len the last point to sort
	 * @param subjectSet the array to look up the position of ints in
	 */
	public static void sortArrayByFirstInstanceInArray(int[] toSort, int off,
			int len, int[] subjectSet) {
		// Insertion sort on smallest arrays
		for (int i = off; i < len + off; i++)
			for (int j = i; j > off
					&& firstInstance(subjectSet, toSort[j - 1]) > 
					   firstInstance(subjectSet, toSort[j]); j--)
				    swap(toSort, j, j - 1);
	}

	/**
	 * Swaps x[a] with x[b].
	 * @param x array to swap in
	 * @param a position to swap
	 * @param b position to swap
	 */
	private static void swap(int x[], int a, int b) {
		int t = x[a];
		x[a] = x[b];
		x[b] = t;
	}

	/**
	 * returns position of first v in a[]
	 * @param a array to search in
	 * @param v value
	 * @return position of first v in a[]
	 */
	private static int firstInstance(int[] a, int v) {
		for (int i = 0; i < a.length; i++) {
			int j = a[i];
			if(j==v) return i;
		}
		return -1; ///umm is this wise?
	}

}
