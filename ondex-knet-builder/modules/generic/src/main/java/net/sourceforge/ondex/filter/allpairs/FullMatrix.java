package net.sourceforge.ondex.filter.allpairs;

/**
 * Implements a full matrix based on a 2D array.
 * 
 * @author Jochen Weile, B.Sc.
 *
 * @param <E>
 */
public class FullMatrix<E> extends Matrix<E> {

	//####FIELDS####
	
	/**
	 * The actual matrix.
	 */
	private Object[][] matrix;

	//####CONSTRUCTOR####
	
	/**
	 * constructs the matrix. When set to 'undirected' mode, only half
	 * of the matrix is allocated in order to save memory.
	 */
	public FullMatrix(int size, boolean directed, E defaultVal) {
		super(size,directed,defaultVal);
		
		if (!directed) {//then create only half of the matrix
			matrix = new Object[size][]; 
			for (int i = 1; i < size; i++) {
				matrix[i] = new Object[i];
				for (int j = 0; j < i; j++)
					matrix[i][j] = defaultVal;
			}
		}
		else {
			matrix = new Object[size][size];
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					matrix[i][j] = defaultVal;
				}
			}
		}
	}

	//####METHODS####
	
	/**
	 * gets a matrix entry
	 */
	@SuppressWarnings("unchecked")
	public E get(int i, int j) {
		if (i == j)
			return defaultVal;
		if (!directed && (i < j)) { // then switch positions to fit half sized matrix
			int dummy = i;
			i = j;
			j = dummy;
		}
		Object out = matrix[i][j];
		if (out != null)
			return (E) out;
		else 
			return null;
	}

	/**
	 * sets a matrix entry
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
		matrix[i][j] = v;
		entries++;
	}
	
	
}
