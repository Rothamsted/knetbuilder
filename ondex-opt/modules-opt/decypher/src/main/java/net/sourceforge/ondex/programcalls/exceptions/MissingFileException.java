package net.sourceforge.ondex.programcalls.exceptions;

public class MissingFileException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public MissingFileException(String file) {
		super(file);
	}
	
}
