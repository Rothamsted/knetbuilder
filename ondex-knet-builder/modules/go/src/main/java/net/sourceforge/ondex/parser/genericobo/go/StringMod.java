package net.sourceforge.ondex.parser.genericobo.go;

import java.util.StringTokenizer;

/**
 * 
 * @author hoekmanb
 *
 */
public class StringMod {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

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

	public static String removeBlanks(String s) {
		String ret = s;
		String newString = "";

		StringTokenizer st = new StringTokenizer(s);

		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			newString = newString + token;
		}

		ret = newString;
		return ret;
	}

	public static String removeActivity(String name) {

		if (name.endsWith("activity")) {

			name = name.substring(0, name.lastIndexOf("activity")).trim();
		}
		return name;
	}

	public static String removeUnwanted(String input) {

		input = input.replace('_', ' ');
		input = input.replace(',', ' ');
		input = input.replace('/', ' ');
		input = input.replace('\\', ' ');

		StringTokenizer st = new StringTokenizer(input);

		String backup_input = input;
		String result = input;
		boolean take = true;

		if (st.countTokens() > 1) { // remove tokens in "(...)" only if there is
			// more then one token
			result = "";
			while (st.hasMoreTokens()) {
				String token = st.nextToken();

				if (token.indexOf("(") != -1)
					take = false;
				if ((token.indexOf(")") != -1) && !take)
					take = true;

				if ((take) && (token.indexOf(")") == -1))
					result = result + " " + token;
			}
		}

		if (result.length() == 0) // if the result is still empty ...
			result = backup_input;

		result = result.trim();

		return result;
	}

}
