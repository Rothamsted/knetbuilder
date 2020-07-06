package net.sourceforge.ondex.parser.transfac.matrix;

import net.sourceforge.ondex.parser.transfac.AbstractTFObject;


/**
 * Class representing a Matrix object in TF.
 * 
 * @author taubertj
 *
 */
public class MatrixObject extends AbstractTFObject {
	
	// representing the associated matrix
	private String matrix = null;

	/**
	 * Constructor for a unique TF accession.
	 * 
	 * @param accession - String
	 */
	protected MatrixObject(String accession) {
		super(accession);
	}

	/**
	 * Sets the matrix string.
	 * 
	 * @param matrix - String
	 */
	protected void setMatrix(String matrix) {
		this.matrix = matrix;
	}

	/**
	 * Returns the matrix string.
	 * 
	 * @return String
	 */
	public String getMatrix() {
		return matrix;
	}
}
