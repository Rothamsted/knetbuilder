package net.sourceforge.ondex.parser.transfac.factor;

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
 * Parser for parsing the TF factor.dat
 *
 * @author taubertj
 */
public class FactorParser extends AbstractTFParser {

    /**
     * Constructor inits superclass.
     *
     * @param conceptWriter - ConceptWriter
     * @param pa            - ParserArguments
     */
    public FactorParser(ConceptWriter conceptWriter, ONDEXPluginArguments pa) throws InvalidPluginArgumentException {
        super(conceptWriter, pa);
    }

    public
    @Override
    void start() throws IOException, InvalidPluginArgumentException {

        Parser.propagateEventOccurred(new GeneralOutputEvent("FactorParser started...", ""));

        File dir = new File((String) pa.getUniqueValue(FileArgumentDefinition.INPUT_DIR));

        BufferedReader br = new BufferedReader(
                new FileReader(dir.getAbsolutePath() +
                        File.separator + "factor.dat"));

        // kick the first 3 lines of the file (just copyright etc.
        br.readLine();
        br.readLine();
        br.readLine();

        // iterator over all lines of file
        FactorObject f = null;
        Publication pub = null;
        StringBuilder sequence = null;
        StringBuilder structural_features = null;
        StringBuilder cell_specificity_p = null;
        StringBuilder cell_specificity_n = null;
        StringBuilder expression_pattern = null;
        StringBuilder functional_properties = null;
        while (br.ready()) {

            // get next line
            String line = br.readLine();

            if (line.startsWith("AC")) {
                pub = null;
                String acc = parseLine(line).trim().toUpperCase();
                if (acc.length() < 1) {
                    Parser.propagateEventOccurred(
                            new DataFileErrorEvent("Invalid accession skiping entry: " + line, ""));
                    f = null;
                    continue;
                } else {
                    f = new FactorObject(acc);
                }
            } else if (line.startsWith("AS")) {
                Iterator<String> it = parseSplit(parseLine(line), ",").iterator();
                while (it.hasNext()) {
                    f.addAccessionAlternative(it.next());
                }
            } else if (line.startsWith("FA")) {
                f.setName(parseLine(line));
            } else if (line.startsWith("SY")) {
                Iterator<String> it = parseSplit(parseLine(line), ";").iterator();
                while (it.hasNext()) {
                    f.addSynonym(it.next());
                }
            } else if (line.startsWith("OS")) {
                f.setSpecies(parseSpecies(parseLine(line)));
            } else if (line.startsWith("GE")) {
                f.setEncodingGene(parseEncodedFactor(parseLine(line)));
            } else if (line.startsWith("SQ")) {
                if (sequence == null) {
                    sequence = new StringBuilder(parseLine(line).replaceAll(" ", ""));
                } else {
                    sequence.append(parseLine(line).replaceAll(" ", ""));
                }
            }

            // description parsing
            else if (line.startsWith("SF")) {
                if (structural_features == null) {
                    structural_features = new StringBuilder(
                            "Structural features: " + parseLine(line));
                } else {
                    structural_features.append(" " + parseLine(line));
                }
            } else if (line.startsWith("CP")) {
                if (cell_specificity_p == null) {
                    cell_specificity_p = new StringBuilder(
                            "Cell specificity (psoitive): " + parseLine(line));
                } else {
                    cell_specificity_p.append(" " + parseLine(line));
                }
            } else if (line.startsWith("CN")) {
                if (cell_specificity_n == null) {
                    cell_specificity_n = new StringBuilder(
                            "Cell specificity (negative): " + parseLine(line));
                } else {
                    cell_specificity_n.append(" " + parseLine(line));
                }
            } else if (line.startsWith("EX")) {
                if (expression_pattern == null) {
                    expression_pattern = new StringBuilder(
                            "Expression pattern: " + parseLine(line));
                } else {
                    expression_pattern.append(" " + parseLine(line));
                }
            } else if (line.startsWith("FF")) {
                if (functional_properties == null) {
                    functional_properties = new StringBuilder(
                            "Functional properties: " + parseLine(line));
                } else {
                    functional_properties.append(" " + parseLine(line));
                }
            } else if (line.startsWith("IN")) {
                // -- interacting factors --
                line = parseLine(line);
                if (line.indexOf(";") > -1) {
                    line = line.substring(0, line.indexOf(";"));
                    f.addInteractingFactor(line.trim());
                } else {
                    Parser.propagateEventOccurred(new DataFileErrorEvent(
                            "IN line inconsistent: " + line, ""));
                }
            } else if (line.startsWith("MX")) {
                // -- matrices --
                line = parseLine(line);
                if (line.indexOf(";") > -1) {
                    line = line.substring(0, line.indexOf(";"));
                    f.addMatrix(line.trim());
                } else {
                    Parser.propagateEventOccurred(new DataFileErrorEvent(
                            "MX line inconsistent: " + line, ""));
                }
            } else if (line.startsWith("BS")) {
                String line_content = parseLine(line);

                // -- binding site --
                String bindingSiteAcc = parseBindingSiteAcc(line_content);
                if (bindingSiteAcc != null) {
                    f.addBindingSite(bindingSiteAcc.toUpperCase());
                }

                // -- regulated gene --
                String regulatedGenAcc = parseGeneAcc(line_content);
                if (regulatedGenAcc != null) {
                    f.addRegulatedGene(regulatedGenAcc);
                }
            } else if (line.startsWith("DR")) {
                DBlink link = parseDatabaseLink(parseLine(line));
                if (link != null) {
                    f.addDatabaseLink(link);
                    if (link.getCv().equalsIgnoreCase("DATF")) {
                        f.addDatabaseLink(new DBlink(link.getAcc(), "TAIR")); //a exception that shares its accessions with TAIR
                    }
                }
            } else if (line.startsWith("RX")) {
                String type = line.substring(2, line.indexOf(':')).toLowerCase();
                if (type.contains("ubmed")) {
                    pub = new Publication(line.replaceAll("[^0-9]", ""));
                    f.addPublication(pub);
                    // ubmed (not pubmed) for one exception
                } else {
                    Parser.propagateEventOccurred(new DataFileErrorEvent(
                            "Unknown publication id :" + type, ""));
                }
            } else if (pub != null && line.startsWith("RA")) {
                pub.setAuthors(parseLine(line));
            } else if (pub != null && line.startsWith("RT")) {
                pub.setTitle(parseLine(line));
            } else if (pub != null && line.startsWith("RL")) {
                pub.setSource(parseLine(line));
            }

            // end of entry
            else if (line.startsWith("//")) {

                // write remaining information in factor object
                if (sequence != null)
                    f.setSequence(sequence.toString());

                // -- description --
                StringBuilder description = new StringBuilder();
                if (structural_features != null
                        || cell_specificity_p != null
                        || cell_specificity_n != null
                        || expression_pattern != null
                        || functional_properties != null) {
                    if (structural_features != null) {
                        description.append(" ");
                        description.append(structural_features);
                    }
                    if (cell_specificity_p != null) {
                        description.append(" ");
                        description.append(cell_specificity_p);
                    }
                    if (cell_specificity_n != null) {
                        description.append(" ");
                        description.append(cell_specificity_n);
                    }
                    if (expression_pattern != null) {
                        description.append(" ");
                        description.append(expression_pattern);
                    }
                    if (functional_properties != null) {
                        description.append(" ");
                        description.append(functional_properties);
                    }
                }
                f.setDescription(description.toString().trim());
                description = null;

                // add factor object to list
                conceptWriter.createFactor(f);

                // reset variables;
                f = null;
                sequence = null;
                functional_properties = null;
                expression_pattern = null;
                cell_specificity_p = null;
                cell_specificity_n = null;
                structural_features = null;
            }
        }

        br.close();
        br = null;

        // print all clashed taxa
        Iterator<String> it = clashedTaxa.iterator();
        while (it.hasNext()) {
            Parser.propagateEventOccurred(
                    new DataFileErrorEvent("No taxid found for " + it.next(), ""));
        }
        conceptWriter.validateSiteToTF("Factor"); //If Sites have already been written will validate
        Parser.propagateEventOccurred(new GeneralOutputEvent("FactorParser finished...", ""));
    }
}
