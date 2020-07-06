package net.sourceforge.ondex.args;

/**
 * Interface to define contract for numerical arguments (number).
 * 
 * @author hindlem
 * 
 */
public interface NumericalArgumentDefinition<R extends Number> extends
		ArgumentDefinition<R> {

	/**
	 * Returns range of valid values.
	 * 
	 * @return Range<R>
	 */
	public abstract Range<R> getValueRange();

	/**
	 * Internal class representing a range.
	 * 
	 * @author hindlem
	 * 
	 * @param <V>
	 *            the numerical object
	 */
	class Range<V> {

		// lower bound
		private V lowerRange;

		// upper bound
		private V upperRange;

		/**
		 * Constructor which sets lower and upper range.
		 * 
		 * @param lowerRange
		 *            V
		 * @param upperRange
		 *            V
		 */
		public Range(V lowerRange, V upperRange) {
			this.lowerRange = lowerRange;
			this.upperRange = upperRange;
		}

		/**
		 * Return the lower numerical range.
		 * 
		 * @return V
		 */
		public V getLowerLimit() {
			return lowerRange;
		}

		/**
		 * Returns the upper numerical range.
		 * 
		 * @return V
		 */
		public V getUpperLimit() {
			return upperRange;
		}
	}
}
