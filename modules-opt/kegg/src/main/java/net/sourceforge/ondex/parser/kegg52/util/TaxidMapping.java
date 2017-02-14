/*
 * Created on 17-May-2005
 *
 */
package net.sourceforge.ondex.parser.kegg52.util;


import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;
import net.sourceforge.ondex.parser.kegg52.Parser;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;


/**
 * @author taubertj
 */
public class TaxidMapping {

    private static final ConcurrentHashMap<String, String> mapping = new ConcurrentHashMap<String, String>();

    private static Pattern patt = Pattern.compile("[\\s|\\t]+");

    public static void createTaxidMapping() {
        mapping.clear();

        BufferedReader in = null;

        File genesDir = new File(Parser.pathToTaxonomy).getParentFile();
        if (!new File(Parser.pathToTaxonomy).exists()) {
            System.err.println("Warning " + genesDir.getAbsolutePath() + " doesn not exist");
        } else {
            System.out.println("Found " + genesDir.getAbsolutePath());
        }


        if (new File(Parser.pathToTaxonomy).exists()) {
            try {
                in = new BufferedReader(
                        new FileReader(Parser.pathToTaxonomy));
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            }
        } else if (new File(Parser.pathToGenome).exists()) {
            try {
                in = new BufferedReader(
                        new FileReader(Parser.pathToGenome));
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            }
        } else {
            System.out.println("Missing: " + Parser.pathToGenome + " or " + Parser.pathToTaxonomy + " looking in " + Parser.pathToKegg + File.separator + "genes.tar.gz");
            try {
                TarInputStream tis = new TarInputStream(new GZIPInputStream(new FileInputStream(Parser.pathToKegg + File.separator + "genes.tar.gz")));
                TarEntry entry;
                while ((entry = tis.getNextEntry()) != null) {
                    String name = entry.getName();
                    if (name.equalsIgnoreCase(new File(Parser.pathToGenome).getName())) {
                        System.out.println("Found: " + new File(Parser.pathToGenome).getName());
                        in = new BufferedReader(new InputStreamReader(tis));
                        break;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String org = "";
        boolean inEntry = false;
        try {
            while (in.ready()) {
                String line = in.readLine().trim();
                if (line.startsWith("ENTRY")) {
                    inEntry = true;
                    String[] array = patt.split(line);
                    org = array[array.length - 1].trim();
                } else if (inEntry && line.startsWith("TAXONOMY")) {
                    String[] array = patt.split(line);
                    String taxid = array[array.length - 1].trim();
                    int colonLoc = taxid.indexOf(':');
                    if (colonLoc > -1) {
                        taxid = taxid.substring(colonLoc + 1, taxid.length());
                    }
                    mapping.put(org.toUpperCase(), taxid);
                } else if (inEntry && line.startsWith("///")) {
                    inEntry = false;
                }
            }
            in.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        System.out.println(mapping.size() + " organism names found");
    }

    public static ConcurrentHashMap<String, String> getMapping() {
        if (mapping.size() == 0) throw new RuntimeException("Mapping empty.");
        return mapping;
    }
}
