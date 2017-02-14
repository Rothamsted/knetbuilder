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
 * Parser for parsing the TP molecule.dat
 *
 * @author taubertj
 */
class MoleculeParser extends AbstractTPParser {

    /**
     * Constructor inits superclass.
     *
     * @param conceptWriter - ConceptWriter
     * @param pa            - ParserArguments
     */
    protected MoleculeParser(ConceptWriter conceptWriter, ONDEXPluginArguments pa) throws InvalidPluginArgumentException {
        super(conceptWriter, pa);
    }

    @Override
    protected void start() throws IOException, InvalidPluginArgumentException {

        Parser.propagateEventOccurred(new GeneralOutputEvent("MoleculeParser started...", "start()"));

        File dir = new File((String) pa.getUniqueValue(FileArgumentDefinition.INPUT_DIR));

        BufferedReader br = new BufferedReader(
                new FileReader(dir.getAbsolutePath() +
                        File.separator + "molecule.dat"));

        // kick the first 3 lines of the file (just copyright etc.)
        br.readLine();
        br.readLine();
        br.readLine();

        // iterator over all lines of file
        Molecule m = null;
        Publication pub = null;
        StringBuilder description = null;
        StringBuilder sequence = null;
        while (br.ready()) {

            // get next line
            String line = br.readLine();

            if (line.startsWith("AC")) {
                String acc = parseLine(line);
                if (acc.length() < 1) {
                    Parser.propagateEventOccurred(
                            new DataFileErrorEvent("Invalid accession skiping entry: " + line, "start()"));
                    m = null;
                    description = null;
                    sequence = null;
                    continue;
                }
                m = new Molecule(acc);
            } else if (line.startsWith("NA")) {
                m.setName(parseLine(line));
            } else if (line.startsWith("SY")) {
                m.addSynonym(parseLine(line));
            } else if (line.startsWith("GE")) {
                m.setEncodingGene(parseValue(parseLine(line)));
            } else if (line.startsWith("OS")) {
                m.setSpecies(parseSpecies(parseLine(line)));
            } else if (line.startsWith("TY")) {
                m.setType(parseType(parseLine(line)));
            } else if (line.startsWith("SQ")) {
                if (sequence == null) {
                    sequence = new StringBuilder(parseLine(line).replaceAll(" ", ""));
                } else {
                    sequence.append(parseLine(line).replaceAll(" ", ""));
                }
            } else if (line.startsWith("DR")) {
                DBlink link = parseDatabaseLink(parseLine(line));
                if (link != null) {
                    m.addDatabaseLink(link);
                }
            } else if (line.startsWith("CC")) {
                if (description == null) {
                    description = new StringBuilder(parseLine(line));
                } else {
                    description.append(" " + parseLine(line));
                }
            } else if (line.startsWith("CP")) {
                String desc = "LocatedIn: " + parseLine(line);
                if (description == null) {
                    description = new StringBuilder(desc);
                } else {
                    description.append(" " + desc);
                }
            } else if (line.startsWith("CN")) {
                String desc = "NotLocatedIn: " + parseLine(line);
                if (description == null) {
                    description = new StringBuilder(desc);
                } else {
                    description.append(" " + desc);
                }
            } else if (line.startsWith("ST")) {
                m.addSubunit(parseValue(parseLine(line)));
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
                m.addPublication(pub);
                pub = null;
            }

            // end of entry (!!)
            if (line.startsWith("//")) {
                if (sequence != null)
                    m.setSequence(sequence.toString());
                if (description != null)
                    m.setDescription(description.toString());
                description = null;
                sequence = null;
                conceptWriter.createMolecule(m);
                m = null;
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

        Parser.propagateEventOccurred(new GeneralOutputEvent("MoleculeParser finished...", "start()"));
    }

    /**
     * Parses the type of entry from a line.
     *
     * @param line - String
     * @return String
     */
    private String parseType(String line) {

        //TODO: Fix this function
        //To get the types from the molecule file: cat molecule.dat | grep ^TY | sort -u > types.txt
        //But it is systematicaly incorrect:

        String type = "Protein";

        if (line.startsWith("family"))
            type = "ProtFam";

        if (line.startsWith("complex"))
            type = "Protcmplx";

        if (line.startsWith("other"))
            type = "Comp";

        return type;
    }
}






























