package net.sourceforge.ondex.scripting;
/**
 * 
 * @author lysenkoa
 * A special exception that happens when command does not execute correctly
 */
public class FunctionException extends Exception {

	private static final long serialVersionUID = -5949502502301723953L;
	private int param;
	public static final int INVALID_FUNTION_CALL = -6;
	public static final int ENVIRONMENTAL_ARGS_MISSING = 5;
	public static final int BAD_INPUT = -4;
	public static final int NO_SUCH_FUNCTION = -3;
	public static final int MISSING_ARGUMENTS = -2;
	public static final int TOO_MANY_ARGUMENTS = -1;
	/**
	 * 
	 * @param errorMessage - message to convey
	 * @param param - error clue. Positive number - position in the command(command failed to parse), negative number - error type
	 */
	public FunctionException(String errorMessage, int param){
	    super(errorMessage);
	    this.param = param;
	}
	/**
	 * 
	 * @return error clue
	 */
	public int getParam() {
		return param;
	}
}
