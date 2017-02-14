package net.sourceforge.ondex.validator.taxonomy;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.DatabaseErrorEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.validator.AbstractONDEXValidator;
import org.apache.log4j.Level;

import java.io.*;

/**
 * Implements a validation for the NCBI taxonomy.
 *
 * @author taubertj
 */
@Custodians(custodians = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
public class Validator extends AbstractONDEXValidator {

    // arguments for input/output dir
    private ONDEXPluginArguments va = new ONDEXPluginArguments(new ArgumentDefinition<?>[0]);

    // berkeley env
    private Environment myEnv = null;

    // berkeley store
    private EntityStore store = null;

    // berkeley index structure
    private PrimaryIndex<String, Entry> index = null;

    /**
     * Returns name of this validator.
     *
     * @return String
     */
    public String getName() {
        return "Taxonomy";
    }

    /**
     * Returns version of this validator.
     *
     * @return String
     */
    public String getVersion() {
        return new String("08.01.2007");
    }

    @Override
    public String getId() {
        return "taxonomy";
    }

    @Override
    public void setArguments(ONDEXPluginArguments va) throws InvalidPluginArgumentException {
        this.va = va;

        File inputDir = new File((String) va
                .getUniqueValue(FileArgumentDefinition.INPUT_DIR));


        //optional species file found ftp://ftp.ebi.ac.uk/pub/databases/uniprot/current_release/knowledgebase/complete/docs
        File uprotCodes = new File(inputDir.getAbsolutePath() + File.separator
                + "speclist.txt");

        try {
            EnvironmentConfig myEnvConfig = new EnvironmentConfig();
            StoreConfig storeConfig = new StoreConfig();

            myEnvConfig.setAllowCreate(true);
            storeConfig.setAllowCreate(true);

            // make sure directory exists
            File dir = new File((String) va
                    .getUniqueValue(FileArgumentDefinition.EXPORT_DIR));

            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Open the environment and entity store
            myEnv = new Environment(dir, myEnvConfig);
            store = new EntityStore(myEnv, "EntityStore", storeConfig);

            // get index contained in store
            index = store.getPrimaryIndex(String.class, Entry.class);

            // FIXME: This is a hack because index.count() seems to be broken.
            if (index.map().keySet().size() == 0) {

                // construct filename
                String filename = inputDir.getAbsolutePath() + File.separator
                        + "names.dmp";

                GeneralOutputEvent goe = new GeneralOutputEvent(
                        "Using taxonomy flatfile " + filename,
                        "[Validator - validate]");
                goe.setLog4jLevel(Level.INFO);
                fireEventOccurred(goe);

                // reader for file
                BufferedReader reader = new BufferedReader(
                        new FileReader(filename));

                // parser taxonomy names.dmp
                while (reader.ready()) {
                    String line = reader.readLine();
                    String[] split = line.split("\t\\|\t");
                    String name = split[1].toLowerCase().trim();
                    index.put(new Entry(name, split[0]));
                    // here handle some stemming exceptions
                    if (name.contains("/")) {
                        index.put(new Entry(name.replaceAll("/", "_"), split[0]));
                    }
                    if (name.contains(".")) {
                        index.put(new Entry(name.replaceAll("\\.", ""), split[0]));
                    }
                }

                // close reader
                reader.close();
            }

        } catch (DatabaseException dbe) {
            fireEventOccurred(new DatabaseErrorEvent(dbe.getMessage(),
                    "[Validator - validate]"));
        } catch (FileNotFoundException fnfe) {
            fireEventOccurred(new DataFileMissingEvent(fnfe
                    .getMessage(), "[Validator - validate]"));
        } catch (IOException ioe) {
            fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
                    "[Validator - validate]"));
        }

        if (uprotCodes.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(uprotCodes));

                boolean start = false;

                String taxaid = null;

                // read file line by line
                while (reader.ready()) {
                    String line = reader.readLine();
                    if (start) {
                        if (line.toUpperCase().matches("^[A-Z].+")) {
                            taxaid = null;
                            String[] componants = line.split(" ");
                            String uprotId = componants[0].trim();
                            if (uprotId.length() == 0) {
                                continue;
                            }

                            for (String element : componants) {
                                int colon = element.indexOf(":");
                                if (colon > 0) {
                                    taxaid = element.substring(0, colon);
                                    index.put(new Entry(uprotId.toLowerCase(), taxaid));
                                }
                            }
                        } else if (taxaid != null && line.indexOf("=") > 0) {
                            int equ = line.indexOf("=");
                            line = line.substring(equ + 1, line.length()).trim();
                            if (line.length() > 0) {
                                index.put(new Entry(line.toLowerCase(), taxaid));
                            }
                        }

                    } else if (line.startsWith("_____")) {
                        start = true;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DatabaseException e) {
                e.printStackTrace();
            }

        } else {
            System.err.println("Warning UniProt species codes where not found by the validator: " + this.getName());
        }

    }

    @Override
    public ONDEXPluginArguments getArguments() {
        return va;
    }

    @Override
    public Object validate(Object o) {

        // check whether argument is a String or not
        if (o instanceof String) {

            try {
                // lookup org in map
                Entry org = index.get(o.toString().toLowerCase());
                String species = o.toString().toLowerCase();
                while (org == null && species.lastIndexOf(" ") > 0) {
                    species = species.substring(0, species.lastIndexOf(" ")).trim();
                    org = index.get(species);
                }

                // if org was found return taxid
                if (org != null) return org.getId();
            } catch (DatabaseException dbe) {
                fireEventOccurred(new DatabaseErrorEvent(dbe.getMessage(),
                        "[Validator - validate]"));
            }

        }

        // nothing was found or error
        return null;
    }

    @Override
    public void cleanup() {

        index = null;

        // if a database was opened close it
        if (store != null) {
            try {
                store.close();
                store = null;
            } catch (DatabaseException dbe) {
                fireEventOccurred(new DatabaseErrorEvent(dbe.getMessage(),
                        "[Validator - cleanup]"));
            }
        }

        if (myEnv != null) {
            try {
                // Finally, close environment.
                myEnv.close();
                myEnv = null;
            } catch (DatabaseException dbe) {
                fireEventOccurred(new DatabaseErrorEvent(dbe.getMessage(),
                        "[Validator - cleanup]"));
            }
        }
    }

    /**
     * Requires no special arguments.
     */
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        FileArgumentDefinition inputDir = new FileArgumentDefinition(
                FileArgumentDefinition.INPUT_DIR,
                "directory with taxonomy files", true, true, true, false);
        FileArgumentDefinition outputDir = new FileArgumentDefinition(
                FileArgumentDefinition.EXPORT_DIR,
                "temporary directory for index structure", true, true, true,
                false);
        return new ArgumentDefinition<?>[]{inputDir, outputDir};
    }

}
