package net.sourceforge.ondex.exception.type;

/**
 * Exception type for data loss due to a failed de-serialisation.
 * 
 * @author Jochen Weile, B.Sc.
 *
 */
public class RetrievalException extends DataLossException {


	//####FIELDS####
	
	/**
	 * serial id.
	 */
	private static final long serialVersionUID = -4956744543349618816L;

	//####CONSTRUCTOR####
	
	/**
	 * standard constructor.
	 */
	public RetrievalException() {
		
	}

	/**
	 * constructor for custom message.
	 * @param msg
	 */
	public RetrievalException(String msg) {
		super(msg);
	}


	//####METHODS####
}
