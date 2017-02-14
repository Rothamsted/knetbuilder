package net.sourceforge.ondex.parser.transfac.matrix;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.transfac.AbstractTFParser;
import net.sourceforge.ondex.parser.transfac.ConceptWriter;
import net.sourceforge.ondex.parser.transfac.Parser;
import net.sourceforge.ondex.parser.transfac.sink.Publication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * Parser for parsing the TF matrix.dat
 *
 * @author taubertj
 */
public class MatrixParser extends AbstractTFParser {

    /**
     * Constructor inits superclass.
     *
     * @param conceptWriter - ConceptWriter
     * @param pa            - ParserArguments
     */
    public MatrixParser(ConceptWriter conceptWriter, ONDEXPluginArguments pa) throws InvalidPluginArgumentException {
        super(conceptWriter, pa);
    }

    public
    @Override
    void start() throws IOException, InvalidPluginArgumentException {

        Parser.propagateEventOccurred(new GeneralOutputEvent(
                "MatrixParser started...", "start()"));

        File dir = new File((String) pa.getUniqueValue(FileArgumentDefinition.INPUT_DIR));


        BufferedReader br = new BufferedReader(new FileReader(dir.getAbsolutePath()
                + File.separator + "matrix.dat"));

        // kick the first 3 lines of the file (just copyright etc.
        br.readLine();
        br.readLine();
        br.readLine();

        // iterator over all lines of file
        MatrixObject m = null;
        Publication pub = null;
        StringBuilder matrix = null;
        while (br.ready()) {

            // get next line
            String line = br.readLine();

            if (line.startsWith("AC")) {
                pub = null;
                String acc = parseLine(line);
                if (acc.length() < 1) {
                    Parser.propagateEventOccurred(new DataFileErrorEvent(
                            "Invalid accession skiping entry: " + line,
                            "start()"));
                    m = null;
                    continue;
                } else {
                    m = new MatrixObject(acc);
                }
            } else if (line.startsWith("AS")) {
                m.addAccessionAlternative(parseLine(line));
            } else if (line.startsWith("NA")) {
                m.setName(parseLine(line));
            } else if (line.startsWith("DE")) {
                m.setDescription(parseLine(line));
            } else if (line.startsWith("RX")) {
                String type = line.substring(2, line.indexOf(':'))
                        .toLowerCase();
                if (type.contains("ubmed")) {
                    String pubmedId = line.replaceAll("[^0-9]", "");
                    if (pubmedId.length() > 0) {
                        pub = new Publication(pubmedId);
                        m.addPublication(pub);
                    }
                    // ubmed (not pubmed) for one exception
                } else {
                    Parser.propagateEventOccurred(new DataFileErrorEvent(
                            "Unknown publication id : " + type, "start()"));
                }
            } else if (pub != null && line.startsWith("RA")) {
                pub.setAuthors(parseLine(line));
            } else if (pub != null && line.startsWith("RT")) {
                pub.setTitle(parseLine(line));
            } else if (pub != null && line.startsWith("RL")) {
                pub.setSource(parseLine(line));
            } else if (line.startsWith("P0")) {
                matrix = new StringBuilder(30);
                matrix.append(parseLine(line) + "\\n");
            } else if (line.substring(0, 2).matches("\\d{2}")) {
                matrix.append(parseLine(line) + "\\n");
            } else if (line.startsWith("//")) {
                m.setMatrix(matrix.toString());
                conceptWriter.createMatrix(m);
                m = null;
                matrix = null;
            }
        }

        br.close();
        br = null;

        // print all clashed taxa
        Iterator<String> it = clashedTaxa.iterator();
        while (it.hasNext()) {
            Parser.propagateEventOccurred(new DataFileErrorEvent(
                    "No taxid found for " + it.next(), "start()"));
        }

        Parser.propagateEventOccurred(new GeneralOutputEvent(
                "MatrixParser finished...", "start()"));
	}
}
