package net.sourceforge.ondex.parser.glycomedb;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.DatabaseTarget;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.parser.ONDEXParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.zip.GZIPInputStream;

@DatabaseTarget(name = "GlycomeDB", description = "A carbohydrate structure metadatabase", version = "", url = "http://www.glycome-db.org")
@Authors(authors = {"Jan Taubert", "Victor Lesk"}, emails = {"jantaubert at users.sourceforge.net", "v.lesk at imperial.ac.uk"})
@Custodians(custodians = {"Victor Lesk"}, emails = {"v.lesk at imperial.ac.uk"})
public class Parser extends ONDEXParser
{

    public boolean readsDirectory() {
        return false;
    }

    public boolean readsFile() {
        return true;
    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
                FileArgumentDefinition.INPUT_FILE_DESC, true, true, false, false)};
    }

    public String getName() {
        return "GlycomeDB (Legacy version)";
    }

    public String getVersion() {
        return "0.1";
    }

    @Override
    public String getId() {
        return "glycomedb";
    }


    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws Exception {
        File file = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE));
        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));

        boolean inStructure = false;
        boolean inRemoteMapping = false;
        boolean inRemote = false;

        Hashtable<String, String> hashtable = new Hashtable<String, String>();
        Hashtable<String, ONDEXConcept> conceptHashtable = new Hashtable<String, ONDEXConcept>();

        ONDEXGraphMetaData metaData = graph.getMetaData();

        DataSource dataSource = metaData.getFactory().createDataSource("GLYCOMEDB", "glycomedb");
        DataSource ccsd = metaData.getDataSource("CCSD");
        ConceptClass conceptClass = metaData.getFactory().createConceptClass("GLYCOSTRUCTURE", "glycostructure");
        EvidenceType evidenceType = metaData.getEvidenceType("IMPD");
        AttributeName attributeName = metaData.getFactory().createAttributeName("struct", "structure", String.class);

        while (reader.ready()) {
            String lineCapture = reader.readLine().trim();

            if (inStructure) {
                if (lineCapture.equals("\\.")) {
                    inStructure = false;
                } else {
                    String[] array = lineCapture.split("\t");
                    ONDEXConcept c = graph.getFactory().createConcept(array[0], dataSource, conceptClass, evidenceType);

                    c.createAttribute(attributeName, array[1], false);

                    conceptHashtable.put(array[0], c);
                }
            } else if (lineCapture.startsWith("COPY structure ")) {
                inStructure = true;
            } else if (inRemoteMapping) {
                if (lineCapture.equals("\\.")) {
                    inRemoteMapping = false;
                } else {
                    String[] array = lineCapture.split("\t");

                    hashtable.put(array[1], array[2]);

                }
            } else if (lineCapture.startsWith("COPY remote_structure_has_structure ")) {
                inRemoteMapping = true;
            }
        }

        reader.close();
        reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));

        while (reader.ready()) {
            String lineCapture = reader.readLine().trim();

            if (inRemote) {
                if (lineCapture.equals("\\.")) {
                    inRemote = false;
                } else {
                    String[] array = lineCapture.split("\t");

                    if (hashtable.containsKey(array[0])) {
                        ONDEXConcept c = conceptHashtable.get(hashtable.get(array[0]));

                        if (array[1].equals("carbbank")) {
                            c.createConceptAccession(array[2], ccsd, true);
                        }
                    }
                }
            } else if (lineCapture.startsWith("COPY remote_structure ")) {
                inRemote = true;
            }
        }
    }
}
