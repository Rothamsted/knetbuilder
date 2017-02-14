package net.sourceforge.ondex.parser.transpath;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.config.ValidatorRegistry;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * Superclass for GeneParser, MoleculeParser and ReactionParser.
 *
 * @author taubertj
 */
abstract class AbstractTPParser {

    // TP accession prepender
    protected final static String TPACC = "TP_";

    // given ParserArguments
    protected ONDEXPluginArguments pa;

    // current concept writer to ONDEX
    protected ConceptWriter conceptWriter;

    // list of failed Taxonomy lookups
    protected HashSet<String> clashedTaxa = new HashSet<String>();

    // used for mapping of species exceptions
    private static HashMap<String, String> exceptions = null;

    /**
     * Constructor, which sets all internal variables.
     *
     * @param conceptWriter - ConceptWriter
     * @param pa            - ParserArguments
     */
    protected AbstractTPParser(ConceptWriter conceptWriter, ONDEXPluginArguments pa) throws InvalidPluginArgumentException {
        this.conceptWriter = conceptWriter;
        this.pa = pa;

        // get content of taxonomy exception file
        if (exceptions == null) {
            exceptions = new HashMap<String, String>();

            File dir = new File((String) pa.getUniqueValue(FileArgumentDefinition.INPUT_DIR));


            File file = new File(dir.getAbsolutePath()
                    + File.separator + "taxonomy_exceptions.txt");

            try {
                BufferedReader reader = new BufferedReader(
                        new FileReader(file));
                while (reader.ready()) {
                    String line = reader.readLine();
                    if (!line.startsWith("*")) {
                        String[] split = line.split("\t");
                        exceptions.put(split[0], split[1]);
                    }
                }
                reader.close();
                reader = null;
            } catch (FileNotFoundException fnfe) {
                Parser.propagateEventOccurred(
                        new DataFileMissingEvent(fnfe.getMessage(), "AbstractTPParser(ConceptWriter conceptWriter, ParserArguments pa)"));
            } catch (IOException ioe) {
                Parser.propagateEventOccurred(
                        new DataFileErrorEvent(ioe.getMessage(), "AbstractTPParser(ConceptWriter conceptWriter, ParserArguments pa)"));
            }
        }
    }

    /**
     * Performs parsing of associated file.
     */
    protected abstract void start() throws IOException, InvalidPluginArgumentException;

    /**
     * Parse a DBlink from a given line.
     *
     * @param line - String
     * @return DBlink
     */
    protected DBlink parseDatabaseLink(String line) {

        String cv = null;

        // check for DataSource and map to ONDEX CVs

        if (line.startsWith("<SWISSPROT")
                || line.startsWith("<UPROT")
                || line.startsWith("<TREMBL")
                || line.startsWith("<UNIPROT")
                || line.startsWith("<UNIPROTKB"))
            cv = MetaData.UPROTKB;
        else if (line.startsWith("<InterPro"))
            cv = MetaData.IPRO;
        else if (line.startsWith("<TRANSFAC"))
            cv = "TF";
        else if (line.startsWith("<PIR"))
            cv = "PIR";
        else if (line.startsWith("{GENOMIC}<EMBL/GenBank/DDBJ"))
            cv = "PROID";
        else if (line.startsWith("{GENOMIC}<UNIGENE"))
            cv = "NC_GI";
        else if (line.startsWith("{GENOMIC}<REFSEQ"))
            cv = "NC_NM";
        else return null; // unknown line skip

        // get accession
        String acc = parseBeginEnd(line, ":", ">");
        if (acc != null && acc.length() > 0) {
            // nb duplicates are defined by entry string not object comparisons
            return new DBlink(acc, cv);
        }

        return null;
    }

    /**
     * Normalizes a line by removing first line identifier
     * and then trimming it.
     *
     * @param line - String
     * @return String
     */
    protected String parseLine(String line) {
        String newLine = line.substring(2, line.length()).trim();
        if (newLine.endsWith("."))
            newLine = newLine.substring(0, newLine.length() - 1);
        return newLine;
    }

    /**
     * Parses a possible species name out of a line.
     *
     * @param line - String
     * @return String
     */
    protected String parseSpecies(String line) {

        String specie = "";

        try {
            specie = parseBeginEnd(line, ", ", ".");
        } catch (StringIndexOutOfBoundsException e) {
            specie = line.substring(1, line.length() - 1);
        }

        String org = specie.trim();

        // look for possible mapping
        if (exceptions.containsKey(org))
            org = exceptions.get(org);
        specie = org.trim().toLowerCase();

        String taxId = (String) ValidatorRegistry.validators.get("taxonomy").validate(specie);

        if (taxId == null && specie.indexOf('(') > -1) {
            specie = specie.substring(0, specie.indexOf('('));
            taxId = (String) ValidatorRegistry.validators.get("taxonomy").validate(specie);
        }

        if (taxId == null) {
            taxId = "";
            clashedTaxa.add(org);
        }

        return taxId;
    }

    /**
     * Splits a line at semicolons.
     *
     * @param line - String
     * @return HashSet<String>
     */
    protected HashSet<String> parseSplit(String line) {

        HashSet<String> list = new HashSet<String>();

        StringTokenizer st = new StringTokenizer(line, ";");
        while (st.hasMoreElements()) {
            String element = st.nextToken().trim();
            if (element.endsWith("."))
                element = element.substring(0, element.length() - 1).trim();

            if (element.length() > 0) list.add(element);
        }
        st = null;

        return list;
    }

    /**
     * Parses a TP value out of a line.
     *
     * @param line - String
     * @return String
     */
    protected String parseValue(String line) {
        return parseBeginEnd(line, "<", ">");
    }

    /**
     * Parses content out of a line between given boundaries.
     *
     * @param line  - String
     * @param begin - String
     * @param end   - String
     * @return String
     * @throws StringIndexOutOfBoundsException
     *
     */
    private String parseBeginEnd(String line, String begin, String end)
            throws StringIndexOutOfBoundsException {
        int b = line.indexOf(begin);
        int e = line.indexOf(end);
		String begEnd = line.substring(b + 1, e).trim();
		if (begEnd.length() == 0) begEnd = null;
		return begEnd;
	}
}
