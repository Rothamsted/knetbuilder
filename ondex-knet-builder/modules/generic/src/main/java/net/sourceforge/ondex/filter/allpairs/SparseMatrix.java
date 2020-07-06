package net.sourceforge.ondex.filter.allpairs;

import java.util.HashMap;

/**
 * Implements a sparse matrix based on a hashtable. 
 * This may grow bigger than an actual full matrix if filled
 * more than one third!
 * 
 * @author Jochen Weile, B.Sc.
 *
 * @param <E>
 */
public class SparseMatrix<E> extends Matrix<E> {

	//####FIELDS####
	
	
	/**
	 * The Hashtable that stores the data.
	 */
	private HashMap<Integer,E> map;
	
	
	//####CONSTRUCTOR####
	
	/**
	 * initialises the sparse matrix.
	 */
	public SparseMatrix(int size, boolean directed, E defaultVal) {
		super(size,directed,defaultVal);
		map = new HashMap<Integer,E>();
	}

	
	//####METHODS####
	
	/**
	 * gets an entry from the matrix.
	 */
	public E get(int i, int j) {
		if (i == j)
			return defaultVal;
		if (!directed && (i < j)) { // then switch positions to fit half sized matrix
			int dummy = i;
			i = j;
			j = dummy;
		}
		E out = map.get(genKey(i,j));

		return (out != null) ? out : defaultVal; 
	}

	/**
	 * puts an entry into the matrix.
	 * @see net.sourceforge.ondex.filter.allpairs.Matrix#set(int, int, java.lang.Object)
	 */
	public void set(int i, int j, E v) {
		if (i == j) 
			return;
		if (!directed && (i < j)) { // then switch positions to fit half sized matrix
			int dummy = i;
			i = j;
			j = dummy;
		}
		
		map.put(genKey(i,j), v);

	}
	
	/**
	 * generates a unique string for the pair of integers given.
	 * it can be used as a key for the hashtable.
	 * @param i row index
	 * @param j column index
	 * @return
	 */
	private int genKey(int i, int j) {
		return (i * size) + j;
	}
	
}
