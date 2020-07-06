package net.sourceforge.ondex.filter.allpairs;

/**
 * This abstract nxn-matrix can either be implemented as a sparse matrix
 * or as a full array-based matrix.
 * 
 * @author Jochen Weile, B.Sc.
 *
 * @param <E> The type stored in the matrix.
 */
public abstract class Matrix<E> {

	//####FIELDS####
	
	/**
	 * The number of rows = number of columns
	 */
	protected int size;
	
	/**
	 * Determines whether index pairs are considered directed or not.
	 * (If not, only half of the matrix must be created and space can be
	 * saved).
	 */
	protected boolean directed;
	
	/**
	 * All matrix entries are initialized with this value.
	 */
	protected E defaultVal;
	
	/**
	 * the number of elements currently stored in the matrix.
	 */
	protected int entries = 0;

	//####CONSTRUCTOR####
	
	/**
	 * standard constructor.
	 */
	public Matrix(int size, boolean directed, E defaultVal) {
		this.size = size;
		this.directed = directed;
		this.defaultVal = defaultVal;
	}

	//####METHODS####
	
	/**
	 * gets an entry from the matrix.
	 */
	public abstract E get(int i, int j);
	
	/**
	 * stores a value in the matrix.
	 * @param i the row in which to store.
	 * @param j the column in which to store.
	 * @param v the value to be stored.
	 */
	public abstract void set(int i, int j, E v);
	
	/**
	 * returns what kind of matrix representation should be used for the next
	 * instance. Sparse matrices are more efficient then full matrices if they 
	 * are filled up to one third. if they are filled beyond that threshold it
	 * is more efficient to use a full matrix instead. 
	 * @return A string saying either "sparse" or "full" determined by the fill 
	 * ratio of the matrix.
	 */
	public String suggestNextInstance() {
		if (entries < (int)(Math.pow((double)size,2) * 0.09))
			return "sparse";
		else
			return "full";
	}
}
