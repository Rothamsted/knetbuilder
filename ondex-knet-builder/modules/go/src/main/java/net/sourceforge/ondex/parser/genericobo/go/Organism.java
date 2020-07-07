package net.sourceforge.ondex.parser.genericobo.go;

import java.util.StringTokenizer;

/**
 * 
 * @author hoekmanb
 *
 */
public class Organism {

	/**
	 * Get right organism name out of input String.
	 * 
	 * @param input -
	 *            String
	 * @return String
	 */
	public static String getOrganism(String input) {
		String result = "nosensu";

		input = input.toLowerCase();
		input = input.replace(',', ' ');
		input = input.replace('\\', ' ');
		input = input.replace('_', ' ');

		if (input.indexOf("sensu") != -1) {
			StringTokenizer st = new StringTokenizer(input);
			String organism = "";

			boolean org = false;
			while (st.hasMoreTokens()) {
				String token = st.nextToken();

				if (token.indexOf("sensu") != -1) {
					org = true;
					token = st.nextToken();
				}

				if (org) {
					if (token.indexOf(")") != -1) {
						token = token.replace(')', ' ');
						org = false;
					}
					organism = organism + " " + token;
				}
			}
			result = organism.trim();

			// exception for typos in GO
			if (result.compareTo("deuterostoma") == 0)
				result = "deuterostomia";

			if (result.compareTo("arthropda") == 0)
				result = "arthropoda";

			if ((result.compareTo("protostomia and nematoda") == 0)
					|| (result.compareTo("nematoda and protostomia") == 0))
				result = "protostomia";
		}

		if (result.indexOf("research community") > -1) {
			result = "nosensu";
		}

		return result;
	}

}
