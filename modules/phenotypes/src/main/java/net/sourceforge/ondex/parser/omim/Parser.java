package net.sourceforge.ondex.parser.omim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.ONDEXParser;

import org.apache.log4j.Level;

public class Parser extends ONDEXParser
{

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[] { new FileArgumentDefinition(
				FileArgumentDefinition.INPUT_DIR, "directory with OMIM files",
				true, true, true, false) };
	}

	public String getName() {
		return "OMIM Parser";
	}

	public String getVersion() {
		return "14.07.2008";
	}

	@Override
	public String getId() {
		return "omim";
	}

	public void start() throws InvalidPluginArgumentException {
		GeneralOutputEvent goe = new GeneralOutputEvent(
				"Starting OMIM parsing...", "[Parser - setONDEXGraph]");
		goe.setLog4jLevel(Level.INFO);
		fireEventOccurred(goe);

		// check DataSource
		DataSource dataSourceOmim = graph.getMetaData().getDataSource(MetaData.CV_OMIM);
		if (dataSourceOmim == null) {
			this.fireEventOccurred(new DataSourceMissingEvent(MetaData.CV_OMIM,
					"[Parser - setONDEXGraph]"));
		}
		// check CC
		ConceptClass ccOmim = graph.getMetaData().getConceptClass(
				MetaData.CC_DISEASE);
		if (ccOmim == null) {
			this.fireEventOccurred(new ConceptClassMissingEvent(
					MetaData.CC_DISEASE, "[Parser - setONDEXGraph]"));
		}
		// check ET
		EvidenceType et = graph.getMetaData().getEvidenceType(MetaData.ET);
		if (et == null) {
			this.fireEventOccurred(new EvidenceTypeMissingEvent(MetaData.ET,
					"[Parser - setONDEXGraph]"));
		}

		File path = new File((String) args
				.getUniqueValue(FileArgumentDefinition.INPUT_DIR));

		String fileName = path.getAbsolutePath() + File.separator
				+ "morbidmap.txt";

		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));

			String inputline = br.readLine();
			while (br.ready()) {
				System.out.println(inputline);
				inputline = br.readLine();
			}
			br.close();

			goe = new GeneralOutputEvent("Successfully read file.",
					"[Extractor - extractClasses]");
			goe.setLog4jLevel(Level.INFO);
			fireEventOccurred(goe);

		} catch (FileNotFoundException fnfe) {
			fireEventOccurred(new DataFileMissingEvent(fnfe.getMessage(),
					"[Parser - setONDEXGraph]"));
		} catch (IOException ioe) {
			fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
					"[Parser - setONDEXGraph]"));
		}

	}

}
