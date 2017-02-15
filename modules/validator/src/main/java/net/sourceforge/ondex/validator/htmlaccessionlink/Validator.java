package net.sourceforge.ondex.validator.htmlaccessionlink;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.validator.AbstractONDEXValidator;

/**
 * Return URLs for external links on set conditions
 * 
 * @author hindlem
 */
public class Validator extends AbstractONDEXValidator {

	/**
	 * Constructor for a given session.
	 */
	public Validator() {
		super();
	}

	// private static final String MAPPINGFILE = "mappings.txt";

	// arguments for input/output dir
	private ONDEXPluginArguments va;

	private Map<String, String> urlMap;

	/**
	 * Returns name of this validator.
	 * 
	 * @return String
	 */
	public String getName() {
		return new String("HTML Accession link");
	}

	/**
	 * Returns version of this validator.
	 * 
	 * @return String
	 */
	public String getVersion() {
		return new String("20.08.2008");
	}

	@Override
	public String getId() {
		return "htmlaccessionlink";
	}

	@Override
	public void setArguments(ONDEXPluginArguments va)
			throws InvalidPluginArgumentException {

		File dir = new File((String) va
				.getUniqueValue(FileArgumentDefinition.INPUT_DIR));
		File f = new File(dir.getAbsolutePath() + File.separator
				+ "mappings.txt");

		urlMap = new HashMap<String, String>();

		try {
			int lineNum = 1;

			String cc = null;
			String cv = null;
			String url = "";

			BufferedReader br = new BufferedReader(new FileReader(f));
			while (br.ready()) {
				String line = br.readLine();
				if (line.charAt(0) == '#') {
					continue;
				}

				cc = null;
				cv = null;
				url = "";

				String[] csvItems = line.split(",");
				for (int i = 0; i < csvItems.length; i++) {
					String item = csvItems[i].trim();
					if (item.charAt(0) == '"') {
						int end = item.length();
						if (item.charAt(end - 1) == '"') {
							end = end - 1;
						}
						item = item.substring(1, end);
					}
					switch (i) {
					case 0:
						cv = item;
						break;
					case 1:
						url = item;
						break;
					case 2:
						cc = item;
						break;
					default:
						System.err.println("mappings.txt is invalid at line "
								+ lineNum);
					}
					lineNum++;
				}
				Condition condition = null;
				if (cc != null && cv != null)
					condition = new Condition(cv, cc);
				else if (cv != null) {
					condition = new Condition(cv);
				}

				if (condition != null) {
					urlMap.put(condition.toString(), url);
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ONDEXPluginArguments getArguments() {
		return va;
	}

	@Override
	public Object validate(Object o) {
		if (o instanceof Condition) {
			String url = urlMap.get(o.toString());
			if (url == null) {
				// try a more general condition
				url = urlMap.get(new Condition(((Condition) o).getCv())
						.toString());
			}
			return url;
		} else if (o instanceof DataSource) {
			return urlMap.get(new Condition(((DataSource) o).getId()).toString());
		} else if (o instanceof String) {
			return urlMap.get(new Condition(o.toString()).toString());
		}

		throw new RuntimeException("Object of type " + o.getClass()
				+ " is not a valid paramiter");
	}

	@Override
	public void cleanup() {
		urlMap.clear();
	}

	/**
	 * Requires no special arguments.
	 */
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[] { new FileArgumentDefinition(
				FileArgumentDefinition.INPUT_DIR,
				"directory containing mappings.txt", true, true, true, false) };
	}

}
