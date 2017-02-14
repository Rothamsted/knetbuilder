package net.sourceforge.ondex.parser.transpath;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * Parser for parsing the TP reaction.dat
 *
 * @author taubertj
 */
class ReactionParser extends AbstractTPParser {

    /**
     * Constructor inits superclass.
     *
     * @param conceptWriter - ConceptWriter
     * @param pa            - ParserArguments
     */
    protected ReactionParser(ConceptWriter conceptWriter, ONDEXPluginArguments pa) throws InvalidPluginArgumentException {
        super(conceptWriter, pa);
    }

    @Override
    protected void start() throws IOException, InvalidPluginArgumentException {

        Parser.propagateEventOccurred(new GeneralOutputEvent("ReactionParser started...", "start()"));

        File dir = new File((String) pa.getUniqueValue(FileArgumentDefinition.INPUT_DIR));

        BufferedReader br = new BufferedReader(new FileReader(dir.getAbsolutePath()
                + File.separator + "reaction.dat"));

        // kick the first 3 lines of the file (just copyright etc.
        br.readLine();
        br.readLine();
        br.readLine();

        // iterator over all lines of file
        Reaction r = null;
        Publication pub = null;
        StringBuilder description = null;
        while (br.ready()) {

            // get next line
            String line = br.readLine();

            if (line.startsWith("AC")) {
                String acc = parseLine(line);
                if (acc.length() < 1) {
                    Parser.propagateEventOccurred(
                            new DataFileErrorEvent("Invalid accession skiping entry: " + line, "start()"));
                    r = null;
                    description = null;
                    continue;
                } else {
                    r = new Reaction(acc);
                }
            } else if (line.startsWith("AS")) {
                Iterator<String> it = parseSplit(parseLine(line)).iterator();
                while (it.hasNext()) {
                    r.addAccessionAlternative(it.next());
                }
            } else if (line.startsWith("NA")) {
                r.setName(parseLine(line));
            } else if (line.startsWith("CC")) {
                if (description == null) {
                    description = new StringBuilder(parseLine(line));
                } else {
                    description.append(" " + parseLine(line));
                }
            } else if (line.startsWith("MB")) {
                r.addInMolecule(parseValue(parseLine(line)));
            } else if (line.startsWith("MA")) {
                r.addOutMolecule(parseValue(parseLine(line)));
            } else if (line.startsWith("MC")) {
                r.addCatalysationMolecule(parseValue(parseLine(line)));
            } else if (line.startsWith("MI")) {
                r.addInhibitionMolecule(parseValue(parseLine(line)));
            } else if (line.startsWith("RX")) {
                String type = line.substring(2, line.indexOf(':')).toLowerCase();
                if (type.contains("ubmed")) {
                    pub = new Publication(line.replaceAll("[^0-9]", ""));
                    // ubmed (not pubmed) for one exception
                } else {
                    Parser.propagateEventOccurred(new DataFileErrorEvent(
                            "Unknown publication id :" + type, "start()"));
                }
            } else if (pub != null && line.startsWith("RA")) {
                pub.setAuthors(parseLine(line));
            } else if (pub != null && line.startsWith("RT")) {
                pub.setTitle(parseLine(line));
            } else if (pub != null && line.startsWith("RL")) {
                pub.setSource(parseLine(line));
            } else if (pub != null && line.startsWith("XX")) {
                r.addPublication(pub);
                pub = null;
            }

            // end of entry (!!)
            else if (line.startsWith("//")) {
                if (description != null)
                    r.setDescription(description.toString());
                description = null;
                conceptWriter.createReaction(r);
                r = null;
            }
        }

        br.close();
        br = null;

        Parser.propagateEventOccurred(new GeneralOutputEvent("ReactionParser finished...", "start()"));
	}
	
}
