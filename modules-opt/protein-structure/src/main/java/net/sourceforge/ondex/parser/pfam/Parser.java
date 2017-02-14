package net.sourceforge.ondex.parser.pfam;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.pfam.sink.DbLink;
import net.sourceforge.ondex.parser.pfam.sink.Family;
import net.sourceforge.ondex.parser.pfam.sink.Publication;
import net.sourceforge.ondex.parser.pfam.transformer.FamilyTransformer;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Parser for the http://pfam.sanger.ac.uk/ pfam database
 *
 * @author peschr
 */
public class Parser extends ONDEXParser
{
    private int pfamCounter;

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    /**
     * @return always null
     */
    public ArgumentDefinition<?>[] getArgumentDefinitions() {

        FileArgumentDefinition inputFile = new FileArgumentDefinition(
                FileArgumentDefinition.INPUT_FILE, FileArgumentDefinition.INPUT_FILE_DESC, true,
                true, false, false);
        BooleanArgumentDefinition searchForReferences = new BooleanArgumentDefinition(
                ArgumentNames.SEARCH_FOR_PFAM_ACCESSION_ARG,
                ArgumentNames.SEARCH_FOR_PFAM_ACCESSION_ARG_DESC, false, true);
        return new ArgumentDefinition<?>[]{searchForReferences, inputFile};
    }

    public String getName() {
        return "Pfam";
    }

    public String getVersion() {
        return "23.01.07";
    }

    @Override
    public String getId() {
        return "pfam";
    }

    /**
     * definies the keywords
     *
     * @author peschr
     */
    interface KeyWords {
        public static String ACCESSION_KEY = "#=GF AC";

        public static String ID_KEY = "#=GF ID";

        public static String DE_KEY = "#=GF DE";

        public static String PUBLICATION_START_KEY = "#=GF RN";

        public static String PUBLICATION_ID_KEY = "#=GF RM";

        public static String PUBLICATION_TITLE_KEY = "#=GF RT";

        public static String PUBLICATION_AUTHOR_KEY = "#=GF RA";

        public static String PUBLICATION_JOURNAL_KEY = "#=GF RL";

        public static String DATABASE_REFERENCE_KEY = "#=GF DR";

        public static String SOURCE_REFERENCE_KEY = "#=GS";
    }

    private FamilyTransformer transformer;

    private Filter accessionFilter;

    /**
     * returns the part after the keyword
     *
     * @param line whole line
     * @param key  the keyword
     * @return String all after the keyword
     */
    private String getValue(String line, String key) {
        return line.substring(key.length()).trim();
    }

    /**
     * searchs for all concepts with given DataSource and adds them to the filter
     *
     * @param concepts concepts of perhaps a given cv type
     * @param targetDataSource which should be seached
     */
    private void setupFilters(Set<ONDEXConcept> concepts, DataSource targetDataSource)
            throws InvalidPluginArgumentException {
        accessionFilter = new Filter();
        if ((Boolean) super.args
                .getUniqueValue(ArgumentNames.SEARCH_FOR_PFAM_ACCESSION_ARG))
            return;
        for (ONDEXConcept concept : concepts) {
            for (ConceptAccession accession : concept.getConceptAccessions()) {
                // accession equals targetCV
                if (!accession.getElementOf().equals(targetDataSource))
                    continue;
                accessionFilter.addAccession(accession.getAccession());
            }
        }
        ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
                .fireEventOccurred(
                        new GeneralOutputEvent("found "
                                + accessionFilter.accessions.size()
                                + " pfam references in current graph",
                                Parser.class.toString()));

    }

    public void start() throws InvalidPluginArgumentException {
        // transformer for an entity
        transformer = new FamilyTransformer(graph);

        Pattern semicolon = Pattern.compile(";");
        Pattern dot = Pattern.compile("\\.");

        // initialize the filter
        setupFilters(graph.getConceptsOfConceptClass(graph.getMetaData()
                .getConceptClass(MetaData.CC_Protein)), graph.getMetaData()
                .getDataSource(MetaData.CV_Pfam));
        try {
            // open pfam database file
            InputStream fis = null;
            File file = new File((String) args
                    .getUniqueValue(FileArgumentDefinition.INPUT_FILE));
            if (file.getName().endsWith(".gz")) {
                fis = new GZIPInputStream(new FileInputStream(file));
            } else {
                fis = new FileInputStream(file);
            }
            BufferedReader input = new BufferedReader(
                    new InputStreamReader(fis));
            Family family = null;
            Publication pub = null;
            String line;
            while ((line = input.readLine()) != null) {
                if (line.startsWith(KeyWords.ID_KEY)) {
                    if (family != null
                            && accessionFilter.filter(family.getAccession())) {
                        pfamCounter++;
                        transformer.transform(family);
                        if (pfamCounter % 500 == 0) {
                            ONDEXEventHandler.getEventHandlerForSID(
                                    graph.getSID()).fireEventOccurred(
                                    new GeneralOutputEvent(
                                            "Parsed " + pfamCounter
                                                    + " protein families",
                                            Parser.class.toString()));
                        }
                    }
                    family = new Family();
                    family.setId(getValue(line, KeyWords.ID_KEY));
                } else if (line.startsWith(KeyWords.DE_KEY)) {
                    String tmp = getValue(line, KeyWords.DE_KEY);
                    family.setDescription(tmp);
                } else if (line.startsWith(KeyWords.ACCESSION_KEY)) {
                    String tmp = getValue(line, KeyWords.ACCESSION_KEY);
                    String[] accession = dot.split(tmp);
                    // accession[0] is the actually accession
                    family.setAccession(accession[0].trim());
                    // accession[1] is the accession version
                } else if (line.startsWith(KeyWords.PUBLICATION_START_KEY)) {
                    pub = new Publication();
                } else if (line.startsWith(KeyWords.PUBLICATION_ID_KEY)) {
                    pub.setId(getValue(line, KeyWords.PUBLICATION_ID_KEY));
                } else if (line.startsWith(KeyWords.PUBLICATION_TITLE_KEY)) {
                    pub
                            .setTitle(getValue(line,
                                    KeyWords.PUBLICATION_TITLE_KEY));
                } else if (line.startsWith(KeyWords.PUBLICATION_AUTHOR_KEY)) {
                    // skip
                } else if (line.startsWith(KeyWords.PUBLICATION_JOURNAL_KEY)) {
                    pub.setJournal(getValue(line,
                            KeyWords.PUBLICATION_JOURNAL_KEY));
                } else if (line.startsWith(KeyWords.DATABASE_REFERENCE_KEY)) {
                    String[] data = semicolon.split(getValue(line,
                            KeyWords.DATABASE_REFERENCE_KEY));
                    DbLink dbLink = new DbLink(data[0], data[1].trim());
                    family.addDbLink(dbLink);
                } else if (line.startsWith(KeyWords.SOURCE_REFERENCE_KEY)) {

                    int lastIndex = line.lastIndexOf("DR PDB");
                    if (lastIndex != -1) {
                        String acc = line.substring(lastIndex + 8);
                        acc = acc.substring(0, acc.indexOf(' ')).trim();
                        DbLink dbLink = new DbLink("PDB", acc);
                        family.addDbLink(dbLink);
                    }
                }

            }
            transformer.transform(family);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
                .fireEventOccurred(
                        new GeneralOutputEvent("parsed " + pfamCounter
                                + " pfam entries", Parser.class.toString()));
    }

    /**
     * Accession filter
     *
     * @author peschr
     */
    public class Filter {
        private HashSet<String> accessions = new HashSet<String>();

        /**
         * stores not to filter elements
         *
         * @param accession
         */
        public void addAccession(String accession) {
            accessions.add(accession);
        }

        /**
         * checks if a given accession should be filtered
         *
         * @param accession given accession
         * @return boolean, true if not
         */
        public boolean filter(String accession) {
			return accessions.size() == 0 ? true : accessions
					.contains(accession);
		}
	}
}
