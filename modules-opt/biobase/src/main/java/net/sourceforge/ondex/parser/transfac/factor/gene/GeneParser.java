package net.sourceforge.ondex.parser.transfac.factor.gene;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.transfac.AbstractTFParser;
import net.sourceforge.ondex.parser.transfac.ConceptWriter;
import net.sourceforge.ondex.parser.transfac.DBlink;
import net.sourceforge.ondex.parser.transfac.Parser;
import net.sourceforge.ondex.parser.transfac.sink.Publication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;


/**
 * Parser for parsing the TF gene.dat
 *
 * @author taubertj
 */
public class GeneParser extends AbstractTFParser {

    /**
     * Constructor inits superclass.
     *
     * @param conceptWriter - ConceptWriter
     * @param pa            - ParserArguments
     */
    public GeneParser(ConceptWriter conceptWriter, ONDEXPluginArguments pa) throws InvalidPluginArgumentException {
        super(conceptWriter, pa);
    }

    public
    @Override
    void start() throws IOException, InvalidPluginArgumentException {

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
        GeneObject g = null;
        Publication pub = null;
        while (br.ready()) {

            // get next line
            String line = br.readLine();

            if (line.startsWith("AC")) {
                pub = null;
                String acc = parseLine(line);
                if (acc.length() < 1) {
                    Parser.propagateEventOccurred(
                            new GeneralOutputEvent("Invalid accession skiping entry: " + line, "start()"));
                    g = null;
                    continue;
                } else {
                    g = new GeneObject(acc);
                }
            } else if (line.startsWith("AS")) {
                g.addAccessionAlternative(parseLine(line));
            } else if (line.startsWith("DE")) {
                g.setDescription(parseLine(line));
            } else if (line.startsWith("SD")) {
                g.setName(parseLine(line));
            } else if (line.startsWith("SY")) {
                Iterator<String> it = parseSplit(parseLine(line), ";").iterator();
                while (it.hasNext()) {
                    g.addSynonym(it.next());
                }
            } else if (line.startsWith("OS")) {
                g.setSpecies(parseSpecies(parseLine(line)));
            }

            // description parsing
            else if (line.startsWith("CH")) {
                g.setChromosomalLocation(parseLine(line));
            } else if (line.startsWith("RG")) {
                g.setRegulation(parseLine(line));
            } else if (line.startsWith("DR")) {
                DBlink link = parseDatabaseLink(parseLine(line));
                if (link != null) {
                    g.addDatabaseLink(link);
                }
            } else if (line.startsWith("BS")) {
                String line_content = parseLine(line);
                String bindingSiteAcc = parseBindingSiteAcc(line_content);
                if (bindingSiteAcc != null) {
                    g.addBindingSite(bindingSiteAcc.toUpperCase());
                }
            } else if (line.startsWith("RX")) {
                String type = line.substring(2, line.indexOf(':')).toLowerCase();
                if (type.contains("ubmed")) {
                    pub = new Publication(line.replaceAll("[^0-9]", ""));
                    g.addPublication(pub);
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
            }

            // end of entry (!!)
            else if (line.startsWith("//")) {
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
