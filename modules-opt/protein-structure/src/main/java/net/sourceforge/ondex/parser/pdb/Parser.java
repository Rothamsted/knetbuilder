package net.sourceforge.ondex.parser.pdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.pdb.sink.ProteinProtoType;
import net.sourceforge.ondex.parser.pdb.transformer.ProteinTransformer;
import net.sourceforge.ondex.tools.ziptools.ZInputStream;

/**
 * PDB Parser. The Parser searches in a folder for all .enz-files and parse
 * them.
 * 
 * @author peschr
 */
public class Parser extends ONDEXParser {

	/**
	 * Can be used to list files with a ent.z file extension.
	 * 
	 * @author peschr
	 */
	class EntDotZFilter implements FileFilter {
		public boolean accept(File file) {
			String name = file.getName().toLowerCase();
			return name.endsWith("ent.z") && file.isFile();
		}
	}

	/**
	 * @author peschr
	 */
	class NoMoreInterestingDataException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	/**
	 * Can be used to filter all not realeated files
	 * 
	 * @author peschr
	 */
	class PDBFileNameFilter implements FileFilter {
		private HashSet<String> fileNames = new HashSet<String>();
		private String postfix = ".ent.z";
		private String prefix = "pdb";

		public boolean accept(File file) {
			String name = file.getName().toLowerCase();
			return fileNames.contains(name) && file.isFile();
		}

		public void addFileName(String fileName) {
			fileNames.add(prefix + fileName.toLowerCase() + postfix);
		}
	}

	// regular expression for ECNumbers
	private static Pattern ecNumber = Pattern
			.compile("E[.]{0,1}C[.|:]{0,1}\\s*([1-9.]*)");

	private ProteinProtoType protein;

	private ProteinTransformer transformer;

	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[] { new FileArgumentDefinition(
				FileArgumentDefinition.INPUT_DIR, "directory with PDB files",
				true, true, true, false) };

	}

	public ONDEXGraph getGraph() {
		return graph;
	}

	@Override
	public String getId() {
		return "pdb";
	}

	public String getName() {
		return new String("PDB flat file parser");
	}

	public String getVersion() {
		return new String("28.11.2007");
	}

	/**
	 * Extract the information. A NoMoreInterestingDataException will be thrown,
	 * if there is no more interesting data to parse ...
	 * 
	 * @param key
	 * @param value
	 * @throws NoMoreInterestingDataException
	 */
	private void parseLine(String key, String value)
			throws NoMoreInterestingDataException {
		if (key.equals("DBREF")) {
			int offset = 2;
			String dbName = null;
			String parsedDbName = value.substring(offset + 19, offset + 26);
			if (parsedDbName.equals("GB"))
				dbName = MetaData.CV_GenBank;
			else if (parsedDbName.equals("UNP")) {
				dbName = MetaData.CV_UniProt;
			} else {
				return;
			}
			String accession1 = value.substring(offset + 26, offset + 35);
			int endPos;
			if (value.length() < offset + 45) {
				endPos = value.length();
			} else {
				endPos = offset + 45;
			}
			String accession2 = value.substring(offset + 35, endPos);
			protein.addDblinks(dbName, accession1, accession2);
		} else if (key.equals("HEADER")) {
			int dateTerminater = value.lastIndexOf("-");
			protein.setAccessionNr(value.substring(dateTerminater + 3,
					dateTerminater + 10).trim().toLowerCase());
		} else if (key.equals("COMPND")) {
			Matcher m = ecNumber.matcher(value);
			if (m.matches()) {
				protein.setEcNumber(m.group(1));
			}
		} else if (key.equals("SEQRES")) {
			throw new NoMoreInterestingDataException();
		}
	}

	/**
	 * Iterates over all files in the inputDir and pass them to the parseLine
	 * function.
	 * 
	 * @param filter
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void ReadFiles(FileFilter filter) throws FileNotFoundException,
			IOException, InvalidPluginArgumentException {
		File path = new File((String) args
				.getUniqueValue(FileArgumentDefinition.INPUT_DIR));

		File[] listFiles = path.listFiles(filter);

		if (listFiles == null) {
			ONDEXEventHandler
					.getEventHandlerForSID(graph.getSID())
					.fireEventOccurred(
							new GeneralOutputEvent(
									"Input dir is not accessible or no pdb files are loacated there",
									Parser.class.toString()));
			return;
		}
		for (int i = 0; i < listFiles.length; i++) {
			protein = new ProteinProtoType();

			if (listFiles[i].isFile()) {
				ZInputStream un = new ZInputStream(new FileInputStream(
						listFiles[i]));

				BufferedReader input = new BufferedReader(
						new InputStreamReader(un));
				String line;
				try {
					line = input.readLine();
				} catch (Exception e) {
					ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
							.fireEventOccurred(
									new GeneralOutputEvent(
											"Structure can not be read ("
													+ listFiles[i].getName(),
											Parser.class.toString()));
					continue;
				}
				boolean noMoreInterestingData = false;
				while ((line) != null) {
					if (noMoreInterestingData)
						continue;
					int index = line.indexOf(" ");
					String key = line.substring(0, index);
					String value = line.substring(index);
					try {
						this.parseLine(key, value);
					} catch (NoMoreInterestingDataException e) {
						noMoreInterestingData = true;
						break;
					}
					try {
						line = input.readLine();
					} catch (Exception e) {
						ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
								.fireEventOccurred(
										new GeneralOutputEvent(
												"Structure can not be read ("
														+ listFiles[i]
																.getName(),
												Parser.class.toString()));
						break;
					}
				}

			}
			protein.setPdbFileName(listFiles[i].getAbsolutePath());
			transformer.transform(protein);
		}
	}

	@Override
	public String[] requiresValidators() {
		return null;
	}

	public void setGraph(ONDEXGraph graph) {
		this.graph = graph;
	}

	/**
	 * setups the filters
	 */
	private FileFilter setupFilters(Set<ONDEXConcept> concepts, DataSource targetDataSource) {
		PDBFileNameFilter filter = new PDBFileNameFilter();
		for (ONDEXConcept concept : concepts) {
			for (ConceptAccession accession : concept.getConceptAccessions()) {
				if (!accession.getElementOf().equals(targetDataSource))
					continue;
				filter.addFileName(accession.getAccession());
			}
		}
		ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
				.fireEventOccurred(
						new GeneralOutputEvent("found "
								+ filter.fileNames.size()
								+ " pdb references in current graph",
								Parser.class.toString()));

		return filter.fileNames.size() == 0 ? new EntDotZFilter() : filter;
	}

	public void start() {
		FileFilter filter = setupFilters(graph.getConceptsOfConceptClass(graph
				.getMetaData().getConceptClass(MetaData.CC_ProtFam)), graph
				.getMetaData().getDataSource(MetaData.CV_PDB));

		try {
			transformer = new ProteinTransformer(graph);
			ReadFiles(filter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
