/*
 * Created on 25-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg52.ko;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.parser.kegg52.MetaData;
import net.sourceforge.ondex.parser.kegg52.Parser;
import net.sourceforge.ondex.parser.kegg52.sink.Concept;
import net.sourceforge.ondex.parser.kegg52.sink.ConceptAcc;
import net.sourceforge.ondex.parser.kegg52.sink.ConceptName;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;


/**
 * @author taubertj
 */
public class KoParser {

    private String pathToKO;

    public KoParser(String pathToKO) {
        this.pathToKO = pathToKO;
    }

    private Map<Concept, Set<String>> koConceptToGenes = new HashMap<Concept, Set<String>>();

    private HashMap<String, Concept> koNamesToKoConcept = new HashMap<String, Concept>();

    private HashMap<String, Concept> koAccessionToKoConcept = new HashMap<String, Concept>();

    /**
     * Parses to ko.txt file and constructs a KO concept for each entry
     *
     * @return Mapping KO concept to containing genes
     */
    public void parse() {
        koConceptToGenes.clear();

        final Pattern spaceSplit = Pattern.compile(" ");
        final Pattern commaSplit = Pattern.compile(",");
        final Pattern colonSplit = Pattern.compile(":");

        try {
            File file = new File(pathToKO);
            BufferedReader reader = null;

            if (file.exists()) {
                reader = new BufferedReader(new FileReader(file));
            } else {
                try {
                    if (!new File(Parser.pathToGenome).exists()) {
                        TarInputStream tis = new TarInputStream(new GZIPInputStream(new FileInputStream(Parser.pathToKegg + File.separator + "genes.tar.gz")));

                        TarEntry entry;
                        while ((entry = tis.getNextEntry()) != null) {
                            String name = entry.getName();
                            if (name.equalsIgnoreCase(file.getName())) {
                                reader = new BufferedReader(new InputStreamReader(tis));
                                break;
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //some state variables, which show the section, we are in
            boolean inDefinition = false;
            boolean inDblinks = false;
            boolean inGenes = false;

            //global variables for concept and org string
            //for multiple line parsing
            Concept concept = null;
            String org = null;
            Set<String> genes = null;
            ArrayList<String> ambiguousName = new ArrayList<String>();
            while (reader.ready()) {
                String line = reader.readLine();
                if (line.length() > 12) {

                    //when line does not begin with blanks reset states
                    if (line.substring(0, 11).trim().length() != 0) {
                        inDefinition = false;
                        inDblinks = false;
                        inGenes = false;
                    }

                    //this starts a new entry resulting in a new concept
                    if (line.startsWith("ENTRY")) {
                        String koAccession = line.substring(12, 18).toUpperCase();
                        concept = new Concept("ko:" + koAccession, MetaData.CV_KEGG, MetaData.CC_KEGG_ONTOLOGY);
                        koAccessionToKoConcept.put(koAccession.toUpperCase(), concept);
                        genes = new HashSet<String>();
                        koConceptToGenes.put(concept, genes);
                    }

                    //this parses a multiline definition
                    else if (inDefinition || line.startsWith("DEFINITION")) {
                        inDefinition = true;
                        if (concept.getDescription() == null) {
                            concept.setDescription(
                                    line.substring(12, line.length()));
                        } else {
                            //append to exsisting description
                            concept.setDescription(concept.getDescription()
                                    + line.substring(12, line.length()));
                        }
                    }
                    //parses the name line into serveral concept names
                    else if (line.startsWith("NAME")) {
                        line = line.substring(12, line.length()).toUpperCase();
                        String[] result = commaSplit.split(line);
                        for (int i = 0; i < result.length; i++) {
                            String name = result[i].trim();
                            ConceptName conceptName = new ConceptName(
                                    concept.getId(), name);
                            if (!koNamesToKoConcept.containsKey(name.toUpperCase()) && !ambiguousName.contains(name.toUpperCase())) {
                                koNamesToKoConcept.put(name.toUpperCase(), concept);
                            } else {
                                ambiguousName.add(name.toUpperCase());
                                koNamesToKoConcept.remove(name.toUpperCase()); //this is an ambiguous term
                            }

                            if (concept.getConceptAccs() == null || concept.getConceptAccs().size() == 0) {
                                //the first name is preffered
                                conceptName.setPreferred(true);
                            }
                            concept.getConceptNames().add(conceptName);
                        }
                    }

                    //this parses multiline db links, one line per link
                    else if (inDblinks || line.startsWith("DBLINKS")) {
                        inDefinition = false;
                        inDblinks = true;
                        line = line.substring(12, line.length());
                        String[] result = colonSplit.split(line);
                        //first comes db name, then list of accs
                        if (result.length == 2) {
                            String db = result[0].trim();
                            String acc = result[1].trim().toUpperCase();
                            String[] accs = spaceSplit.split(acc);
                            for (String access : accs) {

                                ConceptAcc conceptAcc = new ConceptAcc(
                                        concept.getId(), access, db);
                                concept.getConceptAccs().add(conceptAcc);
                            }
                        }
                    }

                    //now the multiline gene part
                    else if (inGenes || line.startsWith("GENES")) {
                        inDblinks = false;
                        inGenes = true;
                        line = line.substring(12, line.length());
                        line = removeBrackets(line);
                        String[] results = colonSplit.split(line);
                        //the org abreviation comes first
                        if (results.length == 2) {
                            org = results[0];
                            line = results[1];
                        }
                        //split into several gene names
                        results = spaceSplit.split(line.trim());
                        for (String gene : results) {
                            //add gene to ko concept
                            String genename = org.toLowerCase() + ":" + gene;
                            genes.add(genename.toUpperCase());
                        }
                    }
                }
            }
            reader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            Parser.propagateEventOccurred(new DataFileErrorEvent(ioe.getMessage(), ""));
        }
    }

    /**
     * This remove everthing within brackets from a string
     *
     * @param s - given string
     * @return resulting string
     */
    private static String removeBrackets(String s) {
        StringBuilder buf = new StringBuilder(s.trim());
        while (buf.indexOf("(") > -1) {
            buf.delete(buf.indexOf("("), buf.indexOf(")") + 1);
            if ((buf.indexOf(")") < buf.indexOf("(")) ||
                    (buf.indexOf("(") == -1 && buf.indexOf(")") > -1))
                buf.delete(buf.indexOf(")"), buf.indexOf(")") + 1);
        }
        return buf.toString();
    }

    public Map<Concept, Set<String>> getKoConceptToGenes() {
        return koConceptToGenes;
    }

    public void finalise() {
        koConceptToGenes.clear();
    }

    public HashMap<String, Concept> getKoNamesToKoConcept() {
        return koNamesToKoConcept;
    }

    public HashMap<String, Concept> getKoAccessionToKoConcept() {
        return koAccessionToKoConcept;
	}

}
