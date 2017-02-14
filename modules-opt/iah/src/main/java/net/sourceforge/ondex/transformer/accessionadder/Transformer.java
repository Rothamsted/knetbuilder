package net.sourceforge.ondex.transformer.accessionadder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.config.ValidatorRegistry;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.type.InconsistencyEvent;
import net.sourceforge.ondex.transformer.ONDEXTransformer;
import net.sourceforge.ondex.validator.AbstractONDEXValidator;

import org.apache.log4j.Level;

/**
 * Created by IntelliJ IDEA.
 * User: nmrp3
 * Date: 18-Nov-2009
 * Time: 16:10:44
 * To change this template use File | Settings | File Templates.
 */
public class Transformer extends ONDEXTransformer
{
    public static final String FILE_ARG = "File";
    public static final String FILE_ARG_DESC = "Input file";
    public static final String CC_ARG = "CC";
    public static final String CC_ARG_DESC = "Concept class to modify";
    public static final String APPLY_TO_SUB_TYPES_ARG = "ApplyToSubTypes";
    public static final String APPLY_TO_SUB_TYPES_ARG_DESC = "Apply this mapping to all sub-types of CC";

    @Override
    public String[] requiresValidators() {
        return new String[]{"cvregex"};
    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new StringArgumentDefinition(FILE_ARG, FILE_ARG_DESC, true, null, false),
                new StringArgumentDefinition(CC_ARG, CC_ARG_DESC, true, null, false),
                new BooleanArgumentDefinition(APPLY_TO_SUB_TYPES_ARG, APPLY_TO_SUB_TYPES_ARG_DESC, false, true),
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_DIR,"directory for IAH files", true, true, true, false)
        };
    }

    @Override
    public String getVersion() {
        return "16/11/09";
    }

    @Override
    public String getName() {
        return "Accession Adder";
    }

    @Override
    public String getId() {
        return "accessionadder";
    }

    @Override
    public void start() throws Exception {
        File inputDir = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_DIR));
        File inputFile = new File(inputDir, (String) getArguments().getUniqueValue(FILE_ARG));
        ConceptClass targetClass = graph.getMetaData().getConceptClass((String) getArguments().getUniqueValue(CC_ARG));
        boolean andSubTypes = (Boolean) getArguments().getUniqueValue(APPLY_TO_SUB_TYPES_ARG);
        AbstractONDEXValidator validator = ValidatorRegistry.validators.get("cvregex");

        Map<String, Set<AccCV>> accs = loadFile(inputFile, validator);
        Set<ONDEXConcept> concepts = fetchConcepts(targetClass, andSubTypes);
        addAccessions(concepts, accs);
    }

    private Map<String, Set<AccCV>> loadFile(File inputFile, AbstractONDEXValidator validator)
            throws FileNotFoundException {
        Map<String, Set<AccCV>> allAccs = new HashMap<String, Set<AccCV>>();

        Scanner scanner = new Scanner(new FileReader(inputFile)); // todo: handle exception

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] items = line.split("\t");

            Set<AccCV> accs = new HashSet<AccCV>();
            for (String item : items) {
                if (item.length() == 0) { // skip empty columns
                    continue;
                }

                String cvName = (String) validator.validate(item);
                DataSource dataSource;
                if (cvName == null) {
                    dataSource = graph.getMetaData().getDataSource("unknown");
                } else {
                    dataSource = graph.getMetaData().getDataSource(cvName); // todo: deal with nulls
                }

                accs.add(new AccCV(item, dataSource)); // todo: deal with nulls
            }
            for (AccCV acc : accs) {
                if (allAccs.containsKey(acc.getAcc())) {
                    logInconsistency("Accession duplicated: " + acc);
                }
                allAccs.put(acc.getAcc(), accs);
            }
        }

        return allAccs;
    }

    private Set<ONDEXConcept> fetchConcepts(ConceptClass cc, boolean andSubTypes) {
        if (andSubTypes) {
            Set<Integer> toKeep = new HashSet<Integer>();
            for (ONDEXConcept c : graph.getConcepts()) {
                if (c.inheritedFrom(cc)) {
                    toKeep.add(c.getId());
                }
            }
            return BitSetFunctions.create(graph, ONDEXConcept.class, toKeep); // todo: eugh!
        } else {
            return graph.getConceptsOfConceptClass(cc);
        }
    }

    private void addAccessions(Set<ONDEXConcept> concepts, Map<String, Set<AccCV>> accs) {
        for (ONDEXConcept c : concepts) {
            Set<AccCV> matches = null;
            for (ConceptAccession acc : c.getConceptAccessions()) {
                Set<AccCV> ms = accs.get(acc.getAccession());
                if (ms != null && !ms.isEmpty()) {
                    matches = ms;
                    break;  // todo: just takes the first one - should do better?
                }
            }
            if (matches != null) {
                for (AccCV m : matches) {
                    c.createConceptAccession(m.getAcc(), m.getDataSource(), false);
                }
            }
        }
    }

    /**
     * Logs an inconsistency event with message <code>s</code>.
     *
     * @param s the message to log as an inconsistency.
     */
    private void logInconsistency(String s) {
        InconsistencyEvent e = new InconsistencyEvent("\n" + s, "");
        e.setLog4jLevel(Level.DEBUG);
        fireEventOccurred(e);
    }

    private static final class AccCV {
        private final String acc;
        private final DataSource dataSource;

        private AccCV(String acc, DataSource dataSource) {
            this.acc = acc;
            this.dataSource = dataSource;
        }

        public String getAcc() {
            return acc;
        }

        public DataSource getDataSource() {
            return dataSource;
        }
    }
}
