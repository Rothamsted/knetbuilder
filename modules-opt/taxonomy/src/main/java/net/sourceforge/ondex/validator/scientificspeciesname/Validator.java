package net.sourceforge.ondex.validator.scientificspeciesname;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.DatabaseErrorEvent;
import net.sourceforge.ondex.validator.AbstractONDEXValidator;


import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import org.apache.log4j.Logger;

/**
 * Implements a species formal scientific name lookup for the NCBI taxonomy.
 *
 * @author taubertj, hindlem, jgrzebyta
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
    private PrimaryIndex<Integer, Entry> index = null;
    private Logger log = Logger.getLogger(getClass());

    /**
     * Returns name of this validator.
     *
     * @return String
     */
    public String getName() {
        return "ScientificSpeciesName";
    }

    /**
     * Returns version of this validator.
     *
     * @return String
     */
    public String getVersion() {
        //return "24.02.2012";
        return "04.12.2012";
    }

    @Override
    public String getId() {
        return "scientificspeciesname";
    }

    /**
     * Gets taxonomy file from FTp directly.
     *
     * @param inputDir
     */
    private void downloadTaxonomy(File inputDir) throws IOException {
        // construct filename
        File file = new File(inputDir.getAbsolutePath() + File.separatorChar
                + "names.dmp");

        if (!file.exists()) {
            log.info(
                    String.format(
                    "Trying to download NCBI Taxonomy from FTP to %s [Validator - downloadTaxonomy]",
                    inputDir.getAbsolutePath()));


            URL url = new URL(
                    "ftp://ftp.ncbi.nih.gov/pub/taxonomy/taxdmp.zip");

            // stream content
            ZipInputStream zis = new ZipInputStream(url.openStream());
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                System.out.println("Unzipping: " + entry.getName());

                int size;
                byte[] buffer = new byte[2048];

                FileOutputStream fos = new FileOutputStream(
                        inputDir.getAbsolutePath() + File.separatorChar
                        + entry.getName());
                BufferedOutputStream bos = new BufferedOutputStream(fos,
                        buffer.length);

                while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
                    bos.write(buffer, 0, size);
                }
                bos.flush();
                bos.close();
            }
            zis.close();
        }
    }

    @Override
    public void setArguments(ONDEXPluginArguments va)
            throws InvalidPluginArgumentException {
        this.va = va;

        /* 
         * I moved the try to line 178. In that way validator can use
         * files downloaded by user
         * 
         */
        //try {
        
        
        EnvironmentConfig myEnvConfig = new EnvironmentConfig();
        StoreConfig storeConfig = new StoreConfig();

        myEnvConfig.setAllowCreate(true);
        storeConfig.setAllowCreate(true);

        File inputDir = new File(
                (String) va
                .getUniqueValue(FileArgumentDefinition.INPUT_DIR));

        // only one global species name validtor ever required
        String s = (String) va
                .getUniqueValue(FileArgumentDefinition.EXPORT_DIR);
        s = s.substring(0, s.indexOf("dbs") + 3) + File.separatorChar
                + s.substring(s.indexOf("validatorsout"), s.length());
        File dir = new File(s);

        // make sure index directory exists
        if (!dir.exists()) {
            dir.mkdirs();
            // try to download for new installation
            try {
                downloadTaxonomy(inputDir);
                log.debug("taxonomy files installed");
            } catch (IOException ioe) {
                log.warn(ioe.getMessage());
                log.warn("Install taxonomy data files yourself in: " + inputDir.getAbsolutePath());
            }
        }

        // Open the environment and entity store
        myEnv = new Environment(dir, myEnvConfig);
        store = new EntityStore(myEnv, "ScientificSpeciesNameEntityStore",
                storeConfig);

        // get index contained in store
        index = store.getPrimaryIndex(Integer.class, Entry.class);

        // override as we use the same data source as taxonomy
        String filename = inputDir.getAbsolutePath() + File.separator
                + "names.dmp";
        log.info(String.format("Using taxonomy flatfile %s [Validator - validate]", filename));
        // FIXME: This is a hack because index.count() seems to be broken.
        log.debug("index size: " + index.map().keySet().size());
        try {
            if (index.map().keySet().isEmpty()) {



                // reader for file
                BufferedReader reader = new BufferedReader(new FileReader(filename));

                // parser taxonomy names.dmp
                while (reader.ready()) {
                    String line = reader.readLine();
                    String[] split = line.split("\t\\|\t");
                    String type = split[3].toLowerCase().trim()
                            .substring(0, split[3].length() - 1).trim();
                    if (type.equals("scientific name")) {
                        String name = split[1].toLowerCase().trim();
                        index.put(new Entry(Integer.valueOf(split[0]), name));
                    }
                }

                // close reader
                reader.close();
            }

        } catch (DatabaseException dbe) {
            fireEventOccurred(new DatabaseErrorEvent(dbe.getMessage(),
                    "[Validator - validate]"));
        } catch (FileNotFoundException fnfe) {
            fireEventOccurred(new DataFileMissingEvent(fnfe.getMessage(),
                    "[Validator - validate]"));
        } catch (IOException ioe) {
            fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
                    "[Validator - validate]"));
        }
    }

    @Override
    public ONDEXPluginArguments getArguments() {
        return va;
    }

    @Override
    public Object validate(Object o) {
        if (log.isDebugEnabled()) {
            log.debug("validate object: %s" + o.toString());
            log.debug("object class: " + o.getClass().getSimpleName());
        }
        // check whether argument is a String or not
        if (o instanceof String || o instanceof Number) {

            Integer taxid;
            try {
                taxid = Integer.parseInt(o.toString());
            } catch (NumberFormatException nfe) {
                // this seems to be already an organism name
                return o;
            }

            try {
                // lookup org in map
                log.debug(String.format("tax id: '%d'", taxid));
                Entry org = index.get(taxid);
                log.debug("Entry value: " + org);

                // if org was found return taxid
                if (org != null) {
                    return org.getName();
                }
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
