package net.sourceforge.ondex.validator.ubiquitous;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.validator.AbstractONDEXValidator;

import org.apache.log4j.Level;

/**
 * Dictionary based look up for ubiquitous concepts returns true if chemical
 * name is ubiquitous NB validate(Object o) is thread safe
 * 
 * @author hindlem
 */
public class Validator extends AbstractONDEXValidator {

	private static Vector<String> ubiquitousChemicals = null;

	private ONDEXPluginArguments va = new ONDEXPluginArguments(new ArgumentDefinition<?>[0]);

	/**
	 * reads a ubiquitousChemicals.dic file to the ubiquitousChemicals array
	 * 
	 * @return ubiquitous Chemicals
	 * @throws InvalidPluginArgumentException
	 */
	private Vector<String> readFile() throws InvalidPluginArgumentException {
		File dir = new File((String) va
				.getUniqueValue(FileArgumentDefinition.INPUT_DIR));

		Vector<String> ubiquitousChems = new Vector<String>(100);
		if (dir == null)
			throw new NullPointerException(
					"InputDir not specified in DictionaryUbiquitousLookup Validator arguments");

		String filename = dir.getAbsolutePath() + File.separator
				+ "ubiquitousChemicals.dic";
		GeneralOutputEvent so = new GeneralOutputEvent(
				"Using ubiquitous chemicalse file " + filename, "");
		so.setLog4jLevel(Level.INFO);
		fireEventOccurred(so);

		try {
			final BufferedReader fis = new BufferedReader(new FileReader(
					filename));
			while (fis.ready()) {
				String line = fis.readLine();
				if (line.length() > 0)
					ubiquitousChems.add(line.trim().toUpperCase());
			}
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ubiquitousChems;
	}

	public String getName() {
		return "ubiquitousDictionaryAnalyser";
	}

	public String getVersion() {
		return "1";
	}

	@Override
	public String getId() {
		return "ubiquitous";
	}

	public void setArguments(ONDEXPluginArguments va)
			throws InvalidPluginArgumentException {
		this.va = va;
		if (ubiquitousChemicals == null) {
			ubiquitousChemicals = readFile();
		}
	}

	public ONDEXPluginArguments getArguments() {
		return va;
	}

	@Override
	public Object validate(Object o) {
		if (ubiquitousChemicals == null) {
			return null;
		}
		return ubiquitousChemicals.contains(o.toString().toUpperCase());
	}

	/**
	 * Wrapper method for validate(Object o)
	 * 
	 * @param name
	 *            chemical name
	 * @return if this chemical name is known to be ubiquitous
	 */
	public boolean isUbiquitousConceptName(String name) {
		return (Boolean) validate(name.toUpperCase());
	}

	/**
	 * Tests if either concept names are ubiquitous
	 * 
	 * @param name1
	 *            chemical name
	 * @param name2
	 *            chemical name
	 * @return
	 */
	public boolean eitherIsUbiquitousConceptName(String name1, String name2) {
		if (!isUbiquitousConceptName(name1.toUpperCase()) // does not
				&& !isUbiquitousConceptName(name2.toUpperCase())) {// and does
			// not
			return false;
		}
		return true;
	}

	@Override
	public void cleanup() {
		if (ubiquitousChemicals != null)
			ubiquitousChemicals.clear();
		ubiquitousChemicals = null;
	}

	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[] { new FileArgumentDefinition(
				FileArgumentDefinition.INPUT_DIR,
				"directory containing ubiquitousChemicals.dic", true, true, true, false) };
	}

}
