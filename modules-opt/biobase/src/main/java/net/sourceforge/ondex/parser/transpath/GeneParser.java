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
 * Parser for parsing the TP gene.dat
 *
 * @author taubertj
 */
class GeneParser extends AbstractTPParser {

    /**
     * Constructor inits superclass.
     *
     * @param conceptWriter - ConceptWriter
     * @param pa            - ParserArguments
     */
    protected GeneParser(ConceptWriter conceptWriter, ONDEXPluginArguments pa) throws InvalidPluginArgumentException {
        super(conceptWriter, pa);
    }

    @Override
    protected void start() throws IOException, InvalidPluginArgumentException {

        Parser.propagateEventOccurred(new GeneralOutputEvent("GeneParser started...", "start()"));

        File dir = new File((String) pa.getUniqueValue(FileArgumentDefinition.INPUT_DIR));


        BufferedReader br = new BufferedReader(
                new FileReader(dir.getAbsolutePath() +
                        File.separator + "gene.dat"));

        // kick the first 3 lines of the file (just copyright etc.
        br.readLine();
        br.readLine();
        br.readLine();

        // iterator over all lines of file
        Gene g = null;
        Publication pub = null;
        while (br.ready()) {

            // get next line
            String line = br.readLine();

            if (line.startsWith("AC")) {
                String acc = parseLine(line);
                if (acc.length() < 1) {
                    Parser.propagateEventOccurred(
                            new DataFileErrorEvent("Invalid accession skiping entry: " + line, "start()"));
                    g = null;
                    continue;
                } else {
                    g = new Gene(acc);
                }
            } else if (line.startsWith("AS")) {
                Iterator<String> it = parseSplit(parseLine(line)).iterator();
                while (it.hasNext()) {
                    g.addAccessionAlternative(it.next());
                }
            } else if (line.startsWith("NA")) {
                g.setName(parseLine(line));
            } else if (line.startsWith("SY")) {
                Iterator<String> it = parseSplit(parseLine(line)).iterator();
                while (it.hasNext()) {
                    g.addSynonym(it.next());
                }
            } else if (line.startsWith("OS")) {
                g.setSpecies(parseSpecies(parseLine(line)));
            } else if (line.startsWith("DR")) {
                DBlink link = parseDatabaseLink(parseLine(line));
                if (link != null) {
                    g.addDatabaseLink(link);
                }
            } else if (line.startsWith("RX")) {
                String type = line.substring(2, line.indexOf(':')).toLowerCase();
                if (type.contains("ubmed")) {
                    pub = new Publication(line.replaceAll("[^0-9]", ""));
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
            } else if (pub != null && line.startsWith("XX")) {
                g.addPublication(pub);
                pub = null;
            }

            // end of entry (!!)
            if (line.startsWith("//")) {
                conceptWriter.createGene(g);
                g = null;
            }
        }

        br.close();
        br = null;

        // print all clashed taxa
        Iterator<String> it = clashedTaxa.iterator();
        while (it.hasNext()) {
            Parser.propagateEventOccurred(
                    new DataFileErrorEvent("No taxid found for " + it.next(), "start()"));
        }

        Parser.propagateEventOccurred(new GeneralOutputEvent("GeneParser finished...", "start()"));
	}
}