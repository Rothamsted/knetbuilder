package net.sourceforge.ondex.args;

/**
 * Interface to define contract for non continuous arguments (discrete).
 * 
 * @author hindlem
 *
 */
public interface NonContinuousArgumentDefinition<R> extends ArgumentDefinition<R> {

	/**
	 * Returns a list of valid values.
	 * 
	 * @return R[]
	 */
	public abstract R[] getValidValues();

}
