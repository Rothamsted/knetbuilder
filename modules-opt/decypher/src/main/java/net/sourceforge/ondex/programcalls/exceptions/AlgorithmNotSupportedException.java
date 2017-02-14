package net.sourceforge.ondex.programcalls.exceptions;

public class AlgorithmNotSupportedException extends Exception {

	private static final long serialVersionUID = 1L;

	public AlgorithmNotSupportedException(String algorithm) {
		super(algorithm);
	}
	
}
