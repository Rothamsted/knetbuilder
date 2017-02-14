package net.sourceforge.ondex.parser.transfac;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.config.ValidatorRegistry;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Superclass for GeneParser, FactorParser and MatrixParser.
 *
 * @author taubertj
 */
public abstract class AbstractTFParser {

    // given ParserArguments
    protected ONDEXPluginArguments pa;

    // current concept writer to ONDEX
    protected ConceptWriter conceptWriter;

    // pattern for Gene accession eg. G000001
    private Pattern patternGeneAcc = Pattern.compile("G[0-9]{6}");

    // pattern for Site accession eg. R04077
    private Pattern patternSiteAcc = Pattern.compile("R[0-9]{5}");

    // list of failed Taxonomy lookups
    protected HashSet<String> clashedTaxa = new HashSet<String>();

    // used for mapping of species exceptions
    private static HashMap<String, String> exceptions = null;

    /**
     * Constructor, which sets all internal variables.
     *
     * @param conceptWriter ConceptWriter
     * @param pa            ParserArguments
     */
    protected AbstractTFParser(ConceptWriter conceptWriter, ONDEXPluginArguments pa) throws InvalidPluginArgumentException {
        this.conceptWriter = conceptWriter;
        this.pa = pa;

        // get content of taxonomy exception file
        if (exceptions == null) {
            exceptions = new HashMap<String, String>();
            File dir = new File((String) pa.getUniqueValue(FileArgumentDefinition.INPUT_DIR));
            File file = new File(dir.getAbsolutePath() + File.separator
                    + "taxonomy_exceptions.txt");

            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
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
                Parser.propagateEventOccurred(new DataFileErrorEvent(fnfe
                        .getMessage(), ""));
            } catch (IOException ioe) {
                Parser.propagateEventOccurred(new DataFileErrorEvent(ioe
                        .getMessage(), ""));
            }
        }
    }

    /**
     * Performs parsing of associated file.
     */
    protected abstract void start() throws IOException, InvalidPluginArgumentException;

    /**
     * Normalizes a line by removing first line identifier and then trimming it.
     *
     * @param line String
     * @return String
     */
    protected String parseLine(String line) {
        String newLine = line.substring(2, line.length()).trim();
        if (newLine.endsWith("."))
            newLine = newLine.substring(0, newLine.length() - 1);
        return newLine;
    }

    /**
     * Splits synomys at ; and removes trailing . if present.
     *
     * @param line String
     * @return ArrayList<String>
     */
    protected ArrayList<String> parseSynonyms(String line) {

        StringTokenizer st = new StringTokenizer(line, ";");
        ArrayList<String> synonyms_list = new ArrayList<String>();

        while (st.hasMoreElements()) {
            String synonym = st.nextToken().trim();
            if (synonym.endsWith(".")) {
                synonym = synonym.substring(0, synonym.length() - 1);
            }
            synonyms_list.add(synonym);
        }

        return synonyms_list;
    }

    /**
     * Parses a gene id from a BS line.
     *
     * @param line String
     * @return String
     */
    protected String parseGeneAcc(String line) {
        Matcher match = patternGeneAcc.matcher(line);
        if (match.find()) {
            return match.group();
        }
        return null;
    }

    /**
     * Parses a Binding Site Accession from a BS line.
     *
     * @param line String
     * @return String
     */
    protected String parseBindingSiteAcc(String line) {
        Matcher match = patternSiteAcc.matcher(line);
        if (match.find()) {
            return match.group();
        }
        return null;
    }

    /**
     * Parses a possible species name out of a line.
     *
     * @param line String
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

        String taxId = (String) ValidatorRegistry.validators.get("taxonomy")
                .validate(specie);

        if (taxId == null && specie.endsWith("sp")) {
            specie = specie.substring(0, specie.length() - "sp".length())
                    .trim();
            taxId = (String) ValidatorRegistry.validators.get("taxonomy").validate(
                    specie);
        }

        if (taxId == null && specie.endsWith("spec")) {
            specie = specie.substring(0, specie.length() - "spec".length())
                    .trim();
            taxId = (String) ValidatorRegistry.validators.get("taxonomy").validate(
                    specie);
        }

        if (taxId == null && specie.indexOf('(') > -1) {
            specie = specie.substring(0, specie.indexOf('(')).trim();
            taxId = (String) ValidatorRegistry.validators.get("taxonomy").validate(
                    specie);
        }

        if (taxId == null && specie.endsWith("var")) {
            specie = specie.substring(0, specie.length() - "var".length())
                    .trim();
            taxId = (String) ValidatorRegistry.validators.get("taxonomy").validate(
                    specie);
        }

        if (taxId == null && specie.endsWith("subsp")) {
            specie = specie.substring(0, specie.length() - "subsp".length())
                    .trim();
            taxId = (String) ValidatorRegistry.validators.get("taxonomy").validate(
                    specie);
        }

        if (taxId == null && specie.endsWith("cv")) {
            specie = specie.substring(0, specie.length() - "cv".length())
                    .trim();
            taxId = (String) ValidatorRegistry.validators.get("taxonomy").validate(
                    specie);
        }

        if (taxId == null) {
            taxId = "";
            clashedTaxa.add(org);
        }

        return taxId;
    }

    /**
     * Parse a TF from a "FA" line.
     *
     * @param line String
     * @return String
     */
    protected String parseEncodedFactor(String line) {
        StringTokenizer st = new StringTokenizer(line, ";");
        return st.nextToken().trim();
    }

    /**
     * Splits a line at semicolons.
     *
     * @param line String
     * @return HashSet<String>
     */
    protected HashSet<String> parseSplit(String line, String delim) {

        HashSet<String> list = new HashSet<String>();

        StringTokenizer st = new StringTokenizer(line, delim);
        while (st.hasMoreElements()) {
            String element = st.nextToken().trim();
            if (element.endsWith("."))
                element = element.substring(0, element.length() - 1).trim();

            if (element.length() > 0)
                list.add(element);
        }
        st = null;

        return list;
    }

    /**
     * Parse a DBlink from a given line.
     *
     * @param line String
     * @return DBlink
     */
    protected DBlink parseDatabaseLink(String line) {

        String cv = "";

        // get accession
        String acc = parseBeginEnd(line, ":", ";");
        if (acc == null) {
            return null;
        } else if (acc.length() == 0) {
            return null;
        }

        // check for DataSource and map to ONDEX CVs
        if (line.startsWith("SWISSPROT") || line.startsWith("UPROTKB")
                || line.startsWith("UPROT") || line.startsWith("TREMBL")
                || line.startsWith("UNIPROT") || line.startsWith("UNIPROTKB"))
            cv = "UNIPROTKB";
        else if (line.startsWith("TRANSPATH"))
            cv = "TP";
        else if (line.startsWith("PIR"))
            cv = "PIR";
        else if (line.startsWith("EMBL"))
            cv = "EMBL";
        else if (line.startsWith("BRENDA"))
            cv = "BRENDA";
        else if (line.startsWith("REFSEQ")) {
            // Get from Validator the type
            cv = (String) ValidatorRegistry.validators.get("cvregex").validate(acc);
        } else if (line.startsWith("UNIGENE"))
            cv = "UNIGENE";
        else if (line.startsWith("MGI"))
            cv = "MGI";
        else if (line.startsWith("AFFYMETRIX"))
            cv = "AFFYMETRIX";
        else if (line.startsWith("ENSEMBL"))
            cv = "ENSEMBL";
        else if (line.startsWith("BKL"))
            cv = "BKL";
        else if (line.startsWith("TRANSPRO"))
            cv = "TRANSPRO";
        else if (line.startsWith("TRRD"))
            cv = "TRRD";
        else if (line.startsWith("ENSEMBL"))
            cv = "ENSEMBL";
        else if (line.startsWith("OMIM"))
            cv = "OMIM";
        else if (line.startsWith("HGNC"))
            cv = "HGNC";
        else if (line.startsWith("SMARTDB"))
            cv = "SMARTDB";
        else if (line.startsWith("ENTREZGENE")) // Are NC_GE identifiers.
            cv = "NC_GE";
        else if (line.startsWith("PATHODB"))
            cv = "PATHODB";
        else if (line.startsWith("RGD"))
            cv = "RGD";
        else if (line.startsWith("Flybase") || line.startsWith("FLYBASE"))
            cv = "FLYBASE";
        else if (line.startsWith("EPD"))
            cv = "EPD";
        else if (line.startsWith("TRANSCOMPEL"))
            cv = "TRANSCOMPEL";
        else if (line.startsWith("DATF"))
            cv = "DATF";
        else if (line.startsWith("RSNP"))
            cv = "RSNP";
        else if (line.startsWith("PDB"))
            cv = "PDB";
        else if (line.startsWith("MIRBASE"))
            cv = "MIRBASE";
        else {
            System.err.println(line + " contains unknown db accession");
            return null; // unknown line skip
        }

        if (cv != null && acc != null && acc.length() > 0) {
            // nb duplicates are defined by entry string not object comparisons
            return new DBlink(acc, cv);
        }

        return null;
    }

    /**
     * Parses content out of a line between given boundaries.
     *
     * @param line  String
     * @param begin String
     * @param end   String
     * @return String
     * @throws StringIndexOutOfBoundsException
     *
     */
    private String parseBeginEnd(String line, String begin, String end)
            throws StringIndexOutOfBoundsException {
        int b = line.indexOf(begin);
        int e = line.indexOf(end);

        // check if both boundaries are in the right order
        if (e == -1)
            e = line.length();

        // get substring
        String begEnd = line;
        try {
            begEnd = line.substring(b + 1, e).trim();
            if (begEnd.endsWith("."))
                begEnd = begEnd.substring(0, begEnd.length() - 1);
            if (begEnd.length() == 0)
                begEnd = null;
        } catch (StringIndexOutOfBoundsException aio) {
			System.err.println("Line: " + line + " Begin: " + begin + "(" + b
					+ ")" + " End: " + end + "(" + e + ")");
		}

		return begEnd;
	}
}
