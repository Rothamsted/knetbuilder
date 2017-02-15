package net.sourceforge.ondex.parser.drastic;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.config.ValidatorRegistry;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import org.apache.log4j.Level;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Drastic Parser
 * for the file: "Drastic Data.csv"
 * from http://www.scri.sari.ac.uk/TiPP/PPS/DRASTIC/mpage/downloads.asp
 * <p/>
 * latest tested version: 16/01/07
 *
 * @author rwinnenb
 */
public class Parser extends ONDEXParser
{

    private EntityCollection eCollection;

    public String getName() {
        return new String("Drastic");
    }

    public String getVersion() {
        return new String("16.01.2007");
    }

    @Override
    public String getId() {
        return "drastic";
    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
    	return new ArgumentDefinition<?>[] { new FileArgumentDefinition(
				FileArgumentDefinition.INPUT_FILE,
				"DRASTIC data CSV file to import", true, true, false, false) };
    }


    public void start() throws InvalidPluginArgumentException {
    	File file = new File((String) args
				.getUniqueValue(FileArgumentDefinition.INPUT_FILE));
        eCollection = new EntityCollection();
     
        /*
           * set attribute columns
           */
        final int C_ANNO = 4;
        final int C_NAME = 5;
        final int C_ACC = 6;
        final int C_AGI = 37;
        final int C_EC = 36;
        final int C_REG = 7;
        final int C_SPE1 = 11;
        final int C_SPE2 = 12;
        final int TREAT = 14;
        final int DESCR = 15;
        final int COMP = 3;
        final int REF = 2;

        try {

            //filename of Drastic flat file
            

            GeneralOutputEvent so = new GeneralOutputEvent("Using drastic flatfile " + file.getAbsolutePath(), getCurrentMethodName());
            so.setLog4jLevel(Level.INFO);
            fireEventOccurred(so);

            /*
                * parsing
                */
            BufferedReader in = new BufferedReader(new FileReader(file));
            String inputline = in.readLine();

            // for every line in the database flat file
            while ((inputline = in.readLine()) != null) {

                String[] draIN = parseLine(inputline);
                String[] accs = {draIN[C_ACC], draIN[C_AGI], draIN[C_EC]};

                DraEntity de_tmp = new DraEntity();

                de_tmp.setAnnotation(draIN[C_ANNO]);
                de_tmp.setReg(draIN[C_REG]);
                de_tmp.setSpecies(draIN[C_SPE1], draIN[C_SPE2]);
                de_tmp.setAccessions(accs);
                de_tmp.setGeneName(draIN[C_NAME]);
                de_tmp.setTreatment(draIN[TREAT]);
                de_tmp.setTreatmentDescription(draIN[DESCR]);
                de_tmp.setCompatibility(draIN[COMP]);
                de_tmp.setRefID(Integer.parseInt(draIN[REF]));

                //add current object to the entity vector
                eCollection.add(de_tmp);

            }
            in.close();

            /*
                * write DRA_CONCEPT (genes and treatments);
                */
            DataSource cvdra = graph.getMetaData().getDataSource("DRA");
            AttributeName antaxid = graph.getMetaData().getAttributeName("TAXID");
            EvidenceType et = graph.getMetaData().getEvidenceType("IMPD");

            //Iterator for entity collection
            Iterator<Concept> it = eCollection.getConcepts().iterator();

            //keep track of assigned automated IDs
            HashMap<String, Integer> idMapping = new HashMap<String, Integer>();

            while (it.hasNext()) {

                Concept currentConcept = it.next();

                //**write concept**
                String cID = "DRA_" + currentConcept.getID();
                String annotation = currentConcept.getDescription();
                String conceptClass = currentConcept.getConceptClass();

                ConceptClass cc = graph.getMetaData().getConceptClass(conceptClass);
                ONDEXConcept c = graph.getFactory().createConcept(cID, annotation, annotation, cvdra, cc, et);
                idMapping.put(cID, c.getId());

                //**set Taxonomy ID**
                String taxid = null;
                if (conceptClass.equals("Treatment")) {
                    taxid = (String) ValidatorRegistry.validators.get("taxonomy").validate(currentConcept.getKey().toLowerCase());
                } else {
                    taxid = currentConcept.getTaxID();
                }

                if (taxid != null) {
                    c.createAttribute(antaxid, taxid, false);
                }

                //**write Accessions**
                Iterator<String> it_acc = currentConcept.getAccs();
                while (it_acc.hasNext()) {

                    String acc = it_acc.next();

                    DataSource accdataSource = null;
                    String cvName = (String) ValidatorRegistry.validators.get("cvregex").validate(acc);
                    if (cvName != null) {
                        accdataSource = graph.getMetaData().getDataSource(cvName);
                        if (accdataSource == null) {
                            fireEventOccurred(new DataSourceMissingEvent("Cannot find " + cvName, getCurrentMethodName()));
                        }
                    }
                    if (accdataSource == null) {
                        fireEventOccurred(new GeneralOutputEvent("Cannot find " + cvName, getCurrentMethodName()));
                    } else {
                        c.createConceptAccession(acc, accdataSource, false);
                    }
                }

                //**write gene names**
                String gname = null;
                if (!(gname = currentConcept.getPrefName()).equals(""))
                    c.createConceptName(gname, true);

                Iterator<String> it_names;
                if ((it_names = currentConcept.getNames()) != null) {

                    while (it_names.hasNext()) {

                        String tmp = it_names.next();
                        c.createConceptName(tmp, false);
                    }
                }
            }

            /*
                * write DRA_RELATIONS AND GDS_RELATION
                */
            RelationType rt = graph.getMetaData().getRelationType("pr_by");
            AttributeName attr = graph.getMetaData().getAttributeName("REG");

            RelationLogger rlg = new RelationLogger();

            Iterator<Relation> it_rel = eCollection.getRelations().iterator();

            while (it_rel.hasNext()) {

                Relation currentRelation = it_rel.next();

                Integer fromId = idMapping.get("DRA_" + currentRelation.getFrom());
                Integer toId = idMapping.get("DRA_" + currentRelation.getTo());
                ONDEXConcept from = graph.getConcept(fromId);
                ONDEXConcept to = graph.getConcept(toId);

                if (rlg.checkInRelation(from, to)) {
                    ONDEXRelation r = graph.getFactory().createRelation(from, to, rt, et);
                    r.createAttribute(attr, currentRelation.getRegulation(), false);
                }
            }
        } catch (IOException io) {

            io.printStackTrace();
        }
    }

    /*
      * reads one line of flat file and returns all columns in an Array of Strings
      */

    private static String[] parseLine(String inputline) {
        boolean eol = false;
        int pointer = 0;
        String[] draIN = new String[50];
        String fString = "";

        while (!eol) {

            // check, if there are single quotes
            if (inputline.startsWith("\'")) {

                // if single quotes get everything up to next single
                // quote
                // also commas!
                inputline = inputline.substring(1);// get rid of single
                // quote
                draIN[pointer] = inputline.substring(0, inputline
                        .indexOf("\'"));
                inputline = inputline
                        .substring(inputline.indexOf("\'") + 1);
            } else {

                // if no single quotes get everything up to next comma
                draIN[pointer] = inputline.substring(0, inputline
                        .indexOf(","));
                inputline = inputline
                        .substring(inputline.indexOf(","));
            }

            // if next one is a comma, then next entry otherwise end of
            // line
            if (inputline.indexOf(",") > -1) {

                //get rid of comma
                inputline = inputline.substring(inputline.indexOf(",") + 1);

                pointer++;
            } else {

                eol = true;
            }
        }

        String[] draIN2 = new String[pointer];

        for (int i = 0; i < pointer; i++) {

            draIN2[i] = draIN[i];
            fString = fString + draIN2[i] + "\t";
        }
        return draIN2;
    }

    @Override
    public String[] requiresValidators() {
        return new String[]{"cvregex", "taxonomy"};
    }

    /**
     * Convenience method for outputing the current method name in a dynamic way
     *
     * @return the calling method name
     */
    public static String getCurrentMethodName() {
        Exception e = new Exception();
        StackTraceElement trace = e.fillInStackTrace().getStackTrace()[1];
        String name = trace.getMethodName();
        String className = trace.getClassName();
        int line = trace.getLineNumber();
        return "[CLASS:" + className + " - METHOD:" + name + " LINE:" + line + "]";
    }
}

