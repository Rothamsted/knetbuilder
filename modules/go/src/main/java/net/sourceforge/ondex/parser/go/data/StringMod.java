package net.sourceforge.ondex.parser.go.data;

import java.util.StringTokenizer;

/**
 * Class contains static methods for String modifications.
 * 
 * @author taubertj
 * 
 */
public class StringMod {

	/**
	 * Transfering a given EC number into a four position one (e.g. 1.2 ->
	 * 1.2.-.-)
	 * 
	 * @param s
	 *            EC number
	 * @return normalized four position EC number
	 */
	public static String fillEC(String s) {

		String[] blocks = s.split("\\.");
		String news = s;

		if (blocks.length < 4) {

			for (int i = blocks.length; i < 4; i++) {

				news = news + ".-";
			}
		}
		return news;
	}

	/**
	 * Remove ending "activity" from a name.
	 * 
	 * @param name
	 *            String
	 * @return String
	 */
	public static String removeActivity(String name) {

		if (name.endsWith(" activity")) {

			name = name.substring(0, name.lastIndexOf(" activity")).trim();
		}
		return name;
	}

	/**
	 * Removes unnecessary content from
	 * 
	 * @param input
	 *            String
	 * @return String
	 */
	public static String removeUnwanted(String input) {

		input = input.replace('_', ' ');
		input = input.replace(',', ' ');
		input = input.replace('/', ' ');
		input = input.replace('\\', ' ');

		String result = input;

		// only remove last "(...)"
		if (result.lastIndexOf("(") > -1
				&& result.lastIndexOf(")") == result.length() - 1) {
			result = result.substring(0, result.lastIndexOf("("));
		}

		result = result.trim();

		return result;
	}

}
