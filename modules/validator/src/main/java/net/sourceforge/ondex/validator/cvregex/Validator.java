package net.sourceforge.ondex.validator.cvregex;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.PropertyResourceBundle;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.validator.AbstractONDEXValidator;

import org.apache.log4j.Level;

/**
 * NB this returns predicted DataSource's based on given concept accessions, the
 * validate method is thread safe
 * 
 * @author hindlem (moved original author unknown)
 */
public class Validator extends AbstractONDEXValidator {

	private PropertyResourceBundle prb = null;

	@Override
	public void setArguments(ONDEXPluginArguments va)
			throws InvalidPluginArgumentException {
		this.va = new ONDEXPluginArguments(new ArgumentDefinition<?>[0]);

		// construct filename
		File dir = new File((String) va
				.getUniqueValue(FileArgumentDefinition.INPUT_DIR));
		String filename = dir.getAbsolutePath() + File.separator
				+ "regex.properties";

		GeneralOutputEvent so = new GeneralOutputEvent(
				"Using regex properties file " + filename, "");
		so.setLog4jLevel(Level.INFO);
		fireEventOccurred(so);

		// reads the regular expressions from a .properties file
		try {
			BufferedInputStream fis = new BufferedInputStream(
					new FileInputStream(filename));
			prb = new PropertyResourceBundle(fis);
		} catch (FileNotFoundException fe) {
			fireEventOccurred(new DataFileMissingEvent(fe.getMessage(), ""));
		} catch (IOException ioe) {
			fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(), ""));
		}
	}

	private HashMap<String, Pattern> insensitiveRegex = new HashMap<String, Pattern>();
	private HashMap<String, Pattern> sensitiveRegex = new HashMap<String, Pattern>();

	private Pattern patternSpace = Pattern.compile(" ");

	private ReentrantLock resourceLock = new ReentrantLock();

	@Override
	public Object validate(Object o) {

		if (o instanceof String) {

			// database accession that has to be checked
			String accession = (String) o;

			resourceLock.lock();
			try {
				String cv = null;

				Enumeration<String> en = prb.getKeys();

				while (en.hasMoreElements()) {

					String key = en.nextElement();
					String[] regexAndTag = patternSpace.split(prb
							.getString(key));

					// for case-insensitive matching
					if ((regexAndTag.length == 3)
							&& (regexAndTag[2].equals("1"))) {
						Pattern p = insensitiveRegex.get(regexAndTag[0]);
						if (p == null) {
							p = Pattern.compile(regexAndTag[0],
									Pattern.CASE_INSENSITIVE);
							insensitiveRegex.put(regexAndTag[0], p);
						}
						Matcher m = p.matcher(accession);
						if (m.matches()) {
							cv = regexAndTag[1];
						}
					}

					// for case-sensitive matching (default)
					else {
						Pattern p = sensitiveRegex.get(regexAndTag[0]);
						if (p == null) {
							p = Pattern.compile(regexAndTag[0]);
							sensitiveRegex.put(regexAndTag[0], p);
						}
						Matcher m = p.matcher(accession);
						if (m.matches()) {
							cv = regexAndTag[1];
						}
					}
				}
				return cv;
			} finally {
				resourceLock.unlock();
			}
		}
		return null;
	}

	@Override
	public void cleanup() {
		sensitiveRegex.clear();
		insensitiveRegex.clear();
		prb = null;
	}

	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[] { new FileArgumentDefinition(
				FileArgumentDefinition.INPUT_DIR,
				"directory containing regex.properties", true, true, true,
				false) };
	}

	public String getName() {
		return "validate datasource cv by regular expression";
	}

	public String getVersion() {
		return "1";
	}

	@Override
	public String getId() {
		return "cvregex";
	}

}
